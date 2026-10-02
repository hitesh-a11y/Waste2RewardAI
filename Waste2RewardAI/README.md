# ♻️ Waste2Reward AI

**Scan. Segregate. Recycle. Earn.**

A native Android hackathon MVP for AI-assisted waste segregation and circular-economy engagement.

## What is implemented
- Kotlin + Jetpack Compose UI
- CameraX live camera preview and capture
- Three scan modes: Single, Mixed, Rescan
- Visible-light limitation messaging (no opaque-bag/X-ray claim)
- Replaceable local classifier interface
- Deterministic demo classifier for a live hackathon demo
- Segregation scoring and verified rescan points
- Rewards prototype
- Environmental impact dashboard
- Guest mode
- Demo recycling centers
- Community waste-report prototype
- Optional Firebase Firestore scan repository
- TFLite dependency and model integration boundary
- Python/TensorFlow training starter

## Important model limitation
The included demo classifier is **not a trained production AI model** and no accuracy is claimed. It exists so the complete camera → analysis → scoring → rewards experience can be demonstrated when a trained model is unavailable. Replace `DemoWasteClassifier` with `TFLiteWasteClassifier` after validating a legally usable dataset and model.

## Open in Android Studio
1. Install Android Studio with Android SDK 35 and a JDK supported by your Android Studio release.
2. Open this `Waste2RewardAI` folder.
3. Allow Gradle to download dependencies.
4. Run on an Android 8.0+ phone/emulator with a camera.
5. Grant camera permission.
6. Tap **SCAN WASTE** → **Smart Waste Scan** → capture.

The repository cannot include a Firebase project configuration or secret/API credentials. The app remains usable in Guest/demo mode without them.

## Firebase setup (optional)
1. Create a Firebase project.
2. Add Android package `com.waste2reward.ai`.
3. Download `google-services.json` into `app/`.
4. Add the Google services Gradle plugin to the project only after Firebase is configured.
5. Enable Authentication/Firestore and apply production security rules before deployment.

The included repository safely falls back if Firebase is not configured.

## TFLite model pipeline
`ai_training/prepare_dataset.py` creates the ten required class folders. Train/evaluate with TensorFlow, convert to TFLite, then put the model at `app/src/main/assets/waste_model.tflite` and connect its exact input/output tensor contract in `TFLiteWasteClassifier`.

Required classes: Plastic, Paper, Cardboard, Glass, Metal, Organic, E-waste, Hazardous, Textile, Other.

## Demo flow
1. Open app in Guest mode.
2. Tap **SCAN WASTE**.
3. Select Mixed.
4. Capture a visible tray/table scene.
5. Demo output shows five visible objects and a mixed score.
6. Tap **Separate & Rescan**.
7. Capture again in Rescan mode.
8. The verified rescan reaches 100/100 and awards points.
9. Open Rewards and Impact.

## Honesty / safety
- A normal phone camera cannot see through a closed opaque bag.
- Demo recycling locations are labeled as demo locations.
- Reports are not claimed to reach a government department without a real integration.
- Rewards are prototype demonstrations, not guaranteed physical benefits.
- Environmental conversion factors are intentionally not presented as exact CO2 savings.
- Low-confidence handling should be implemented in the production model adapter rather than silently guessing.

## Architecture
`CameraX → classifier interface → waste items → disposal rules → segregation score → verified points → rewards`

Online services are secondary: Firebase synchronization, accounts, maps and community reporting can be added without changing the core scanner contract.

## Hackathon pitch
“Waste2Reward AI uses computer vision to identify visible waste, tells users how to segregate it, verifies improvement through a second scan, and rewards responsible segregation with Eco Points.”
