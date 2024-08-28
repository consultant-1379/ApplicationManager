package com.ericsson.component.aia.sdk.applicationmanager.maas.mapper;

import com.ericsson.component.aia.sdk.applicationmanager.model.MAASInstance;

/**
 * SDKMapper is generic interface for mapping files which would be used to generate dynamic configuration files All mappers should be registered here
 */
public interface SDKMapper {

    /**
     * mapper method maps maas values and dynamic placeholders and stores in map
     *
     * @param maasInstance
     *            maasInstance
     */
    void mapper(final MAASInstance maasInstance);

}