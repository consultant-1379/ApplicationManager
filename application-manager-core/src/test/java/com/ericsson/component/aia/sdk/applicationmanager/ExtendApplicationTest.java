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
package com.ericsson.component.aia.sdk.applicationmanager;

import static com.ericsson.component.aia.sdk.applicationmanager.common.Constants.UTF_8_ENCODING;
import static com.ericsson.component.aia.sdk.applicationmanager.tests.common.TestCommons.CURRENT_PATH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.aia.metadata.api.MetaDataServiceIfc;
import com.ericsson.component.aia.sdk.applicationmanager.applications.ApplicationManagerImpl;
import com.ericsson.component.aia.sdk.applicationmanager.config.ApplicationManagerConfiguration;
import com.ericsson.component.aia.sdk.applicationmanager.tests.common.TestCommons;
import com.ericsson.component.aia.sdk.git.exceptions.SdkGitException;
import com.ericsson.component.aia.sdk.git.repo.service.GitSshService;
import com.ericsson.component.aia.sdk.pba.model.PBAInstance;
import com.ericsson.component.aia.sdk.pba.tools.PBASchemaTool;
import com.ericsson.component.aia.sdk.templatemanager.TemplateManager;
import com.ericsson.component.aia.sdk.util.docker.SdkDockerService;

/**
 * ExtendApplicationTest checks all validation scenarios for extending
 * application scenarios.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExtendApplicationTest {

	/** Logger for ExtendApplicationTest */
	private static final Logger Log = LoggerFactory.getLogger(ExtendApplicationTest.class);

	@Mock
	private TemplateManager templateManager;

	@Mock
	private MetaDataServiceIfc metaDataServiceManager;

	@Mock
	private SdkDockerService sdkDockerService;

	@Mock
	private GitSshService gitReadOnlySSHRepositoryService;

	private static final PBASchemaTool pbaSchemaTool = new PBASchemaTool();

	private final PBAInstance pbaInstance = getTemporaryPBAInstance();

	private ApplicationManagerImpl applicationManager;

	@Before
	public void setup() throws Exception {
		ApplicationManagerConfiguration.storageLocation = "target/applicationManager";
		ApplicationManagerConfiguration.applicationCatalogName = "aia-application-catlogs";

		ApplicationManagerConfiguration.gitServiceType = "GOGS";
		ApplicationManagerConfiguration.gitServiceUrl = "http://localhost/api/v1";

		ApplicationManagerConfiguration.gitAccessToken = "fb737170f6a647eeab1e563ce626e8642ae32c3a";
		final Path testRepo = Files.createDirectories(Paths.get("target/TEST_EXTEND_REPO"));

		final Path testRepoZip = testRepo.resolve("aia-flink-streaming-1.0.12.zip");
		Files.copy(Paths.get("src/test/resources/aia-flink-streaming-1.0.12.zip"), testRepoZip);

		applicationManager = ApplicationManagerImpl.builder().templateManager(templateManager)
				.metaDataServiceManager(metaDataServiceManager).pbaSchemaTool(pbaSchemaTool)
				.sdkDockerService(sdkDockerService).gitSSHService(gitReadOnlySSHRepositoryService).build();

		final String pbaAsString = FileUtils.readFileToString(
				new File(TestCommons.CURRENT_PATH + "/src/test/resources/test-project/pba.json"), UTF_8_ENCODING);
		when(metaDataServiceManager.get(anyString(), anyString())).thenReturn(pbaAsString);
		when(gitReadOnlySSHRepositoryService.clone(anyString(), anyString())).thenReturn(testRepo);
	}

	/**
	 * testExtendApplication is a valid test scenario for testing Create-Application
	 * use-case.
	 *
	 * TODO: testing pending
	 */
	@Test
	public void testExtendApplication() throws SdkGitException {
		final Path newApplicationPath = applicationManager.extendApplication("rgf56-76rf-yu45", pbaInstance);
		assertThat("ApplicationManager extendApplication Failed", newApplicationPath.toFile().exists());
	}

	private static PBAInstance getTemporaryPBAInstance() {
		try {
			return new PBASchemaTool().getPBAModelInstance(
					FileUtils.readFileToString(new File(CURRENT_PATH + "/src/test/resources/extended_pba.json")));
		} catch (final IOException e) {
			Log.error("exception occurred, when trying to get a getTemporaryPBAInstance", e);
			throw new RuntimeException(e);
		}
	}
}
