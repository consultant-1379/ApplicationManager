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

/**
 * ApplicationAbstractFactory defines factory classes for Application and Maas
 *
 */
public abstract class ApplicationAbstractFactory {


    /**
     * <p>
     * This method will get application.
     * </p>
     *
     * @return
     *         <p>
     *         return application
     *         </p>
     */
    public abstract Application getApplication();

    /**
     * <p>
     * This method will get Maas.
     * </p>
     *
     * @return
     *         <p>
     *         return maas
     *         </p>
     */
    public abstract Maas getMaas();

}