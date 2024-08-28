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
package com.ericsson.component.aia.sdk.applicationmanager.common;

/**
 * This Interface holds Constants which are useful across Application SDK.
 */
public interface Constants {

    /** Default SDK delimiter. eg AppName-version */
    String SDK_DELIMITER = "-";

    /** Constant string for root path. */
    String ROOT_PATH = "/";

    /** Constant string for placeholder in template. */
    String PBA_NAME_CAMELCASE = "pbaNameInCamelCase";

    /** Constant string for placeholder in template. */
    String PBA_NAME_CAMELCASE_WITH_JAVA_EXT = PBA_NAME_CAMELCASE + FileExtensions.JAVA.getExtensionType();

    /** Constant string for placeholder in template. */
    String PBA_NAME = "pbaName";

    /** Constant string for placeholder in template. */
    String PBA_VERSION = "pbaVersion";

    /** Constant string for placeholder in template. */
    String PBA_DESCRIPTION = "pbaDescription";

    /** Constant string for Hypen */
    char CHAR_HYPEN = '-';

    /** Constant string for regex to replace special characters. */
    String REGEX_TO_REPLACE_SPECIAL_CHARACTERS = "[^\\w\\s]";

    /** Constant string for java UTF-8 encoding. */
    String UTF_8_ENCODING = "UTF-8";

    String DEFAULT_STARTING_VERSION = "0.0.0";
}
