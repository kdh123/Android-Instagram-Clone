## 🔐 Feature: Google Login & Authentication

This branch (`feature/login`) implements a secure and seamless user authentication flow using **Firebase Authentication** and **Google Identity Services**.

---

## 📺 Preview
| First Login | Already Logged In |
|-------------|------------------|
| <img src="https://github.com/user-attachments/assets/47f9a3f5-1f06-4fe4-bc4e-5531e37b0043" width="250"/> | <img src="https://github.com/user-attachments/assets/fb8e4f2e-1d89-4034-b81c-df4bae41718e" width="250"/> |
| *First-time Google login flow* | *Auto-login with existing session* |


---

## 🛠 Key Implementations

### 1. Firebase Auth & Google Identity Services

- **Seamless Authentication**  
  Integrated **Google One Tap Sign-In** to provide a fast and user-friendly login experience.

- **Persistent Session**  
  Utilizes Firebase Authentication’s built-in local caching mechanism to keep users logged in even after the app is closed and reopened.

---

### 2. Authentication-Based Initial Routing

- **Splash Screen Logic**  
  Implemented a dedicated splash sequence that determines the initial screen based on the user’s authentication state.

- **Smart Redirect**
  - If `currentUser` is **not null**, the user is automatically navigated to the **Home Screen**
  - If `currentUser` is **null**, the user is redirected to the **Login Screen**

- **Optimized UX**  
  A minimum splash duration of **1 second** is enforced to:
  - Ensure proper Firebase SDK initialization
  - Provide a smooth and visually stable transition experience

---
