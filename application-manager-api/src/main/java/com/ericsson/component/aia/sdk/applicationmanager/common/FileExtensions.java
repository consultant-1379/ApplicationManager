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
 * FileExtensions enum contains different file extensions format like zip, jar, tar etc..
 */
public enum FileExtensions {

    /** The zip. */
    ZIP(".zip"),

    /** The jar. */
    JAR(".jar"),

    /** The java. */
    JAVA(".java");

    /** The extension type. */
    private String extensionType;

    /**
     * Instantiates a new file extensions.
     *
     * @param extensionType
     *            the extension type
     */
    FileExtensions(final String extensionType) {
        this.extensionType = extensionType;
    }

    /**
     * Gets the extension type.
     *
     * @return the extensionType
     */
    public String getExtensionType() {
        return extensionType;
    }
}
