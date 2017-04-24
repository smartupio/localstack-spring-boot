package io.smartup.cloud.configurator;


import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.buffered.AmazonSQSBufferedAsyncClient;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class SpringAmazonSQSAsyncConfigurator extends AbstractAmazonClientConfigurator<AmazonSQSBufferedAsyncClient>{
    private boolean immutable;

    @Override
    public Class<AmazonSQSBufferedAsyncClient> getAmazonClientClass() {
        return AmazonSQSBufferedAsyncClient.class;
    }

    @Override
    public String getEndpoint() {
        return "http://localhost:4576";
    }

    @Override
    public Region getRegion() {
        return Region.getRegion(Regions.DEFAULT_REGION);
    }

    @Override
    protected void preProcessBean(AmazonSQSBufferedAsyncClient amazonBean) {
        this.immutable = setImmutable(amazonBean, false);
    }

    @Override
    protected void postProcessBean(AmazonSQSBufferedAsyncClient amazonBean) {
        setImmutable(amazonBean, this.immutable);
    }

    private boolean setImmutable(AmazonSQSBufferedAsyncClient amazonBean, boolean immutable) {
        try {
            Field fieldRealSqs = ReflectionUtils.findField(AmazonSQSBufferedAsyncClient.class, "realSQS");
            fieldRealSqs.setAccessible(true);
            AmazonSQS realSqs = (AmazonSQS) fieldRealSqs.get(amazonBean);

            Field isImmutable = ReflectionUtils.findField(realSqs.getClass(), "isImmutable");

            if (isImmutable != null) {
                isImmutable.setAccessible(true);
                Boolean field = (Boolean) ReflectionUtils.getField(isImmutable, realSqs);
                ReflectionUtils.setField(isImmutable, realSqs, immutable);
                return field;
            }
            return false;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Can not access field from realSQS", e);
        }
    }
}
