package io.smartup.localstack.configurator;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;

public class AmazonLambdaConfigurator extends AbstractAmazonClientConfigurator<AWSLambda> {
    @Override
    public Class<AWSLambda> getAmazonClientClass() {
        return AWSLambda.class;
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
