# Maven Central Deploy Setup — New Windows Laptop

For deploying `mark59-core`, `mark59-scripting`, and `mark59-datahunter-api` to Maven Central
using the Sonatype Central Portal (`central-publishing-maven-plugin`) with GPG signing.

---

## 1. Install JDK

Install JDK 21 (minimum JDK 17 — the compiler target in the poms).

Open-source JDK 17 (or 21) distributions available from:
- **[Adoptium Temurin](https://adoptium.net/)** — most popular, community-recommended default
- **[Microsoft Build of OpenJDK](https://www.microsoft.com/openjdk)** — good choice on Windows
- **[Amazon Corretto](https://aws.amazon.com/corretto/)** — free LTS distribution from AWS
- **[Azul Zulu](https://www.azul.com/downloads/)** — another popular free option
- **[Oracle OpenJDK](https://jdk.java.net/17/)** — Oracle's own open-source builds (separate from the commercial Oracle JDK)

Set environment variables:
- `JAVA_HOME` = path to JDK install (e.g. `C:\Program Files\Eclipse Adoptium\jdk-21...`)
- Add `%JAVA_HOME%\bin` to `PATH`

---

## 2. Install Eclipse / STS

Install your preferred version of Eclipse or Spring Tool Suite.

In **Window → Preferences → Java → Installed JREs**, register the new JDK and set it as default.

---

## 3. Install GPG (Gpg4win)

Download and install from https://gpg4win.org

This provides the `gpg` command used by `maven-gpg-plugin` during the release build.

---

## 4. Transfer Your Existing GPG Key

On your **old laptop**, export your private key:

```
gpg --export-secret-keys --armor mark59ptf@gmail.com > mark59-private-key.asc
```

Copy the file to the new laptop, then import it:

```
gpg --import mark59-private-key.asc
```

Verify it is present:

```
gpg --list-secret-keys
```

> **Alternative:** Generate a new GPG key on the new laptop, upload it to a keyserver
> (`gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>`), and register it
> in your Central Portal account.

---

## 5. Configure `~/.m2/settings.xml`

The poms use `<publishingServerId>central</publishingServerId>`, so the server id must be `central`.

The credentials are **token-based** — obtain them from
https://central.sonatype.com → **Account → Generate User Token**.

Create or update `C:\Users\<you>\.m2\settings.xml`:

```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username><!-- your Central Portal token username --></username>
      <password><!-- your Central Portal token password --></password>
    </server>
  </servers>

  <profiles>
    <profile>
      <id>release</id>
      <properties>
        <gpg.passphrase><!-- your GPG key passphrase --></gpg.passphrase>
      </properties>
    </profile>
  </profiles>
</settings>
```

> The `gpg.passphrase` property works here because the pom already sets
> `--pinentry-mode loopback`, allowing Maven to pass the passphrase non-interactively.
> Omit the property if you prefer to be prompted instead.

---

## 6. Configure Maven in Eclipse/STS

In **Window → Preferences → Maven → User Settings**, confirm it points to
`C:\Users\<you>\.m2\settings.xml`.

STS uses its embedded Maven by default — that is fine, or you can point it to
an external Maven install.

---

## 7. Verify GPG Signing (no upload)

Run this against one of the three deployable projects (e.g. `mark59-core`) to confirm
GPG is set up correctly. Nothing is uploaded.

**Goals:** `verify`  **Profiles:** `release`

Or with tests skipped for a faster check:

**Goals:** `verify -DskipTests`  **Profiles:** `release`

The `maven-gpg-plugin` is bound to the `verify` phase, so it will sign all artifacts
and fail loudly if the key or passphrase is wrong.

---

## 8. Test Central Portal Credentials (optional upload/drop)

To also verify the Central Portal token credentials, run a full deploy:

**Goals:** `deploy`  **Profiles:** `release`

Because `<autoPublish>` is commented out in the poms (defaults to `false`), this uploads
the bundle to the Central Portal staging area but **does not publish** it.

Log in to https://central.sonatype.com → **Deployments**, inspect the upload, and
**drop** it if it was only a credentials test — no artifacts will be publicly released.

---

## 9. Run the Real Deploy

Right-click on `mark59-core`, `mark59-scripting`, or `mark59-datahunter-api` in Eclipse/STS
→ **Run As → Maven build...**

- **Goals:** `clean deploy`
- **Profiles:** `release`

After a successful upload, log in to https://central.sonatype.com → **Deployments**
and click **Publish** to release the artifacts to Maven Central.

---

## Summary

| Step | What it checks | Uploads to Central? |
|---|---|---|
| `verify -P release` | GPG key/passphrase | No |
| `deploy -P release` | GPG + Central Portal credentials | Yes, but droppable |
| `clean deploy -P release` | Full release | Yes — publish manually to go live |
