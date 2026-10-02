# How to build the APK

## Method 1 — GitHub Actions (Recommended if you don't want Android Studio)

1. Create a new repository on GitHub
2. Push this entire project folder to the repository
3. Go to the **Actions** tab on GitHub
4. The workflow **"Build APK"** will run automatically
5. When it finishes, click the workflow run → download the artifact **Waste2RewardAI-debug**
6. Inside you will find `app-debug.apk`

You can also click **"Run workflow"** manually anytime from the Actions tab.

## Method 2 — Local command line

Requires JDK 17 + Android SDK:

```bash
echo "sdk.dir=/path/to/Android/Sdk" > local.properties
chmod +x gradlew
./gradlew assembleDebug
```

APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

## Method 3 — Android Studio

Open the project folder in Android Studio → Build → Build APK(s)
