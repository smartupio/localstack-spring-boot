package io.smartup.oss.localstack;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import io.smartup.oss.concurrency.FileBasedMutex;
import io.smartup.oss.concurrency.FileBasedSharedLock;
import io.smartup.oss.configurator.LocalStackConfigurator;
import io.smartup.oss.docker.DockerService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnProperty(prefix = "localstack", name = "enabled", havingValue = "true")
@Import(LocalStackConfigurator.class)
public class LocalStackAutoConfiguration {
    @Bean
    public DockerClient dockerClient() throws DockerCertificateException {
        return DefaultDockerClient.fromEnv().build();
    }

    @Bean
    public FileBasedSharedLock fileBasedSharedLock() {
        return new FileBasedSharedLock(".lstck_lck");
    }

    @Bean
    public FileBasedMutex fileBasedMutex() {
        return new FileBasedMutex(".lstck_mtx");
    }

    @Bean
    public DockerService dockerService(DockerClient dockerClient) {
        return new DockerService(dockerClient);
    }

    @Bean
    public LocalStackService localStackService(FileBasedMutex fileBasedMutex,
                                               FileBasedSharedLock fileBasedSharedLock,
                                               DockerService dockerService) {
        return new LocalStackService(fileBasedMutex, fileBasedSharedLock, dockerService);
    }

    @Bean
    public LocalStackApplicationListener localStackApplicationListener(LocalStackService localStackService) {
        return new LocalStackApplicationListener(localStackService);
    }
}
