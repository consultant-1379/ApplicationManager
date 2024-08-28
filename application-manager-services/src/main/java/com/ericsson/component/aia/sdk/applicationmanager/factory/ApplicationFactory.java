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
 * ApplicationFactory factory class for Application
 *
 */
public class ApplicationFactory extends ApplicationAbstractFactory {

    private final GenericApplication genericApplication;


    /**
     * Instantiates a new ApplicationFactory
     *
     * @param applicationFactoryBuilder
     *            the applicationFactoryBuilder
     */
    ApplicationFactory(final ApplicationFactoryBuilder applicationFactoryBuilder) {
        genericApplication = new GenericApplication(applicationFactoryBuilder);
    }
    /* (non-Javadoc)
     * @see com.ericsson.component.aia.sdk.applicationmanager.factory.ApplicationAbstractFactory
     * #getApplication(com.ericsson.component.aia.sdk.pba.model.PBAInstance)
     */
    @Override
    public Application getApplication() {
        return genericApplication;
    }

    /* (non-Javadoc)
     * @see com.ericsson.component.aia.sdk.applicationmanager.factory.ApplicationAbstractFactory
     * #getMaas(java.lang.String)
     */
    @Override
    public Maas getMaas() {
        return null;
    }

}