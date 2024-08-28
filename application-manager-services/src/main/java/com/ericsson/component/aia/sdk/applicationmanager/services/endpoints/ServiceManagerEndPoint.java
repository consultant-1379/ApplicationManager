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
package com.ericsson.component.aia.sdk.applicationmanager.services.endpoints;

import static org.springframework.http.ResponseEntity.status;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.ericsson.component.aia.sdk.applicationmanager.api.ServiceManager;
import com.ericsson.component.aia.sdk.applicationmanager.service.views.DependencyInfo;
import com.ericsson.component.aia.sdk.applicationmanager.services.endpoints.response.ServiceResponse;
import com.ericsson.component.aia.sdk.applicationmanager.views.ServicesView;
import com.ericsson.component.aia.sdk.pba.model.PBAInstance;
import com.ericsson.component.aia.sdk.pba.model.Pba;

import io.swagger.annotations.ApiOperation;

/**
 * This rest end point is used to serve up the services which are available to the UI. A service is represented as a special variety of application.
 */
@CrossOrigin
@Controller
public class ServiceManagerEndPoint {

    @Autowired
    private ServiceManager serviceManager;

    /**
     * Get the available services names.
     *
     * @param serviceType
     *            the service type
     * @return the all available technologies.
     */
    @RequestMapping(value = "/services/{serviceType}", method = RequestMethod.GET)
    @ApiOperation(value = "getAllAvailibleTechnologies accepts serviceType and returns HTTPStatus.OK and serviceResponse")
    public ResponseEntity<ServiceResponse<Collection<String>>> getAllAvailibleTechnologies(@PathVariable("serviceType") final String serviceType) {
        final ServiceResponse<Collection<String>> serviceResponse = new ServiceResponse<>();
        serviceResponse.setData(serviceManager.getAvailableTechnologies(serviceType));
        return status(HttpStatus.OK).body(serviceResponse);
    }

    /**
     * Get the versions available for a service.
     *
     * @param serviceType
     *            the service type
     * @param technology
     *            The technology.
     * @return the available versions.
     */
    @RequestMapping(value = "/services/{serviceType}/{technology}", method = RequestMethod.GET)
    @ApiOperation(value = "getAvailableVersions accepts serviceType, technology and returns HTTPStatus.OK and serviceResponse")
    public ResponseEntity<ServiceResponse<ServicesView>> getAvailableVersions(@PathVariable("serviceType") final String serviceType,
                                                                              @PathVariable("technology") final String technology) {
        final ServiceResponse<ServicesView> serviceResponse = new ServiceResponse<>();

        serviceResponse.setData(serviceManager.getAvailableTechnologyVersions(serviceType, technology));
        return status(HttpStatus.OK).body(serviceResponse);
    }

    /**
     * Get the integration & extension points available for a specific service version.
     *
     * @param serviceType
     *            the service type
     * @param technology
     *            The technology.
     * @param serviceId
     *            The ID of the specific service version.
     * @return the defaults for technology.
     */
    @RequestMapping(value = "/services/{serviceType}/{technology}/{id:.+}", method = RequestMethod.GET)
    @ApiOperation(value = "getDefaultsForTechnology accepts serviceType, technology, serviceId and returns serviceResponse")
    public ResponseEntity<ServiceResponse<Pba>> getDefaultsForTechnology(@PathVariable("serviceType") final String serviceType,
                                                                         @PathVariable("technology") final String technology,
                                                                         @PathVariable("id") final String serviceId) {
        final ServiceResponse<Pba> serviceResponse = new ServiceResponse<>();
        final PBAInstance pbaInstance = serviceManager.getServiceInstance(serviceId);
        serviceResponse.setData(pbaInstance == null ? null : pbaInstance.getPba());
        return status(HttpStatus.OK).body(serviceResponse);
    }

    /**
     * This POST endpoint accepts {@link PBAInstance} of a new service to be created as JSON string and returns {@link HttpStatus.OK} code along with
     * the Id of the new service.
     *
     * @param servicePba
     *            the service PBA as a String
     * @return {@link ResponseEntity} containing the Id of the new service.
     *
     */
    @RequestMapping(value = "/service", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "createService accepts pba and returns HTTPStatus.OK and serviceResponse")
    public ResponseEntity<String> createService(@RequestBody final String servicePba) {
        return status(HttpStatus.OK).body(serviceManager.createService(servicePba));
    }

    /**
     * Gets the dependencies.
     *
     * @param pbaId
     *            the pba id
     * @return the dependencies
     */
    @RequestMapping(value = "/service/{id}/dependency", method = RequestMethod.GET)
    @ApiOperation(value = "getDependencies accepts pbaId and returns HTTPStatus.OK and serviceResponse")
    public ResponseEntity<ServiceResponse<Collection<DependencyInfo>>> getDependencies(@PathVariable("id") final String pbaId) {
        final ServiceResponse<Collection<DependencyInfo>> serviceResponse = new ServiceResponse<>();
        serviceResponse.setData(serviceManager.getDependencies(pbaId));
        return status(HttpStatus.OK).body(serviceResponse);
    }

    /**
     * Update service.
     *
     * @param pbaId
     *            the pba id
     * @param servicePba
     *            the service pba
     */
    @RequestMapping(value = "/service/{id:.+}", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "updateService accepts pbaId, pba and returns void")
    public void updateService(@PathVariable("id") final String pbaId, @RequestBody final String servicePba) {
        serviceManager.updateService(pbaId, servicePba);
    }

    /**
     * This DELETE endpoint accepts the path param of the PBA Id of the service to be deleted and returns {@link HttpStatus.OK} if the service is
     * deleted successfully.
     *
     * @param pbaId
     *            the unique id of the service to delete.
     *
     */
    @RequestMapping(value = "/service/{id:.+}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "deleteService accepts pbaId and returns void")
    public void deleteService(@PathVariable("id") final String pbaId) {
        serviceManager.deleteService(pbaId);
    }

    /**
     * Add dependencies for services.
     *
     * @param serviceId
     *            - the pba to be used.
     * @param dependencies
     *            the dependencies to be added.
     */
    @RequestMapping(value = "/service/{id}/addDependencies", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "addDependencies accepts serviceId, dependencies collection and returns void")
    public void addDependencies(@PathVariable("id") final String serviceId, @RequestBody final Collection<DependencyInfo> dependencies) {
        serviceManager.addDependencies(serviceId, dependencies);
    }

    /**
     * Update dependencies for services.
     *
     * @param serviceId
     *            - the pba to be used.
     * @param dependencies
     *            the dependencies to be added.
     */
    @RequestMapping(value = "/service/{id}/updateDependencies", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "updateDependencies accepts serviceId, dependencies collection and returns void")
    public void updateDependencies(@PathVariable("id") final String serviceId, @RequestBody final Collection<DependencyInfo> dependencies) {
        serviceManager.updateDependencies(serviceId, dependencies);
    }

    /**
     * Clear dependencies for services.
     *
     * @param serviceId
     *            - the pba to be used.
     */
    @RequestMapping(value = "/service/{id}/clearDependencies", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "clearDependencies accepts serviceId and returns void")
    public void clearDependencies(@PathVariable("id") final String serviceId) {
        serviceManager.clearDependencies(serviceId);
    }

    /**
     * This GET endpoint returns the a {@link ResponseEntity} containing the {@link PBAInstance} which corresponds to the specified service ID
     *
     * @param serviceId
     *            the service id
     * @return {@link ResponseEntity} containing the {@link PBAInstance} which corresponds to the specified service ID.
     *
     */
    @RequestMapping(value = "/service/{id:.+}", method = RequestMethod.GET)
    @ApiOperation(value = "getService accepts serviceId and returns PBAInstance")
    public ResponseEntity<PBAInstance> getService(@PathVariable("id") final String serviceId) {
        return status(HttpStatus.OK).body(serviceManager.getServiceInstance(serviceId));
    }
}
