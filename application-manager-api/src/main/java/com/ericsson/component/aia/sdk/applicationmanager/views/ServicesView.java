/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
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
import java.util.List;

import com.ericsson.component.aia.sdk.pba.model.ServiceInfo;

/**
 * This class is used as a DTO for returning a list of services to the UI
 */
@SuppressWarnings("PMD.ShortVariable")
public class ServicesView {

    private List<String> id = new ArrayList<>();
    private List<String> versions = new ArrayList<>();

    /**
     * This method will create a Services DTO object which presents the available versions of a technology.
     *
     * @param service
     *            A version of the service which is available.
     */
    public void addServices(final ServiceInfo service) {
        id.add(service.getId());
        versions.add(service.getVersion());
    }

    public List<String> getId() {
        return id;
    }

    public void setId(final List<String> id) {
        this.id = id;
    }

    public List<String> getVersions() {
        return versions;
    }

    public void setVersions(final List<String> versions) {
        this.versions = versions;
    }

}
