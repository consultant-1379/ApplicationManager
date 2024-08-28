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
package com.ericsson.component.aia.sdk.applicationmanager.services;

import static com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConfiguration.applicationCatalogName;
import static com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConfiguration.serviceCatalogName;

import java.util.Collection;
import java.util.Optional;

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.aia.metadata.exception.MetaDataServiceException;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ServiceManagerException;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ServiceManagerExceptionCodes;
import com.ericsson.component.aia.sdk.applicationmanager.service.views.DependencyInfo;
import com.ericsson.component.aia.sdk.pba.model.Pba;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;

/**
 * The Class DependencyInformationService.
 */
public class DependencyInformationServiceImpl implements DependencyInformationService {

    private final MetaDataServiceIfc metaDataServiceManager;
    private final PBASchemaTool pbaSchemaTool;

    /**
     * Instantiates a new dependency information service.
     *
     * @param metaDataServiceManager
     *            the meta data service manager
     * @param pbaSchemaTool
     *            the pba schema tool
     */
    public DependencyInformationServiceImpl(final MetaDataServiceIfc metaDataServiceManager, final PBASchemaTool pbaSchemaTool) {
        this.metaDataServiceManager = metaDataServiceManager;
        this.pbaSchemaTool = pbaSchemaTool;
    }

    /**
     * Gets the dependency info.
     *
     * @param pbaId
     *            the id of the application or service to build a dependency graph for.
     * @return Collection of all dependencies of the application.
     */
    @Override
    public Collection<DependencyInfo> getDependencyCollection(final String pbaId) {
        final MutableGraph<DependencyInfo> graph = GraphBuilder.directed().build();
        addDependency(pbaId, graph);
        return graph.nodes();
    }

    /**
     * Adds the dependency to the accumulator if not already present. It will also recursively add dependencies to the graph.
     *
     * @param pbaId
     *            The id of the dependency to add to the graph
     * @param graph
     *            the graph to add the dependency to.
     * @return the dependency info for the PBA which was added to the graph.
     */
    private DependencyInfo addDependency(final String pbaId, final MutableGraph<DependencyInfo> graph) {
        final DependencyInfo node = getDependencyInfo(pbaId);
        graph.addNode(node);
        node.getDependsOn().forEach(dependencyId -> addEdgeIfItDoesntCauseCycle(graph, node, addDependency(dependencyId, graph)));
        return node;
    }

    private DependencyInfo getDependencyInfo(final String pbaId) {
        final Pba pba = getPba(pbaId);
        return new DependencyInfo(pba);
    }

    private Pba getPba(final String pbaId) {
        final String pbaString = lookForService(pbaId).orElseGet(() -> {
            try {
                return metaDataServiceManager.get(applicationCatalogName, pbaId);
            } catch (final MetaDataServiceException e) {
                throw new ServiceManagerException(ServiceManagerExceptionCodes.PBA_NOT_FOUND,
                        "Error occured when generating dependency graph whilst locating PBA in meta store", e);
            }
        });

        return pbaSchemaTool.getPBAModelInstance(pbaString).getPba();
    }

    private Optional<String> lookForService(final String pbaId) {
        try {
            return Optional.of(metaDataServiceManager.get(serviceCatalogName, pbaId));
        } catch (final MetaDataServiceException e) {
            return Optional.empty();
        }
    }

    private void addEdgeIfItDoesntCauseCycle(final MutableGraph<DependencyInfo> graph, final DependencyInfo nodeOne, final DependencyInfo nodeTwo) {
        graph.putEdge(nodeOne, nodeTwo);
        if (Graphs.hasCycle(graph)) {
            graph.removeEdge(nodeOne, nodeTwo);
            nodeOne.removeDependency(nodeTwo);
        }
    }
}
