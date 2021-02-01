package io.smartup.localstack.configurator;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.S3ClientOptions;

public class AmazonS3Configurator extends AbstractAmazonClientConfigurator<AmazonS3> {
    @Override
    public Class<AmazonS3> getAmazonClientClass() {
        return AmazonS3.class;
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
    protected void preProcessBean(AmazonS3 amazonS3) {
        super.preProcessBean(amazonS3);
        amazonS3.setS3ClientOptions(S3ClientOptions.builder().disableChunkedEncoding().setPathStyleAccess(true).build());
    }
}
