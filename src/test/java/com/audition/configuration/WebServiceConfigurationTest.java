package com.audition.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

class WebServiceConfigurationTest {

    private final WebServiceConfiguration configuration = new WebServiceConfiguration();

    @Test
    void objectMapper_appliesExpectedJacksonConfiguration() {
        final ObjectMapper objectMapper = configuration.objectMapper();

        assertFalse(objectMapper.getSerializationConfig()
            .isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
        assertFalse(objectMapper.getDeserializationConfig()
            .isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertEquals(JsonInclude.Include.NON_EMPTY,
            objectMapper.getSerializationConfig().getDefaultPropertyInclusion().getValueInclusion());
        assertNotNull(objectMapper.getDateFormat());
    }

    @Test
    void restTemplate_usesConfiguredObjectMapperAndLoggingInterceptor() {
        final ObjectMapper objectMapper = configuration.objectMapper();
        final RestTemplateLoggingInterceptor interceptor =
            new RestTemplateLoggingInterceptor(new com.audition.common.logging.AuditionLogger());
        final ConnectionProperties connectionProperties = new ConnectionProperties();
        connectionProperties.setConnectTimeout(3000);
        connectionProperties.setReadTimeout(4000);

        final RestTemplate restTemplate =
            configuration.restTemplate(objectMapper, interceptor, connectionProperties);

        assertNotNull(restTemplate);
        assertTrue(restTemplate.getMessageConverters().stream()
            .filter(MappingJackson2HttpMessageConverter.class::isInstance)
            .map(MappingJackson2HttpMessageConverter.class::cast)
            .anyMatch(converter -> converter.getObjectMapper() == objectMapper));
        assertTrue(restTemplate.getInterceptors().contains(interceptor));
    }

    @Test
    void connectionProperties_hasDefaultTimeouts() {
        final ConnectionProperties connectionProperties = new ConnectionProperties();

        assertEquals(5000, connectionProperties.getConnectTimeout());
        assertEquals(5000, connectionProperties.getReadTimeout());
    }

    @Test
    void connectionProperties_applyTo_configuresRequestFactory() {
        final ConnectionProperties connectionProperties = new ConnectionProperties();
        connectionProperties.setConnectTimeout(3000);
        connectionProperties.setReadTimeout(4000);
        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        connectionProperties.applyTo(requestFactory);

        assertNotNull(requestFactory);
    }
}
