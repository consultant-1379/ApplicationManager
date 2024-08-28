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
package com.ericsson.component.aia.sdk.applicationmanager.services;

import java.util.Collection;

import com.ericsson.component.aia.sdk.applicationmanager.service.views.DependencyInfo;

/**
 * Interface used to retrieve data from the dependency information service
 */
public interface DependencyInformationService {

    /**
     * Gets the dependency info.
     *
     * @param pbaId
     *            the pba id
     * @return the dependency info
     */
    Collection<DependencyInfo> getDependencyCollection(String pbaId);

}