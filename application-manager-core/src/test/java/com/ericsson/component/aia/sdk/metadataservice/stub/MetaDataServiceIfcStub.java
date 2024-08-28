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
package com.ericsson.component.aia.sdk.metadataservice.stub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.aia.metadata.exception.MetaDataServiceException;
import com.ericsson.aia.metadata.filter.Filter;
import com.ericsson.aia.metadata.model.MetaData;

/**
 * Stub implementation of meta data service useful for unit testing
 */
public class MetaDataServiceIfcStub implements MetaDataServiceIfc {

    private final Map<String, Map<String, List<MetaData>>> data = new HashMap<>();

    @Override
    public void createSchema(final String name) throws MetaDataServiceException {
        data.put(name, new HashMap<>());
    }

    @Override
    public boolean schemaExists(final String name) throws MetaDataServiceException {
        return data.containsKey(name);
    }

    @Override
    public String put(final String schemaName, final String key, final String value) throws MetaDataServiceException {

        data.get(schemaName).putIfAbsent(key, new ArrayList<>());

        final MetaData metaData = new MetaData();
        metaData.setKey(key);
        metaData.setValue(value);
        data.get(schemaName).get(key).add(metaData);

        return key;
    }

    @Override
    public String update(final String schemaName, final String key, final String value) throws MetaDataServiceException {
        data.get(schemaName).get(key).clear();
        final MetaData metaData = new MetaData();
        metaData.setKey(key);
        metaData.setValue(value);
        data.get(schemaName).get(key).add(metaData);
        return key;
    }

    @Override
    public String updatePbaFieldValue(final String schemaName, final String key, final String path, final String value)
            throws MetaDataServiceException {
        return null;
    }

    @Override
    public String get(final String schemaName, final String key) throws MetaDataServiceException {
        final List<MetaData> values = data.get(schemaName).get(key);
        if (values == null || values.isEmpty()) {
            throw new MetaDataServiceException("");
        }
        return values.get(0).getValue();
    }

    @Override
    public void delete(final String schemaName, final String key) throws MetaDataServiceException {
        data.get(schemaName).remove(key);
    }

    @Override
    public ArrayList<MetaData> find(final String schema, final String filter) throws MetaDataServiceException {

        final ArrayList<MetaData> result = new ArrayList<>();
        data.get(schema).forEach((key, values) -> {
            result.addAll(values);
        });

        return result;
    }

    @Override
    public ArrayList<MetaData> findAll(final String schema) throws MetaDataServiceException {
        final ArrayList<MetaData> result = new ArrayList<>();
        data.get(schema).forEach((key, values) -> {
            result.addAll(values);
        });

        return result;
    }

    @Override
    public ArrayList<MetaData> findByPropertyValue(final String schema, final String propertyPath, final String propertyValue)
            throws MetaDataServiceException {

        final ArrayList<MetaData> result = new ArrayList<>();
        data.get(schema).forEach((key, values) -> {
            result.addAll(values);
        });

        return result;
    }

    @Override
    public String put(String schemaName, String idPath, String key, String value) throws MetaDataServiceException {
        return this.put(schemaName, key, value);
    }

    @Override
    public Collection<MetaData> findByFilter(String schema, Filter filter) throws MetaDataServiceException {
        final ArrayList<MetaData> result = new ArrayList<>();
        data.get(schema).forEach((key, values) -> {
            result.addAll(values);
        });

        return result;
    }

    public boolean exists(String schemaName, String key) throws MetaDataServiceException {
        return false;
    }

    public void updateDocument(String schemaName, String key, Map<String, Object> fields) throws MetaDataServiceException {

    }

    public Collection<Map<String, Object>> findByFilter(String schema, Filter filter, List<String> columns) throws MetaDataServiceException {
        return null;
    }

    public void put(String schemaName, Map<String, Object> documentMap) throws MetaDataServiceException {

    }

    public String get(String schemaName, String key, String column) throws MetaDataServiceException {
        return null;
    }

}
