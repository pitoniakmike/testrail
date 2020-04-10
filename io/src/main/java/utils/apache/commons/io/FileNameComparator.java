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

package utils.apache.commons.io;

import java.io.File;
import java.util.Comparator;

public class FileNameComparator implements Comparator<File> {
    public int compare( File a, File b ) {
        return a.getName().compareTo( b.getName() );
    }
}