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

/**
 * .
 */
public enum PublishStatus {
    QUEUED, STARTED, CHECKING_PROJECT_FORMAT, PUSHING_DOCKER_IMAGE,
    PUSHING_SOURCE_TO_REPO, UPDATING_META_DATA_STORE, VALIDATING_OPERATION, FINISHED, FAILED
}
