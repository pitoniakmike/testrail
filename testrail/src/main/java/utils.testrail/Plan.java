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

/*name  string  The name of the test plan (required)
description string  The description of the test plan
milestone_id    int The ID of the milestone to link to the test plan
entries array   An array of objects describing the test runs of the plan, see the example below and add_plan_entry*/
public class Plan {
    private static final Logger LOGGER = LoggerFactory.getLogger(new Throwable().getStackTrace()[0].getClassName());
    private TestRailServices testRailServices = null;
    private String planName = null;
    private Integer planId = null;
    private String projectName = null;
    private Integer projectId = null;
    private String description = null;
    private boolean update = false;

    
    public static Builder builder(TestRailServices testRailServices, String planName){
        return new Builder(testRailServices, planName);
    }
    
    
    
    public static class Builder {
        private TestRailServices testRailServices = null;
        private String planName;
        private Integer planId = null;
        private Integer projectId = null;
        private String projectName = null;
        private String description;
        private boolean update = false;
        

        private Builder(TestRailServices testRailServices, String planName) {
            this.testRailServices = testRailServices;
            this.planName = planName;
        }
        
        public Builder planId(Integer planId) {
            this.planId = planId;
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
        
        public Plan build(boolean update) throws TestRailConfigException, IOException {
        	this.update = update;
            return new Plan(this);
        }

    }

    private Plan(Builder builder) throws TestRailConfigException, IOException {
        this.testRailServices = builder.testRailServices;
        this.planName = builder.planName;
        this.planId = builder.planId;
        this.projectId = builder.projectId;
        this.projectName = builder.projectName;
        this.description = builder.description;
        
        if(update) {
        	update();
        }
    }
    
    private void update() throws TestRailConfigException, IOException {
    	if(projectId==null){
            projectId = testRailServices.getProjectIdByName(projectName, true);
        }
        if(planId==null){
            projectId = testRailServices.getPlanIdByName(projectId, planName, false);
        }
    }
 
    public String getPlanName() {
        return planName;
    }
    
    public Integer getPlanId() {
        return planId;
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
            .add("name", planName);
            if(description!=null){
                jsonObjectBuilder.add("description", description);
            }
        return jsonObjectBuilder.build().toString();
    }
    
    public JsonObject getPlan(boolean bFailIfExists) throws IOException, ParseException, TestRailConfigException{
        return testRailServices.getPlan(planId, bFailIfExists);
    }
    
    public JsonObject add(boolean bFailIfExists) throws IOException, ParseException, TestRailConfigException{
        return testRailServices.addPlan(projectId, toJson());
    }

    public Integer getSuiteIdByName(String suiteName, boolean bFailOnNotFound) throws TestRailConfigException, IOException{
    	return testRailServices.getSuiteIdByName(projectId, suiteName, bFailOnNotFound);
       
    }
    
    public Boolean isExists() throws IOException, TestRailConfigException {
        return planId!=null;
    }
    
}
