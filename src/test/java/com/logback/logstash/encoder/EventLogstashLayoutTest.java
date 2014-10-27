package com.logback.logstash.encoder;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by ddallemule on 07/10/2014.
 */
public class EventLogstashLayoutTest {
    EventLogstashLayout logstashLayout = new EventLogstashLayout();



    @Test
    public void testDoLayout() throws IOException {
        LoggingEvent loggingEvent = new LoggingEvent();
        loggingEvent.setLevel(Level.INFO);
        loggingEvent.setLoggerName("loggerName");
        loggingEvent.setMessage("log message");
        loggingEvent.setThreadName("thread-name");
        long currentTimeStamp = System.currentTimeMillis();
        loggingEvent.setTimeStamp(currentTimeStamp);
        Map<String,String> props = new HashMap<String,String>();
        props.put("HOSTNAME", "localhost");
        loggingEvent.setLoggerContextRemoteView(new LoggerContextVO("context", props, System.currentTimeMillis() ));
        String jsonLogAsString = logstashLayout.doLayout(loggingEvent);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonLogAsString);
        assertEquals("log message", jsonNode.get(EventLogstashLayout.MESSAGE).asText());
        assertEquals("INFO", jsonNode.get(EventLogstashLayout.LEVEL).asText());
        assertEquals(1, jsonNode.get(EventLogstashLayout.VERSION).asInt());
        assertEquals("loggerName", jsonNode.get(EventLogstashLayout.LOGGER_NAME).asText());
        assertEquals("thread-name", jsonNode.get(EventLogstashLayout.THREAD_NAME).asText());
        assertEquals("localhost", jsonNode.get(EventLogstashLayout.HOST).asText());
        String expectedTimeStamp = EventLogstashLayout.ISO_DATETIME_TIME_ZONE_FORMAT_WITH_MILLIS.format(currentTimeStamp);
        assertEquals(expectedTimeStamp, jsonNode.get(EventLogstashLayout.TIMESTAMP).asText());
    }


    @Test
    public void testDoLayoutMissingField() throws IOException {
        LoggingEvent loggingEvent = new LoggingEvent();
        loggingEvent.setLevel(Level.INFO);
        loggingEvent.setMessage("log message");
        loggingEvent.setThreadName("thread-name");
        long currentTimeStamp = System.currentTimeMillis();
        loggingEvent.setTimeStamp(currentTimeStamp);
        Map<String,String> props = new HashMap<String,String>();
        props.put("HOSTNAME", "localhost");
        loggingEvent.setLoggerContextRemoteView(new LoggerContextVO("context", props, System.currentTimeMillis() ));
        String jsonLogAsString = logstashLayout.doLayout(loggingEvent);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonLogAsString);
        assertEquals("log message", jsonNode.get(EventLogstashLayout.MESSAGE).asText());
        assertEquals("INFO", jsonNode.get(EventLogstashLayout.LEVEL).asText());
        assertEquals(1, jsonNode.get(EventLogstashLayout.VERSION).asInt());
        assertEquals("null", jsonNode.get(EventLogstashLayout.LOGGER_NAME).asText());
        assertEquals("thread-name", jsonNode.get(EventLogstashLayout.THREAD_NAME).asText());
        assertEquals("localhost", jsonNode.get(EventLogstashLayout.HOST).asText());
        String expectedTimeStamp = EventLogstashLayout.ISO_DATETIME_TIME_ZONE_FORMAT_WITH_MILLIS.format(currentTimeStamp);
        assertEquals(expectedTimeStamp, jsonNode.get(EventLogstashLayout.TIMESTAMP).asText());

    }

    @Test
    public void testDoLayoutException() throws IOException {
        LoggingEvent loggingEvent = new LoggingEvent();
        loggingEvent.setLevel(Level.INFO);
        loggingEvent.setLoggerName("loggerName");
        loggingEvent.setMessage("log message");
        loggingEvent.setThreadName("thread-name");
        long currentTimeStamp = System.currentTimeMillis();
        loggingEvent.setTimeStamp(currentTimeStamp);
        Map<String,String> props = new HashMap<String,String>();
        props.put("HOSTNAME", "localhost");
        loggingEvent.setLoggerContextRemoteView(new LoggerContextVO("context", props, System.currentTimeMillis() ));
        // test exception in the log
        loggingEvent.setThrowableProxy(new ThrowableProxy(new NullPointerException("Exception message 123")));

        String jsonLogAsString = logstashLayout.doLayout(loggingEvent);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonLogAsString);
        assertEquals("log message", jsonNode.get(EventLogstashLayout.MESSAGE).asText());
        assertEquals("INFO", jsonNode.get(EventLogstashLayout.LEVEL).asText());
        assertEquals(1, jsonNode.get(EventLogstashLayout.VERSION).asInt());
        assertEquals("loggerName", jsonNode.get(EventLogstashLayout.LOGGER_NAME).asText());
        assertEquals("thread-name", jsonNode.get(EventLogstashLayout.THREAD_NAME).asText());
        assertEquals("localhost", jsonNode.get(EventLogstashLayout.HOST).asText());
        String expectedTimeStamp = EventLogstashLayout.ISO_DATETIME_TIME_ZONE_FORMAT_WITH_MILLIS.format(currentTimeStamp);
        assertEquals(expectedTimeStamp, jsonNode.get(EventLogstashLayout.TIMESTAMP).asText());
        assertTrue(jsonNode.get(EventLogstashLayout.STACK_TRACE).asText().contains("Exception message 123"));

    }
}
