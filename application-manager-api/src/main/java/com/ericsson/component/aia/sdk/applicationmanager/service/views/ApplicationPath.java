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
package com.ericsson.component.aia.sdk.applicationmanager.service.views;

import java.nio.file.Path;

/**
 * @author ezsalro
 *
 */
public class ApplicationPath {

    private final String pbaId;
    private final Path path;
    private final long timestamp;

    /**
     * @param pbaId
     *            - pba
     * @param path
     *            - path
     * @param timestamp
     *            - created when
     */
    public ApplicationPath(final String pbaId, final Path path, final long timestamp) {
        super();
        this.pbaId = pbaId;
        this.path = path;
        this.timestamp = timestamp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((pbaId == null) ? 0 : pbaId.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        final ApplicationPath other = (ApplicationPath) obj;
        if (path == null && other.path != null || pbaId == null && other.pbaId != null) {
            return false;
        }

        return (path.equals(other.path) && pbaId.equals(other.pbaId));
    }

    /**
     * @return pba id
     */
    public String getPbaId() {
        return pbaId;
    }

    /**
     * @return path
     */
    public Path getPath() {
        return path;
    }

    /**
     * @return timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

}
