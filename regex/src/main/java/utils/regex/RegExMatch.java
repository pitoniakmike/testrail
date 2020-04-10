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

package utils.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class RegExMatch {
	private int start = -1;
	private int end = -1;
	private List<String> matchList = new ArrayList<>();
	
	
	public RegExMatch(Matcher matcher) {
		this.start = matcher.start();
		this.end = matcher.end();
		for(int i=0; i<=matcher.groupCount(); i++) {
			matchList.add(matcher.group(i));
		}
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public List<String> getMatchList() {
		return matchList;
	}
	
	@Override
	public String toString() {
		String response = "";
		
		response+= "\nstart: " + start;
		response+= "\nend: " + end;
		for(int i=0; i<= matchList.size(); i++) {
			response+= "\nmatch " + i + ": " + matchList.get(i);
		}
		
		return response;
	}
	
	
}
