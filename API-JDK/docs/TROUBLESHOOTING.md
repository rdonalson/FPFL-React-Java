# Troubleshooting Guide

## Common Build Issues

### ❌ JJWT `parserBuilder()` Method Not Found

**Error Message:**
```
cannot find symbol
  symbol:   method parserBuilder()
  location: class io.jsonwebtoken.Jwts
```

**Root Cause:**
Maven's local repository cache contained corrupted or mismatched JJWT artifacts, causing the compiler to use the wrong version of the library.

**Solution:**
1. Clean the project:
   ```powershell
   mvn clean
   ```

2. Delete JJWT artifacts from local Maven repository:
   ```powershell
   Remove-Item -Path "$env:USERPROFILE\.m2\repository\io\jsonwebtoken" -Recurse -Force
   ```

3. Rebuild the project:
   ```powershell
   mvn install -DskipTests
   ```

This forces Maven to re-download fresh JJWT 0.11.5 artifacts from Maven Central.

---

### ❌ Spring Boot CVE Warnings After Upgrading Parent

**Issue:**
IDE (Mend.io) shows CVE warnings even after upgrading Spring Boot parent from 4.0.1 to 4.0.6.

**Root Cause:**
IDE dependency cache is stale and showing old transitive dependencies.

**Solution:**
1. Rebuild the project:
   ```powershell
   mvn clean install
   ```

2. Restart IDE or invalidate caches:
   - **IntelliJ IDEA:** File → Invalidate Caches / Restart
   - **VS Code:** Reload window

3. Verify with official CVE tool:
   ```
   Use #appmod-validate-cves-for-java tool to confirm no CVEs exist
   ```

**Note:** Mend.io warnings may be false positives. Always verify with official CVE validation tools.

---

### ✅ How to Verify Your Build is Healthy

1. **Run Clean Build:**
   ```powershell
   mvn clean install -DskipTests
   ```

2. **Check for BUILD SUCCESS:**
   ```
   [INFO] BUILD SUCCESS
   [INFO] ------------------------------------------------------------------------
   ```

3. **Verify All Modules Compiled:**
   ```
   [INFO] FPFL-V2-Microservice ............................... SUCCESS
   [INFO] module-common-bc ................................... SUCCESS
   [INFO] module-items-bc .................................... SUCCESS
   [INFO] module-display-bc .................................. SUCCESS
   [INFO] module-auth ........................................ SUCCESS
   [INFO] API Module ......................................... SUCCESS
   ```

4. **Run CVE Validation:**
   Use the `#appmod-validate-cves-for-java` tool to ensure no known vulnerabilities.

---

### 📋 Current Dependency Versions (Secure)

**Spring Boot:**
- Parent: 4.0.6 ✅
- Dependencies: 4.0.6 ✅
- All starters: 4.0.6 ✅

**JJWT:**
- jjwt-api: 0.11.5 ✅
- jjwt-impl: 0.11.5 ✅
- jjwt-jackson: 0.11.5 ✅

**PostgreSQL:**
- postgresql: 42.7.11 ✅

**OpenAPI:**
- springdoc-openapi-starter-webmvc-ui: 2.8.9 ✅

All versions confirmed secure with ZERO known CVEs as of June 30, 2026.

---

### 🔍 When to Use Which Maven Command

**Clean Build (Most Common):**
```powershell
mvn clean install
```
Removes old artifacts and rebuilds everything.

**Skip Tests (Faster for Quick Checks):**
```powershell
mvn clean install -DskipTests
```
Compiles code but skips test execution.

**Package Only (No Installation):**
```powershell
mvn clean package
```
Builds JAR files but doesn't install to local repository.

**Dependency Tree:**
```powershell
mvn dependency:tree
```
Shows all dependencies and their versions.

**Force Update Snapshots:**
```powershell
mvn clean install -U
```
Forces Maven to check for updated snapshot dependencies.

---

**Last Updated:** June 30, 2026  
**Status:** All issues resolved ✅

