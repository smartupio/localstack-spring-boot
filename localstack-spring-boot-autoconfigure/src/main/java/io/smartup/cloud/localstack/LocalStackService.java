package io.smartup.cloud.localstack;

import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.PortBinding;
import io.smartup.cloud.docker.DockerService;
import io.smartup.cloud.utils.FileBasedCounter;
import io.smartup.cloud.utils.FileBasedMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * LocalStackService provides methods for starting and stopping the LocalStack docker container.
 *
 * These methods will be synchronized between multiple JVM processes using FileBasedCounter
 * and FileBasedMutex.
 *
 * @see FileBasedCounter
 * @see FileBasedMutex
 */
public class LocalStackService {
    private static final String LOCALSTACK_IMAGE = "ambrusadrianz/localstack-mirror";
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
            dockerService.startContainer(container);
            fileBasedCounter.increment();
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
        int currentValue = fileBasedCounter.decrement();

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
        Map<String, List<PortBinding>> portBindings = new HashMap<>();

        Set<String> exposedPorts = generateExposedPorts();

        exposedPorts.forEach(s -> portBindings.put(s, Collections.singletonList(PortBinding.of("localhost", s))));

        HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

        return ContainerConfig.builder()
                .image(LOCALSTACK_IMAGE)
                .hostConfig(hostConfig)
                .exposedPorts(exposedPorts)
                .build();
    }

    private Set<String> generateExposedPorts() {
        Set<String> exposedPorts = new HashSet<>();

        for (int i = 4565; i < 4578; i++) {
            exposedPorts.add(String.valueOf(i));
        }

        return exposedPorts;
    }
}
