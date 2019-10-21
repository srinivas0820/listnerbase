package com.jbhunt.edi.sterlingarchive.utils;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class EventHubSendUtil {
    private EventHubClient client;
    private ConnectionStringBuilder connectionStringBuilder;
    private ExecutorService executorService;
    private Long clientLastInitializedMillis = -1L;
    private static final Long refreshMillis = 1000L * 55L; //refresh client every 55 seconds.

    @Value("${eventHub.namespaceName}")
    private String namespaceName;

    @Value("${eventHub.eventHubName}")
    private String eventHubName;

    @Value("${eventHub.sasKeyName}")
    private String sasKeyName;

//    @Value("${eventHub.sasKey}")
//    private String sasKey;

    @Value("${EDI-ARCHIVE-DOCMETA-EH-KEY}")
    private String sasKey;

    public EventHubSendUtil() {
    }

    public void sendMessage(String message) throws Exception {
        if(connectionStringBuilder == null || executorService == null) {
            connectionStringBuilder = new ConnectionStringBuilder();
            connectionStringBuilder.setNamespaceName(namespaceName)
                    .setEventHubName(eventHubName)
                    .setSasKeyName(sasKeyName)
                    .setSasKey(sasKey);
            executorService = Executors.newWorkStealingPool();
        }
        if (client != null && System.currentTimeMillis() - clientLastInitializedMillis >= refreshMillis) {
            closeEventHubClient();
            createNewEventHubClient();
        } else if (client == null) {
            createNewEventHubClient();
        }
        EventData sendEvent = EventData.create(message.getBytes());
        try {
            client.sendSync(sendEvent);
        } catch (EventHubException e) {
            log.error("Sending the message to EventHub failed: " + e.getMessage());
            throw e;
        }
    }

    private void createNewEventHubClient() throws Exception {
        try {
            client = EventHubClient.createSync(connectionStringBuilder.toString(), executorService);
            clientLastInitializedMillis = System.currentTimeMillis();
        } catch(Exception e) {
            log.error("Creating the EventHubClient failed. " + e.getClass().toString() + ": " + e.getMessage());
            throw e;
        }
    }

    private void closeEventHubClient() {
        try {
            client.closeSync();
        } catch(EventHubException e) {
            log.error("Closing the EventHubClient failed: " + e.getMessage());
        }
    }
}
