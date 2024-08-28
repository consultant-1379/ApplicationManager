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
package com.ericsson.component.aia.sdk.applicationmanager.views;

import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerExceptionCodes;
import com.ericsson.component.aia.sdk.templatemanager.exception.code.ExceptionCode;

/**
 * This view is used to return information about the current status of an async task
 */
public class TaskStatusView {

    private boolean finished;
    private int step;
    private String stepDescription;
    private ApplicationManagerExceptionCodes exceptionCode;

    public ExceptionCode getExceptionCode() {
        return exceptionCode;
    }

    public void setExceptionCode(final ApplicationManagerExceptionCodes exceptionCode) {
        this.exceptionCode = exceptionCode;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(final boolean finished) {
        this.finished = finished;
    }

    public int getStep() {
        return step;
    }

    public void setStep(final int step) {
        this.step = step;
    }

    public String getStepDescription() {
        return stepDescription;
    }

    public void setStepDescription(final String stepDescription) {
        this.stepDescription = stepDescription;
    }

}
