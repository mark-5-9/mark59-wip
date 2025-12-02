# Migration Guide: SimpleAES to SecureAES

## Overview

This guide helps you migrate from the insecure `SimpleAES` class to the secure `SecureAES` implementation.

## Security Improvements

| Feature | SimpleAES | SecureAES |
|---------|-----------|-----------|
| Encryption Mode | ECB (insecure) | GCM (secure, authenticated) |
| Key Derivation | SHA-1 truncated | PBKDF2-HMAC-SHA256 (100k iterations) |
| Encryption Key | Hardcoded static | Environment variable/configurable |
| Initialization Vector | None | Random per encryption |
| Salt | None | Random per encryption |
| Authentication | None | Built-in with GCM |
| Deterministic | Yes (same input = same output) | No (same input = different output) |
| Tampering Detection | None | Yes (authentication tag) |

## Migration Steps

### 1. Generate a Secure Encryption Key

Run the key generation utility:

```bash
# Using Java directly
java -cp mark59-core.jar com.mark59.core.utils.SecureAES genkey

# Or using Maven
cd mark59-core
mvn exec:java -Dexec.mainClass="com.mark59.core.utils.SecureAES" -Dexec.args="genkey"
```

This will output something like:
```
Generated new encryption key:
xJ3k9mPqR2vN8yB5wE7tF4sL6hG1jD0a==

Set this as environment variable:
export MARK59_ENCRYPTION_KEY="xJ3k9mPqR2vN8yB5wE7tF4sL6hG1jD0a=="

Or as system property:
-DMARK59_ENCRYPTION_KEY="xJ3k9mPqR2vN8yB5wE7tF4sL6hG1jD0a=="
```

**IMPORTANT**: Store this key securely (e.g., in a secrets manager, environment variable, or secure configuration).

### 2. Set the Encryption Key

Choose one method:

#### Option A: Environment Variable (Recommended)

Linux/Mac:
```bash
export MARK59_ENCRYPTION_KEY="your-generated-key-here"
```

Windows (PowerShell):
```powershell
$env:MARK59_ENCRYPTION_KEY="your-generated-key-here"
```

Windows (CMD):
```cmd
set MARK59_ENCRYPTION_KEY=your-generated-key-here
```

#### Option B: System Property

Add to JVM startup parameters:
```bash
-DMARK59_ENCRYPTION_KEY="your-generated-key-here"
```

#### Option C: Application Server Configuration

For Tomcat, add to `setenv.sh` or `setenv.bat`:
```bash
JAVA_OPTS="$JAVA_OPTS -DMARK59_ENCRYPTION_KEY=your-generated-key-here"
```

### 3. Update Code References

Replace all uses of `SimpleAES` with `SecureAES`:

**Before:**
```java
import com.mark59.core.utils.SimpleAES;

String encrypted = SimpleAES.encrypt("myPassword");
String decrypted = SimpleAES.decrypt(encrypted);
```

**After:**
```java
import com.mark59.core.utils.SecureAES;

String encrypted = SecureAES.encrypt("myPassword");
String decrypted = SecureAES.decrypt(encrypted);
```

### 4. Re-encrypt Existing Encrypted Values

**CRITICAL**: Values encrypted with `SimpleAES` cannot be decrypted by `SecureAES` and vice versa.

You must:

1. **Decrypt** all existing encrypted values using `SimpleAES`
2. **Re-encrypt** them using `SecureAES`
3. **Update** them in your database/configuration

#### Example Migration Script

```java
import com.mark59.core.utils.SimpleAES;
import com.mark59.core.utils.SecureAES;

public class MigrationHelper {

    public static String migrateEncryptedValue(String oldEncrypted) {
        try {
            // Decrypt with old method
            String plaintext = SimpleAES.decrypt(oldEncrypted);

            // Re-encrypt with new method
            String newEncrypted = SecureAES.encrypt(plaintext);

            return newEncrypted;
        } catch (Exception e) {
            throw new RuntimeException("Failed to migrate encrypted value", e);
        }
    }

    public static void main(String[] args) {
        // Example: migrate a password
        String oldEncrypted = "abc123..."; // Your old encrypted value
        String newEncrypted = migrateEncryptedValue(oldEncrypted);

        System.out.println("Old: " + oldEncrypted);
        System.out.println("New: " + newEncrypted);

        // Update this value in your database/configuration
    }
}
```

### 5. Files to Update

Based on the codebase scan, update these files:

1. **mark59-trends-load/src/main/java/com/mark59/trends/load/TrendsLoad.java**
   - Lines 299-300: Database password decryption

2. **mark59-metrics-common/src/main/java/com/mark59/metrics/utils/MetricsUtils.java**
   - Line 122: Server profile password decryption

3. **mark59-metrics/src/main/java/com/mark59/metrics/controller/ServerMetricRestController.java**
   - Line 123: Password encryption

### 6. Database Updates

If you have encrypted passwords stored in the database:

```sql
-- Example: Update server profiles table
-- First, create a backup!
CREATE TABLE server_profile_backup AS SELECT * FROM server_profile;

-- Then use a migration script to decrypt (SimpleAES) and re-encrypt (SecureAES)
-- This cannot be done directly in SQL - use Java application code
```

### 7. Testing

After migration, test thoroughly:

1. **Encrypt a test value** with `SecureAES`
2. **Verify decryption** works correctly
3. **Test different values** produce different encrypted outputs
4. **Test database connectivity** with migrated passwords
5. **Test server profile authentication** with migrated credentials

## Rollback Plan

If you need to rollback:

1. Keep `SimpleAES` class in codebase (don't delete yet)
2. Have old encrypted values backed up
3. Can revert code changes and restore old encrypted values

## Production Deployment Checklist

- [ ] Encryption key generated and stored securely
- [ ] Environment variable or system property configured
- [ ] All code references updated from SimpleAES to SecureAES
- [ ] All encrypted values migrated and tested
- [ ] Database backups created
- [ ] Rollback plan documented and tested
- [ ] Team trained on new key management procedures
- [ ] Monitoring in place for decryption errors

## Key Management Best Practices

1. **Never commit the encryption key to version control**
2. **Use different keys for different environments** (dev, staging, production)
3. **Rotate keys periodically** (e.g., annually)
4. **Store keys in a secrets manager** (AWS Secrets Manager, Azure Key Vault, HashiCorp Vault)
5. **Limit access** to the encryption key (principle of least privilege)
6. **Audit key access** and usage
7. **Have a key rotation procedure** documented

## Troubleshooting

### "No encryption key set" warning

**Cause**: `MARK59_ENCRYPTION_KEY` not set

**Solution**: Set the environment variable or system property

### "Decryption failed" error

**Possible causes**:
1. Wrong encryption key
2. Corrupted encrypted data
3. Trying to decrypt SimpleAES data with SecureAES (or vice versa)

**Solution**: Verify key is correct and data was encrypted with SecureAES

### Different encrypted values for same input

**This is expected behavior!** SecureAES is non-deterministic - each encryption produces a different output even for the same input. This is a security feature, not a bug.

## Benefits of SecureAES

1. **Authenticated Encryption** - Detects if data has been tampered with
2. **Proper Key Derivation** - Uses industry-standard PBKDF2
3. **Random IVs** - Each encryption is unique
4. **Configurable Keys** - Different keys per environment
5. **Modern Algorithms** - AES-256-GCM is NIST approved
6. **Non-deterministic** - Prevents pattern analysis attacks

## Questions?

For additional help:
- Review the SecureAES.java source code documentation
- Check OWASP cryptographic guidelines
- Consult with your security team
