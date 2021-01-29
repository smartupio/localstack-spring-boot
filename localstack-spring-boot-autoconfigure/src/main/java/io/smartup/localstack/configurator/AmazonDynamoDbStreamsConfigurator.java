package io.smartup.localstack.configurator;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBStreams;

public class AmazonDynamoDbStreamsConfigurator extends AbstractAmazonClientConfigurator<AmazonDynamoDBStreams> {
    @Override
    public Class<AmazonDynamoDBStreams> getAmazonClientClass() {
        return AmazonDynamoDBStreams.class;
    }

    @Override
    public String getEndpoint() {
        return getLocalStackHost();
    }

    @Override
    public Region getRegion() {
        return Region.getRegion(Regions.DEFAULT_REGION);
    }
}
