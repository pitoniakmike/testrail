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

public enum TYPE {


    Automated("Automated"),

    Manual("Manual");

    private final String type;

    //this private constructor is called by each enum constucted above, and its value will be returned by the request() method
    private TYPE(String type) {
        this.type = type;
    }
    

    public String type() {
        return type;
    }

    public static TYPE fromValue(String v) {
        for (TYPE c: TYPE.values()) {
            if (c.type.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

} 
