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

package utils.file;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/** JDK 7+. */
public final class LineMatcher {


  public void matchingLines(String fileName) {
    //Pattern and Matcher are used here, not String.matches(regexp),
    //since String.matches(regexp) would repeatedly compile the same
    //regular expression
    Pattern regexp = Pattern.compile("^\\\\N\\t(\\d)+\\t(\\w)+");
    Matcher matcher = regexp.matcher("");

    Path path = Paths.get(fileName);
    //another way of getting all the lines:
    //Files.readAllLines(path, ENCODING); 
    try (
      BufferedReader reader = Files.newBufferedReader(path, ENCODING);
      LineNumberReader lineReader = new LineNumberReader(reader);
    ){
      String line = null;
      while ((line = lineReader.readLine()) != null) {
        matcher.reset(line); //reset the input
        if (!matcher.find()) {
          String msg = "Line " + lineReader.getLineNumber() + " is bad: " + line;
          throw new IllegalStateException(msg);
        }
      }      
    }    
    catch (IOException ex){
      ex.printStackTrace();
    }
  }

  final static Charset ENCODING = StandardCharsets.UTF_8;
  
  /** Test harness. */
  public static void main(String... arguments) {
    LineMatcher lineMatcher = new LineMatcher();
    lineMatcher.matchingLines("C:\\Temp\\RegexpTest.txt");
    System.out.println("Done.");
  }
} 
