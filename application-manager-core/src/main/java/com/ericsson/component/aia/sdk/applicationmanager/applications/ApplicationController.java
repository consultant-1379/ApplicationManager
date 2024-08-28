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
package com.ericsson.component.aia.sdk.applicationmanager.applications;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.sdk.applicationmanager.service.views.ApplicationPath;

/**
 * @author ezsalro
 *
 */
public class ApplicationController {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationController.class);

    private static final ApplicationController INSTANCE = new ApplicationController();

    private final Map<String, ApplicationPath> applications = new ConcurrentHashMap<>();

    private ApplicationController() {
    }

    /**
     * @return instance
     */
    public static ApplicationController instance() {
        return INSTANCE;
    }

    /**
     * @return applications
     */
    public Collection<ApplicationPath> getApplications() {
        return applications.values();
    }

    /**
     * @param pbaId
     *            - pba
     * @param path
     *            - application path
     * @return the generated id
     */
    public String addApplication(final String pbaId, final Path path) {
        final ApplicationPath app = new ApplicationPath(pbaId, path, System.currentTimeMillis());
        final ApplicationPath old = this.applications.put(pbaId, app);
        if (old != null && !old.equals(app)) {
            try {
                LOG.debug("Deleting old application {}", old.getPath().toString());
                FileUtils.forceDelete(old.getPath().toFile());
            } catch (final IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return pbaId;
    }

    /**
     * @param pbaId
     *            - pba
     */
    public void removeApplication(final String pbaId) {
        this.applications.remove(pbaId);
    }

    /**
     * @param pbaId
     *            - pba
     * @return - the path of application
     */
    public Path getApplication(final String pbaId) {
        final ApplicationPath app = this.applications.get(pbaId);
        if (app != null) {
            return app.getPath();
        }
        return null;
    }

}
