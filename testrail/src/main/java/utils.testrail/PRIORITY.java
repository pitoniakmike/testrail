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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/*
	TestNG Priorities
    The scheduling priority. 
    Lower priorities will be scheduled first.

    Default: 0
    
 */

@XmlType(name = "priority")
@XmlEnum
public enum PRIORITY {
	@XmlEnumValue("Low")
	Low(1),
	@XmlEnumValue("Medium")
	Medium(2),
	@XmlEnumValue("High")
	High(3),
	@XmlEnumValue("Critical")
	Critical(4);
	
	
	private Integer priority;

    
    //this private constructor is called by each enum constucted above, and its value will be returned by the request() method
    private PRIORITY(Integer priority){
        this.priority = priority;
    }
    
    public int getValue() {
        return priority;
    }
    
    public Integer priority() {
        return priority;
    }
    
    public static PRIORITY fromValue(Integer v) {
        for (PRIORITY c: PRIORITY.values()) {
            if (c.priority==v) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }
    
    public static PRIORITY fromValue(String v) {
        for (PRIORITY c: PRIORITY.values()) {
            if (c.toString().equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }
    
}
