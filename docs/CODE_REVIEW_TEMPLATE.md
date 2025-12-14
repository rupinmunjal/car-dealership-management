# Code Review Template

**PR Title:** [Title of the Pull Request]  
**PR Link:** [URL]  
**Author:** [Name]  
**Reviewer:** [Name]  
**Date:** [YYYY-MM-DD]  
**Branch:** [feature-branch] → [target-branch]

---

## 1. Overview

**Summary of Changes:**
> [Brief description of what this PR accomplishes]

**Related Issues/Tickets:**
- [ ] Issue #___: [Description]
- [ ] Issue #___: [Description]

---

## 2. Code Quality Checklist

### Functionality
- [ ] Code accomplishes the stated purpose
- [ ] Edge cases are handled appropriately
- [ ] Error handling is implemented correctly
- [ ] No obvious bugs or logic errors

### Code Standards
- [ ] Follows project naming conventions
- [ ] Functions are small and focused (single responsibility)
- [ ] No hardcoded values (uses constants/environment variables)
- [ ] No commented-out code left in
- [ ] No debug statements left in (console.log, print, etc.)

### Documentation
- [ ] Functions have appropriate comments where needed
- [ ] Complex logic is explained with inline comments
- [ ] README updated if new features added
- [ ] API documentation updated if endpoints changed

### Testing
- [ ] Unit tests added for new functionality
- [ ] Existing tests still pass
- [ ] Edge cases covered in tests
- [ ] Test names clearly describe what is being tested

### Security
- [ ] No sensitive data exposed (passwords, keys, tokens)
- [ ] Input validation implemented
- [ ] No SQL injection vulnerabilities
- [ ] No XSS vulnerabilities

---

## 3. Review Findings

### ✅ Approved Items
| Item | Notes |
|------|-------|
| | |

### ⚠️ Suggestions (Non-blocking)
| File:Line | Suggestion |
|-----------|------------|
| | |

### ❌ Required Changes (Blocking)
| File:Line | Issue | Severity |
|-----------|-------|----------|
| | | High/Medium/Low |

---

## 4. Testing Verification

### Tests Run
- [ ] Unit tests - Passed ✅ / Failed ❌
- [ ] Integration tests - Passed ✅ / Failed ❌
- [ ] Linting - Passed ✅ / Failed ❌

### Manual Testing
- [ ] Feature tested locally
- [ ] Tested in staging environment (if applicable)
- [ ] No regressions observed

---

## 5. Final Decision

**Decision:** [ ] APPROVED / [ ] REQUEST CHANGES / [ ] NEEDS DISCUSSION

**Comments:**
> [Final thoughts or conditions for approval]

---

**Reviewer Signature:** _____________________  
**Date:** _____________________

---

## Quick Reference: Code Review Best Practices

1. **Be constructive** - Focus on the code, not the person
2. **Be specific** - Point to exact lines and explain why
3. **Be timely** - Review within 24-48 hours
4. **Ask questions** - "Why did you choose this approach?" vs "This is wrong"
5. **Acknowledge good code** - Positive feedback matters
