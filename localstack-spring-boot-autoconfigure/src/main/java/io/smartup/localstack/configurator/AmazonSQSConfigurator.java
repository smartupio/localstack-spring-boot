package io.smartup.localstack.configurator;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.buffered.AmazonSQSBufferedAsyncClient;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class AmazonSQSConfigurator extends AbstractAmazonClientConfigurator<AmazonSQS> {
    @Override
    public Class<AmazonSQS> getAmazonClientClass() {
        return AmazonSQS.class;
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
    protected void preProcessBean(AmazonSQS amazonBean) {
        if (amazonBean instanceof AmazonSQSBufferedAsyncClient) {
            setImmutable((AmazonSQSBufferedAsyncClient) amazonBean, false);
        } else {
            super.preProcessBean(amazonBean);
        }
    }

    @Override
    protected void postProcessBean(AmazonSQS amazonBean) {
        if (amazonBean instanceof AmazonSQSBufferedAsyncClient) {
            setImmutable((AmazonSQSBufferedAsyncClient) amazonBean, this.immutable);
        } else {
            super.postProcessBean(amazonBean);
        }
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