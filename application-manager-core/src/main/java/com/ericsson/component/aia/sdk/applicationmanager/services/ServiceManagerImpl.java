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
package com.ericsson.component.aia.sdk.applicationmanager.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.metamodel.query.parser.QueryParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.aia.metadata.exception.MetaDataServiceException;
import com.ericsson.aia.metadata.filter.builder.MetadataFilterBuilder;
import com.ericsson.aia.metadata.filter.builder.model.Operator;
import com.ericsson.aia.metadata.model.MetaData;
import com.ericsson.component.aia.sdk.applicationmanager.api.ServiceManager;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ServiceManagerException;
import com.ericsson.component.aia.sdk.applicationmanager.exceptions.ServiceManagerExceptionCodes;
import com.ericsson.component.aia.sdk.applicationmanager.service.views.DependencyInfo;
import com.ericsson.component.aia.sdk.applicationmanager.util.AppSdkUtil;
import com.ericsson.component.aia.sdk.applicationmanager.views.ServicesView;
import com.ericsson.component.aia.sdk.pba.exception.InvalidPbaException;
import com.ericsson.component.aia.sdk.pba.model.PBAInstance;
import com.ericsson.component.aia.sdk.pba.model.ServiceInfo;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class provides implementations of {@link ServiceManager} operations.
 *
 * @author eanmerr
 *
 */
public class ServiceManagerImpl implements ServiceManager {

    private static final String PBA_SERVICE_INFO_SERVICE_TYPE = "pba.serviceInfo.serviceType";

    private static final String PBA_SERVICE_INFO_TECHNOLOGY = "pba.serviceInfo.technology";

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceManagerImpl.class);

    private static final String SERVICE_PBA_ID_PATH = "pba.serviceInfo.id";

    private String serviceCatalogName;
    private DependencyInformationService dependencyInformationService;
    private MetaDataServiceIfc metaDataService;
    private final PBASchemaTool pbaSchemaTool;
    private final ObjectMapper mapper;

    /**
     * Instantiates a ServiceManagerImpl.
     *
     * @param pbaSchemaTool
     *            {@link PBASchemaTool} used for validating the service PBA structure.
     */
    public ServiceManagerImpl(final PBASchemaTool pbaSchemaTool) {
        this.pbaSchemaTool = pbaSchemaTool;
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public Collection<String> getAvailableTechnologies(final String serviceType) {
        LOGGER.trace("Get Available Technologies method invoked ");
        final Set<String> availibleTechnologies = new HashSet<>();

        try {
            final ArrayList<MetaData> listOfMetaData = metaDataService.findByPropertyValue(serviceCatalogName, PBA_SERVICE_INFO_SERVICE_TYPE,
                    serviceType.toUpperCase());
            for (final MetaData metaData : listOfMetaData) {
                if (metaData.getValue() == null || metaData.getValue().trim().isEmpty()) {
                    continue;
                }
                final PBAInstance pba = mapper.readValue(metaData.getValue(), PBAInstance.class);
                availibleTechnologies.add(pba.getPba().getServiceInfo().getTechnology());
            }
            return availibleTechnologies;

        } catch (final MetaDataServiceException | IOException exp) {
            throw new ServiceManagerException(ServiceManagerExceptionCodes.ERROR_INVOKING_METADATASERVICE_ON_LIST,
                    "List operation failed for templates", exp);
        }
    }

    @Override
    public ServicesView getAvailableTechnologyVersions(final String serviceType, final String technology) {
        LOGGER.trace("List templates method invoked ");
        final ServicesView availableTechnologyVersions = new ServicesView();
        try {

            final Collection<MetaData> listOfMetaData = metaDataService.findByFilter(serviceCatalogName,
                    MetadataFilterBuilder.newMongoDBFilter().addFilter(PBA_SERVICE_INFO_SERVICE_TYPE, Operator.EQ_IGNORE_CASE, serviceType)
                            .addProperty(PBA_SERVICE_INFO_TECHNOLOGY, technology).build());

            for (final MetaData metaData : listOfMetaData) {
                if (StringUtils.isBlank(metaData.getValue())) {
                    continue;
                }

                final ServiceInfo serviceInfo = mapper.readValue(metaData.getValue(), PBAInstance.class).getPba().getServiceInfo();
                availableTechnologyVersions.addServices(serviceInfo);
            }

        } catch (final MetaDataServiceException | IOException exp) {
            throw new ServiceManagerException(ServiceManagerExceptionCodes.ERROR_INVOKING_METADATASERVICE_ON_LIST,
                    "List operation failed for templates", exp);
        }
        return availableTechnologyVersions;
    }

    @Override
    public PBAInstance getServiceInstance(final String serviceId) {
        LOGGER.trace("List templates method invoked ");

        try {
            final ArrayList<MetaData> listOfMetaData = metaDataService.find(serviceCatalogName, serviceId);
            for (final MetaData metaData : listOfMetaData) {
                if (metaData.getValue() == null || metaData.getValue().trim().isEmpty()) {
                    continue;
                }

                return mapper.readValue(metaData.getValue(), PBAInstance.class);
            }

        } catch (final MetaDataServiceException | IOException exp) {
            throw new ServiceManagerException(ServiceManagerExceptionCodes.ERROR_INVOKING_METADATASERVICE_ON_LIST,
                    "List operation failed for services", exp);
        }

        return null;
    }

    @Override
    public String createService(final String servicePba) {
        try {
            final PBAInstance pbaInstance = verifyStructureOfPba(servicePba);
            final String serviceId = validatePbaService(pbaInstance);

            if (StringUtils.isNotEmpty(serviceId)) {
                try {
                    final String pbaSchemma = metaDataService.get(serviceCatalogName, serviceId);
                    if (StringUtils.isNotEmpty(pbaSchemma)) {
                        throw new ServiceManagerException(ServiceManagerExceptionCodes.SERVICE_ALREADY_EXISTS,
                                "Service already exists for ID: " + serviceId);
                    }
                } catch (final MetaDataServiceException | QueryParserException ex) {
                    LOGGER.error(ex.getMessage());
                }
            }

            return metaDataService.put(serviceCatalogName, SERVICE_PBA_ID_PATH, serviceId, servicePba);
        } catch (final MetaDataServiceException exp) {
            throw new ServiceManagerException(ServiceManagerExceptionCodes.ERROR_INVOKING_METADATASERVICE_ON_CREATE,
                    "Create operation failed for service", exp);
        }
    }

    private String validatePbaService(final PBAInstance pba) {
        if (pba.getPba() == null || pba.getPba().getServiceInfo() == null) {
            throw new ServiceManagerException(ServiceManagerExceptionCodes.PBA_IS_INVALID, "ServiceInfo is invalid in PBA");
        }

        final ServiceInfo serviceInfo = pba.getPba().getServiceInfo();

        if (StringUtils.isEmpty(serviceInfo.getServiceType()) || StringUtils.isEmpty(serviceInfo.getTechnology())
                || StringUtils.isEmpty(serviceInfo.getVersion())) {
            throw new ServiceManagerException(ServiceManagerExceptionCodes.PBA_IS_INVALID, "ServiceInfo is invalid in PBA");
        }

        return AppSdkUtil.createServiceId(serviceInfo);
    }

    @Override
    public String updateService(final String pbaId, final String servicePba) {
        try {
            verifyStructureOfPba(servicePba);
            return metaDataService.update(serviceCatalogName, pbaId, servicePba);
        } catch (final MetaDataServiceException exp) {
            throw new ServiceManagerException(ServiceManagerExceptionCodes.ERROR_INVOKING_METADATASERVICE_ON_UPDATE,
                    "Update operation failed for service", exp);
        }
    }

    @Override
    public void deleteService(final String taskId) {
        try {
            metaDataService.delete(serviceCatalogName, taskId);
        } catch (final MetaDataServiceException exp) {
            throw new ServiceManagerException(ServiceManagerExceptionCodes.ERROR_INVOKING_METADATASERVICE_ON_DELETE,
                    "Delete operation failed for service", exp);
        }

    }

    @Override
    public Collection<DependencyInfo> getDependencies(final String pbaId) {
        return dependencyInformationService.getDependencyCollection(pbaId);
    }

    public void setMetaDataService(final MetaDataServiceIfc metaDataService) {
        this.metaDataService = metaDataService;
    }

    public void setServiceCatalogName(final String serviceCatalogName) {
        this.serviceCatalogName = serviceCatalogName;
    }

    public void setDependencyInformationService(final DependencyInformationService dependencyInformationService) {
        this.dependencyInformationService = dependencyInformationService;
    }

    /**
     * This method will throw a runtime exception if the PBA string is not the correct format for PBA.
     *
     * @param servicePba
     *            The PBA to validate in string format
     */
    private PBAInstance verifyStructureOfPba(final String servicePba) {
        try {
            return pbaSchemaTool.getPBAModelInstance(servicePba);
        } catch (final InvalidPbaException ex) {
            throw new ServiceManagerException(ServiceManagerExceptionCodes.PBA_IS_INVALID, ex);
        }
    }

    @Override
    public void addDependencies(final String serviceId, final Collection<DependencyInfo> dependenciesInfo) {
        try {
            processDependencyChange(serviceId, dependenciesInfo, false);
        } catch (final MetaDataServiceException exp) {
            throw new ServiceManagerException(ServiceManagerExceptionCodes.ERROR_INVOKING_METADATASERVICE_WHILE_CHANGING_DEPENDENCIES,
                    "Add Dependencies operation failed for service", exp);
        } catch (final Exception exp) {
            throw new ServiceManagerException(ServiceManagerExceptionCodes.UNKNOW_ERROR, "Error on add Dependencies for service", exp);
        }

    }

    @Override
    public void updateDependencies(final String serviceId, final Collection<DependencyInfo> dependenciesInfo) {
        try {
            processDependencyChange(serviceId, dependenciesInfo, true);
        } catch (final MetaDataServiceException exp) {
            throw new ServiceManagerException(ServiceManagerExceptionCodes.ERROR_INVOKING_METADATASERVICE_WHILE_CHANGING_DEPENDENCIES,
                    "Update Dependencies operation failed for service", exp);
        } catch (final Exception exp) {
            throw new ServiceManagerException(ServiceManagerExceptionCodes.UNKNOW_ERROR, "Error on update Dependencies for service", exp);
        }

    }

    private void processDependencyChange(final String serviceId, final Collection<DependencyInfo> dependenciesInfo, final boolean clear)
            throws MetaDataServiceException {
        if (dependenciesInfo == null || dependenciesInfo.isEmpty()) {
            return;
        }
        final Collection<String> dependencies = dependenciesInfo.stream().filter(dep -> StringUtils.isNotEmpty(dep.getId())).map(dep -> dep.getId())
                .collect(Collectors.toSet());
        final String servicePba = metaDataService.get(serviceCatalogName, serviceId);
        if (StringUtils.isEmpty(servicePba)) {
            throw new ServiceManagerException(ServiceManagerExceptionCodes.PBA_NOT_FOUND, String.format("Service [%s] not found", serviceId));
        }
        final PBAInstance pbaInstance = verifyStructureOfPba(servicePba);
        if (clear) {
            pbaInstance.getPba().getBuildInfo().getDependencies().clear();
        }
        pbaInstance.getPba().getBuildInfo().getDependencies().addAll(dependencies);
        final String schemma = pbaSchemaTool.convertToJsonString(pbaInstance);
        metaDataService.update(serviceCatalogName, serviceId, schemma);
    }

    @Override
    public void clearDependencies(final String serviceId) {
        try {
            final String servicePba = metaDataService.get(serviceCatalogName, serviceId);
            if (StringUtils.isEmpty(servicePba)) {
                throw new ServiceManagerException(ServiceManagerExceptionCodes.PBA_NOT_FOUND, String.format("Service [%s] not found", serviceId));
            }
            final PBAInstance pbaInstance = verifyStructureOfPba(servicePba);
            pbaInstance.getPba().getBuildInfo().getDependencies().clear();
            final String schemma = pbaSchemaTool.convertToJsonString(pbaInstance);
            metaDataService.update(serviceCatalogName, serviceId, schemma);
        } catch (final MetaDataServiceException exp) {
            throw new ServiceManagerException(ServiceManagerExceptionCodes.ERROR_INVOKING_METADATASERVICE_WHILE_CHANGING_DEPENDENCIES,
                    "Clear Dependencies operation failed for service", exp);
        } catch (final Exception exp) {
            throw new ServiceManagerException(ServiceManagerExceptionCodes.UNKNOW_ERROR, "Error on clear Dependencies for service", exp);
        }

    }

}
