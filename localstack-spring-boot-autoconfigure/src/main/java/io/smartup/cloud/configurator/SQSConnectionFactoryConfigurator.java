package io.smartup.cloud.configurator;

import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

public class SQSConnectionFactoryConfigurator extends AbstractAmazonClientConfigurator<SQSConnectionFactory> {
    @Override
    public Class<SQSConnectionFactory> getAmazonClientClass() {
        return SQSConnectionFactory.class;
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
