package io.smartup.cloud.configurator;

import com.amazonaws.regions.Region;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class AbstractAmazonClientConfigurator<T> implements ApplicationContextAware, BeanPostProcessor {
    private ApplicationContext applicationContext;
    protected boolean immutable;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    protected T getBean() {
        return applicationContext.getBean(getAmazonClientClass());
    }

    public abstract Class<T> getAmazonClientClass();

    public abstract String getEndpoint();

    public abstract Region getRegion();

    @PostConstruct
    protected T setup() {
        T amazonBean = getBean();

        Method setRegion = ReflectionUtils.findMethod(amazonBean.getClass(), "setRegion", Region.class);
        Method setEndpoint = ReflectionUtils.findMethod(amazonBean.getClass(), "setEndpoint", String.class);

        preProcessBean(amazonBean);
        if (setRegion != null && setEndpoint != null) {
            ReflectionUtils.invokeMethod(setRegion, amazonBean, getRegion());
            ReflectionUtils.invokeMethod(setEndpoint, amazonBean, getEndpoint());
        }
        postProcessBean(amazonBean);

        return amazonBean;
    }

    protected void preProcessBean(T amazonBean) {
        Field isImmutable = ReflectionUtils.findField(amazonBean.getClass(), "isImmutable");

        if (isImmutable != null) {
            isImmutable.setAccessible(true);
            this.immutable = (Boolean) ReflectionUtils.getField(isImmutable, amazonBean);
            ReflectionUtils.setField(isImmutable, amazonBean, false);
            isImmutable.setAccessible(false);
        }
    }

    protected void postProcessBean(T amazonBean) {
        Field isImmutable = ReflectionUtils.findField(amazonBean.getClass(), "isImmutable");

        if (isImmutable != null) {
            isImmutable.setAccessible(true);
            ReflectionUtils.setField(isImmutable, amazonBean, this.immutable);
            isImmutable.setAccessible(false);
        }
    }
}
