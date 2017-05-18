package io.smartup.cloud.docker;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.ProgressDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper service for the DockerClient class
 *
 * @see com.spotify.docker.client.DockerClient
 */
public class DockerService {
    private static final Logger LOG = LoggerFactory.getLogger(DockerService.class);
    private final DockerClient dockerClient;

    public DockerService(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    /**
     * Creates a container with given configuration and name.
     *
     * @param containerConfig configuration of the container
     * @param name name of the container
     * @return created container
     *
     * @throws DockerServiceException if the container could not be created
     *
     * @see Container
     */
    public Container createContainer(ContainerConfig containerConfig, String name) {
        try {
            ContainerCreation containerCreation = dockerClient.createContainer(containerConfig, name);

            return getContainerById(containerCreation.id(), false)
                    .orElseThrow(() -> new DockerServiceException("Could not create container"));
        } catch (DockerException|InterruptedException e) {
            throw new DockerServiceException(e);
        }
    }

    /**
     * Starts the received container.
     * If the container is already running nothing will happen.
     *
     * @param container container to be started
     * @throws DockerServiceException if the startup failed
     */
    public void startContainer(Container container) {
        try {
            LOG.info("Checking if container is running...");
            Optional<Container> containerExists = getContainerById(container.id(), true);
            if (! containerExists.isPresent()) {
                LOG.info("Starting the container");
                dockerClient.startContainer(container.id());
            }
            LOG.info("Container is running");
        } catch (DockerException | InterruptedException e) {
            throw new DockerServiceException(e);
        }
    }

    /**
     * Stops the received container.
     *
     * @param container container to be stopped
     * @param timeout timeout before the command kills the container
     * @throws DockerServiceException if shutdown failed
     */
    public void stopContainer(Container container, int timeout) {
        try {
            dockerClient.stopContainer(container.id(), timeout);
        } catch (DockerException | InterruptedException e) {
            throw new DockerServiceException(e);
        }
    }

    /**
     * Pull Docker image from DockerHub
     *
     * @param image name of the image
     * @throws DockerServiceException if pull failed
     */
    public void pullDockerImage(String image) {
        try {
            System.out.println("Pulling docker image");
            dockerClient.pull(image, (progressMessage) -> {
                ProgressDetail progressDetail = progressMessage.progressDetail();
                printProgress(
                        progressDetail.start(),
                        progressDetail.current(),
                        progressDetail.total()
                );
            });
        } catch (DockerException | InterruptedException e) {
            throw new DockerServiceException(e);
        }
    }

    /**
     * Find container by its id
     *
     * @param containerId id of the container
     * @param running find between running containers or all
     * @return optional container
     * @throws DockerServiceException if search failed
     *
     * @see Container
     */
    public Optional<Container> getContainerById(String containerId, boolean running) {
        try {
            return dockerClient
                    .listContainers(DockerClient.ListContainersParam.allContainers(!running))
                    .stream()
                    .filter(c -> c.id().equals(containerId))
                    .findAny();
        } catch (DockerException | InterruptedException e) {
            throw new DockerServiceException(e);
        }
    }

    /**
     * Find container by its name
     *
     * @param containerName name of the container
     * @param running find between running containers or all
     * @return optional container
     * @throws DockerServiceException if search failed
     *
     * @see Container
     */
    public Optional<Container> getContainerByName(String containerName, boolean running) {
        try {
            return dockerClient
                    .listContainers(DockerClient.ListContainersParam.allContainers(!running))
                    .stream()
                    .filter(c -> containsLike(containerName, c.names()))
                    .findAny();
        } catch (DockerException | InterruptedException e) {
            throw new DockerServiceException(e);
        }
    }

    /**
     * Find image by its name
     *
     * @param imageName name of the image
     * @return optional image
     * @throws DockerServiceException if search failed
     *
     * @see Image
     */
    public Optional<Image> getImageByName(String imageName) {
        try {
            return dockerClient
                    .listImages()
                    .stream()
                    .filter(i -> containsLike(imageName, i.repoTags()))
                    .findAny();
        } catch (DockerException | InterruptedException e) {
            throw new DockerServiceException(e);
        }
    }

    /**
     * Remove the received container
     *
     * @param container container to be removed
     * @throws DockerServiceException if removal failed
     *
     * @see Container
     */
    public void removeContainer(Container container) {
        try {
            dockerClient.removeContainer(container.id());
        } catch (DockerException|InterruptedException e) {
            throw new DockerServiceException(e);
        }
    }

    private static void printProgress(long startTime, long total, long current) {
        long eta = current == 0 ? 0 :
                (total - current) * (System.currentTimeMillis() - startTime) / current;

        String etaHms = current == 0 ? "N/A" :
                String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(eta),
                        TimeUnit.MILLISECONDS.toMinutes(eta) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(eta) % TimeUnit.MINUTES.toSeconds(1));

        StringBuilder string = new StringBuilder(140);
        int percent = (int) (current * 100 / total);
        string
                .append('\r')
                .append(String.join("", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), " ")))
                .append(String.format(" %d%% [", percent))
                .append(String.join("", Collections.nCopies(percent, "=")))
                .append('>')
                .append(String.join("", Collections.nCopies(100 - percent, " ")))
                .append(']')
                .append(String.join("", Collections.nCopies((int) (Math.log10(total)) - (int) (Math.log10(current)), " ")))
                .append(String.format(" %d/%d, ETA: %s", current, total, etaHms));

        System.out.print(string);
    }

    private boolean containsLike(String value, Collection<String> values) {
        return values.stream().anyMatch(v -> v.contains(value));
    }
}
