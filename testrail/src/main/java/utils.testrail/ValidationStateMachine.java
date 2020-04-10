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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ValidationStateMachine {
	private static final Logger LOGGER = LoggerFactory.getLogger(new Throwable().getStackTrace()[0].getClassName());
	private TestTrackerProject testTrackerProjectAnnotation = null;
	private TestTracker testTrackerAnnotation = null;
	Class<?> testClass = null;
	private enum PROVISION_STATES {
		VALIDATE_PROJECT,
		VALIDATE_MILESTONE,
		VALIDATE_RUN,
		VALIDATE_SUITE,
		VALIDATE_SECTION,
		VALIDATE_TESTCASE,
		COMPLETE;
	}
	
	
	public ValidationStateMachine(TestTrackerProject testTrackerProjectAnnotation, TestTracker testTrackerAnnotation, Class<?> testClass) {
		this.testTrackerProjectAnnotation = testTrackerProjectAnnotation;
		this.testTrackerAnnotation = testTrackerAnnotation;
		this.testClass = testClass;
		
	}

	
	public void validateAnnotations() throws Exception {
		PROVISION_STATES parserState = PROVISION_STATES.VALIDATE_PROJECT;
	
		try {
			while(parserState != PROVISION_STATES.COMPLETE) {
		        
				
		        switch(parserState) {   

		        	case VALIDATE_PROJECT:
		        		
		        		if (testTrackerProjectAnnotation == null){
							//throw new IllegalArgumentException("TestTracker Project Annotation not defined on class: " + testClass.getName());
						}
						
		        		parserState = PROVISION_STATES.VALIDATE_MILESTONE;	
		        	
			    		break;
	                
			    		
		        	case VALIDATE_MILESTONE:
		        		
		        		if (testTrackerProjectAnnotation.mileStoneName() != null){
		        			if(testTrackerProjectAnnotation.createMileStone() && 
		        					testTrackerProjectAnnotation.mileStoneName().equals(TestRailConstants.MILESTONE_NAME_DEFAULT)) {
		        				throw new IllegalArgumentException("TestTracker MileStone Annotation not defined on class: " + testClass.getName());
						}
						
		        		parserState = PROVISION_STATES.VALIDATE_RUN;	
		        	
			    		break;	
		        	}
		        		
		        	case VALIDATE_RUN:
		        		
		        		if (testTrackerProjectAnnotation.runName() != null){
		        			if(testTrackerProjectAnnotation.createRun() && 
		        					testTrackerProjectAnnotation.runName().equals(TestRailConstants.RUN_NAME_DEFAULT)) {
		        				throw new IllegalArgumentException("TestTracker Run Annotation not defined on class: " + testClass.getName());
						}
						
		        		parserState = PROVISION_STATES.VALIDATE_SUITE;	
		        	
			    		break;	
		        	}
		        		
		        	case VALIDATE_SUITE:
		        		
		        		if (testTrackerProjectAnnotation.suiteName() != null){
		        			if(testTrackerProjectAnnotation.createSuite() && 
		        					testTrackerProjectAnnotation.suiteName().equals(TestRailConstants.SUITE_NAME_DEFAULT)) {
		        				throw new IllegalArgumentException("TestTracker Suite Annotation not defined on class: " + testClass.getName());
						}
						
		        		parserState = PROVISION_STATES.VALIDATE_SECTION;	
		        	
			    		break;	
		        	}
		        		
		        	case VALIDATE_SECTION:
		        		
		        		if (testTrackerProjectAnnotation.sectionName() != null){
		        			if(testTrackerProjectAnnotation.createSection() && 
		        					testTrackerProjectAnnotation.sectionName().equals(TestRailConstants.SECTION_NAME_DEFAULT)) {
		        				throw new IllegalArgumentException("TestTracker Section Annotation not defined on class: " + testClass.getName());
						}
						
		        		parserState = PROVISION_STATES.VALIDATE_TESTCASE;	
		        	
			    		break;	
		        	}
		        		
					case VALIDATE_TESTCASE:
						
						if (testTrackerAnnotation.testCaseName() != null){
							if(testTrackerProjectAnnotation.createTestCase() && 
									testTrackerProjectAnnotation.suiteName().equals(TestRailConstants.TESTCASE_NAME_DEFAULT)) {
								throw new IllegalArgumentException("TestTracker TestCase Annotation not defined on class: " + testClass.getName());
						}
	
						parserState = PROVISION_STATES.COMPLETE;	

						break;	
}
		        	
		            default:
		                
		            	LOGGER.error("Illegal State Machine State");
		            	parserState = PROVISION_STATES.COMPLETE;    
		        }
		    }
		}finally {
			
		}
		
	}
	
	
}

	

