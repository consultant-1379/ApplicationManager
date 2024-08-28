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
package com.ericsson.component.aia.sdk.applicationmanager.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.ericsson.component.aia.sdk.pba.model.Arg;
import com.ericsson.component.aia.sdk.pba.model.IntegrationPoint;
import com.ericsson.component.aia.sdk.pba.model.Pba;

/**
 * This view is used to list applications with the technologies used by their integration points.
 *
 */
@SuppressWarnings("PMD.ShortVariable")
public class PbaVersionView {

    private int maturity;
    private int status;
    private String id;
    private String version;

    private Collection<PbaIntegrationPointView> integrationPoints = new ArrayList<>();

    /**
     * Instantiates a new pba version view.
     *
     * @param pba
     *            the pba
     */
    public PbaVersionView(final Pba pba) {
        id = pba.getApplicationInfo().getId();
        version = pba.getApplicationInfo().getVersion();
        integrationPoints = getIntegrationPointTechnologies(pba);
    }

    /**
     * Instantiates a new pba version view filtering out any no allowed technologies.
     *
     * @param pba
     *            the pba
     * @param allowedTechnology
     *            the allowed technology
     */
    public PbaVersionView(final Pba pba, final Set<String> allowedTechnology) {
        id = pba.getApplicationInfo().getId();
        version = pba.getApplicationInfo().getVersion();
        integrationPoints = getIntegrationPointTechnologies(pba, allowedTechnology);
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public Collection<PbaIntegrationPointView> getIntegrationPoints() {
        return integrationPoints;
    }

    public void setIntegrationPoints(final Collection<PbaIntegrationPointView> integrationPoints) {
        this.integrationPoints = integrationPoints;
    }

    public int getMaturity() {
        return maturity;
    }

    public void setMaturity(final int maturity) {
        this.maturity = maturity;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(final int status) {
        this.status = status;
    }

    /**
     * Gets the integration point technologies.
     *
     * @param pba
     *            the pba
     * @return the integration point technologies
     */
    private Collection<PbaIntegrationPointView> getIntegrationPointTechnologies(final Pba pba) {
        final Collection<PbaIntegrationPointView> technologies = new ArrayList<>();

        for (final IntegrationPoint integrationPoint : pba.getIntegrationPoints()) {
            getIntegrationPointView(technologies, integrationPoint);
        }

        return technologies;
    }

    private Collection<PbaIntegrationPointView> getIntegrationPointTechnologies(final Pba pba, final Set<String> allowedTechnology) {
        final Collection<PbaIntegrationPointView> technologies = new ArrayList<>();

        for (final IntegrationPoint integrationPoint : pba.getIntegrationPoints()) {
            if (allowedTechnology.contains(integrationPoint.getTechnology())) {
                getIntegrationPointView(technologies, integrationPoint);
            }
        }

        return technologies;
    }

    private void getIntegrationPointView(final Collection<PbaIntegrationPointView> technologies, final IntegrationPoint integrationPoint) {
        final PbaIntegrationPointView integrationPointView = new PbaIntegrationPointView();
        integrationPointView.setTechnology(integrationPoint.getTechnology());
        integrationPointView.setDescription(integrationPoint.getDescription() == null ? "" : integrationPoint.getDescription());

        final PbaIntegrationPointUriView integrationPointUriView = new PbaIntegrationPointUriView();
        integrationPointUriView.setAddress((String) integrationPoint.getUri().getAddress());
        integrationPointUriView.setProtocol(integrationPoint.getUri().getProtocol());

        final Collection<PbaIntegrationPointUriView> uris = new ArrayList<>();
        final Collection<PbaIntegrationPointUriArgsView> args = new ArrayList<>();

        for (final Arg arg : integrationPoint.getUri().getArgs()) {
            final PbaIntegrationPointUriArgsView argsView = new PbaIntegrationPointUriArgsView();
            argsView.setValue(arg.getValue() == null ? "" : arg.getValue().toString());
            argsView.setKey(arg.getKey() == null ? "" : arg.getKey());
            args.add(argsView);
        }

        integrationPointUriView.setArgs(args);
        integrationPointView.setUri(uris);
        uris.add(integrationPointUriView);
        technologies.add(integrationPointView);
    }

    @Override
    public String toString() {
        return "PbaListView [ id=" + id + ",version=" + version + ", integrationPoints=" + integrationPoints + "]";
    }
}
