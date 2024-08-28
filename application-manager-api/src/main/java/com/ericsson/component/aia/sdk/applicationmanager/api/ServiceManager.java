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
package com.ericsson.component.aia.sdk.applicationmanager.api;

import java.util.Collection;

import com.ericsson.component.aia.sdk.applicationmanager.service.views.DependencyInfo;
import com.ericsson.component.aia.sdk.applicationmanager.views.ServicesView;
import com.ericsson.component.aia.sdk.pba.model.PBAInstance;

/**
 * Interface for the basic query operations performed by the service manager.
 */
public interface ServiceManager {

    /**
     * Get a list of all the technologies which are available as a service.
     *
     * @param serviceType
     *            The service type
     * @return collection of technologies names.{@link Collection}
     */
    Collection<String> getAvailableTechnologies(String serviceType);

    /**
     * Get a PBA for the service matching the provided ID
     *
     * @param serviceId
     *            the service id
     * @return {@link PBAInstance}
     */
    PBAInstance getServiceInstance(String serviceId);

    /**
     * Gets the dependencies of an application or service as a collection of nodes.
     *
     * @param pbaId
     *            the service id
     * @return the dependencies
     */
    Collection<DependencyInfo> getDependencies(String pbaId);

    /**
     * Creates a new service .
     *
     * @param servicePba
     *            The PBA of the new Service.
     * @return The Id of the newly inserted PBA.
     */
    String createService(String servicePba);

    /**
     * Deletes a service from the service repository.
     *
     * @param taskId
     *            The Id of the service PBA to delete.
     */
    void deleteService(String taskId);

    /**
     * Add dependencies to a existing service.
     *
     * @param pbaId
     *            - service to be changed.
     * @param dependencies
     *            - dependencies to be added.
     */
    void addDependencies(String pbaId, Collection<DependencyInfo> dependencies);

    /**
     * Update the dependencies of a existing service.
     *
     * @param pbaId
     *            - service to be updated.
     * @param dependencies
     *            - dependencies to be changed.
     */
    void updateDependencies(String pbaId, Collection<DependencyInfo> dependencies);

    /**
     * Deletes all dependencies for the specified service.
     *
     * @param serviceId
     *            - service to be deleted.
     */
    void clearDependencies(final String serviceId);

    /**
     * Update the service specified by the ID
     *
     * @param pbaId
     *            The Id of the service to update
     * @param servicePba
     *            The update to be made
     * @return Id the updated service
     */
    String updateService(String pbaId, String servicePba);

    /**
     * Gets Available technology versions based on servicetype and technology
     *
     * @param serviceType
     *            Service Type
     * @param technology
     *            Technology
     * @return ServiceView
     *
     */
    ServicesView getAvailableTechnologyVersions(String serviceType, String technology);
}
