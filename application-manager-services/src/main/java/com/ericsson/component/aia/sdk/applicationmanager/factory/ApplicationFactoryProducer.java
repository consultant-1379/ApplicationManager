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

import java.util.Optional;

import com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConstants;
import com.ericsson.component.aia.sdk.applicationmanager.util.AppTemplateConnector;

/**
 * ApplicationFactoryProducer creates factory either for application or maas
 *
 */
public class ApplicationFactoryProducer {

    private final ApplicationFactory applicationFactory;
    private final MaasFactory maasFactory;

    /**
     * initializes ApplicationFactoryProducer
     * @param applicationFactory
     *           applicationFactory
     * @param maasFactory
     *           maasFactory
     */
    ApplicationFactoryProducer(final ApplicationFactory applicationFactory, final MaasFactory maasFactory) {
        this.applicationFactory = applicationFactory;
        this.maasFactory = maasFactory;
    }


    /**
     * getFactory returns the factory
     * @param type
     *           type whether APPLICATION or SERVICE
     * @param template
     *           kafka, spark, flink (or Maas for service type)
     * @param jsonStr
     *           json as String (could be Pba or Maas)
     * @return ApplicationAbstractFactory
     *           applicationAbstractFactory
     */
    public ApplicationAbstractFactory getFactory(final String type, final String template, final String jsonStr) {

        boolean isValidMaas = false, isValidTemplate = false;
        if (ApplicationManagerConstants.SERVICE.equalsIgnoreCase(type) && ApplicationManagerConstants.MAAS.equalsIgnoreCase(template)) {
            isValidMaas = true;
        } else {
            final Optional<AppTemplateConnector> templateConnector = AppTemplateConnector
                .findAppTemplateConnector(template);
            if (templateConnector.isPresent()) {
                isValidTemplate = true;
            }
        }
        if (ApplicationManagerConstants.APPLICATION.equalsIgnoreCase(type) && isValidTemplate && jsonStr instanceof String ) {
            return applicationFactory;
        } else if (isValidMaas && jsonStr instanceof String ) {
            return maasFactory;
        }

        return null;
    }

}