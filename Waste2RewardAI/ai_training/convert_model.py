import tensorflow as tf
converter=tf.lite.TFLiteConverter.from_saved_model("waste2reward.keras")
tflite=converter.convert()
open("waste_model.tflite","wb").write(tflite)
print("Created waste_model.tflite. Copy it to app/src/main/assets/ and implement its tensor contract in TFLiteWasteClassifier.")
