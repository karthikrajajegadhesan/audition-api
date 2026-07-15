package com.audition.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@ConfigurationProperties(prefix = "application.connection")
public class ConnectionProperties {

    private int connectTimeout = 5000;
    private int readTimeout = 5000;
    private int writeTimeout = 5000;
    private int responseTimeout = 5000;

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(final int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(final int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(final int writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public int getResponseTimeout() {
        return responseTimeout;
    }

    public void setResponseTimeout(final int responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    public void applyTo(final SimpleClientHttpRequestFactory requestFactory) {
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
    }
}