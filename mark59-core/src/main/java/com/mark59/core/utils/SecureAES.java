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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Secure AES encryption utility using AES-256-GCM mode with proper key derivation.
 * This implementation provides authenticated encryption with the following security features:
 * <ul>
 *   <li>AES-256-GCM mode (Galois/Counter Mode) for authenticated encryption</li>
 *   <li>PBKDF2 with SHA-256 for proper key derivation from passwords</li>
 *   <li>Random IV (Initialization Vector) for each encryption operation</li>
 *   <li>Random salt for key derivation</li>
 *   <li>Authentication tag to prevent tampering</li>
 *   <li>Non-deterministic encryption (same plaintext produces different ciphertexts)</li>
 * </ul>
 *
 * <p><b>Migration from SimpleAES (prior version of mark59's encryption) :</b></p>
 * <ol>
 *   <li>Set environment variable or system property: MARK59_ENCRYPTION_KEY</li>
 *   <li>Replace SimpleAES.encrypt() with SecureAES.encrypt()</li>
 *   <li>Replace SimpleAES.decrypt() with SecureAES.decrypt()</li>
 *   <li>Re-encrypt all existing encrypted values (they are not compatible)</li>
 * </ol>
 *
 * <p><b>Key Management:</b></p>
 * The encryption key should be provided via environment variable or system property:
 * <pre>
 * Environment variable: MARK59_ENCRYPTION_KEY=your-secret-key-here
 * System property: -DMARK59_ENCRYPTION_KEY=your-secret-key-here
 * </pre>
 *
 * If no key is provided, a default key is used (NOT RECOMMENDED for production).
 *
 * @author Philip Webb
 * Written: Australian Spring 2025
 */
public class SecureAES {

	private static final Logger LOG = LogManager.getLogger(SecureAES.class);

	// Algorithm constants
	private static final String ALGORITHM = "AES";
	private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
	private static final int GCM_TAG_LENGTH = 128; // bits
	private static final int GCM_IV_LENGTH = 12; // bytes (96 bits recommended for GCM)
	private static final int SALT_LENGTH = 16; // bytes

	// Key derivation constants
	private static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256";
	private static final int KEY_LENGTH = 256; // bits
	private static final int PBKDF2_ITERATIONS = 100000; // OWASP recommended minimum

	// Encryption key - should be set via environment variable or system property
	private static final String ENCRYPTION_KEY_ENV = "MARK59_ENCRYPTION_KEY";
	private static final String DEFAULT_KEY = "__Mark59.com____Default__Key__"; // Fallback only

	private static final String ENCRYPTION_KEY_NOT_SET_WARNING_MSG = "\nNo encryption key set via environment variable or system property '"
			+ ENCRYPTION_KEY_ENV + "'. Using default key. This is not secure for a Production Environment.\n";

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private static volatile boolean PRINTED_ONCE = false;

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 * This class contains only static methods and should not be instantiated.
	 */
	private SecureAES() {
		// Utility class - no instances should be created
	}

	/**
	 * Encrypts a string using AES-256-GCM with PBKDF2 key derivation.
	 * Each encryption uses a random IV and salt, making the output non-deterministic.
	 *
	 * <p>Output format (Base64 encoded): [salt(16 bytes)][iv(12 bytes)][ciphertext + auth tag]</p>
	 *
	 * @param plaintext the string to encrypt (must not be null)
	 * @return Base64 encoded encrypted data including salt, IV, and ciphertext with authentication tag
	 * @throws IllegalArgumentException if plaintext is null
	 * @throws RuntimeException if encryption fails
	 */
	public static String encrypt(String plaintext) {
		if (plaintext == null) {
			throw new IllegalArgumentException("Plaintext cannot be null");
		}

		try {
			// Generate random salt for key derivation
			byte[] salt = new byte[SALT_LENGTH];
			SECURE_RANDOM.nextBytes(salt);

			// Generate random IV for this encryption
			byte[] iv = new byte[GCM_IV_LENGTH];
			SECURE_RANDOM.nextBytes(iv);

			// Derive encryption key from password
			SecretKey secretKey = deriveKey(getEncryptionKey(), salt);

			// Initialize cipher in GCM mode
			Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
			GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

			// Encrypt the plaintext
			byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

			// Combine salt + IV + ciphertext into single byte array
			ByteBuffer byteBuffer = ByteBuffer.allocate(salt.length + iv.length + ciphertext.length);
			byteBuffer.put(salt);
			byteBuffer.put(iv);
			byteBuffer.put(ciphertext);

			// Return Base64 encoded result
			return Base64.getEncoder().encodeToString(byteBuffer.array());

		} catch (Exception e) {
			LOG.error("Error while encrypting: " + e.toString(), e);
			throw new RuntimeException("Encryption failed", e);
		}
	}

	/**
	 * Decrypts a string that was encrypted using the encrypt() method.
	 *
	 * @param encryptedData Base64 encoded encrypted data (salt + IV + ciphertext + auth tag)
	 * @return decrypted plaintext string
	 * @throws IllegalArgumentException if encryptedData is null or invalid
	 * @throws RuntimeException if decryption fails or authentication tag verification fails
	 */
	public static String decrypt(String encryptedData) {
		if (encryptedData == null || encryptedData.trim().isEmpty()) {
			throw new IllegalArgumentException("Encrypted data cannot be null or empty");
		}

		try {
			// Decode Base64
			byte[] decoded = Base64.getDecoder().decode(encryptedData);

			// Validate minimum length (salt + IV + at least some ciphertext + tag)
			int minLength = SALT_LENGTH + GCM_IV_LENGTH + GCM_TAG_LENGTH / 8;
			if (decoded.length < minLength) {
				throw new IllegalArgumentException("Invalid encrypted data: too short");
			}

			// Extract salt, IV, and ciphertext
			ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);

			byte[] salt = new byte[SALT_LENGTH];
			byteBuffer.get(salt);

			byte[] iv = new byte[GCM_IV_LENGTH];
			byteBuffer.get(iv);

			byte[] ciphertext = new byte[byteBuffer.remaining()];
			byteBuffer.get(ciphertext);

			// Derive the same key using the extracted salt
			SecretKey secretKey = deriveKey(getEncryptionKey(), salt);

			// Initialize cipher for decryption
			Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
			GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

			// Decrypt and verify authentication tag
			byte[] plaintext = cipher.doFinal(ciphertext);

			return new String(plaintext, StandardCharsets.UTF_8);

		} catch (Exception e) {
			LOG.error("Error while decrypting: " + e.toString(), e);
			throw new RuntimeException("Decryption failed: " + e.getMessage(), e);
		}
	}

	/**
	 * Derives a cryptographic key from a password using PBKDF2 with SHA-256.
	 *
	 * @param password the password to derive the key from
	 * @param salt random salt for key derivation
	 * @return derived SecretKey suitable for AES encryption
	 * @throws NoSuchAlgorithmException if PBKDF2WithHmacSHA256 is not available
	 * @throws InvalidKeySpecException if key derivation fails
	 */
	private static SecretKey deriveKey(String password, byte[] salt)
			throws NoSuchAlgorithmException, InvalidKeySpecException {

		KeySpec spec = new PBEKeySpec(
			password.toCharArray(),
			salt,
			PBKDF2_ITERATIONS,
			KEY_LENGTH
		);

		SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
		byte[] keyBytes = factory.generateSecret(spec).getEncoded();

		return new SecretKeySpec(keyBytes, ALGORITHM);
	}

	/**
	 * Retrieves the encryption key from environment variable or system property.
	 * Falls back to default key if not set (not recommended for production).
	 *
	 * @return encryption key
	 */
	private static String getEncryptionKey() {
		// Try environment variable first
		String key = System.getenv(ENCRYPTION_KEY_ENV);

		// Fall back to system property
		if (key == null || key.trim().isEmpty()) {
			key = System.getProperty(ENCRYPTION_KEY_ENV);
		}

		// Fall back to default (log warning).  Warnings will only be printed out once per JVM, to prevent
		// logs being swamped during a full JMeter performance test.
		if (key == null || key.trim().isEmpty()) {
			if (! PRINTED_ONCE) {
				LOG.warn(ENCRYPTION_KEY_NOT_SET_WARNING_MSG);
				System.out.println(ENCRYPTION_KEY_NOT_SET_WARNING_MSG);
				PRINTED_ONCE = true;
			}
			return DEFAULT_KEY;
		}
		return key;
	}

	/**
	 * Generates a cryptographically secure random key suitable for use as MARK59_ENCRYPTION_KEY.
	 * The generated key is 32 characters long (sufficient for AES-256).
	 *
	 * @return Base64 encoded random key
	 */
	public static String generateSecureKey() {
		byte[] keyBytes = new byte[32]; // 256 bits
		SECURE_RANDOM.nextBytes(keyBytes);
		return Base64.getEncoder().encodeToString(keyBytes);
	}

	/**
	 * Main method for testing and key generation.
	 *
	 * Usage:
	 * <pre>
	 * # Test encryption/decryption
	 * java SecureAES test "My secret text"
	 *
	 * # Generate a new encryption key
	 * java SecureAES genkey
	 * </pre>
	 *
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		if (args.length > 0 && "genkey".equalsIgnoreCase(args[0])) {
			// Generate a new encryption key
			String newKey = generateSecureKey();
			System.out.println("Generated new encryption key:");
			System.out.println(newKey);
			System.out.println();
			System.out.println("Set this as environment variable:");
			System.out.println("export MARK59_ENCRYPTION_KEY=\"" + newKey + "\"");
			System.out.println();
			System.out.println("Or as system property:");
			System.out.println("-DMARK59_ENCRYPTION_KEY=\"" + newKey + "\"");
			return;
		}

		// Test encryption/decryption
		String originalString = args.length > 0 && !"genkey".equalsIgnoreCase(args[0]) ? args[0] : "My test string!";

		System.out.println("Original:  " + originalString);

		// Encrypt
		String encrypted1 = SecureAES.encrypt(originalString);
		System.out.println("Encrypted: " + encrypted1);

		// Decrypt
		String decrypted = SecureAES.decrypt(encrypted1);
		System.out.println("Decrypted: " + decrypted);

		// Demonstrate non-deterministic encryption
		String encrypted2 = SecureAES.encrypt(originalString);
		System.out.println();
		System.out.println("Second encryption of same text (should be different):");
		System.out.println("Encrypted: " + encrypted2);
		System.out.println("Decrypted: " + SecureAES.decrypt(encrypted2));

		System.out.println();
		System.out.println("Match: " + decrypted.equals(originalString));
		System.out.println("Encrypted values differ: " + !encrypted1.equals(encrypted2));

		// Warning message should not be repeated
		String encrypted3 = SecureAES.encrypt(originalString);
		System.out.println();
		System.out.println("Third encryption of same text (warning message should not be repeated):");
		System.out.println("Encrypted: " + encrypted3);
		System.out.println("Decrypted: " + SecureAES.decrypt(encrypted2));

		// Decrypt should work repeatedly on same encrypted string (only if using the same MARK59_ENCRYPTION_KEY! )
		System.out.println();
		System.out.println("Using previous encryption from above:");
		System.out.println("Encrypted: " + encrypted2);

		System.out.println();
		System.out.println("Match: " + decrypted.equals(originalString));

		
		System.out.println("_______________");
		System.out.println("Demo create key");
		System.out.println();		
		String[] argsGenkey = {"genkey"} ;
		SecureAES.main(argsGenkey); 
    }		

}
