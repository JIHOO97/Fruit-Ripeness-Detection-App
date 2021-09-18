package org.tensorflow.lite.examples.detection;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.examples.detection.dialog.LoadingDialog;
import org.tensorflow.lite.examples.detection.env.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class RipenessActivity extends AppCompatActivity {
    private static final int INPUT_SIZE = 150;
    private static final int MAX_TIMEOUT_DURATION = 120000;
    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;

    private ImageView screenImage;
    private RequestQueue mQueue;
    private LoadingDialog loadingDialog;
    private Dialog informationDialog;
    private ToggleButton toggleButton;
    private Bitmap ripenessBitmap;
    private float[] croppedBoxRatios;
    private Paint paint;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ripeness_layout);

        String fruitName = getIntent().getStringExtra("fruitName");
        croppedBoxRatios = getIntent().getFloatArrayExtra("croppedBoxRatios");

        loadingDialog = new LoadingDialog(this);
        loadingDialog.startLoadingDialog();

        informationDialog = new Dialog(this);

        // show information dialog
        toggleButton = (ToggleButton) findViewById(R.id.information_toggle_button) ;
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(toggleButton.isChecked()) {
                    openFruitOptionDialog(fruitName);
                } else {
                    dismissInformationDialog();
                }
            }
        });

        screenImage = (ImageView) findViewById(R.id.screen_imageview);
        String screenShotFileName = "screenshot.jpg";
        Bitmap screenShotBitmap = loadFruitBitmap(screenShotFileName);
        ripenessBitmap = screenShotBitmap.copy(Bitmap.Config.ARGB_8888, true); // to put it into the canvas
        screenImage.setImageBitmap(screenShotBitmap);

        // get information about the cropped image
        String croppedFruitFileName = "croppedFruit.jpg";
        Bitmap croppedFruitBitmap = loadFruitBitmap(croppedFruitFileName);
        int[][][][] fruitBitmapToArray = getFruitArray(croppedFruitBitmap);
        JSONObject fruitArrayToObject = getFruitJSONObject(fruitBitmapToArray, fruitName);

        // send request to server
        mQueue = Volley.newRequestQueue(this);
        getRipeness(fruitArrayToObject);

        // set the paint to draw text
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(254,203,58)); // Text Color
        paint.setStrokeWidth(5 / getResources().getDisplayMetrics().density);
    }

    private Bitmap loadFruitBitmap(String filename) {
        final String root = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "tensorflow";
        final File myDir = new File(root);
        final File file = new File(myDir, filename);
        String filePath = file.getPath();
        Bitmap screenBitmap = BitmapFactory.decodeFile(filePath);

        return screenBitmap;
    }

    private int[][][][] getFruitArray(Bitmap croppedFruitBitmap) {
        int[] croppedFruitPixels = new int[INPUT_SIZE * INPUT_SIZE];
        croppedFruitBitmap = Utils.processBitmap(croppedFruitBitmap, INPUT_SIZE);
        croppedFruitBitmap.getPixels(croppedFruitPixels, 0, croppedFruitBitmap.getWidth(), 0, 0, croppedFruitBitmap.getWidth(), croppedFruitBitmap.getHeight());

        int pixel = 0;
        int[][][][] ripenessInputArray = new int[1][INPUT_SIZE][INPUT_SIZE][3];

        for (int i = 0; i < INPUT_SIZE; ++i) {
            for (int j = 0; j < INPUT_SIZE; ++j) {
                int index = 0;
                final int val = croppedFruitPixels[pixel++];
                ripenessInputArray[0][i][j][index++] = (((val >> 16) & 0xFF));
                ripenessInputArray[0][i][j][index++] = (((val >> 8) & 0xFF));
                ripenessInputArray[0][i][j][index++] = ((val & 0xFF));
                // Log.d("RipenessFruitArray", "[" + )
            }
        }

        return ripenessInputArray;
    }

    private JSONObject getFruitJSONObject(int[][][][] ripenessInputArray, String fruitName) {
        JSONObject object = new JSONObject();
        try {
            int index = 0;
            for(int i = 0; i < INPUT_SIZE; i++)
            {
                for(int j = 0; j < INPUT_SIZE; j++)
                {
                    for(int r = 0; r < 3; r++)
                    {
                        object.put("user" + index++, ripenessInputArray[0][i][j][r]);
                    }
                }
            }
            object.put("fruitName", fruitName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object;
    }

    private void getRipeness(JSONObject jsonFruitObject) {
//        String url = "http://192.168.200.102:5000";
        String url = "http://192.168.55.120:5000";

        Log.d("RipenessServer", "Sending Request...");

        // Request a string response from the provided URL.
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonFruitObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingDialog.dismissDialog();

                Log.d("RipenessServer", response.toString());

                JSONArray jsonArray = null;
                try {
                    jsonArray = response.getJSONArray("prediction");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String ripeness = null;
                try {
                    ripeness = getRipenessPercentage(jsonArray.get(0).toString());
                    Log.d("RipenessServer", ripeness);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                TextView ripenessPercentage = findViewById(R.id.ripeness_percentage);
                ripenessPercentage.setText(ripeness);

                if(croppedBoxRatios.length == 4) {
                    float leftRatio = croppedBoxRatios[0];
                    float topRatio = croppedBoxRatios[1];
                    float rightRatio = croppedBoxRatios[2];
                    float bottomRatio = croppedBoxRatios[3];

                    float[] pnts = new float[16];
                    pnts[0] = leftRatio*300;
                    pnts[1] = topRatio*300;
                    pnts[2] = rightRatio*300;
                    pnts[3] = topRatio*300;
                    pnts[4] = leftRatio*300;
                    pnts[5] = bottomRatio*300;
                    pnts[6] = rightRatio*300;
                    pnts[7] = bottomRatio*300;

                    pnts[8] = leftRatio*300;
                    pnts[9] = topRatio*300;
                    pnts[10] = leftRatio*300;
                    pnts[11] = bottomRatio*300;
                    pnts[12] = rightRatio*300;
                    pnts[13] = topRatio*300;
                    pnts[14] = rightRatio*300;
                    pnts[15] = bottomRatio*300;

                    Canvas canvas = new Canvas(ripenessBitmap);
                    canvas.drawLines(pnts, 0, 16, paint);
                    // canvas.drawRect(leftRatio*300, topRatio*300, rightRatio*300, bottomRatio*300,paint);
                    screenImage.setImageBitmap(ripenessBitmap);
                }

                // save screenImage to sharedpreference
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap currentScreenBitmap = takeScreenShot(getRootView());
                        saveBitmapToSharedPreference(currentScreenBitmap);
                    }
                }, 1000);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                loadingDialog.dismissDialog();
                Log.d("RipenessServer", "Error: " + error);
            }
        }) {
            @Override       //Send Header
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("content-type", "application/json");

                return params;
            }
        };

        // set the timeout of the request
        request.setRetryPolicy(new DefaultRetryPolicy(
                MAX_TIMEOUT_DURATION,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the RequestQueue.
        mQueue.add(request);
    }

    private String getRipenessPercentage(String jsonArray) {
        String ripePercentage = "";
        for(int i = 1; i < jsonArray.length(); ++i) {
            if(jsonArray.charAt(i) == ',') break;
            ripePercentage += jsonArray.charAt(i);
        }

        float floatRipePercentage = Float.parseFloat(ripePercentage);
        // floatRipePercentage = (float) ((float)Math.round(floatRipePercentage * 100d) / 100d);
        floatRipePercentage *= 100;

        ripePercentage = String.format("%.2f", floatRipePercentage);

        return ripePercentage + "%";
    }

    private void openFruitOptionDialog(String fruitName) {
        informationDialog.setContentView(R.layout.fruit_information);
        makeDialogBackgroundTransparent(informationDialog);
        enableOutsideTouchOnDialog();

        // set text according to the fruit type
        TextView informationUnripeFruitName = (TextView) informationDialog.findViewById(R.id.information_unripe_fruit_name);
        TextView informationUnripeContent = (TextView) informationDialog.findViewById(R.id.information_unripe_content);
        TextView informationGoodFruitName = (TextView) informationDialog.findViewById(R.id.information_good_fruit_name);
        TextView informationGoodContent = (TextView) informationDialog.findViewById(R.id.information_good_content);

        String unripeFruitName = null, unripeContent = null, goodFruitName = null, goodContent = null;

        switch(fruitName) {
            case "Apple":
                unripeFruitName = "Unripe apple may be";
                unripeContent = "- Toxic \n- Bad for pancrease \n- Sour";
                goodFruitName = "How are apples good for your health?";
                goodContent = "- Helps with weight loss \n- Lowers risk of heart disease \n- Lowers risk of diabetes";

                break;
            case "Orange":
                unripeFruitName = "Unripe orange is generally safe to eat, but eating too many oranges may be";
                unripeContent = "- Cause abdominal cramps\n- Lead to diarrhea";
                goodFruitName = "How are oranges good for your health?";
                goodContent = "- High Vitamin C\n- Healthy immune system\n- Prevents skin damange\n- Lowers cholesterol\n- Controls blood sugar level";

                break;
            case "Tomato":
                unripeFruitName = "Unripe tomato contains tomatine and solanine which are toxic that may";
                unripeContent = "- Cause fever\n- Cause abdominal pain\n- Cause diarrhea\n- Cause vomiting";
                goodFruitName = "How are tomatoes good for your health?";
                goodContent = "- Reduce heart disease\n- Reduce cancer\n- Great source of vitamin C\n- Great source of potassium";

                break;
            case "Mango":
                unripeFruitName = "Eating unripe mango in excess may cause";
                unripeContent = "- Throat irritation\n- Indigestion\n- Dysentery\n- Adbominal colic";
                goodFruitName = "How are mangoes good for your health?";
                goodContent = "- High antioxidants\n- Boost immunity\n- Improve digestive Health\n- Support eye and heart health";

                break;
        }

        if(unripeFruitName != null) {
            informationUnripeFruitName.setText(unripeFruitName);
            informationUnripeContent.setText(unripeContent);
            informationGoodFruitName.setText(goodFruitName);
            informationGoodContent.setText(goodContent);
        }

        informationDialog.show();
    }

    private void dismissInformationDialog() {
        informationDialog.dismiss();
    }

    private void makeDialogBackgroundTransparent(Dialog dialog) {
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // this is optional
        }
    }

    private void enableOutsideTouchOnDialog() {
        getRootView().post(new Runnable() {
            @Override
            public void run() {
                Window dialogWindow = informationDialog.getWindow();
                // Make the dialog possible to be outside touch
                dialogWindow.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
                dialogWindow.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                getRootView().invalidate();
            }
        });
    }

    private View getRootView() {
        return getWindow().getDecorView().getRootView();
    }

    private void saveBitmapToSharedPreference(Bitmap bitmap) {
        String storedBitmaps = StartingActivity.sharedPreferences.getString("bitmaps", "");

        // convert bitmap to string
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 99, byteArrayOutputStream);
        String stringBitmap = new String(Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT));

        if(!storedBitmaps.equals("")) {
            storedBitmaps += ("DES01" + stringBitmap);
        } else {
            storedBitmaps = stringBitmap;
        }

        SharedPreferences.Editor editor = StartingActivity.sharedPreferences.edit();
        editor.putString("bitmaps", storedBitmaps);
        editor.apply();
    }

    public Bitmap takeScreenShot(View view) {
        // configuramos para que la view almacene la cache en una imagen
        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        view.buildDrawingCache();

        if(view.getDrawingCache() == null) return null; // Verificamos antes de que no sea null

        // utilizamos esa cache, para crear el bitmap que tendra la imagen de la view actual
        Bitmap snapshot = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        view.destroyDrawingCache();

        return snapshot;
    }
}