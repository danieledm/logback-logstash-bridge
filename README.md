Logback Logstash
================

## Overiew

Provides a Logback appenders (access log and application log) for outputting log messages into logstash or throught any plain TCP socket.
Also provides logback encoder display for encoding messages in JSON format.

## Access log


This is example shows how to configure access log ( logback-access.xml ).

```xml
<configuration>

    <property file="${CATALINA_BASE}/conf/logback-logstash-${columbusEnv}.properties" />

    <appender name="outputfile" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <File>./logs/columbus-api-access.log</File>
        <Append>true</Append>
        <encoder>
            <pattern>common</pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>./logs/columbus-api-access.log.%d{yyyy-MM-dd}</fileNamePattern>
        </rollingPolicy>
    </appender>

    <appender name="stash" class="com.logback.logstash.appender.AccessSocketAppender">
        <remoteHost>${logstash.host}</remoteHost>
        <port>${logstash.port}</port>
        <encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
            <layout class="com.logback.logstash.encoder.AccessLogstashLayout" >
                <channel>columbus-api-access</channel>
            </layout>
        </encoder>
    </appender>

    <appender-ref ref="outputfile"/>
    <appender-ref ref="stash" />

</configuration>
```

com.logback.logstash.appender.AccessSocketAppender is the socket appender for access log. It is configured to use com.logback.logstash.encoder.AccessLogstashLayout which produce a specific JSON format. Any Logback encoder can be used within this appender.

See more about Backlog access log configuration here: http://logback.qos.ch/access.html


## Application log

This is an example of application log configuration. (logback.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <property resource="columbus-${columbusEnv}.properties" />

    <appender name="stdout" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} | %-15t | %-5p | %-25.25c{1} | %m%n</pattern>
        </encoder>
    </appender>
    <appender name="outputfile" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <File>./logs/columbus-api.log</File>
        <Append>true</Append>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} | %-15t | %-5p | %-25c{1} | %m%n</pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>./logs/columbus-api.log.%d{yyyy-MM-dd}</fileNamePattern>
        </rollingPolicy>
    </appender>

    <appender name="stash" class="com.logback.logstash.appender.EventSocketAppender">
        <remoteHost>${logstash.host}</remoteHost>
        <port>${logstash.port}</port>
        <encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
           <layout class="com.ogback.logstash.encoder.EventLogstashLayout">
               <channel>columbus-api</channel>
           </layout>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="stdout"/>
        <appender-ref ref="outputfile"/>
        <appender-ref ref="stash" />
    </root>
</configuration>
```

Configuration is very similar to access log but it works at application level.

Any change to the JSON format can be made in logastah layout classes.
