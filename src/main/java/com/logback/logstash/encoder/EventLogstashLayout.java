package com.logback.logstash.encoder;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.LayoutBase;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import org.apache.commons.lang.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;

/**
 * this class populates the json to send to Logstash.
 * If you want to add or remove a field to the raw logstash json this the right place to look into.
 * Created by ddallemule on 07/10/2014.
 */
public class EventLogstashLayout extends LayoutBase<ILoggingEvent> {

    public static final String TIMESTAMP = "@timestamp";
    public static final String VERSION = "@version";
    public static final String LEVEL = "level";
    public static final String HOST = "host";
    public static final String MESSAGE = "message";
    public static final String LOGGER_NAME = "logger_name";
    public static final String THREAD_NAME = "thread_name";
    public static final String STACK_TRACE = "stack_trace";
    public static final String CHANNEL = "channel";

    private static final JsonFactory FACTORY = new MappingJsonFactory().enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
    public static final FastDateFormat ISO_DATETIME_TIME_ZONE_FORMAT_WITH_MILLIS = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
    private static final String NEW_LINE = System.getProperty("line.separator");
    private final Logger log = LoggerFactory.getLogger(EventLogstashLayout.class);
    private String channel = "";

    public String doLayout(ILoggingEvent event) {
        StringWriter writer = new StringWriter();
        JsonGenerator jsonGenerator = null;
        try {
            jsonGenerator = FACTORY.createGenerator(writer);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField(TIMESTAMP, ISO_DATETIME_TIME_ZONE_FORMAT_WITH_MILLIS.format(event.getTimeStamp()));
            jsonGenerator.writeNumberField(VERSION, 1);
            jsonGenerator.writeStringField(HOST, event.getLoggerContextVO().getPropertyMap().get("HOSTNAME"));
            jsonGenerator.writeStringField(LEVEL, event.getLevel().toString());
            jsonGenerator.writeStringField(MESSAGE, event.getFormattedMessage());
            jsonGenerator.writeStringField(LOGGER_NAME, event.getLoggerName());
            jsonGenerator.writeStringField(THREAD_NAME, event.getThreadName());
            IThrowableProxy throwableProxy = event.getThrowableProxy();
            if (throwableProxy != null) {
                jsonGenerator.writeStringField(STACK_TRACE, ThrowableProxyUtil.asString(throwableProxy));
            }
            jsonGenerator.writeStringField(CHANNEL, channel);
            jsonGenerator.writeEndObject();
            jsonGenerator.close();
        } catch (IOException e) {
            //notify the problem and carry on
            log.error("Error building JSON for Logstash: " + e.getMessage());
        }
        writer.append(NEW_LINE);
        return writer.toString();
    }

    public void setChannel(String channel){
            this.channel = channel;
    }

}
