# Smartup LocalStack Spring Boot Autoconfiguration

## Introduction

The scope of this autoconfiguration library is to
provide a simple way for mocking AWS services on a local machine.

This could be beneficial for dev and test purposes.

## Requirements

To use this autoconfiguration you need **Docker 1.6+** and **JDK8**.

## Usage

* First you have to include *localstack-spring-boot-starter* dependency in your project
* Next you'll need to annotate your SpringBootApplication class with *@EnableLocalStack*
* Then you can configure the AutoConfiguration from your application.properties, or application.yml file

## Configuration

You need to make sure that the profile you want to use LocalStack in includes the following configuration:

```yaml
    localstack:
      enabled: true
      api-gateway:
        enabled: true
      kinesis:
        enabled: true
      dynamodb:
        enabled: true
      dynamodb-streams:
        enabled: true
      elasticsearch:
        enabled: true
      s3:
        enabled: true
      firehose:
        enabled: true
      lambda:
        enabled: true
      sns:
        enabled: true
      sqs:
        enabled: true
      redshift:
        enabled: true
```

The *localstack.enabled* config is mandatory, you must specify this.
The services that are going to be used must be specified, otherwise they won't be enabled.

**Note:**
If a service is not enabled that does not mean that the service won't run, it just means that it
won't be configured.

## How it works

**Smartup LocalStack Autoconfiguration** works by pulling and starting the *atlassianlabs/localstack* 
Docker image and running a container on application startup. 

The configuration of the *AmazonWebServiceClients'* present in your project will be 
overwritten if the configuration is enabled.

If multiple services are using the Docker container then the last one that shuts down will close the
docker container.