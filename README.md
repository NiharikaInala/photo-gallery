# Photo Gallery

This Android app is basically a clean, lightweight way to browse through a collection of photos. The main screen just shows a simple paginated list of filenames, and tapping on one takes you to a detail view where you can see the actual image alongside the author's info.

Under the hood, I wanted to keep things practical by properly separating the networking, app state, and the Jetpack Compose UI. It handles all the usual edge cases too which include loading spinners, empty states, error handling with a retry button, smooth pagination as you scroll, standard back navigation, and it survives configuration changes without losing your place.

## Features

- Browse photographs in an incrementally loaded list
- View each image at its original aspect ratio
- Adapt the detail layout to image orientation: landscape images are centered when space permits, while portrait images start at the top and remain scrollable
- See author information on the detail screen
- Retry catalog and image-loading failures
- Preserve the current screen during Activity recreation
- Use both toolbar and Android system Back navigation

## Technology

- Kotlin and Kotlin Coroutines
- Jetpack Compose and Material 3
- MVVM with an Activity-scoped ViewModel
- Hilt dependency injection
- Retrofit, OkHttp, and Gson
- Coil image loading
- JUnit and Compose UI tests
- JaCoCo coverage reporting

## Project Structure

The application uses a single module with focused `data`, `di`, and `ui` packages. The repository maps the remote API response into application-safe models, the ViewModel owns screen state and user actions, and stateless composables render that state.

The required legacy endpoint returns the complete filename catalog, so the app fetches it once and reveals records in batches of 20 as the user scrolls. A server-backed implementation would use the paginated v2 contract when the backend also supplies the required filename field.

## Data Source

Photograph metadata and images are provided by the [Lorem Picsum API](https://picsum.photos/).

## Screenshots

| Photo list | Photo detail loading | Photo detail |
| --- | --- | --- |
| <img width="1080" height="2340" alt="Screenshot_20260913_192206" src="https://github.com/user-attachments/assets/44d86cfe-874b-45ce-8815-2da2cb43ec3c" /> | <img width="1080" height="2340" alt="Screenshot_20260913_192240" src="https://github.com/user-attachments/assets/95213945-843b-4e57-a8a6-4187f90aaf70" /> | <img width="1080" height="2340" alt="Screenshot_20260913_192307" src="https://github.com/user-attachments/assets/abbad9b5-8fc0-4721-82c3-267c04ed6895" /> |



## UI Test Coverage

<img width="1440" height="339" alt="UI_test_coverage" src="https://github.com/user-attachments/assets/9852225a-52a4-46e5-a643-58fec7b852ba" />

<img width="1440" height="439" alt="ui_test_coverage_2" src="https://github.com/user-attachments/assets/007a509b-6f91-40cc-9371-3e06c8928512" />


## Unit Test Coverage

<img width="1440" height="439" alt="Screenshot 2026-09-13 at 8 13 03 PM" src="https://github.com/user-attachments/assets/5309904c-dfe0-4390-a8f3-60bfe6cffb15" />

<img width="1440" height="451" alt="Screenshot 2026-09-13 at 8 13 33 PM" src="https://github.com/user-attachments/assets/1b4e05ce-c66c-4c13-b3d6-fcc532d817a8" />

<img width="1440" height="252" alt="Screenshot 2026-09-13 at 8 13 17 PM" src="https://github.com/user-attachments/assets/b4eaad2b-9b34-48a7-afe6-c41a384a4ac4" />





## Demo

<img width="400" height="893" alt="Screen_recording_20260913_220123" src="https://github.com/user-attachments/assets/71eb2589-1ca5-4037-b8af-9bda285e25ae" />

## Download

A prebuilt debug APK is available:

[Download Photo Gallery APK](https://github.com/NiharikaInala/PhotoGallery/releases/download/v1.0.0/photo-gallery-debug.apk)

Android may require permission to install applications from the browser or file manager.



## Getting Started

Open the project in Android Studio, allow Gradle synchronization to complete, and run the `app` configuration on an emulator or Android device running API 24 or later.

To run the local quality checks:

```shell
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Instrumented UI tests require a connected emulator or device:

```shell
./gradlew connectedDebugAndroidTest
```
