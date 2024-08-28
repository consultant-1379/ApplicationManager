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

import com.ericsson.component.aia.sdk.pba.model.Pba;

/**
 * The Class PublishedApplicationsView is used to convert all the application PBAs to a form which can be easily represented by the UI.
 */
@SuppressWarnings("PMD.ShortVariable")
public class PublishedApplicationsView {

    /** The name. */
    private String name;

    /** The title. */
    private String title;

    /** The description. */
    private String description;

    /** The versions. */
    private Collection<PbaVersionView> versions = new ArrayList<>();

    /**
     * Instantiates a new published applications view.
     *
     * @param pba
     *            the pba
     */
    public PublishedApplicationsView(final Pba pba) {
        name = pba.getApplicationInfo().getName();
        title = getTitle(pba);
        description = pba.getApplicationInfo().getDescription();
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name
     *            the new name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Gets the versions.
     *
     * @return the versions
     */
    public Collection<PbaVersionView> getVersions() {
        return versions;
    }

    /**
     * Sets the versions.
     *
     * @param versions
     *            the new versions
     */
    public void setVersions(final Collection<PbaVersionView> versions) {
        this.versions = versions;
    }

    /**
     * Adds the version.
     *
     * @param version
     *            the version
     */
    public void addVersion(final PbaVersionView version) {
        this.versions.add(version);
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title.
     *
     * @param title
     *            the new title
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description
     *            the new description
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "PublishedApplicationsView [name=" + name + ", title=" + title + ", description=" + description + ", versions=" + versions + "]";
    }

    /**
     * Gets the title.
     *
     * @param pba
     *            the pba
     * @return the title
     */
    private String getTitle(final Pba pba) {
        final String applicationTitle = pba.getApplicationInfo().getTitle();
        if (applicationTitle != null) {
            return applicationTitle;
        }
        return pba.getTemplateInfo().getTitle();
    }

}