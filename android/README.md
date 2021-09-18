# How to run Fripe

### Overview

This is a camera app that continuously detects the objects (bounding boxes and
classes) in the frames seen by your device's back camera, using a YOLO Fastest model. Furthermore, after pressing on a certain object, the application will call on a server to run the ripeness classification model. These instructions
walk you through building and running the demo on an Android device.

The model file is already in the assets folder.

Application can run either on device or emulator.

<!-- TODO(b/124116863): Add app screenshot. -->

## Build the demo using Android Studio

### Prerequisites

*   If you don't have already, install
    **[Android Studio](https://developer.android.com/studio/index.html)**,
    following the instructions on the website.

*   You need an Android device and Android development environment with minimum
    API 21.

*   Android Studio 3.2 or later.

### Building

*   Open Android Studio, and from the Welcome screen, select Open an existing
    Android Studio project.

*   If it asks you to do a Gradle Sync, click OK.

*   You may also need to install various platforms and tools, if you get errors
    like "Failed to find target with hash string 'android-21'" and similar.
    Click the `Run` button (the green arrow) or select `Run > Run 'android'`
    from the top menu. You may need to rebuild the project using `Build >
    Rebuild` Project.

*   If it asks you to use Instant Run, click Proceed Without Instant Run.

*   Also, you need to have an Android device plugged in with developer options
    enabled at this point. See
    **[here](https://developer.android.com/studio/run/device)** for more details
    on setting up developer devices.

### Yolo Tiny
In \app\src\main\java\org\tensorflow\lite\examples\detection\tflite\YoloV4Classifier.java, make sure that the variable
isTiny is equal to **TRUE**. This will allow you to run the most updated and efficient yolo-fastest model.

### Ripeness Activity
This file is for finding the ripeness of a fruit. It also controls the communication with the server for getting ripeness data back.

### Detector Activty
This controls the communication between the model and the camera.

### Model used

Downloading, extraction and placing it in assets folder has been managed
automatically by download.gradle.

### Additional Note

_Please do not delete the assets folder content_._ Without the assets folder,
it is not possible to run the model._
