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
import java.util.List;

import com.ericsson.component.aia.sdk.applicationmanager.common.Constants;

/**
 * This class is used as a DTO for returning a list of the application versions
 */
public class ApplicationVersionView {

    private boolean newApplication = true;
    private String maxVersion = Constants.DEFAULT_STARTING_VERSION;
    private List<String> versions = new ArrayList<>();

    /**
     * Gets the versions.
     *
     * @return the versions
     */
    public List<String> getVersions() {
        return versions;
    }

    /**
     * Adds the versions.
     *
     * @param versions
     *            the versions
     */
    public void setVersions(final List<String> versions) {
        this.versions = versions;
    }

    /**
     * Gets the max version.
     *
     * @return the max version
     */
    public String getMaxVersion() {
        return maxVersion;
    }

    @Override
    public String toString() {
        return "ApplicationVersionView [newApplication=" + newApplication + ", maxVersion=" + maxVersion + ", versions=" + versions + "]";
    }

    /**
     * Sets the max version.
     *
     * @param maxVersion
     *            the new max version
     */
    public void setMaxVersion(final String maxVersion) {
        this.maxVersion = maxVersion;
    }

    /**
     * Checks if is new application.
     *
     * @return true, if is new application
     */
    public boolean isNewApplication() {
        return newApplication;
    }

    /**
     * Sets the new application.
     *
     * @param newApplication
     *            the new new application
     */
    public void setNewApplication(final boolean newApplication) {
        this.newApplication = newApplication;
    }

}
