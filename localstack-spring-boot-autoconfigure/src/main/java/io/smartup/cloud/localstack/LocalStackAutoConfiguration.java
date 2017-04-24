package io.smartup.cloud.localstack;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import io.smartup.cloud.docker.DockerService;
import io.smartup.cloud.concurrency.FileBasedCounter;
import io.smartup.cloud.concurrency.FileBasedMutex;
import io.smartup.cloud.configurator.LocalStackConfigurator;
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
    public FileBasedCounter fileBasedCounter() {
        return new FileBasedCounter(".lstck_cnt");
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
    public LocalStackService localStackService(FileBasedMutex fileBasedMutex, FileBasedCounter fileBasedCounter, DockerService dockerService) {
        return new LocalStackService(fileBasedMutex, fileBasedCounter, dockerService);
    }

    @Bean
    public LocalStackApplicationListener localStackApplicationListener(LocalStackService localStackService) {
        return new LocalStackApplicationListener(localStackService);
    }
}
