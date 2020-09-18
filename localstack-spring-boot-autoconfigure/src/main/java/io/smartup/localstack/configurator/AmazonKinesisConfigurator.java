package io.smartup.localstack.configurator;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesis.AmazonKinesis;

public class AmazonKinesisConfigurator extends AbstractAmazonClientConfigurator<AmazonKinesis> {
    @Override
    public Class<AmazonKinesis> getAmazonClientClass() {
        return AmazonKinesis.class;
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
