# False Positive: XSS in 201 Created Response
**Scanner:** Qodana  
**Rule:** QodanaXss  
**Endpoint:** POST /item-types  
**File:** src/main/java/com/example/api/ItemTypeController.java  
**Last Reviewed:** 2026‑01‑13  
**Status:** Confirmed False Positive

---

## Summary
Qodana reports that the 201 Created response returns unsanitized user input.  
This is incorrect. All user-controlled fields are sanitized at three layers:

1. **Inbound request DTO** – sanitized before persistence
2. **Database entity** – sanitized after creation
3. **ApiResponse wrapper** – sanitized immediately before returning the 201 response

This breaks the tainted flow and satisfies OWASP XSS prevention rules.

---

## Evidence
### Scanner Evidence
Paste the scanner’s evidence block here (request, response, rule ID, trace).
- `SanitizerImpl: API-JDK/module-common-bc/src/main/java/com/financialplanner/modulecommonbc/sanitizer/SanitizerImpl.java`

### Code Evidence
Show the sanitization calls in:
- `ItemTypeDtoMapper: API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/mapper/ItemTypeDtoMapper.java`
- `ApiResponseFactory: API-JDK/module-api/src/main/java/com/financialplanner/moduleapi/response/ApiResponseFactory.java`


---

## Justification
Static analyzers treat 201 Created as a high‑risk sink because it often returns newly created user-controlled data.  
In this case, all data is sanitized before returning, so the warning is a false positive.

---

## Decision
This rule is suppressed **only** for this endpoint using:
- Inline suppression annotation
- Qodana rules file entry

---

## Review Plan
This suppression will be reviewed every 6 months or when:
- the sanitizer changes
- the response model changes
- the scanner updates its rule set  
