# Use Case Specification
## Online Fruit Shop System
### UC-01 Register Account

## 1. Management Information
**ID and Name:** UC-01 Register Account
**Created By:** Duong Minh Hoang
**Date Created:** May 19, 2026
**Feature:** Authentication -> Account Access

## 2. Actor Definitions & Purpose
**Primary Actor:** Guest
**Secondary Actors:** Admin (only when Shop Owner approval is required), Google OAuth System (Google API), Email/OTP Delivery Service (if enabled).
**Description:** Allows a Guest to create a new Customer or Shop Owner account to securely access authenticated features of the Online Fruit Shop System. It supports both manual registration and quick registration via Google OAuth.

## 3. Execution Conditions
**Trigger:** The Guest clicks the "Register" button from the home page, login page, or is redirected after attempting an action that requires authentication (e.g., proceeding to checkout from the cart).
**Preconditions:**
- PRE-1: The user is not currently logged into the system.
- PRE-2: The registration page and Google OAuth API are functioning normally.
**Postconditions:**
- POST-1: The system successfully saves the new account information into the database.
- POST-2: The account status is set to "Active" (for Customers or Google registrations) or "Pending Approval" (for Shop Owners awaiting Admin verification).
- POST-3: The system logs the registration event in the Audit Log (if required).

## 4. Scenarios (Flow of Events)
### Normal Flow (Registration via Email/Password):
1. The Guest opens the Register Page.
2. The system displays the registration form containing the following fields: Full Name, Email, Phone number (optional), Password, Confirm Password, Account Type (Customer / Shop Owner), and a "Register with Google" button.
3. The Guest enters valid information into the required form fields.
4. The Guest selects the Account Type (Default is Customer).
5. The Guest clicks the "Register" button.
6. The system verifies data format validity, password strength and match, and Email uniqueness.
7. The system creates a new account in the database and assigns roles based on the selected Account Type.
8. The system securely encrypts (hashes) the password before persisting it.
9. The system sends a welcome/verification email (if required by configuration).
10. The system displays a successful registration message and redirects the user to the Login Page.

### Alternative Flows:
**4.1. Registration via Google OAuth**
1. At step 3 of the Normal Flow, the Guest clicks the "Register with Google" button.
2. The system redirects the Guest to the Google authentication page.
3. The Guest logs in and grants the system permission to access basic profile information (Email, Full Name, Avatar).
4. Google returns a Profile Token to the system.
5. The system extracts the Email and Full Name from the Token.
6. The system recognizes this as a new Email (not currently existing in the Database), and automatically creates a new account with the default type set to Customer and status set to Active. (No password is required).
7. The system automatically establishes a login session for the user.
8. The system redirects the Guest directly to the Home Page or another specified landing page.

**4.2. Shop Owner Account Registration Requiring Admin Approval**
1. At step 4 of the Normal Flow, the Guest selects the Account Type as "Shop Owner".
2. The Guest continues through step 7 of the Normal Flow.
3. The system creates the account but assigns the status as "Pending Approval" instead of Active (enforcing business rule USR-01).
4. The system sends a notification to the Admin regarding the new Shop creation request.
5. The system notifies the Guest: "Account created successfully. Your Shop account is currently awaiting Admin approval." and ends the Use Case.

### Exceptions / Error Handling:
**6.E1 Invalid or Missing Data**
1. At step 6 of the Normal Flow, the system detects an invalid Email format, a password that is too short, or mismatched passwords.
2. The system highlights the erroneous fields and displays the corresponding error messages.
3. The Guest corrects the data and clicks "Register" to try again. The system returns to step 6.

**7.E1 Email Already Exists**
1. At step 6 of the Normal Flow, or step 6 of Alternative Flow 4.1 (Google), the system detects that the Email has already been registered.
2. The system displays the message: "This email is already in use. Please log in or use a different email address."
3. The Guest chooses to return to the Login page or enter a new Email to continue.

**4.1.E1 Google OAuth Authentication Error**
1. At step 4 of Flow 4.1, Google returns an error (due to user denying permission, token expiration, or network failure).
2. The system displays the message: "Google registration failed. Please try again or register manually."
3. The Guest returns to the standard registration page.

**10.E1 System / Database Error**
1. At step 7 of the Normal Flow, the system cannot save the data due to a database connection failure.
2. The system logs the error and displays: "The system is currently experiencing issues. Please try again later."
3. The system gracefully ends the Use Case without saving partial data.

## 5. Additional Information
**Priority:** High (P0 - Core functionality)
**Frequency of Use:** High. Daily usage, whenever new customers or shops join the platform.
**Business Rules:** 
- USR-01: Shop Owner registration accounts must be approved by an Admin to become Active.
- USR-05: Guests are allowed to browse the cart but must complete the Register/Login Use Case before Checkout.
**Other Information:** Guest can register only as Customer or Shop Owner. Shop Owner accounts may remain Pending until approval if the platform policy requires it. Registration events can be logged for audit if required by system policy.
**Assumptions:** Email verification, OTP, or captcha may be enabled by configuration. If enabled, the exact mechanism is a system setting, not a separate business actor.
