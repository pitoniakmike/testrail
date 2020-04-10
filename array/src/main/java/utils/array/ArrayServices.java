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

package utils.array;


import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class ArrayServices implements Serializable, Cloneable{
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(new Throwable().getStackTrace()[0].getClassName());    
	public static final String  NEWLINE = "\r\n|\n|\r";
	
	public static ArrayServices build() {
	    return new ArrayServices();
	}
	
	private ArrayServices() {}
    
    /*
      	boolean java.util.Arrays.equals(Object[] a, Object[] a2)
      	
		Returns true if the two specified arrays of Objects are equal to one another. The two arrays are considered equal if both arrays contain the same number of elements, and all corresponding pairs of elements in the two arrays are equal. Two objects e1 and e2 are considered equal if (e1==null ? e2==null : e1.equals(e2)). In other words, the two arrays are equal if they contain the same elements in the same order. Also, two array references are considered equal if both are null.

		Parameters:
		a one array to be tested for equality
		a2 the other array to be tested for equality
		Returns:
		true if the two arrays are equal
     */

    
    public <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}
    
    public List<Object> listContains(List<Object> targetList, List<Object> valueList) throws IOException{
		List<Object> matchList = new ArrayList<Object>();
		
		Iterator<Object> valueListIterator = valueList.iterator();
		while (valueListIterator.hasNext()) {
			Object nextValue = valueListIterator.next();
			
			if(targetList.contains(nextValue)){
				LOGGER.info(nextValue + ":" + targetList.get(targetList.indexOf(nextValue)));
				matchList.add(nextValue);
			}
		}
		
		return matchList;
	}
	
	public <T>Iterator<T> getIterator(T[] strArray){
		return Arrays.stream(strArray).iterator();
	}

    
    /**
     * @param strDataArray
     * @return
     */    
    public void sortList(List list){
    	Collections.sort(Collections.synchronizedList(list)); 
    }
    
    /*
        Adds all the elements of the given arrays into a new array.

    	The new array contains all of the element of array1 followed by all of the elements array2. 
    	When an array is returned, it is always a new array.

     	ArrayUtils.addAll(null, null)     = null
     	ArrayUtils.addAll(array1, null)   = cloned copy of array1
     	ArrayUtils.addAll(null, array2)   = cloned copy of array2
     	ArrayUtils.addAll([], [])         = []
     	ArrayUtils.addAll([null], [null]) = [null, null]
     	ArrayUtils.addAll(["a", "b", "c"], ["1", "2", "3"]) = ["a", "b", "c", "1", "2", "3"]

    	Parameters:
        	array1 - the first array whose elements are added to the new array, may be null
        	array2 - the second array whose elements are added to the new array, may be null 
    	Returns:
        	The new array, null if null array inputs. The type of the new array is the type of the 
        	first array.
     */
    
    public Object[] addAll(Object[] array1, Object[] array2){
    	 return ArrayUtils.addAll(array1, array2);
    }
    

    public <T> List<T> collectionToList(Collection<T> col) {
        return new ArrayList<T>(col);
    }
    
    public <T> void addArrayToCollection(T[] array, Collection<T> collection){
    	collection.addAll(Arrays.asList(array));
    }
    
    
    /*
        Outputs an array as a String, treating null as an empty array.

    	Multi-dimensional arrays are handled correctly, including multi-dimensional primitive arrays.
    	The format is that of Java source code, for example {a,b}.

    	Parameters:
        	array - the array to get a toString for, may be null 
    	Returns:
        	a String representation of the array, '{}' if null array input
    */
    public String toString(Object[] array){
    	return ArrayUtils.toString(array);
    }
    
    public Object[] reverse(Object[] array){
   	  	ArrayUtils.reverse(array);
   	  	return array;
    }
    
    
    public void reverse(List<?> list){
        Collections.reverse(list);
    }
    
    /**
     * @param strDataArray
     * @return
     */    
    public String[] sortStringArray(String[] strDataArray){
    	Arrays.sort(strDataArray);   
    	return strDataArray;
    }
    
    /**
     * @param intDataArray
     * @return
     */    
    public int[] sortIntArray(int[] intDataArray){
    	Arrays.sort(intDataArray);  
        return intDataArray;
    }
    
    public String[] pathToStringArray(String path){
    	ArrayList<String> arrayList = new ArrayList<>();
    	StringTokenizer st = new StringTokenizer(path, "/");
    	while(st.hasMoreTokens()){
    		arrayList.add(st.nextToken());
    	}
    	return arrayList.toArray(new String[arrayList.size()]);
    }
    
    public String listToDelimitedString(List<String> list, String delimiter){
    	StringBuilder sb = new StringBuilder();

    	for (Object obj : list) {
    		sb.append(sb.length()!=0 ? delimiter + obj.toString() : obj.toString());
    	}

    	return sb.toString();
    }
    
    public String[] listToStringArray(List<String> list){
		return list.toArray(new String[list.size()]);
	}
    
    public List<String> stringArrayToList(String[] arr){
    	return  Arrays.asList(arr);
    }
    
    public Vector<String> stringArrayToVector(String[] arr){
    	return arr==null ? null : new Vector<String>(Arrays.asList(arr));
    }
    
    public void displayList(List list){
  	  	Iterator iterator = list.iterator();
  	  	while ( iterator.hasNext() ){
  	  		LOGGER.info("{}", iterator.next());
  	  	}
    }
    
    public Vector listToVector(List list){
    	return new Vector(list);
    }

    public String strArrayToCsv(String[] strArray){
        StringBuilder sb = new StringBuilder();
        
    
        for(int i=0; i<strArray.length; i++){
        	sb.append(sb.length()==0 ? strArray[i] : "," + strArray[i]);
        }

        return sb.toString();
    }
    
    public String strArrayToString(String[] strArray){
        StringBuilder sb = new StringBuilder();
        
    
        for(int i=0; i<strArray.length; i++){
        	sb.append(sb.length()==0 ? strArray[i] : " " + strArray[i]);
        }

        return sb.toString();
    }
    
    public int[] stringArraytoIntArray(String[] strArray) {
    	int intArray[] = null;
    	
    	if (strArray != null) {
	    	intArray = new int[strArray.length];
	    	for (int i = 0; i < strArray.length; i++) {
	    		intArray[i] = Integer.parseInt(strArray[i]);
	    	}
    	}
    	
    	return intArray;
    }
    
    public boolean contains(int[] arr, Object val) {
    	return Arrays.asList(arr).contains(val); 
    }
    
    public List<Integer> intArrayToList(int[] intArray) {
    	List<Integer> arrayList = new ArrayList<Integer>();
    	
    	if (intArray != null) {
	    	for (int i = 0; i < intArray.length; i++) {
	    		arrayList.add(intArray[i]);
	    	}
    	}
    	
    	return arrayList;
    }
    
    public List<Object> objArrayToList(Object[] objArray){
    	List<Object> arrayList = new ArrayList<Object>();
    	
    	if (objArray != null) {
	    	for (int i = 0; i < objArray.length; i++) {
	    		arrayList.add(objArray[i]);
	    	}
    	}
    	
    	return arrayList;
    }
    
    public String[] delimitedStringtoStringArray(String str, String delimiter){
    	String[] sArray = null;
    	
    	if(str!=null){
    		sArray = str.split(delimiter);
    	}
    	
    	return sArray;
    }
    
    public int[] delimitedStringtoIntArray(String str, String delimiter){
    	int[] intArray = null;
    	
    	if(str!=null){
    		intArray = stringArraytoIntArray(str.split(delimiter));
    	}
    	
    	return intArray;
    }
    
    public String listToString(List<?> list){
  	  	StringBuilder sb = new StringBuilder();
    	Iterator<?> iterator = list.iterator();
  	  	while ( iterator.hasNext() ){
  	  		sb.append(iterator.next() + "\n");
  	  	}
  	  	
  	  	return sb.toString().trim();
    }
      
    public <T> boolean compareArrays(T[] array1, T[] array2){
    	return Arrays.equals(array1, array2);
    }

    public <T> List<T> toList(T[] array){
    	return new ArrayList<T>(Arrays.asList(array));
    }
    
    public <T> T[] appendValue(T[] array, T... value) {
		return ArrayUtils.insert(array.length, array, value);
	}

    public <T> Set<T> toSet(List<T> list){
    	return new HashSet<T>(list);
    }
    
    public <T> List<T> fromSet(Set<T> list){
    	return new ArrayList<T>(list);
    }
    
	public <T> Set<T> union(List<T> listA, List<T> listB) {
		return union(toSet(listA), toSet(listB));
	}

	public <T> Set<T> intersection(List<T> listA, List<T> listB) {
		return intersection(toSet(listA), toSet(listB));
	}

	public <T> Set<T> difference(List<T> listA, List<T> listB) {
		return difference(toSet(listA), toSet(listB));
	}

	/*
	 * In mathematics, the symmetric difference of two sets is the set of
	 * elements which are in either of the sets and not in their intersection.
	 * 
	 * For example, the symmetric difference of the sets {1,2,3} and {3,4} is
	 * {1,2,4}.
	 * 
	 * The symmetric difference of the set of all students and the set of all
	 * females consists of all male students together with all female
	 * non-students.
	 */
	public <T> Set<T> symDifference(List<T> listA, List<T> listB) {
		return symDifference(toSet(listA), toSet(listB));
	}

	public <T> boolean isSubset(List<T> listA, List<T> listB) {
		return isSubset(toSet(listA), toSet(listB));
	}

	public <T> boolean isSuperset(List<T> listA, List<T> listB) {
		return isSuperset(toSet(listA), toSet(listB));
	}

	
	private <T> Set<T> union(Set<T> setA, Set<T> setB) {
		Set<T> tmp = new TreeSet<T>(setA);
		tmp.addAll(setB);
		return tmp;
	}

	private <T> Set<T> intersection(Set<T> setA, Set<T> setB) {
		Set<T> tmp = new TreeSet<T>();
		for (T x : setA){
			if (setB.contains(x)){
				tmp.add(x);
			}
		}
		return tmp;
	}

	private <T> Set<T> difference(Set<T> setA, Set<T> setB) {
		Set<T> tmp = new TreeSet<T>(setA);
		tmp.removeAll(setB);
		return tmp;
	}
	/*
	 * In mathematics, the symmetric difference of two sets is the set of
	 * elements which are in either of the sets and not in their intersection.
	 * 
	 * For example, the symmetric difference of the sets {1,2,3} and {3,4} is
	 * {1,2,4}.
	 * 
	 * The symmetric difference of the set of all students and the set of all
	 * females consists of all male students together with all female
	 * non-students.
	 */
	private <T> Set<T> symDifference(Set<T> setA, Set<T> setB) {
		Set<T> tmpA;
		Set<T> tmpB;

		tmpA = union(setA, setB);
		tmpB = intersection(setA, setB);
		return difference(tmpA, tmpB);
	}

	private <T> boolean isSubset(Set<T> setA, Set<T> setB) {
		return setB.containsAll(setA);
	}

	private <T> boolean isSuperset(Set<T> setA, Set<T> setB) {
		return setA.containsAll(setB);
	}


   
    
    public void main(String[] args) {
    	try{
	
			//LOGGER.info(ArrayServices.build().toList(new int[]{1, 2}));
			//LOGGER.info(ArrayServices.build().toList(new float[]{1.0f, 2.0f}));
			
			List<String> target = ArrayServices.build().toList(new String[]{"a", "b", "c", "d"});
			List<String> values = ArrayServices.build().toList(new String[]{"a", "d"});
			LOGGER.info("{}", target);
			LOGGER.info("{}", values);
			
			
			List<Integer> intList = new ArrayList<Integer> (Arrays.asList(1, 2, 3));
			List<Integer> intList2 = new ArrayList<Integer> (Arrays.asList(1, 2, 3));

			//LOGGER.info(listContains(target, values));
			
			Set<String> s  = ArrayServices.build().toSet(target);
    		LOGGER.info("{}", s.size());
    		
    		Set<String> s2  = ArrayServices.build().<String>toSet(values);

    		List<String> l = ArrayServices.build().<String>fromSet(s);
    		List<String> l2 = ArrayServices.build().<String>fromSet(s2);
    		
    		List untypedList = new ArrayList(Arrays.asList(1, 2, 3));
    		
    		
    		LOGGER.info("difference: {}", ArrayServices.build().<String>difference(l, l2));
    		
    		// T inferred to be String
    		LOGGER.info("difference: {}", ArrayServices.build().difference(l, l2));

    		// Calculating Intersection of two Set in Java
            Set<String> intersection = ArrayServices.build().<String>union(l, l2);
            
            LOGGER.info("Intersection of two Set %s and %s in Java is %s %n",
                    s.toString(), s2.toString(), intersection.toString());
            
            LOGGER.info("Number of elements common in two Set : "
                               + intersection.size());
         
            // Calculating Union of two Set in Java
            Set<String> unionSet = ArrayServices.build().<String>union(l, l2);
            
            LOGGER.info("Union of two Set %s and %s in Java is %s %n",
                    s.toString(), s2.toString(), unionSet.toString());
            
            LOGGER.info("total number of element in union of two Set is : "
                                + unionSet.size());
   
            Set<String> s3  = ArrayServices.build().symDifference(target, values);
            LOGGER.info("symDifference: {}" , s3);
                   

    	}catch(Exception e){
    		LOGGER.error("{}\n{}", e.getMessage()==null ? "" : e.getMessage(), ExceptionUtils.getStackTrace(e));
    	}
		
	}
    
    /**
     * Returns true if String array has duplicates.
     * @param strings
     * @return
     */
    public boolean arrayHasDuplicates(String[] strings) {
        
        boolean retBool = false;
        HashSet<String> set = new HashSet<String>();
        for(String string : strings) {
            if(!set.add(string)) {
                retBool = true;
                break;
            }
        }
        return retBool;
    }
    
    public List<String> readFileToListOfString(String filePath) throws IOException{
    	return readFileToListOfString(filePath, false);
    }
    
    public List<String> readFileToListOfString(String filePath, boolean removeEmptyStrs) throws IOException{
        List<String> lineList = Files.readAllLines(Paths.get(filePath));
        if(removeEmptyStrs){
            lineList.removeIf(String::isEmpty);
        }
        return lineList;
    }
    
    public List<String> strToList(String str){
        return new ArrayList<>(Arrays.asList(str.split(NEWLINE)));
    }
    
    @Override
	protected ArrayServices clone() throws CloneNotSupportedException{
		return (ArrayServices)SerializationUtils.clone(this);
	}

}
