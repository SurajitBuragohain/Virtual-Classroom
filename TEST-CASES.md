# Virtual Classroom - Automated Test Cases

Run all automated unit tests with:

    mvn clean test

## Test cases

| ID | Test | Expected result |
|---|---|---|
| TC-001 | Hash a password | Password is PBKDF2 salted hash and verifies correctly |
| TC-002 | Verify wrong password | Verification returns false |
| TC-003 | Hash same password twice | Different salted hashes are generated |
| TC-004 | Invalid stored hash | Verification returns false |
| TC-005 | Registration with missing mandatory field | Registration is rejected |
| TC-006 | Registration with invalid role | Registration is rejected |
| TC-007 | Registration with phone not exactly 10 digits | Registration is rejected |
| TC-008 | Student registration without teacher | Registration is rejected |
| TC-009 | Login with missing credentials | Login is rejected |
| TC-010 | Login with null credentials | Login is rejected |
| TC-011 | Unauthenticated classroom access | User is redirected to login |
| TC-012 | Classroom without ID | HTTP 400 is returned |
| TC-013 | Classroom with non-numeric ID | HTTP 400 is returned |
| TC-014 | Unauthenticated action request | User is redirected to login |
| TC-015 | Action parameter missing | HTTP 400 is returned |
| TC-016 | Unknown action | HTTP 400 is returned |

## Database-dependent tests

The above tests intentionally avoid changing your real MySQL data. They test validation, security boundaries and password functionality without requiring a running database for every test.

For full end-to-end testing, use a separate test database and add integration tests for registration, login, classroom creation, assignment submission, grading and feedback.
