package com.jbhunt.edi.sterlingarchive.processor;

import org.apache.camel.Exchange;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class PostProcessorTest {
    private PostProcessor postProcessor;

    @Mock
    private Exchange exchange;

    @Before
    public void setup() {
        postProcessor = new PostProcessor();
    }

    @Test(expected = Exception.class)
    public void testException() throws Exception {
        when(exchange.getProperty(anyString())).thenReturn(new Exception());
        postProcessor.process(exchange);
    }

    @Test
    public void testNoException() throws Exception {
        postProcessor.process(exchange);
    }
}