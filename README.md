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
| <img width="1080" height="2340" alt="Screenshot_20260913_192206" src="https://github.com/user-attachments/assets/41c9a7a5-7962-4662-89e6-2ebc7cd1867c" />| <img width="1080" height="2340" alt="Screenshot_20260913_192240" src="https://github.com/user-attachments/assets/88af71dd-9d52-443a-849a-b7f9e78bc2db" />| <img width="1080" height="2340" alt="Screenshot_20260913_192307" src="https://github.com/user-attachments/assets/4cc3716f-3ff2-4e98-b2a8-59afc567a21a" />




## UI Test Coverage

<img width="1440" height="339" alt="UI_test_coverage" src="https://github.com/user-attachments/assets/09b21f2e-5728-4101-a907-fe170dfabb84" />

<img width="1440" height="439" alt="ui_test_coverage_2" src="https://github.com/user-attachments/assets/655807a4-60c2-4d0a-b499-e373ce581d20" />



## Unit Test Coverage

<img width="1440" height="439" alt="Screenshot 2026-09-13 at 8 13 03 PM" src="https://github.com/user-attachments/assets/7147725d-4377-4f81-8d3e-f7b57d7cd948" />

<img width="1440" height="451" alt="Screenshot 2026-09-13 at 8 13 33 PM" src="https://github.com/user-attachments/assets/199726a1-c5d7-481e-80ab-7d67dc05b41e" />

<img width="1440" height="252" alt="Screenshot 2026-09-13 at 8 13 17 PM" src="https://github.com/user-attachments/assets/33d0e785-3e39-4097-b8d2-b942760ae72a" />

## Demo

<img width="400" height="893" alt="Screen_recording_20260913_220123" src="https://github.com/user-attachments/assets/d7ac79b6-d77f-4af0-874d-031dacb0da28" />


## Download

A prebuilt debug APK is available:

[Download Photo Gallery APK](https://github.com/NiharikaInala/photo-gallery/releases/download/v1.0.0/photo-gallery-debug.apk)

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
