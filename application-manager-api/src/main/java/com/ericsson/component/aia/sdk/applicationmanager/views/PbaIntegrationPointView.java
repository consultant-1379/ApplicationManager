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
 * This view is used to list the applications' integration points.
 *
 */
public class PbaIntegrationPointView {
    private String technology;
    private String description;
    private Collection<PbaIntegrationPointUriView> uri = new ArrayList<>();

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(final String technology) {
        this.technology = technology;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Collection<PbaIntegrationPointUriView> getUri() {
        return uri;
    }

    public void setUri(final Collection<PbaIntegrationPointUriView> uri) {
        this.uri = uri;
    }
}
