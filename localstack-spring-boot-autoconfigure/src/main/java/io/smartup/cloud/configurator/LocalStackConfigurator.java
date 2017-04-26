package io.smartup.cloud.configurator;

import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.sns.AmazonSNS;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "localstack", name = "enabled", havingValue = "true")
public class LocalStackConfigurator {
    @Configuration
    @ConditionalOnProperty(prefix = "localstack", name = "s3.enabled", havingValue = "true")
    @ConditionalOnClass(AmazonS3.class)
    public static class AmazonS3Configuration {
        @Bean
        public AmazonS3Configurator amazonS3Configurator() {
            return new AmazonS3Configurator();
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "localstack", name = "sqs.enabled", havingValue = "true")
    public static class AmazonSQSConfiguration {
        @Bean
        public AmazonSQSConfigurator amazonSQSConfigurator() {
            return new AmazonSQSConfigurator();
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "localstack", name = "dynamodb.enabled", havingValue = "true")
    @ConditionalOnBean(AmazonDynamoDB.class)
    public static class AmazonDynamoDbConfiguration {
        @Bean
        public AmazonDynamoDbConfigurator amazonDynamoDbConfigurator() {
            return new AmazonDynamoDbConfigurator();
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "localstack", name = "sns.enabled", havingValue = "true")
    @ConditionalOnBean(AmazonSNS.class)
    public static class AmazonSNSConfiguration {
        @Bean
        public AmazonSNSConfigurator amazonSNSConfigurator() {
            return new AmazonSNSConfigurator();
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "localstack", name = "route53.enabled", havingValue = "true")
    @ConditionalOnBean(AmazonRoute53.class)
    public static class AmazonRoute53Configuration {
        @Bean
        public AmazonRoute53Configurator amazonRoute53Configurator() {
            return new AmazonRoute53Configurator();
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "localstack", name = "ses.enabled", havingValue = "true")
    @ConditionalOnBean(AmazonSimpleEmailService.class)
    public static class AmazonSESConfiguration {
        @Bean
        public AmazonSESConfigurator amazonSESConfigurator() {
            return new AmazonSESConfigurator();
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "localstack", name = "sqs.enabled", havingValue = "true")
    @ConditionalOnBean(SQSConnectionFactory.class)
    public static class SQSConnectionFactoryConfiguration {
        @Bean
        public SQSConnectionFactoryConfigurator sqsConnectionFactoryConfigurator() {
            return new SQSConnectionFactoryConfigurator();
        }
    }
}
