package io.smartup.cloud.configurator;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;

public class AmazonSQSConfigurator extends AbstractAmazonClientConfigurator<AmazonSQS> {
    @Override
    public Class<AmazonSQS> getAmazonClientClass() {
        return AmazonSQS.class;
    }

    @Override
    public String getEndpoint() {
        return "http://localhost:4576";
    }

    @Override
    public Region getRegion() {
        return Region.getRegion(Regions.DEFAULT_REGION);
    }
}