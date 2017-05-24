package io.smartup.oss.localstack;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

/**
 * LocalStackApplicationListener is responsible for starting the LocalStack service
 * on startup and on shutdown it's responsible for shutting down the service.
 *
 * The startup is made possible by implementing the BeanPostProcessor interface, because for this library
 * it's critical to start the LocalStackService as soon as possible. The BeanPostProcessor interface allows
 * us to start the service during the first bean creations.
 *
 * The shutdown is made possible by implementing the ApplicationListener<ContextClosedEvent> interface, so
 * the callback method will be called once the application is shutting down.
 *
 * @see BeanPostProcessor
 * @see ApplicationListener
 */
public class LocalStackApplicationListener implements BeanPostProcessor, ApplicationListener<ContextClosedEvent> {
    private final LocalStackService localStackService;
    public LocalStackApplicationListener(LocalStackService localStackService) {
        this.localStackService = localStackService;
        this.localStackService.start();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        localStackService.stop();
    }
}
