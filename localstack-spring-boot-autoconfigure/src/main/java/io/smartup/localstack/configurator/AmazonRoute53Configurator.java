package io.smartup.localstack.configurator;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.route53.AmazonRoute53;

public class AmazonRoute53Configurator extends AbstractAmazonClientConfigurator<AmazonRoute53> {
    @Override
    public Class<AmazonRoute53> getAmazonClientClass() {
        return AmazonRoute53.class;
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
