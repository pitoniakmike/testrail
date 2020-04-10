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



public enum CATEGORY {
    Regression(1), 
    Compatibility(2),
    Acceptance(3), 
    Performance(4), 
    Solution(5), 
    Other(6);

    
    private final int category;
    
    //this private constructor is called by each enum constucted above, and its value will be returned by the request() method
    private CATEGORY(int category) {
        this.category = category;
    }

    public int getValue() {
        return category;
    }
    
    public static CATEGORY fromValue(Integer v) {
        for (CATEGORY c: CATEGORY.values()) {
            if (c.category==v) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }
    
    public static CATEGORY fromValue(String v) {
        for (CATEGORY c: CATEGORY.values()) {
            if (c.toString().equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
    
}
