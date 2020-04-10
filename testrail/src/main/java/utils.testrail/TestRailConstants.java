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

public class TestRailConstants {
	public static final String 	TESTCASE_NAME_DEFAULT = "TestCase Name not assigned";
	public static final String 	PROJECT_NAME_DEFAULT = "Project Name not assigned";
	public static final String 	RUN_NAME_DEFAULT = "Run Name not assigned";
	public static final String 	SUITE_NAME_DEFAULT = "Suite Name not assigned";
	public static final String 	SECTION_NAME_DEFAULT = "Section Name not assigned";
	public static final String 	MILESTONE_NAME_DEFAULT = "Project Name not assigned";
	public static final String 	CATEGORY_DEFAULT = "Regression";
	public static final String 	ASSIGNED_TO_DEFAULT = "Not Assigned";
	public static final Integer RETRY_DEFAULT = 6;
	public static final Integer RETRY_SLEEP_DEFAULT = 30000;
	public static final String  CSV_FILE_PATH = "results.csv";
	public static final String  CSV_FILE_HEADDER = "Project," + "version," + "MileStone," + "Run," + "Suite Name," + "Section Name," + "TestCase Title," + "Focus," + "Assigned To," + "Status," + "Comments";
	public static final String  VERSION = "Version not assigned";
}
