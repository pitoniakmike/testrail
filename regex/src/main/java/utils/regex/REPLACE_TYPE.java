/* Copyright (C) 2002 by Michael Pitoniak (pitoniakm@msn.com)
 * All rights are reserved.
 * Reproduction and/or redistribution in whole or in part is expressly
 * prohibited without the written consent of the copyright owner.
 *
 * This Software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */

package utils.regex;


public enum REPLACE_TYPE {
    BEFORE(1), AFTER(2), REPLACE(3), DELETE(4);

    private final int value;
    
    
    private REPLACE_TYPE(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}