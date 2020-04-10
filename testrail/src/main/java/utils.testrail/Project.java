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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


/*	name  string  The name of the project (required)
	announcement    string  The description of the project
	show_announcement   bool    True if the announcement should be displayed on the project's overview page and false otherwise
	suite_mode  integer The suite mode of the project (1 for single suite mode, 2 for single suite + baselines, 3 for multiple suites) (added with TestRail 4.0)
*/
public class Project {
    private static final Logger LOGGER = LoggerFactory.getLogger(new Throwable().getStackTrace()[0].getClassName());
    private TestRailServices testRailServices = null;
    private String projectName = null;
    private Integer projectId = null;
    private String announcement = null;
    private Boolean showAnnouncement = null;
    private SUITE_MODE suiteMode = null;
    private boolean update = false;

    
    public static Builder builder(TestRailServices testRailServices, String projectName){
        return new Builder(testRailServices, projectName);
    }
    
    
    public static class Builder {
        private TestRailServices testRailServices = null;
        private String projectName = null;
        private String announcement = null;
        private Boolean showAnnouncement = null;
        private SUITE_MODE suiteMode = null;
        private boolean update = false;
        

        private Builder(TestRailServices testRailServices, String projectName) {
            this.testRailServices = testRailServices;
            this.projectName = projectName;
        }
        
        public Builder announcement(String announcement) {
            this.announcement = announcement;
            return this;
        }
        
        public Builder showAnnouncement(Boolean showAnnouncement) {
            this.showAnnouncement = showAnnouncement;
            return this;
        }

        public Builder suiteMode(SUITE_MODE suiteMode) {
            this.suiteMode = suiteMode;
            return this;
        }
        
        public Project build(boolean update) throws TestRailConfigException, IOException {
        	this.update = update;
            return new Project(this);
        }

    }

    private Project(Builder builder) throws TestRailConfigException, IOException {
        this.testRailServices = builder.testRailServices;
        this.projectName = builder.projectName;
        this.announcement = builder.announcement;
        this.showAnnouncement = builder.showAnnouncement;
        this.suiteMode = builder.suiteMode;
        this.update = builder.update;
        
        if(update) {
        	update();
        }
    }
    
    private void update() throws TestRailConfigException, IOException {
    	if(projectId==null) {
            projectId = testRailServices.getProjectIdByName(projectName, false);
        }
    }
   
    public String getProjectName() {
        return projectName;
    }
    
    public Integer getProjectId() {
        return projectId;
    }

    public String getAnnouncement() {
        return announcement;
    }

    public Boolean getShowAnnouncement() {
        return showAnnouncement;
    }

    public SUITE_MODE getSuiteMode() {
        return suiteMode;
    }

    public String toJson() throws IOException {
        HashMap<String, Object> config = new HashMap<>();
        JsonBuilderFactory factory = Json.createBuilderFactory(config);

        JsonObjectBuilder jsonObjectBuilder = factory.createObjectBuilder()
            .add("name", projectName);
            if(announcement!=null){
                jsonObjectBuilder.add("announcement", announcement);
            }
            if(showAnnouncement!=null){
                jsonObjectBuilder.add("show_announcement", showAnnouncement);
            }
            if(suiteMode!=null){
                jsonObjectBuilder.add("suite_mode", suiteMode.getValue());
            }
            return jsonObjectBuilder.build().toString();
    }
    
    public JsonArray getProjects() throws IOException{
        return testRailServices.getProjects();
    }
    
    public JsonObject getProject(boolean bFailOnNotFound) throws IOException{
        return testRailServices.getProject(projectId, bFailOnNotFound);
    }
    
    public JsonObject add(boolean bFailIfExists) throws IOException, ParseException, TestRailConfigException{
        return testRailServices.addProject(toJson());
    }
    
    public Boolean isExists() throws IOException, TestRailConfigException {
        return projectId!=null;
    }

    public JsonObject delete(boolean bFailOnNotFound) throws IOException, TestRailConfigException {
        return testRailServices.deleteProject(projectId, bFailOnNotFound);
    }
    
 
    
}
