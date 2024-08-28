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
package com.ericsson.component.aia.sdk.applicationmanager.exceptions;

import com.ericsson.component.aia.sdk.templatemanager.exception.code.ExceptionCode;

/**
 * @author ezsalro
 *
 */
public enum ServiceManagerExceptionCodes implements ExceptionCode {

    NOT_AUTHORIZED("2000001"),
    PBA_NOT_FOUND("2000002"),
    ERROR_INVOKING_METADATASERVICE_ON_CREATE("2000003"),
    ERROR_INVOKING_METADATASERVICE_ON_UPDATE("2000004"),
    ERROR_INVOKING_METADATASERVICE_ON_LIST("2000005"),
    ERROR_INVOKING_METADATASERVICE_ON_DELETE("2000006"),
    PBA_IS_INVALID("2000007"),
    SERVICE_ALREADY_EXISTS("2000008"),
    UNKNOW_ERROR("2000009"),
    ERROR_INVOKING_METADATASERVICE_WHILE_CHANGING_DEPENDENCIES("2000010");

    private final String code;

    /**
     * @param code
     *            error code
     */
    ServiceManagerExceptionCodes(final String code) {
        this.code = code;
    }

    /**
     * @param text
     *            code in string format
     * @return the equivalent enum
     */
    public ServiceManagerExceptionCodes getExceptionCodes(final String text) {
        for (final ServiceManagerExceptionCodes code : ServiceManagerExceptionCodes.values()) {
            if (code.getCode().equals(text)) {
                return code;
            }
        }
        return null;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return this.code;
    }

}
