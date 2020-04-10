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

package apache.commons.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.net.URL;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ApacheCommonsConfigServices implements Serializable, Cloneable{
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(new Throwable().getStackTrace()[0].getClassName());
	
	
	public static ApacheCommonsConfigServices build() {
	    return new ApacheCommonsConfigServices();
	}
	
	private ApacheCommonsConfigServices() {}
	
	
	public Configuration load(String propFilePath) throws FileNotFoundException, ConfigurationException{
		File file = new File(propFilePath);
		if (!file.exists()) {
			throw new FileNotFoundException(propFilePath);
		}
		
		return new PropertiesConfiguration(propFilePath);
	}
	
	
	public Configuration loadUrl(String resourcePath) throws FileNotFoundException, ConfigurationException{
		URL url = ClassLoader.getSystemResource(resourcePath);
		return new PropertiesConfiguration(url);
	}
	
	
	
	
	
	public static void main(String[] args) {
		try {
			Configuration configuration = new ApacheCommonsConfigServices().load("support/properties/password.properties");
			LOGGER.info("{}", configuration.getString("password"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
    protected ApacheCommonsConfigServices clone() throws CloneNotSupportedException{
        return (ApacheCommonsConfigServices)SerializationUtils.clone(this);
    }

}
