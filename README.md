# 📱 Instagram Clone (Android)
> **🚀 Status: Work In Progress (Active Development)** > This project is being developed step-by-step and is updated frequently. New features and architectural improvements are added continuously.
---
> **An Instagram clone project built with Jetpack Compose and Clean Architecture.**
>
> This project aims to design a scalable app architecture using a modern Android tech stack and implement production-ready features utilizing Firebase as the backend.
---

## 🛠 Tech Stack

Developed by strictly following Android's recommended guidelines and actively adopting the latest libraries.

### **Core Libraries**
* **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
* **Language**: [Kotlin](https://kotlinlang.org/)
* **DI(Dependency Injection)**: [Hilt](https://dagger.dev/hilt/) 
* **Async**: [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
* **Navigation**: Compose Navigation

### **Architecture**
* **Clean Architecture**: Strict separation of Domain, Data, and Presentation layers
* **Multi-Module**: Modularization strategy based on features and concerns
* **MVI Pattern**: Clear separation of UI state and business logic

### **Backend (Firebase)**
* **Auth**: User authentication and session management
* **Firestore / Realtime DB**: Scalable data storage and real-time interactions
* **Storage**: High-resolution image and media hosting

---

## 🏗 Project Structure

Modules are separated according to Clean Architecture principles to ensure the independence of each layer.

```text
├── app                     # Entry point and Dependency Injection (DI) setup
├── core                    # Shared modules across the application
│   ├── domain              # Business logic, Entities, and UseCases (Pure Kotlin)
│   ├── data                # Data source management and Repository implementations
│   ├── designsystem        # Design System (UI Themes)
│   └── common              # Common utilities
└── feature                 # Feature-based modules (Independent UI and ViewModels)
```

---

## 📅 Roadmap & Progress
Development is progressing step-by-step by feature unit and will be updated upon completion.

[x] Initial Setup: Multi-module project architecture design and base configuration

[x] Phase 1 ([Auth](https://github.com/kdh123/Android-Instagram-Clone/tree/feature/login)): Implementation of sign-up/login flow based on Firebase Auth

[ ] Phase 2 ([Feed](https://github.com/kdh123/Android-Instagram-Clone/tree/feature/feed)): Image post upload and real-time feed list implementation

[ ] Phase 3 (Social): User search and Follow/Following system construction

[ ] Phase 4 (Interaction): Real-time interactions such as Likes and Comments

[ ] Ongoing Updates: Performance optimization and UI/UX detail enhancement 🚀

---

## 💡 Key Development Focus
- Layer Separation: Maintained core:domain as a pure Kotlin module to remove framework dependencies and increase testability.
- Module Independence: Minimized dependencies between feature modules to aim for an independent development and build environment.
- Scalability: Leveraged Firebase's serverless architecture to quickly expand features and ensure real-time data synchronization.
