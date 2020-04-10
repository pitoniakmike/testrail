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



public enum SUITE_MODE {
    SingleSuite(1), 
    SingleSuiteWithBaseLine(2),
    MultipleSuites(3); 


    
    private final int mode;
    
    //this private constructor is called by each enum constucted above, and its value will be returned by the request() method
    private SUITE_MODE(int mode) {
        this.mode = mode;
    }

    public int getValue() {
        return mode;
    }
    
    public static SUITE_MODE fromValue(Integer v) {
        for (SUITE_MODE c: SUITE_MODE.values()) {
            if (c.mode==v) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }
    
    public static SUITE_MODE fromValue(String v) {
        for (SUITE_MODE c: SUITE_MODE.values()) {
            if (c.toString().equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
    
}
