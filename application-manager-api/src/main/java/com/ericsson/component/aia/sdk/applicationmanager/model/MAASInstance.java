package com.ericsson.component.aia.sdk.applicationmanager.model;

import javax.annotation.Generated;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


/**
 * MAASInstance helps to retrive maas object
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "maas"
    })
public class MAASInstance {


    @JsonProperty("maas")
    private Maas maas;

    @JsonProperty("maas")
    public Maas getMaas() {
        return maas;
    }

    @JsonProperty("maas")
    public void setMaas(final Maas maas) {
        this.maas = maas;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(maas).toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }

        final MAASInstance rhs = ((MAASInstance) other);
        return new EqualsBuilder().append(maas, rhs.maas).isEquals();
    }

}
