# 📸 Advanced Feed Upload Strategy (feature/feed-add)

This module implements a sophisticated feed upload mechanism designed to maximize perceived speed and ensure reliability, benchmarking Instagram's actual production strategy.

> **Inspired by [Instagram's Feed Upload UX](https://www.youtube.com/watch?v=V27XkmVPqYQ)**
> (1:23: The strategy of starting the upload as soon as a photo is taken/selected to minimize latency while the user writes a caption.)

---

## 🚀 Key Strategy: Early Uploading & Background Persistence

To eliminate the "waiting screen" after hitting the post button, this project employs the following strategies:

1. **Early Uploading**: Instead of waiting for the user to finish writing, the image upload starts immediately in the background as soon as media selection is complete.
2. **Background Persistence (WorkManager)**: By utilizing Android's `WorkManager`, the upload process is guaranteed to complete even if the user exits the app or the process is killed by the OS.

---

## 🛠 Technical Implementation: Two-Step Worker Chaining

The upload process is architected using two independent `CoroutineWorker` classes that synchronize via a local database (`FeedUploadStatus`).

### 1. UploadFeedImagesWorker
* **Role**: Handles the parallel upload of multiple images to Firebase Storage.
* **Parallel Optimization**: Uses `flatMapMerge(concurrency = 10)` to upload up to 10 images simultaneously, significantly reducing total upload time.
* **Order Preservation**: Implements indexing and `sortedBy` logic to ensure that the original selection order of images is strictly maintained despite parallel processing.
* **Resilience**: Includes a `retry(2)` policy for `IOException` to handle unstable network conditions gracefully.

### 2. UploadFeedContentWorker
* **Role**: Combines the generated `downloadUrl` list with the user's caption to finalize the feed entry in the database.
* **Reactive State Observation**: Uses `transformWhile` to reactively observe the local DB. It waits for the upload state to reach `IMAGE_SUCCESS` and then triggers the final metadata submission immediately.

---

## 🔄 Sequence Diagram & Flow

1. **Image Selection**: `UploadFeedImagesWorker` is enqueued immediately. Local state transitions to `LOADING`.
2. **Writing Caption**: While the user is typing, images are being uploaded in the background. Once finished, the state updates to `IMAGE_SUCCESS` with the URL list.
3. **Post Button Click**: 
    - If images are already uploaded (`IMAGE_SUCCESS`), `UploadFeedContentWorker` completes the post instantly.
    - If the upload is still in progress, the worker observes the local DB and automatically finishes the post the moment the URLs are ready.

---

## 💡 Why WorkManager?
- **Persistence**: Tasks survive app exits and system reboots, ensuring a high success rate for uploads.
- **Constraints**: Can be configured to run only when the device has a valid network connection.
- **Visibility**: Allows the UI to observe `WorkInfo` and provide real-time feedback (e.g., progress bars or success/failure notifications) to the user.
