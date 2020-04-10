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


package utils.log4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.EnhancedPatternLayout;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.RollingFileAppender;
import org.slf4j.LoggerFactory;

import utils.apache.commons.io.ApacheCommonsIOServices;



public class Log4jServices {
	private static org.slf4j.Logger logger = LoggerFactory.getLogger(new Throwable().getStackTrace()[0].getClassName());
	
	
	public static Log4jServices build() {
	    return new Log4jServices();
	}
	
	private Log4jServices() {}
	
	//logger2 = LogManager.getLogger( "org.jboss.resteasy");
	//logger2.setLevel(Level.OFF);
		
	public void setClassLogLevel(String classPath, Level logLevel) {
		Logger logger = LogManager.getLogger(classPath);
		logger.setLevel(logLevel);	
	}
		
		
	
	
	/*
	  	log4j.logger.migrationlog = INFO, migration
		log4j.appender.migration = org.apache.log4j.RollingFileAppender
		log4j.appender.migration.File = C:/work/log/migration.log
		log4j.appender.migration.MaxFileSize=20MB
		log4j.appender.migration.MaxBackupIndex=1
		log4j.appender.migration.layout = org.apache.log4j.PatternLayout
		log4j.appender.migration.layout.conversionPattern = %d %-5p %c - %m%n
		

		In such case, your Java code should be as follows,
		
		Logger logger = Logger.getLogger("migrationlog"); //Defining the Logger
		FileAppender appender = (FileAppender)logger.getAppender("migration");
		return new File(appender.getFile());
		
		Note that its not migrationlog which was used when making the logger object is used 
		when retrieving the file name, but migration
		
		
		log4j {  
			//   
			appender.stdout = "org.apache.log4j.ConsoleAppender"    
			appender."stdout.layout"="org.apache.log4j.PatternLayout"   
			// 
			appender.scrlog = "org.apache.log4j.RollingFileAppender"  
			appender."scrlog.Append"="true" 
			appender."maxSizeRollBackups value"="3"
			appender."maximumFileSize value"="1MB"
			appender."scrlog.File"="logger.log" 
			appender."scrlog.layout"="org.apache.log4j.PatternLayout"
			//eclipse: =%d %-5p %C.%M(%F:%L) - %m%n
			appender."scrlog.layout.ConversionPattern"="%d %5p %c{1}:%L - %m%n"
			
			rootLogger="debug,scrlog,stdout"     
		
		}

		
		
	 */
	
	public List<String> listRootAppenders(){
		List<String> paths = new ArrayList<>();
		Logger logger = Logger.getRootLogger();
		   
		   Enumeration en = logger.getAllAppenders();
		   int nbrOfAppenders = 0;
		   while (en.hasMoreElements()) {
		    nbrOfAppenders++;
		    Object ap = en.nextElement();
		    paths.add(ap.toString());
		   }
		   
		   return paths;
	}
	
	public String getRootAppenderPath(String appenderName){
		String path = null;
		Logger logger = Logger.getRootLogger();
		   
		   Enumeration en = logger.getAllAppenders();
		   while (en.hasMoreElements()) {
		    Object ap = en.nextElement();
		    if (ap instanceof FileAppender ){
		    	logger.debug(((FileAppender)ap).getName());
		    	  if(((FileAppender)ap).getName().equals(appenderName)){
		    		  path = System.getProperty("user.dir") + "/" + ((FileAppender)ap).getFile();
		    	  }
		      }
		   }
		   
		   return path;
	}
	
	
	public List<String> listAppenders(String loggerName){
		List<String> paths = new ArrayList<>();
		Logger logger = Logger.getLogger(loggerName);
		   
		   Enumeration en = logger.getAllAppenders();
		   int nbrOfAppenders = 0;
		   while (en.hasMoreElements()) {
		    nbrOfAppenders++;
		    Object ap = en.nextElement();
		    paths.add(ap.toString());
		   }
		   
		   return paths;
	}
	
	public String getRootLoggerName(){
		return Logger.getRootLogger().getName();
	}
	
	
	public List<String> getLogFilePaths(){
		List<String> paths = new ArrayList<>();
		try {
			Enumeration e = Logger.getRootLogger().getAllAppenders();
		    while ( e.hasMoreElements() ){
		      Appender app = (Appender)e.nextElement();
		      if ( app instanceof FileAppender ){
		    	  String fileName = ((FileAppender)app).getName();
		    	  File file = new File(((FileAppender)app).getFile());
		    	  paths.add(fileName + ": " + ApacheCommonsIOServices.build().findFilePathInDirList(file.getName(), true));
		      }
		    }
		}catch(Exception e) {
			logger.error("{}\n{}", e.getMessage()==null ? "" : e.getMessage(), ExceptionUtils.getStackTrace(e));
		}
	    
	    return paths;
	}
	
	public String getLogFilePath(String loggerName, String appenderName){
		Logger logger = Logger.getLogger(loggerName); //Defining the Logger
		FileAppender appender = (FileAppender)logger.getAppender(appenderName);
		return new File(appender.getFile()).getAbsolutePath();
	}
	
	public List<String> getLogFilePath(String appenderName){
		List<String> paths = new ArrayList<>();
		Enumeration e = Logger.getRootLogger().getAllAppenders();
	    while ( e.hasMoreElements() ){
	      Appender app = (Appender)e.nextElement();
	      if ( app instanceof FileAppender ){
	    	  logger.debug(app.getName());
	    	  if(app.getName().equals(appenderName)){
	    		  paths.add(System.getProperty("user.dir") + "/" + ((FileAppender)app).getFile());
	    	  }
	      }
	    }
	    
	    return paths;
	}

	private void updateLog4jConfiguration(String logFile) { 
	    Properties props = new Properties(); 
	    try { 
	        InputStream configStream = getClass().getResourceAsStream( "/log4j.properties"); 
	        props.load(configStream); 
	        configStream.close(); 
	    } catch (IOException e) { 
	        logger.debug("Errornot laod configuration file "); 
	    } 
	    props.setProperty("log4j.appender.FILE.file", logFile); 
	    LogManager.resetConfiguration(); 
	    PropertyConfigurator.configure(props); 
	 }

	
	public void setLogLevel(Level value) {
		LogManager.getRootLogger().setLevel(value);
	}
	
	
	public void setRootLogLevel(Level level) {
		LogManager.getRootLogger().setLevel(level);
	}
	
	/*
	 * 
	 * 
	 * 	NOTE: 	this sets root logger thresholds for all classes that DO NOT  set their own.
	 *        	if a log level is set in beforeClass() for a test that will override this setting
	 * 			default set in log4j.xml, then beforeClass() in testng, then here
	 * 
	 * 
	 */
	public void setAppenderLogLevel(Level value) {
		for(Enumeration<?> aen = LogManager.getRootLogger().getAllAppenders(); aen.hasMoreElements(); ) {
            Appender appender = (Appender) aen.nextElement();
            logger.debug("log4j appender: {}", appender.getName());
		}
		
		ConsoleAppender consoleAppender = (ConsoleAppender)LogManager.getRootLogger().getAppender("STDOUT");
		consoleAppender.setThreshold(value);
		consoleAppender.activateOptions();

		RollingFileAppender appender = (RollingFileAppender)LogManager.getRootLogger().getAppender("RollingFileAppender");
		appender.setThreshold(value);
		appender.activateOptions();
	}
	
	public void modifyAppender(String logFileName, String appenderName, int backupIndex, String size) throws IOException {
	    ApacheCommonsIOServices.build().deleteFiles(ApacheCommonsIOServices.build().listFilesWildCard(".", logFileName+".*", false));
		Logger logger = LogManager.getRootLogger();
		RollingFileAppender rfappender = (RollingFileAppender) logger.getAppender(appenderName);
		rfappender.setMaxBackupIndex(backupIndex);
		rfappender.setMaxFileSize(size);
		rfappender.activateOptions();
	}
	
	public void modifyAppender(String logFileName, String appenderName, int backupIndex, long size) throws IOException {
	    ApacheCommonsIOServices.build().deleteFiles(ApacheCommonsIOServices.build().listFilesWildCard(".", logFileName+".*", false));
		Logger logger = LogManager.getRootLogger();
		RollingFileAppender rfappender = (RollingFileAppender) logger.getAppender(appenderName);
		rfappender.setMaxBackupIndex(backupIndex);
		rfappender.setMaximumFileSize(size);
		rfappender.activateOptions();
	}
	
	/*
	 * Change the name of the the log file as configured through log4j.xml
	 * by replacing the placeholder file name token ("Launcher") with the
	 * a new "actionName".
	 */
	private void log4jConfig(String actionName) {
		String LOG4J_ROLLING_FILE_NAME_TOKEN = "Launcher";
	    Logger rootLogger = LogManager.getRootLogger();
	    RollingFileAppender fileAppender = (RollingFileAppender)rootLogger.getAppender("fileAppender");

	    // <param name="FileNamePattern" value="/var/log/Launcher.log.%d{yyyy-MM-dd}.gz"/>
	    String currentLogFile = fileAppender.getFile();
	    String newLogPattern = currentLogFile.replace(LOG4J_ROLLING_FILE_NAME_TOKEN, actionName);
	    fileAppender.setFile(newLogPattern);
	    
	    fileAppender.activateOptions();
	}
	
	/*
	 	Why do I see a warning about "No appenders found for logger" and "Please configure log4j properly"?
	 	
		This occurs when the default configuration files log4j.properties and log4j.xml can not be found and the 
		application performs no explicit configuration. log4j uses Thread.getContextClassLoader().getResource() to 
		locate the default configuration files and does not directly check the file system. Knowing the appropriate 
		location to place log4j.properties or log4j.xml requires understanding the search strategy of the class 
		loader in use. log4j does not provide a default configuration since output to the console or to the file 
		system may be prohibited in some environments
		
		add a classpath entry to the run configuration for the folder that contains your log4j.xml
		file for it to be found and displayed her
		
		There is another workaround for this also. Eclipse by default will include your output folder 
		(usually named bin) in your classpath. Typically anything that are not compilable in src folder 
		will be copied to bin as is. I assumed your property file is not located in src folder. What 
		you can do is to open your project property and add the folder where your property is located 
		into Java Buld Path -> Source (tab). This way eclipse will copy the content of that folder 
		into bin and will be in the classpath.
	*/
	public void findLog4jPropertiesFile(){
	    ClassLoader cl = Thread.currentThread().getContextClassLoader();  
	       
	    while(cl != null)  
	    {  
	       URL loc = cl.getResource("log4j.xml");  
	       if(loc!=null) {
	    	   logger.debug(Log4jServices.class.getName() + ": Search and destroy --> " + loc);  
	       }
	       cl = cl.getParent();  
	    }  
	}
	
	
	
	/*
	 * 
	 * 	NOTE: 	calling this from main will not work for JUnit Annotated test cases..as they dont use main()!!!!!
	 * 			multiple calls to PropertyConfigurator.configure() will result in multiple output writers, and will NOT replace existing ones...
	 * 
	 */
	public void init(String log4JPropertiesPath) throws IOException{
		
		//LoggerServices.build().disableApacheCommonsLogging();
		
		//By default, if no other configuration is given, log4j will search your classpath for a 'log4j.properties' file
		//here we locate the one that will be used if it exists on classpath....
		URL propertyFileURL = Thread.currentThread().getContextClassLoader().getResource("log4j.properties"); 
		
		File log4JPropertiesFile = new File(log4JPropertiesPath);
		
	    if (log4JPropertiesFile.exists()) {
	        PropertyConfigurator.configure(log4JPropertiesPath); 
	        logger.info("loaded log4j.properties : " + log4JPropertiesFile.getCanonicalPath().replaceAll("\\\\", "/"));
	    }else{ 
	    	if(propertyFileURL != null){
	    		String warning = 	"\n\n*******************************************************************************************************\n" + 
	    							"*******************************************************************************************************\n" +		
	    							"*******************************************************************************************************\n" +	
	    							"\n" + 
	    							
									"\tlog4j.properties file not found: " + log4JPropertiesFile + "\n\tloading log4j.properties found on classPath: " + propertyFileURL + "\n" +
	    		
									"\n" + 
									"*******************************************************************************************************\n" + 
									"*******************************************************************************************************\n" +		
									"*******************************************************************************************************\n\n" ;
	    		
	    		logger.warn(warning);
	    	}else{
	    		BasicConfigurator.configure();
	    		logger.error("log4j.properties file  not found: " + log4JPropertiesPath + ", and none found on classpath...loaded BasicConfigurator (System.err output)");
	    	}
	    }
	    logger.info("current working dir: " + new File(".").getCanonicalPath().replaceAll("\\\\", "/"));
	}
	
	
	/*
	 * Set the maximum size that the output file is allowed to reach before
	 * being rolled over to backup files.
	 * 
	 * In configuration files, the MaxFileSize option takes an long integer in
	 * the range 0 - 2^63. You can specify the value with the suffixes "KB",
	 * "MB" or "GB" so that the integer is interpreted being expressed
	 * respectively in kilobytes, megabytes or gigabytes. For example, the value
	 * "10KB" will be interpreted as 10240.
	 * 
	 * 	ALL appenders other than STD_OUT ConsoleAppender created programmatically
	 *	add a stripped down log4j.xml with ConsoleAppender ONLY under shared-resources/src/main/resources
	 *	the rest go in @BeforeClass testng method.
	 *
	 */
	public FileAppender createNewFileAppender(String appenderName, String logFilePath) {
	    FileAppender appender = new FileAppender();
	    appender.setName(appenderName);
	    appender.setLayout(new EnhancedPatternLayout("%d %-5p %C.%M(%F:%L) - %m%n"));
	    appender.setFile(logFilePath);
	    appender.setAppend(false);
	    appender.activateOptions();
	    Logger.getRootLogger().addAppender(appender);
	    return appender;
	}
	
	
	/*
	 * Set the maximum size that the output file is allowed to reach before
	 * being rolled over to backup files.
	 * 
	 * In configuration files, the MaxFileSize option takes an long integer in
	 * the range 0 - 2^63. You can specify the value with the suffixes "KB",
	 * "MB" or "GB" so that the integer is interpreted being expressed
	 * respectively in kilobytes, megabytes or gigabytes. For example, the value
	 * "10KB" will be interpreted as 10240.
	 */
	public RollingFileAppender createNewRollingFileAppender(String appenderName, String logFilePath, String maxFileSize, int maxBackups) {
		RollingFileAppender appender = new RollingFileAppender();
	    appender.setName(appenderName);
	    appender.setLayout(new EnhancedPatternLayout("%d %-5p %C.%M(%F:%L) - %m%n"));
	    appender.setFile(logFilePath);
	    appender.setAppend(true);
	    appender.setMaxFileSize(maxFileSize);
	    appender.setMaxBackupIndex(maxBackups);
	    appender.activateOptions();
	    Logger.getRootLogger().addAppender(appender);
	    return appender;
	}
	
	
	
	
	public void addAppender(String appenderName) {
		Appender appender = LogManager.getRootLogger().getAppender(appenderName);
		LogManager.getRootLogger().addAppender(appender);
	}
	
	public void removeAppender(String appenderName) {
		Appender appender = LogManager.getRootLogger().getAppender(appenderName);
		LogManager.getRootLogger().removeAppender(appender);
	}
	
	public void addConsoleAppender(String appenderName) {
		Logger rootLogger = LogManager.getRootLogger();
		ConsoleAppender consoleAppender = new ConsoleAppender();
		consoleAppender.setWriter(new OutputStreamWriter(System.out));
		consoleAppender.setLayout(new PatternLayout("%d %-5p %C.%M(%F:%L) - %m%n"));
		rootLogger.addAppender(consoleAppender);
	}
	
	
	
	
	public static void main(String[] args) {
		try {
			//listAppenders("root");
			//LOGGER.debug(Log4jServices.build().getLogFilePath("rootLogger", "scrlog"));
			//LOGGER.debug(System.getProperty("user.dir"));
			//Log4jServices.build().getLogFilePaths();
			Log4jServices.build().modifyAppender("output.log", "RollingFileAppender", 2, "100KB");
			for(int i=0; i<1000; i++){
				logger.info("TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
