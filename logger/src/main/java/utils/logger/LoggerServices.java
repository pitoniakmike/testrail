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

package utils.logger;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import utils.log4j.Log4jServices;


public class LoggerServices {
	private static final Logger LOGGER = LoggerFactory.getLogger(new Throwable().getStackTrace()[0].getClassName());
	public static final String  NEWLINE = "\r\n|\n|\r";
	private final int DEFAULT_BANNER_WIDTH = 25;
	private final int MIN_BANNER_CHARS = 8;
	private final int MAX_BANNER_CHARS = 70;
	private final char DEFAULT_BANNER_CHAR = '#';
	private final String BANNER_PAD = "\n\n";
	private final String FOOTER_PAD = "\n";
	
	
	public static LoggerServices build() {
	    return new LoggerServices();
	}
	
	private LoggerServices() {

	}
	
	public synchronized String bannerWrap(String txt){
		return banner(txt);
	}
	
	/*
	 * 
	 * Places headderTxt in BANNER_CHAR String
	 * 
	 */
	public synchronized String bannerWrap(String headderTxt, String txt){
		return bannerWrap(headderTxt, txt, DEFAULT_BANNER_CHAR);
	}
	
	public synchronized String bannerWrap(String headderTxt, String txt, char bannerChar){
		int width = headderTxt.length()+40;
		String[] data = txt.split(NEWLINE);
		for(String s : data){
			if(s.length()>width){
				width=s.length();
			}
		}
		return bannerWrap(headderTxt, txt, width, bannerChar);
	}

	/*
	    ex)
	     
		################################
	
		Copying local files out to Proxy
	
		################################
	
	*/
	public synchronized String bannerWrap(String txt, char c){
		return banner(txt, c);
	}

	public synchronized String bannerWrap(String headderTxt, String txt, int length, char c){
		return bannerWrp(headderTxt, txt, length, c);
	}
	
	private String banner(String txt){
		return txt.contains("\n") ? bannerMultiLine(txt, DEFAULT_BANNER_WIDTH) : banner(txt, DEFAULT_BANNER_CHAR);
	}
	
	private String banner(String txt, char c){
		return txt.contains("\n") ? bannerMultiLine(txt, DEFAULT_BANNER_WIDTH, c) :banner(txt, c, DEFAULT_BANNER_WIDTH);
	}
	
	private String banner(String txt, char c, int bannerWidth){
		return  txt.contains("\n") ? bannerMultiLine(txt, bannerWidth, c) : bannerPad(txt, c, bannerWidth);
	}

	private String bannerPad(String txt, char c, int bannerWidth){
		String header = BANNER_PAD;
		String padStr = "";
		
		if(txt.length()> bannerWidth){
			bannerWidth = txt.length();
		}

		for (int i = 0; i<bannerWidth; i++){
			header = header + c;
		}
		int pad = (bannerWidth-txt.length())/2;
		
		for(int i=0; i<pad; i++){
			padStr+= " ";
		}
		
		header+=BANNER_PAD+ padStr + txt + BANNER_PAD;
		for (int i = 0; i<bannerWidth; i++){
			header = header + c;
		}
		header += FOOTER_PAD;
		
		return header;
	}
	
	private String bannerWrp(String headderTxt, String txt, int length, char c){
		String header = BANNER_PAD;
		final char BANNER_CHAR = c;
		
		int bannerCharLength=(txt.length()-headderTxt.length())/2>6 ? (txt.length()-headderTxt.length())/2 : MIN_BANNER_CHARS;
		bannerCharLength = bannerCharLength>MAX_BANNER_CHARS ? MAX_BANNER_CHARS : bannerCharLength;
		//header
		for (int i = 0; i<bannerCharLength; i++){
			header = header + BANNER_CHAR;
		}
		header+=" " + headderTxt + " ";
		//footer
		for (int i = 0; i<bannerCharLength; i++){
			header = header+= BANNER_CHAR;
		}
		int finalLength = header.length()-2;
		header+= BANNER_PAD + txt + BANNER_PAD;
		for (int i = 0; i<finalLength; i++){
			header = header+= BANNER_CHAR;
		}
		header += FOOTER_PAD;
		
		return header;
	}

	private String bannerMultiLine(String txt, Integer length){
		return bannerMultiLine(txt, length, DEFAULT_BANNER_CHAR);
	}
	
	private String bannerMultiLine(String txt, Integer length, char c){
		String header = BANNER_PAD;
		final char BANNER_CHAR = c;
		
		int bannerWidth = getMaxStrLength(txt)<length ? length : getMaxStrLength(txt);

		for (int i = 0; i<bannerWidth; i++){
			header = header + BANNER_CHAR;
		}
		header+=BANNER_PAD+ txt + BANNER_PAD;
		for (int i = 0; i<bannerWidth; i++){
			header = header + BANNER_CHAR;
		}
		header += FOOTER_PAD;
		
		return header;
	}
	
	
	private int getMaxStrLength(String str){
		int length = -1;
		String[] arr = str.split("\\n");
		for(String s : arr){
			length = length<s.length() ? s.length(): length;
		}
		
		return length;
	}
	
	/*
	 * 
	 * 	TURN OFF ALL APACHE LOGGIG
	 * 
	 * 
	 */
	public void disableApacheCommonsLogging(){
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
	}
	
	
	/*
	 * 
	 * 	Log4j
	 * 
	 */
	public void disableLog4jLogging(String name) {
		org.apache.log4j.LogManager.getLogger(name).setLevel(org.apache.log4j.Level.OFF);
	}
	
	
	/*
	 * 
	 * 	java.util.logging
	 * 
	 */
	
	//name: "net.sf.expectit"
	public ConsoleHandler enableLogging(String name) {
        final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(name);
        final ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINE);
        LOGGER.addHandler(handler);
        LOGGER.setLevel(Level.FINE);
        return handler;
    }

	//name: "net.sf.expectit"
	public void disableLogging(String name) {
        final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
        final ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.OFF);
        logger.setLevel(Level.OFF);
    }

	
	
	public void main(String[] args) {
		try{
			//LOGGER.info(banner("m message", DEFAULT_BANNER_CHAR));
			//LOGGER.info(banner("ashaskfdhahs\nfhsdfsjdfhkjshjfdskfdhkjhsfd\nskjfsdjfkj", DEFAULT_BANNER_CHAR, DEFAULT_BANNER_WIDTH));

			LOGGER.info(bannerWrap("headder"));
			LOGGER.info(bannerWrap("headder", '#'));
			LOGGER.info(bannerWrap("headder", "body\nasdsahsashdsdasdasjdklasd"));
			LOGGER.info(bannerWrap("headder", "body\nasdsahsashd\nsdasdasjdklasd"));
			LOGGER.info(bannerWrap("headder", "body"));
		}catch(Exception e){
			LOGGER.error("{}\n{}", e.getMessage()==null ? "" : e.getMessage(), ExceptionUtils.getStackTrace(e));
		}
	}

}
