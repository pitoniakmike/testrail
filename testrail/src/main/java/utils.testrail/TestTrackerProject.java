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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestTrackerProject {
	int projectId() default 0;
	String projectName() default TestRailConstants.PROJECT_NAME_DEFAULT;
	
	int runId() default 0;
	String runName() default TestRailConstants.RUN_NAME_DEFAULT;
	
	String version() default TestRailConstants.VERSION;
	
	int suiteId() default 0;
	String suiteName() default TestRailConstants.SUITE_NAME_DEFAULT;
	
	int sectionId() default 0;
	String sectionName() default TestRailConstants.SECTION_NAME_DEFAULT;
	
	int mileStoneId() default 0;
	String mileStoneName() default TestRailConstants.MILESTONE_NAME_DEFAULT;
	
	String category() default TestRailConstants.CATEGORY_DEFAULT;

    int assignedToId() default 0;
    String assignedToName() default TestRailConstants.ASSIGNED_TO_DEFAULT;

    boolean enabled() default true;
    
    boolean publish() default false;
    
    boolean createProject() default false;
    
    boolean createMileStone() default false;
    
    boolean createSuite() default false;
    
    boolean createRun() default false;

    boolean createSection() default false;
    
    boolean createTestCase() default false;
    
    /** whether to ignore this Test.  Ignored tests are responsible for updating TestRail on their own */
    //boolean ignore() default false;
    /** whether automation should mark the testcase in testrail as automated.  Set to false for tests that require manual validation */
    boolean isFullyAutomated() default false;


}
