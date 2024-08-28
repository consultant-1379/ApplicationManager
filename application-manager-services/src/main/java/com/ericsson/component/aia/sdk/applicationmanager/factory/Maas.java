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
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.sdk.applicationmanager.factory;

import org.springframework.http.ResponseEntity;

import com.ericsson.component.aia.sdk.applicationmanager.services.endpoints.response.ServiceResponse;

/**
 * Maas for creating applications
 *
 */
public interface Maas {

    /**
     * create method
     * @param maasJsonStr
     *            maasJsonStr
     * @return response
     *            response
     */
    ResponseEntity<ServiceResponse<String>> create(final String maasJsonStr);
}