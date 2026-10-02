import tensorflow as tf
model=tf.keras.models.load_model("waste2reward.keras")
test=tf.keras.utils.image_dataset_from_directory("dataset/test",image_size=(224,224),batch_size=32)
print(model.evaluate(test))
