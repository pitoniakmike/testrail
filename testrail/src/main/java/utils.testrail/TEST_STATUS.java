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


/*1 = Passed
2 = Blocked
4 = Retest
5 = Failed*/
public enum TEST_STATUS {
    PASSED(1), BLOCKED(2), RETEST(4), FAILED(5);

    private final int value;
    
    
    private TEST_STATUS(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
