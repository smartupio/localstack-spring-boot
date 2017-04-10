package io.smartup.cloud.localstack;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import io.smartup.cloud.docker.DockerService;
import io.smartup.cloud.utils.FileBasedCounter;
import io.smartup.cloud.utils.FileBasedMutex;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LocalStackProperties.class)
@ConditionalOnProperty(prefix = "localstack", name = "enabled", havingValue = "true")
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
    public LocalStackBeanPostProcessor localStackBeanPostProcessor(LocalStackProperties localStackProperties) {
        return new LocalStackBeanPostProcessor(localStackProperties);
    }

    @Bean
    public DockerService dockerService(DockerClient dockerClient) {
        return new DockerService(dockerClient);
    }

    @Bean
    public LocalStackService localStackService(FileBasedMutex fileBasedMutex, FileBasedCounter fileBasedCounter,
                                               LocalStackProperties localStackProperties, DockerService dockerService) {
        return new LocalStackService(fileBasedMutex, fileBasedCounter, localStackProperties, dockerService);
    }

    @Bean
    public LocalStackApplicationListener localStackApplicationListener(LocalStackService localStackService) {
        return new LocalStackApplicationListener(localStackService);
    }
}
