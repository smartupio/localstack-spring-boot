package io.smartup.localstack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class LocalStackHostProvider {

    private static final Logger log = LoggerFactory.getLogger(LocalStackHostProvider.class);

    private static final String LOCALSTACK_ADDRESS = "localstack";
    private static final String FALLBACK_ADDRESS = "localhost";

    public String provideLocalStackHost() {
        String localStackAddress = null;
        try {
            log.info("Trying to resolve localstack hostname=" + LOCALSTACK_ADDRESS);
            InetAddress inetAddress = InetAddress.getByName(LOCALSTACK_ADDRESS);
            log.info("Successfully resolved localstack hostname=" + LOCALSTACK_ADDRESS);

            localStackAddress = LOCALSTACK_ADDRESS;
        } catch (UnknownHostException e) {
            log.info("Unable to resolve '" + LOCALSTACK_ADDRESS + "' as hostname");
        }

        if (localStackAddress == null) {
            log.info("Falling back to fallback hostname=" + FALLBACK_ADDRESS);
            return FALLBACK_ADDRESS;
        }

        log.info("Providing localstack hostname=" + localStackAddress);
        return localStackAddress;
    }

}
