## 🔐 기능: 구글 로그인 및 인증 (Google Login & Authentication)

이 브랜치(`feature/login`)는 **Firebase Authentication**과 **Google Identity Services**를 활용하여 안전하고 매끄러운 사용자 인증 플로우를 구현합니다.

---

## 📺 미리보기 (Preview)
| 최초 로그인 | 자동 로그인 (기존 세션 유지) |
|-------------|------------------|
| <img src="https://github.com/user-attachments/assets/0c3068b7-2120-4b32-bf12-83e3a9e77878" width="250"/> | <img src="https://github.com/user-attachments/assets/fac6d41a-cb5d-470a-9826-b98d4c926392" width="250"/> |
| *최초 구글 로그인 플로우* | *기존 세션을 통한 자동 로그인* |

---

## 🛠 주요 구현 사항

### 1. Firebase Auth 및 Google Identity Services

- **매끄러운 인증 경험 (Seamless Authentication)** **Google One Tap Sign-In**을 통합하여 사용자가 복잡한 절차 없이 빠르게 로그인할 수 있는 UX를 제공합니다.

- **인증 세션 유지 (Persistent Session)** Firebase Authentication의 내장 로컬 캐싱 메커니즘을 활용하여 앱을 종료한 후 다시 실행하더라도 로그인 상태가 안정적으로 유지되도록 구현했습니다.

---

### 2. 인증 상태 기반 초기 라우팅 (Initial Routing)

- **스플래시 화면 로직 (Splash Screen Logic)** 앱 시작 시 사용자의 인증 상태를 즉시 파악하고, 결과에 따라 적절한 초기 화면을 결정하는 전용 스플래시 시퀀스를 구현했습니다.

- **스마트 리다이렉트 (Smart Redirect)**
    - `currentUser`가 **null이 아닌 경우**: 로그인된 상태로 간주하여 **홈 화면(Home Screen)**으로 자동 이동합니다.
    - `currentUser`가 **null인 경우**: 인증 정보가 없으므로 **로그인 화면(Login Screen)**으로 리다이렉트됩니다.

- **UX 최적화 (Optimized UX)** 다음과 같은 목적으로 최소 **1초**의 스플래시 노출 시간을 강제 적용했습니다.
    - Firebase SDK가 완전히 초기화될 수 있는 안정적인 시간 확보
    - 화면 전환 시 시각적으로 튀는 현상(Jank)을 방지하고 부드러운 사용자 경험 제공