package io.smartup.localstack;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * The LocalStackBeanPostProcessor class is responsible for overriding the
 * AmazonWebServiceClients that are used by the user of this library.
 */
public class LocalStackBeanPostProcessor implements BeanPostProcessor {
    private final List<LocalStackProperties.Service> enabledServices;

    public LocalStackBeanPostProcessor(LocalStackProperties localStackProperties) {
        this.enabledServices = localStackProperties.getEnabledServices();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (! (bean instanceof AmazonWebServiceClient)) {
            return bean;
        }

        for (LocalStackProperties.Service service : enabledServices) {
            if (classNamesMatch(bean.getClass(), service.getClass())) {
                AmazonWebServiceClient awsClient = (AmazonWebServiceClient) bean;

                Field isImmutable = ReflectionUtils.findField(AmazonWebServiceClient.class, "isImmutable");
                isImmutable.setAccessible(true);
                boolean immutable = (Boolean) ReflectionUtils.getField(isImmutable, awsClient);
                ReflectionUtils.setField(isImmutable, awsClient, false);

                awsClient.setRegion(Region.getRegion(Regions.DEFAULT_REGION));
                awsClient.setEndpoint("http://" + LocalStackProperties.HOST +":" + service.getDockerPort());

                ReflectionUtils.setField(isImmutable, awsClient, immutable);
                isImmutable.setAccessible(false);
            }
        }

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * This method checks if a bean should be overwritten by the LocalStack urls
     *
     * Example:
     *  - beanClass' name is AmazonDynamoDBAsyncClient
     *  - serviceClass' name is DynamoDB
     *
     *  The method will check whether the beanClass' name contains the serviceClass' name.
     *
     * @param beanClass beans class to be checked
     * @param serviceClass serviceClass to be checked against
     * @return true if beanClass' name contains the servicesClass' name
     */
    private boolean classNamesMatch(Class beanClass, Class<? extends LocalStackProperties.Service> serviceClass) {
        String fullyQualifiedName = serviceClass.getName();
        String serviceName = fullyQualifiedName.substring(fullyQualifiedName.indexOf("$") + 1);
        return beanClass.getName().toLowerCase().contains(serviceName.toLowerCase());
    }
}
