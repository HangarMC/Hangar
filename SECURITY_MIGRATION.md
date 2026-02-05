# Spring Security Method Security Migration

## Overview

This document describes the migration from Spring Security's deprecated `AccessDecisionManager`/`AccessDecisionVoter` pattern to the new `AuthorizationManager` API, as recommended by Spring Security 5.8+.

## Problem Statement

The Hangar project was using the old and deprecated method security approach:
- `GlobalMethodSecurityConfiguration` with `@EnableGlobalMethodSecurity`
- Custom `AccessDecisionVoter` implementations for authorization
- `AccessDecisionManager` for managing voter decisions
- `AnnotationMetadataExtractor` for extracting annotation metadata

Spring Security 5.8+ deprecated these classes in favor of:
- `@EnableMethodSecurity` annotation
- `AuthorizationManager<MethodInvocation>` interface
- Direct Spring AOP integration

## Migration Summary

### What Was Replaced

#### Old Infrastructure (Removed):
1. **HangarUnanimousBased** - Custom `AccessDecisionManager` that passed all config attributes to voters at once
2. **HangarMetadataSources** - Custom metadata source that collected annotations from methods and classes
3. **HangarDecisionVoter<A>** - Abstract base class for all custom voters
4. **Metadata Extractors** (6 classes) - Extracted annotation metadata into `ConfigAttribute` objects
5. **Voter Implementations** (6 classes) - Made authorization decisions based on annotations

#### Custom Voters Replaced:
1. **PermissionRequiredVoter** → `PermissionRequiredAuthorizationManager`
2. **PrivilegedVoter** → `PrivilegedAuthorizationManager`
3. **UnlockedVoter** → `UnlockedAuthorizationManager`
4. **AalUnlockedVoter** → `AalAuthorizationManager`
5. **CurrentUserVoter** → `CurrentUserAuthorizationManager`
6. **VisibilityRequiredVoter** → `VisibilityRequiredAuthorizationManager`

### New Infrastructure (Created):

#### Core Classes:
1. **HangarAuthorizationManager** - Base class for all custom authorization managers
   - Provides helper methods for granted/denied decisions
   - Implements `AuthorizationManager<MethodInvocation>`

2. **HangarUnanimousAuthorizationManager** - Composite authorization manager
   - Chains multiple authorization managers
   - Requires unanimous approval (any denial results in overall denial)
   - Returns granted if at least one manager grants access
   - Abstain pattern: managers return `null` if annotation not present

#### Authorization Managers:

Each manager:
- Checks if its annotation is present on the method/class
- Returns `null` (abstain) if annotation not present
- Evaluates SpEL expressions where needed
- Makes authorization decisions
- Throws appropriate `HangarApiException` on denial

**1. PermissionRequiredAuthorizationManager**
- Handles `@PermissionRequired` annotation
- Supports PROJECT, ORGANIZATION, and GLOBAL permission types
- Evaluates SpEL expressions for permission scope arguments
- Throws `HangarApiException(HttpStatus.NOT_FOUND)` on denial (intentional 404 for security)

**2. PrivilegedAuthorizationManager**
- Handles `@Privileged` annotation
- Requires user to be logged in AND unlocked
- Returns denied for non-`HangarAuthenticationToken`
- Throws `HangarApiException(HttpStatus.UNAUTHORIZED, "error.privileged")`

**3. UnlockedAuthorizationManager**
- Handles `@Unlocked` annotation
- Requires user account to not be locked
- Returns denied for non-`HangarAuthenticationToken`
- Throws `HangarApiException(HttpStatus.UNAUTHORIZED, "error.userLocked")`

**4. AalAuthorizationManager**
- Handles `@RequireAal` annotation
- Requires minimum Account Authentication Level (AAL)
- Returns denied for non-`HangarAuthenticationToken`
- Throws `HangarApiException(HttpStatus.UNAUTHORIZED, "error.aal1"/"error.aal2")`

**5. CurrentUserAuthorizationManager**
- Handles `@CurrentUser` annotation
- Verifies user is accessing their own resource
- Allows global permission `EditAllUserSettings` to bypass
- Evaluates SpEL expression to extract username
- Throws `HangarApiException.forbidden()` on denial

**6. VisibilityRequiredAuthorizationManager**
- Handles `@VisibilityRequired` annotation
- Checks PROJECT and VERSION visibility
- Evaluates SpEL expression for arguments
- Throws `HangarApiException.notFound()` on denial

### Configuration Changes:

#### MethodSecurityConfig.java:

**Before:**
```java
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
    
    @Override
    protected MethodSecurityMetadataSource customMethodSecurityMetadataSource() {
        return new HangarMetadataSources(this.annotationMetadataExtractors);
    }
    
    @Override
    protected AccessDecisionManager accessDecisionManager() {
        // Create voters and decision manager
    }
}
```

**After:**
```java
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class MethodSecurityConfig {
    
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public Advisor customAuthorizationMethodInterceptor(
            @Autowired List<HangarAuthorizationManager> authorizationManagers) {
        
        AuthorizationManager<MethodInvocation> authorizationManager = 
            new HangarUnanimousAuthorizationManager(authorizationManagers);
        
        return AuthorizationManagerBeforeMethodInterceptor.preAuthorize(authorizationManager);
    }
}
```

## Key Design Decisions

### 1. Abstain Pattern
Authorization managers return `null` when their annotation is not present, allowing other managers to make the decision. This maintains the original voter chain behavior.

### 2. Exception-Based Denial
Rather than just returning denied decisions, managers throw specific `HangarApiException` instances. This preserves the original behavior where voters could throw exceptions in `onAccessDenied()` callbacks.

### 3. Unanimous Voting
`HangarUnanimousAuthorizationManager` requires all managers to either grant or abstain. Any denial results in immediate overall denial, matching the original `HangarUnanimousBased` behavior.

### 4. Annotation Preservation
All custom annotation classes (`@PermissionRequired`, `@Privileged`, etc.) are preserved unchanged. Controllers and services require no modifications.

### 5. Direct Annotation Lookup
Instead of metadata extractors, the new managers directly check for annotations using `AnnotationUtils.findAnnotation()`. This is simpler and more aligned with Spring Security 6's design.

### 6. Co-located Organization
Authorization managers are placed in the same packages as their corresponding annotations for better organization and maintainability. This keeps related code together.

### 7. Automatic Discovery
All `HangarAuthorizationManager` beans are automatically discovered and wired into the configuration. Adding new authorization managers requires no configuration changes.

## Behavioral Equivalence

The migration maintains exact behavioral equivalence with the original implementation:

| Original Voter Return | New Manager Return | Effect |
|----------------------|-------------------|---------|
| `ACCESS_GRANTED` | `AuthorizationDecision(true)` | Access granted |
| `ACCESS_DENIED` | `AuthorizationDecision(false)` or throw exception | Access denied |
| `ACCESS_ABSTAIN` | `null` | Abstain, let other managers decide |

Exception throwing behavior is preserved:
- `PermissionRequiredVoter` threw `HangarApiException(NOT_FOUND)` → Same in `PermissionRequiredAuthorizationManager`
- `CurrentUserVoter.onAccessDenied()` threw `HangarApiException.forbidden()` → Same in `CurrentUserAuthorizationManager`
- `VisibilityRequiredVoter.onAccessDenied()` threw `HangarApiException.notFound()` → Same in `VisibilityRequiredAuthorizationManager`

## Testing

### Validation Performed:
1. ✅ Code review - No issues found
2. ✅ CodeQL security scan - No alerts found
3. ✅ Logic verification - All authorization managers match original voter behavior
4. ✅ Annotation verification - All custom annotations preserved

### What Existing Tests Should Cover:
- Method security annotations on controllers
- Permission checks for different user types
- Global, project, and organization permission scopes
- Locked/unlocked user account handling
- AAL (Account Authentication Level) requirements
- Current user verification
- Project and version visibility checks

## Benefits of Migration

1. **Modern API**: Uses Spring Security 6's recommended approach
2. **No Deprecation Warnings**: Removed all deprecated class usage
3. **Cleaner Code**: Simpler design without metadata extractors
4. **Better Integration**: Direct Spring AOP integration
5. **Future-Proof**: Aligned with Spring Security's evolution
6. **Maintainability**: Easier to understand and modify
7. **Type Safety**: Stronger compile-time checking

## Rollback Plan (If Needed)

If issues are discovered, the old implementation can be restored from git history:
1. Revert the commits that removed voter classes
2. Revert the MethodSecurityConfig changes
3. The annotation classes remain unchanged, so no controller changes needed

## References

- Spring Security 6 Method Security: https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html
- AuthorizationManager API: https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/authorization/AuthorizationManager.html
- Migration Guide: https://docs.spring.io/spring-security/reference/migration/index.html

## Files Changed

### Added:
- `security/authorization/HangarAuthorizationManager.java` - Base class for all authorization managers
- `security/authorization/HangarUnanimousAuthorizationManager.java` - Composite manager
- `security/annotations/permission/PermissionRequiredAuthorizationManager.java` - Co-located with @PermissionRequired
- `security/annotations/privileged/PrivilegedAuthorizationManager.java` - Co-located with @Privileged
- `security/annotations/unlocked/UnlockedAuthorizationManager.java` - Co-located with @Unlocked
- `security/annotations/aal/AalAuthorizationManager.java` - Co-located with @RequireAal
- `security/annotations/currentuser/CurrentUserAuthorizationManager.java` - Co-located with @CurrentUser
- `security/annotations/visibility/VisibilityRequiredAuthorizationManager.java` - Co-located with @VisibilityRequired

### Modified:
- `security/configs/MethodSecurityConfig.java` - Now autowires all HangarAuthorizationManager beans instead of hardcoding

### Removed:
- `security/HangarUnanimousBased.java`
- `security/HangarMetadataSources.java`
- `security/annotations/HangarDecisionVoter.java`
- `security/annotations/aal/AalUnlockedVoter.java`
- `security/annotations/aal/AalMetadataExtractor.java`
- `security/annotations/currentuser/CurrentUserVoter.java`
- `security/annotations/currentuser/CurrentUserMetadataExtractor.java`
- `security/annotations/permission/PermissionRequiredVoter.java`
- `security/annotations/permission/PermissionRequiredMetadataExtractor.java`
- `security/annotations/privileged/PrivilegedVoter.java`
- `security/annotations/privileged/PrivilegedMetadataExtractor.java`
- `security/annotations/unlocked/UnlockedVoter.java`
- `security/annotations/unlocked/UnlockedMetadataExtractor.java`
- `security/annotations/visibility/VisibilityRequiredVoter.java`
- `security/annotations/visibility/VisibilityRequiredMetadataExtractor.java`

### Preserved:
- All annotation classes (`@PermissionRequired`, `@Privileged`, `@Unlocked`, `@RequireAal`, `@CurrentUser`, `@VisibilityRequired`)
- All controller and service code
- All test code
