package com.ericsson.component.aia.sdk.applicationmanager.model;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * UIDef holds maas ui details
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
      "parserDefinition"
    })
public class UIDef {

    @JsonProperty("inputSource")
    private String inputSource;

    @JsonProperty("parserDef")
    private ParserDef parserDef;

    @JsonProperty("inputAdapter")
    private InputAdapter inputAdapter;

    @JsonProperty("outputAdapter")
    private OutputAdapter outputAdapter;

    @JsonProperty("inputSource")
    public String getInputSource() {
        return inputSource;
    }

    @JsonProperty("inputSource")
    public void setInputSource(final String inputSource) {
        this.inputSource = inputSource;
    }

    @JsonProperty("parserDef")
    public ParserDef getParserDef() {
        return parserDef;
    }

    @JsonProperty("parserDef")
    public void setParserDef(final ParserDef parserDef) {
        this.parserDef = parserDef;
    }

    @JsonProperty("inputAdapter")
    public InputAdapter getInputAdapter() {
        return inputAdapter;
    }

    @JsonProperty("inputAdapter")
    public void setInputAdapter(final InputAdapter inputAdapter) {
        this.inputAdapter = inputAdapter;
    }

    @JsonProperty("outputAdapter")
    public OutputAdapter getOutputAdapter() {
        return outputAdapter;
    }

    @JsonProperty("outputAdapter")
    public void setOutputAdapter(final OutputAdapter outputAdapter) {
        this.outputAdapter = outputAdapter;
    }

}