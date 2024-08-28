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

/**
 * The Class PublishApplicationView is the response to a publish application request .
 */
public class PublishApplicationView {

    private String taskId;

    /**
     * Instantiates a new publish application view.
     *
     */
    public PublishApplicationView() {
        super();
    }

    /**
     * Instantiates a new publish application view.
     *
     * @param taskId
     *            the task id
     */
    public PublishApplicationView(final String taskId) {
        super();
        this.taskId = taskId;
    }

    /**
     * Gets the task id.
     *
     * @return the task id
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * Sets the task id.
     *
     * @param taskId
     *            the new task id
     */
    public void setTaskId(final String taskId) {
        this.taskId = taskId;
    }

}