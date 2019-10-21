package com.jbhunt.edi.sterlingarchive.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@ConfigurationProperties
@Data
@RefreshScope
public class ActiveMqProperties {
    /*@Value("${jbhunt.general.jms.activeMQ.connectionFactory.edi.consumer.brokerURL}")
    private String consumerBrokerUrl;

    @Value("${jbhunt.general.jms.activeMQ.connectionFactory.edi.producer.brokerURL}")
    private String producerBrokerUrl;

    @Value("${jbhunt.general.jms.activeMQ.connectionFactory.edi.consumer.maxConnections}")
    private int consumerMaxConnections;

    @Value("${jbhunt.general.jms.activeMQ.connectionFactory.edi.producer.maxConnections}")
    private int producerMaxConnections;*/

   /* @Value("${messaging.activeMq.queueName}")
    private String queueName;

    @Value("${messaging.activeMq.errorQueue}")
    private String errorQueue;

    @Value("${messaging.activeMq.responseTopic}")
    private String responseTopic;*/
}
