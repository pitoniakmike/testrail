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
import java.util.List;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/*
	case_ids    array   An array of case IDs for the custom case selection
	refs    string  A comma-separated list of references/requirements (Requires TestRail 6.1 or later)
*/
public class Run {
    private static final Logger LOGGER = LoggerFactory.getLogger(new Throwable().getStackTrace()[0].getClassName());
    private TestRailServices testRailServices = null;
    private String runName;
    private Integer runId = null;
    private String description;
    private Integer projectId = null;
    private String projectName = null;
    private Integer suiteId = null;
    private String suiteName = null;
    private Integer mileStoneId = null;
    private String mileStoneName = null;
    private Integer assignedTo = null;
    private Boolean includeAll = null;
    private List<Integer> caseIdList = null;
    private String refs = null;
    private boolean update = false;
    
    
    
    public static Builder builder(TestRailServices testRailServices, String projectName, String runName){
        return new Builder(testRailServices, projectName, runName);
    }
    
    
    
    public static class Builder {
        private TestRailServices testRailServices = null;
        private String runName;
        private Integer runId = null;
        private String description;
        private Integer projectId = null;
        private String projectName = null;
        private Integer suiteId = null;
        private String suiteName = null;
        private Integer mileStoneId = null;
        private String mileStoneName = null;
        private Integer assignedTo = null;
        private Boolean includeAll = null;
        private List<Integer> caseIdList = null;
        private String refs = null;
        private boolean update = false;
        
        
        private Builder(TestRailServices testRailServices, String projectName, String runName) {
            this.testRailServices = testRailServices;
            this.projectName = projectName;
            this.runName = runName;
        }
        
        public Builder runId(Integer runId) {
            this.runId = runId;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder projectId(Integer projectId) {
            this.projectId = projectId;
            return this;
        }
        
        public Builder suiteId(Integer suiteId) {
            this.suiteId = suiteId;
            return this;
        }
        
        public Builder suiteName(String suiteName) {
            this.suiteName = suiteName;
            return this;
        }
        
        public Builder mileStoneId(Integer mileStoneId) {
            this.mileStoneId = mileStoneId;
            return this;
        }
        
        public Builder mileStoneName(String mileStoneName) {
            this.mileStoneName = mileStoneName;
            return this;
        }
        
        public Builder assignedTo(Integer assignedTo) {
            this.assignedTo = assignedTo;
            return this;
        }
        
        public Builder includeAll(Boolean includeAll) {
            this.includeAll = includeAll;
            return this;
        }
        
        public Builder caseIdList(List<Integer> caseIdList) {
            this.caseIdList = caseIdList;
            return this;
        }
        
        public Builder refs(String refs) {
            this.refs = refs;
            return this;
        }
        
        public Run build(boolean update) throws TestRailConfigException, IOException {
        	this.update = update;
            return new Run(this);
        }

    }

    private Run(Builder builder) throws TestRailConfigException, IOException {
        this.testRailServices = builder.testRailServices;
        this.runName = builder.runName;
        this.runId = builder.runId;
        this.description = builder.description;
        this.projectId = builder.projectId;
        this.projectName = builder.projectName;
        this.suiteId = builder.suiteId;
        this.suiteName = builder.suiteName;
        this.mileStoneId = builder.mileStoneId;
        this.mileStoneName = builder.mileStoneName;
        this.assignedTo = builder.assignedTo;
        this.includeAll = builder.includeAll;
        this.caseIdList = builder.caseIdList;
        this.refs = builder.refs;
        this.update = builder.update;
        if(update) {
        	update();
        }
    }
    
    private void update() throws TestRailConfigException, IOException {
    	 if(projectId==null){
             projectId = testRailServices.getProjectIdByName(projectName, true);
         }

         if(suiteId==null){
             suiteId = testRailServices.getSuiteIdByName(projectId, suiteName, true);
         }
         
         if(runId==null){
             runId = testRailServices.getRunIdByName(projectId, runName, false);
         }
         
         if(mileStoneId==null){
             suiteId = testRailServices.getMileStoneIdByName(projectId, mileStoneName, true);
         }
    }
 
    public String getRunName() {
        return runName;
    }
    
    public Integer getRunId() {
        return runId;
    }

    public String getProjectName() {
        return projectName;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public String getDescription() {
        return description;
    }

    public Integer getSuiteId() {
        return suiteId;
    }

    public String getSuiteName() {
        return suiteName;
    }

    public Integer getMileStoneId() {
        return mileStoneId;
    }

    public String getMileStoneName() {
        return mileStoneName;
    }

    public Integer getAssignedTo() {
        return assignedTo;
    }

    public Boolean getIncludeAll() {
        return includeAll;
    }

    public List<Integer> getCaseIdList() {
        return caseIdList;
    }

    public String getRefs() {
        return refs;
    }

    public String toJson() throws IOException {
        HashMap<String, Object> config = new HashMap<>();
        JsonBuilderFactory factory = Json.createBuilderFactory(config);
        
        
        JsonObjectBuilder jsonObjectBuilder = factory.createObjectBuilder()
            .add("name", runName);
            if(description!=null){
                jsonObjectBuilder.add("description", description);
            }
            if(projectId!=null){
                jsonObjectBuilder.add("project_id", projectId);
            }
            if(suiteId!=null){
                jsonObjectBuilder.add("suite_id", suiteId);
            }
            if(mileStoneId!=null){
                jsonObjectBuilder.add("milestone_id", mileStoneId);
            }
            if(assignedTo!=null){
                jsonObjectBuilder.add("assignedto_id", assignedTo);
            }
            if(includeAll!=null){
                jsonObjectBuilder.add("include_all", includeAll);
            }
            if(caseIdList!=null){
                //TODO fix
                //jsonObjectBuilder.add("case_ids", caseIdList);
            }
            if(refs!=null){
                jsonObjectBuilder.add("refs", refs);
            }
            
        return jsonObjectBuilder.build().toString();
    }
    

    public Integer getRunIdByName( boolean bFailOnNotFound) throws TestRailConfigException, IOException{
    	return testRailServices.getRunIdByName(projectId, runName, bFailOnNotFound);
    }
    
    public JsonObject getRunByName(boolean bFailOnNotFound) throws IOException, TestRailConfigException{
        return testRailServices.getRunByName(projectId, runName, bFailOnNotFound);
    }

    public JsonObject add(boolean bFailIfExists) throws IOException, ParseException, TestRailConfigException{
        return testRailServices.addRun(projectId, toJson());
    }
    
    //TODO bug on all  if no update
    public Boolean isExists() throws IOException, TestRailConfigException {
        return runId!=null;
    }
    
}
