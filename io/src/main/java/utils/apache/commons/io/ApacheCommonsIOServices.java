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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ApacheCommonsIOServices implements Serializable, Cloneable{
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(new Throwable().getStackTrace()[0].getClassName());    
	
	
	public static ApacheCommonsIOServices build() {
	    return new ApacheCommonsIOServices();
	}
	
	private ApacheCommonsIOServices() {}
	

	// we don't use the dirCheck list because it lists cwd first.  We prefer
    // searching the resources in src/main and src/test before we search the
    // rest of cwd.
    /** the directories searched by findFileInTree() & findRequiredFileInTree() */
    private static List<File> findDirs = new ArrayList<File>();
    static {
        /* The user.dir is used in eclipse for the root of the project
        It's the directory where java was run from, where you started the JVM. 
        Does not have to be within the user's home directory. 
        It can be anywhere where the user has permission to run java. 
        it is also the root of where a fatjar is run from*/
        String cwd = System.getProperty("user.dir");
        Path path = Paths.get(cwd, "src", "main", "resources");
        findDirs.add(new File(path.toString()));
        path = Paths.get(cwd, "src", "test", "resources");
        findDirs.add(new File(path.toString()));
        findDirs.add(new File(cwd));
        if(File.separator.equals("/")) {
        	findDirs.add(new File("/tmp"));
        }

        String automationHome = System.getProperty("AUTOMATION_HOME");
        if (!StringUtils.isBlank(automationHome)) {
            findDirs.add(new File(automationHome));
        }
    }
    
    /*
     	Compares two Strings, and returns the portion where they differ. More precisely, return the remainder 
     	of the second String, starting from where it's different from the first. This means that the difference 
     	between "abc" and "ab" is the empty String and not "c".
     */
    public String difference(String str1, String str2) {
    	return StringUtils.difference(str1, str2);
    }
    
    public static void closeQuietly(final Closeable closeable){
        try{
            if(closeable != null){
                closeable.close();
            }
        }catch (final IOException ioe){
            // ignore
        }
    }
    
    /** The list of patterns findFileInTree() should ignore */
    private List<Pattern> findIgnoreDirs = new ArrayList<Pattern>();{
        // Multiple maven builds w/o a clean in between can leave old files in
        // archive-tmp that we don't want to find if we're running the
        // automation from a sandbox.
        findIgnoreDirs.add(Pattern.compile(".*/target/archive-tmp/.*"));
    }
    
    public void addPathTofindDirsList(String dirPath) throws FileNotFoundException {
    	File file= new File(dirPath);
    	if(!file.exists()) {
    		throw new FileNotFoundException(dirPath);
    	}
    	findDirs.add(new File(dirPath));
    }
    
    public List<File> getFindDirs() {
    	return findDirs;
    }
    
    public HashMap<String, String> fileToHashMap(String filePath, String separatorChar) throws Exception {
    	filePath = findFilePathInDirList(filePath, false);
    	HashMap<String, String> hashMap = new HashMap<>();
    	List<String> lines = readLines(filePath);
    	
	    for(String line: lines) {
	    	if(line.contains(separatorChar)) {
	    		String[] data = line.split(separatorChar);
	    		if(data.length!=2) {
	    			throw new IllegalArgumentException();
	    		}
	    		hashMap.put(data[0], data[1].trim());
	    	}
	    }
    	return hashMap;
    }
    
    
	public HashMap<String, String> propertyFileToHashMap(String propertyFilePath) throws IOException {
		System.out.println("In readPropertyFile method");
		Properties prop = new Properties();
		InputStream input;
		HashMap<String, String> propvalsHashMap = new HashMap<>();
		input = new FileInputStream(propertyFilePath);
		prop.load(input);
		LOGGER.debug("Property File Loaded Succesfully");
		Set<String> propertyNames = prop.stringPropertyNames();
		for (String Property : propertyNames) {
			LOGGER.debug(Property + ":" + prop.getProperty(Property));
			propvalsHashMap.put(Property, prop.getProperty(Property));
		}
		LOGGER.debug("HashMap generated::" + propvalsHashMap);
		
		return propvalsHashMap;
	}
    
    /**
     * Search for partial path filePath in findDirs List
     * 
     * @param findFilePath
     * @param partial or complete path to search for
     * 
     * @return a File associated with the found file or null if the file
     *  can't be found.
     * @throws IOException 
     */
    public String findFilePathInDirList(String findFilePath, boolean silent) throws IOException{
    	List<String> list = new ArrayList<>();
    	if(findFilePath==null || findFilePath.equals("")) {
    		return null;
    	}
    	Path p = Paths.get(findFilePath); 
		if (p.isAbsolute() ) {
			return findFilePath;
		}
    	
    	if((File.separator.equals("\\") && findFilePath.contains("/")) | (File.separator.equals("/") && findFilePath.contains("\\"))){
    		new IOException("filePath: " + findFilePath  + " contains improper separator chars for OS");
    	}
    	
    	for (File dir : findDirs) {
            if (dir.exists() && !dir.canRead()) {
            	LOGGER.info("cannot read dir: {}", dir);
                continue;
            }
            String newPath = FilenameUtils.concat(dir.getAbsolutePath(), findFilePath);
            File f = new File(newPath);
            if (f.exists()) {
            	LOGGER.trace("found:     {}", newPath);
            	list.add(f.getAbsolutePath());
            }else {
            	LOGGER.trace("not found: {}", newPath);
            }
        }
    	if(list.size()==0) {
	    	if(silent) {
	    		return null;
	    	}else {
	    		throw new FileNotFoundException(findFilePath + "\nin dirs:\n" + listToString(findDirs));
	    	}
        }else if(list.size()>1) { 
        	throw new IOException("file not unique: " + findFilePath + "\n" + listToString(list));
        }else {
        	return list.get(0);
        }
    }
    
    private String listToString(List<?> list){
  	  	StringBuilder sb = new StringBuilder();
    	Iterator<?> iterator = list.iterator();
  	  	while ( iterator.hasNext() ){
  	  		sb.append(iterator.next() + "\n");
  	  	}
  	  	
  	  	return sb.toString().trim();
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
    public File findFileInTree(File directory, String fname){
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

   
	
	/*
	 	Cleans a directory without deleting it.

		Parameters:
		directory - directory to clean 
		Throws:
		IOException - in case cleaning is unsuccessful
	 */
	public void cleanDirectory(String dirPath)throws IOException{
		FileUtils.cleanDirectory(new File(normalizePath(dirPath)));
	}
	
	/*
		Compares the contents of two files to determine if they are equal or not.
		This method checks to see if the two files are different lengths or if they point to the same file, before resorting to byte-by-byte comparison of the contents.
		Code origin: Avalon
	
		Parameters:
			file1 - the first file
			file2 - the second file 
		Returns:
			true if the content of the files are equal or they both don't exist, false otherwise 
		Throws:
			IOException - in case of an I/O error
	 */
	public boolean contentEquals(String file1, String file2) throws IOException{
		return FileUtils.contentEquals(new File(normalizePath(file1)), new File(normalizePath(file2)));
	}
	
	/*
		Compares the contents of two files to determine if they are equal or not.
		This method checks to see if the two files are different lengths or if they point to the same file, before resorting to byte-by-byte comparison of the contents.
		Code origin: Avalon
	
		Parameters:
			file1 - the first file
			file2 - the second file 
		Returns:
			true if the content of the files are equal or they both don't exist, false otherwise 
		Throws:
			IOException - in case of an I/O error
	 */
	public boolean contentEquals(File file1, File file2) throws IOException{
		return FileUtils.contentEquals(file1, file2);
	}
	
	/*
	 	Copies a whole directory to a new location preserving the file dates.

		This method copies the specified directory and all its child directories and files to the specified destination. 
		The destination is the new location and name of the directory.
		
		The destination directory is created if it does not exist. If the destination directory did exist, then this 
		method merges the source with the destination, with the source taking precedence.
		
		Note: This method tries to preserve the files' last modified date/times using File.setLastModified(long), 
		however it is not guaranteed that those operations will succeed. If the modification operation fails, 
		no indication is provided.
		
		Parameters:
		srcDir an existing directory to copy, must not be null
		destDir the new directory, must not be null
		Throws:
		NullPointerException - if source or destination is null
		IOException - if source or destination is invalid
		IOException - if an IO error occurs during copying
		
		ex: copyDirectory("src/test/resources/ssh", "/home/mike/projects/automationTest/src/test/resources/ssh");
		
	 */
	public void copyDirectory(String srcDirPath, String destDirPath) throws IOException {
		LOGGER.debug("copying: {} to: {}", srcDirPath, destDirPath);
		 FileUtils.copyDirectory(new File(normalizePath(srcDirPath)), new File(normalizePath(destDirPath)));
	}
	
	/*
		Copies a whole directory to a new location.
	
		This method copies the contents of the specified source directory to within the specified 
		destination directory.
	
		The destination directory is created if it does not exist. If the destination directory did exist, 
		then this method merges the source with the destination, with the source taking precedence.
	
	    Parameters:
	        srcDirPath - an existing directory to copy, must not be null
	        destDirPath - the new directory, must not be null
	        preserveFileDate - true if the file date of the copy should be the same as the original 
	        
	    Throws:
	        NullPointerException - if source or destination is null 
	        IOException - if source or destination is invalid 
	        IOException - if an IO error occurs during copying
	 */
	public void copyDirectory(String srcDirPath, String destDirPath, boolean preserveFileDate) throws IOException {
		 FileUtils.copyDirectory(new File(normalizePath(srcDirPath)), new File(normalizePath(destDirPath)), preserveFileDate);
	}
	
	
	/*
	 	Copies a directory to within another directory preserving the file dates.

	    This method copies the source directory and all its contents to a directory of the same name in the specified destination directory.

	    The destination directory is created if it does not exist. If the destination directory did exist, then this method merges the source with the destination, with the source taking precedence.

	    Parameters:
	        srcDir - an existing directory to copy, must not be null
	        destDir - the directory to place the copy in, must not be null 
	    Throws:
	        NullPointerException - if source or destination is null 
	        IOException - if source or destination is invalid 
	        IOException - if an IO error occurs during copying
	 */
	public void copyDirectoryToDirectory(String srcDirPath, String destDirPath) throws IOException {
		FileUtils.copyDirectoryToDirectory(new File(normalizePath(srcDirPath)), new File(normalizePath(destDirPath)));
	}
	
	
	/*
	   	Copies a filtered directory to a new location preserving the file dates.

	    This method copies the contents of the specified source directory to within the specified destination directory.
	
	    The destination directory is created if it does not exist. If the destination directory did exist, then this method merges the source with the destination, with the source taking precedence.
	    Example: Copy directories only
	
	      // only copy the directory structure
	      FileUtils.copyDirectory(srcDir, destDir, DirectoryFileFilter.DIRECTORY);
	      
	
	    Example: Copy directories and txt files
	
	      // Create a filter for ".txt" files
	      IOFileFilter txtSuffixFilter = FileFilterUtils.suffixFileFilter(".txt");
	      IOFileFilter txtFiles = FileFilterUtils.andFileFilter(FileFileFilter.FILE, txtSuffixFilter);
	
	      // Create a filter for either directories or ".txt" files
	      FileFilter filter = FileFilterUtils.orFileFilter(DirectoryFileFilter.DIRECTORY, txtFiles);
	
	      // Copy using the filter
	      FileUtils.copyDirectory(srcDir, destDir, filter);
	      
	
	    Parameters:
	        srcDir - an existing directory to copy, must not be null
	        destDir - the new directory, must not be null
	        filter - the filter to apply, null means copy all directories and files should be the same as the original 
	    Throws:
	        NullPointerException - if source or destination is null 
	        IOException - if source or destination is invalid 
	        IOException - if an IO error occurs during copying
	 */
	public void copyDirectory(File srcDir, File destDir, FileFilter filter) throws IOException{
		FileUtils.copyDirectory(srcDir, destDir, filter);
	}
	
	/*
	 	Copies a file to a new location.
	
		This method copies the contents of the specified source file to the specified destination file. The directory holding the destination file is created if it does not exist. If the destination file exists, then this method will overwrite it.
		
		Parameters:
			srcFile - an existing file to copy, must not be null
			destFile - the new file, must not be null
		Throws:
			NullPointerException - if source or destination is null 
			IOException - if source or destination is invalid 
			IOException - if an IO error occurs during copying
	 */
	public void copyFile(String srcFile, String destFile) throws IOException{
		copyFile(srcFile, destFile, true);
	}
	
	/*
	 	Copies a file to a new location.

		This method copies the contents of the specified source file to the specified destination file. The directory holding the destination file is created if it does not exist. If the destination file exists, then this method will overwrite it.
		
		Parameters:
			srcFile - an existing file to copy, must not be null
			destFile - the new file, must not be null
			preserveFileDate - true if the file date of the copy should be the same as the original 
		Throws:
			NullPointerException - if source or destination is null 
			IOException - if source or destination is invalid 
			IOException - if an IO error occurs during copying
	 */
	public void copyFile(String srcFile, String destFile, boolean preserveFileDate) throws IOException{
		FileUtils.copyFile(new File(normalizePath(srcFile)), new File(normalizePath(destFile)), preserveFileDate);
	}
	
	/*
		Copies a file to a directory optionally preserving the file date.
	
		This method copies the contents of the specified source file to a file of the same name in the specified destination directory. The destination directory is created if it does not exist. If the destination file exists, then this method will overwrite it.
	
		Parameters:
			srcFile - an existing file to copy, must not be null
			destDir - the directory to place the copy in, must not be null
		Throws:
			NullPointerException - if source or destination is null 
			IOException - if source or destination is invalid 
			IOException - if an IO error occurs during copying
	 */
	public void copyFileToDirectory(String srcFile, String destDir) throws IOException{
		copyFileToDirectory(srcFile, destDir, false);
	}
     
	/*
		Copies a file to a directory optionally preserving the file date.
	
		This method copies the contents of the specified source file to a file of the same name in the specified destination directory. The destination directory is created if it does not exist. If the destination file exists, then this method will overwrite it.
	
		Parameters:
			srcFile - an existing file to copy, must not be null
			destDir - the directory to place the copy in, must not be null
			preserveFileDate - true if the file date of the copy should be the same as the original 
		Throws:
			NullPointerException - if source or destination is null 
			IOException - if source or destination is invalid 
			IOException - if an IO error occurs during copying
	 */
	public void copyFileToDirectory(String srcFile, String destDir, boolean preserveFileDate) throws IOException{
		LOGGER.debug("copying: {} to: {}", srcFile, destDir);
		FileUtils.copyFileToDirectory(new File(normalizePath(srcFile)), new File(normalizePath(destDir)), preserveFileDate);
	}
	
	/*
	 	Deletes a file. If file is a directory, delete it and all sub-directories.
		The difference between File.delete() and this method are:
 		A directory to be deleted does not have to be empty.
 		You get exceptions when a file or directory cannot be deleted. (java.io.File methods returns a boolean)
 		
 		Parameters:
		file - file or directory to delete, must not be null 
		Throws:
		NullPointerException - if the directory is null 
		FileNotFoundException - if the file was not found 
		IOException - in case deletion is unsuccessful
	 */
	public void forceDelete(String path) throws IOException{
		FileUtils.forceDelete(new File(normalizePath(path)));
	}
	
	/*
		Returns the free space on a drive or volume in kilobytes by invoking the command line.

		FileSystemUtils.freeSpaceKb("C:");       // Windows
		FileSystemUtils.freeSpaceKb("/volume");  // *nix

		The free space is calculated via the command line. It uses 'dir /-c' on Windows, 'df -kP' 
		on AIX/HP-UX and 'df -k' on other Unix.

		In order to work, you must be running Windows, or have a implementation of Unix df that 
		supports GNU format when passed -k (or -kP). If you are going to rely on this code, 
		please check that it works on your OS by running some simple tests to compare the command 
		line with the output from this class. If your operating system isn't supported, please 
		raise a JIRA call detailing the exact result from df -k and as much other detail as possible, 
		thanks.

		Parameters:
			path - the path to get free space for, not null, not empty on Unix 
		Returns:
			the amount of free drive space on the drive or volume in kilobytes 
		Throws:
			IllegalArgumentException - if the path is invalid 
			IllegalStateException - if an error occurred in initialisation 
			IOException - if an error occurs when finding the free space
	 */
	public long freeSpaceKb(String path) throws IOException{
		return Files.getFileStore(Paths.get(path)).getUsableSpace();
	}
	
	/*
		Gets the path from a full filename, which excludes the prefix.
	
	    This method will handle a file in either Unix or Windows format. The method is entirely text based, and returns the text before and including the last forward or backslash.
	
	     C:\a\b\c.txt --> a\b\
	     ~/a/b/c.txt  --> a/b/
	     a.txt        --> ""
	     a/b/c        --> a/b/
	     a/b/c/       --> a/b/c/
	     
	
	    The output will be the same irrespective of the machine that the code is running on.
	
	    This method drops the prefix from the result. See getFullPath(String) for the method that retains the prefix.
	
	    Parameters:
	        filename - the filename to query, null returns null 
	    Returns:
	        the path of the file, an empty string if none exists, null if invalid
	 */
	public String getPath(String filename){
		return FilenameUtils.getPath(filename);
	}

	/*
	  	Gets the path from a full filename, which excludes the prefix, and also excluding the final directory separator.

	    This method will handle a file in either Unix or Windows format. The method is entirely text based, and returns the text before the last forward or backslash.
	
	     C:\a\b\c.txt --> a\b
	     ~/a/b/c.txt  --> a/b
	     a.txt        --> ""
	     a/b/c        --> a/b
	     a/b/c/       --> a/b/c
	     
	
	    The output will be the same irrespective of the machine that the code is running on.
	
	    This method drops the prefix from the result. See getFullPathNoEndSeparator(String) for the method that retains the prefix.
	
	    Parameters:
	        filename - the filename to query, null returns null 
	    Returns:
	        the path of the file, an empty string if none exists, null if invalid
	 */
	public String getPathNoEndSeparator(String filename){
		return FilenameUtils.getPathNoEndSeparator(filename);
	}

	/*
	 	Gets the full path from a full filename, which is the prefix + path.

	    This method will handle a file in either Unix or Windows format. The method is entirely text based, and returns the text before and including the last forward or backslash.
	
	     C:\a\b\c.txt --> C:\a\b\
	     ~/a/b/c.txt  --> ~/a/b/
	     a.txt        --> ""
	     a/b/c        --> a/b/
	     a/b/c/       --> a/b/c/
	     C:           --> C:
	     C:\          --> C:\
	     ~            --> ~/
	     ~/           --> ~/
	     ~user        --> ~user/
	     ~user/       --> ~user/
	     
	
	    The output will be the same irrespective of the machine that the code is running on.
	
	    Parameters:
	        filename - the filename to query, null returns null 
	    Returns:
	        the path of the file, an empty string if none exists, null if invalid
	 */
	public String getFullPath(String filename){
		return FilenameUtils.getFullPath(normalizePath(filename));
	}

	/*
	 	Gets the full path from a full filename, which is the prefix + path, and also excluding the final directory separator.

	    This method will handle a file in either Unix or Windows format. The method is entirely text based, and returns the text before the last forward or backslash.
	
	     C:\a\b\c.txt --> C:\a\b
	     ~/a/b/c.txt  --> ~/a/b
	     a.txt        --> ""
	     a/b/c        --> a/b
	     a/b/c/       --> a/b/c
	     C:           --> C:
	     C:\          --> C:\
	     ~            --> ~
	     ~/           --> ~
	     ~user        --> ~user
	     ~user/       --> ~user
	     
	
	    The output will be the same irrespective of the machine that the code is running on.
	
	    Parameters:
	        filename - the filename to query, null returns null 
	    Returns:
	        the path of the file, an empty string if none exists, null if invalid
	 */
	public String getFullPathNoEndSeparator(String filename){
		return FilenameUtils.getFullPathNoEndSeparator(filename);
	}

	/*
	 	Gets the name minus the path from a full filename.

	    This method will handle a file in either Unix or Windows format. The text after the last forward or backslash is returned.
	
	     a/b/c.txt --> c.txt
	     a.txt     --> a.txt
	     a/b/c     --> c
	     a/b/c/    --> ""
	     
	
	    The output will be the same irrespective of the machine that the code is running on.
	
	    Parameters:
	        filename - the filename to query, null returns null 
	    Returns:
	        the name of the file without the path, or an empty string if none exists

	 */
	public String getName(String filePath){
		return FilenameUtils.getName(filePath);
	}
	

	/*
	 	Gets the base name, minus the full path and extension, from a full filename.

	    This method will handle a file in either Unix or Windows format. The text after the last forward or backslash and before the last dot is returned.

	     ~/a/b/c.txt --> c
	     a/b/c.txt --> c
	     a.txt     --> a
	     a/b/c     --> c
	     a/b/c/    --> ""
	     

	    The output will be the same irrespective of the machine that the code is running on.

	    Parameters:
	        filename - the filename to query, null returns null 
	    Returns:
	        the name of the file without the path, or an empty string if none exists
	 */
	public String getBaseName(String filename){
		return FilenameUtils.getBaseName(filename);
	}
	
	/*
	 	Gets the extension of a filename.

	    This method returns the textual part of the filename after the last dot. There must be no directory separator after the dot.

	     foo.txt      --> "txt"
	     a/b/c.jpg    --> "jpg"
	     a/b.txt/c    --> ""
	     a/b/c        --> ""
	     

	    The output will be the same irrespective of the machine that the code is running on.

	    Parameters:
	        filename - the filename to retrieve the extension of. 
	    Returns:
	        the extension of the file or an empty string if none exists.
	 */
	public String getExtension(String filename){
		return FilenameUtils.getExtension(filename);
	}
	
	public String getFileNameNoExtension(String fileNameWithExt){
		return FilenameUtils.removeExtension(fileNameWithExt);
	}
	
	
	public String getFileNameWithExtension(String filePath){
		Path path = Paths.get(filePath);
		return path.getFileName().toString();
	}

	/*
		Makes a directory, including any necessary but nonexistent parent directories. 
		If there already exists a file with specified name or the directory cannot be 
		created then an exception is thrown.
	
		Parameters:
			directory - directory to create, must not be null 
		Throws:
			NullPointerException - if the directory is null 
			IOException - if the directory cannot be created
	 */
	public void forceMkdir(String dirPath) throws IOException{
		FileUtils.forceMkdir(new File(normalizePath(dirPath)));
	}

	/*
	 	Tests if the specified File is newer than the reference File.

		Parameters:
			file - the File of which the modification date must be compared, must not be null
			reference - the File of which the modification date is used, must not be null 
		Returns:
			true if the File exists and has been modified more recently than the reference File 
		Throws:
			IllegalArgumentException - if the file is null 
			IllegalArgumentException - if the reference file is null or doesn't exist
	 */
	public boolean isFileNewer(String file, String reference){
		return FileUtils.isFileNewer(new File(normalizePath(file)), new File(normalizePath(reference)));
	}
	
	/*
	 	Tests if the specified File is newer than the specified Date.

		Parameters:
			file - the File of which the modification date must be compared, must not be null
			date - the date reference, must not be null 
		Returns:
			true if the File exists and has been modified after the given Date. 
		Throws:
			IllegalArgumentException - if the file is null 
			IllegalArgumentException - if the date is null
	 */
	public boolean isFileNewer(String file, Date date){
		return FileUtils.isFileNewer(new File(normalizePath(file)), date);
	}
	
	/*
	 	Tests if the specified File is older than the reference File.

		Parameters:
			file - the File of which the modification date must be compared, must not be null
			reference - the File of which the modification date is used, must not be null 
		Returns:
			true if the File exists and has been modified before the reference File 
		Throws:
			IllegalArgumentException - if the file is null 
			IllegalArgumentException - if the reference file is null or doesn't exist
	 */
	public boolean isFileOlder(String file, String reference){
		return FileUtils.isFileOlder(new File(normalizePath(file)), new File(normalizePath(reference)));
	}
	
	/*
	 	Tests if the specified File is older than the specified Date.

		Parameters:
			file - the File of which the modification date must be compared, must not be null
			date - the date reference, must not be null 
		Returns:
			true if the File exists and has been modified before the given Date. 
		Throws:
			IllegalArgumentException - if the file is null 
			IllegalArgumentException - if the date is null
	 */
	public boolean isFileOlder(String file, Date date){
		return FileUtils.isFileOlder(new File(normalizePath(file)), date);
	}
	
	public boolean isURL(String url) {
	    try {
	        new URL(url);
	        return true;
	    } catch (Exception e) {
	        return false;
	    }
	}
	
	public URL getURl(String path) throws MalformedURLException, FileNotFoundException {
		URL url = null;
		try {
			url = new URL(path);
		}catch(MalformedURLException e) {
			File file = new File(path);
			if(!file.exists()) {
				throw new FileNotFoundException(path);
			}
			url = file.toURI().toURL();
		}
		return url;
	}

	
	//TrueFileFilter.INSTANCE a file filter that always returns true.
    public List<File> listFilesAndDirs(String dirPath, boolean recurseDir, boolean includeSearchPath) throws IOException {
        List<File> files = (List<File>) FileUtils.listFilesAndDirs(new File(dirPath=normalizePath(dirPath)), TrueFileFilter.INSTANCE,  recurseDir == true ? TrueFileFilter.INSTANCE : null);
        if(!includeSearchPath){
            files.remove(files.indexOf(new File(dirPath)));
        }
        return files;
    }
    
    public List<File> listDirs(String dirPath, boolean includeSearchPath) throws IOException {
        List<File> files = (List<File>) FileUtils.listFilesAndDirs(new File(normalizePath(dirPath)), DirectoryFileFilter.DIRECTORY, DirectoryFileFilter.DIRECTORY);
        if(!includeSearchPath){
            files.remove(files.indexOf(new File(dirPath)));
        }
        return files;
    }
    
    public List<File> sortFilesBySize(String dirPath, boolean recurseDir) throws IOException {
    	List<File> files = listFiles(dirPath, recurseDir);
    	File[] fileArray = files.toArray(new File[files.size()]);
    	Arrays.sort(fileArray, new FileSizeComparator(true));
    	return Arrays.asList(fileArray);
    }
    

	//TrueFileFilter.INSTANCE a file filter that always returns true.
    public List<File> listFiles(String dirPath, boolean recurseDir) throws IOException {
        List<File> files = (List<File>) FileUtils.listFiles(new File(normalizePath(dirPath)), TrueFileFilter.INSTANCE,  recurseDir == true ? DirectoryFileFilter.DIRECTORY : null);
        return files;
    }
	
	/*
		Finds files within a given directory (and optionally its subdirectories). All files found are filtered by an IOFileFilter.
		If your search should recurse into subdirectories you can pass in an IOFileFilter for directories. 
		You don't need to bind a DirectoryFileFilter (via logical AND) to this filter. This method does that for you.
	
		An example: If you want to search through all directories called "temp" you pass in FileFilterUtils.NameFileFilter("temp")
	
		Another common usage of this method is find files in a directory tree but ignoring the directories generated CVS. 
		You can simply pass in FileFilterUtils.makeCVSAware(null).
	
		Parameters:
			directory - the directory to search in
			fileFilter - filter to apply when finding files.
			dirFilter - optional filter to apply when finding subdirectories. If this parameter is null, subdirectories 
			will not be included in the search. Use TrueFileFilter.INSTANCE to match all directories. 
		Returns:
			an collection of java.io.File with the matching files
	 */
	public Collection<File> listFiles(String directory, IOFileFilter fileFilter, IOFileFilter dirFilter){
		return FileUtils.listFiles(new File(normalizePath(directory)), fileFilter, dirFilter);
	}
	
	//an collection of java.io.File with the matching files
    public ArrayList<File> listFilesWildCard(String path, String wildCardPattern, boolean recurse){
        return new ArrayList<File>(FileUtils.listFiles(new File(normalizePath(path)), new WildcardFileFilter(wildCardPattern), recurse ? TrueFileFilter.INSTANCE : null));
    }
    
    /*
    	The listFiles(File directory, IOFileFilter fileFilter, IOFileFilter dirFilter) method of the FileUtils 
    	class of the ApacheSW Commons IOS library returns a Collection of files in a specified directory passed 
    	in as its first parameter. If the third parameter (dirFilter) is null, only the files in the specified 
    	directory are returned. If TrueFileFilter.INSTANCE is passed in, all of the files within the specified 
    	directory are returned, including all subdirectories. This is illustrated by GetAllFilesInDirectory.
	*/
    public List<File> listFiles(String dirPath) throws IOException {
    	List<File> fileList = new ArrayList<File>();
    	File dir = new File(dirPath);

		LOGGER.info("Getting all files in " + dir.getPath() + " including those in subdirectories");
		List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		for (File file : files) {
			LOGGER.info("file: " + file.getPath());
			fileList.add(file);
			
		}
		
		return fileList;
    }
	
	//DirectoryFileFilter.DIRECTORY a filter accepts Files that are directories. 
    public List<File> listFilesRegEx(String dirPath, String regEx, boolean recurseDir) throws IOException {
        List<File> files = (List<File>)FileUtils.listFiles(new File(normalizePath(dirPath)),
               new RegexFileFilter(regEx),
               recurseDir == true ? DirectoryFileFilter.DIRECTORY : null);

       return files;
    }

	/*
		Finds files within a given directory (and optionally its subdirectories) which match an array of extensions.
	
		Parameters:
			directory - the directory to search in
			extensions - an array of extensions, ex. {"java","xml"}. If this parameter is null, all files are returned.
			recursive - if true all subdirectories are searched as well 
		Returns:
			an collection of java.io.File with the matching files
	 */
	public List<File> listFiles(String directory, String[] extensions, boolean recursive){
		return (List<File>)FileUtils.listFiles(new File(normalizePath(directory)), extensions, recursive);
	}
	
	public List<File> listFiles(String directory, boolean recursive, String... extensions){
		return (List<File>)FileUtils.listFiles(new File(normalizePath(directory)), extensions, recursive);
	}

	/*
	 	Moves a directory.

		When the destination directory is on another file system, do a "copy and delete".

		Parameters:
			srcDir - the directory to be moved
			destDir - the destination directory 
		Throws:
			NullPointerException - if source or destination is null 
			IOException - if source or destination is invalid 
			IOException - if an IO error occurs moving the file
	 */
	public void moveDirectory(String srcDir, String destDir) throws IOException{
		FileUtils.moveDirectory(new File(normalizePath(srcDir)), new File(normalizePath(destDir)));
	}
	
	/*
		 Moves a directory to another directory.
	
		Parameters:
			src - the file to be moved
			destDir - the destination file
			createDestDir - If true create the destination directory, otherwise if false throw an IOException 
		Throws:
			NullPointerException - if source or destination is null 
			IOException - if source or destination is invalid 
			IOException - if an IO error occurs moving the file
	 */
	public void moveDirectoryToDirectory(String src, String destDir, boolean bCreateDestDir) throws IOException{
		FileUtils.moveDirectoryToDirectory(new File(normalizePath(src)), new File(normalizePath(destDir)), bCreateDestDir);
	}
	
	/*
		Moves a file.
	
		When the destination file is on another file system, do a "copy and delete".
	
		Parameters:
			srcFile - the file to be moved
			destFile - the destination file 
		Throws:
			NullPointerException - if source or destination is null 
			IOException - if source or destination is invalid 
			IOException - if an IO error occurs moving the file
	 */
	public void moveFile(String srcFile, String destFile) throws IOException{
		FileUtils.moveFile(new File(normalizePath(srcFile)), new File(normalizePath(destFile)));
	}
	
	/*
		Moves a file.
	
		When the destination file is on another file system, do a "copy and delete".
	
		Parameters:
			srcFile - the file to be moved
			destFile - the destination file 
		Throws:
			NullPointerException - if source or destination is null 
			IOException - if source or destination is invalid 
			IOException - if an IO error occurs moving the file
	 */
	public void moveFile(File srcFile, File destFile) throws IOException{
		FileUtils.moveFile(srcFile, destFile);
	}
	
	/*
		Moves a file to a directory.
	
		Parameters:
			srcFile - the file to be moved
			destDir - the destination file
			createDestDir - If true create the destination directory, otherwise if false throw an IOException 
		Throws:
			NullPointerException - if source or destination is null 
			IOException - if source or destination is invalid 
			IOException - if an IO error occurs moving the file
	 */
	public void moveFileToDirectory(String srcFile, String destDir, boolean createDestDir) throws IOException{
		FileUtils.moveFileToDirectory(new File(normalizePath(srcFile)), new File(normalizePath(destDir)), createDestDir);
	}
	
	/*
		Moves a file or directory to the destination directory.
	
		When the destination is on another file system, do a "copy and delete".
		
		Parameters:
			src - the file or directory to be moved
			destDir - the destination directory
			createDestDir - If true create the destination directory, otherwise if false throw an IOException 
			Throws:
				NullPointerException - if source or destination is null 
				IOException - if source or destination is invalid 
				IOException - if an IO error occurs moving the file
	 */
	public void moveToDirectory(String src, String destDir, boolean createDestDir) throws IOException {
		FileUtils.moveToDirectory(new File(normalizePath(src)), new File(normalizePath(destDir)), createDestDir);
	}
	
	/*
	 	Reads the contents of a file into a byte array. The file is always closed.

		Parameters:
			file - the file to read, must not be null 
		Returns:
			the file contents, never null 
		Throws:
			IOException - in case of an I/O error
	 */
	public byte[] readFileToByteArray(String file) throws IOException{
		return FileUtils.readFileToByteArray(new File(normalizePath(file)));
	}
	
	
	
	/*
	 	Reads the contents of a file into a String using the default encoding for the VM. The file is always closed.

		Parameters:
			filePath - the file to read, must not be null, ~ is  supported
		Returns:
			the file contents, never null 
		Throws:
			IOException - in case of an I/O error
	 */
	public String readFileToString(String filePath) throws IOException{
		return FileUtils.readFileToString(new File(findFilePathInDirList(filePath, false)), Charset.defaultCharset());
	}
	
	/*
 	Reads the contents of a file into a String using the default encoding for the VM. The file is always closed.
	
		Parameters:
			file - the file to read, must not be null 
		Returns:
			the file contents, never null 
		Throws:
			IOException - in case of an I/O error
	 */
	public String readFileToString(File file) throws IOException{
		return FileUtils.readFileToString(file, Charset.defaultCharset());
	}

	/*
	    Reads the contents of a file line by line to a List of Strings using the default encoding for the VM. The file is always closed.
	
	    Parameters:
	        file - the file to read, must not be null 
	    Returns:
	        the list of Strings representing each line in the file, never null 
	    Throws:
	        IOException - in case of an I/O error
	 */
	public List<String> readLines(String filePath) throws Exception{
		return FileUtils.readLines(new File(normalizePath(filePath)), Charset.defaultCharset());
	}

	/*
	   	Reads the contents of a file line by line to a List of Strings. The file is always closed.
	
	    Parameters:
	        file - the file to read, must not be null
	        encoding - the encoding to use, null means platform default 
	    Returns:
	        the list of Strings representing each line in the file, never null 
	    Throws:
	        IOException - in case of an I/O error 
	        UnsupportedEncodingException - if the encoding is not supported by the VM
	 */
	public List<String> readLines(String filePath, String encoding) throws Exception{
		return FileUtils.readLines(new File(normalizePath(filePath)), encoding);
	}

	/*
	 	Counts the size of a directory recursively (sum of the length of all files).

    	Parameters:
        	directory - directory to inspect, must not be null 
    	Returns:
        	size of directory in bytes, 0 if directory is security restricted 
    	Throws:
        	NullPointerException - if the directory is null
	 */
	public long sizeOfDirectory(String dirPath){
		return FileUtils.sizeOfDirectory(new File(normalizePath(dirPath)));
	}
	
	/*
		Implements the same behavior as the "touch" utility on Unix. It creates a new file with size 0 or, if the file exists already, it is opened and closed without modifying it, but updating the file date and time.
	
		NOTE: As from v1.3, this method throws an IOException if the last modified date of the file cannot be set. Also, as from v1.3 this method creates parent directories if they do not exist.
	
		Parameters:
			file - the File to touch 
		Throws:
			IOException - If an I/O problem occurs
	 */
	public void touch(String file) throws IOException{
		FileUtils.touch(new File(normalizePath(file)));
	}
	
	public synchronized void write(String contentStr, String filePath, boolean append) throws IOException{
		FileUtils.write(new File(normalizePath(filePath)), contentStr, Charset.defaultCharset(), append);
    }
	
	public synchronized void write(String contentStr, String filePath, Charset encoding, boolean append) throws IOException{
        FileUtils.write(new File(normalizePath(filePath)), contentStr, encoding, append);
    }

	public synchronized void writeStringToFile(String contentStr, String filePath) throws IOException{
		FileUtils.write(new File(normalizePath(filePath)), contentStr, Charset.defaultCharset());
	}
	
	/*
		String org.apache.commons.io.FilenameUtils.normalizeNoEndSeparator(String filename)

		Normalizes a path, removing double and single dot path steps, and removing any final 
		directory separator.

		This method normalizes a path to a standard format. The input may contain separators in either 
		Unix or Windows format. The output will contain separators in the format of the system.

		A trailing slash will be removed. A double slash will be merged to a single slash 
		(but UNC names are handled). A single dot path segment will be removed. A double dot 
		will cause that path segment and the one before to be removed. If the double dot has 
		no parent path segment to work with, null is returned.
		
		The output will be the same on both Unix and Windows except for the separator character.
		
		 /foo//               -->   /foo
		 /foo/./              -->   /foo
		 /foo/../bar          -->   /bar
		 /foo/../bar/         -->   /bar
		 /foo/../bar/../baz   -->   /baz
		 //foo//./bar         -->   /foo/bar
		 /../                 -->   null
		 ../foo               -->   null
		 foo/bar/..           -->   foo
		 foo/../../bar        -->   null
		 foo/../bar           -->   bar
		 //server/foo/../bar  -->   //server/bar
		 //server/../bar      -->   null
		 C:\foo\..\bar        -->   C:\bar
		 C:\..\bar            -->   null
		 ~/foo/../bar/        -->   ~/bar
		 ~/../bar             -->   null
		 
		(Note the file separator returned will be correct for Windows/Unix)
		Parameters:
		filename the filename to normalize, null returns null
		Returns:
		the normalized filename, or null if invalid. Null bytes inside string will be removed
	 */
	public String normalizePath(String path){
        String normalizedPath = path;
        
        if(path.startsWith("~")){
            normalizedPath = path.replaceFirst("^~", System.getProperty("user.home"));
        }else if(!path.equals(".")){
            normalizedPath = FilenameUtils.normalizeNoEndSeparator(path);
        }
        
        return normalizedPath;
    }
	
	public byte[] getBytes(String filePath) throws IOException {
        File file = new File(filePath=normalizePath(filePath));
        
        InputStream is = new FileInputStream(file);
        byte[] bytes = IOUtils.toByteArray(is);

        LOGGER.debug("Byte array size: " + bytes.length);
        
        return bytes;
    }
	
	public synchronized boolean fileContains(String filePath, String regEx) throws IOException{
        return ApacheCommonsIOServices.build().readFileToString(filePath).matches(regEx);
    }

	public synchronized boolean fileExists(String filePath){
        boolean bExists = false;
        File file = new File(".");
        LOGGER.trace(file.getAbsolutePath());
        file = new File(filePath=normalizePath(filePath));
        if(file.exists() && file.isFile()){
            bExists = true;
        }
        
        return bExists;
    }
	
	public synchronized boolean dirExists(String filePath){
        boolean bExists = false;
        File file = new File(filePath=normalizePath(filePath));
        if(file.exists() && file.isDirectory()){
            bExists = true;
        }
        
        return bExists;
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
        return new Date(getLastModifiedTime(normalizePath(filePath))).toString();
    }
	
	
	
   // "^output.log.*"
   public void deleteFiles(String dirPath, String fileRegEx,
           boolean recurseDir) throws IOException {
       Collection<File> files = FileUtils.listFiles(new File(normalizePath(dirPath)),
               new RegexFileFilter(fileRegEx),
               recurseDir == true ? DirectoryFileFilter.DIRECTORY : null);

       for (File file : files) {
           FileUtils.forceDelete(file);
       }
   }
   
   //Deletes all files and subdirectories under dir.
   //Returns true if all deletions were successful.
   //If a deletion fails, the method stops attempting to delete and returns false.
   public synchronized void deleteDirectory(String dirPath)throws IOException{
       LOGGER.debug("deleteDir: " + (dirPath=normalizePath(dirPath)));
       FileUtils.deleteDirectory(new File(dirPath));
   }

  
   
   public synchronized void deleteFiles(Collection<File> files) throws IOException{
       for(File file : files){
           LOGGER.debug("deleting file: {}", file.getAbsolutePath());
           FileUtils.forceDelete(file);
       }
   }
   
   public synchronized void delete(String filePath) throws IOException{
      new File(filePath).delete();
   }

   /*   
       Deletes a file, never throwing an exception. If file is a directory, delete it and all sub-directories.

       The difference between File.delete() and this method are:
    
           A directory to be deleted does not have to be empty.
           No exceptions are thrown when a file or directory cannot be deleted.
    
       Parameters:
           file - file or directory to delete, can be null
       Returns:
           true if the file or directory was deleted, otherwise false
   */
   public synchronized void deleteQuietly(String filePath) throws IOException{
       LOGGER.debug("deleting file: {}", filePath);
       FileUtils.deleteQuietly(new File(normalizePath(filePath)));
   }
   
   public String convertToForwardSlash(String backSlasStr) {
       // Strings with backslashes come in like this:
       // "folder\\subfolder"
       // In order to replace "\\" we must use "\\\\"
       return backSlasStr.replaceAll("\\\\", "/");
   }
   
   public boolean mkDirs(String dirPath)throws IOException{
       File file = new File(dirPath=normalizePath(dirPath));
       return file.mkdirs();
   }
   
   public long getLineCnt(String filePath) throws FileNotFoundException, IOException{
	   LineNumberReader lnr = null;
	   long linenumber = 0;
		try{	
			File file = new File(filePath);
			if(!file.exists()){
				throw new FileNotFoundException(filePath);
			}
		    FileReader fr = new FileReader(new File(filePath));
		    lnr = new LineNumberReader(fr);
		    linenumber = 0;
            while (lnr.readLine() != null){
            	linenumber++;
            }
            System.out.println("Total number of lines : " + linenumber);
		}finally{
			if(lnr!=null){
				lnr.close();
			}
		}
		
		return linenumber;      
   }

   public List<String> getLines(final String filePath, final long offset) throws IOException{
	   	File file = new File(filePath);
		if(!file.exists()){
			throw new FileNotFoundException(filePath);
		}
	    final LineIterator it = FileUtils.lineIterator(file);
	    int index = 0;
	    final List<String> list = new ArrayList<>();
	    while(it.hasNext()){
	        final String line = it.nextLine();
	        if(++index > offset){
	        	list.add(line);
	        }
	    }
	    it.close();
	    return list;
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
   
   public String createTempDir() {
       return createTempDir(null, null);
   }
   
   public String createTempDir(String baseDir) {
       return createTempDir(baseDir, null);
   }
   
   /**
    * Creates a temporary subdirectory in the standard temporary directory.
    * This will be automatically deleted upon exit.
    * @param prefix
    *            the prefix used to create the directory, completed by a
    *            current timestamp. Use for instance your application's name
    * 
    * @return the directory
    */
   public String createTempDir(String baseDir, String prefix) {
       baseDir = baseDir==null ? FileUtils.getTempDirectory().getAbsolutePath() : normalizePath(baseDir);
       prefix = prefix==null ? "" : prefix;

       final File tmp = new File(baseDir
               + "/" + prefix + System.nanoTime());
       tmp.mkdir();
       Runtime.getRuntime().addShutdownHook(new Thread() {

           @Override
           public void run() {

               try {
                   FileUtils.deleteDirectory(tmp);
               } catch (IOException e) {
                   LOGGER.error("{}\n{}", e.getMessage()==null ? "" : e.getMessage(), ExceptionUtils.getStackTrace(e));
               }
           }
       });
       return tmp.getAbsolutePath();

   }
   
   public void createFileWithByteLength(String filePath, int byteLength) throws IOException {
	   FileUtils.writeByteArrayToFile(new File(filePath), new byte[byteLength]);
   }
   
   public void createFileWithKbLength(String filePath, int kbLength) throws IOException {
	   FileUtils.writeByteArrayToFile(new File(filePath), new byte[kbLength*1024]);
   }
   
   public void createFileWithMbLength(String filePath, int mbLength) throws IOException {
	   FileUtils.writeByteArrayToFile(new File(filePath), new byte[mbLength*1024*1024]);
   }
   
   public void createFileWithGbLength(String filePath, int gbLength) throws IOException {
	   FileUtils.writeByteArrayToFile(new File(filePath), new byte[gbLength*1024*1024*1024]);
   }
   
   public long getAtime(String path) throws IOException {
	   Path file = Paths.get(path);
	   BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
	   return attrs.lastAccessTime().toMillis();
   }
   
   public long getMtime(String path) throws IOException {
	   return new File(path).lastModified();
   }
   
   @Override
   protected ApacheCommonsIOServices clone() throws CloneNotSupportedException{
       return (ApacheCommonsIOServices)SerializationUtils.clone(this);
   }
   
}
