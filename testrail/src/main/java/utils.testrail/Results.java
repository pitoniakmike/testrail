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
import java.text.ParseException;
import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;


/*	status_id	int	The ID of the test status. The built-in system statuses have the following IDs:
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
	assignedto_id	int	The ID of a user the test should be assigned to
*/
public class Results {
    private static final Logger LOGGER = LoggerFactory.getLogger(new Throwable().getStackTrace()[0].getClassName());
    private TestRailServices testRailServices = null;
    private String projectName = null;
    private Integer projectId = null;
    private Integer mileStoneId = null;
    private String mileStoneName = null;
    private String runName = null;
    private Integer runId = null;
    private String suiteName = null;
    private Integer suiteId = null;
    private String sectionName = null;
    private Integer sectionId = null;
    private String testCaseName = null;
    private Integer testCaseId = null;
    private String assignedToName = null;
    private Integer assignedToId = null;
    private String comment = null;
    private String version = null;
    private String elapsed = null;
    private String defects = null;
    private TEST_STATUS status = null;
    private Boolean failOnError = null;
    private boolean update = false;
    
    
    public static Builder builder(TestRailServices testRailServices, String projectName){
        return new Builder(testRailServices, projectName);
    }

    
    public static class Builder {
        private TestRailServices testRailServices = null;
        private Integer projectId = null;
        private String projectName = null;
        private Integer mileStoneId = null;
        private String mileStoneName = null;
        private String runName = null;
        private Integer runId = null;
        private String suiteName = null;
        private Integer suiteId = null;
        private String sectionName = null;
        private Integer sectionId = null;
        private String testCaseName = null;
        private Integer testCaseId = null;
        private String assignedToName = null;
        private Integer assignedToId = null;
        private String comment = null;
        private String version = null;
        private String elapsed = null;
        private String defects = null;
        private TEST_STATUS status = null;
        private Boolean failOnError = null;
        private boolean update = false;
        
        
        private Builder(TestRailServices testRailServices, String projectName) {
            this.testRailServices = testRailServices;
            this.projectName = projectName;
        }
        
        public Builder projectId(Integer projectId) {
            this.projectId = projectId;
            return this;
        }
        
        public Builder mileStoneName(String mileStoneName) {
            this.mileStoneName = mileStoneName;
            return this;
        }

        public Builder mileStoneId(Integer mileStoneId) {
            this.mileStoneId = mileStoneId;
            return this;
        }
        
        public Builder runName(String runName) {
            this.runName = runName;
            return this;
        }

        public Builder runId(Integer runId) {
            this.runId = runId;
            return this;
        }
        
        public Builder suiteName(String suiteName) {
            this.suiteName = suiteName;
            return this;
        }

        public Builder suiteId(Integer suiteId) {
            this.suiteId = suiteId;
            return this;
        }
        
        public Builder sectionName(String sectionName) {
            this.sectionName = sectionName;
            return this;
        }

        public Builder sectionId(Integer sectionId) {
            this.sectionId = sectionId;
            return this;
        }
        
        public Builder testCaseName(String testCaseName) {
            this.testCaseName = testCaseName;
            return this;
        }

        public Builder testCaseId(Integer testCaseId) {
            this.testCaseId = testCaseId;
            return this;
        }
       
        public Builder assignedToName(String assignedToName) {
            this.assignedToName = assignedToName;
            return this;
        }
        
        public Builder assignedToId(Integer assignedToId) {
            this.assignedToId = assignedToId;
            return this;
        }
        
        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }
        
        public Builder version(String version) {
            this.version = version;
            return this;
        }
        
        public Builder elapsed(String elapsed) {
            this.elapsed = elapsed;
            return this;
        }
        
        public Builder defects(String defects) {
            this.defects = defects;
            return this;
        }
        
        public Builder testStatus(TEST_STATUS status) {
            this.status = status;
            return this;
        }
        
        public Builder failOnError(Boolean failOnError) {
            this.failOnError = failOnError;
            return this;
        }

        public Results build(boolean update) throws TestRailConfigException, IOException {
        	this.update = update;
            return new Results(this);
        }

    }

    private Results(Builder builder) throws TestRailConfigException, IOException {
        this.testRailServices = builder.testRailServices;
        this.projectName = builder.projectName;
        this.projectId = builder.projectId;
        this.mileStoneName = builder.mileStoneName;
        this.mileStoneId = builder.mileStoneId;
        this.runName = builder.runName;
        this.runId = builder.runId;
        this.suiteName = builder.suiteName;
        this.suiteId = builder.suiteId;
        this.sectionName = builder.sectionName;
        this.sectionId = builder.sectionId;
        this.testCaseName = builder.testCaseName;
        this.testCaseId = builder.testCaseId;
        this.assignedToName = builder.assignedToName;
        this.assignedToId = builder.assignedToId;
        this.comment = builder.comment;
        this.version = builder.version;
        this.elapsed = builder.elapsed;
        this.defects = builder.defects;
        this.status = builder.status;
        this.failOnError = builder.failOnError;
        this.update = builder.update;
        if(update) {
        	update();
        }
    }
    
    public void update() throws TestRailConfigException, IOException {
    	if(projectId==null){
            projectId = testRailServices.getProjectIdByName(projectName, true);
        }
        if(runId==null){
            runId = testRailServices.getRunIdByName(projectName, runName,  true);
        }
        
        if(suiteId==null){
            suiteId = testRailServices.getSuiteIdByName(projectId, suiteName, true);
        }
        if(sectionId==null){
            sectionId = testRailServices.getSectionIdByName(projectId, suiteId, sectionName, true);
        }
        if(testCaseId==null){
        	testCaseId = testRailServices.getTestCaseIdByName(projectId, suiteId, sectionId, testCaseName, true);
        }
        if(assignedToId==null){
            assignedToId = testRailServices.getUserIdByName(assignedToName, true);
        }
   }

    public String getProjectName() {
        return projectName;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public String getMileStoneName() {
        return mileStoneName;
    }

    public Integer getMileStoneId() {
        return mileStoneId;
    }

    public String getRunName() {
        return runName;
    }

    public Integer getRunId() {
        return runId;
    }

    public String getSuiteName() {
        return suiteName;
    }

    public Integer getSuiteId() {
        return suiteId;
    }

    public String getSectionName() {
        return sectionName;
    }

    public Integer getSectionId() {
        return sectionId;
    }

    public String getTestCaseName() {
        return testCaseName;
    }

    public Integer getTestCaseId() {
        return testCaseId;
    }

    public String getAssignedToName() {
        return assignedToName;
    }

    public Integer getAssignedToId() {
        return assignedToId;
    }

    public String getComment() {
        return comment;
    }

    public String getVersion() {
        return version;
    }

    public String getElapsed() {
        return elapsed;
    }

    public String getDefects() {
        return defects;
    }

    public TEST_STATUS getStatus() {
        return status;
    }

    public Boolean getFailOnError() {
        return failOnError;
    }


    /*status_id int The ID of the test status. The built-in system statuses have the following IDs:
    1   Passed
    2   Blocked
    3   Untested (not allowed when adding a result)
    4   Retest
    5   Failed
    You can get a full list of system and custom statuses via get_statuses.
    comment string  The comment / description for the test result
    version string  The version or build you tested against
    elapsed timespan    The time it took to execute the test, e.g. "30s" or "1m 45s"
    defects string  A comma-separated list of defects to link to the test result
    assignedto_id   int The ID of a user the test should be assigned to*/
    public String toJson() throws  IOException {
        HashMap<String, Object> config = new HashMap<>();
        JsonBuilderFactory factory = Json.createBuilderFactory(config);

        JsonObjectBuilder jsonObjectBuilder = factory.createObjectBuilder()
            .add("status_id", status.getValue());
            if(comment!=null){
                jsonObjectBuilder.add("comment", comment);
            }
            if(version!=null){
                jsonObjectBuilder.add("version", version);
            }
            if(elapsed!=null){
                jsonObjectBuilder.add("elapsed", elapsed);
            }
            if(defects!=null){
                jsonObjectBuilder.add("defects", defects);
            }
            if(assignedToId!=null){
                jsonObjectBuilder.add("assignedto_id", assignedToId);
            }
            return jsonObjectBuilder.build().toString();
    }
    
    public JsonObject addResultForCase() throws IOException, ParseException, TestRailConfigException{
        return  testRailServices.addResultForCase(runId, testCaseId, toJson());
    }
    
}
