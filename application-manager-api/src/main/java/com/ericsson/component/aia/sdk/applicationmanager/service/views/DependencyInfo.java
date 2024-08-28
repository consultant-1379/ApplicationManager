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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.ericsson.component.aia.sdk.pba.model.Pba;
import com.ericsson.component.aia.sdk.pba.model.PbaInfo;
import com.ericsson.component.aia.sdk.pba.model.ServiceInfo;

/**
 * The Class DependencyInfo.
 */
public class DependencyInfo {

    private String id;
    private String name;
    private String version;
    private String description;
    private Set<String> dependsOn = new HashSet<>();

    /**
     * Default constructor.
     */
    public DependencyInfo() {

    }

    /**
     * Instantiates a new dependency info.
     *
     * @param pba
     *            the pba
     */
    public DependencyInfo(final Pba pba) {
        final Optional<ServiceInfo> pbaServiceInfo = Optional.ofNullable(pba.getServiceInfo());
        pbaServiceInfo.ifPresent((serviceInfo) -> {
            this.id = serviceInfo.getId();
            this.name = serviceInfo.getTechnology();
            this.version = serviceInfo.getVersion();
            this.description = serviceInfo.getDescription();
        });

        final Optional<PbaInfo> pbaApplicationInfo = Optional.ofNullable(pba.getApplicationInfo());
        pbaApplicationInfo.ifPresent((applicationInfo) -> {
            this.id = applicationInfo.getId();
            this.name = applicationInfo.getName();
            this.version = applicationInfo.getVersion();
            this.description = applicationInfo.getDescription();
        });

        dependsOn = new HashSet<>(pba.getBuildInfo().getDependencies());
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public Set<String> getDependsOn() {
        return dependsOn;
    }

    /**
     * This method will remove the dependency on the specified dependencyInfo.
     *
     * @param dependencyInfo
     *            The object to remove the dependency on.
     * @return true, if successful
     */
    public boolean removeDependency(final DependencyInfo dependencyInfo) {
        return dependsOn.remove(dependencyInfo.getId());
    }

    @Override
    public String toString() {
        return "DependencyInfo [id=" + id + ", name=" + name + ", version=" + version + ", description=" + description + ", dependsOn=" + dependsOn
                + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DependencyInfo)) {
            return false;
        }
        final DependencyInfo other = (DependencyInfo) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

}
