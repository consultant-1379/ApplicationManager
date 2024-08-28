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
package com.ericsson.component.aia.sdk.applicationmanager.config;

import java.io.File;
import java.nio.file.Paths;
/**
 * The Interface containing Application Manager Constants.
 */
public interface ApplicationManagerConstants {

    /** The pba application info id. */
    String PBA_APPLICATION_INFO_ID = "pba.applicationInfo.id";

    /** The sdk published application cache folder. */
    String SDK_PUBLISHED_APPLICATION_CACHE_FOLDER = "sdk-published-application-cache-folder";

    /** The sdk application cache folder. */
    String SDK_APPLICATION_CACHE_FOLDER = "sdk-application-cache-folder";

    /** The sdk template cache folder. */
    String SDK_TEMPLATE_CACHE_FOLDER = "sdk-template-cache-folder";

    /** The pba json. */
    String PBA_JSON = "pba.json";

    /** The flow xml. */
    String FLOW_XML = "flow.xml";

    String FORWARD_SLASH = "/";
    String COLON = ":";
    String HYPHEN = "-";
    String ACTIVE_STATUS = "ACTIVE";
    String INACTIVE_STATUS = "INACTIVE";
    String DOCKER_IMAGE_TAR = "DockerImage.tar";

    String DEFAULT_STARTING_VERSION = "0.0.1";
    String APPLICATION_PBA_NAME = "pba.applicationInfo.name";

    String MAAS_BLANK_TEMPLATE_PATH = Paths.get(".").toAbsolutePath().normalize().toString()
                     + File.separator + "target" + File.separator + "MAASBlankTemplate.zip";
    String MAAS_INPUT_SOURCE = "STREAM";
    String APPLICATION = "APPLICATION";
    String SERVICE = "SERVICE";
    String MAAS = "MAAS";
}