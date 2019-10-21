package com.jbhunt.edi.sterlingarchive.configuration;

import com.ibm.mq.jms.MQQueueConnectionFactory;

import com.ibm.msg.client.wmq.WMQConstants;
import com.jbhunt.biz.securepid.PIDCredentials;
import com.jbhunt.edi.sterlingarchive.properties.WebMQProperties;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.jms.JmsConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

@EnableConfigurationProperties(WebMQProperties.class)
@Configuration
public class WebMqConfiguration {

    private final WebMQProperties webMQProperties;

    public WebMqConfiguration(WebMQProperties webMQProperties){
        this.webMQProperties = webMQProperties;
    }

    @Bean
    @Primary
    @Qualifier("consumerConnectionFactory")
    public ConnectionFactory consumerConnectionFactory() {
        MQQueueConnectionFactory factory = null;
        try {
            factory = new MQQueueConnectionFactory();
            factory.setHostName(webMQProperties.getArchiveHost());
            factory.setPort(webMQProperties.getArchivePort());
            factory.setQueueManager(webMQProperties.getArchiveQueueManager());
            factory.setChannel(webMQProperties.getArchiveChannel());
            factory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
        return factory;
    }

    @Bean
    @Qualifier("producerConnectionFactory")
    public ConnectionFactory producerConnectionFactory() {
        MQQueueConnectionFactory factory = null;
        try {
            factory = new MQQueueConnectionFactory();
            factory.setHostName(webMQProperties.getErrorHost());
            factory.setPort(webMQProperties.getErrorPort());
            factory.setQueueManager(webMQProperties.getErrorQueueManager());
            factory.setChannel(webMQProperties.getErrorChannel());
            factory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
        return factory;
    }

    @Bean
    @Qualifier("jmsConsumerConfiguration")
    public JmsConfiguration jmsConsumerConfiguration(@Qualifier("consumerConnectionFactory")
             ConnectionFactory consumerConnectionFactory, PIDCredentials pidCredentials){
        JmsConfiguration jmsConfiguration = new JmsConfiguration(consumerConnectionFactory);
        jmsConfiguration.setUsername(pidCredentials.getUsername());
        jmsConfiguration.setPassword(pidCredentials.getPassword());
        return  jmsConfiguration;
    }

    @Bean
    @Qualifier("jmsProducerConfiguration")
    public JmsConfiguration jmsProducerConfiguration(@Qualifier("producerConnectionFactory")
                                                             ConnectionFactory producerConnectionFactory, PIDCredentials pidCredentials){
        JmsConfiguration jmsConfiguration = new JmsConfiguration(producerConnectionFactory);
        jmsConfiguration.setUsername(pidCredentials.getUsername());
        jmsConfiguration.setPassword(pidCredentials.getPassword());
        return  jmsConfiguration;
    }

    @Bean
    public JmsComponent webSphereConsumer(@Qualifier("jmsConsumerConfiguration")
            JmsConfiguration jmsConsumerConfiguration) {
        return new JmsComponent(jmsConsumerConfiguration);
    }

    @Bean
    public JmsComponent webSphereProducer(@Qualifier("jmsProducerConfiguration")
            JmsConfiguration jmsProducerConfiguration) {
        return new JmsComponent(jmsProducerConfiguration);
    }
}
