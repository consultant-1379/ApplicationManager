package com.ericsson.component.aia.sdk.applicationmanager.model;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * InputAdapter helps to capture subscriber details
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "bootstrap.servers", "name" })
public class InputAdapter {

    @JsonProperty("bootstrap.servers")
    private String bootstrapServer;

    @JsonProperty("name")
    private String topicName;

    @JsonProperty("bootstrap.servers")
    public String getBootstrapServer() {
        return bootstrapServer;
    }

    @JsonProperty("bootstrap.servers")
    public void setBootstrapServer(final String bootstrapServer) {
        this.bootstrapServer = bootstrapServer;
    }

    @JsonProperty("name")
    public String getTopicName() {
        return topicName;
    }

    @JsonProperty("name")
    public void setTopicName(final String topicName) {
        this.topicName = topicName;
    }

}