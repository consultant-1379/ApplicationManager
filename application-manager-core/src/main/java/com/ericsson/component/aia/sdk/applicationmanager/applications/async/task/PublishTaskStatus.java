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
package com.ericsson.component.aia.sdk.applicationmanager.applications.async.task;

import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ApplicationManagerExceptionCodes;
import com.ericsson.component.aia.sdk.applicationmanager.views.TaskStatusView;

/**
 * This class is holder for the status of an executing task.
 */
public class PublishTaskStatus {

    private PublishStatus publishStatus = PublishStatus.QUEUED;
    private ApplicationManagerExceptionCodes errorMessageCode = ApplicationManagerExceptionCodes.UNKNOWN_APPLICATION_ERROR;

    /**
     * Get a view of the current task status. This method may throw an SdkApplicationException if the task encountered an issue the cause of the
     * exception will match that of the exception encountered by the task.
     *
     * @return {@link TaskStatusView}
     */
    public TaskStatusView getTaskStatusView() {
        final TaskStatusView taskStatusView = new TaskStatusView();
        if (this.publishStatus.equals(PublishStatus.FAILED)) {
            taskStatusView.setExceptionCode(errorMessageCode);
        }

        taskStatusView.setStepDescription(publishStatus.toString());
        taskStatusView.setStep(publishStatus.ordinal());
        taskStatusView.setFinished(publishStatus == PublishStatus.FINISHED || publishStatus == PublishStatus.FAILED);
        return taskStatusView;
    }

    public void setPublishStatus(final PublishStatus publishStatus) {
        this.publishStatus = publishStatus;
    }

    public void setErrorMessageCode(final ApplicationManagerExceptionCodes errorMessageCode) {
        this.errorMessageCode = errorMessageCode;
    }

}
