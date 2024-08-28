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
package com.ericsson.component.aia.sdk.applicationmanager.util;

import static com.ericsson.component.aia.sdk.applicationmanager.common.Constants.CHAR_HYPEN;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.sdk.pba.model.PBAInstance;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;

public class PbaToFlowXmlConverterTest {

    private static final String CURRENT_PATH = Paths.get(".").toAbsolutePath().normalize().toString();

    /** Logger for PbaToFlowXmlConverterTest */
    private static final Logger Log = LoggerFactory.getLogger(PbaToFlowXmlConverterTest.class);

    /**
     * Test method for
     * {@link com.ericsson.component.aia.sdk.applicationmanager.util.PbaToFlowXmlConverter#createFlowFromPba(com.ericsson.component.aia.sdk.model.pba.PBAInstance)}
     * .
     */
    @Test
    public void testCreateFlowFromPba() {
        final PBAInstance pbaInstance = getTemporaryPBAInstance();
        final String flowXml = PbaToFlowXmlConverter.createFlowFromPba(pbaInstance);
        assertThat("PbaToFlowXmlConverter createFlowFromPba Failed", StringUtils.isNotEmpty(flowXml));
    }

    /**
     * testForPassingNull is null check.
     */
    @Test(expected = NullPointerException.class)
    public void testForPassingNull() {
        PbaToFlowXmlConverter.createFlowFromPba(null);
    }

    /**
     * testForPassingNull is null check.
     */
    @Test
    public void asc() {
        System.out.println(WordUtils.capitalize("x-12-e", CHAR_HYPEN).replaceAll("[^a-zA-Z0-9]", ""));
    }

    private static PBAInstance getTemporaryPBAInstance() {
        try {
            return new PBASchemaTool().getPBAModelInstance(FileUtils.readFileToString(new File(CURRENT_PATH + "/src/test/resources/pba-good.json")));
        } catch (final IOException e) {
            Log.error("exception occurred, when trying to get a getTemporaryPBAInstance", e);
            throw new RuntimeException(e);
        }
    }
}
