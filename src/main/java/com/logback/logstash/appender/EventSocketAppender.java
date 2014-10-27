package com.logback.logstash.appender;

import ch.qos.logback.classic.net.LoggingEventPreSerializationTransformer;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.PreSerializationTransformer;

/**
 * Created by ddallemule on 09/10/2014.
 */
public class EventSocketAppender extends AbstractSocketAppender<ILoggingEvent>{
    private static final PreSerializationTransformer<ILoggingEvent> PST = new LoggingEventPreSerializationTransformer();


    @Override
    protected void postProcessEvent(ILoggingEvent event) {
    }

    @Override
    protected PreSerializationTransformer<ILoggingEvent> getPST() {
        return PST;
    }
}
