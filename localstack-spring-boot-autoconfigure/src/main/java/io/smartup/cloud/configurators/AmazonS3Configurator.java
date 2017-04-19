package io.smartup.cloud.configurators;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.S3ClientOptions;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class AmazonS3Configurator extends AbstractAmazonClientConfigurator<AmazonS3> {
    @Override
    public Class<AmazonS3> getAmazonClientClass() {
        return AmazonS3.class;
    }

    @Override
    public String getEndpoint() {
        return "http://localhost:4572";
    }

    @Override
    public Region getRegion() {
        return Region.getRegion(Regions.DEFAULT_REGION);
    }

    @Override
    protected AmazonS3 setup() {
        AmazonS3 amazonS3 = getBean();

        Field isImmutable = ReflectionUtils.findField(amazonS3.getClass(), "isImmutable");

        boolean immutable = false;
        if (isImmutable != null) {
            isImmutable.setAccessible(true);
            immutable = (Boolean) ReflectionUtils.getField(isImmutable, amazonS3);
            ReflectionUtils.setField(isImmutable, amazonS3, false);
        }

        amazonS3.setS3ClientOptions(S3ClientOptions.builder().disableChunkedEncoding().setPathStyleAccess(true).build());
        amazonS3.setRegion(getRegion());
        amazonS3.setEndpoint(getEndpoint());

        if (isImmutable != null) {
            ReflectionUtils.setField(isImmutable, amazonS3, immutable);
            isImmutable.setAccessible(false);
        }

        return amazonS3;
    }
}
