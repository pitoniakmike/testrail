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


/*	description   string  The description of the section (added with TestRail 4.0)
	suite_id    int The ID of the test suite (ignored if the project is operating in single suite mode, required otherwise)
	parent_id   int The ID of the parent section (to build section hierarchies)
	name    string  The name of the section (required)
*/
public class Section {
    private static final Logger LOGGER = LoggerFactory.getLogger(new Throwable().getStackTrace()[0].getClassName());
    private TestRailServices testRailServices = null;
    private String sectionName;
    private Integer sectionId = null;
    private String description;
    private Integer projectId = null;
    private String projectName = null;
    private Integer suiteId = null;
    private String suiteName = null;
    private Integer parentId = null;
    private String parentName = null;
    private boolean update = false;

    
    public static Builder builder(TestRailServices testRailServices, String sectionName){
        return new Builder(testRailServices, sectionName);
    }

    
    public static class Builder {
        private TestRailServices testRailServices = null;
        private String sectionName;
        private Integer sectionId = null;
        private String description;
        private Integer projectId = null;
        private String projectName = null;
        private Integer suiteId = null;
        private String suiteName = null;
        private Integer parentId = null;
        private String parentName = null;
        private boolean update = false;

        
        private Builder(TestRailServices testRailServices, String sectionName) {
            this.testRailServices = testRailServices;
            this.sectionName = sectionName;
        }
        
        public Builder sectionId(Integer sectionId) {
            this.sectionId = sectionId;
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
        
        public Builder suiteId(Integer suiteId) {
            this.suiteId = suiteId;
            return this;
        }
        
        public Builder suiteName(String suiteName) {
            this.suiteName = suiteName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Section build(boolean update) throws TestRailConfigException, IOException {
        	this.update = update;
            return new Section(this);
        }

    }

    private Section(Builder builder) throws TestRailConfigException, IOException {
        this.testRailServices = builder.testRailServices;
        this.sectionName = builder.sectionName;
        this.sectionId = builder.sectionId;
        this.description = builder.description;
        this.projectId = builder.projectId;
        this.projectName = builder.projectName;
        this.suiteId = builder.suiteId;
        this.suiteName = builder.suiteName;
        this.parentId = builder.parentId;
        this.parentName = builder.parentName;
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
            suiteId = testRailServices.getSuiteIdByName(projectId, sectionName, true);
        }
        
        if(sectionId==null){
        	sectionId = testRailServices.getSectionIdByName(projectId, suiteId, sectionName, false);
        }
        
        //TODO fix
        if(parentId==null){
            //suiteId = testRailServices.getParentIdByName(projectId, name, true);
        }
   }
 
    public String getSectionName() {
        return sectionName;
    }
    
    public Integer getSectionId() {
        return sectionId;
    }
    
    public String getDescription() {
        return description;
    }

    public String getProjectName() {
        return projectName;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public Integer getSuiteId() {
        return suiteId;
    }

    public String getSuiteName() {
        return suiteName;
    }

    public Integer getParentId() {
        return parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public String toJson() throws IOException {
        HashMap<String, Object> config = new HashMap<>();
        JsonBuilderFactory factory = Json.createBuilderFactory(config);
        
        
        JsonObjectBuilder jsonObjectBuilder = factory.createObjectBuilder()
            .add("name", sectionName);
            if(description!=null){
                jsonObjectBuilder.add("description", description);
            }
            if(projectId!=null){
                jsonObjectBuilder.add("projacet_id", projectId);
            }
            if(description!=null){
                jsonObjectBuilder.add("projectName", projectName);
            }
            if(parentId!=null){
                jsonObjectBuilder.add("parent_id", parentId);
            }
        return jsonObjectBuilder.build().toString();
    }
    
    public JsonArray getSections(Integer projectId, Integer suiteId) throws IOException{
        return testRailServices.getSections(projectId, suiteId);
    }
    
    public Integer getSectionIdByName( boolean bFailOnNotFound) throws TestRailConfigException, IOException{
        return testRailServices.getSectionIdByName(projectId, suiteId, sectionName, bFailOnNotFound);
    }

    public JsonObject add(boolean bFailIfExists) throws IOException, ParseException, TestRailConfigException{
        return testRailServices.addSection(projectId, toJson());
    }
    
    public JsonObject getSectionByName(Integer projectId, Integer suiteId, String sectionName, boolean bFailOnNotFound) throws IOException, TestRailConfigException {
    	return testRailServices.getSectionByName(projectId, suiteId, sectionName, bFailOnNotFound);
    }
    
    public Boolean isExists() throws IOException, TestRailConfigException {
        return sectionId!=null;
    }
    
    
    
}
