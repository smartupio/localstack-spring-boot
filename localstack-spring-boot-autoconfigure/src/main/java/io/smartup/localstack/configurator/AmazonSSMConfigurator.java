package io.smartup.localstack.configurator;


import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;

public class AmazonSSMConfigurator extends AbstractAmazonClientConfigurator<AWSSimpleSystemsManagement> {
    @Override
    public Class<AWSSimpleSystemsManagement> getAmazonClientClass() {
        return AWSSimpleSystemsManagement.class;
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
