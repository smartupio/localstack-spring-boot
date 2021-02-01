package io.smartup.localstack.configurator;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;

public class AmazonSNSConfigurator extends AbstractAmazonClientConfigurator<AmazonSNS> {
    @Override
    public Class<AmazonSNS> getAmazonClientClass() {
        return AmazonSNS.class;
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
