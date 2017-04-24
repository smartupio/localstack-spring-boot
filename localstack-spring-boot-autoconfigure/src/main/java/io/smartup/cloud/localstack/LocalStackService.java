package io.smartup.cloud.localstack;

import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.PortBinding;
import io.smartup.cloud.concurrency.FileBasedCounter;
import io.smartup.cloud.concurrency.FileBasedMutex;
import io.smartup.cloud.docker.DockerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * LocalStackService provides methods for starting and stopping the LocalStack docker container.
 * <p>
 * These methods will be synchronized between multiple JVM processes using FileBasedCounter
 * and FileBasedMutex.
 *
 * @see FileBasedCounter
 * @see FileBasedMutex
 */
public class LocalStackService {
    private static final String LOCALSTACK_IMAGE = "atlassianlabs/localstack:0.3.11";
    private static final String LOCALSTACK_CONTAINER = "localstack-smartup";

    private static final Logger LOG = LoggerFactory.getLogger(LocalStackService.class);

    private final FileBasedMutex fileBasedMutex;
    private final FileBasedCounter fileBasedCounter;
    private final DockerService dockerService;

    public LocalStackService(FileBasedMutex fileBasedMutex,
                             FileBasedCounter fileBasedCounter,
                             DockerService dockerService) {
        this.fileBasedMutex = fileBasedMutex;
        this.fileBasedCounter = fileBasedCounter;
        this.dockerService = dockerService;
    }

    /**
     * The method will check if the LocalStack image exists on the machine.
     * If it does not exist, it will pull the image from DockerHub.
     * <p>
     * If the container does not exist, a container will be created.
     * If the container is already started, it won't start a new container, otherwise the
     * container will be started.
     */
    public void start() {
        fileBasedMutex.lock();
        LOG.info("Checking if LocalStack image exists...");
        try {
            Optional<Image> imageOptional = dockerService.getImageByName(LOCALSTACK_IMAGE);

            if (!imageOptional.isPresent()) {
                LOG.info("Pulling LocalStack image");
                dockerService.pullDockerImage(LOCALSTACK_IMAGE);
            }

            LOG.info("Checking if container exists...");
            Optional<Container> containerOptional = dockerService.getContainerByName(LOCALSTACK_CONTAINER, false);

            Container container = containerOptional.orElseGet(this::createLocalStackContainer);

            if (container.state().equalsIgnoreCase("created") ||
                    container.state().equalsIgnoreCase("exited")) {
                dockerService.startContainer(container);
            }

            fileBasedCounter.incrementAndGet();

        } finally {
            fileBasedMutex.release();
        }
    }

    /**
     * The method will check if the container is running, if it's running and this is the last
     * service that is using the container, the container will be stopped.
     */
    public void stop() {
        fileBasedMutex.lock();
        int currentValue = fileBasedCounter.decrementAndGet();

        LOG.info("The number of services using the container: {}", currentValue);
        Optional<Container> containerRuns = dockerService.getContainerByName(LOCALSTACK_CONTAINER, true);

        if (currentValue == 0 && containerRuns.isPresent()) {
            LOG.info("Stopping the container...");
            try {
                dockerService.stopContainer(containerRuns.get(), 30);
            } finally {
                fileBasedCounter.destroy();
                fileBasedMutex.destroy();
            }
        } else {
            fileBasedCounter.close();
            fileBasedMutex.close();
        }
    }

    private Container createLocalStackContainer() {
        ContainerConfig containerConfig = createLocalStackContainerConfig();
        return dockerService.createContainer(containerConfig, LOCALSTACK_CONTAINER);
    }

    private ContainerConfig createLocalStackContainerConfig() {
        Set<String> exposedPorts = generateExposedPorts();

        Map<String, List<PortBinding>> portBindings =
                exposedPorts.stream()
                        .collect(
                                Collectors.toMap(
                                        Function.identity(),
                                        this::createPortBinding
                                )
                        );

        HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

        return ContainerConfig.builder()
                .image(LOCALSTACK_IMAGE)
                .hostConfig(hostConfig)
                .exposedPorts(exposedPorts)
                .build();
    }

    private List<PortBinding> createPortBinding(String port) {
        return Collections.singletonList(PortBinding.of("localhost", port));
    }

    private Set<String> generateExposedPorts() {
        return IntStream.range(4567, 4582)
                .mapToObj(String::valueOf)
                .collect(Collectors.toSet());
    }
}
