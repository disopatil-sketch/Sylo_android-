# Privacy Policy for SYLO

**Effective Date:** November 26, 2026  
**App Name:** SYLO (*Your Language. Your World.*)  
**Package Name:** `com.aistudio.sylo.krxvmz`  
**Contact Email:** disopatil@gmail.com  

---

## 1. Introduction

Welcome to **SYLO** ("we," "our," or "us"). SYLO is a multilingual social media, short video, live streaming, news, memes, entertainment, and messaging platform built for users across India, South Asia, and global communities.

This Privacy Policy explains how we collect, use, store, share, and protect your personal information when you use the SYLO Android application, in accordance with Google Play Developer Program policies and applicable data protection laws (including India's Digital Personal Data Protection Act, 2023).

By creating an account or using SYLO, you agree to the practices described in this Privacy Policy.

---

## 2. Information We Collect

### 2.1 Account & Authentication Information
When you sign in to SYLO using **Google Sign-In** (via Android Credential Manager and Firebase Authentication), we collect:
- Unique User ID (UID)
- Display name and chosen username/handle
- Email address
- Profile details you choose to add (biography, city/location, preferred interface language, regional content languages, interests, and public/private account preference)

### 2.2 User-Generated Content & Social Activity
When you interact with SYLO, we store the content and actions you create in **Firebase Cloud Firestore**, including:
- Posts, short videos metadata, memes, news submissions, captions, and hashtags
- Comments, likes, saves, shares, and follow/block relationships
- Live video room metadata (stream title, category, language, viewer/reaction counters) and live chat or virtual gift messages
- One-to-one and group chat messages and message reactions
- Content safety, abuse, and copyright moderation reports

### 2.3 Camera, Microphone & Media Selection
- **Camera (`CAMERA`) and Microphone (`RECORD_AUDIO`):** Requested at runtime only when you choose to start a **Live Video** broadcast or use the **Short Video** recording preview. We do not access your camera or microphone in the background.
- **Photos & Videos:** When you upload media from your device, SYLO uses the privacy-preserving **Android Photo Picker**, which grants access only to the specific photo or video you select without requesting broad storage permissions.

### 2.4 Creator Monetization & Payout Information
If you submit a creator payout request, we collect the payout amount, earning source, and the masked UPI ID or bank reference you provide solely to process and track your request.

### 2.5 On-Device Preferences
We store local app preferences on your device using Android DataStore, including dark/light theme selection, Low-Data mode, interface language, online status toggle, and notification preferences.

---

## 3. How We Use Your Information

We use the information we collect to:
- Authenticate your account and personalize your multilingual Home Feed, Short Videos, News, Memes, and Celebrity Zone
- Enable real-time Live Video rooms, live chat, reactions, and direct/group messaging
- Provide on-demand **AI multilingual translation** and **automated content safety checks** when you request them
- Enforce community guidelines, handle user reports, and prevent fraud or abuse (using Firebase App Check)
- Display creator analytics and process creator payout requests
- Maintain account security and allow you to manage or delete your data

---

## 4. AI Processing & Third-Party Service Providers

SYLO relies on trusted Google Cloud / Firebase infrastructure to operate securely:
- **Firebase Authentication & Google Identity Services:** For secure Google Sign-In.
- **Firebase Cloud Firestore:** For storing user profiles, posts, live streams, messages, notifications, and moderation records.
- **Firebase AI Logic (Gemini models):** When you tap **Translate** on a post/comment or run an **AI Safety Check** before publishing, the relevant text is processed via Firebase AI Logic to generate translations or safety assessments.
- **Firebase App Check:** Used to verify that requests originate from an authentic installation of the SYLO app and protect against automated abuse.

We do **not** sell your personal information to third parties or data brokers.

---

## 5. Data Security & Privacy Controls

- All data transmitted between the SYLO app and backend services is encrypted in transit using HTTPS/TLS.
- Private account fields (such as email address, blocked users, notifications, and payout details) are isolated from public creator profiles and protected by strict Firebase Firestore security rules.
- You can switch between **Public** and **Private** account modes, control your online status visibility, block users, and report inappropriate or copyrighted content at any time inside the app.

---

## 6. Data Retention & Account Deletion

We retain your account data only for as long as your account remains active or as necessary to comply with legal and safety obligations.

You can delete your account and associated profile data at any time:
1. **In-App Deletion:** Open **SYLO → Profile → Settings & Safety → Delete Account**.
2. **Email Request:** Contact **disopatil@gmail.com** from your registered email address with the subject line `"SYLO Account Deletion Request"`.

Upon deletion, your account profile and private records are permanently removed from our active Firestore database.

---

## 7. Children's Privacy

SYLO is not directed to children under the age of 13 (or the applicable age of digital consent in your country, such as under 18 without verifiable parental consent under India's DPDP Act). We do not knowingly collect personal data from children without required consent. If you believe a child has provided personal information to SYLO, please contact us at **disopatil@gmail.com** so we can promptly remove the account and data.

---

## 8. Your Rights & Grievance Redressal

Depending on your region (including India and South Asia), you have the right to:
- Access and review the personal data associated with your account
- Update or correct inaccurate profile information directly in the app
- Withdraw consent or request deletion of your account and personal data
- Report privacy concerns, content violations, or copyright claims

To exercise any of these rights or submit a grievance, please contact:
- **Email:** disopatil@gmail.com

---

## 9. Changes to This Privacy Policy

We may update this Privacy Policy from time to time to reflect app improvements or legal requirements. When we make material changes, we will update the **Effective Date** at the top of this policy. Continued use of SYLO after changes take effect constitutes acceptance of the updated policy.
