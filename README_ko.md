## 🛠 핵심 기능 및 기술적 도전 과제

<table table-layout="fixed">
  <tr>
    <td align="center"><b>라이트 모드 UI</b></td>
    <td align="center"><b>다크 모드 UI</b></td>
    <td align="center"><b>핵심 인터랙션</b></td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/bf864a37-1277-45f2-ac3a-dc02781c8c65" width="250"><br>
      <sub>라이트 모드 피드 리스트</sub>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/a3a84877-c5c5-453d-af4d-88460703bd3e" width="250"><br>
      <sub>다크 모드 피드 리스트</sub>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/c466309f-ccab-4153-b766-bd9317dc0339" width="250"><br>
      <sub>좋아요, 댓글 및 답글 기능 시연</sub>
    </td>
  </tr>
</table>

### 1. 피드: 끊김 없는 오프라인 경험 (Paging 3 + RemoteMediator)
네트워크 환경이 좋지 않은 상황에서도 고품질의 사용자 경험을 제공하기 위해, [Instagram의 Background Prefetching](https://medium.com/instagram-engineering/improving-performance-with-background-data-prefetching-b191acb39898) 전략에서 영감을 얻은 "로컬 우선(Local-first)" 전략을 구현했습니다.

* **오프라인 우선 접근 방식**: **Android Paging 3와 RemoteMediator**를 활용하여 네트워크 데이터를 **Room 데이터베이스**에 캐싱합니다. 이를 통해 사용자는 인터넷 연결이 끊긴 상태에서도 이전에 로드된 피드를 끊김 없이 탐색할 수 있습니다.
* **단일 진실 공급원 (SSOT)**: UI는 오직 로컬 Room DB만을 관찰(Observe)하므로, 다양한 화면과 세션 간의 데이터 일관성이 완벽하게 보장됩니다.
* **향후 로드맵**: 현재는 오프라인 가시성에 집중하고 있으며, 추후 탐색(Search/Discovery) 모듈 개발 시 주기적인 백그라운드 로딩을 포함한 풀스케일 프리페칭(Prefetching) 시스템을 구현할 계획입니다.



---

### 2. 좋아요: 낙관적 UI를 통한 안정적인 상호작용
Instagram은 네트워크 상태와 관계없이 좋아요 반응이 즉각적으로 반영되고 안정적으로 동기화되도록 설계되어 있습니다. 이를 로컬 캐싱과 백그라운드 동기화 조합으로 구현했습니다.

* **낙관적 UI (Optimistic UI)**: 사용자가 좋아요 버튼을 누르면 로컬 **Room DB**의 상태를 즉시 업데이트하여 지연 없는 피드백을 제공합니다.
* **지속성 보장 (WorkManager)**: 실제 서버 동기화는 **WorkManager**가 담당합니다.
    * **제약 조건**: `NetworkType.CONNECTED` 제약 조건을 설정하여 인터넷이 연결된 최적의 시점에만 작업이 수행되도록 설계했습니다.
    * **안정성**: 사용자가 좋아요를 누른 직후 앱을 종료하더라도, WorkManager가 백그라운드에서 작업을 끝까지 완수하여 데이터 손실을 방지합니다.

---

### 3. 댓글: 고도화된 UX 및 견고한 아키텍처
댓글 시스템은 중첩된 인터랙션과 정교한 UI 효과를 처리하는 데 중점을 두어 기술적으로 깊이 있게 구현되었습니다.

* **MVI (Model-View-Intent) 패턴**: 엄격한 MVI 아키텍처를 도입하여 복잡한 UI 상태(Loading, Success, Failure)와 사용자 액션(Reply, Delete, Long-press)을 예측 가능한 방식으로 관리합니다.
* **기기 독립적 UI 좌표 계산**:
    * `positionInWindow()`를 활용한 커스텀 좌표 계산 로직을 개발하여 중첩된 답글 작성 시의 포커스 처리를 정교화했습니다.
    * 바텀시트(BottomSheet)와 선택된 댓글 행 사이의 상대적 오프셋을 계산하여, 다양한 기기 크기와 시스템 바 설정에서도 일관되게 동작하는 안정적인 **블러 오버레이 효과**를 구현했습니다.

---

### 💡 주요 개발 집중 사항
- **성능 최적화**: Compose의 `@Immutable` 및 `@Stable` 어노테이션을 적극적으로 활용하여 피드 및 댓글과 같은 복잡한 리스트에서 불필요한 리컴포지션(Re-composition)을 최소화했습니다.
- **확장성**: 멀티 모듈 아키텍처로 설계하여 피드, 인증, 소셜 모듈이 서로 독립성을 유지하고 높은 테스트 가능성을 가질 수 있도록 구축했습니다.