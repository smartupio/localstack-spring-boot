package io.smartup.localstack.configurator;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;

public class AmazonDynamoDbConfigurator extends AbstractAmazonClientConfigurator<AmazonDynamoDB> {
    @Override
    public Class<AmazonDynamoDB> getAmazonClientClass() {
        return AmazonDynamoDB.class;
    }

    @Override
    public String getEndpoint() {
        return "http://" + getLocalStackHost() + ":4569";
    }

    @Override
    public Region getRegion() {
        return Region.getRegion(Regions.DEFAULT_REGION);
    }
}
