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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.TimeZone;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/*name    string  The name of the milestone (required)
description string  The description of the milestone
due_on  timestamp   The due date of the milestone (as UNIX timestamp)
parent_id   int The ID of the parent milestone, if any (for sub-milestones) (available since TestRail 5.3)
start_on    timestamp   The scheduled start date of the milestone (as UNIX timestamp) (available since TestRail 5.3)*/
public class MileStone {
    private static final Logger LOGGER = LoggerFactory.getLogger(new Throwable().getStackTrace()[0].getClassName());
    private TestRailServices testRailServices = null;
    private String mileStoneName = null;
    private Integer mileStoneId = null;
    private String description = null;
    private String projectName = null;
    private Integer projectId = null;
    private String dueOn = null;
    private String parentId = null;
    private String startOn = null;
    private boolean update = false;
    
    
    public static Builder builder(TestRailServices testRailServices, String mileStoneName){
        return new Builder(testRailServices, mileStoneName);
    }
    
    
    
    public static class Builder {
        private TestRailServices testRailServices = null;
        private String mileStoneName;
        private Integer mileStoneId = null;
        private Integer projectId = null;
        private String projectName = null;
        private String description;
        private String dueOn = null;
        private String parentId = null;
        private String startOn = null;
        private boolean update = false;

        private Builder(TestRailServices testRailServices, String mileStoneName) {
            this.testRailServices = testRailServices;
            this.mileStoneName = mileStoneName;
        }
        
        public Builder mileStoneId(Integer mileStoneId) {
            this.mileStoneId = mileStoneId;
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

        public Builder dueOn(String dueOn) {
            this.dueOn = dueOn;
            return this;
        }

        public Builder parentId(String parentId) {
            this.parentId = parentId;
            return this;
        }

        public Builder startOn(String startOn) {
            this.startOn = startOn;
            return this;
        }

        public MileStone build(boolean update) throws TestRailConfigException, IOException {
        	this.update = update;
            return new MileStone(this);
        }
    }

    private MileStone(Builder builder) throws TestRailConfigException, IOException {
        this.testRailServices = builder.testRailServices;
        this.mileStoneName = builder.mileStoneName;
        this.mileStoneId = builder.mileStoneId;
        this.projectId = builder.projectId;
        this.projectName = builder.projectName;
        this.description = builder.description;
        this.dueOn = builder.dueOn;
        this.parentId = builder.parentId;
        this.startOn = builder.startOn;
        this.update = builder.update;
        if(update) {
        	update();
        }
        
    }
    
    private void update() throws TestRailConfigException, IOException {
    	if(projectId==null){
            projectId = testRailServices.getProjectIdByName(projectName, true);
        }
        //TODO do this everywhere, bFailOnNotFound = false beneighn
        if(mileStoneId==null){
            mileStoneId = testRailServices.getMileStoneIdByName(projectId, mileStoneName, false);
        }
   }
 
    public String getMileStoneName() {
        return mileStoneName;
    }
    
    public Integer getMileStoneId() {
        return mileStoneId;
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

    public String getDueOn() {
        return dueOn;
    }

    public String getParentId() {
        return parentId;
    }

    public String getStartOn() {
        return startOn;
    }

    public String toJson() throws IOException, ParseException {
        HashMap<String, Object> config = new HashMap<>();
        JsonBuilderFactory factory = Json.createBuilderFactory(config);
        
        JsonObjectBuilder jsonObjectBuilder = factory.createObjectBuilder()
            .add("name", mileStoneName);
            if(description!=null){
                jsonObjectBuilder.add("description", description);
            }
            if(parentId != null){
                jsonObjectBuilder.add("parent_id", parentId);
            }
            if(startOn != null){
                DateFormat dfm = new SimpleDateFormat("yyyyMMdd");
                dfm.setTimeZone(TimeZone.getTimeZone("EDT"));
                long a = dfm.parse(startOn).getTime();
                // to convert it to seconds
                jsonObjectBuilder.add("start_on", a/1000);
            }
            if(dueOn != null){
                DateFormat dfm = new SimpleDateFormat("yyyyMMdd");
                dfm.setTimeZone(TimeZone.getTimeZone("EDT"));
                long a = dfm.parse(dueOn).getTime();
                // to convert it to seconds
                jsonObjectBuilder.add("due_on", a/1000);
            }
        return jsonObjectBuilder.build().toString();
    }
    
    //TODO make sure all builders ONLY call services api's
    public JsonObject add() throws IOException, ParseException, TestRailConfigException {
        return testRailServices.addMileStone(projectId, toJson());
    }
    
    public JsonObject delete(boolean bFailOnNotFound) throws IOException, ParseException, TestRailConfigException {
        return testRailServices.deleteMileStone(mileStoneId, bFailOnNotFound);
    }
    
    public JsonObject getMileStone(boolean bFailOnNotFound) throws IOException, TestRailConfigException{
        return testRailServices.getMileStone(mileStoneId, bFailOnNotFound);
    }
    
    public JsonObject getMileStoneByName(String mileStoneName, boolean bFailOnNotFound) throws IOException, TestRailConfigException{
        return testRailServices.getMileStoneByName(projectId, mileStoneName, bFailOnNotFound);
    }
    
    public Integer getMileStoneIdByName(boolean bFailOnNotFound) throws TestRailConfigException, IOException{
        return testRailServices.getMileStoneIdByName(projectId, mileStoneName, bFailOnNotFound);
    }
    
    public Boolean isExists() throws IOException, TestRailConfigException {
        return mileStoneId!=null;
    }
    
    
}
