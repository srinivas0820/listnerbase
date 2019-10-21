package com.jbhunt.edi.sterlingarchive.configuration;

import com.jbhunt.edi.sterlingarchive.utils.BlobStorageUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BlobStorageUtilConfig {
    @Bean
    public BlobStorageUtil blobStorageUtil() {
        return new BlobStorageUtil();
    }
}
