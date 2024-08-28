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
package com.ericsson.component.aia.sdk.applicationmanager.util;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * EntryPointMapping generic enum to identify standalone, distributed or source or sink
 */
public enum EntryPointMapping {

    // SOURCE OR SINK DOESNT MATTER
    KAFKA_STANDALONE("standalone", ""), KAFKA_DISTRIBUTED("distributed", ""),

    // local or distributed doesnt matter
    SOURCE("", "source"), SINK("", "sink");

    private static Map<String, EntryPointMapping> lookup = new HashMap<>();
    private String deployMode;
    private String entryPoint;

    /**
     * EntryPointMapping Constructor
     *
     * @param deployMode
     *            deployMode
     * @param entryPoint
     *            entryPoint
     */
    EntryPointMapping(final String deployMode, final String entryPoint) {
        this.deployMode = deployMode;
        this.entryPoint = entryPoint;
    }

    public String getDeployMode() {
        return deployMode;
    }

    public String getEntryPoint() {
        return entryPoint;
    }

    static {
        for (final EntryPointMapping entryPoint : EnumSet.allOf(EntryPointMapping.class)) {
            lookup.put(entryPoint.getDeployMode().toLowerCase() + entryPoint.getEntryPoint(), entryPoint);
        }
    }

    /**
     * getEntryPointMapping method used to retrieve EntryPointMapping based on passed key
     * @param key key
     * @return EntryPointMapping EntryPointMapping
     */
    public static EntryPointMapping getEntryPointMapping(final String key) {
        return lookup.get(key);
    }

}
