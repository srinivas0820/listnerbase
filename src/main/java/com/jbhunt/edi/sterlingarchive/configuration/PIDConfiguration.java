package com.jbhunt.edi.sterlingarchive.configuration;

import com.jbhunt.biz.securepid.FusePIDReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PIDConfiguration {
    @Bean
    public com.jbhunt.biz.securepid.PIDCredentials pidCredentials() {
        FusePIDReader fusePIDReader = new FusePIDReader("edi");
        return fusePIDReader.readPIDCredentials("default");
    }
}
