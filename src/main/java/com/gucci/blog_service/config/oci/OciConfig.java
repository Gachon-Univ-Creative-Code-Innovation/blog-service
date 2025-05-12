package com.gucci.blog_service.config.oci;

import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class OciConfig {
    @Value("${oci.objectstorage.region}")
    private String region;

    @Bean
    public ObjectStorage objectStorageClient() {
        try {
            AuthenticationDetailsProvider provider =
                    new ConfigFileAuthenticationDetailsProvider("DEFAULT");
            return ObjectStorageClient.builder()
                    .region(region)
                    .build(provider);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load OCI config from ~/.oci/config", e);
        }
    }
}
