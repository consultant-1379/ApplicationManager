/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 */
package com.ericsson.component.aia.sdk.applicationmanager.factory;

import org.springframework.http.ResponseEntity;

import com.ericsson.component.aia.sdk.applicationmanager.services.endpoints.response.ServiceResponse;

/**
 * Application defines create method specification
 *
 */
public interface Application {

    /**
     * <p>
     * This method will create a new application.
     * </p>
     *
     * @param jsonStr
     *            <p>
     *            of the application contains metadata (pba/maas) of the application to be created.
     *            </p>
     * @return
     *         <p>
     *         return path to the ZipFile to download the application
     *         </p>
     */
    ResponseEntity<ServiceResponse<String>> create(String jsonStr);
}