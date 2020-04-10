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
import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;


/*
    
    get cases params
    :project_id	The ID of the project
	:suite_id	The ID of the test suite (optional if the project is operating in single suite mode)
	:section_id	The ID of the section (optional)
	:limit	The number of test cases the response should return
	:offset	Where to start counting the tests cases from (the offset)
	:filter	Only return cases with matching filter string in the case title

    add args:
    title	string	The title of the test case (required)
	template_id	int	The ID of the template (field layout) (requires TestRail 5.2 or later)
	type_id	int	The ID of the case type
	priority_id	int	The ID of the case priority
	estimate	timespan	The estimate, e.g. "30s" or "1m 45s"
	milestone_id	int	The ID of the milestone to link to the test case
	refs	string	A comma-separated list of references/requirements
 */
public class TestCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(new Throwable().getStackTrace()[0].getClassName());
    private TestRailServices testRailServices = null;
    private String testCaseName = null;
    private Integer testCaseId = null;
    private String projectName = null;
    private Integer projectId = null;
    private String suiteName = null;
    private Integer suiteId = null;
    private String mileStoneName = null;
    private Integer mileStoneId = null;
    private String sectionName = null;
    private Integer sectionId = null;
    private Integer templateId = null;
    private Integer typeId = null;
    private Integer priorityId = null;
    private String estimate = null;
    private String refs = null;
    private boolean update = false;

    
    public static Builder builder(TestRailServices testRailServices, String testCaseName){
        return new Builder(testRailServices, testCaseName);
    }
    
    public static class Builder {
    	private TestRailServices testRailServices = null;
        private String testCaseName = null;
        private Integer testCaseId = null;
        private String projectName = null;
        private Integer projectId = null;
        private String suiteName = null;
        private Integer suiteId = null;
        private String mileStoneName = null;
        private Integer mileStoneId = null;
        private String sectionName = null;
        private Integer sectionId = null;
        private Integer templateId = null;
        private Integer typeId = null;
        private Integer priorityId = null;
        private String estimate = null;
        private String refs = null;
        private boolean update = false;
        
        private Builder(TestRailServices testRailServices, String testCaseName) {
            this.testRailServices = testRailServices;
            this.testCaseName = testCaseName;
        }

        public Builder testCaseId(Integer testCaseId) {
            this.testCaseId = testCaseId;
            return this;
        }


        public Builder projectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        public Builder projectId(Integer projectId) {
            this.projectId = projectId;
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

        public Builder mileStoneName(String mileStoneName) {
            this.mileStoneName = mileStoneName;
            return this;
        }

        public Builder mileStoneId(Integer mileStoneId) {
            this.mileStoneId = mileStoneId;
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
        
        public Builder templateId(Integer templateId) {
            this.templateId = templateId;
            return this;
        }
        
        public Builder typeId(Integer typeId) {
            this.typeId = typeId;
            return this;
        }
        
        public Builder priorityId(Integer priorityId) {
            this.priorityId = priorityId;
            return this;
        }
        
        public Builder estimate(String estimate) {
            this.estimate = estimate;
            return this;
        }
        
        public Builder refs(String refs) {
            this.refs = refs;
            return this;
        }

      
        public TestCase build(boolean update) throws TestRailConfigException, IOException {
        	this.update = update;
            return new TestCase(this);
        }
    }

    private TestCase(Builder builder) throws TestRailConfigException, IOException {
        this.testRailServices = builder.testRailServices;
        this.testCaseName = builder.testCaseName;
        this.testCaseId = builder.testCaseId;
        this.projectName = builder.projectName;
        this.projectId = builder.projectId;
        this.suiteName = builder.suiteName;
        this.suiteId = builder.suiteId;
        this.mileStoneName = builder.mileStoneName;
        this.mileStoneId = builder.mileStoneId;
        this.sectionName = builder.sectionName;
        this.sectionId = builder.sectionId;
        this.templateId = builder.templateId;
        this.typeId = builder.typeId;
        this.priorityId = builder.priorityId;
        this.estimate = builder.estimate;
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
             suiteId = testRailServices.getSuiteIdByName(projectId, testCaseName, true);
         }
         
         if(sectionId==null){
             sectionId = testRailServices.getSectionIdByName(projectId, suiteId, testCaseName, true);
         }
         
         if(testCaseId==null){
         	testCaseId = testRailServices.getTestCaseIdByName(projectId, suiteId, sectionId, testCaseName, false);
         }
   }
    
    /*	title string  The title of the test case (required)
		template_id int The ID of the template (field layout) (requires TestRail 5.2 or later)
		type_id int The ID of the case type
		priority_id int The ID of the case priority
		estimate    timespan    The estimate, e.g. "30s" or "1m 45s"
		milestone_id    int The ID of the milestone to link to the test case
		refs    string  A comma-separated list of references/requirements*/
    public String toJson() {
        HashMap<String, Object> config = new HashMap<>();
        JsonBuilderFactory factory = Json.createBuilderFactory(config);

        JsonObjectBuilder jsonObjectBuilder = factory.createObjectBuilder()
        	.add("title", testCaseName);
        
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
        	jsonObjectBuilder.add("refs",  refs);
		}
  
        return jsonObjectBuilder.build().toString();
	}
    
    
    

    /*/*
	 * 
	 * 	If TestCase Exists JSON of TestCase is returned
	 * 
	 * 
	 */
	/*private JSONObject addTestCase(Integer mileStoneId, Integer type, CATEGORY category, Integer sectionId, String testCaseName, PRIORITY priority, String focus, String estimate, String preconds, String steps, String expected, String assignedToName) throws IOException, APIException, TestRailConfigException{
		Map<String, Object> data = new HashMap<String, Object>();
		
		data.put("title", testCaseName);
		
		if(category!=null){
			data.put("custom_exectype", category.getValue());
		}

		data.put("custom_focus", (focus==null || focus.equals("")) ? "N/A" : focus);

		if(priority!=null){
			data.put("priority_id", priority.priority());
		}
		if(preconds!=null){
			data.put("custom_preconds", preconds);
		}
		if(steps!=null){
			data.put("custom_steps", steps);
		}
		if(expected!=null){
			data.put("customs_expected", expected);
		}
		if(type!=null){
			data.put("type_id", type);
		}
		if(mileStoneId!=null){
			data.put("milestone_id",  mileStoneId);
		}
		if(estimate!=null){
			data.put("estimate", estimate);
		}
		if(!assignedToName.equals("")){
			data.put("assignedto_id", getUserIdByName(assignedToName, true));
		}
		
		return addTestCase(sectionId, data);
	}*/
    
    public TestRailServices getTestRailServices() {
		return testRailServices;
	}

	public String getTestCaseName() {
		return testCaseName;
	}

	public Integer getTestCaseId() {
		return testCaseId;
	}

	public String getProjectName() {
		return projectName;
	}

	public Integer getProjectId() {
		return projectId;
	}

	public String getSuiteName() {
		return suiteName;
	}

	public Integer getSuiteId() {
		return suiteId;
	}

	public String getMileStoneName() {
		return mileStoneName;
	}

	public Integer getMileStoneId() {
		return mileStoneId;
	}

	public String getSectionName() {
		return sectionName;
	}

	public Integer getSectionId() {
		return sectionId;
	}

	public Integer getTemplateId() {
		return templateId;
	}

	public Integer getTypeId() {
		return typeId;
	}

	public Integer getPriorityId() {
		return priorityId;
	}

	public String getEstimate() {
		return estimate;
	}

	public String getRefs() {
		return refs;
	}

	public Boolean isExists() throws IOException, TestRailConfigException {
        return testCaseId!=null;
    }

    public JsonObject addTestCase(Integer sectionId, String json) throws IOException{
    	return testRailServices.addTestCase(sectionId, toJson());
    }

    
}
