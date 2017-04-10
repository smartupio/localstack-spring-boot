package io.smartup.cloud.localstack;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ConfigurationProperties("localstack")
public class LocalStackProperties {
    public static final String HOST = "localhost";

    private boolean enabled;
    private ApiGateway apiGateway = new ApiGateway();
    private Kinesis kinesis = new Kinesis();
    private DynamoDB dynamodb = new DynamoDB();
    private DynamoDBStreams dynamodbStreams = new DynamoDBStreams();
    private ElasticSearch elasticsearch = new ElasticSearch();
    private S3 s3 = new S3();
    private Firehose firehose = new Firehose();
    private Lambda lambda = new Lambda();
    private SNS sns = new SNS();
    private SQS sqs = new SQS();
    private Redshift redshift = new Redshift();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ApiGateway getApiGateway() {
        return apiGateway;
    }

    public Kinesis getKinesis() {
        return kinesis;
    }

    public DynamoDB getDynamodb() {
        return dynamodb;
    }

    public DynamoDBStreams getDynamodbStreams() {
        return dynamodbStreams;
    }

    public ElasticSearch getElasticsearch() {
        return elasticsearch;
    }

    public S3 getS3() {
        return s3;
    }

    public Firehose getFirehose() {
        return firehose;
    }

    public Lambda getLambda() {
        return lambda;
    }

    public SNS getSns() {
        return sns;
    }

    public SQS getSqs() {
        return sqs;
    }

    public Redshift getRedshift() {
        return redshift;
    }

    public List<Service> getAllServices() {
        List<Service> allServices = new ArrayList<>();
        allServices.add(apiGateway);
        allServices.add(kinesis);
        allServices.add(dynamodb);
        allServices.add(dynamodbStreams);
        allServices.add(elasticsearch);
        allServices.add(s3);
        allServices.add(firehose);
        allServices.add(lambda);
        allServices.add(sns);
        allServices.add(sqs);
        allServices.add(redshift);
        return allServices;
    }

    public List<Service> getEnabledServices() {
        return getAllServices().stream().filter(s -> s.enabled).collect(Collectors.toList());
    }

    public static abstract class Service {
        private String dockerPort;
        private boolean enabled = false;

        public String getDockerPort() {
            return dockerPort;
        }

        public void setDockerPort(String dockerPort) {
            this.dockerPort = dockerPort;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class ApiGateway extends Service {
        public ApiGateway() {
            this.setDockerPort("4567");
        }
    }

    public static class Kinesis extends Service {
        public Kinesis() {
            this.setDockerPort("4568");
        }
    }

    public static class DynamoDB extends Service {
        public DynamoDB() {
            this.setDockerPort("4569");
        }
    }

    public static class DynamoDBStreams extends Service {
        public DynamoDBStreams() {
            this.setDockerPort("4570");
        }
    }

    public static class ElasticSearch extends Service {
        public ElasticSearch() {
            this.setDockerPort("4571");
        }
    }

    public static class S3 extends Service {
        public S3() {
            this.setDockerPort("4572");
        }
    }

    public static class Firehose extends Service {
        public Firehose() {
            this.setDockerPort("4573");
        }
    }

    public static class Lambda extends Service {
        public Lambda() {
            this.setDockerPort("4574");
        }
    }

    public static class SNS extends Service {
        public SNS() {
            this.setDockerPort("4575");
        }
    }

    public static class SQS extends Service {
        public SQS() {
            this.setDockerPort("4576");
        }
    }

    public static class Redshift extends Service {
        public Redshift() {
            this.setDockerPort("4577");
        }
    }
}
