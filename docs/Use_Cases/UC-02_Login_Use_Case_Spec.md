# Use Case Specification
## Online Fruit Shop System
### UC-02 Login

## 1. Management Information
**ID and Name:** UC-02 Login
**Created By:** Duong Minh Hoang
**Date Created:** May 19, 2026
**Feature:** Authentication -> Account Access

## 2. Actor Definitions & Purpose
**Primary Actor:** Guest / Unauthenticated User
**Secondary Actors:** Google OAuth System (Google API).
**Description:** Allows registered users (Customers, Shop Owners, Admins) to authenticate themselves and access secure, role-based features of the Online Fruit Shop System. It supports standard login via Email/Password and Single Sign-On (SSO) via Google OAuth.

## 3. Execution Conditions
**Trigger:** The user clicks the "Login" button from the UI navigation, or the user attempts to access a protected route (e.g., Profile, Checkout, Dashboard) and is redirected to the Login page.
**Preconditions:**
- PRE-1: The user already has a registered account in the system.
- PRE-2: The user is not currently logged into the system.
**Postconditions:**
- POST-1: The system establishes a secure authenticated session for the user.
- POST-2: The user is redirected to their respective dashboard or the previous page they were originally trying to access.

## 4. Scenarios (Flow of Events)
### Normal Flow (Login via Email/Password):
1. The user navigates to the Login Page.
2. The system displays the login form requesting Email and Password, along with a "Login with Google" button and a "Forgot Password" link.
3. The user enters their Email and Password.
4. The user clicks the "Login" button.
5. The system validates that both fields are not empty and the email format is correct.
6. The system encrypts the entered password and compares it against the stored hash for the given Email.
7. The system checks the account status (must be "Active").
8. The system creates a secure session (e.g., generates a session token).
9. The system redirects the user:
   - Customers -> Home Page or the page they were previously on (e.g., Checkout).
   - Shop Owners -> Shop Dashboard.
   - Admins -> Admin Dashboard.

### Alternative Flows:
**4.1. Login via Google OAuth**
1. At step 3 of the Normal Flow, the user clicks the "Login with Google" button.
2. The system redirects the user to the Google authentication page.
3. The user logs in and grants access.
4. Google returns a secure Profile Token to the system.
5. The system extracts the Email from the Token.
6. The system verifies that an account with this Email exists in the Database and checks its status.
7. The system establishes a login session for the user.
8. The system redirects the user to their designated dashboard/page based on their role.

### Exceptions / Error Handling:
**6.E1 Invalid Email or Password**
1. At step 6 of the Normal Flow, the system finds no matching Email, or the password hashes do not match.
2. The system displays a generic error message: "Invalid email or password." (To prevent email enumeration attacks).
3. The user re-enters their credentials to try again.

**7.E1 Account Pending Approval**
1. At step 7 of the Normal Flow (or step 6 of Flow 4.1), the system detects the account is a Shop Owner with a "Pending Approval" status.
2. The system denies login and displays: "Your account is still pending Admin approval."
3. The user remains on the Login page.

**7.E2 Account Locked/Banned**
1. At step 7 of the Normal Flow (or step 6 of Flow 4.1), the account status is marked as "Locked" or "Banned".
2. The system denies login and displays: "Your account has been locked. Please contact support."
3. The user remains on the Login page.

**4.1.E1 Unregistered Google Account**
1. At step 6 of Flow 4.1, the system does not find the extracted Email in the database.
2. The system prompts the user: "Account not found. Would you like to register a new account with this Google profile?"
3. The user may choose to be redirected to the Registration page or cancel.

**8.E1 Session Creation Failure**
1. At step 8 of the Normal Flow, the server fails to create a session due to an internal error or server misconfiguration.
2. The system displays: "An internal system error occurred. Please try again later."
3. The system logs the incident.

## 5. Additional Information
**Priority:** High (P0 - Core functionality)
**Frequency of Use:** High. Every time a user returns to the platform after their session expires.
**Business Rules:** 
- USR-01: Shop Owners pending approval cannot log in.
- SEC-01: Account should be temporarily locked out after 5 consecutive failed login attempts to prevent brute-force attacks.
**Other Information:** Shop Owner accounts may remain Pending until approval if the platform policy requires it, restricting login access. Login events can be logged for audit if required by system policy.
**Assumptions:** Email verification, OTP, or captcha may be enabled by configuration. If enabled, the exact mechanism is a system setting, not a separate business actor.