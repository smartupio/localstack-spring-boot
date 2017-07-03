package io.smartup.cloud.configurator;

import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class SQSConnectionFactoryConfigurator extends AbstractAmazonClientConfigurator<SQSConnectionFactory> {
    @Override
    public Class<SQSConnectionFactory> getAmazonClientClass() {
        return SQSConnectionFactory.class;
    }

    @Override
    public String getEndpoint() {
        return "http://" + getLocalStackHost() + ":4576";
    }

    @Override
    public Region getRegion() {
        return Region.getRegion(Regions.DEFAULT_REGION);
    }

    @Override
    protected SQSConnectionFactory setup() {
        SQSConnectionFactory amazonBean = getBean();

        Field region = ReflectionUtils.findField(SQSConnectionFactory.class, "region");
        Field endpoint = ReflectionUtils.findField(SQSConnectionFactory.class, "endpoint");

        region.setAccessible(true);
        endpoint.setAccessible(true);

        preProcessBean(amazonBean);
        ReflectionUtils.setField(region, amazonBean, getRegion());
        ReflectionUtils.setField(endpoint, amazonBean, getEndpoint());
        postProcessBean(amazonBean);

        return amazonBean;
    }
}
