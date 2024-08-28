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
public enum ApplicationManagerExceptionCodes implements ExceptionCode {

    NOT_AUTHORIZED("1000001"),
    ERROR_CREATING_PBA_INSTANCE("1000002"),
    PBA_IS_INVALID("1000003"),
    PBA_NOT_FOUND("1000004"),
    ERROR_COPYING_UPLOADED_FILE_TO_SERVER("1000005"),
    ERROR_CREATING_APPLICATION("1000006"),
    APPLICATION_FILE_IS_CORRUPTED("1000007"),
    ERROR_EXTENDING_APPLICATION("1000008"),
    ERROR_RETRIEVING_STATUS_FOR_APPLICATION("1000009"),
    ERROR_READING_SCHEMMA("1000010"),
    APPLICATION_NOT_FOUND("1000011"),
    ERROR_EXTRACTING_APPLICATION_DATA("1000012"),
    ERROR_UPDATING_PBA("1000013"),
    ERROR_ACCESSING_GIT_REPOSITORY("1000014"),
    ERROR_INVOKING_METADATASERVICE_ON_PUBLISHED("1000015"),
    ERROR_INVOKING_METADATASERVICE_ON_UNPUBLISHED("1000016"),
    PBA_IS_CORRUPTED("1000017"),
    ERROR_INVOKING_METADATASERVICE_ON_LIST("1000018"),
    ERROR_INVOKING_METADATASERVICE_ON_GET("1000019"),
    ERROR_INVOKING_METADATASERVICE_ON_DOWNLOAD_APPLICATION("1000020"),
    ERROR_CLONING_APPLICATION_ON_FILESYSTEM("1000021"),
    ERROR_INVOKING_METADATASERVICE_ON_LIST_APPLICATION("1000022"),
    ERROR_REGISTERING_SERVICES("1000023"),
    UNKNOWN_APPLICATION_ERROR("1000024"),
    ERROR_SAVING_APPLICATION_IN_METADATA_SERVICE("1000025"),
    APPLICATION_DEPENDENCY_NOT_FOUND("1000026"),
    PARENT_APPLICATION_NOT_FOUND("1000027"),
    ERROR_ACCESSING_FILE_SYSTEM("1000028"),
    ERROR_ACCESSING_CACHE_SYSTEM("1000029"),
    KAFKA_TECHNOLOGY_IS_MANDATORY("1000030"),
    DUPLICATED_KAFKA_TECHNOLOGY("1000031"),
    //MAAS Related
    MAAS_TEMPLATE_NOT_FOUND("1000032"),
    MAAS_INVALID_DATA("1000033"),
    ERROR_INVOKING_METADATASERVICE_ON_DELETE("1000034"),
    ERROR_DELETING_GIT_REPOSITORY("1000035"),
    ERROR_DELETING_DOCKER_IMAGE("1000036"),
    WRONG_ENVIRONMENT_GIT_URL("1000037"),
    ILLEGAL_UPLOADED_FILE_SIZE("1000038");

    private final String code;

    /**
     * @param code
     *            error code
     */
    ApplicationManagerExceptionCodes(final String code) {
        this.code = code;
    }

    /**
     * @param text
     *            code in string format
     * @return the equivalent enum
     */
    public ApplicationManagerExceptionCodes getExceptionCodes(final String text) {
        for (final ApplicationManagerExceptionCodes code : ApplicationManagerExceptionCodes.values()) {
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
