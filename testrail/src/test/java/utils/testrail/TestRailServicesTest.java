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
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import utils.gson.GsonServices;

import static org.testng.Assert.*;


public class TestRailServicesTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(new Throwable().getStackTrace()[0].getClassName());
    private TestRailServices testRailServices = null;
    private Integer projectId = null;
    private Integer suiteId = null;

    //https://itriagehealth.testrail.com/index.php?/dashboard
    //https://example.testrail.com/index.php?/api/v2/get_case/1
    //https://itriagehealth.testrail.net//index.php?/api/v2/get_projects%22
    
    @BeforeClass()
    public void beforeClass() {
        LogManager.getRootLogger().setLevel(Level.INFO);
        testRailServices = TestRailServices.builder("https://itriagehealth.testrail.net/index.php?/api/v2").userName("Trustthepr0cess!")
                .passWord("Trustthepr0cess!")
                .retryCnt(3)
                .build();

        //testRailServices = new TestRailServices("src/main/resources/testrail.properties");
        //projectId = testRailServices.getProjectIdByName("apiTestProj", true);
    }
    
    @Test
    public void addProjectTest() throws IOException, ParseException, TestRailConfigException {
        JsonObject jsonObject = testRailServices.getProjectBuilder("projectName")
            .announcement("announcement")
            .showAnnouncement(true)
            .suiteMode(SUITE_MODE.MultipleSuites)
            .build(true)
            .add(true);
        LOGGER.info("\n{}", GsonServices.build().prettyPrint(jsonObject));
    }
    
    @Test
    public void getRunBuilderTest() throws IOException, TestRailConfigException {
        Run run = testRailServices.getRunBuilder("projectName", "runName")
            .description("description")
            .projectId(3)
            .mileStoneId(4)
            .assignedTo(5)
            .includeAll(true)
            .suiteId(40)
            .refs("refs")
            .build(true);
        LOGGER.info("\n{}", GsonServices.build().prettyPrint(run.toJson()));
    }
    
    @Test
    public void getPlanBuilderTest() throws IOException, TestRailConfigException {
        Plan plan = testRailServices.getPlanBuilder("mileStoneNane")
            .description("description")
            .projectId(1)
            .build(true);
        LOGGER.info("\n{}", GsonServices.build().prettyPrint(plan.toJson()));
    }
    
    
    @Test
    public void getMileStoneBuilderTest() throws IOException, ParseException, TestRailConfigException {
        MileStone mileStone = testRailServices.getMileStoneBuilder("mileStoneNane")
            .description("description")
            .projectId(1)
            .build(true);
        LOGGER.info("\n{}", GsonServices.build().prettyPrint(mileStone.toJson()));
    }
    
    @Test
    public void getSuiteBuilderTest() throws IOException, TestRailConfigException {
        Suite suite = testRailServices.getSuiteBuilder("suiteName")
            .description("description")
            .projectId(1)
            .build(true);
        LOGGER.info("\n{}", GsonServices.build().prettyPrint(suite.toJson()));
    }
    
    @Test
    public void getSectionBuilderTest() throws IOException, TestRailConfigException {
        Section section = testRailServices.getSectionBuilder("suiteName")
            .description("description")
            .projectId(1)
            .suiteId(2)
            .build(true);
        LOGGER.info("\n{}", GsonServices.build().prettyPrint(section.toJson()));
    }
    
    
    /*.add("title", testCaseName);
    
    if(templateId!=null){
    	jsonObjectBuilder.add("template_id",  templateId);
	}
    if(typeId!=null){
    	jsonObjectBuilder.add("type_id",  typeId);
	}
    if(priorityId!=null){
    	jsonObjectBuilder.add("priority_id",  priorityId);
	}
    if(estimate!=null){
    	jsonObjectBuilder.add("estimate",  estimate);
	}
    if(mileStoneId!=null){
    	jsonObjectBuilder.add("milestone_id",  mileStoneId);
	}
    if(refs!=null){
    	jsonObjectBuilder.add("refa",  refs);
	}*/
    @Test
    public void getTestCaseBuilderTest() throws IOException, TestRailConfigException {
        TestCase testCase = testRailServices.getTestCaseBuilder("TestCase")
            .projectId(1)
            .suiteId(2)
            .sectionId(3)
            .testCaseId(4)
            .mileStoneId(1)
            .projectId(2)
            .priorityId(3)
            .templateId(5)
            .estimate("estimate")
            .refs("refs")
            .typeId(6)
            .build(true);
        LOGGER.info("\n{}", GsonServices.build().prettyPrint(testCase.toJson()));
    }
    
    @Test
    public void getResultsBuilderTest() throws IOException, TestRailConfigException {
        Results results = testRailServices.getResultsBuilder("projectName")
        		.projectId(1)
        		.suiteId(2)
        		.testCaseId(3)
        		.sectionId(4)
        		.assignedToId(5)
        		.assignedToId(1)
        		.assignedToName("mike")
        		.comment("comment")
        		.defects("defects")
        		.version("version")
        		.elapsed("elapsed")
        		.testStatus(TEST_STATUS.PASSED)
        		.build(true);
        
        LOGGER.info("\n{}", GsonServices.build().prettyPrint(results.toJson()));
    }
    
    
    
    @Test
    public void getProjectsTest() throws IOException {
        JsonArray jsonArray = testRailServices.getProjects();
        LOGGER.info("{}", GsonServices.build().prettyPrint(jsonArray));
    }
    
    @Test()
    public void isProjectExistsTest() throws IOException, TestRailConfigException{
        assertTrue(testRailServices.isProjectExists("apiTestProj"));
    }
    
    @Test
    public void getProjectByNameTest() throws TestRailConfigException, IOException{
        JsonObject jsonObject = testRailServices.getProjectByName("apiTestProj", true);
        LOGGER.info("{}", GsonServices.build().prettyPrint(jsonObject));
    }
    
    @Test
    public void projectBuilderTest() throws IOException, TestRailConfigException{
        Project  project = testRailServices.getProjectBuilder("apiTestProj").build(true);
        LOGGER.info("{}", project);
    }

    @Test
    public void getRunsTest() throws IOException {
        JsonArray jsonArray = testRailServices.getRuns(projectId);
        LOGGER.info("{}", GsonServices.build().prettyPrint(jsonArray));
    }
    
    @Test()
    public void isRunExistsTest() throws IOException, TestRailConfigException{
    	assertTrue(testRailServices.getRunBuilder("projectName", "runName").build(true).isExists());
    }
    
    @Test
    public void getRunByNameTest() throws TestRailConfigException, IOException{
        JsonObject jsonObject = testRailServices.getRunBuilder("projectName", "runName").build(true).getRunByName(true);
        LOGGER.info("{}", GsonServices.build().prettyPrint(jsonObject));
    }
    
    @Test
    public void runBuilderTest() throws IOException, TestRailConfigException{
        Project  project = testRailServices.getProjectBuilder("apiTestProj").build(true);
        LOGGER.info("{}", project);
    }
  
    @Test
    public void geMileStonesTest() throws IOException {
        JsonArray jsonArray = testRailServices.getMileStones(projectId);
        LOGGER.info("{}", GsonServices.build().prettyPrint(jsonArray));
    }
    
    @Test
    public void geSuitesTest() throws IOException {
        JsonArray jsonArray = testRailServices.getSuites(projectId);
        LOGGER.info("{}", GsonServices.build().prettyPrint(jsonArray));
    }
    
    @Test
    public void geSectionsTest() throws IOException {
        JsonArray jsonArray = testRailServices.getSections(projectId, suiteId);
        LOGGER.info("{}", GsonServices.build().prettyPrint(jsonArray));
    }
    
    @Test
    public void gePlansTest() throws IOException {
        JsonArray jsonArray = testRailServices.getPlans(projectId);
        LOGGER.info("{}", GsonServices.build().prettyPrint(jsonArray));
    }
    
    @Test
    public void getTestCasesTest() throws IOException {
        JsonArray jsonArray = testRailServices.getTestCases(projectId, suiteId, 1);
        LOGGER.info("{}", GsonServices.build().prettyPrint(jsonArray));
    }
    
    

    
    
    
    @Test()
    public void getProjectTest3() throws IOException{
        JsonObject jsonObject = testRailServices.getProject(566, false);
        LOGGER.info("{}", GsonServices.build().prettyPrint(jsonObject));
    }
    
    @Test()
    public void getProjectTest() throws IOException{
        JsonArray jsonArray = testRailServices.getProjects("apiTestProj");
        assertEquals(jsonArray.size(), 1);
        LOGGER.info("{}", GsonServices.build().prettyPrint(jsonArray));
    }
    
    @Test()
    public void addMileStoneTest() throws IOException, ParseException, TestRailConfigException{
        JsonObject jsonObject =testRailServices.getMileStoneBuilder("mileStoneNane")
                .description("description")
                .projectId(1)
                .build(true)
                .add();
        assertNotNull(jsonObject);
        LOGGER.info("{}", GsonServices.build().prettyPrint(jsonObject));
    }
    
    @Test()
    public void mileStoneBuilderTest() throws ParseException, TestRailConfigException, IOException{
        LOGGER.info("\n{}", GsonServices.build().prettyPrint(testRailServices.getMileStoneBuilder("apiTestProj")
        .projectId(4)
        .description("descr")
        .dueOn("20200401")
        .build(true).toJson()));
    }
    
    @Test()
    public void addMileStoneBuilderTest() throws ParseException, TestRailConfigException, IOException{
        LOGGER.info("\n{}", GsonServices.build().prettyPrint(testRailServices.getMileStoneBuilder("apiTestProj")
        .projectId(4)
        .description("descr")
        .dueOn("20200401")
        .build(true).add()));
    }
    
    @Test()
    public void isMileStoneExistsTest() throws IOException, TestRailConfigException{
    	testRailServices.getMileStoneBuilder("milestone1").projectName("apiTestProj").build(true).isExists();
    }
    
    @Test()
    public void getMileStoneTest() throws TestRailConfigException, IOException{
    	LOGGER.info("{}", testRailServices.getMileStoneBuilder("milestone1").projectName("apiTestProj").build(true).getMileStone(true));
    }
    
    @Test()
    public void getSuiteByNameTest() throws TestRailConfigException, IOException{
        Integer suiteId = testRailServices.getSuiteBuilder("apiTestProj")
                .description("descr").build(true).getSuiteIdByName("suiteName", true);
       
        LOGGER.info("{}", suiteId);
    }
    
    
    
    
    
    @Test()
    public void updateResultsTest() throws TestRailConfigException, IOException, ParseException{
        testRailServices.getResultsBuilder("projectName")
        .sectionId(1)
        .suiteId(2)
        .defects("defects")
        .build(true)
        .addResultForCase();
        
        
    }
    
    /*
    @Test(dependsOnMethods = {"getProjectTest"})
    public void addMileStoneTest(){
        JsonObject jsonObject = testRailServices.addMileStone("apiTestProj", "milestone1", "descr", "20150803", true);
        Assert.assertTrue(jsonObject!=null);
        LOGGER.info("{}", GsonServices.build().prettyPrint(jsonObject));
    }
    
    
    
    

    @Test(dependsOnMethods = {"getMileStoneTest"})
    public void addSuiteTest(){
        JsonObject jsonObject = testRailServices.addSuite("apiTestProj", "suite1", "description", true);
        Assert.assertTrue(jsonObject!=null);
        LOGGER.info("{}", GsonServices.build().prettyPrint(jsonObject));
    }
    
    @Test(dependsOnMethods = {"addSuiteTest"})
    public void isSuiteExistsTest(){
        Assert.assertTrue(testRailServices.isSuiteExists("apiTestProj", "suite1"));
    }
    
    
    
    @Test(dependsOnMethods = {"getSuiteByNameTest"})
    public void addSectionTest(){
        
        JsonObject jsonObject = testRailServices.addSection("apiTestProj", "suite1", "section1", "description", true);
        Assert.assertTrue(jsonObject!=null);
        LOGGER.info("{}", GsonServices.build().prettyPrint(jsonObject));
    }
    
    @Test(dependsOnMethods = {"addSectionTest"})
    public void isSectionExistsTest(){
        Assert.assertTrue(testRailServices.isSectionExists("apiTestProj", "suite1", "section1"));
    }
    
    //public Integer getSectionIdByName(Integer projectId, Integer suiteId, String sectionName, boolean bFailOnNotFound)
    @Test(dependsOnMethods = {"isSectionExistsTest"})
    public void getSectionTest(){
        Integer sectionId = testRailServices.getSectionIdByName("apiTestProj", "suite1", "section1", true);
        Assert.assertTrue(sectionId!=null);
        LOGGER.info("sectionId: {}", sectionId);
    }
    
    @Test(dependsOnMethods = {"getSectionTest"})
    public void addTestCaseTest(){
        LOGGER.info("PROJECTS: {}", GsonServices.build().prettyPrint(testRailServices.getProjects()));
        NetNumberTestRailTestCase netNumberTestRailTestCase = NetNumberTestRailTestCase.builder("xxxx")
                .project("apiTestProj")
                .milestone("milestone1")
                .assignedToName("Rick Turmel")
                .type(TYPE.Automated)
                .category(CATEGORY.Performance)
                .suiteName("suite1")
                .sectionName("section1")
                .priority(PRIORITY.High)
                .focus("xxx test focus")
                .steps("steps")
                .preconds("preconds")
                .expected("expected")
                .category(CATEGORY.Performance)
                .build();

        JsonObject jsonObject = testRailServices.addTestCase(netNumberTestRailTestCase, true);
        LOGGER.info("{}", GsonServices.build().prettyPrint(jsonObject));
    }
    
    @Test()
    public void addTestCaseNoSectionTest(){
        LOGGER.info("PROJECTS: {}", GsonServices.build().prettyPrint(testRailServices.getProjects()));
        NetNumberTestRailTestCase netNumberTestRailTestCase = NetNumberTestRailTestCase.builder("noSectionTest")
                .project("apiTestProj")
                .milestone("milestone1")
                .assignedToName("Rick Turmel")
                .type(TYPE.Automated)
                .category(CATEGORY.Performance)
                .suiteName("suite1")
                .priority(PRIORITY.High)
                .focus("xxx test focus")
                .steps("steps")
                .preconds("preconds")
                .expected("expected")
                .category(CATEGORY.Performance)
                .build();

        JsonObject jsonObject = testRailServices.addTestCase(netNumberTestRailTestCase, true);
        LOGGER.info("{}", GsonServices.build().prettyPrint(jsonObject));
    }
    
    @Test(dependsOnMethods = {"addTestCaseTest"})
    public void isTestCaseExistsTest(){
        Assert.assertTrue(testRailServices.isTestCaseExists("apiTestProj", "suite1", "section1", "mikeTest"));
    }

    @Test(dependsOnMethods = {"isTestCaseExistsTest"})
    public void addRunTest(){
        JsonObject jsonObject = testRailServices.addRun("apiTestProj", "milestone1", "run1", "suite1", "description", true);
        Assert.assertTrue(jsonObject!=null);
        LOGGER.info("run: {}", GsonServices.build().prettyPrint(jsonObject));
    }
    
    @Test(dependsOnMethods = {"addRunTest"})
    public void getRunTest(){
        Integer runId = testRailServices.getRunIdByName("apiTestProj", "milestone1", "run1", "suite1", true);
        Assert.assertTrue(runId!=null);
    }
    
    @Test(dependsOnMethods = {"getRunTest"})
    public void postResultsTest(){
        JsonObject jsonObject = testRailServices.postTestResults("apiTestProj", "milestone1", "run1", "suite1", "section1", "mikeTest", "description", "Rick Turmel", "1.1.1", null, null, TEST_STATUS.PASSED, true);
        Assert.assertTrue(jsonObject!=null);
        LOGGER.info("run: {}", GsonServices.build().prettyPrint(jsonObject));
    }
    
    @Test(dependsOnMethods = {"postResultsTest"})
    public void getResultsTest(){
        JSONArray jSONArray = testRailServices.getResults("apiTestProj", "milestone1", "run1", "suite1", "section1", "mikeTest");
        Assert.assertTrue(jSONArray.size()==1);
        LOGGER.info("run: {}", GsonServices.build().prettyPrint(jSONArray));
    }

    @Test(dependsOnMethods = {"getResultsTest"})
    public void deleteRunTest(){
        JsonObject jsonObject = testRailServices.deleteRun("apiTestProj", "milestone1", "run1", "suite1", true);
        Assert.assertTrue(jsonObject!=null);
        LOGGER.info("run: {}", GsonServices.build().prettyPrint(jsonObject));
    }
    
    @Test(dependsOnMethods = {"deleteRunTest"})
    public void deleteTestCaseTest(){
        JsonObject jsonObject = testRailServices.deleteTestCase("apiTestProj", "suite1", "section1", "mikeTest", true);
        Assert.assertTrue(jsonObject!=null);
        LOGGER.info("run: {}", GsonServices.build().prettyPrint(jsonObject));
    }
    
    @Test(dependsOnMethods = {"deleteTestCaseTest"})
    public void deleteSectionTest(){
        JSONArray jSONArray = testRailServices.deleteSection("apiTestProj", "suite1", "section1", true);
        Assert.assertTrue(jSONArray.size()==1);
        LOGGER.info("run: {}", GsonServices.build().prettyPrint(jSONArray));
    }

    @Test(dependsOnMethods = {"deleteSectionTest"})
    public void deleteSuiteTest(){
        JSONArray jSONArray = testRailServices.deleteSuite("apiTestProj", "suite1");
        Assert.assertTrue(jSONArray.size()==1);
        LOGGER.info("run: {}", GsonServices.build().prettyPrint(jSONArray));
    }

    @Test(dependsOnMethods = {"deleteSuiteTest"})
    public void deleteProjectTest(){
        JSONArray jSONArray = testRailServices.deleteProject("apiTestProj");
        Assert.assertTrue(jSONArray.size()==1);
        LOGGER.info("run: {}", GsonServices.build().prettyPrint(jSONArray));
    }


    @Test
    public void getTestCaseTypesTest() throws IOException, APIException, ConfigurationException{
        LOGGER.info("{}", GsonServices.build().prettyPrint(testRailServices.getTestCaseTypes()));
    }

    @Test
    public void getTestCaseTypeByNameTest() throws IOException, APIException, ConfigurationException, TestRailConfigException{
        LOGGER.info("{}", testRailServices.getTestCaseTypeIdByName("Automated", true));
    }
    
    @Test
    public void getTestCaseFieldsTest(){
        LOGGER.info("{}", GsonServices.build().prettyPrint(testRailServices.getTestCaseFields()));
    }
    
    @Test
    public void getTestCasesTest(){
        LOGGER.info("{}", testRailServices.getTestCases("TITAN - Demo", "Performance", null));
    }
    
    @Test
    public void generateTestCaseTest(){
        JSONArray jSONArray = testRailServices.getTestCases("TITAN - Demo", "Performance", null);
        LOGGER.info("{}", testRailServices.generateTestCase(jSONArray));
    }
    
    @Test
    public void getTestCaseTest(){
        JSONArray jSONArray = testRailServices.getTestCases("apiTestProj", "suite1", null);
        LOGGER.info("{}", jSONArray);
    }
    
    @Test
    public void getTestCaseTest2(){
        LOGGER.info("{}", testRailServices.getTestCase(74));
    }
    
    @Test
    public void getTestCaseIdTest2(){
        LOGGER.info("{}", testRailServices.getTestCaseIdByName("TITAN - Demo", "Performance", "ENUM", "ENUM, 1 - normal form, IPV4, Solaris", true));
    }
    
    @Test
    public void getUsersTest(){
        LOGGER.info("{}", GsonServices.build().prettyPrint(testRailServices.getUsers()));
    }
    
    @Test
    public void getTestUserIdByNameTest(){
        LOGGER.info("{}", testRailServices.getUserIdByName("Rick Turmel", true));
    }
    
    @Test
    public void getTestUserIdByNameFailTest(){
        LOGGER.info("{}", testRailServices.getUserIdByName("xxxx", true));
    }

    @Test
    public void isTestCaseExistsTest2(){
        LOGGER.info("{}", testRailServices.isTestCaseExists("TITAN - Demo", "Performance", "ENUM", "ENUM, 1 - normal form, IPV4, Solaris"));
    }
    
    @Test
    public void isSectionExistsTest2(){
        LOGGER.info("{}", testRailServices.isSectionExists("TITAN - Demo", "Performance", "ENUMs"));
    }
    
    @Test
    public void isSectionExistsLoadTest(){
        for(int i=0; i<100; i++){
            try{
                Assert.assertTrue(testRailServices.isSectionExists("TITAN - Demo", "Performance", "ENUM"));
            }catch(APIException e){
                LOGGER.info(e.toString());
                Assert.assertTrue(e.getMessage().contains("section exists") | e.getMessage().contains("TestRail API return HTTP 429"));
            }
            
        }
    }
    
    @Test
    public void isSuiteExistsTest2(){
        LOGGER.info("{}", testRailServices.isSuiteExists("TITAN - Demo", "Performance"));
    }
    
    @Test
    public void isSuiteExistsLoadTest(){
        for(int i=0; i<100; i++){
            Assert.assertTrue(testRailServices.isSuiteExists("TITAN - Demo", "Performance"));
        }
    }
    
    @Test
    public void addSuiteLoadLoadTest(){
        for(int i=0; i<100; i++){
            try{
                Assert.assertTrue(testRailServices.addSuite("TITAN - Demo", "Performance", "", true)==null);
            }catch(APIException e){
                LOGGER.info(e.toString());
                Assert.assertTrue(e.getMessage().contains("suite exists") | e.getMessage().contains("TestRail API return HTTP 429"));
            }
        }
    }
    
    @Test
    public void isProjectExistsTest2(){
        LOGGER.info("{}", testRailServices.isProjectExists("TITAN - Demo"));
    }
    
    @Test
    public void addMileStoneTest2(){
        LOGGER.info("{}", GsonServices.build().prettyPrint(testRailServices.addMileStone("TITAN - Demo", "mikeTest", "descr", "20150803", true)));
    }
    
    @Test
    public void updateMileStoneTest(){
        LOGGER.info("{}", GsonServices.build().prettyPrint(testRailServices.updateMileStone("TITAN - Demo", "mikeTest", "descr", "20150803", false)));
    }
    
    @Test
    public void addTestCaseInNewSuiteTest(){
        String PROJECT = "TITAN - Demo";
        String SUITE = "Performance3";
        String SECTION = "sectionName";

        testRailServices.addSuite(PROJECT, SUITE, "descr", true);
        testRailServices.addSection(PROJECT, SUITE, SECTION, "description", true);
        
        NetNumberTestRailTestCase netNumberTestRailTestCase = NetNumberTestRailTestCase.builder("mikeTest1")
                .project(PROJECT)
                .milestone("7.6.1")
                .type(TYPE.Automated)
                .category(CATEGORY.Performance)
                .suiteName(SUITE)
                .sectionName("sectionName")
                .priority(PRIORITY.High)
                .focus("test focus")
                .category(CATEGORY.Performance)
                .build();

        JsonObject jsonObject = testRailServices.addTestCase(netNumberTestRailTestCase, true);
        LOGGER.info("{}", GsonServices.build().prettyPrint(jsonObject));
    }
    
    @Test
    public void addTestCaseTest2(){
        LOGGER.info("PROJECTS: {}", GsonServices.build().prettyPrint(testRailServices.getProjects()));
        NetNumberTestRailTestCase netNumberTestRailTestCase = NetNumberTestRailTestCase.builder("mikeTest2")
                .project("TITAN - Demo")
                .milestone("7.6.1")
                .assignedToName("Rick Turmel")
                .type(TYPE.Automated)
                .category(CATEGORY.Performance)
                .suiteName("Performance")
                .sectionName("ENUM")
                .priority(PRIORITY.High)
                .focus("test focus")
                .steps("steps")
                .preconds("preconds")
                .expected("expected")
                .category(CATEGORY.Performance)
                .build();

        JsonObject jsonObject = testRailServices.addTestCase(netNumberTestRailTestCase, true);
        LOGGER.info("{}", GsonServices.build().prettyPrint(jsonObject));
    }
    
    @Test
    public void addTestCaseTest3(){
        LOGGER.info("{}", testRailServices.getProjects());
        
        NetNumberTestRailTestCase netNumberTestRailTestCase = NetNumberTestRailTestCase.builder("mikeTest2")
                .project("TITAN - Demo")
                .milestone("7.6.1")
                .type(TYPE.Automated)
                .category(CATEGORY.Performance)
                .suiteName("Performance")
                .sectionName("ENUM")
                .build();
        JsonObject jsonObject = testRailServices.addTestCase(netNumberTestRailTestCase, true);
        LOGGER.info("{}", GsonServices.build().prettyPrint(jsonObject));
    }
    
    @Test
    public void addTestCaseTest4(){
        
        NetNumberTestRailTestCase netNumberTestRailTestCase = NetNumberTestRailTestCase.builder("mikeTest3")
                .project("TITAN - Demo")
                .suiteName("Performance")
                .sectionName("ENUM")
                .type(TYPE.Automated)
                .category(CATEGORY.Performance)
                .build();
        JsonObject jsonObject = testRailServices.addTestCase(netNumberTestRailTestCase, true);
        LOGGER.info("{}", GsonServices.build().prettyPrint(jsonObject));
    }

    @Test
    public void deleteProjectTest3(){
        JsonObject jsonObject = testRailServices.deleteProject(77);
        Assert.assertTrue(jsonObject!=null);
        LOGGER.info("{}", GsonServices.build().prettyPrint(jsonObject));
    }

    @Test
    public void addSectionTest2(){
        LOGGER.info("{}", GsonServices.build().prettyPrint(testRailServices.addSection("TITAN - Demo", "Performance", "foo", "description", "ENUM", true)));
    }
    
    @Test
    public void deleteSectionTest2(){
        
        testRailServices.deleteSection("TITAN - Demo", "Performance", "foo", true);
    }

    
    
    @Test
    public void updateTestCaseTest(){
        
        NetNumberTestRailTestCase netNumberTestRailTestCase = NetNumberTestRailTestCase.builder("addTestCaseWithTestNGNameTest")
                .project("apiTestProj")
                .suiteName("suite1")
                .sectionName("section1")
                .type(TYPE.Automated)
                .category(CATEGORY.Compatibility)
                .assignedToName("Akriti")//broke
                .priority(PRIORITY.Medium)
                .milestone("milestone1")
                .focus("james")
                .estimate("mikeestimate")
                .expected("mikeExpected")
                .preconds("mikePreConds")
                .steps("mikesteps")//broke
                .build();
        //LOGGER.info("{}", GsonServices.build().prettyPrint(testRailServices.addTestCase(netNumberTestRailTestCase, false)));
        
        
        JsonObject jsonObject = testRailServices.updateTestCase(netNumberTestRailTestCase);
        LOGGER.info("{}", GsonServices.build().prettyPrint(jsonObject));
        
        
        {
              "created_by": 1,
              "milestone_id": null,
              "custom_expected": null,
              "custom_preconds": null,
              "estimate_forecast": null,
              "priority_id": 3,
              "created_on": 1441898372,
              "section_id": 7,
              "estimate": null,
              "suite_id": 3,
              "type_id": 3,
              "id": 789,
              "custom_steps": null,
              "title": "mikeTest1",
              "refs": null,
              "custom_exectype": 1,
              "updated_by": 1,
              "updated_on": 1441904399,
              "custom_focus": "test focusxxx"
            }*/
        
        /*testRailServices.getTestCaseIdByName("TITAN - Demo", "Performance", "ENUM", "mikeTest4", true);
        
        Assert.assertTrue(((Number)jsonObject.get("custom_exectype")).intValue()==CATEGORY.Regression.getValue());
        LOGGER.info("{}", jsonObject.get("custom_focus"));
        Assert.assertTrue(jsonObject.get("custom_focus").equals("test focusxxx"));
        
        testRailServices.deleteTestCase(netNumberTestRailTestCase, true);
    }
    
    @Test()
    public void deleteSuiteTest2(){
        testRailServices.deleteSuite("apiTestProj", "Performance");
    }
    
    @Test
    public void testCaseExistsTest(){
        LOGGER.info("{}", testRailServices.isTestCaseExists("TITAN - Demo", "Performance", "ENUM", "mikeTest2"));
    }
    
    @Test
    public void deleteTestCaseTest2(){
        LOGGER.info("{}", testRailServices.getProjects());
        testRailServices.deleteTestCase("TITAN - Demo", "Performance", "ENUM", "mikeTest1", true);
    }
    
    @Test
    public void getResultsTestTest(){
        LOGGER.info("{}", GsonServices.build().prettyPrint(testRailServices.getResults(73)));
    }
    
    @Test
    public void getRsultsTestTest(){
        
        LOGGER.info("{}", GsonServices.build().prettyPrint(
                testRailServices.getResults("TITAN - Demo", "7.6.1", "ENUM", "Performance", "Performance", "ENUM, 1 - normal form, IPV4, Solaris"))
        );
    }
    
    @Test
    public void getProjectTest2(){
        LOGGER.info("{}", GsonServices.build().prettyPrint(testRailServices.getProjects("TITAN - Demo")));
    }

    
    @Test
    public void getProjectIdByNameTest(){
        LOGGER.info("ProjectId: {}", testRailServices.getProjectIdByName("TITAN - Demoo", true));
    }
    
    @Test
    public void getMileStonesTest(){
        LOGGER.info("{}", GsonServices.build().prettyPrint(testRailServices.getMileStones("TITAN - Demo")));
    }
    
    @Test
    public void getMileStoneTest2(){
        
        LOGGER.info("{}", GsonServices.build().prettyPrint(testRailServices.getMileStone("TITAN - Demo", "7.6.1", true)));
    }
    
    @Test
    public void getSectionsTest2(){
        LOGGER.info("{}", GsonServices.build().prettyPrint(testRailServices.getSections("TITAN - Demo", "Performance")));
    }
    
    @Test
    public void getSuiteByNameTest2(){
        LOGGER.info("{}", GsonServices.build().prettyPrint(testRailServices.getSuiteByName("view-edge", "Legacy", true)));
    }
    
    @Test
    public void getTestCaseIdIdByNameTest(){
        LOGGER.info("{}", testRailServices.getTestCaseIdByName("View_Edge_DEMO", "Legacy", "primitive_negative_edge", "EntryInstPrimitiveNeg1-Clob_Range1", true));
    }
    
    @Test
    public void getRunsTest(){
        LOGGER.info("\nTest Runs:\n{}", GsonServices.build().prettyPrint(testRailServices.getRuns("TITAN - Demo")));
    }
    
    @Test
    public void getRunIdByNameTest(){
        LOGGER.info("RUNID: {}", testRailServices.getRunIdByName("TITAN - Demo", "7.6.1", "Performance", "Performance", true));
    }
    
    @Test
    public void getRunIdByNameTest2(){
        LOGGER.info("{}", testRailServices.getRunIdByName("TITAN - Demo", "7.6.1", "Performance", "CAP", true));
    }
    
    /*
     * (projectName = "security",                    // Project                   

         suiteName   = "suite_library_unit_test",       // Suite

         sectionName = "user_functionality", // Section

         mileStoneName = "ms_user_master",        // Milestone

         runName                    = "run_revision_7.6.2", // Run Name

         category            = "Regression",            // Category
     */
    /*
    @Test
    public void getRunIdByNameTest3(){
        LOGGER.info("{}", testRailServices.getRunIdByName("security", "ms_user_master", "run_revision_7.6.2", "suite_user_master", true));
    }
    
    */
    

}
