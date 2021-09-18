# Fruit Ripeness Detection App

**Watch Demo Here**

[![Watch Demo](https://img.youtube.com/vi/71kKF9EHjYE/0.jpg)](https://www.youtube.com/watch?v=71kKF9EHjYE)

## Introduction
With the increase in computational power and the improvement of machine learning models,
our team believes the method of determining fruit ripeness can be significantly simplified.
This means not having to think about large, expensive machines or carrying around
unnecessary weight. Our project aims to minimize the functionality of determining fruit
ripeness to our smartphones only.

## Objectives
**1. Accurately identify fruits in real-time**
> For the first objective, there are two factors we need to consider: accuracy and speed of the
detection. The model should provide correct information about the type of fruit and fast
enough to support real-time detection on devices. To ensure this, only quality data should be
used for training the model. Collected data should go through inspection procedure to make
sure they are correctly annotated. YOLO and SSD were object detection models that we
implemented since these models have been researched to have relatively fast inference speed
and accuracy.

**2. Precisely calculate the percentage of ripeness of fruits**
For the second objective, deep neural network models should be able to precisely detect the
ripeness of the detected fruits. More precisely, the models should be able to calculate the
ripeness of the fruits with very similar colors differently, although they look the same in our
eyes. In order to accomplish this objective, several factors should be considered to determine
the ripeness: size, weight, color characteristic, and more. Thus, separate models will be
constructed and trained for each type of fruit, with thousands of ripe and unripe fruit images.

**3. Provide a user-friendly and intuitive interface with high user-experience**
Our last objective is to minimize the complexity and maximize the usability of the
application. The goal of the application is to improve people’s lives, and this improvement
cannot be achieved without constructing a suitable medium that people can use. No matter
how efficient and accurate the object detection and ripeness classification models are, if the
experience of the application is poor, users will not use the application. Therefore, it is
important to make sure that users will have an enjoyable experience while using the mobile
application. This includes designing a user-interface that is pleasing to the eye and a seamless
experience without any disruptive behaviour or lag.

**References:** 
1. [TensorFlow 2 — Object Detection on Custom Dataset with Object Detection API](https://medium.com/swlh/image-object-detection-tensorflow-2-object-detection-api-af7244d4c34e)
2. [General approach for Custom Object Detection with Tensorflow 2](https://medium.com/mlearning-ai/general-approach-for-custom-object-detection-with-tensorflow-2-61593a67a02d)
