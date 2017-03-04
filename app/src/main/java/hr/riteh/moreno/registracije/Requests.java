package hr.riteh.moreno.registracije;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;


import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by moren on 02-Nov-16.
 */

public class Requests {

    private Activity activity;
    private CountDownLatch latch;

    public Requests(Activity activity){
        this.activity = activity;
    }


    CookieJar cookieJar = new CookieJar() {
        private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
        @Override
        public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
            cookieStore.put(httpUrl.host(), list);
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl httpUrl) {
            List<Cookie> cookies = cookieStore.get(httpUrl.host());
            return cookies != null ? cookies : new ArrayList<Cookie>();
        }
    };

    private final OkHttpClient client = new OkHttpClient.Builder().cookieJar(cookieJar)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS).build();


    private String postResp;

    final ArrayList<String> responses = new ArrayList<>();


    private static final String TAG = "Requests";

    public void getImage(String url, final ImageView captcha) throws Exception{

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream inputStream = response.body().byteStream();
                final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        captcha.setImageBitmap(bitmap);
                    }
                });


            }
        });
    }

    public void postForm(String city, String regNumber, String regLetters, String captcha, final Intent intent, final ProgressDialog dialog) throws Exception{

        latch = new CountDownLatch(1);

        String registration = regNumber + regLetters;

        makeRequest(city, registration, captcha, dialog);

        latch.await(1, TimeUnit.MINUTES);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        });

        Globals.data = responses;
        //intent.putStringArrayListExtra("EXTRA_ARRAY", responses);
        activity.startActivity(intent);

    }

    public void postMultipleForms(String city, String regNumber, String regLetters, String captcha, Integer checkNum, Integer checkLet, final Intent intent, final ProgressDialog dialog) throws  Exception{
        // array list sa svim kombinacijama registracija
        ArrayList<String> registrations = new ArrayList<>();

        if (checkNum >= 0){
            latch = new CountDownLatch(10);
            for (int i = 0; i<=9; i++){
                StringBuilder newReqNum = new StringBuilder(regNumber);
                newReqNum.setCharAt(checkNum, (char) (i + '0'));
                registrations.add(newReqNum.toString() + regLetters);
            }
        }

        else if (checkLet >= 0){
            latch = new CountDownLatch(26);
            for (char alphabet='A'; alphabet<='Z'; alphabet++){
                StringBuilder newRegLet = new StringBuilder(regLetters);
                newRegLet.setCharAt(checkLet, alphabet);
                registrations.add(regNumber + newRegLet);
            }
        }

        //Log.e(TAG, registrations.toString());

        for (String registration : registrations){
            makeRequest(city, registration, captcha, dialog);
        }

        latch.await(1, TimeUnit.MINUTES);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        });

        Globals.data = responses;
        //intent.putStringArrayListExtra("EXTRA_ARRAY", responses);
        activity.startActivity(intent);

    }


    public void advancedSearch(String city, List<String> numbers1, List<String> numbers2, List<String> numbers3, List<String> numbers4,
                               List<String> letters1, List<String> letters2, String captcha, Intent intent, final ProgressDialog dialog, Integer combinations) throws Exception{

        ArrayList<String> registrations = new ArrayList<>();

        latch = new CountDownLatch(combinations);

        for (String num1 : numbers1){
            for (String num2 : numbers2){
                for (String num3 : numbers3){
                    for (String num4 : numbers4){
                        for (String let1 : letters1){
                            for (String let2 : letters2){
                                String reg = num1 + num2 + num3 + num4 + let1 + let2;
                                registrations.add(reg);
                            }
                        }
                    }
                }
            }
        }

        for (String registration : registrations){
            makeRequest(city, registration, captcha, dialog);
        }

        latch.await(1, TimeUnit.MINUTES);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        });

        Globals.data = responses;
        //intent.putStringArrayListExtra("EXTRA_ARRAY", responses);
        activity.startActivity(intent);
    }


    private void makeRequest(String city, String registration, String captcha, final ProgressDialog dialog){
        String reg = city + registration;


        Date date = new Date();
        String formatDate = new SimpleDateFormat("dd.MM.yyyy").format(date);

        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("action", "get_data")
                .addFormDataPart("srch_type", "nezgoda")
                .addFormDataPart("datum", formatDate)
                .addFormDataPart("zupanija", city)
                .addFormDataPart("reg1", registration)
                .addFormDataPart("reg", reg)
                .addFormDataPart("security_code", captcha)
                .addFormDataPart("submit", "Traži")
                .build();

        Request request = new Request.Builder()
                .url("http://www.huo.hr/hrv/provjera-osiguranja/11/")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                latch.countDown();
                e.printStackTrace();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(activity.getApplicationContext(), "Došlo je do mrežne pogreške." +
                                " Ispis može biti nepotpun. ", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                postResp = response.body().string();

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.setMessage("Preostalo: " + latch.getCount());
                    }
                });

                responses.add(postResp);
                latch.countDown();
            }
        });
    }
}




