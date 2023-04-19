package com.mobilab.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
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
    private TextView cityNameTV, temperatureTV, conditionTV, weatherReportTV;
    private TextInputEditText cityEdt;
    private ImageView backIV, iconIV, searchIV, logOutIV;
    private RecyclerView weatherRV;
    private ArrayList<WeatherRVModel> weatherRVModelArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String cityName;

    private boolean isFahrenheit = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);
        //Objects.requireNonNull(getSupportActionBar()).hide();

        homeRL = findViewById(R.id.idRLHome);
        loadingPB = findViewById(R.id.idPBLoading);
        cityNameTV = findViewById(R.id.idTVCityName);
        temperatureTV = findViewById(R.id.idTVTemperature);
        conditionTV = findViewById(R.id.idTVCondition);
        weatherRV = findViewById(R.id.idRVWeather);
        cityEdt = findViewById(R.id.idEditCity);
        backIV = findViewById(R.id.idIVBack);
        iconIV = findViewById(R.id.idIVIcon);
        searchIV = findViewById(R.id.idTVSearch);
        weatherReportTV = findViewById(R.id.idTVweatherReport);
        logOutIV = findViewById(R.id.logOut);

        Switch tempSwitch = findViewById(R.id.tempSwitch);
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
                shareWeatherReport();
            }
        });

        logOutIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, SignInActivity.class));
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
                Toast.makeText(this, "Could not get your location, please provide a location or try again later", Toast.LENGTH_LONG).show();
                getWeatherInfo("Delhi");
            }
        }

        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = cityEdt.getText().toString();
                if(city.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please Enter City Name!", Toast.LENGTH_SHORT).show();
                } else {
                    cityNameTV.setText(city);
                    getWeatherInfo(city);
                }
            }
        });

    }

    private void shareWeatherReport() {
        StringBuilder weatherReport = new StringBuilder();
        String currentDate = getCurrentDateString();

        weatherReport.append("Weather Report for ");
        weatherReport.append(cityNameTV.getText());
        weatherReport.append(" on ");
        weatherReport.append(currentDate);
        weatherReport.append("\n\n");

        for (WeatherRVModel model : weatherRVModelArrayList) {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            SimpleDateFormat output = new SimpleDateFormat("hh:mm aa");
            try {
                Date t = input.parse(model.getTime());
                weatherReport.append("Time: ").append(output.format(t)).append("\n");
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String temperature = model.getTemperature();
            if (isFahrenheit) {
                double tempC = Double.parseDouble(temperature);
                double tempF = (tempC * 9 / 5) + 32;
                temperature = String.format("%.1f°F", tempF);
            } else {
                temperature = temperature + "°C";
            }
            weatherReport.append("Temperature: ").append(temperature).append("\n");

            String windSpeed = model.getWindSpeed();
            if (isFahrenheit) {
                double windKmph = Double.parseDouble(windSpeed);
                double windMph = windKmph / 1.609;
                windSpeed = String.format("%.1f MPH", windMph);
            } else {
                windSpeed = windSpeed + " Km/h";
            }
            weatherReport.append("Wind Speed: ").append(windSpeed).append("\n");

            weatherReport.append("\n");
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Weather Report");
        shareIntent.putExtra(Intent.EXTRA_TEXT, weatherReport.toString());
        startActivity(Intent.createChooser(shareIntent, "Share Weather Report"));
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
            } else {
                Toast.makeText(this, "Please provide the permission", Toast.LENGTH_SHORT).show();
                finish();
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
        }
    }


    private void getWeatherInfo(String cityName) {
        String url = "http://api.weatherapi.com/v1/forecast.json?key=f9823e702b4e405fbaa52927232803&q=" + cityName + "&days=1&aqi=yes&alerts=yes";
        cityNameTV.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModelArrayList.clear();

                try {
                    String temperature = response.getJSONObject("current").getString("temp_c");
                    double tempCTV = Double.parseDouble(temperature);
                    double tempFTV = (tempCTV * 9 / 5) + 32;
                    String temperatureText = isFahrenheit ? String.format("%.1f°F", tempFTV) : String.format("%.1f°C", tempCTV);
                    temperatureTV.setText(temperatureText);
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);
                    conditionTV.setText(condition);
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
//                .setNeutralButton("", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();

    }

}