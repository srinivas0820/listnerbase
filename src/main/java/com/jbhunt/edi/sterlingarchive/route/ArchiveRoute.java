package com.jbhunt.edi.sterlingarchive.route;

import com.jbhunt.edi.sterlingarchive.dto.ArchiveDataDTO;
import com.jbhunt.edi.sterlingarchive.processor.DocumentProcessor;
import com.jbhunt.edi.sterlingarchive.processor.OnErrorProcessor;
import com.jbhunt.edi.sterlingarchive.processor.PostProcessor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.zip.ZipException;

@Component
@RefreshScope
public class ArchiveRoute extends RouteBuilder {

    @Value("${messaging.webMq.archive.queueName}")
    private String webMqName;

    @Value("${messaging.webMq.error.queueName}")
    private String errorQueue;

    private final DocumentProcessor documentProcessor;
    private final OnErrorProcessor onErrorProcessor;
    private final PostProcessor postProcessor;

    public ArchiveRoute(DocumentProcessor documentProcessor, OnErrorProcessor onErrorProcessor,
                        PostProcessor postProcessor) {
        this.documentProcessor = documentProcessor;
        this.onErrorProcessor = onErrorProcessor;
        this.postProcessor = postProcessor;
    }

    @Override
    public void configure() {
        String errorQueueName;
        String consumerQueueName;

        String errorDirect = "direct:error";
        String incrementRetryDirect = "direct:incrementretry";
        String mainProcessSeda = "seda:mainProcess";
        String postprocessorDirect = "direct:postprocessor";

        boolean testMode = false;
        if(testMode) {
            consumerQueueName = "activeAmqConsumer:" + webMqName;
            errorQueueName = "activeMQProducer:" + errorQueue;
        } else {
            consumerQueueName = String.format("webSphereConsumer:queue:%s", webMqName);
            errorQueueName = String.format("webSphereProducer:queue:%s", errorQueue);
        }
        this.getContext().setAllowUseOriginalMessage(true);

        onException(IllegalStateException.class)
                .handled(true)
                .log(LoggingLevel.INFO, log, "IllegalStateException caught. Document:\n${body}")
                .process(onErrorProcessor)
                .to(errorQueueName)
                .end();

        onException(ZipException.class)
                .handled(true)
                .log(LoggingLevel.INFO, log, "ZipException caught. Document:\n${body}")
                .process(onErrorProcessor)
                .end();

        //after the document is not XML, invalid XML, or not ProcessData
        onException(IOException.class)
                .handled(true)
                .log(LoggingLevel.INFO, log, "IOException caught. Document:\n${body}")
                .process(onErrorProcessor)
                .end();

        //after any other exception
        onException(Exception.class)
                .handled(true)
                .log(LoggingLevel.INFO, log, "Exception caught. Original XML document:\n${body}")
                .process(onErrorProcessor)
                .to(errorQueueName)
                .end();

        // Enable Logging to the default logger
        errorHandler(defaultErrorHandler().log(log));

        //Send documents from queue this listener reads from
        //(WebMQ) directly to the main process SEDA to allow for
        //async document processing
        from(consumerQueueName)
                .log("${body}")
                .to(mainProcessSeda)
                .end();

        //from SEDA, through processor
        from(mainProcessSeda)
                .threads(20)
                .process(documentProcessor)
                .to(postprocessorDirect)
                .end();

        //postprocessor checks exchange to see if exception property
        //was set on it (since the async doc processor cannot throw
        //exception)
        from(postprocessorDirect)
                .process(postProcessor)
                .end();

/*        //from direct:error to error queue
        from(errorDirect)
                .log("Posting original document to error queue (" + errorQueue + ").")
                .to(errorQueueName)
                .log("Document successfully posted to error queue.")
                .end();*/
    }
}
