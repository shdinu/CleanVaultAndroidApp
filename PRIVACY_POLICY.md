# Privacy Policy & Disclaimer for Clean Vault

**Effective Date:** September 1, 2026  
**Last Updated:** September 1, 2026  
**Application Name:** Clean Vault  
**Package Name:** `com.ambesoftnet.cleanvault`  
**Developer / Publisher:** Ambesoft Technologies  
**Contact Email:** [ambesoftnet@gmail.com](mailto:ambesoftnet@gmail.com)  

---

## 1. Overview & Purpose

**Clean Vault** is a general-purpose note-taking application and an open architectural educational sample. It is designed to demonstrate modern Android development best practices, Clean Architecture, Jetpack Compose, Room database caching, and hardware-backed cryptographic data storage on Android.

By downloading, installing, accessing, or using Clean Vault ("the Application", "we", "us", or "our"), you ("User", "you") acknowledge that you have read, understood, and agreed to this Privacy Policy and the Disclaimers contained herein.

---

## 2. Educational Project & General Purpose Declaration

This application is provided primarily as an **educational demonstration, learning example, and general-purpose utility**. 

- The Application is made available for learning, testing, and general productivity purposes.
- It is not intended as a mission-critical or high-security enterprise secrets vault.
- Users are encouraged to evaluate and inspect the codebase to understand architectural patterns and encryption workflows.

---

## 3. Disclaimer of Warranties & Limitation of Liability

### 3.1 "AS IS" and "AS AVAILABLE"
The Application, its source code, and all related materials are provided **"AS IS"** and **"AS AVAILABLE"**, without warranty of any kind, whether express, implied, statutory, or otherwise, including but not limited to warranties of merchantability, fitness for a particular purpose, title, non-infringement, security, correctness, or uninterrupted functionality.

### 3.2 Limitation of Liability
To the maximum extent permitted by applicable law:
- **No Liability for Loss or Damage:** The developer, author, contributors, and Ambesoft Technologies shall **NOT** be held liable for any direct, indirect, incidental, special, consequential, exemplary, or punitive damages, including but not limited to loss of data, loss of profits, device malfunctions, corruption of stored notes/secrets, business interruption, or system downtime arising out of or in connection with the use of, or inability to use, the Application.
- **No Liability for Misinformation or Errors:** We do not warrant the accuracy, completeness, or reliability of any content, sample data, notes, or documentation within the Application. We are not liable for any errors, omissions, or misinformation.
- **No Liability for Functionality Issues:** We do not guarantee that the Application will be error-free, bug-free, continuous, or compatible with all hardware and operating system versions.

### 3.3 User Responsibility for Backups & Security
You are solely responsible for:
- Maintaining independent, secure backups of any critical data, notes, or secrets entered into the Application.
- Protecting physical and biometric access to your device.
- Understanding that uninstalling the Application or clearing application cache/data will permanently delete all locally stored notes and encryption keys.

---

## 4. Information Collection and Use

Clean Vault is built with a **strict privacy-first philosophy**. We do not collect, harvest, store, sell, or rent your personal information to any third parties.

### 4.1 Local Storage and On-Device Processing
- **Notes & Database:** All personal notes created in the Application are stored exclusively in an offline local SQLite/Room database on your device (`app_database`).
- **Encrypted Secrets:** Sensitive notes and encrypted key-value pairs are stored using Android's `EncryptedSharedPreferences` backed by the **Android KeyStore** (using AES-256-GCM for values and AES-256-SIV for keys).
- **Master Encryption Keys:** Cryptographic keys are generated and stored in your device's hardware-backed secure enclave (TEE/StrongBox) where supported. They **never** leave your device or get uploaded to any remote server in plaintext.

### 4.2 Personally Identifiable Information (PII)
- Clean Vault does **not** collect your name, physical address, email address, phone number, biometric records, contacts, or location data.
- The Application does not require user registration, social logins, or accounts.

### 4.3 Remote Network Calls (Demo Data)
- The Application includes network capabilities exclusively to demonstrate REST API consumption with Retrofit (e.g., retrieving public sample posts from `jsonplaceholder.typicode.com`).
- No user-entered notes, secrets, device identifiers, or personal data are ever transmitted to this or any external endpoint.

### 4.4 Analytics, Tracking, and Advertising
- **No Ads:** Clean Vault contains zero third-party advertising SDKs, banner ads, or interstitials.
- **No Tracking/Analytics:** We do not use Google Firebase Analytics, Mixpanel, Facebook SDK, or any behavioral tracking tools. We do not track your keystrokes, activity, or usage patterns.

---

## 5. Device Permissions & Package Visibility

The Application requests only the minimal permissions required for its demonstrated functionality:

| Permission / Query | Purpose | User Data Impact |
| :--- | :--- | :--- |
| `android.permission.INTERNET` | Required to fetch sample REST API placeholder data for educational architectural demonstration. | No personal data or notes are sent over the network. |
| `<queries>` for `android.intent.action.VIEW` (`upi://`) | Allows the app to query and launch installed UPI payment applications (e.g., Google Pay, PhonePe, Paytm, BHIM) when the user voluntarily taps the optional "Buy Me a Coffee" support button. | Clean Vault does not process payments or store banking/card data. All transactions happen in external UPI apps. |
| `<queries>` for `android.intent.action.DIAL` (`tel:`) | Allows launching the default device phone dialer when the user taps the contact phone number. | The app does not make automatic calls or read call logs. |
| `<queries>` for `android.intent.action.SENDTO` (`mailto:`) | Allows opening the device email client to send voluntary feedback/inquiries. | No background emails are sent; the user controls the email content. |

---

## 6. Third-Party Links & Voluntary Support

The Application contains options to contact the developer or support the project via UPI payments ("Buy Me a Coffee"):
- When you click contact links or UPI donation options, you are redirected to third-party applications (e.g., your email provider, dialer, or UPI banking application).
- Clean Vault does not collect, process, or store financial credentials, account numbers, or UPI PINs.
- Interactions within third-party applications are governed by their respective privacy policies and terms of service.

---

## 7. Data Retention and Deletion

- Because all data is stored locally on your device, you retain full ownership and control over your data.
- **To Delete All Data:** 
  1. Open your Android device **Settings** → **Apps** → **Clean Vault**.
  2. Select **Storage & cache** → **Clear storage / Clear data**.
  3. Or simply **Uninstall** the Application.
- This immediately and irreversibly destroys all local databases, application preferences, and hardware encryption keys.

---

## 8. Children's Privacy (COPPA & GDPR-K Compliance)

Clean Vault is a general-purpose utility and educational software sample. It is not directed to children under the age of 13 (or 16 in certain jurisdictions). We do not knowingly collect personal information from children. If you are a parent or guardian and believe that your child has provided us with personal information, please contact us so that we can take necessary steps.

---

## 9. Security Measures

While Clean Vault implements industry-standard encryption practices (AES-256 with Android KeyStore):
- Security also relies on the integrity of your device's operating system, screen locks, and physical security.
- No electronic storage or cryptographic implementation can be guaranteed to be 100% immune to vulnerabilities, physical device compromises, or operating system exploits (e.g., rooted devices).

---

## 10. Changes to This Privacy Policy

We may update this Privacy Policy periodically to reflect enhancements, changes in practices, or regulatory requirements. Any modifications will be posted within the repository and marked with an updated **"Last Updated"** date. Continued use of the Application after revisions constitutes acceptance of the updated policy.

---

## 11. Contact Us

If you have any questions, suggestions, or concerns regarding this Privacy Policy, disclaimers, or data practices, please contact us:

- **Organization:** Ambesoft Technologies
- **Developer Email:** [ambesoftnet@gmail.com](mailto:ambesoftnet@gmail.com)
- **Support / Phone:** +91 9898151797
- **Project Repository:** [CleanVault Android Project](file:///e:/RND/KotlinApps/SecureVaultNotes/CleanVaultAndroidApp)
