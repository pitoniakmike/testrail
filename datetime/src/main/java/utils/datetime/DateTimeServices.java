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

package utils.datetime;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class DateTimeServices implements Serializable, Cloneable{
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(new Throwable().getStackTrace()[0].getClassName());
	public static final String UNIX_DATE_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy"; 
	public static final String BASIC_DATE_TIME_FORMAT = "yyyy-MM-dd' 'HH:mm:ss";
	private long current= System.currentTimeMillis();
	
	
	
	public static DateTimeServices build() {
	    return new DateTimeServices();
	}
	
	private DateTimeServices() {}
	
	/*
	 	Since the granularity of a PC can be as high as 55ms (down to 10ms), you can't use 
	 	the System time to generate a unique ID because of the risk of getting duplicated IDs. 
	 	This can be solved by using the following technique to make sure that the number returned 
	 	is unique (in a single JVM). 
	 */
	public synchronized long getUniqueLong(){
	    return current++;
	}



	public String elapsedTime(long startTimeMs, long currentTimeMs) {
	    return DurationFormatUtils.formatDuration(currentTimeMs-startTimeMs, "HH:mm:ss");
	}

	//00:10:00,006
	public String getTimeMsAsHrMinSecStr(long timeInMS){
		return DurationFormatUtils.formatDuration(timeInMS, "HH:mm:ss");
	}
	
	//"yyyy/MM/dd" or "yyyy/MM/dd HH:mm:ss"
	public long getDateTimeMs(String dateTime) throws ParseException{
		return  getDateTimeMs(dateTime, dateTime.contains(":") ? "yyyy/MM/dd HH:mm:ss" : "yyyy/MM/dd");
	}
	
	//"yyyy/MM/dd" , "yyyy/MM/dd HH:mm:ss" 

	public long getDateTimeMs(String dateTime, String simpleDateFormat) throws ParseException{
		 DateFormat formatter = new SimpleDateFormat(simpleDateFormat);
		 return formatter.parse(dateTime).getTime();
	}

	public long getTimeMsFromDate(String date){
		String[] tokens = date.split("/");
	    return getTimeMsFromDate(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
	}
	
	public long getTimeMsFromDate(int year, int month, int dayOfMonth){
		GregorianCalendar gregorianCalendar = new GregorianCalendar(year, month, dayOfMonth);    
	    Date d = gregorianCalendar.getTime();
	    
	    return d.getTime();
	}
	
	public long getTimeMsFromDate(int year, int month, int dayOfMonth, int hourOfDay, int minute){
		GregorianCalendar gregorianCalendar = new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute);    
	    Date d = gregorianCalendar.getTime();
	    
	    return d.getTime();
	}
	
	public long getTimeMsFromDate(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second){
		GregorianCalendar gregorianCalendar = new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute, second);    
	    Date d = gregorianCalendar.getTime();
	    
	    return d.getTime();
	}

	/*
	  	Wed Jul 17 12:45:19 EDT 2013			"EEE MMM dd HH:mm:ss zzz yyyy"
	  	2011/03/25 09:14:21						"yyyy/MM/dd HH:mm:ss" 	
	  	2001.07.04 AD at 12:08:56 PDT			"yyyy.MM.dd G 'at' HH:mm:ss z" 	
		Wed, Jul 4, '01							"EEE, MMM d, ''yy" 	
		12:08 PM								"h:mm a" 	
		12 o'clock PM, Pacific Daylight Time	"hh 'o''clock' a, zzzz" 	
		0:08 PM, PDT							"K:mm a, z" 	
		02001.July.04 AD 12:08 PM				"yyyyy.MMMMM.dd GGG hh:mm aaa" 	
		Wed, 4 Jul 2001 12:08:56 -0700			"EEE, d MMM yyyy HH:mm:ss Z" 	
		010704120856-0700						"yyMMddHHmmssZ" 	
	 */
	public long getTimeMsFromUnixDateStr(String unixDateStr) throws ParseException{
	    return getTimeMsFromUnixDatePattern(unixDateStr, UNIX_DATE_FORMAT); 
	}

	/*
	  	Wed Jul 17 12:45:19 EDT 2013			"EEE MMM dd HH:mm:ss zzz yyyy"
	  	2011/03/25 09:14:21						"yyyy/MM/dd HH:mm:ss" 	
	  	2001.07.04 AD at 12:08:56 PDT			"yyyy.MM.dd G 'at' HH:mm:ss z" 	
		Wed, Jul 4, '01							"EEE, MMM d, ''yy" 	
		12:08 PM								"h:mm a" 	
		12 o'clock PM, Pacific Daylight Time	"hh 'o''clock' a, zzzz" 	
		0:08 PM, PDT							"K:mm a, z" 	
		02001.July.04 AD 12:08 PM				"yyyyy.MMMMM.dd GGG hh:mm aaa" 	
		Wed, 4 Jul 2001 12:08:56 -0700			"EEE, d MMM yyyy HH:mm:ss Z" 	
		010704120856-0700						"yyMMddHHmmssZ" 	
	 */
	public long getTimeMsFromUnixDatePattern(String dateStr, String pattern) throws ParseException{
	    SimpleDateFormat formatter = new SimpleDateFormat(pattern);  
	    Date date = formatter.parse(dateStr);  
		
	    return date.getTime(); 
	}

	/*
	  	Wed Jul 17 12:45:19 EDT 2013			"EEE MMM dd HH:mm:ss zzz yyyy"
	  	2011/03/25 09:14:21						"yyyy/MM/dd HH:mm:ss" 	
	  	2001.07.04 AD at 12:08:56 PDT			"yyyy.MM.dd G 'at' HH:mm:ss z" 	
		Wed, Jul 4, '01							"EEE, MMM d, ''yy" 	
		12:08 PM								"h:mm a" 	
		12 o'clock PM, Pacific Daylight Time	"hh 'o''clock' a, zzzz" 	
		0:08 PM, PDT							"K:mm a, z" 	
		02001.July.04 AD 12:08 PM				"yyyyy.MMMMM.dd GGG hh:mm aaa" 	
		Wed, 4 Jul 2001 12:08:56 -0700			"EEE, d MMM yyyy HH:mm:ss Z" 	
		010704120856-0700						"yyMMddHHmmssZ" 	
	 */
	public String getSimpleDateFormat(String format){
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
	    return sdf.format(date);
	}
	
	public String getFormattedCurrentDateTime(String format){
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat (format);

        return formatter.format(date);
    }

	//  04 21:53:43 EST 2003
	public Date getDate(String dateStr, String simpleDateFormat) throws ParseException{
		SimpleDateFormat formatter = new SimpleDateFormat(simpleDateFormat);
		Date date = formatter.parse(dateStr);
		
	    return date;
	}
	// http://oak.cs.ucla.edu/cs144/projects/java/simpledateformat.html
	// example:   "04 21 2012 12:32:30"
	//M 	Month in year 	Month 	July; Jul; 07
	public Date getDate(String M_dd_yyyy_HH_mm_ss) throws ParseException{
		SimpleDateFormat formatter = new SimpleDateFormat("M dd yyyy HH:mm:ss");
		Date date = formatter.parse(M_dd_yyyy_HH_mm_ss);
		
	    return date;
	}

	//Jun 7, 2013 12:10:56 PM
	public Date getDateHms(String MMM_dd_yyyy_HH_mm_ss) throws ParseException{
		SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss a");
		Date date = formatter.parse(MMM_dd_yyyy_HH_mm_ss);
		
	    return date;
	}
	
	public Date getCurrentDate(){
		Date date = new Date(System.currentTimeMillis());
	    return date;
	}

	public String getCurrentDateTimeLog4jFormat(){
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
	    return sdf.format(date);
	}
	
	//2018-02-16 07:24:52,828
	public String getCurrentTimeStamp() {
	    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS").format(new Date());
	}
	
	public String getCurrentDateTime(String format){
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
	    return sdf.format(date);
	}
	
	public String getCurrentDateTime(){
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
	    return sdf.format(date);
	}
	
	public String getDateTime(){   
	    return getDateTime("yyyy-MM-dd_hh:mm:ss");
	} 

	//returns 2012-11-20_08-49-05
	public  final String getDateTimeFileExtension() {
		return getDateTimeFileExtension("EST");
	}

	//returns 2012-11-20_08-49-05
	public  final String getDateTimeFileExtension(String timeZone) {  
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss");  
        df.setTimeZone(TimeZone.getTimeZone(timeZone));  
        return df.format(new Date());  
    }  
	
	public String getDateTime(String format){   
	    DateFormat df = new SimpleDateFormat(format);   
	    return df.format(new Date());   
	} 
	
	public String getDateTime(long timeMs){   
	    return getDateTime("yyyy-MM-dd_hh:mm:ss", timeMs);   
	} 
	
	//"yyyy-MM-dd_hh:mm:ss"
	public String getDateTime(String format, long timeMs){   
	    DateFormat df = new SimpleDateFormat(format);     
	    return df.format(new Date(timeMs));   
	} 

	public String getDateTimeAsString(String delim) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH)+1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        int sec = c.get(Calendar.SECOND);
        return month + delim + day + delim + year + delim + hour + delim + (min > 9 ? "" + min : "0" + min) + delim +
        (sec > 9 ? "" + sec : "0" + sec);
    }

	public String getTimeMsAsString(long ms){
		String response = null;
		DecimalFormat f = new DecimalFormat("##.00");
		
		if(ms<1000){
			response = ms + "ms";
		}else if(ms>=1000 && ms<60000){
			response = f.format(ms/1000.0) + "sec";
		}else if(ms>=60000 && ms<86400000){
			response = f.format(ms/60000.0) + "hr";
		}else if(ms>=86400000){
			response = f.format(ms/86400000.0) + "day";
		} 
		
		return response;
	}
	
    public String getTimeAsString(String delim) {
        Calendar c = Calendar.getInstance();

        int hour = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        int sec = c.get(Calendar.SECOND);
        return hour + delim + (min > 9 ? "" + min : "0" + min) + delim +
        (sec > 9 ? "" + sec : "0" + sec);
    }
    
    // 3 minutes, 9 seconds, 189ms
    public String getTimeAsString(long timeMs, boolean bMillis) {
    	 String response = null;
         long minutes = (timeMs / 1000) / 60;
         long seconds = (timeMs / 1000) % 60;
         long ms = (timeMs / 1000) % 60000;
         
         response = minutes + "min, " + seconds + "sec";
         if(bMillis) {
        	 response += ", " + ms + "ms";
         }
         return response;
    }
    
    public String getTimeMsAsString()throws IOException{
    	Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SS");
        String datedStr = sdf.format(cal.getTime());
        datedStr = datedStr.replaceAll("/" , "-");
        datedStr = datedStr.replaceAll(" " , "_");
        datedStr = datedStr.replaceAll(":" , "-");
        datedStr = datedStr.replaceAll("\\." , "-");
        
        return datedStr;
    }
    
    /*
      	Unix Timestamp

		seconds since Jan 01 1970. (UTC)
		
		NOT milliseconds
     */
    public long getUnixTimeStamp(){
    	return System.currentTimeMillis()/1000;
    }
    
    public long getUnixTimeStamp(Date date){
    	return date.getTime()/1000;
    }
    
    public long getUnixTimeStamp(String dateTimeFormat, String dateTime) throws ParseException{
    	SimpleDateFormat formatter = new SimpleDateFormat(dateTimeFormat);
		Date date = formatter.parse(dateTime);
    	return date.getTime()/1000;
    }
    
    // example:   "04 21 2012 12:32:30"
 	//M 	Month in year 	Month 	July; Jul; 07
    public long getUnixTimeStamp(String M_dd_yyyy_HH_mm_ss) throws ParseException{
    	SimpleDateFormat formatter = new SimpleDateFormat("M dd yyyy HH:mm:ss");
		Date date = formatter.parse(M_dd_yyyy_HH_mm_ss);
    	return date.getTime()/1000;
    }
    
    public Date getDateFromUnixTimeStamp(long unixTimeStamp){
    	return new Date(unixTimeStamp*1000);
    }

    public int getMonth(){
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.MONTH);
    }
    
    public String getMonthAsString(){
    	String[] monthName = {"January", "February",
            "March", "April", "May", "June", "July",
            "August", "September", "October", "November",
            "December"};

        Calendar cal = Calendar.getInstance();
        return monthName[cal.get(Calendar.MONTH)];
    }
    
    /*
     * 	Field number for get and set indicating the month. This is a calendar-specific value. 
     * The first month of the year in the Gregorian and Julian calendars is JANUARY which is 0; 
     * the last depends on the number of months in a year.
     */
    public String getMonthAsString(boolean zeroBased, boolean pad){
        Calendar cal = Calendar.getInstance();
        int month = zeroBased ? cal.get(Calendar.MONTH) : cal.get(Calendar.MONTH)+1;
        return (pad && month<10) ? "0" + String.valueOf(month) :  String.valueOf(month);
    }
    
    //dayMonthYear: creates date string in the format "9/18/2010".
	public String getDayMonthYearAsString() {
		String sName = null;
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(System.currentTimeMillis());
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int year = cal.get(Calendar.YEAR);
		sName = "" + month + "/" + day + "/" + year;
		return sName;
	}
	
	//	20180628
	public String getYearWeekDayAsString() {
		return getYear() + getMonthAsString(false, true)  + getDayOMonthAsString(true) ;
	}
    
    public String getDayOfWeekAsString(boolean pad) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(System.currentTimeMillis());
		int day =  cal.get(Calendar.DAY_OF_WEEK);
		
		return (pad && day<10) ? "0" + String.valueOf(day) :  String.valueOf(day);
	}
    
    public int getDayOMonth() {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(System.currentTimeMillis());

		return cal.get(Calendar.DAY_OF_MONTH);
	}
    
    /*
     * Field number for get and set indicating the day of the month. This is a synonym for DATE. 
     * The first day of the month has value 1.
     * 
     */
    public String getDayOMonthAsString(boolean pad){
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return (pad && day<10) ? "0" + String.valueOf(day) :  String.valueOf(day);
    }
    
    public int getYear() {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(System.currentTimeMillis());

		return cal.get(Calendar.YEAR);
	}
    
    public int getHour() {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(System.currentTimeMillis());

		return cal.get(Calendar.HOUR);
	}
    
    public int getHourOfDay() {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(System.currentTimeMillis());

		return cal.get(Calendar.HOUR_OF_DAY);
	}
    
    public int getMinute() {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(System.currentTimeMillis());

		return cal.get(Calendar.MINUTE);
	}

	public int getSecond() {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(System.currentTimeMillis());

		return cal.get(Calendar.SECOND);
	}
    
    //2/9/2009 16:41:22
    public String getExcelTimeStamp(){
    	String excelTimeStamp = null;
    	GregorianCalendar currentCalendar = new GregorianCalendar();
    	currentCalendar.setTime(new Date());
    	excelTimeStamp = currentCalendar.get(Calendar.MONTH)+1 + 
    			"/" + currentCalendar.get(Calendar.DAY_OF_MONTH) + 
    			"/" + currentCalendar.get(Calendar.YEAR) + 
    			" " + currentCalendar.get(Calendar.HOUR_OF_DAY) + 
    			":" + currentCalendar.get(Calendar.MINUTE) +
    			":" + currentCalendar.get(Calendar.SECOND);
    	
    	return excelTimeStamp;
    }
    
  //negative numbers for past dates
    public Date getDateMinuteOffset(int offset){
    	Calendar currentDate = new GregorianCalendar();
       	currentDate.add(GregorianCalendar.MINUTE, offset);

    	return currentDate.getTime();
    }
    
    //negative numbers for past dates
    public Date getDateHourOffset(int offset){
    	Calendar currentDate = new GregorianCalendar();
       	currentDate.add(GregorianCalendar.HOUR_OF_DAY, offset);

    	return currentDate.getTime();
    }
    
    //negative numbers for past dates
    public Date getDateDayOffset(int offset){
    	Calendar currentDate = new GregorianCalendar();
       	currentDate.add(GregorianCalendar.DAY_OF_MONTH, offset);

    	return currentDate.getTime();
    }
    
    //negative numbers for past dates
    public Date getDateWeekOffset(int offset){
    	Calendar currentDate = new GregorianCalendar();
       	currentDate.add(GregorianCalendar.WEEK_OF_YEAR, offset);

    	return currentDate.getTime();
    }
    
    //negative numbers for past dates
    public long getDateTimeMsWeekOffset(int offset){
    	Calendar currentDate = new GregorianCalendar();
       	currentDate.add(GregorianCalendar.WEEK_OF_YEAR, offset);

    	return currentDate.getTimeInMillis();
    }
    

    //negative numbers for past dates
    public Date getDateMonthOffset(int offset){
    	Calendar currentDate = new GregorianCalendar();
       	currentDate.add(GregorianCalendar.MONTH, offset);

    	return currentDate.getTime();
    }
 
    //negative numbers for past dates
    public Date getDateYearOffset(int offset){
    	Calendar currentDate = new GregorianCalendar();
       	currentDate.add(GregorianCalendar.YEAR, offset);

    	return currentDate.getTime();
    }
    
    public long diffDates(Date startDate, Date endDate){
    	return endDate.getTime()-startDate.getTime();
    }

    public String getDeltaTimeAsString(long startTime, long stopTime, boolean displayMs) {
        long scriptRunTimeInSec;
        int hour = 0;
        int min = 0;
        int sec = 0;
        int ms = 0;
        String runtime = null;
        
        scriptRunTimeInSec = stopTime - startTime;
        hour = (int)(scriptRunTimeInSec/3600000L);
        min = (int)((scriptRunTimeInSec % 3600000)/60000);
        sec = (int)((scriptRunTimeInSec % 60000)/1000);
        ms = (int)((scriptRunTimeInSec % 1000));
        runtime = hour + ":" + (min > 9 ? "" + min : "0" + min) + ":" + (sec > 9 ? "" + sec : "0" + sec) ;
        if(displayMs){
        	runtime+= ":" + (ms > 9 ? "" + ms : "0" + ms);
        }

        return runtime;
    }
    
    public synchronized void waitUntil(long timeMs, long sleepMs) throws Exception{
    	while(System.currentTimeMillis() < timeMs){
    		Thread.sleep(sleepMs);
    	}
    }
    
    public void waitUntil(int hourOfDay, int minute, int second, long sleepMs) throws Exception{
    	GregorianCalendar currentCalendar = new GregorianCalendar();
		currentCalendar.setTime(new Date());

		waitUntil(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH),
				currentCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute, second, sleepMs);
	}
    
    public void waitUntil(int year, int month,
			int dayOfMonth, int hourOfDay, int minute, int second, long sleepMs) throws Exception{
		
    	if(sleepMs==0){
    		throw new Exception("Illegal wait interval (infinite)");
    	}
    	GregorianCalendar currentCalendar = new GregorianCalendar();
		currentCalendar.setTime(new Date());

		GregorianCalendar futureCalendar = new GregorianCalendar(year, month,
				dayOfMonth, hourOfDay, minute, second);

		//returns:
		//the value 0 if the time represented by the argument is equal to the time represented by this Calendar; 
		//a value less than 0 if the time of this Calendar is before the time represented by the argument; 
		//and a value greater than 0 if the time of this Calendar is after the time represented by the argument. 
		while (futureCalendar.compareTo(currentCalendar) >= 0) {
			LOGGER.info("Comparing: " + futureCalendar.getTime() + " to: " + currentCalendar.getTime());
			currentCalendar.setTime(new Date());
			Thread.sleep(sleepMs);
		}
	}
    
    public void waitUntilTop(long sleepMs) throws Exception{
    	if(sleepMs==0){
    		throw new Exception("Illegal wait interval (infinite)");
    	}
    	GregorianCalendar currentCalendar = new GregorianCalendar();
		currentCalendar.setTime(new Date());
    	
    	waitUntil(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH),
        			currentCalendar.get(Calendar.DAY_OF_MONTH), currentCalendar.get(Calendar.HOUR_OF_DAY), 59, 59, sleepMs);
	}
    
    public void waitUntilBottom(long sleepMs) throws Exception{
    	if(sleepMs==0){
    		throw new Exception("Illegal wait interval (infinite)");
    	}
    	GregorianCalendar currentCalendar = new GregorianCalendar();
		currentCalendar.setTime(new Date());
		
    	waitUntil(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH),
    		currentCalendar.get(Calendar.DAY_OF_MONTH), currentCalendar.get(Calendar.HOUR_OF_DAY), 30, 0, sleepMs);
	}
	/*
     * 	-1 if date1 before date2, 0 if equal, 1 id date1 after date2
     */
	public int compareDates(long date1, long date2) throws Exception{
		return compareDates(new Date(date1), new Date(date2));
	}
    
    /*
     * 	-1 if date1 before date2, 0 if equal, 1 id date1 after date2
     */
	public int compareDates(Date date1, Date date2) throws Exception{
		int results;
		
		if (date1.equals(date2)){
			results = 0;
		}else if (date1.before(date2)){
	    	results = -1;
	    }else{
	    	results = 1;
	    }
		
		return results;
	}
	
	public boolean isDateBefore(Date date1, Date date2) throws Exception{
		boolean results = true;
		
		if (date1.equals(date2)){
			results = false;
		}else if (date1.after(date2)){
	    	results = false;
	    }
		
		return results;
	}
	
	public boolean isDateAfter(Date date1, Date date2) throws Exception{
		boolean results = true;
		
		if (date1.equals(date2)){
			results = false;
		}else if (date1.before(date2)){
	    	results = false;
	    }
		
		return results;
	}
	
	public boolean isSameDate(Date date1, Date date2) throws Exception{
		return date1.equals(date2);
	}

	public static void main(String[] args) {
		try{
			
			
			LOGGER.info(new DateTimeServices().getCurrentDateTime("yyyy-MM-dd HH:mm:ss,SSS"));
			/*LOGGER.info(new DateTimeServices().getTimeMsAsString(1001));
			LOGGER.info(new DateTimeServices().getTimeMsAsString(34000));
			LOGGER.info(new DateTimeServices().getTimeMsAsString(60000));
			LOGGER.info(new DateTimeServices().getTimeMsAsString(260000));
			LOGGER.info(new DateTimeServices().getTimeMsAsString(360000));
			LOGGER.info(new DateTimeServices().getTimeMsAsString(360001));*/
			
			//LOGGER.info(new DateTimeServices().getTimeMsAsString(86400000));
			
			//long l = new DateTimeServices().getTimeMsFromUnixDateStr("Thu Jul 18 17:24:09 EDT 2013");
			//LOGGER.info("{}", new Date(l));
			//LOGGER.info("{}", l);
			//LOGGER.info(new DateTimeServices().getDateTimeFileExtension());
			//LOGGER.info(new DateTimeServices().getDateTimeMs("2012-07-30", "yyyy-MM-dd"));
			//LOGGER.info(new DateTimeServices().getDateTimeMs("2012-07-31", "yyyy-MM-dd"));
			//LOGGER.info(new DateTimeServices().getFormattedCurrentDateTime("dd-MM-yyyy"));
			//LOGGER.info(new DateTimeServices().getFormattedCurrentDateTime("yyyy-MM-dd"));
			
			//LOGGER.info(new DateTimeServices().getYear());
			//LOGGER.info(new DateTimeServices().getMonthAsString(false, true));
			//LOGGER.info(new DateTimeServices().getDayOMonthAsString(true));
			
			
			//LOGGER.info(new DateTimeServices().getSimpleDateFormat("yyyy/MM/dd HH:mm:ss"));
			//LOGGER.info(new DateTimeServices().getSimpleDateFormat("MM/dd/yyyy"));
			//LOGGER.info(new DateTimeServices().getSimpleDateFormat("hh:mm:ss"));
			//LOGGER.info(getDeltaTimeAsString(System.currentTimeMillis(), System.currentTimeMillis()));
		}catch(Exception e){
			LOGGER.error("{}\n{}", e.getMessage()==null ? "" : e.getMessage(), ExceptionUtils.getStackTrace(e));
		}
	}
	
	@Override
	   protected DateTimeServices clone() throws CloneNotSupportedException{
	       return (DateTimeServices)SerializationUtils.clone(this);
	   }
	

}
