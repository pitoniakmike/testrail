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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/*
     status_id  int The ID of the test status. The built-in system statuses have the following IDs:
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
    assignedto_id   int The ID of a user the test should be assigned to
 */
public class TestRailResults {
	private String project;
	private String version;
	private String mileStone;
	private String run;
	private String suiteName;
	private String sectionName;
	private String testCaseTitle;
	private String focus;
	private String assignedTo;
	private String status;
	private String comments;
	
	
	public String getProject() {
		return project;
	}
	
	public void setProject(String project) {
		this.project = project;
	}
	
	public String getversion() {
		return version;
	}
	
	public void setversion(String version) {
		this.version = version;
	}
	
	public String getMileStone() {
		return mileStone;
	}
	
	public void setMileStone(String mileStone) {
		this.mileStone = mileStone;
	}
	
	public String getRun() {
		return run;
	}
	
	public void setRun(String run) {
		this.run = run;
	}
	public String getSuiteName() {
		return suiteName;
	}
	
	public void setSuiteName(String suiteName) {
		this.suiteName = suiteName;
	}
	
	public String getSectionName() {
		return sectionName;
	}
	
	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}
	
	public String getTestCaseTitle() {
		return testCaseTitle;
	}
	
	public void setTestCaseTitle(String testCaseTitle) {
		this.testCaseTitle = testCaseTitle;
	}
	
	public String getFocus() {
		return focus;
	}
	
	public void setFocus(String focus) {
		this.focus = focus;
	}
	
	public String getAssignedTo() {
		return assignedTo;
	}
	
	public void setAssignedTo(String assignedTo) {
		this.assignedTo = assignedTo;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getComments() {
		return comments;
	}
	
	public void setComments(String comments) {
		this.comments = comments;
	}
	
	
	public String toString() {
	    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	
}
