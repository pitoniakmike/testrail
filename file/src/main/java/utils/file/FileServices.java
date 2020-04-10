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
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class FileServices {
	private static final Logger LOGGER = LoggerFactory.getLogger(new Throwable().getStackTrace()[0].getClassName());
	
	
	public static FileServices build() {
	    return new FileServices();
	}
	
	
	private FileServices() {}
	
	// we don't use the dirCheck list because it lists cwd first.  We prefer
    // searching the resources in src/main and src/test before we search the
    // rest of cwd.
    /** the directories searched by findFileInTree() & findRequiredFileInTree() */
    private List<File> findDirs = new ArrayList<File>();
    {
        /* The user.dir is used in eclipse for the root of the project
    	It's the directory where java was run from, where you started the JVM. 
    	Does not have to be within the user's home directory. 
    	It can be anywhere where the user has permission to run java. 
    	it is also the root of where a fatjar is run from*/
        String cwd = System.getProperty("user.dir");
        findDirs.add(new File(cwd + "/src/main/resources"));
        findDirs.add(new File(cwd + "/src/test/resources"));
        findDirs.add(new File(cwd));

        String automationHome = System.getProperty("AUTOMATION_HOME");
        if (!StringUtils.isBlank(automationHome)) {
            findDirs.add(new File(automationHome));
        }
        
        findDirs.add(new File("/tmp"));
    }
    
    /** The list of patterns findFileInTree() should ignore */
    private List<Pattern> findIgnoreDirs = new ArrayList<Pattern>();
    {
        // Multiple maven builds w/o a clean in between can leave old files in
        // archive-tmp that we don't want to find if we're running the
        // automation from a sandbox.
        findIgnoreDirs.add(Pattern.compile(".*/target/archive-tmp/.*"));
    }
    
    public String[] getFileContentsAsFilteredStrArray(String filePath, String lineSplitRegEx) throws NumberFormatException, IOException{
    	List<String> list = new ArrayList<>();
    	BufferedReader reader = null;
    	String line;

    	try{
	    	reader = new BufferedReader(new FileReader(filePath=normalizePath(filePath)));
	    	while ((line = reader.readLine()) != null) {
	    		if(line.trim().length()>0 && !line.trim().startsWith("#")){
	    			if(lineSplitRegEx!=null){
	    				String[] arr = line.split(lineSplitRegEx);
	    				for(String s : arr){
	    					list.add(s);
	    				}	
	    			}else{
	    				list.add(line);
	    			}
	    		}
	    	}
    	}finally{
    		reader.close();
    	}
    	
    	return list.toArray(new String[0]);
    }
    
    public boolean createFile(String filePath, String text, boolean overWrite)throws IOException{
        createFile(filePath=normalizePath(filePath), overWrite);
        FileOutputStream fileOutputStream = null;
        DataOutputStream dataOutputStream = null;
        fileOutputStream = new FileOutputStream(filePath, true);
        dataOutputStream = new DataOutputStream(fileOutputStream);
        dataOutputStream.writeBytes(text);
        dataOutputStream.flush();
        dataOutputStream.close();
        fileOutputStream.close();
        
        return true;
    }
    
    public boolean createFile(String filePath, boolean overWrite)throws IOException{
       File file = new File(filePath=normalizePath(filePath));
       if(file.exists() && overWrite){
           if(!file.delete()){
                throw new IOException("detete failed: " + filePath);
            }
       }
       return file.createNewFile();
    }
    
    public boolean dirExists(String dirPath)throws IOException{
        boolean bExists = false;
        File file = new File(dirPath=normalizePath(dirPath));
        if(file.exists() && file.isDirectory()){
            bExists = true;
        }
        
        return bExists;
    }

    public synchronized boolean fileExists(String filePath){
        boolean bExists = false;
        File file = new File(filePath=normalizePath(filePath));
        if(file.exists() && file.isFile()){
            bExists = true;
        }
        
        return bExists;
    }

	// creates file if non existent
	// overwrites if append false
    public synchronized boolean write(String filePath, String text, boolean append)
			throws IOException {
		boolean success = false;
		BufferedWriter out = null;

		try {
			LOGGER.debug("writing file: {}", new File(filePath=normalizePath(filePath)).getAbsolutePath());
			out = new BufferedWriter(new FileWriter(filePath, append));
			out.write(text.replaceAll("\r\n", "\n"));
			out.newLine();
			success = true;
		} finally {
			if (out != null) {
				out.close();
			}
		}

		return success;
	}

    /**
     * Search the given directory and all subdirectories for the
     * first file that matches the given filename
     * 
     * @param directory
     * @param fname the name of the file to search for
     * 
     * @return a File associated with the found file or null if the file
     *  can't be found.
     */
    public File findFileInTree(File directory, String fname)
    {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("File '" + directory + "' is not a directory");
        }

		// todo throw illegal and do this before checking that its a directory
        if (!directory.canRead()) {
            return null;
        }
        File[] flist = directory.listFiles();
        if (flist == null) {
            return null;
        }

        OUTER: for (File child : flist) {
            // ignore any files that match any of the ignore patterns
            for (Pattern p : findIgnoreDirs) {
                // move on to the next file if we find any pattern matches
                if (p.matcher(child.getAbsolutePath()).matches()) {
                    // TODO get weird exceptions with this uncommented: LOGGER.trace("skipping {} as it matched ignore pattern {}", child, p);
                    continue OUTER;
                }
            }

            if (child.isFile() && child.getName().equals(fname))
                return child;
            if (child.isDirectory() && directory.canRead()) {
                File retval = findFileInTree(child, fname);
                /* If we find the file in a subdir, return it.  Otherwise move on to the next child. */
                if (retval != null)
                    return retval;
            }
        }

        // if we get this far, we didn't find the file
        return null;
    }

    public String findFileInTree(String fname)
    {
        for (File dir : findDirs) {
            if (!dir.canRead()) {
                continue;
            }
            LOGGER.info("findFileInTree: {}", dir);
            File f = findFileInTree(dir, fname);
            if (f != null){
            	LOGGER.info("found findFileInTree: {}", f.getAbsolutePath());
                return f.getAbsolutePath();
            }
        }

        // if we get this far, we didn't find the file
       return null;
    }
	
	

	
    public boolean mkDirs(String dirPath)throws IOException{
        File file = new File(dirPath=normalizePath(dirPath));
        return file.mkdirs();
    }
	
    public String mkDatedDir(String rootDirPath)throws IOException{
    	String datedDirPath = null;
    	
    	if(!new File(rootDirPath=normalizePath(rootDirPath)).exists()){
        	throw new IOException("dir does not exist: " + rootDirPath);
        }
    	Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SS");
        String datedOutputDir = sdf.format(cal.getTime());
        datedOutputDir = datedOutputDir.replaceAll("/" , "-");
        datedOutputDir = datedOutputDir.replaceAll(" " , "_");
        datedOutputDir = datedOutputDir.replaceAll(":" , "-");
        datedOutputDir = datedOutputDir.replaceAll("\\." , "-");
        if(!new File(rootDirPath + "/" + datedOutputDir).exists()){
	        if(FileServices.build().mkDirs(rootDirPath + "/" + datedOutputDir)){
	            datedDirPath = rootDirPath + "/" + datedOutputDir;
	        }else{
	        	throw new IOException("mkdirs failed: "+ rootDirPath + "/" + datedOutputDir);
	        }
        }

        return datedDirPath;
    }
	
	public String convertToForwardSlash(String backSlasStr) {
		// Strings with backslashes come in like this:
		// "folder\\subfolder"
		// In order to replace "\\" we must use "\\\\"
		return backSlasStr.replaceAll("\\\\", "/");
	}
	
    //Deletes all files and subdirectories under dir.
    //Returns true if all deletions were successful.
    //If a deletion fails, the method stops attempting to delete and returns false.
    public synchronized void deleteDirectory(String dirPath)throws IOException{
    	LOGGER.debug("deleteDir: " + (dirPath=normalizePath(dirPath)));
    	FileUtils.deleteDirectory(new File(dirPath));
    }

	public Collection<File> findFiles(String dirPath, String regEx,
			boolean recurseDir) throws IOException {
		Collection<File> files = FileUtils.listFiles(new File(dirPath=normalizePath(dirPath)),
				new RegexFileFilter(regEx),
				recurseDir == true ? DirectoryFileFilter.DIRECTORY : null);

		return files;
	}

	// "^output.log.*"
	public void deleteFiles(String dirPath, String fileRegEx,
			boolean recurseDir) throws FileNotFoundException {
		Collection<File> files = FileUtils.listFiles(new File(dirPath=normalizePath(dirPath)),
				new RegexFileFilter(fileRegEx),
				recurseDir == true ? DirectoryFileFilter.DIRECTORY : null);

		for (File file : files) {
			deleteFile(file.getAbsolutePath());
		}
	}
	
	public synchronized void deleteFiles(Collection<File> files) throws FileNotFoundException{
    	for(File path : files){
    		LOGGER.debug("deleting file: {}", path);
    		deleteFile(path.getAbsolutePath());
    	}
    }
	public synchronized boolean deleteFile(String filePath) throws FileNotFoundException{
		 return deleteFile(filePath=normalizePath(filePath), true);
	}
	
	public synchronized boolean deleteFile(String filePath, boolean failIfNotExist) throws FileNotFoundException{
        boolean bSuccess = true;
        
        File file = new File(filePath=normalizePath(filePath));
        if(file.exists() && !file.isFile() && failIfNotExist){
        	throw new FileNotFoundException(filePath);
        }

        if(file.exists() && file.isFile()){
        	LOGGER.debug("deleting file: {}", file.getAbsolutePath());
            file.delete();
            bSuccess = !file.exists();
        }
        
        return bSuccess;
    }
	
    public List<String> listFiles(String dirPath, boolean recursive) throws IOException{
        return listFiles(dirPath = normalizePath(dirPath), null, recursive);
    }
	
	//A long value representing the time the file was last modified, measured in milliseconds since the 
    //epoch (00:00:00 GMT, January 1, 1970), or 0L if the file does not exist or if an I/O error occurs 
	public long getLastModifiedTime(String filePath) throws FileNotFoundException{
		File file = new File(filePath=normalizePath(filePath));
		if(!file.exists()){
			throw new FileNotFoundException(filePath);
		}
		return file.lastModified();
	}
	
	public String getLastModifiedTimeAsStr(String filePath) throws FileNotFoundException{
		File file = new File(filePath=normalizePath(filePath));
		if(!file.exists()){
			throw new FileNotFoundException(filePath);
		}
		return new Date(file.lastModified()).toString();
	}
	
	   //If folder is relative path all subDirs returned as relative paths
    //If folder is absolute path all subDirs are returned as absolute paths
    public List<String> getPaths(String dirPath, boolean recursive, boolean includeDirs, boolean relativePaths)throws IOException{
    	return getPaths(dirPath, dirPath, recursive, includeDirs, relativePaths);
    }        
    
    //If folder is relative path all subDirs returned as relative paths
    //If folder is absolute path all subDirs are returned as absolute paths
    private List<String> getPaths(String baseDir, String dirPath, boolean recursive, boolean includeDirs, boolean relativePaths)throws IOException{
        List<String> list = new ArrayList<>();
        baseDir=normalizePath(baseDir);
        dirPath=normalizePath(dirPath);
        
        baseDir = baseDir.endsWith("/") ? baseDir : baseDir + "/";
        
        File file = new File(dirPath);
        
        if(file.exists()){
        	if(file.isDirectory()){
	            //listFiles() returns an array of File objects, one for each file or directory in the directory
		        File[] files = file.listFiles();
		        if(files != null){
			        for(int i=0; i<files.length; i++){
			            if(files[i].isDirectory()){
			            	if(includeDirs){
		            			if(relativePaths){
		            				list.add(files[i].getPath().replaceFirst(baseDir, ""));
		            			}else{
		            				list.add(files[i].getPath());
		            			}
		            		}
			                if(recursive){
			                	list.addAll(getPaths(baseDir, files[i].getPath(), recursive, includeDirs, relativePaths));
			                }
			            }else{
			            	if(relativePaths){
	            				list.add(files[i].getPath().replaceFirst(baseDir, ""));
	            			}else{
	            				list.add(files[i].getPath());
	            			}
			            }
			        }
		        }
        	}else{
        		throw new IOException("not a directory: " + dirPath);
        	}
        }else{
        	throw new FileNotFoundException(dirPath);
        }
        
        return list;
    }

	/*
	 * Allows iteration over the files in a given directory (and optionally its
	 * subdirectories) which match an array of extensions. This method is based
	 * on listFiles(File, String[], boolean).
	 * 
	 * Parameters: directory - the directory to search in extensions - an array
	 * of extensions, ex. {"java","xml"}. If this parameter is null, all files
	 * are returned. recursive - if true all subdirectories are searched as well
	 * Returns: an iterator of java.io.File with the matching files
	 */
	public List<String> listFiles(String dirPath, String[] extensions,
			boolean recursive) throws IOException {
		List<String> fileList = new ArrayList<>();

		Collection<File> collection = FileUtils.listFiles(new File(dirPath=normalizePath(dirPath)),
				extensions, recursive);

		for (Iterator<File> it = collection.iterator(); it.hasNext();) {
			fileList.add(((File) it.next()).getPath());
		}

		return fileList;
	}
	
    public List<String> getAllPathsAsList(String dirPath, boolean includeDirs, boolean recursive)throws IOException{
		return getAllPathsAsList(dirPath, includeDirs, recursive, null);
	}

    //If folder is relative path all subDirs returned as relative paths
    //If folder is absolute path all subDirs are returned as absolute paths
    public List<String> getAllPathsAsList(String dirPath, boolean includeDirs, boolean recursive, JFileFilter jFileFilter) throws IOException{
        ArrayList<String> list = new ArrayList<>();
        File[] files = null;

        File file = new File(dirPath=normalizePath(dirPath));
        if(file.exists()){
	        if(file.isDirectory()){
	        	if(jFileFilter != null){
	        		files = file.listFiles(jFileFilter);
	        	}else{
	        		files = file.listFiles();
	        	}
		        if(files != null){
			        for(int i=0; i<files.length; i++){
			            if(files[i].isFile()){
			            	list.add(files[i].getPath().replaceAll("\\\\",  "/"));
			            }else if(files[i].isDirectory() && recursive){
			            	if(includeDirs){
				        		list.add(file.getPath());
				        	}
			            	list.addAll(getAllPathsAsList(files[i].getPath(), includeDirs, recursive, jFileFilter));
			            }
			        }
		        }
	        }else{
	        	throw new IOException("not dir: " + dirPath);
	        }
        }else{
        	throw new FileNotFoundException(dirPath);
        }
        
        return list;
    }
    
	public String getFileContents(String filePath)throws IOException{
    	String fileContentStr =  null;
    	
		File inFile = new File(filePath=normalizePath(filePath));
		FileInputStream fis = new FileInputStream(inFile);
		FileChannel inChannel = fis.getChannel();
		ByteBuffer buf = ByteBuffer.allocate((int)inChannel.size());
		inChannel.read(buf);
		fileContentStr = new String(buf.array());
		inChannel.close();
		fis.close();
		
		return fileContentStr;
    }
	
	public String getFileNameNoExtension(String filePath){
		return FilenameUtils.getBaseName(filePath);
    }
	
	//removes subdirs
    public boolean cleanDirectory(String dirPath)throws IOException {
    	return cleanDirectory(new File(dirPath=normalizePath(dirPath)));
    }
	
	public boolean cleanDirectory(File directory)throws IOException {
    	if(!directory.exists() | !directory.isDirectory()){
    		throw new IOException("directory not found: " + directory.getCanonicalPath());
    	}
    	FileUtils.cleanDirectory(directory);
    	
    	return directory.listFiles().length==0;
    }
	
	public byte[] getBytes(String filePath) throws IOException {
        File file = new File(filePath=normalizePath(filePath));
        
        InputStream is = new FileInputStream(file);
        byte[] bytes = IOUtils.toByteArray(is);

        LOGGER.debug("Byte array size: " + bytes.length);
        
        return bytes;
    }
	
	public String normalizePath(String path){
        String normalizedPath = path;
        
        if(path.startsWith("~")){
            normalizedPath = System.getProperty("user.home");
            if(path.length()>1){
                normalizedPath+=path.substring(1);
            }
        }else if(!path.equals(".")){
            normalizedPath = FilenameUtils.normalizeNoEndSeparator(path);
        }
        return normalizedPath;
    }
	
	public List<String> matchingLines(String fileName, String regEx, boolean caseInSensitve, boolean dotAll, boolean multiLine) throws IOException {
	    return matchingLines(fileName, regEx, caseInSensitve, dotAll, multiLine, StandardCharsets.UTF_8);
	}
	
	public List<String> matchingLines(String fileName, String regEx, boolean caseInSensitve, boolean dotAll, boolean multiLine, Charset charset) throws IOException {
	    List<String> matchList = new ArrayList<>();
	    List<String> lineList = null;
	    String line = null;

	    Path path = Paths.get(fileName);
	    lineList = Files.readAllLines(path, charset);
	    ListIterator<String> lineIterator = lineList.listIterator();
	    while(lineIterator.hasNext()) {
	        if(matches(lineIterator.next(), regEx, caseInSensitve, dotAll, multiLine)) {
	            matchList.add(line);
	        }
	    }      
	    return matchList;
	}
	
	//Returns boolean indicating if regex pattern match exists in input String
    private boolean matches(String input, String regEx, boolean caseSensitve, boolean dotAll, boolean multiLine) {
    	Pattern pattern = getPattern(regEx, caseSensitve, dotAll, multiLine);
    	Matcher matcher = pattern.matcher(input);
    	
    	return matcher.find();
    }
    
	private Pattern getPattern(String regEx, boolean caseSensitve, boolean dotAll, boolean multiLine){
		int options = 0;
        
        /*Enables case-insensitive matching. 
         By default, a pattern is case-sensitive. By adding a flag, a pattern can be made case-insensitive.
		 It is also possible to control case sensitivity within a pattern using the inline modifier (?i). 
		 The inline modifier affects all characters to the right and in the same enclosing group, if any. 
		 For example, in the pattern a(b(?i)c)d, only c is allowed to be case-insensitive. You can also force case 
		 sensitivity with (?-i).

	     The inline modifier can also contain pattern characters using the form (?i:abc). In this case, only those 
	     pattern characters inside the inline modifier's enclosing group are affected. This form does not capture text 
    	 By default, case-insensitive matching assumes that only characters in the US-ASCII 
    	 charset are being matched. Unicode-aware case-insensitive matching can be enabled by 
    	 specifying the UNICODE_CASE flag in conjunction with this flag. 
    	 Case-insensitive matching can also be enabled via the embedded flag expression (?i). 
    	 Specifying this flag may impose a slight performance penalty.*/
        if(!caseSensitve){
        	// Omitting UNICODE_CASE causes only US ASCII characters to be matched case insensitively
            // This is appropriate if you know beforehand that the subject string will only contain
            // US ASCII characters as it speeds up the pattern matching.
            options |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
    	}
        if(dotAll){
        	// By default, the dot will not match line break characters.
            // Specify this option to make the dot match all characters, including line breaks
            options |= Pattern.DOTALL;
    	}
        if(multiLine){
        	// By default, the caret ^, dollar $ as well as \A, \z and \Z
            // only match at the start and the end of the string
            // Specify this option to make ^ and \A also match after line breaks in the string,
            // and make $, \z and \Z match before line breaks in the string
            options |= Pattern.MULTILINE;
        }
        LOGGER.trace("regex: {}", regEx);

		return Pattern.compile(regEx, options);
	}
    


}
