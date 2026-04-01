/*
 *  Copyright 2019 Mark59.com
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mark59.core.utils.Mark59Constants.DatabaseTxnTypes;
import com.mark59.core.utils.Mark59Constants.JMeterFileDatatypes;

/**
 * General catch-all class for useful functions
 *
 *  @author Philip Webb
 *  Written: Australian Summer 2020
 */
public class Mark59Utils {

	private static final Logger LOG = LogManager.getLogger(Mark59Utils.class);

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 * This class contains only static methods and should not be instantiated.
	 */
	private Mark59Utils() {
		// Utility class - no instances should be created
	}

	/**
	 * Allows a map with additional keys or keys that already exist in a base map to be combined. Where the same key exists in
	 * both maps, the value in the override (additional entries) map will be used.
	 * <p>returned as Jmeter Arguments
	 *
	 * @param baseMap  Map of base key values
	 * @param overrideEntriesMap  additional or override key values
	 * @return jmeterArguments
	 */
	public static Arguments mergeMapWithAnOverrideMap(Map<String,String> baseMap, Map<String, String> overrideEntriesMap) {
		Arguments jmeterArguments = new Arguments();
		Map<String,String> baseMapMergedWithOverrideMap = new LinkedHashMap<>();

		// use the override map to change entries for existing base map values in the merge
		for (Map.Entry<String, String> baseEntry : baseMap.entrySet()) {
			if (overrideEntriesMap.containsKey(baseEntry.getKey())){
				baseMapMergedWithOverrideMap.put(baseEntry.getKey(), overrideEntriesMap.get(baseEntry.getKey()));
			} else {
				baseMapMergedWithOverrideMap.put(baseEntry.getKey(), baseMap.get(baseEntry.getKey()));
			}
		}

		// add entries to the merge for keys in the override map, but were not in the base map
		for (Map.Entry<String, String> overrideEntry : overrideEntriesMap.entrySet()) {
			if ( ! baseMap.containsKey(overrideEntry.getKey())){
				baseMapMergedWithOverrideMap.put(overrideEntry.getKey(), overrideEntriesMap.get(overrideEntry.getKey()));
			}
		}

		for (Map.Entry<String, String> parameter : baseMapMergedWithOverrideMap.entrySet()) {
			jmeterArguments.addArgument(parameter.getKey(), parameter.getValue());
		}

		if (LOG.isDebugEnabled()){LOG.debug("jmeter arguments at end of mergeMapWithAnOverrideMap : "
				+ Arrays.toString(jmeterArguments.getArgumentsAsMap().entrySet().toArray()));}
		return jmeterArguments;
	}


	/**
	 * Strings starting with 't', 'T', 'y', 'Y' are assumed to mean true,
	 * all other values will return false
	 *
	 * @param str  the string to be resolved to true or false
	 * @return boolean true or false
	 */
	public static boolean resolvesToTrue(final String str) {
		if (Mark59Utils.isBlank(str)) {	return false;}
		if (str.trim().toLowerCase().startsWith("t")) {return true;}
		return str.trim().toLowerCase().startsWith("y");
	}


	/**
	 * Convenience method to print out a Map
	 *
	 * @param <K> Map entry key
	 * @param <V> Map entry value
	 * @param map map to pretty print
	 * @return formatted string representation of the map
	 */
	public static <K,V> String prettyPrintMap (final Map<K,V> map) {
	    String prettyOut = "\n    ------------------------------- ";

	    if (map != null && !map.isEmpty() ){

			for (Entry<K,V> mapEntry: map.entrySet()) {
				prettyOut += "\n   | " + mapEntry.getKey() + " | " + mapEntry.getValue() + " | " ;
			}
	    } else {
			prettyOut+= "\n   |        empty or null map     | " ;
	    }
	    return prettyOut+= "\n    ------------------------------- \n";
	}



	/**
	 * Maps JMeter file 'dt' (datatype) column to the TXN_TYPE values use in the Mark59 database tables
	 *  <p><b>Mapping Summary  (JMeter file type 'maps to'  database TXN_TYPE) :</b>
	 *  <table>
	 *  <caption>__________________________________________________________________________________</caption>
  	 *	<tr><td>CPU_UTIL  </td><td> --&gt; </td><td>CPU_UTIL   		</td><td></td></tr>
  	 *	<tr><td>MEMORY    </td><td> --&gt; </td><td>MEMORY     		</td><td></td></tr>
  	 *	<tr><td>DATAPOINT </td><td> --&gt; </td><td>DATAPOINT  		</td><td></td></tr>
  	 *	<tr><td>CDP       </td><td> --&gt; </td><td>TRANSACTION		</td><td>but tagged as a DevTools (CDP) transaction *</td></tr>
  	 *	<tr><td>'' (blank)</td><td> --&gt; </td><td>TRANSACTION		</td><td>a standard transaction</td></tr>
  	 *	<tr><td>(unmapped)</td><td> --&gt; </td><td>TRANSACTION		</td><td>default catch-all</td></tr>
  	 *  </table>
  	 *  <p>*  A separate check needs to be done when processing JMeter files for CDP tagging
  	 *
	 * @see Mark59Constants.DatabaseTxnTypes
	 * @see Mark59Constants.JMeterFileDatatypes
	 * @param jmeterFileDatatype one of the possible datatype (dt) values on the JMeter results file
	 * @return DatabaseDatatypes (string value)
	 */
	public static String convertJMeterFileDatatypeToDbTxntype(String jmeterFileDatatype) {
		if ( JMeterFileDatatypes.TRANSACTION.getDatatypeText().equals(jmeterFileDatatype)){    //expected to map any blank to transaction
			return DatabaseTxnTypes.TRANSACTION.name();
		} else if ( JMeterFileDatatypes.CDP.getDatatypeText().equals(jmeterFileDatatype)){
			return DatabaseTxnTypes.TRANSACTION.name();
		} else if ( JMeterFileDatatypes.CPU_UTIL.getDatatypeText().equals(jmeterFileDatatype)){
			return DatabaseTxnTypes.CPU_UTIL.name();
		} else if ( JMeterFileDatatypes.MEMORY.getDatatypeText().equals(jmeterFileDatatype)){
			return DatabaseTxnTypes.MEMORY.name();
		} else if ( JMeterFileDatatypes.DATAPOINT.getDatatypeText().equals(jmeterFileDatatype)){
			return DatabaseTxnTypes.DATAPOINT.name();
		} else {
			return DatabaseTxnTypes.TRANSACTION.name();   // just assume its a transaction (so a 'PARENT' would become a transaction on the db)
		}
	}


	/**
	 * Constructs metric transaction names based on server id and rules (using data that can be obtained from commandResponseParser)
	 *
	 * <p>A key element of creating metric transaction ids is the mapping of Mark59 Metric Transaction Types
	 * as the transaction id prefix.  The table below summarizes the relationships.
	 *
	 *  <p><b>Mapping Summary  (Meter Transaction Type 'maps to' transaction id prefix)</b>
	 *  <table>
	 *  <caption>_________________________________</caption>
  	 *	<tr><td>CPU_UTIL </td><td> --&gt; </td><td>CPU_</td></tr>
  	 *	<tr><td>MEMORY   </td><td> --&gt; </td><td>Memory_</td></tr>
  	 *	<tr><td>DATAPOINT</td><td> --&gt; </td><td>no prefix</td></tr>
  	 *	</table>
	 *
	 * <p>Suffixes are added after the server name, if entered.
	 *
	 * <p>The general format is : <code>prefix + reported server id + suffix (if provided)</code>
	 *
	 * @param metricTxnType Metric Transaction Types as recorded in the database
	 * @param reportedServerId     Server Name as it will appear in the transaction
	 * @param metricNameSuffix     Optional suffix appended to the end of the transaction name
	 * @return the candidate transactionId to be used
	 */
	public static String constructCandidateTxnIdforMetric(String metricTxnType, String reportedServerId, String metricNameSuffix ) {
		String txnIdPrefix = "";
		if ( DatabaseTxnTypes.CPU_UTIL.name().equals(metricTxnType)){
			txnIdPrefix = "CPU_";
		} else if ( DatabaseTxnTypes.MEMORY.name().equals(metricTxnType)){
			txnIdPrefix = "Memory_";
		}

		String candidateTxnId = txnIdPrefix + reportedServerId;

		if 	(Mark59Utils.isNotBlank(metricNameSuffix)){
			candidateTxnId = candidateTxnId + "_" + metricNameSuffix;
		}
		return candidateTxnId;
	}


	/**
	 * return the Set of split strings
	 * @param commaDelimitedString string with comma(",") being used as a field delimiter
	 * @return Set of strings
	 */
	public static Set<String> commaDelimStringToStringSet(String commaDelimitedString) {
		List<String> listOfStrings = commaDelimStringToStringList(commaDelimitedString);
		return new HashSet<>(listOfStrings);
	}

	/**
	 * return the split list of strings
	 * @param commaDelimitedString string with comma(",") being used as a field delimiter
	 * @return list of strings
	 */
	public static List<String> commaDelimStringToStringList(String commaDelimitedString) {
		List<String> listOfStrings = new ArrayList<>();
		if ( Mark59Utils.isNotBlank(commaDelimitedString)){
			listOfStrings =  Arrays.asList(StringUtils.stripAll(StringUtils.split(commaDelimitedString, ",")));
		}
		return listOfStrings;
	}

	/**
	 * return the split list of strings
	 * @param pipeDelimitedString string with pipe char ("|") being used as a field delimiter
	 * @return list of strings
	 */
	public static List<String> pipeDelimStringToStringList(String pipeDelimitedString) {
		List<String> listOfStrings = new ArrayList<>();
		if ( Mark59Utils.isNotBlank(pipeDelimitedString)){
			listOfStrings =  Arrays.asList(StringUtils.stripAll(StringUtils.split(pipeDelimitedString, "|")));
		}
		return listOfStrings;
	}


    /**
     * Removes a substring only if it is at the end of a source string,
     * otherwise returns the source string.
     * <pre>
     * Mark59UtilsremoveEnd(null, *)      = null
     * Mark59Utils.removeEnd("", *)        = ""
     * Mark59Utils.removeEnd(*, null)      = *
     * Mark59Utils.removeEnd("www.domain.com", ".com.")  = "www.domain.com"
     * Mark59Utils.removeEnd("www.domain.com", ".com")   = "www.domain"
     * Mark59Utils.removeEnd("www.domain.com", "domain") = "www.domain.com"
     * Mark59Utils.removeEnd("abc", "")    = "abc"
     * </pre>
     *
     * @param str  the source String to search, may be null
     * @param remove  the String to search for and remove, may be null
     * @return the substring with the string removed if found, null if null String input
     */
    public static String removeEnd(final String str, final String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        if (str.endsWith(remove)) {
            return str.substring(0, str.length() - remove.length());
        }
        return str;
    }

    /**
     * Checks if a CharSequence is empty ("") or null.
     *
     * <pre>
     * Mark59Utils.isEmpty(null)      = true
     * Mark59Utils.isEmpty("")        = true
     * Mark59Utils.isEmpty(" ")       = false
     * Mark59Utils.isEmpty("bob")     = false
     * Mark59Utils.isEmpty("  bob  ") = false
     * </pre>
     *
     * @param cs  the CharSequence to check, may be null
     * @return true if the CharSequence is empty or null
     */
    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * Checks if a CharSequence is not empty ("") and not null.
     *
     * <pre>
     * Mark59Utils.isNotEmpty(null)      = false
     * Mark59Utils.isNotEmpty("")        = false
     * Mark59Utils.isNotEmpty(" ")       = true
     * Mark59Utils.isNotEmpty("bob")     = true
     * Mark59Utils.isNotEmpty("  bob  ") = true
     * </pre>
     *
     * @param cs  the CharSequence to check, may be null
     * @return true if the CharSequence is not empty and not null
     */
    public static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }

    /**
     * Checks if a CharSequence is empty (""), null or whitespace only.
     *
     * <pre>
     * Mark59Utils.isBlank(null)      = true
     * Mark59Utils.isBlank("")        = true
     * Mark59Utils.isBlank(" ")       = true
     * Mark59Utils.isBlank("bob")     = false
     * Mark59Utils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param cs  the CharSequence to check, may be null
     * @return true if the CharSequence is null, empty or whitespace only
     */
    public static boolean isBlank(final CharSequence cs) {
        if (cs == null || cs.length() == 0) {
            return true;
        }
        for (int i = 0; i < cs.length(); i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a CharSequence is not empty (""), not null and not whitespace only.
     *
     * <pre>
     * Mark59Utils.isNotBlank(null)      = false
     * Mark59Utils.isNotBlank("")        = false
     * Mark59Utils.isNotBlank(" ")       = false
     * Mark59Utils.isNotBlank("bob")     = true
     * Mark59Utils.isNotBlank("  bob  ") = true
     * </pre>
     *
     * @param cs  the CharSequence to check, may be null
     * @return true if the CharSequence is not empty and not null and not whitespace only
     */
    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * Checks if a CharSequence contains a search CharSequence (case-sensitive).
     * Returns false if either CharSequence is null.
     *
     * <pre>
     * Mark59Utils.contains(null, *)          = false
     * Mark59Utils.contains(*, null)          = false
     * Mark59Utils.contains("", "")           = true
     * Mark59Utils.contains("abc", "")        = true
     * Mark59Utils.contains("abc", "a")       = true
     * Mark59Utils.contains("abc", "z")       = false
     * Mark59Utils.contains("abc", "ab")      = true
     * Mark59Utils.contains("abc", "ABC")     = false
     * </pre>
     *
     * @param cs  the CharSequence to check, may be null
     * @param searchSeq  the CharSequence to find, may be null
     * @return true if the CharSequence contains the search CharSequence (case-sensitive), false if not or either is null
     */
    public static boolean contains(final CharSequence cs, final CharSequence searchSeq) {
        if (cs == null || searchSeq == null) {
            return false;
        }
        return cs.toString().contains(searchSeq.toString());
    }



    /**
     * <p>Note : this method has been taken directly from Commons string utils 
     * <p>Tests if the CharSequence contains only Unicode digits. A decimal point is not a Unicode digit and returns false.
     * <p>null will return false. An empty CharSequence (length()=0) will return false.
     * <p> Note that the method does not allow for a leading sign, either positive or negative. Also, if a String passes 
     * the numeric test, it may still generate a NumberFormatException when parsed by Integer.parseInt or Long.parseLong, 
     * e.g. if the value is outside the range for int or long respectively.
     * </p>
     *
     * <pre>
     * Mark59Utils.isNumeric(null)   = false
     * Mark59Utils.isNumeric("")     = false
     * Mark59Utils.isNumeric("  ")   = false
     * Mark59Utils.isNumeric("123")  = true
     * Mark59Utils.isNumeric("\u0967\u0968\u0969")  = true
     * Mark59Utils.isNumeric("12 3") = false
     * Mark59Utils.isNumeric("ab2c") = false
     * Mark59Utils.isNumeric("12-3") = false
     * Mark59Utils.isNumeric("12.3") = false
     * Mark59Utils.isNumeric("-123") = false
     * Mark59Utils.isNumeric("+123") = false
     * </pre>
     *
     * @param cs the CharSequence to check, may be null.
     * @return true if only contains digits, and is non-null.
     */
    public static boolean isNumeric(final CharSequence cs) {
        if (isEmpty(cs)) {
            return false;
        }
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isDigit(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    
    
	/**
	 * LINUX is returned as the default, other options are WINDOWS and MAC
	 * @return string
	 */
	public static String obtainOperatingSystemForLocalhost() {
		String operatingSystem = Mark59Constants.OS.LINUX.getOsName();

		String osNameProp = System.getProperty("os.name", Mark59Constants.OS.LINUX.getOsName())
				.toUpperCase(java.util.Locale.ENGLISH);

		if ( osNameProp.indexOf("WIN") >= 0 ){
			operatingSystem = Mark59Constants.OS.WINDOWS.getOsName();
		} else if ( osNameProp.indexOf("MAC") >= 0 ) {
			operatingSystem = Mark59Constants.OS.MAC.getOsName();
		}
		return operatingSystem;
	}

}
