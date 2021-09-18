        /*
         * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
         *
         * Licensed under the Apache License, Version 2.0 (the "License");
         * you may not use this file except in compliance with the License.
         * You may obtain a copy of the License at
         *
         *       http://www.apache.org/licenses/LICENSE-2.0
         *
         * Unless required by applicable law or agreed to in writing, software
         * distributed under the License is distributed on an "AS IS" BASIS,
         * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
         * See the License for the specific language governing permissions and
         * limitations under the License.
         */

        package org.tensorflow.lite.examples.detection;

        import android.app.AlertDialog;
        import android.app.Dialog;
        import android.content.Intent;
        import android.graphics.Bitmap;
        import android.graphics.Bitmap.Config;
        import android.graphics.Canvas;
        import android.graphics.Color;
        import android.graphics.Matrix;
        import android.graphics.Paint;
        import android.graphics.Paint.Style;
        import android.graphics.RectF;
        import android.graphics.Typeface;
        import android.graphics.drawable.ColorDrawable;
        import android.media.ImageReader.OnImageAvailableListener;
        import android.os.SystemClock;
        import android.util.Log;
        import android.util.Size;
        import android.util.TypedValue;
        import android.view.MotionEvent;
        import android.view.View;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.FrameLayout;
        import android.widget.ImageView;
        import android.widget.ListView;
        import android.widget.TextView;
        import android.widget.Toast;

        import java.io.IOException;
        import java.util.ArrayList;
        import java.util.List;

        import org.tensorflow.lite.examples.detection.adapter.FruitAdapter;
        import org.tensorflow.lite.examples.detection.customview.OverlayView;
        import org.tensorflow.lite.examples.detection.customview.OverlayView.DrawCallback;
        import org.tensorflow.lite.examples.detection.env.BorderedText;
        import org.tensorflow.lite.examples.detection.env.ImageUtils;
        import org.tensorflow.lite.examples.detection.env.Logger;
        import org.tensorflow.lite.examples.detection.env.Utils;
        import org.tensorflow.lite.examples.detection.tflite.Detector;
        import org.tensorflow.lite.examples.detection.tflite.TFLiteObjectDetectionAPIModel;
        import org.tensorflow.lite.examples.detection.tflite.Classifier;
        import org.tensorflow.lite.examples.detection.tflite.YoloV4Classifier;
        import org.tensorflow.lite.examples.detection.tracking.MultiBoxTracker;

        /**
         * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
         * objects.
         */
        public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {
          private static final Logger LOGGER = new Logger();

          // Configuration values for the prepackaged SSD model.
          private static final int TF_OD_API_INPUT_SIZE = 416;
          private static final boolean TF_OD_API_IS_QUANTIZED = false;
          private static final String TF_OD_API_MODEL_FILE = "yolo-fastest-final.tflite";
          // private static final String TF_OD_API_MODEL_FILE = "yolo-fastest_metadata.tflite";
          private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/coco.txt";
          // private static final String TF_OD_API_LABELS_FILE = "classes.txt";
          private static final DetectorMode MODE = DetectorMode.TF_OD_API;
          // Minimum detection confidence to track a detection.
          private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.85f;
          private static final boolean MAINTAIN_ASPECT = false;
          private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
          private static final boolean SAVE_PREVIEW_BITMAP = false;
          private static final float TEXT_SIZE_DIP = 10;
          OverlayView trackingOverlay;
          private Integer sensorOrientation;

          private Classifier detector;

          private long lastProcessingTimeMs;
          private Bitmap rgbFrameBitmap = null;
          private Bitmap croppedBitmap = null;
          private Bitmap cropCopyBitmap = null;

          private boolean computingDetection = false;

          private long timestamp = 0;

          private Matrix frameToCropTransform;
          private Matrix cropToFrameTransform;

          private MultiBoxTracker tracker;

          private BorderedText borderedText;

          private List<Classifier.Recognition> boxes = null;

          private RectF detectedBox = null;

          private FrameLayout frameLayout;

          @Override
          public void onPreviewSizeChosen(final Size size, final int rotation) {
            frameLayout = findViewById(R.id.container);

            final float textSizePx =
                    TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
            borderedText = new BorderedText(textSizePx);
            borderedText.setTypeface(Typeface.MONOSPACE);

            tracker = new MultiBoxTracker(this);

            int cropSize = TF_OD_API_INPUT_SIZE;

            try {
              detector =
                      YoloV4Classifier.create(
                              getAssets(),
                              TF_OD_API_MODEL_FILE,
                              TF_OD_API_LABELS_FILE,
                              TF_OD_API_IS_QUANTIZED);
//      detector =
//          TFLiteObjectDetectionAPIModel.create(
//              this,
//              TF_OD_API_MODEL_FILE,
//              TF_OD_API_LABELS_FILE,
//              TF_OD_API_INPUT_SIZE,
//              TF_OD_API_IS_QUANTIZED);
              cropSize = TF_OD_API_INPUT_SIZE;
            } catch (final IOException e) {
              e.printStackTrace();
              LOGGER.e(e, "Exception initializing Detector!");
              Toast toast =
                      Toast.makeText(
                              getApplicationContext(), "Detector could not be initialized", Toast.LENGTH_SHORT);
              toast.show();
              finish();
            }

            previewWidth = size.getWidth();    // 680
            previewHeight = size.getHeight();  // 480

            sensorOrientation = rotation - getScreenOrientation();
            LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

            LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
            rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
            croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

            frameToCropTransform =
                    ImageUtils.getTransformationMatrix(
                            previewWidth, previewHeight,
                            cropSize, cropSize,
                            sensorOrientation, MAINTAIN_ASPECT);

            cropToFrameTransform = new Matrix();
            frameToCropTransform.invert(cropToFrameTransform);

            trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
            trackingOverlay.addCallback(
                    new DrawCallback() {
                      @Override
                      public void drawCallback(final Canvas canvas) {
                        tracker.draw(canvas);
                        if (isDebug()) {
                          tracker.drawDebug(canvas);
                        }
                      }
                    });

            tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
          }

          @Override
          protected void processImage() {
            ++timestamp;
            final long currTimestamp = timestamp;
            trackingOverlay.postInvalidate();

            // No mutex needed as this method is not reentrant.
            if (computingDetection) {
              readyForNextImage();
              return;
            }
            computingDetection = true;
            // LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

            rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

            readyForNextImage();

            final Canvas canvas = new Canvas(croppedBitmap);
            canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
            // For examining the actual TF input.
            if (SAVE_PREVIEW_BITMAP) {
              ImageUtils.saveBitmap(croppedBitmap);
            }

            runInBackground(
                    new Runnable() {
                      @Override
                      public void run() {
                        // LOGGER.i("Running detection on image " + currTimestamp);
                        final long startTime = SystemClock.uptimeMillis();
                        final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
                        lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                        cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
                        final Canvas canvas = new Canvas(cropCopyBitmap);
                        final Paint paint = new Paint();
                        paint.setColor(Color.RED);
                        paint.setStyle(Style.STROKE);
                        paint.setStrokeWidth(2.0f);

                        float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                        switch (MODE) {
                          case TF_OD_API:
                            minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                            break;
                        }

                        final List<Classifier.Recognition> mappedRecognitions =
                                new ArrayList<Classifier.Recognition>();

                        for (final Classifier.Recognition result : results) {
                          final RectF location = result.getLocation();
                          if (location != null && result.getConfidence() >= minimumConfidence) {
                            canvas.drawRect(location, paint);

                            cropToFrameTransform.mapRect(location);

                            result.setLocation(location);
                            mappedRecognitions.add(result);
                          }
                        }

                        boxes = mappedRecognitions;

                        tracker.trackResults(mappedRecognitions, currTimestamp);
                        trackingOverlay.postInvalidate();

                        computingDetection = false;
                      }
                    });
          }

          @Override
          protected int getLayoutId() {
            return R.layout.tfe_od_camera_connection_fragment_tracking;
          }

          @Override
          protected Size getDesiredPreviewFrameSize() {
            return DESIRED_PREVIEW_SIZE;
          }

          // Which detection model to use: by default uses Tensorflow Object Detection API frozen
          // checkpoints.
          private enum DetectorMode {
            TF_OD_API;
          }

          @Override
          protected void setUseNNAPI(final boolean isChecked) {
            runInBackground(
                    () -> {
                      try {
                        detector.setUseNNAPI(isChecked);
                      } catch (UnsupportedOperationException e) {
                        LOGGER.e(e, "Failed to set \"Use NNAPI\".");
                        runOnUiThread(
                                () -> {
                                  Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                      }
                    });
          }

          @Override
          protected void setNumThreads(final int numThreads) {
            runInBackground(() -> detector.setNumThreads(numThreads));
          }

          @Override
          public boolean onTouchEvent(MotionEvent event) {
            int x = (int)event.getX();
            int y = (int)event.getY();

            switch(event.getAction())
            {
              case MotionEvent.ACTION_DOWN:
                Log.d("touch", "x: " + x + " y: " + y);

                String fruitName = null;

                Bitmap selectedBitmap = null;
                float leftRatio = -1, topRatio = -1, rightRatio = -1, bottomRatio = -1;
                if(boxes != null) {
                  for (final Classifier.Recognition box : boxes) {
                    final RectF location = box.getLocation(); // location of the box

                    // there is a problem in output boxes produced by the model
                    leftRatio = (previewHeight - location.bottom) / previewHeight;
                    topRatio = location.left / previewWidth;
                    rightRatio = (previewHeight - location.top) / previewHeight;
                    bottomRatio = location.right / previewWidth;

                    tracker.getFrameToCanvasMatrix().mapRect(location);

                    if (x >= location.left && x <= location.right && y >= location.top && y <= location.bottom) {
                      fruitName = box.getTitle();
                      selectedBitmap = cropCopyBitmap;
                      break;
                    }
                  }

                  // if the object is detected and the box is touched,
                  // crop the image and detect its ripeness
                  if (selectedBitmap != null && leftRatio != -1) {
                    int width = (int) ((rightRatio - leftRatio) * 300);
                    int height = (int) ((bottomRatio - topRatio) * 300);

                    Bitmap croppedBitmap = cropBitmap(selectedBitmap, (int) (leftRatio * 300), (int) (topRatio * 300), width, height);

                    // make the size of the cropped image to 300x300 to fit our model
                    croppedBitmap = Utils.processBitmap(croppedBitmap, 300);

                    // save the current screen bitmap
                    ImageUtils.saveBitmap(cropCopyBitmap, "screenshot.jpg");
                    ImageUtils.saveBitmap(croppedBitmap, "croppedFruit.jpg");

                    // pass ratios to RipenessActivity
                    float[] croppedBoxRatios = {leftRatio, topRatio};

                    isSelectedFruitTrueDialog(fruitName, croppedBoxRatios);
                  }
                }

                break;
            }

            return super.onTouchEvent(event);
          }

          private Bitmap cropBitmap(Bitmap bitmap, int left, int top, int width, int height) {
            return Bitmap.createBitmap(bitmap, left, top, width, height);
          }

          private void isSelectedFruitTrueDialog(String fruit, float[] croppedBoxRatios) {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.check_fruit_dialog);
            makeDialogBackgroundTransparent(dialog);

            TextView content = dialog.findViewById(R.id.check_fruit_dialog_content);
            content.setText("Are you sure this is an " + fruit + "?");

            ImageView selectedFruitFalse = dialog.findViewById(R.id.check_fruit_dialog_no);
            // if the detected fruit is wrong
            selectedFruitFalse.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                dialog.dismiss();

                String[] fruits = {"Apple",
                        "Peach",
                        "Mango",
                        "Orange",
                        "Tomato"};

                openFruitOptionDialog(fruits, croppedBoxRatios);
              }
            });

            ImageView selectedFruitTrue = dialog.findViewById(R.id.check_fruit_dialog_yes);
            // if the detected fruit is correct
            selectedFruitTrue.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                // send cropped data to the server
                goToRipenessActivity(fruit, croppedBoxRatios);
                dialog.dismiss();
              }
            });

            dialog.show();
          }

          private void openFruitOptionDialog(String[] fruits, float[] croppedBoxRatios) {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.fruit_dialog_list_view);
            makeDialogBackgroundTransparent(dialog);
            ListView listView = dialog.findViewById(R.id.lv_assignment_users);
            TextView tv = dialog.findViewById(R.id.tv_popup_title);
            ArrayAdapter arrayAdapter = new FruitAdapter(this, R.layout.fruit_dialog_list_view, fruits);
            listView.setAdapter(arrayAdapter);
            listView.setOnItemClickListener((adapterView, view, which, l) -> {
              Log.d("Dialog", "showAssignmentsList: " + fruits[which]);
              // send the fruitBitmap to the server
              goToRipenessActivity(fruits[which], croppedBoxRatios);
              dialog.dismiss();
            });
            dialog.show();
          }

          private void makeDialogBackgroundTransparent(Dialog dialog) {
            if (dialog.getWindow() != null) {
              dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // this is optional
            }
          }

          private void goToRipenessActivity(String fruit, float[] croppedBoxRatios) {
            Intent intent = new Intent(this, RipenessActivity.class);
            intent.putExtra("fruitName", fruit);
            intent.putExtra("croppedBoxRatios", croppedBoxRatios);
            startActivity(intent);
          }
        }