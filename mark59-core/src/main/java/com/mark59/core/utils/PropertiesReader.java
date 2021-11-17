/*
 *  Copyright 2019 Insurance Australia Group Limited
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mark59.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Static properties file reader, loading the mark59 properties file into memory
 * just once per run to reduce disk I/O.
 * 
 * @author Michael Cohen
 * @author Philip Webb Written: Australian Winter 2019
 */
public class PropertiesReader {
	private static final Logger LOG = LogManager.getLogger(PropertiesReader.class);

	private static final String WINDOWS = "WINDOWS";
	private static final String NIX = "*NIX";
	private static final String MARK59_PROPERTIES = "mark59.properties";

	private String os = NIX;
	private String osNameLowerCase = "nix";
	private Properties properties = new Properties();

	private static PropertiesReader instance;

	private PropertiesReader() throws IOException {

		if (System.getProperty("os.name") != null) {
			osNameLowerCase = System.getProperty("os.name").toLowerCase(java.util.Locale.ENGLISH);
		}
		
		os = NIX;
		if (osNameLowerCase.indexOf("win") >= 0) {
			os = WINDOWS;
		}

		LOG.info("");
		LOG.info("Host Server Details : ");
		LOG.info("    Name : " + InetAddress.getLocalHost().getHostName());
		LOG.info("    IP   : " + IpUtilities.getLocalHostIP());
		LOG.info("    o/s  : " + osNameLowerCase);
		LOG.info("    ------------------ ");
		LOG.info("");

		loadMark59Properties();

		if (LOG.isDebugEnabled()) {
			LOG.debug("");
			LOG.debug("Printing all jvm System Properties ");
			LOG.debug("------------------------------------- ");
			System.getProperties().forEach((key, value) -> LOG.debug(key + ": " + value));
			LOG.debug("------------------------------------- ");

			LOG.debug(" Mark59 Properties Loaded From " + MARK59_PROPERTIES + ":");
			properties.forEach((p, v) -> {if (((String) p).startsWith("mark59")) {LOG.debug("    " + p + " : " + v);}});
			LOG.debug("    ----------------------- ");
			LOG.debug("    " + MARK59_PROPERTIES + " properties may be overwritten by being set as system properties (eg directly in the Jmetar plans)   ");
		}

		properties.forEach((p, v) -> {
			if (!Arrays.asList(PropertiesKeys.MARK59_PROPERTY_KEYS).contains(p)) {
				LOG.warn("Mark59 properties file (" + MARK59_PROPERTIES + ") contains an unmapped property name : '" + p
						+ "' - have you misspelt a property?");
			}
		});

		LOG.info("");
		LOG.info("Mark59 property settings : ");
		setMark59property(PropertiesKeys.MARK59_PROP_SCREENSHOT_DIRECTORY);
		setMark59property(PropertiesKeys.MARK59_PROP_DRIVER_CHROME, true);
		setMark59property(PropertiesKeys.MARK59_PROP_DRIVER_FIREFOX, true);
		setMark59property(PropertiesKeys.MARK59_PROP_SERVER_PROFILES_EXCEL_FILE_PATH);
		setMark59property(PropertiesKeys.MARK59_PROP_BROWSER_EXECUTABLE, true);
		LOG.info("    ----------------------- ");
	}

	/**
	 * Load the JMeter properties file; if not found, then default to
	 * "org/apache/jmeter/mark59.properties" from the classpath.
	 * <p>
	 * Based on the 'loadJMeterProperties' method in org.apache.jmeter.util.JMeterUtils (ApacheJMeter_core-xxx.jar),
	 * and class org.apache.jmeter.NewDriver  (ApacheJMeter-xxx.jar)
	 */
	private void loadMark59Properties() {

		String javaClassPath = System.getProperty("java.class.path");
		LOG.debug("javaClassPath : " + javaClassPath);

		// Try and find the JMeter install directory (based on initial class path), or if using an IDE the project root  
		
		String searchDir;
		StringTokenizer tok = new StringTokenizer(javaClassPath, File.pathSeparator);
		String javaClassPathFile = tok.nextToken();

		if (tok.countTokens() == 1 || (tok.countTokens() == 2 && osNameLowerCase.startsWith("mac os x"))) {
			// 							Java on Mac OS can add a second entry to the initial classpath ..
			File jar = new File(javaClassPathFile);
			try {
				searchDir = jar.getCanonicalFile().getParentFile().getParent();
			} catch (IOException e) {
				searchDir = null;
			}
			LOG.debug("mark59.properties dir based on java.class.path : " + searchDir);
		} else { // e.g. started from IDE with full classpath
			searchDir = System.getProperty("jmeter.home", "");
			if (searchDir.length() == 0) { // e.g. started from IDE with full classpath
				searchDir = new File(System.getProperty("user.dir")).getPath();
			}
			LOG.debug("mark59.properties dir based on jmeter.home or user.dir : " + searchDir);
		}

		// Next, try and find a mark59.properties file		
		
		if (!loadMark59propertiesOk(searchDir + File.separator + MARK59_PROPERTIES)) {
			
			// try again (using a path that works running as a Win System Administrator)
			String searchJavaClassPathParentFile = new File(javaClassPathFile).getParent() + File.separator	+ MARK59_PROPERTIES; 
			LOG.debug(" mark59.properties not found using user.dir, jmeter.home or java.class.path trying : "+ searchJavaClassPathParentFile);

			if (!loadMark59propertiesOk(searchJavaClassPathParentFile)) {
				
				LOG.debug("mark59.properties not found in dirs, attempting to find on class search path org/apache/jmeter/mark59.properties .. ");
				InputStream is = ClassLoader.getSystemResourceAsStream("org/apache/jmeter/mark59.properties");
				if (is != null) {
					try {
						properties.load(is);
						LOG.info("Using mark59.properties found on the class path (org/apache/jmeter/mark59.properties) .. ");
					} catch (IOException e) {
						LOG.info("mark59.properties not found (error reading classpath entry org/apache/jmeter/mark59.properties) : " + e.getMessage());
						LOG.info("Note that mark59 properties may of been set directy in system properties (see below)");
					}
				} else {
					LOG.info("mark59.properties not found !! Note that mark59 properties may of been set directy in system properties (see below)");
				}
			}
		}
	}

	
	private boolean loadMark59propertiesOk(String mark59properties) {
		FileInputStream is = null;
		try {
			File mark59propertiesFile = new File(mark59properties);
			is = new FileInputStream(new File(mark59properties));
			properties.load(is);
			is.close();
			LOG.info("Using mark59.properties found at " + mark59propertiesFile + ".");
			return true;
		} catch (Exception e) {
			try {is.close();} catch (Exception e1){}
			return false;
		}
	}

	private void setMark59property(String mark59PropertyKey) {
		setMark59property(mark59PropertyKey, false);
	}

	/**
	 * Properties in mark59.properties should already be loaded before this method is invoked. 
	 * Here, system properties can be used to override the mark59.properties, 
	 * and properties will be updated with substitutions when required.
	 * 
	 * @param mark59PropertyKey
	 * @param isExecutable
	 */
	private void setMark59property(String mark59PropertyKey, boolean isExecutable) {
		if (StringUtils.isNotEmpty(System.getProperty(mark59PropertyKey))) {
			properties.setProperty(mark59PropertyKey, System.getProperty(mark59PropertyKey));
			substitutePredfinedStringsIfNecessary(mark59PropertyKey, properties.getProperty(mark59PropertyKey),	isExecutable);
			LOG.info("    " + mark59PropertyKey + " has been set from System properties  : " + properties.getProperty(mark59PropertyKey));
		} else if (StringUtils.isNotEmpty(properties.getProperty(mark59PropertyKey))) {
			substitutePredfinedStringsIfNecessary(mark59PropertyKey, properties.getProperty(mark59PropertyKey),	isExecutable);
			LOG.info("    " + mark59PropertyKey + " has been set from " + MARK59_PROPERTIES + " : "	+ properties.getProperty(mark59PropertyKey));
		} else {
			LOG.info("    " + mark59PropertyKey + " has not been set. ");
		}
	}

	
	private void substitutePredfinedStringsIfNecessary(String mark59PropertyKey, String propertyValue,	boolean isExecutable) {
		
		if (propertyValue.contains("${mark59.runs}")) {
			if (os.equals(WINDOWS)) {
				propertyValue = propertyValue.replace("${mark59.runs}", System.getenv("SYSTEMDRIVE") + "/Mark59_Runs");
			} else { // nix or mac
				propertyValue = propertyValue.replace("${mark59.runs}",	System.getProperty("user.home") + "/Mark59_Runs");
			}
			properties.setProperty(mark59PropertyKey, propertyValue);
		}
		
		if (propertyValue.contains("${user.home}")) {
			propertyValue = propertyValue.replace("${user.home}", System.getProperty("user.home"));
			properties.setProperty(mark59PropertyKey, propertyValue);
		}

		if (isExecutable && os.equals(WINDOWS) && !propertyValue.trim().endsWith(".exe")) {
			properties.setProperty(mark59PropertyKey, propertyValue.trim() + ".exe");
		}
		if (isExecutable && !os.equals(WINDOWS) && propertyValue.trim().endsWith(".exe")) {  // nix or mac
			properties.setProperty(mark59PropertyKey, propertyValue.replace(".exe", "").trim());
		}
	}

	/**
	 * @param key property name
	 * @return property value
	 */
	public String getProperty(String key) {
		if (!properties.containsKey(key))
			return null;
		return properties.getProperty(key);
	}

	/**
	 * @return exist or otherwise new instance of PropertiesReader
	 * @throws IOException when attempting to read mark59.properties
	 */
	public static synchronized PropertiesReader getInstance() throws IOException {
		if (instance == null) {
			instance = new PropertiesReader();
		}
		return instance;
	}

}
