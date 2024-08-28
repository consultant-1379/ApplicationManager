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
package com.ericsson.component.aia.sdk.applicationmanager.converters;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.ericsson.component.aia.sdk.applicationmanager.views.PbaVersionView;
import com.ericsson.component.aia.sdk.applicationmanager.views.PublishedApplicationsView;
import com.ericsson.component.aia.sdk.pba.model.PBAInstance;
import com.ericsson.component.aia.sdk.pba.model.PbaInfo;

/**
 * The Class PbaToListViewConverter is used to structure the PBAInstances of available service.
 */
public class PbaToListViewConverter {

    private final Map<String, PublishedApplicationsView> applicationNameVersionMapping = new HashMap<>();

    /**
     * Adds the pba instance to the view.
     *
     * @param pbaInstance
     *            the pba
     */
    public void addPbaInstance(final PBAInstance pbaInstance) {
        final PbaInfo applicationInfo = pbaInstance.getPba().getApplicationInfo();

        final PbaVersionView pbaListView = new PbaVersionView(pbaInstance.getPba());
        if (!pbaListView.getIntegrationPoints().isEmpty()) {
            PublishedApplicationsView publishedApplicationsView = applicationNameVersionMapping.get(applicationInfo.getName());

            if (publishedApplicationsView == null) {
                publishedApplicationsView = new PublishedApplicationsView(pbaInstance.getPba());
                applicationNameVersionMapping.put(applicationInfo.getName(), publishedApplicationsView);
            }

            publishedApplicationsView.addVersion(pbaListView);
        }
    }

    /**
     * Adds the pba instance to the view.
     *
     * @param pbaInstance
     *            the pba
     * @param allowedTechnology
     *            the allowed technology
     */
    public void addPbaInstance(final PBAInstance pbaInstance, final Set<String> allowedTechnology) {
        final PbaInfo applicationInfo = pbaInstance.getPba().getApplicationInfo();
        final PbaVersionView pbaListView = new PbaVersionView(pbaInstance.getPba(), allowedTechnology);

        if (!pbaListView.getIntegrationPoints().isEmpty()) {
            PublishedApplicationsView publishedApplicationsView = applicationNameVersionMapping.get(applicationInfo.getName());

            if (publishedApplicationsView == null) {
                publishedApplicationsView = new PublishedApplicationsView(pbaInstance.getPba());
                applicationNameVersionMapping.put(applicationInfo.getName(), publishedApplicationsView);
            }

            publishedApplicationsView.addVersion(pbaListView);
        }
    }

    /**
     * Gets the pba list views.
     *
     * @return the pba list views
     */
    public Collection<PublishedApplicationsView> getPbaListViews() {
        return applicationNameVersionMapping.values();
    }

    @Override
    public String toString() {
        return "PbaToListViewConverter [applicationNameVersionMapping=" + applicationNameVersionMapping + "]";
    }

}