package com.gucci.blog_service.config.oci;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "oci.objectstorage")
public class OciProperties {
    /** application.yml 의 oci.objectstorage.region */
    private String region;
    /** application.yml 의 oci.objectstorage.namespace */
    private String namespace;
    /** application.yml 의 oci.objectstorage.bucket */
    private String bucket;
}
