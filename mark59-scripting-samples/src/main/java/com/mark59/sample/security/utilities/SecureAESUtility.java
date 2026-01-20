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
package com.mark59.sample.security.utilities;

import com.mark59.core.utils.SecureAES;


/**
 * SecureAES replaces SimpleAES
 * 
 * <p>Use this utility to create a new encryption for a string 
 *   
 * <p>For details on migration from SimpleAES please refer
 * <br>https://github.com/mark-5-9/mark59/blob/master/docs/MIGRATION_SimpleAES_to_SecureAES.md 
 * 
 * <p><b>Key Management:</b></p>
 * The encryption key (salt) should be provided via environment variable or system property:
 * <pre>
 * Environment variable: MARK59_ENCRYPTION_KEY=your-secret-key-here
 * System property: -DMARK59_ENCRYPTION_KEY=your-secret-key-here
 * </pre>
 *
 * If no key is provided, a default key is used (NOT RECOMMENDED for production).
 *
 * @author Philip Webb
 * Written: Australian Summer 2025
 */
public class SecureAESUtility {

	/**
	 * Main method for encrypt/decrypt a string, and printing out a new encryption key 
	 */
	public static void main(String[] args) {
		String originalString = "My test string!";
		String encryptedString = SecureAES.encrypt(originalString);
		String decryptedString = SecureAES.decrypt(encryptedString);

		System.out.println(originalString);
		System.out.println(encryptedString);
		System.out.println(decryptedString);
		
		System.out.println();
		System.out.println();
		String newKey = SecureAES.generateSecureKey();
		System.out.println("Here's a newly generated encryption key"
				+ " you may choose to use :");
		System.out.println("------------------------------------------------");
		System.out.println(newKey);
		System.out.println("------------------------------------------------");		
		System.out.println();
	}

}