/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.sdk.applicationmanager.exceptions;

import com.ericsson.component.aia.sdk.templatemanager.exception.AppSdkException;
import com.ericsson.component.aia.sdk.templatemanager.exception.code.ExceptionCode;

/**
 * ApplicationManagerException is a runtime exception thrown by SDK application when a internal error or unexpected error occurs.
 *
 * @author ezsalro
 *
 */
public class ApplicationManagerException extends AppSdkException {

    private static final long serialVersionUID = 5900848440189801198L;

    /**
     * Default constructor.
     */
    public ApplicationManagerException() {
        super();
    }

    /**
     * @param code
     *            error code
     * @param msg
     *            message
     */
    public ApplicationManagerException(final ExceptionCode code, final String msg) {
        super(code, msg);
    }

    /**
     * @param code
     *            error code
     * @param msg
     *            message
     * @param exception
     *            exception to be throw
     */
    public ApplicationManagerException(final String code, final String msg, final Throwable exception) {
        super(code, msg, exception);
    }

    /**
     * @param code
     *            error code
     * @param exception
     *            exception to be throw
     */
    public ApplicationManagerException(final ExceptionCode code, final Throwable exception) {
        super(code, exception);
    }

    /**
     * @param code
     *            error code
     * @param msg
     *            message
     * @param runtimeException
     *            exception to be throw
     */
    public ApplicationManagerException(final String code, final String msg, final String runtimeException) {
        super(code, msg, runtimeException);
    }

    /**
     * @param code
     *            error code
     * @param msg
     *            message
     * @param exception
     *            exception to be throw
     */
    public ApplicationManagerException(final ExceptionCode code, final String msg, final Throwable exception) {
        super(code, msg, exception);
    }

    /**
     * @param code
     *            error code
     * @param msg
     *            message
     * @param exception
     *            exception to be throw
     * @param runtimeException
     *            runtime error code
     */
    public ApplicationManagerException(final String code, final String msg, final Throwable exception, final String runtimeException) {
        super(code, msg, exception, runtimeException);
    }

}
