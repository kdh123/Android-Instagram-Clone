# 📱 Instagram 클론 (Android)
> **🚀 상태: 개발 진행 중 (Active Development)**
> 이 프로젝트는 기능 단위로 단계별 개발이 진행되고 있으며 자주 업데이트됩니다. 새로운 기능과 아키텍처 개선사항이 지속적으로 추가되고 있습니다.

---
> **Jetpack Compose와 Clean Architecture를 기반으로 구축된 Instagram 클론 프로젝트입니다.**
>
> 이 프로젝트는 현대적인 Android 기술 스택을 사용하여 확장 가능한 앱 아키텍처를 설계하고, Firebase를 백엔드로 활용하여 실제 서비스 수준의 기능을 구현하는 것을 목표로 합니다.
---

## 🛠 기술 스택

Android 공식 가이드라인을 엄격히 준수하며 최신 라이브러리를 적극적으로 도입하여 개발되었습니다.

### **핵심 라이브러리**
* **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
* **언어**: [Kotlin](https://kotlinlang.org/)
* **의존성 주입 (DI)**: [Hilt](https://dagger.dev/hilt/)
* **비동기 처리**: [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
* **네비게이션**: Compose Navigation

### **아키텍처**
* **Clean Architecture**: Domain, Data, Presentation 레이어의 엄격한 분리
* **Multi-Module**: 기능 및 관심사 기반의 모듈화 전략
* **MVI 패턴**: UI 상태와 비즈니스 로직의 명확한 분리

### **백엔드 (Firebase)**
* **Auth**: 사용자 인증 및 세션 관리
* **Firestore / Realtime DB**: 확장 가능한 데이터 저장소 및 실시간 상호작용
* **Storage**: 고해상도 이미지 및 미디어 호스팅

---

## 🏗 프로젝트 구조

각 계층의 독립성을 보장하기 위해 Clean Architecture 원칙에 따라 모듈을 분리했습니다.

```text
├── app                     # 진입점 및 의존성 주입(DI) 설정
├── core                    # 애플리케이션 전반에서 공유되는 공통 모듈
│   ├── domain              # 비즈니스 로직, 엔티티, 유즈케이스 (Pure Kotlin)
│   ├── data                # 데이터 소스 관리 및 레포지토리 구현체
│   ├── designsystem        # 디자인 시스템 (UI 테마 및 공통 컴포넌트)
│   └── common              # 공통 유틸리티
└── feature                 # 기능별 모듈 (독립적인 UI 및 ViewModel)
```

---

## 📅 로드맵 및 진행 상황
개발은 기능 단위로 단계별로 진행되며, `[x]` 표시가 된 항목은 실제 서비스 수준의 로직으로 구현이 완료된 기능입니다.

* [x] **초기 설정**: 멀티 모듈 프로젝트 아키텍처 설계 및 기본 환경 설정.
* [x] **Phase 1 ([인증](https://github.com/kdh123/Android-Instagram-Clone/tree/feature/login))**: Firebase Auth 기반의 회원가입 및 로그인 플로우 구현.
* [x] **Phase 2 ([피드 및 인터랙션](https://github.com/kdh123/Android-Instagram-Clone/tree/feature/feed-home))**: 핵심 피드 기능 및 실시간 상호작용 시스템 구축.
    * [x] **[고도화된 다중 이미지 업로드](https://github.com/kdh123/Android-Instagram-Clone/tree/feature/feed-add)**: 병렬 처리 및 **순서 보장 로직**을 적용한 업로드 프로세스 (최대 10장).
    * [x] **인터랙션 시스템**: 좋아요, 댓글, 답글 시스템 및 중첩 비즈니스 로직과 로컬 상태 관리(Optimistic UI).
    * [x] **UI 최적화**: 기기 독립적인 좌표 계산 로직 및 블러 오버레이 효과 적용.
* [ ] **Phase 3 (탐색 및 검색)**: 사용자 및 콘텐츠 탐색 시스템.
    * [ ] 최적화된 검색 쿼리 로직을 적용한 실시간 사용자 검색.
    * [ ] 콘텐츠 탐색 피드 (그리드 스타일 익스플로러 레이아웃).
* [ ] **Phase 4 (소셜 및 관계)**: 팔로우 및 팔로잉 관계 시스템 구축.
    * [ ] 확장 가능한 팔로우/언팔로우 관계 관리 로직.
    * [ ] 사용자 프로필 편집 및 활동 통계 요약 정보.
* [ ] **Phase 5 (릴스)**: 숏폼 비디오 스트리밍 서비스.
    * [ ] **ExoPlayer/Media3**를 활용한 끊김 없는 비디오 재생 구현.
    * [ ] 세로 스크롤 기반의 전용 UI/UX 레이아웃.
* [ ] **지속적인 업데이트**:
    * [ ] 앱 실행 속도 및 성능 최적화 (Baseline Profiles, R8/D8 적용).
    * [ ] UI/UX 디테일 고도화 (MotionLayout, 커스텀 애니메이션). 🚀

---

## 💡 주요 개발 집중 사항
- **계층 분리(Layer Separation)**: 프레임워크 의존성을 제거하고 테스트 가능성을 극대화하기 위해 `core:domain` 모듈을 순수 Kotlin 모듈로 설계 및 유지했습니다.
- **모듈 독립성(Module Independence)**: 각 기능(Feature) 모듈 간의 의존성을 최소화하여 독립적인 개발 환경과 효율적인 빌드 환경을 구축하는 데 집중했습니다.
- **확장성(Scalability)**: Firebase의 서버리스 아키텍처를 전략적으로 활용하여 새로운 기능을 신속하게 확장하고 실시간 데이터 동기화를 보장하도록 설계했습니다.