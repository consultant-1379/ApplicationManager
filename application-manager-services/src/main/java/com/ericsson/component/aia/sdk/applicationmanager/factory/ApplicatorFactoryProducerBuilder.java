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

import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerException;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerExceptionCodes;

/**
 * ApplicatorFactoryProducerBuilder for building factory classes
 *
 */
public class ApplicatorFactoryProducerBuilder {

    private final ApplicationFactory applicationFactory;
    private final MaasFactory massFactory;

    /**
     * initializes ApplicatorFactoryProducerBuilder
     * @param applicationFactoryBuilder
     *           applicationFactoryBuilder
     */
    ApplicatorFactoryProducerBuilder(final ApplicationFactoryBuilder applicationFactoryBuilder) {
        applicationFactory = new ApplicationFactory(applicationFactoryBuilder);
        massFactory = new MaasFactory(applicationFactoryBuilder);
    }

    /**
     * build method for creating instance of ApplicationFactoryProducer
     * @return ApplicationFactoryProducer
     *           ApplicationFactoryProducer
     */
    public ApplicationFactoryProducer build() {

        if (applicationFactory == null) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_REGISTERING_SERVICES, "applicationFactory is null");
        }

        if (massFactory == null) {
            throw new ApplicationManagerException(ApplicationManagerExceptionCodes.ERROR_REGISTERING_SERVICES, "massFactory is null");
        }

        return new ApplicationFactoryProducer(applicationFactory, massFactory);
    }

    /**
     * builder method used to create instance of ApplicatorFactoryProducerBuilder
     * @param applicationFactoryBuilder
     *              applicationFactoryBuilder
     * @return ApplicatorFactoryProducerBuilder
     *              ApplicatorFactoryProducerBuilder
     */
    public static ApplicatorFactoryProducerBuilder builder(final ApplicationFactoryBuilder applicationFactoryBuilder) {
        return new ApplicatorFactoryProducerBuilder(applicationFactoryBuilder);
    }

}