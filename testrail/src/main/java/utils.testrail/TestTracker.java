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


/**
 * Associate a test method with one or more TestRail test cases
 * <p>
 * Using the ignore field, automation can be told to completely ignore this testcase.
 * <p>
 * Using the isFullyAutomated field, automation can be told to update testrail milestones
 * but not update the Automated, Date Automated, and Last Automation Run fields.  Useful
 * for test which have automated actions, but require manual verification of results
 *
 * Additional testcases can be updated by settting the additionaTests list to values containing
 * "CASEID:SUITEID[:true|false]" where the third field overrides the fullyAutomated.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestTracker{
	int testCaseId() default 0;
	String testCaseName() default TestRailConstants.TESTCASE_NAME_DEFAULT;

	String comment() default "";
	String category() default TestRailConstants.CATEGORY_DEFAULT;
    
    String assignedToName() default TestRailConstants.ASSIGNED_TO_DEFAULT;
    int assignedToId() default 0;

    boolean publish() default true;

    String steps() default "";
    String preConditions() default "";
    
    String focus() default "";
    
    String elapsed() default "";
    String defects() default "";
    
    /** whether to ignore this Test.  Ignored tests are responsible for updating TestRail on their own */
    //boolean ignore() default false;
    /** whether automation should mark the testcase in testrail as automated.  Set to false for tests that require manual validation */
    boolean isFullyAutomated() default false;
    /** addition testcases specified as a list, with each value being "caseId:suiteId" */
    String[] additionalTests() default {};
}