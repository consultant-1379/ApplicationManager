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

/**
 * MaasFactory to create maas application
 */
public class MaasFactory extends ApplicationAbstractFactory {

    private final ApplicationFactoryBuilder applicationFactoryBuilder;

    /**
     * initializes MaasFactory
     * @param applicationFactoryBuilder
     *               applicationFactoryBuilder
     */
    MaasFactory(final ApplicationFactoryBuilder applicationFactoryBuilder) {
        this.applicationFactoryBuilder = applicationFactoryBuilder;
    }

    @Override
    public Application getApplication() {
        return null;
    }

    @Override
    public Maas getMaas() {
        return new MaasApplication(applicationFactoryBuilder);
    }

}