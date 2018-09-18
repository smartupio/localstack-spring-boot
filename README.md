# Smartup LocalStack Spring Boot AutoConfiguration

## Introduction

The scope of this AutoConfiguration library is to
provide a simple way for configuring AWS clients with localstack.

## Requirements

To use this AutoConfiguration you need **JDK8**.

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
      ssm:
        enabled: true
      lambda:
        enabled: true
```

The AutoConfiguration now supports `SSL`, in order to use it set the `localstack.use-ssl` flag to `true`.

The *localstack.enabled* config is mandatory, you must specify this.

The services that are going to be used must be specified, otherwise they won't be enabled.

## How it works

The configuration of the *AmazonWebServiceClients'* present in your project will be 
overwritten if the configuration is enabled.
