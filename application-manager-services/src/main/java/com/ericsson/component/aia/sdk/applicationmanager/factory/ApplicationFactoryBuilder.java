package com.ericsson.component.aia.sdk.applicationmanager.factory;

import com.ericsson.component.aia.sdk.applicationmanager.api.ApplicationManager;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.templatemanager.cache.ArchivePathCache;

/**
 * ApplicationFactoryBuilder contains ApplicationManager, PBASchemaTool and ArchivePathCache
 *
 */
public class ApplicationFactoryBuilder {


    private final ApplicationManager applicationManager;
    private final PBASchemaTool pbaSchemaTool;
    private final ArchivePathCache simpleCache;

    /**
     * ApplicationFactoryBuilder initializes
     * @param applicationManager
     *           applicationManager
     * @param pbaSchemaTool
     *           applicationManager
     */
    public ApplicationFactoryBuilder(final ApplicationManager applicationManager, final PBASchemaTool pbaSchemaTool) {
        this.applicationManager = applicationManager;
        this.pbaSchemaTool = pbaSchemaTool;
        //TODO:: inject from beans
        this.simpleCache = new ArchivePathCache();
    }

    public ApplicationManager getApplicationManager() {
        return applicationManager;
    }

    public PBASchemaTool getPbaSchemaTool() {
        return pbaSchemaTool;
    }

    public ArchivePathCache getSimpleCache() {
        return simpleCache;
    }

}