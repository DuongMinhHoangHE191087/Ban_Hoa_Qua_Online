# Use Case Specification
## Online Fruit Shop System
### UC-03 Forgot Password

## 1. Management Information
**ID and Name:** UC-03 Forgot Password
**Created By:** Duong Minh Hoang
**Date Created:** May 19, 2026
**Feature:** Authentication -> Account Access

## 2. Actor Definitions & Purpose
**Primary Actor:** Guest / Unauthenticated User
**Secondary Actors:** Email/OTP Delivery Service (Notification Service).
**Description:** Allows a user who has forgotten their password to verify their identity via an external channel (e.g., Email or Phone OTP) and create a new password to regain access to their account.

## 3. Execution Conditions
**Trigger:** The user clicks the "Forgot Password" link on the Login Page.
**Preconditions:**
- PRE-1: The user has an existing account in the system.
- PRE-2: The user is not currently logged into the system.
- PRE-3: The Email/OTP delivery service is operational.
**Postconditions:**
- POST-1: The user's account password is updated to the newly provided password.
- POST-2: All existing active sessions for this user may be invalidated (depending on security policy).
- POST-3: The system logs the password reset event for security auditing.

## 4. Scenarios (Flow of Events)
### Normal Flow (Password Reset via Email OTP / Link):
1. The user navigates to the Forgot Password Page.
2. The system displays a form requesting the registered Email address.
3. The user enters their Email and submits the form.
4. The system verifies that the Email exists in the database.
5. The system generates a secure, time-limited password reset token/OTP and sends it to the user's associated Email.
6. The system displays a generic success message confirming that a reset link/code has been sent (to prevent email enumeration).
7. The user checks their email and clicks the reset link or enters the OTP on the verification page.
8. The system validates the token/OTP for correctness and expiration status.
9. The system displays the "Create New Password" form (New Password, Confirm Password).
10. The user enters their new password and confirms it.
11. The system verifies that the passwords match and meet the password strength policy.
12. The system securely encrypts (hashes) the new password and updates the user's account in the database.
13. The system displays a success message and redirects the user to the Login Page to authenticate with their new credentials.

### Alternative Flows:
**4.1. Account registered solely via Google OAuth**
1. At step 4 of the Normal Flow, the system detects that the Email belongs to an account created exclusively via Google OAuth with no local password set.
2. The system sends an email notifying the user that they use Google Login and therefore do not have a standard password to reset.
3. The Use Case terminates (or optionally prompts the user to use "Login with Google").

### Exceptions / Error Handling:
**4.E1 Unregistered Email**
1. At step 4 of the Normal Flow, the system does not find the Email in the database.
2. The system still displays the generic success message from step 6: "If the email is registered, you will receive a reset link shortly." (This prevents attackers from enumerating valid account emails).
3. No email is sent, and the Use Case ends.

**8.E1 Expired or Invalid Token/OTP**
1. At step 8 of the Normal Flow, the system finds that the token has expired or is invalid.
2. The system displays: "The password reset link/code is invalid or has expired. Please request a new one."
3. The system provides a link to return to the Forgot Password Page.

**11.E1 Password Mismatch or Weak Password**
1. At step 11 of the Normal Flow, the new password does not meet the defined complexity requirements or the confirmation does not match.
2. The system highlights the errors and prompts the user to try again.
3. The user makes corrections and resubmits to step 11.

## 5. Additional Information
**Priority:** High (P0 - Core functionality)
**Frequency of Use:** Medium. Used whenever a user forgets their credentials.
**Business Rules:**
- SEC-02: Password reset tokens must be strictly time-bound (e.g., expire in 15 minutes) and single-use only.
- SEC-03: On successful password reset, all existing active authentication sessions should be terminated to prevent unauthorized access.
**Other Information:** The system must not reveal whether an email address actually exists in the database during the initial request phase. Password reset events can be logged for audit if required by system policy.
**Assumptions:** Email verification, OTP, or captcha may be enabled by configuration. If enabled, the exact mechanism is a system setting, not a separate business actor.