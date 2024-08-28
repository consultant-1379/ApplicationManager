/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.sdk.applicationmanager.views;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This view is used to list the integration points' URI attributes.
 *
 */
public class PbaIntegrationPointUriView {
    private String protocol;
    private String address;
    private Collection<PbaIntegrationPointUriArgsView> args = new ArrayList<>();

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(final String address) {
        this.address = address;
    }

    public Collection<PbaIntegrationPointUriArgsView> getArgs() {
        return args;
    }

    public void setArgs(final Collection<PbaIntegrationPointUriArgsView> args) {
        this.args = args;
    }

    @Override
    public String toString() {
        return "PbaIntegrationPointUriView [protocol=" + protocol + ", address=" + address + ", args=" + args + "]";
    }
}
