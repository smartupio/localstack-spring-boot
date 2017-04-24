package io.smartup.cloud.configurator;


import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.buffered.AmazonSQSBufferedAsyncClient;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SpringAmazonSQSAsyncConfigurator extends AbstractAmazonClientConfigurator<AmazonSQSBufferedAsyncClient>{

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

        AmazonSQS realSqs;

        try {
            realSqs = (AmazonSQS) ReflectionUtils.findField(AmazonSQSBufferedAsyncClient.class, "realSQS").get(amazonBean);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Can not access field from realSQS", e);
        }

        Field isImmutable = ReflectionUtils.findField(realSqs.getClass(), "isImmutable");

        boolean immutable = false;
        if (isImmutable != null) {
            isImmutable.setAccessible(true);
            immutable = (Boolean) ReflectionUtils.getField(isImmutable, amazonBean);
            ReflectionUtils.setField(isImmutable, amazonBean, false);
        }

        Method setRegion = ReflectionUtils.findMethod(amazonBean.getClass(), "setRegion", Region.class);
        Method setEndpoint = ReflectionUtils.findMethod(amazonBean.getClass(), "setEndpoint", String.class);

        preProcessBean(amazonBean);
        if (setRegion != null && setEndpoint != null) {
            ReflectionUtils.invokeMethod(setRegion, amazonBean, getRegion());
            ReflectionUtils.invokeMethod(setEndpoint, amazonBean, getEndpoint());
        }
        postProcessBean(amazonBean);

        if (isImmutable != null) {
            ReflectionUtils.setField(isImmutable, amazonBean, immutable);
            isImmutable.setAccessible(false);
        }
    }
}
