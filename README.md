## 🛠 Core Features & Technical Challenges
🇰🇷 **[Read this in Korean](./README_ko.md)**

<table table-layout="fixed">
  <tr>
    <td align="center"><b>Light Mode UI</b></td>
    <td align="center"><b>Dark Mode UI</b></td>
    <td align="center"><b>Core Interactions</b></td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/bf864a37-1277-45f2-ac3a-dc02781c8c65" width="250"><br>
      <sub>Feed list in Light Mode</sub>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/a3a84877-c5c5-453d-af4d-88460703bd3e" width="250"><br>
      <sub>Feed list in Dark Mode</sub>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/c466309f-ccab-4153-b766-bd9317dc0339" width="250"><br>
      <sub>Likes, Comments, and Replies</sub>
    </td>
  </tr>
</table>


### 1. Feed: Seamless Offline Experience (Paging 3 + RemoteMediator)
To ensure a high-quality user experience even in poor network environments, I implemented a "Local-first" strategy inspired by [Instagram's Background Prefetching](https://medium.com/instagram-engineering/improving-performance-with-background-data-prefetching-b191acb39898).

* **Offline-First Approach**: Utilizing **Android Paging 3 with RemoteMediator**, the app caches network data into a **Room database**. This allows users to browse previously loaded feeds even without an internet connection.
* **Single Source of Truth (SSOT)**: The UI only observes the local Room database, ensuring data consistency across different screens and sessions.
* **Future Roadmap**: While current focus is on offline visibility, I plan to implement a full-scale prefetching system (periodic background loading) when developing the Search/Discovery module.



---

### 2. Likes: Reliable Interaction with Optimistic UI
Instagram ensures that a "Like" is reflected immediately and synchronized reliably, regardless of network status. I replicated this using a combination of local caching and background synchronization.

* **Optimistic UI**: When a user clicks the Like button, the state is immediately updated in the local **Room DB** to provide instant feedback.
* **Guaranteed Persistence (WorkManager)**: The actual server synchronization is handled by **WorkManager**. 
    * **Constraints**: Configured with `NetworkType.CONNECTED` to ensure tasks only run when the internet is available.
    * **Reliability**: Even if the user closes the app immediately after liking a post, WorkManager guarantees the task completes in the background, preventing data loss.



---

### 3. Comments: Advanced UX & Robust Architecture
The comment system is built with a focus on deep technical implementation, specifically handling nested interactions and sophisticated UI effects.

* **MVI (Model-View-Intent) Pattern**: Implemented a strict MVI architecture to manage complex UI states (Loading, Success, Failure) and user actions (Reply, Delete, Long-press) in a predictable way.
* **Device-Independent UI Positioning**: 
    * Developed a custom coordinate calculation logic using `positionInWindow()` to handle nested reply focus.
    * By calculating relative offsets between the BottomSheet and the selected comment row, I achieved a stable **Blur Overlay effect** that works consistently across various device sizes and system bar configurations.

---

### 💡 Key Development Focus
- **Performance Optimization**: Actively using `@Immutable` and `@Stable` annotations in Compose to minimize unnecessary re-compositions in complex lists like the Feed and Comments.
- **Scalability**: Designed the architecture to be multi-module, ensuring that the Feed, Auth, and Social modules remain independent and highly testable.
