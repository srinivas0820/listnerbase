package com.jbhunt.edi.sterlingarchive.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@ConfigurationProperties
@Data
@RefreshScope
public class WebMQProperties {
    @Value("${messaging.webMq.archive.host}")
    private String archiveHost;

    @Value("${messaging.webMq.archive.port}")
    private int archivePort;

    @Value("${messaging.webMq.archive.queueManager}")
    private String archiveQueueManager;

    @Value("${messaging.webMq.archive.channel}")
    private String archiveChannel;

    @Value("${messaging.webMq.error.host}")
    private String errorHost;

    @Value("${messaging.webMq.error.port}")
    private int errorPort;

    @Value("${messaging.webMq.error.queueManager}")
    private String errorQueueManager;

    @Value("${messaging.webMq.error.channel}")
    private String errorChannel;
}