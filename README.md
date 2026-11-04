# vector-grid

Campsite reservation Android app for CMPUT 301 (Fall 2026), by team CMPUT301F26VECTOR. Kotlin, Jetpack Compose and Firebase Firestore.

## Build and run

1. Open the `Project_halfway` folder in Android Studio.
2. Add a Google Maps API key (next section). Without one, the app still builds and runs, but every map is an empty grey grid.
3. Run on an emulator with a **Google Play** system image, or on a real phone. Maps need Google Play services.

From the command line, inside `Project_halfway`:

```sh
./gradlew assembleDebug              # build the app
./gradlew testDebugUnitTest          # unit tests
./gradlew connectedDebugAndroidTest  # UI tests (needs a running emulator)
```

## Google Maps setup

The app uses the [Maps SDK for Android](https://developers.google.com/maps/documentation/android-sdk) through [maps-compose](https://github.com/googlemaps/android-maps-compose). Owners drop a pin for their campsite, and the campsite details show the pin with Directions and Open in Google Maps buttons. Directions use [Maps URLs](https://developers.google.com/maps/documentation/urls/get-started), which need no extra API.

### 1. Create the key (one person, once)

1. In the [Google Cloud console](https://console.cloud.google.com/), create a project. Maps only load once the project has a billing account. Check the current [Maps SDK for Android pricing](https://developers.google.com/maps/documentation/android-sdk/usage-and-billing) and set a **budget alert** under **Billing > Budgets & alerts**.
2. Go to **APIs & Services > Library** and enable **Maps SDK for Android**.
3. Go to **APIs & Services > Credentials > Create credentials > API key**, then restrict the key:
   - **Application restrictions:** Android apps. Add the package name `com.example.project_halfway` together with each teammate's debug SHA-1 certificate fingerprint (step 2 below).
   - **API restrictions:** Maps SDK for Android only.
4. Share the key with the team privately, never in the repo or a public channel.

### 2. Add your SHA-1 to the key (each teammate)

Every computer has its own debug signing key, so the Maps key only works on your computer once your SHA-1 is added. In `Project_halfway`, run:

```sh
./gradlew signingReport
```

Copy the `SHA1` line under `Variant: debug` and send it to the person who manages the key.

### 3. Put the key in local.properties (each teammate)

Add this line to `Project_halfway/local.properties`. The file is git-ignored, and Android Studio creates it:

```properties
MAPS_API_KEY=AIza...your key...
```

`app/build.gradle.kts` reads the key and puts it into `AndroidManifest.xml`. If it is missing, the build prints `MAPS_API_KEY is missing from local.properties` and maps show a grey grid. If it is wrong or not allowed for your SHA-1, Logcat shows an authorization error from the Maps SDK.

### Notes

- UI tests don't need a key: they replace real maps with placeholders (`LocalShowRealMaps`) and a fake picker map (`FakePickerMap`).
- Place names for a dropped pin come from Android's built-in Geocoder, which is free. On some emulators it finds nothing, and the coordinates are shown instead.
- The app is built for Java 17, which the Maps library requires.
