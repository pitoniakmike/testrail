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

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import utils.file.FileServices;
import utils.gson.GsonServices;
import utils.logger.LoggerServices;



public class TestRailListener implements ITestListener{
	private static final Logger LOGGER = LoggerFactory.getLogger(new Throwable().getStackTrace()[0].getClassName());
	private TestTrackerProject testTrackerProjectAnnotation = null;
	private Class<?> testClass = null;
	private Method testMethod = null;
	private TestTracker testTrackerAnnotation = null;
	private String testMethodName = null;
	private Integer testCaseId = null;
	private String testCaseName = null;
	private String projectName = null;
	private Integer projectId = null;
	private String version = null;
	private Integer runId = null;
	private String runName = null;
	private String mileStoneName = null;
	private Integer mileStoneId = null;
	private String suiteName = null;
	private Integer suiteId = null;
	private Integer sectionId = null;
	private String sectionName = null;
	private String category = null;
	private String assignedToName = null;
	private Integer assignedToId = null;
	private String comment = null;
	private Boolean enable = null;
	private Boolean publish = null;
	private boolean exception = false;
	private Boolean createProject = null;
	private Boolean createMileStone = null;
	private Boolean createSuite = null;
	private Boolean createRun = null;
	private String steps = null;
	private String preConditions= null;
	private String focus = null;
	private String elapsed = null;
	private String defects = null;
	private String[] additionalTests = null;
	private Boolean isFullyAutomated = null;
	private JsonObject resultsContainer = null;
	private JsonArray resultsArray = null;
	private List<Results> resultsList = new ArrayList();
	private TestRailServices testRailServices = null;
	
	
	/**
	  * Invoked after the test class is instantiated and before
	  * any configuration method is called.
	 */
	//TODO focus should not be part of results just testcase definition...just comment field which holds error
	@Override
	public void onStart(ITestContext context){
		try {
			testRailServices = new TestRailServices("src/main/resources/testrail.properties");
			//TODO better way?
			Class<? extends Object> testClass = context.getAllTestMethods()[0].getInstance().getClass();
			testTrackerProjectAnnotation = testClass.getAnnotation(TestTrackerProject.class);
			projectId = testTrackerProjectAnnotation.projectId();
			projectName = testTrackerProjectAnnotation.projectName();
			runId = testTrackerProjectAnnotation.runId();
			runName = testTrackerProjectAnnotation.runName();
			version = testTrackerProjectAnnotation.version();
			suiteName = testTrackerProjectAnnotation.suiteName();
			suiteId = testTrackerProjectAnnotation.suiteId();
			sectionId = testTrackerProjectAnnotation.sectionId();
			sectionName = testTrackerProjectAnnotation.sectionName();
			mileStoneId = testTrackerProjectAnnotation.mileStoneId();
			mileStoneName = testTrackerProjectAnnotation.mileStoneName();
			category = testTrackerProjectAnnotation.category();
			assignedToName = testTrackerProjectAnnotation.assignedToName();
			assignedToId = testTrackerProjectAnnotation.assignedToId();
			enable = testTrackerProjectAnnotation.enabled();
			publish = testTrackerProjectAnnotation.publish();
			createProject = testTrackerProjectAnnotation.createProject();;
			createMileStone = testTrackerProjectAnnotation.createMileStone();
			createSuite = testTrackerProjectAnnotation.createSuite();
			createRun = testTrackerProjectAnnotation.createRun();
			
			LOGGER.info(LoggerServices.build().bannerWrap("OnStart"));
			
			//i need to get runId to post results
			if(runId==null && publish){
				runId = testRailServices.getResultsBuilder(projectName).runName(runName).build(false).getRunId();
				//runId = getTestRailClient().getRunIdByName(projectName, runName, true);
			}
	
			/*if(publish && testTrackerAnnotation.publish()) {
				LOGGER.info(LoggerServices.build().bannerWrap("Publishing"));
				if(projectId == null) {
					//projectId = getTestRailClient().getProjectIdByName(projectName, true);
					
				}
			}else {
				LOGGER.info(LoggerServices.build().bannerWrap("Not Publishing"));
			}*/
		}catch(Exception e) {
			e.printStackTrace();
			LOGGER.error("{}\n{}", e.getMessage()==null ? "" : e.getMessage(), ExceptionUtils.getStackTrace(e));
		}
		
	}
	
	/**
	  * Invoked each time before a test will be invoked.
	  * The <code>ITestResult</code> is only partially filled with the references to
	  * class, method, start millis and status.
	  *
	  * @param result the partially filled <code>ITestResult</code>
	  * @see ITestResult#STARTED
	  */
	@Override
	public void onTestStart(ITestResult result) {
		try {
			testMethod = result.getMethod().getConstructorOrMethod().getMethod();
			testMethodName = testMethod.getName();
			testTrackerAnnotation = testMethod.getAnnotation(TestTracker.class);
			if (testTrackerAnnotation != null) {
				testCaseId = testTrackerAnnotation.testCaseId();
				testCaseName = testTrackerAnnotation.testCaseName();
				category = testTrackerAnnotation.category();
				comment = testTrackerAnnotation.comment();
				assignedToId = testTrackerAnnotation.assignedToId();
				assignedToName = testTrackerAnnotation.assignedToName();
				steps = testTrackerAnnotation.steps();
				preConditions = testTrackerAnnotation.preConditions();
				focus = testTrackerAnnotation.focus();
				elapsed = testTrackerAnnotation.elapsed();
				defects = testTrackerAnnotation.defects();
				isFullyAutomated = testTrackerAnnotation.isFullyAutomated();
				additionalTests = testTrackerAnnotation.additionalTests();
			}
			
			/*we need runId
			testid
			
			status_id	int	The ID of the test status. The built-in system statuses have the following IDs:
				1	Passed
				2	Blocked
				3	Untested (not allowed when adding a result)
				4	Retest
				5	Failed
				You can get a full list of system and custom statuses via get_statuses.
				comment	string	The comment / description for the test result
				version	string	The version or build you tested against
				elapsed	timespan	The time it took to execute the test, e.g. "30s" or "1m 45s"
				defects	string	A comma-separated list of defects to link to the test result
				assignedto_id	int	The ID of a user the test should be assigned to*/
			
			new ValidationStateMachine(testTrackerProjectAnnotation, testTrackerAnnotation, testClass).validateAnnotations();
			
			if (testTrackerAnnotation != null) {
				if (testTrackerProjectAnnotation == null) {
					throw new Exception("TestTracker Project Annotation not defined on class:{}" + testClass.getName());
				}
				if (testTrackerProjectAnnotation.enabled()) {
					if ((testTrackerProjectAnnotation.publish() && testTrackerAnnotation.publish())) {

						
						if (!testTrackerAnnotation.testCaseName().equals(TestRailConstants.TESTCASE_NAME_DEFAULT)) {
							testCaseName = testTrackerAnnotation.testCaseName();
						} else {
							testCaseName = testMethodName;
						}
						if (!testTrackerAnnotation.category().equals(TestRailConstants.CATEGORY_DEFAULT)) {
							category = testTrackerAnnotation.category();
						} else {
							category = testTrackerProjectAnnotation.category();
						}

						if (testTrackerProjectAnnotation.createMileStone()
								&& !testRailServices.getMileStoneBuilder(mileStoneName).build(false).isExists()) {
							testRailServices.getMileStoneBuilder(mileStoneName)
						    .projectName(projectName)
						    .build(false).add();
							//testRailServices.addMileStone(projectName, mileStoneName, true);
						}
						//TODO finish
						/*if (testTrackerProjectAnnotation.createSuite()
								&& !getTestRailClient().isSuiteExists(projectName, suiteName)) {
							//testRailServices.addSuite(projectName, suiteName, true);
							getTestRailClient().getSuiteBuilder(suiteName).projectName(projectName).build().add(true);
						}

						if (testTrackerProjectAnnotation.createRun()
								&& !getTestRailClient().isRunExists(projectName, runName))
							getTestRailClient().addRun(projectName, runName, null, true);
						
						if (testTrackerProjectAnnotation.createSection()
								&& !getTestRailClient().isSectionExists(projectName, suiteName, sectionName)) {
							getTestRailClient().addSection(projectName, suiteName, sectionName, true);
						}*/

						/*if (testTrackerProjectAnnotation.createTestCase()) {
							if (testTrackerProjectAnnotation.publish()) {
								/*if (testTrackerAnnotation.publish()) {
									if (!getTestRailClient().isTestCaseExists(projectName, suiteName, sectionName,
											testCaseName)) {
										jsonObject = getTestRailClient().addTestCase(NetNumberTestRailTestCase
												.builder(testCaseName)
												.createTestCase(testTrackerProjectAnnotation.createTestCase())
												.project(projectName)
												.version(version)
												.createProject(testTrackerProjectAnnotation.createProject())
												.focus(testTrackerAnnotation.focus()).milestone(mileStoneName)
												.steps(testTrackerAnnotation.steps())
												.preconds(testTrackerAnnotation.preConditions())
												.type(TYPE.Automated)
												.priority(PRIORITY.High).category(CATEGORY.fromValue(category))
												.suiteName(suiteName)
												.createSuite(testTrackerProjectAnnotation.createSuite())
												.sectionName(sectionName)
												.createSection(testTrackerProjectAnnotation.createSection()).build(), true);
										LOGGER.debug("add testCase:{}", GsonServices.build().prettyPrint(jsonObject));
									} else {
										LOGGER.info("TestCase Exists\nProject: " + projectName + "\nSuite: " + suiteName
												+ "\nSection: " + sectionName + "\nTestCaseName: " + testCaseName);
									}
								} else {
									LOGGER.info("publish set to false...");
								}
							} else {
								LOGGER.info("NOT adding TestCase,  TestTrackerProjectAnnotation publish: "
										+ testTrackerProjectAnnotation.publish());
							}
						}*/
					} else {
						LOGGER.info(LoggerServices.build().bannerWrap("TestTrackerProject Annotation disabled"));
					}
				}
			} else {
				LOGGER.info(LoggerServices.build().bannerWrap("TestTrackerProject Annotation disabled"));
			}
		} catch (IllegalArgumentException e) {
			exception = true;
			throw e;
		}catch (Exception e) {
			LOGGER.error("{}\n{}", e.getMessage() == null ? "" : e.getMessage(), ExceptionUtils.getStackTrace(e));
			result.setThrowable(e);
			result.setStatus(ITestResult.FAILURE);
		}
	}
	
	/**
	  * Invoked each time a test succeeds.
	  *
	  * @param result <code>ITestResult</code> containing information about the run test
	  * @see ITestResult#SUCCESS
	 */
	@Override
	public void onTestSuccess(ITestResult result){
		try{
			processTestResults(result);
		}catch(Exception e){
			LOGGER.error("{}\n{}", e.getMessage()==null ? "" : e.getMessage(), ExceptionUtils.getStackTrace(e));
			result.setThrowable(e);
			result.setStatus(ITestResult.SUCCESS);
		}
	}
	
	/**
	  * Invoked each time a test fails.
	  *
	  * @param result <code>ITestResult</code> containing information about the run test
	  * @see ITestResult#FAILURE
	 */
	@Override
	public void onTestFailure(ITestResult result){
		try{
			if(!exception) {
				processTestResults(result);
			}
		}catch(Exception e){
			LOGGER.error("{}\n{}", e.getMessage()==null ? "" : e.getMessage(), ExceptionUtils.getStackTrace(e));
			result.setThrowable(e);
			result.setStatus(ITestResult.FAILURE);
		}
	}
	
	/**
	  * Invoked each time a test is skipped.
	  *
	  * @param result <code>ITestResult</code> containing information about the run test
	  * @see ITestResult#SKIP
	 */
	@Override
	public void onTestSkipped(ITestResult result){
		LOGGER.info("test method " + testMethodName + " skipped");
	}

	/**
	  * Invoked each time a method fails but has been annotated with
	  * successPercentage and this failure still keeps it within the
	  * success percentage requested.
	  *
	  * @param result <code>ITestResult</code> containing information about the run test
	  * @see ITestResult#SUCCESS_PERCENTAGE_FAILURE
	 */
	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result){
		LOGGER.info("test failed but within success % " + testMethodName);
	}

	/**
	  * Invoked after all the tests have run and all their
	  * Configuration methods have been called.
	 */
	//TODO get rid of buffer
	@Override
	public void onFinish(ITestContext context){
		try{
			LOGGER.info(LoggerServices.build().bannerWrap("OnFinish"));
			if(testTrackerProjectAnnotation.publish() && testTrackerAnnotation.publish()){
				LOGGER.info(LoggerServices.build().bannerWrap("Posting Results"));
				String resultsArrayJson =  generateResultsArray(resultsList);
				LOGGER.info("{}",resultsArrayJson);
				testRailServices.addResults(runId, resultsArrayJson);
			}
		}catch(Exception e){
			LOGGER.error("{}\n{}", e.getMessage()==null ? "" : e.getMessage(), ExceptionUtils.getStackTrace(e));
		}
	}
	
	//TODO bug....description picked up from test annotation...should it be testTracker annotation? guard for null and don't set if null to get default message
	/*
	 * 
	 * 	called by onTestSucecss and onTestFailure
	 * 
	 */
	private void processTestResults(ITestResult result) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, ConfigurationException, IOException, ParseException, TestRailConfigException {
		if(!exception) {
			LOGGER.info(LoggerServices.build().bannerWrap("processResults"));
			String status = result.isSuccess() ? TEST_STATUS.PASSED.name() : TEST_STATUS.FAILED.name();
			
			if(testTrackerProjectAnnotation!=null && testTrackerProjectAnnotation.enabled() && testTrackerAnnotation != null){
					LOGGER.info(LoggerServices.build().bannerWrap("Buffering Results"));
					
					LOGGER.debug("suiteName:{}", suiteName);
					LOGGER.debug("sectionName:{}", testTrackerProjectAnnotation.sectionName());
					LOGGER.debug("runName:{}", runName);
					LOGGER.debug("mileStoneName:{}", testTrackerProjectAnnotation.mileStoneName());
					LOGGER.debug("elapsed:{}", elapsed);
					LOGGER.debug("assignedToId:{}", assignedToId);
					LOGGER.debug("assignedToName:{}", assignedToName);
				
					Results results =  Results.builder(testRailServices, projectName)
							.projectId(projectId)
							.mileStoneName(mileStoneName)
							.mileStoneId(mileStoneId)
							.runName(runName)
							.runId(runId)
							.suiteName(suiteName)
							.suiteId(suiteId)
							.sectionName(sectionName)
							.sectionId(sectionId)
							.version(version)
							.testCaseName(testCaseName)
							.testCaseId(testCaseId)
							.assignedToName(assignedToName)
							.assignedToId(assignedToId)
							.testStatus(TEST_STATUS.valueOf(status))
							.comment(comment)
							.build(false);
					LOGGER.debug("results: {}", results);		
					resultsList.add(results);
			}
		}
	}

	/*
	 "results": [
		{
			"case_id": 1,
			"status_id": 5,
			"comment": "This test failed",
			"defects": "TR-7"

		},
		{
			"case_id": 2,
			"status_id": 1,
			"comment": "This test passed",
			"elapsed": "5m",
			"version": "1.0 RC1"
		},
	 */
	/*
	 * 
	 * 	called by onFinish() when tests are done to update or serialize test results
	 * 
	 */
	public String generateResultsArray(List<Results> resultsList) throws IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException, ConfigurationException, TestRailConfigException {
		JsonObject objContainer = new JsonObject();
		JsonArray resultsArray = new JsonArray();
		objContainer.add("results", resultsArray);
		if(publish) {
			//we update all data in each result before it is bulk published on onFinish()
			LOGGER.info(LoggerServices.build().bannerWrap("Updating Buffered Results with TestRail Id's"));
			for(Results results : resultsList) {
				results.update();
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("case_id", results.getTestCaseName());
				jsonObject.addProperty("status_id", results.getStatus().getValue());
				jsonObject.addProperty("comment", results.getComment());
				jsonObject.addProperty("defects", results.getDefects());
				jsonObject.addProperty("elapsed", results.getElapsed());
				jsonObject.addProperty("version", results.getVersion());
			    resultsArray.add(jsonObject);
			}
		    LOGGER.info("{}", GsonServices.build().prettyPrint(objContainer));
		    FileServices.build().write("buffer.json", GsonServices.build().prettyPrint(objContainer), false);
		}else {
			LOGGER.info(LoggerServices.build().bannerWrap("Serializing Buffered Results to file"));
			for(Results results : resultsList) {
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("case_id", results.getTestCaseName());
				jsonObject.addProperty("status_id", results.getStatus().getValue());
				jsonObject.addProperty("comment", results.getComment());
				jsonObject.addProperty("defects", results.getDefects());
				jsonObject.addProperty("elapsed", results.getElapsed());
				jsonObject.addProperty("version", results.getVersion());
			    resultsArray.add(jsonObject);
			}
			LOGGER.info("{}", GsonServices.build().prettyPrint(objContainer));
		    FileServices.build().write("buffer2.json", GsonServices.build().prettyPrint(objContainer), false);
		}
	    return objContainer.toString();
	}

}
