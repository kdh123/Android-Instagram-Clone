# 📱 Instagram Clone (Android)
🇰🇷 **[Read this in Korean](./README_ko.md)**

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
Development is progressing step-by-step by feature unit. Features marked with `[x]` are fully implemented with production-ready logic.

* [x] **Initial Setup**: Multi-module project architecture design and base configuration.
* [x] **Phase 1 ([Auth](https://github.com/kdh123/Android-Instagram-Clone/tree/feature/login))**: Implementation of sign-up/login flow based on Firebase Auth.
* [x] **Phase 2 ([Feed & Interaction](https://github.com/kdh123/Android-Instagram-Clone/tree/feature/feed-home))**: Core Feed functionalities and Real-time interactions.
    * [x] **[Advanced Multi-Image Upload](https://github.com/kdh123/Android-Instagram-Clone/tree/feature/feed-add)**: Parallel processing with **order preservation logic** (up to 10 images).
    * [x] **Interaction System**: Like/Comment/Reply system with nested business logic and local state management.
    * [x] **UI Optimization**: Device-independent coordinate calculation and blur overlay effects.
* [ ] **Phase 3 (Discovery & Search)**: User/Content discovery system.
    * [ ] Real-time user search with optimized query logic.
    * [ ] Content discovery feed (Grid-style explorer).
* [ ] **Phase 4 (Social & Relationship)**: Follow/Following system construction.
    * [ ] Scalable follow/unfollow relationship management.
    * [ ] User profile customization and activity stats.
* [ ] **Phase 5 (Reels)**: Short-form video streaming service.
    * [ ] Video playback using **ExoPlayer/Media3**.
    * [ ] Vertical scroll-based UI/UX implementation.
* [ ] **Ongoing Updates**:
    * [ ] Performance optimization (Baseline Profiles, R8/D8).
    * [ ] Enhanced UI/UX details (MotionLayout, Custom Animations). 🚀

---

## 💡 Key Development Focus
- Layer Separation: Maintained core:domain as a pure Kotlin module to remove framework dependencies and increase testability.
- Module Independence: Minimized dependencies between feature modules to aim for an independent development and build environment.
- Scalability: Leveraged Firebase's serverless architecture to quickly expand features and ensure real-time data synchronization.
