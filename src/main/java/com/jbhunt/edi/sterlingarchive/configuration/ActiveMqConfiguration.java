package com.jbhunt.edi.sterlingarchive.configuration;

import com.jbhunt.biz.securepid.PIDCredentials;
import com.jbhunt.edi.sterlingarchive.properties.ActiveMqProperties;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

@EnableConfigurationProperties(ActiveMqProperties.class)
@Configuration
public class ActiveMqConfiguration {
    private final com.jbhunt.biz.securepid.PIDCredentials pidCredentials;
    private final ActiveMqProperties activeMqProperties;

    public ActiveMqConfiguration(PIDCredentials pidCredentials, ActiveMqProperties activeMqProperties) {
        this.pidCredentials = pidCredentials;
        this.activeMqProperties = activeMqProperties;
    }

    @Bean
    public ActiveMQComponent activeAmqConsumer() {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
        activeMQConnectionFactory.setBrokerURL("nio://winx-12386:61616");
        activeMQConnectionFactory.setUserName("admin");
        activeMQConnectionFactory.setPassword("admin");

        PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory();
        pooledConnectionFactory.setConnectionFactory(activeMQConnectionFactory);
        pooledConnectionFactory.setMaxConnections(1);

        ActiveMQComponent activeMQComponent = new ActiveMQComponent();
        activeMQComponent.setConnectionFactory(pooledConnectionFactory);
        activeMQComponent.setCacheLevel(DefaultMessageListenerContainer.CACHE_CONSUMER);
        activeMQComponent.setTransacted(true);
        return activeMQComponent;
    }

    @Bean
    public ActiveMQComponent activeMQProducer() {
        // Connection Factory
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
        activeMQConnectionFactory.setBrokerURL("nio://winx-12386:61616");
        activeMQConnectionFactory.setUserName("admin");
        activeMQConnectionFactory.setPassword("admin");

        // Pooled Connection Factory
        PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory();
        pooledConnectionFactory.setConnectionFactory(activeMQConnectionFactory);
        pooledConnectionFactory.setMaxConnections(1);

        // ActiveMQ Component
        ActiveMQComponent activeMQComponent = new ActiveMQComponent();
        activeMQComponent.setConnectionFactory(pooledConnectionFactory);
        activeMQComponent.setCacheLevel(DefaultMessageListenerContainer.CACHE_CONNECTION);
        activeMQComponent.setTransacted(false);

        return activeMQComponent;
    }
}
