"""Training starter for a lightweight 10-class image classifier.
Use a legally licensed dataset. Tune epochs/augmentation for the chosen dataset."""
import tensorflow as tf
from pathlib import Path

DATA = Path("dataset")
IMG=(224,224); BATCH=32
train=tf.keras.utils.image_dataset_from_directory(DATA/"train", image_size=IMG, batch_size=BATCH)
val=tf.keras.utils.image_dataset_from_directory(DATA/"validation", image_size=IMG, batch_size=BATCH)
model=tf.keras.Sequential([
 tf.keras.layers.Rescaling(1./255,input_shape=(*IMG,3)),
 tf.keras.layers.RandomFlip("horizontal"), tf.keras.layers.RandomRotation(.05),
 tf.keras.layers.Conv2D(24,3,activation="relu"), tf.keras.layers.MaxPooling2D(),
 tf.keras.layers.Conv2D(48,3,activation="relu"), tf.keras.layers.MaxPooling2D(),
 tf.keras.layers.Conv2D(96,3,activation="relu"), tf.keras.layers.GlobalAveragePooling2D(),
 tf.keras.layers.Dense(10,activation="softmax")
])
model.compile(optimizer="adam",loss="sparse_categorical_crossentropy",metrics=["accuracy"])
model.fit(train,validation_data=val,epochs=15)
model.save("waste2reward.keras")
print("Model saved. Evaluate it before deployment; do not invent an accuracy number.")
