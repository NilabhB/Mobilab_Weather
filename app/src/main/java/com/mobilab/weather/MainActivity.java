package com.mobilab.weather;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV, regionCountryTV, temperatureTV, conditionTV, weatherReportTV, feelsLikeTV, windSpeedTV,
            humidityTV, weatherForecastTV, localTimeTV;
    private TextInputEditText cityEdt;
    private ImageView backIV, iconIV, searchIV, logOutIV;
    private RecyclerView weatherRV;
    private ArrayList<WeatherRVModel> weatherRVModelArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String cityName;
    private Switch tempSwitch;

    private View separation;

    private SwipeRefreshLayout swipeRefreshLayout;

    private Handler handler;
    private Runnable refreshRunnable;

    private boolean isFahrenheit = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
        //Objects.requireNonNull(getSupportActionBar()).hide();

        tempSwitch = findViewById(R.id.tempSwitch);
        homeRL = findViewById(R.id.idRLHome);
        loadingPB = findViewById(R.id.idPBLoading);
        cityNameTV = findViewById(R.id.idTVCityName);
        regionCountryTV = findViewById(R.id.idTVRegionCountry);
        temperatureTV = findViewById(R.id.idTVTemperature);
        feelsLikeTV = findViewById(R.id.idTVFeelsLike);
        windSpeedTV = findViewById(R.id.idTVWindSpeed);
        humidityTV = findViewById(R.id.idTVHumidity);
        conditionTV = findViewById(R.id.idTVCondition);
        weatherRV = findViewById(R.id.idRVWeather);
        cityEdt = findViewById(R.id.idEditCity);
        backIV = findViewById(R.id.idIVBack);
        iconIV = findViewById(R.id.idIVIcon);
        searchIV = findViewById(R.id.idTVSearch);
        weatherReportTV = findViewById(R.id.idTVweatherReport);
        weatherForecastTV = findViewById(R.id.idTVweatherForecast);
        logOutIV = findViewById(R.id.logOut);
        separation = findViewById(R.id.separation);
        localTimeTV = findViewById(R.id.idTVLocalTime);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);


        YoYo.with(Techniques.Pulse).duration(2000).repeat(0).playOn(iconIV);
        YoYo.with(Techniques.Pulse).duration(2000).repeat(5).playOn(tempSwitch);
        YoYo.with(Techniques.Pulse).duration(2000).repeat(0).playOn(temperatureTV);
        YoYo.with(Techniques.Landing).duration(2000).repeat(0).playOn(cityNameTV);
        YoYo.with(Techniques.Landing).duration(2000).repeat(0).playOn(regionCountryTV);
        YoYo.with(Techniques.Landing).duration(2000).repeat(0).playOn(conditionTV);
        YoYo.with(Techniques.Landing).duration(2000).repeat(0).playOn(feelsLikeTV);
        YoYo.with(Techniques.Landing).duration(2000).repeat(0).playOn(windSpeedTV);
        YoYo.with(Techniques.Landing).duration(2000).repeat(0).playOn(humidityTV);
        YoYo.with(Techniques.Landing).duration(2000).repeat(0).playOn(weatherReportTV);
        YoYo.with(Techniques.FlipInX).duration(2000).repeat(0).playOn(logOutIV);
        YoYo.with(Techniques.Landing).duration(2000).repeat(0).playOn(weatherForecastTV);
        YoYo.with(Techniques.Landing).duration(2000).repeat(0).playOn(localTimeTV);
        YoYo.with(Techniques.Pulse).duration(2000).repeat(5).playOn(separation);
        YoYo.with(Techniques.RollIn).duration(2000).repeat(0).playOn(searchIV);


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getWeatherInfo(cityName);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

                handler = new Handler(Looper.getMainLooper());
                refreshRunnable = new Runnable() {
                    @Override
                    public void run() {
                        getWeatherInfo(cityName);
                        handler.postDelayed(this, 60000*10); // 60000 milliseconds = 1 minute
                    }
                };
                handler.post(refreshRunnable);



        cityEdt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String city = Objects.requireNonNull(cityEdt.getText()).toString();
                    cityEdt.setText("");
                    if (city.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Please Enter City Name!", Toast.LENGTH_SHORT).show();
                    } else {
                        //cityNameTV.setText(city);
                        cityName = city; // Update the cityName variable
                        getWeatherInfo(city);
                    }
                    return true;
                }
                return false;
            }
        });


        tempSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isFahrenheit = isChecked;
            getWeatherInfo(cityName);
            weatherRVAdapter.toggleUnits(isChecked);
        });

        weatherRVModelArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this, weatherRVModelArrayList);
        weatherRV.setAdapter(weatherRVAdapter);

        weatherReportTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                String currentTime = new SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(new Date());
                String currentTemperature = temperatureTV.getText().toString();
                String feelsLikeTemperature = feelsLikeTV.getText().toString().substring(11);
                String currentWindSpeed = windSpeedTV.getText().toString().substring(12);
                String currentHumidity = humidityTV.getText().toString().substring(9);
                String cityLocalTime = localTimeTV.getText().toString().substring(3);
                shareWeatherReport(currentTime, currentTemperature, feelsLikeTemperature, currentWindSpeed, currentHumidity, cityLocalTime);
            }
        });

        logOutIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
                new AlertDialog.Builder(MainActivity.this)
                        .setIcon(R.drawable.baseline_logout_24)
                        .setTitle("Log Out!")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseAuth.getInstance().signOut();
                                startActivity(new Intent(MainActivity.this, SignInActivity.class));
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            // Location permissions not granted, request permissions
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
        } else {
            // Location permissions granted, get the user's location
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                cityName = getCityName(location.getLongitude(),location.getLatitude());
                getWeatherInfo(cityName);
            } else {
                // Couldn't get location, show message or provide a default location
                Toast.makeText(this, "Sorry! Could not get your location.", Toast.LENGTH_LONG).show();
                getWeatherInfo("New Delhi");
            }
        }

        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = Objects.requireNonNull(cityEdt.getText()).toString();
                cityEdt.setText("");
                if(city.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please Enter City Name!", Toast.LENGTH_SHORT).show();
                } else {
                   // cityNameTV.setText(city);
                    cityName = city; // Update the cityName variable
                    getWeatherInfo(city);
                }
            }
        });

    }

    private void shareWeatherReport(String currentTime, String currentTemperature, String feelsLikeTemperature, String currentWindSpeed, String currentHumidity, String cityLocalTime) {
        StringBuilder weatherReport = new StringBuilder();
        String currentDate = getCurrentDateString();

        weatherReport.append("Weather Report for ");
        weatherReport.append(cityNameTV.getText()).append(", ").append(regionCountryTV.getText());
        weatherReport.append(" on ");
        weatherReport.append(currentDate);
        weatherReport.append("\n\n");

        weatherReport.append("Instance Time: ").append(currentTime).append(" IST\n");
        weatherReport.append("City Local Instance Time: ").append(cityLocalTime).append("\n");
        weatherReport.append("Current Temperature: ").append(currentTemperature).append("\n");
        weatherReport.append("Feels Like: ").append(feelsLikeTemperature).append("\n");
        weatherReport.append("Wind Speed: ").append(currentWindSpeed).append("\n");
        weatherReport.append("Humidity: ").append(currentHumidity).append("\n\n\n");

        weatherReport.append("Hourly Weather Forecast (City Local Time):\n\n");

        for (WeatherRVModel model : weatherRVModelArrayList) {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            try {
                Date t = input.parse(model.getTime());
                weatherReport.append("Time: ").append(output.format(t)).append("\n");
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String temperature = model.getTemperature();
            if (isFahrenheit) {
                double tempF = Double.parseDouble(temperature);
                temperature = String.format("%.1f°F", tempF);
            } else {
                temperature = temperature + "°C";
            }
            weatherReport.append("Temperature: ").append(temperature).append("\n");

            String windSpeed = model.getWindSpeed();
            if (isFahrenheit) {
                double windMph = Double.parseDouble(windSpeed);
                windSpeed = String.format("%.1f MPH", windMph);
            } else {
                windSpeed = windSpeed + " Km/h";
            }
            weatherReport.append("Wind Speed: ").append(windSpeed).append("\n");

            weatherReport.append("\n");
        }

        getCurrentUserName().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    String userName = task.getResult();
                    if (userName != null) {
                        weatherReport.append("Report Generated by:\n").append(userName).append("\n");
                    }
                }

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Weather Report");
                shareIntent.putExtra(Intent.EXTRA_TEXT, weatherReport.toString());
                startActivity(Intent.createChooser(shareIntent, "Share Weather Report"));
            }
        });
    }


    private Task<String> getCurrentUserName() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference reference = database.collection("Users").document(firebaseUser.getUid());

        return reference.get().continueWith(new Continuation<DocumentSnapshot, String>() {
            @Override
            public String then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        return documentSnapshot.getString("name");
                    }
                }
                return null;
            }
        });
    }





    private String getCurrentDateString() {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        Calendar calendar = Calendar.getInstance();
        return dateFormat.format(calendar.getTime());
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PERMISSION_CODE) {
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                getWeatherInfo(cityName);
            } else {
                Toast.makeText(this, "Please provide the permission", Toast.LENGTH_SHORT).show();
                getWeatherInfo("New Delhi");
            }
        }
    }

    private String getCityName(double longitude, double latitude) {
        String cityName = "Not Found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude,longitude,10);
            for(Address adr : addresses) {
                if(adr != null) {
                    String city = adr.getLocality();
                    if(city!=null && !city.equals("")) {
                        cityName = city;
                    } else {
                        Log.d("TAG","CITY NOT FOUND!");
                        Toast.makeText(this, "City is not found", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityName;
    }


    private void getWeatherInfo(String cityName) {
        String url = "http://api.weatherapi.com/v1/forecast.json?key=f9823e702b4e405fbaa52927232803&q=" + cityName + "&days=1&aqi=yes&alerts=yes";
        //cityNameTV.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModelArrayList.clear();

                try {
                    String temperature = response.getJSONObject("current").getString("temp_c");
                    double temperC = Double.parseDouble(temperature);
                    double temperF = (temperC * 9 / 5) + 32;
                    String temperatureText = isFahrenheit ? String.format("%.1f °F", temperF) : String.format("%.1f °C", temperC);
                    temperatureTV.setText(temperatureText);

                    String feelsLikeTemp = response.getJSONObject("current").getString("feelslike_c");
                    double tempFeelsLikeC = Double.parseDouble(feelsLikeTemp);
                    double tempFeelsLikeF = (tempFeelsLikeC * 9 / 5) + 32;
                    String temperatureTextFeelsLike = isFahrenheit ? String.format("%.1f °F", tempFeelsLikeF) : String.format("%.1f °C", tempFeelsLikeC);
                    feelsLikeTV.setText("Feels Like: " + temperatureTextFeelsLike);

                    String WindSpeed = response.getJSONObject("current").getString("wind_kph");
                    double windSpeedKMh = Double.parseDouble(WindSpeed);
                    double windSpeedMPH = windSpeedKMh / 1.609;
                    String WindSpeedText = isFahrenheit ? String.format("%.1f MPH", windSpeedMPH) : String.format("%.1f Km/h", windSpeedKMh);
                    windSpeedTV.setText("Wind Speed: " + WindSpeedText);

                    String humidity = response.getJSONObject("current").getString("humidity");
                    humidityTV.setText("Humidity: " + humidity + "%");

                    String city = response.getJSONObject("location").getString("name");
                    cityNameTV.setText(city);

                    String region = response.getJSONObject("location").getString("region").concat(", ");
                   // regionTV.setText(region);

                    String country = response.getJSONObject("location").getString("country");
                    regionCountryTV.setText(region + country);

                    String localTime = response.getJSONObject("location").getString("localtime");
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a");
                    try {
                        Date parsedLocalTime = inputFormat.parse(localTime);
                        String formattedLocalTime = outputFormat.format(parsedLocalTime);
                        localTimeTV.setText("LT " + formattedLocalTime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    conditionTV.setText(condition);

                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);

                    int isDay = response.getJSONObject("current").getInt("is_day");
                    if (isDay == 1) {
                        //Daytime picture
                        Picasso.get().load("https://unsplash.com/photos/NAVi0Eyia8w/download?ixid=MnwxMjA3fDB8MXxzZWFyY2h8MTZ8fHNreSUyMGJhY2tncm91bmR8ZW58MHx8fHwxNjgwMzE4MjE5&force=true").into(backIV);
                    } else {
                        //Night picture
                        Picasso.get().load("https://unsplash.com/photos/G2jAOMGGlPE/download?ixid=MnwxMjA3fDB8MXxzZWFyY2h8MTJ8fHNreSUyMGJhY2tncm91bmR8ZW58MHx8fHwxNjgwMzE4MjE5&force=true").into(backIV);
                    }

                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forcast0 = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = forcast0.getJSONArray("hour");

                    for (int i = 0; i < hourArray.length(); i++) {
                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String time = hourObj.getString("time");

                        String tempC = hourObj.getString("temp_c");
                        double tempC_double = Double.parseDouble(tempC);
                        temperature = tempC;
                        if (isFahrenheit) {
                            double tempF = (tempC_double * 9 / 5) + 32;
                            temperature = String.format("%.1f", tempF);
                        }

                        String windKmph = hourObj.getString("wind_kph");
                        double windKmph_double = Double.parseDouble(windKmph);
                        String windSpeed = windKmph;
                        if (isFahrenheit) {
                            double windMph = windKmph_double/ 1.609;
                            windSpeed = String.format("%.1f", windMph);
                        }

                        String img = hourObj.getJSONObject("condition").getString("icon");
                        weatherRVModelArrayList.add(new WeatherRVModel(time, temperature, img, windSpeed));
                    }
                    weatherRVAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Please Enter valid city name!", Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(jsonObjectRequest);
    }



    private final AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.5F);

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.baseline_exit_to_app_24)
                .setTitle("Exit App")
                .setMessage("Do you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
                        finish();
                    }
                })
                .setNeutralButton("Rate App", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String YourPageURL = "https://play.google.com/store/apps/details?id=com.mobilab.weather";
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(YourPageURL));
                        startActivity(browserIntent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable);
        }
    }
}