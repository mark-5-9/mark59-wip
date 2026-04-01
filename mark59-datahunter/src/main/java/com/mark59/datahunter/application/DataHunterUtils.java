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

package com.mark59.datahunter.application;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;



public final class DataHunterUtils  {

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 * This class contains only static methods and should not be instantiated.
	 */
	private DataHunterUtils() {
		throw new AssertionError("Utility class should not be instantiated");
	}

	public static void expireSession(HttpServletRequest httpServletRequest) {
		if (httpServletRequest != null){
			expireSession( httpServletRequest, 10);
		}
	}

	public static void expireSession(HttpServletRequest httpServletRequest, int intervalinSecs) {
		HttpSession httpSession = httpServletRequest.getSession(false);
		if (httpSession != null){
			httpSession.setMaxInactiveInterval(intervalinSecs);
		}
	}

	public static boolean isEmpty(final String s) {
		return s == null || s.trim().isEmpty();
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
				prettyOut+= "\n   | " + mapEntry.getKey() + " | " + mapEntry.getValue() + " | " ;
			}
	    } else {
			prettyOut+= "\n   |        empty or null map     | " ;
	    }
	    return prettyOut+= "\n    ------------------------------- \n";
	}


	/**
	 * Convenience method to print out a Map as HTML table.
	 *
	 * @param <K> Map entry key
	 * @param <V> Map entry value
	 * @param map map to pretty print (must contain only trusted data)
	 * @return http (table) formatted representation of the map
	 */
	public static <K,V> String prettyHttpPrintMap (final Map<K,V> map) {
	    StringBuilder prettyOut = new StringBuilder("<br>");

	    if (map != null && !map.isEmpty() ){
	    	prettyOut.append("<table>");
			for (Entry<K,V> mapEntry: map.entrySet()) {
				prettyOut.append("<tr><td>")
				         .append(htmlEscape(String.valueOf(mapEntry.getKey())))
				         .append("</td><td>:</td><td>")
				         .append(htmlEscape(String.valueOf(mapEntry.getValue())))
				         .append("</td></tr>");
			}
	    	prettyOut.append("</table>");
	    } else {
			prettyOut.append("<br> (no sql parameters) ");
	    }
	    return prettyOut.toString();
	}

	/**
	 * Escapes HTML special characters to prevent XSS attacks.
	 *
	 * @param text text to escape
	 * @return HTML-escaped text
	 */
	private static String htmlEscape(String text) {
		if (text == null) {
			return "";
		}
		return text.replace("&", "&amp;")
		           .replace("<", "&lt;")
		           .replace(">", "&gt;")
		           .replace("\"", "&quot;")
		           .replace("'", "&#39;");
	}


	/**
	 * returns the given list with empty values and white spaces trimmed off
	 *
	 * <p>EG: ", ,,cat 1, ,, mat 2, , ,," returns "cat 1,mat 2"
	 *
	 * @param commaDelimitedString a list of comma delimited strings (can be a single value)
	 * @return the commaDelimitedString but with whitespace stripped from the start and end of every string
	 */
	public static String commaDelimStringTrimAll(String commaDelimitedString) {
		String trimmedStrings = "";
		// note when an empty string is passed to the split, it creates a empty first element ...
		if (isNotBlank(commaDelimitedString)){
			String[] strippedStringAry = StringUtils.stripAll(StringUtils.split(commaDelimitedString, ","));
			for (String strippedString : strippedStringAry){
				if (isNotBlank(strippedString)){
					trimmedStrings = trimmedStrings + strippedString + ",";
				}
			}
			trimmedStrings = StringUtils.stripEnd(trimmedStrings, ",");
		}
		return trimmedStrings;
	}


	/**
	 * @param commaDelimitedString string with comma(",") being used as a field delimiter
	 * @return the Set of split strings
	 */
	public static Set<String> commaDelimStringToStringSet(String commaDelimitedString) {
		List<String> listOfStrings = new ArrayList<>();
		if ( isNotBlank(commaDelimitedString)){
			listOfStrings =  Arrays.asList(StringUtils.stripAll(StringUtils.split(commaDelimitedString, ",")));
		}
		return new HashSet<>(listOfStrings);
	}


	/**
	 * @param uri Parameter to encode
	 * @return encoded parameter
	 */
	public static String encode(String uriParm) {
		try {
			return URLEncoder.encode(nullToEmpty(uriParm), StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException("UnsupportedEncodingException using url : " + uriParm );
		}
	}


	private static String nullToEmpty(String str) {
		return null == str ? "" : str;
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
    
	
	
}