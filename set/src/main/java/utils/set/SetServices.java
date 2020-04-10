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

package utils.set;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class SetServices implements Serializable, Cloneable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(new Throwable().getStackTrace()[0].getClassName());
	

	public static SetServices build() {
	    return new SetServices();
	}
	
	private SetServices() {}
	
	
	public <T> Set<T> union(Set<T> setA, Set<T> setB) {
		Set<T> tmp = new TreeSet<T>(setA);
		tmp.addAll(setB);
		return tmp;
	}

	public <T> Set<T> intersection(Set<T> setA, Set<T> setB) {
		Set<T> tmp = new TreeSet<T>();
		for (T x : setA){
			if (setB.contains(x)){
				tmp.add(x);
			}
		}
		return tmp;
	}

	public <T> Set<T> difference(Set<T> setA, Set<T> setB) {
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
	public <T> Set<T> symDifference(Set<T> setA, Set<T> setB) {
		Set<T> tmpA;
		Set<T> tmpB;

		tmpA = union(setA, setB);
		tmpB = intersection(setA, setB);
		return difference(tmpA, tmpB);
	}

	public <T> boolean isSubset(Set<T> setA, Set<T> setB) {
		return setB.containsAll(setA);
	}

	public <T> boolean isSuperset(Set<T> setA, Set<T> setB) {
		return setA.containsAll(setB);
	}

	
	
	public static void main(String args[]) {
		TreeSet<Character> set1 = new TreeSet<Character>();
		TreeSet<Character> set2 = new TreeSet<Character>();

		set1.add('A');
		set1.add('B');
		set1.add('C');
		set1.add('D');

		set2.add('C');
		set2.add('D');
		set2.add('E');
		set2.add('F');

		SetServices setServices = SetServices.build();
		LOGGER.info("set1: " + set1);
		LOGGER.info("set2: " + set2);
		LOGGER.info("difference: " + setServices.difference(set1, set2));
		LOGGER.info("symDifference: " + setServices.symDifference(set1, set2));
		

		LOGGER.info("Union: " + setServices.union(set1, set2));
		LOGGER.info("Intersection: " + setServices.intersection(set1, set2));
		LOGGER.info("Difference (set1 - set2): "
				+ setServices.difference(set1, set2));
		LOGGER.info("Symmetric Difference: " + setServices.symDifference(set1, set2));

		TreeSet<Character> set3 = new TreeSet<Character>(set1);

		set3.remove('D');
		LOGGER.info("set3: " + set3);

		LOGGER.info("Is set1 a subset of set2? " + setServices.isSubset(set1, set3));
		LOGGER.info("Is set1 a superset of set2? " + setServices.isSuperset(set1, set3));
		LOGGER.info("Is set3 a subset of set1? " + setServices.isSubset(set3, set1));
		LOGGER.info("Is set3 a superset of set1? " + setServices.isSuperset(set3, set1));

	}
	
	@Override
    protected SetServices clone() throws CloneNotSupportedException{
        return (SetServices)SerializationUtils.clone(this);
    }
}
