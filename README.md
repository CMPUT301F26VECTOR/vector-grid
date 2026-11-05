# vector-grid

A campsite reservation app for Android. Campsite owners list their campsites and run lotteries for them. Entrants browse campsites, join a lottery's waiting list, and are drawn at random, so nobody has to race to book first.

Built for CMPUT 301 (Fall 2026) by team CMPUT301F26VECTOR with Kotlin, Jetpack Compose and Firebase.

- [Using the app](#using-the-app)
- [Testing the app](#testing-the-app)
- [Working on the code](#working-on-the-code)
- [Google Maps API key](#google-maps-api-key)
- [FAQ](#faq)

---

## Using the app

### What you need
- An Android phone or emulator running **Android 7.0 or newer**, with **Google Play services**, which maps need.
- An internet connection.
- An APK from the team, or a build you make yourself (see [Working on the code](#working-on-the-code)).

You don't need a Google account, an API key or a location permission. The app never asks for your own location; maps only show where campsites are.

### As an entrant
1. Open the app and tap **Entrant**. Your device is your identity, so there's no username or password.
2. **Browse Campsites** lists every campsite. Search by name, campground or area, or tap the filter icon to filter by location, dates and number of guests.
3. Tap a campsite to see its details: rating, location, a map of where it is, services and price.
   - **Directions** opens Google Maps with a route to the campsite.
   - **Open in Google Maps** shows its pin there.
4. If the campsite has an open lottery, tap **Join** to get on its waiting list, or **Leave** to come off it. A card's "Lottery closes in N days" tag shows how long registration stays open.
5. **My Reservations & Entries > Waitlisted** lists every waiting list you're on.
6. From the entrant home you can:
   - edit your name, email and phone number;
   - read the **App Policy and Guidelines**;
   - **Delete Profile**.

### As a campsite owner
1. Open the app, tap **Campsite Owner**, then log in, or tap **Create Account** to sign up with an email and password.
2. **Register a Campsite**: fill in the details, then tap **Set location on map**.
   - Tap the map to drop a pin, or hold the pin to drag it, then tap **Save location**.
   - If the Location box is empty, it fills in with the place name.
   - The pin is optional; a campsite without one shows "No location yet".
3. **My Campsites** lists your campsites. Tap one to open its details, where you can:
   - **Change location**: move the map pin.
   - **Create lottery**: set the registration period, the booking period, the number of winners and, optionally, a waiting-list limit.
   - **View waitlist and winners**: see who joined. Once registration has ended, the winners are drawn at random when you open this screen.

### If something looks wrong
- **The map is an empty grey grid:** your copy of the app was built without a working Google Maps key. Ask the team for a build that has one. See the [FAQ](#faq).
- **No campsites appear:** check your internet connection. The list loads from the cloud.

---

## Testing the app

### Automated tests
From the `Project_halfway` folder:

    ./gradlew testDebugUnitTest          # unit tests, no emulator needed
    ./gradlew connectedDebugAndroidTest  # UI tests, needs a running emulator

- **Where they are:** unit tests in `app/src/test`, UI tests in `app/src/androidTest`.
- **No Maps key needed:** the UI tests draw placeholders instead of real maps (`LocalShowRealMaps`), and the location picker uses a fake map (`FakePickerMap`).
- **Results:** reports are in `app/build/reports/`.

**Known issue:** `CampsiteOwnerAuthentication.testingCreateAccount` signs up with a fixed email address, so it fails on every run after the first. Change the email in the test before running it.

### Manual testing
1. Use an emulator with a **Google Play** system image, or a real phone, and a build with a working Maps key.
2. **As an owner:** create an account, register 2–3 campsites with map pins, and create a lottery whose registration period includes today. For locations, try:
   - Jasper: 52.8507, -118.0770
   - Waterton: 49.0491, -113.9089
   - Banff: 51.1913, -115.5367
3. **As an entrant:**
   - search and filter for those campsites;
   - open one and check the map and Directions;
   - join and leave the lottery.
4. **Back as the owner:** open **View waitlist and winners** to see the entrant who joined.

Each sign-up needs a new email address, because Firebase refuses duplicates.

---

## Working on the code

### Set-up
1. Clone the repo and open the **`Project_halfway`** folder in Android Studio. The app targets Java 17, which Android Studio's bundled JDK provides.
2. Add the Google Maps key to `Project_halfway/local.properties`. See [Google Maps API key](#google-maps-api-key).
3. Run **File > Sync Project with Gradle Files**, then run the app.

`app/google-services.json` (the Firebase configuration) is already in the repo, so Firebase works without extra set-up.

Map code: `LocationPickerDialog.kt` (the pin picker), `CampsiteMapPreview.kt` (the map on the details card), `GoogleMapsLinks.kt` (Directions) and `ReverseGeocoder.kt` (place names).

### Conventions
- **File names** use UpperCamelCase and are named after the file's main class or composable. No spaces or symbols.
- **Every file starts with a comment** describing its purpose and any outstanding issues. Every public class and function has a KDoc comment. `./gradlew :app:dokkaGenerate` builds the docs locally; on every push to `main`, they are published to the [Dokka site](https://cmput301f26vector.github.io/vector-grid/) and the wiki's Code Documentation page.
- **Work on a branch and open a pull request into `main`.** Ask a teammate to review it before merging, and name the user story (for example US 02.01.02) in the PR.

---

## Google Maps API key

Only people who **build** the app need this. People who just use it don't.

### 1. Create the key (one person, once)
1. In the [Google Cloud console](https://console.cloud.google.com/), create a project. Maps only load once the project has a billing account. Check the current [Maps SDK for Android pricing](https://developers.google.com/maps/documentation/android-sdk/usage-and-billing), and set a **budget alert** under **Billing > Budgets & alerts**.
2. Go to **APIs & Services > Library** and enable **Maps SDK for Android**.
3. Go to **APIs & Services > Credentials > Create credentials > API key**, then restrict the key:
   - **Application restrictions:** Android apps. Add the package name `com.example.project_halfway` together with each builder's debug SHA-1 (step 2).
   - **API restrictions:** Maps SDK for Android only.
4. Share the key privately, never in the repo or a public channel.

### 2. Add your SHA-1 to the key (everyone who builds the app)
Each computer signs its builds with its own debug certificate. In `Project_halfway`, run:

    ./gradlew signingReport

Copy the `SHA1` line under `Variant: debug`, and send it to the person who manages the key. Changes can take a few minutes to start working.

### 3. Put the key in local.properties (everyone who builds the app)
Add this line to `Project_halfway/local.properties`, which is git-ignored:

    MAPS_API_KEY=AIza...your key...

`app/build.gradle.kts` reads the key and puts it into the app's manifest. If the key is missing, the build prints `MAPS_API_KEY is missing from local.properties` and maps show a grey grid. If the key is wrong, or your SHA-1 isn't on it, Logcat shows `Authorization failure` from the Maps SDK.

---

## FAQ

**Do people using the app need their own API key?**
No. The key is put into the app when it's built, so every copy of that build carries it. When a map opens, the app sends the key to Google, and usage is billed to the team's Cloud project. Users never see or enter a key.

**Does every user need a SHA-1 certificate?**
No. The SHA-1 is the fingerprint of the certificate that **signs** the app, and the person who builds the app does the signing. The fingerprint travels inside the APK. Google checks it so that a copied key won't work in anyone else's app. Only people who build the app need their SHA-1 added: each teammate, the TA if they build from source, and the release certificate for a published app.

**Why is my map grey?**
The build has no working key. The usual causes:
- `MAPS_API_KEY` is missing from `local.properties`;
- your SHA-1 isn't added to the key;
- Maps SDK for Android isn't enabled;

Check Logcat for `Maps SDK` errors.

**How is a campsite's location stored?**
As two numbers, `latitude` and `longitude`, on the campsite's Firestore document, next to the `location` text. Both are 0 when the owner hasn't set a pin.

---

## Acknowledgement

This README was drafted with the help of an AI tool, then reviewed by a human.

**Tool:**
> Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. Used through Claude Code on November 4, 2026, by Samarth Sathya Karthik. https://claude.ai

**Role of the tool:** Claude wrote the instructions for using, testing and working on the app, the Google Maps API key setup and the FAQ, based on the app's codebase and structure.
