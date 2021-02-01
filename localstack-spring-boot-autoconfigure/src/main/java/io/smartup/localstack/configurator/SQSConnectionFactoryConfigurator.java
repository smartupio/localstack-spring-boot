package io.smartup.localstack.configurator;

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
        return getLocalStackHost();
    }

    @Override
    public Region getRegion() {
        return Region.getRegion(Regions.DEFAULT_REGION);
    }

    @Override
    protected SQSConnectionFactory setup() {
        return getBean();
    }
}
