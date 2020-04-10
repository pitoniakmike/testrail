/*
 * Copyright (C) 2002 by Michael Pitoniak (pitoniakm@msn.com)
 * All rights are reserved.
 * Reproduction and/or redistribution in whole or in part is expressly
 * prohibited without the written consent of the copyright owner.
 *
 * This Software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */

package utils.testrail;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import utils.log4j.Log4jServices;


@Listeners(TestRailListener.class)
@TestTrackerProject(
		projectName = "apiTestProj",
		projectId = 1,
		version = "1.2.3", 
		mileStoneName = "milesStone5", 
		mileStoneId = 2,
		suiteName = "suite5", 
		suiteId = 3,
		sectionName = "section5", 
		sectionId = 4,
		runName="run5", 
		runId = 5,
		category="Regression", 
		publish=true, 
		isFullyAutomated=true, 
		createMileStone=false, 
		createSuite=false, 
		createRun=false, 
		createSection=false, 
		createTestCase=false)
public class BasicTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(new Throwable().getStackTrace()[0].getClassName());


	@BeforeClass()
	public void beforeClass() throws Exception{
		LogManager.getRootLogger().setLevel(Level.DEBUG);
	}

	@TestTracker()
	@Test( )
	public void basicTest() throws Exception{
		LOGGER.info("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
		Assert.assertTrue(true);
	}

	@TestTracker(assignedToName = "mike", elapsed = "30s", preConditions = "myPreConds", focus="myFocus", comment = "foobar")
	@Test( dependsOnMethods={"basicTest"})
	public void basicTest2() throws Exception{
		LOGGER.info("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
		Assert.assertTrue(true);
	}
	
	
}
