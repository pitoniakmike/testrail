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


/*	name    string  The name of the milestone (required)
	description string  The description of the milestone
	due_on  timestamp   The due date of the milestone (as UNIX timestamp)
	parent_id   int The ID of the parent milestone, if any (for sub-milestones) (available since TestRail 5.3)
	start_on    timestamp   The scheduled start date of the milestone (as UNIX timestamp) (available since TestRail 5.3)
*/
public class Suite {
    private static final Logger LOGGER = LoggerFactory.getLogger(new Throwable().getStackTrace()[0].getClassName());
    private TestRailServices testRailServices = null;
    private String suiteName = null;
    private Integer suiteId = null;
    private String projectName = null;
    private Integer projectId = null;
    private String description = null;
    private boolean update = false;
    
    
    public static Builder builder(TestRailServices testRailServices, String suiteName){
        return new Builder(testRailServices, suiteName);
    }
    
    
    
    public static class Builder {
        private TestRailServices testRailServices = null;
        private String suiteName;
        private Integer suiteId = null;
        private Integer projectId = null;
        private String projectName = null;
        private String description;
        private boolean update = false;

        private Builder(TestRailServices testRailServices, String suiteName) {
            this.testRailServices = testRailServices;
            this.suiteName = suiteName;
        }
        
        public Builder suiteId(Integer suiteId) {
            this.suiteId = suiteId;
            return this;
        }
        
        public Builder projectId(Integer projectId) {
            this.projectId = projectId;
            return this;
        }
        
        public Builder projectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Suite build(boolean update) throws TestRailConfigException, IOException {
        	this.update = update;
            return new Suite(this);
        }

    }

    private Suite(Builder builder) throws TestRailConfigException, IOException {
        this.testRailServices = builder.testRailServices;
        this.suiteName = builder.suiteName;
        this.suiteId = builder.suiteId;
        this.projectId = builder.projectId;
        this.projectName = builder.projectName;
        this.description = builder.description;
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
            suiteId = testRailServices.getSuiteIdByName(projectId, suiteName, false);
        }
   }
 
    public String getSuiteName() {
        return suiteName;
    }
    
    public Integer getSuiteId() {
        return suiteId;
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

    public String toJson() throws IOException {
        HashMap<String, Object> config = new HashMap<>();
        JsonBuilderFactory factory = Json.createBuilderFactory(config);

        JsonObjectBuilder jsonObjectBuilder = factory.createObjectBuilder()
            .add("name", suiteName);
            if(description!=null){
                jsonObjectBuilder.add("description", description);
            }
        return jsonObjectBuilder.build().toString();
    }
    
    public JsonObject add(boolean bFailIfExists) throws IOException, ParseException, TestRailConfigException{
        return testRailServices.addSuite(projectId, toJson());
    }
    
    public JsonObject getSuite(boolean bFailOnNotFound) throws IOException{
    	return testRailServices.getSuite(suiteId, bFailOnNotFound);
    }

    public Integer getSuiteIdByName(String suiteName, boolean bFailOnNotFound) throws TestRailConfigException, IOException{
    	return testRailServices.getSuiteIdByName(projectId, suiteName, bFailOnNotFound);
    }
    
    public Boolean isExists() throws IOException, TestRailConfigException {
        return suiteId!=null;
    }
    
}
