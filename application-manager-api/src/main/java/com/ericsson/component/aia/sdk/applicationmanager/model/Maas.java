package com.ericsson.component.aia.sdk.applicationmanager.model;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Maas helps to capture maas details
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "version",
    "status",
    "UIServiceDef"
    })
public class Maas {

    @JsonProperty("version")
    private String version;

    @JsonProperty("status")
    private String status = "ACTIVE";

    @JsonProperty("UIDef")
    private UIDef uiDef;

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(final String version) {
        this.version = version;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(final String status) {
        this.status = status;
    }

    @JsonProperty("UIDef")
    public UIDef getUiDef() {
        return uiDef;
    }

    @JsonProperty("UIDef")
    public void setUiDef(final UIDef uiDef) {
        this.uiDef = uiDef;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(uiDef).toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }

        final Maas rhs = ((Maas) other);
        return new EqualsBuilder().append(version, rhs.version).append(uiDef, rhs.uiDef).isEquals();
    }

}