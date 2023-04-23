package com.mobilab.weather;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class WeatherRVAdapter extends RecyclerView.Adapter<WeatherRVAdapter.ViewHolder> {
    private Context context;
    private ArrayList<WeatherRVModel> weatherRVModelArrayList;

    private boolean isFahrenheit = false;

    public void toggleUnits(boolean isFahrenheit) {
        this.isFahrenheit = isFahrenheit;
        notifyDataSetChanged();
    }


    public WeatherRVAdapter(Context context, ArrayList<WeatherRVModel> weatherRVModelArrayList) {
        this.context = context;
        this.weatherRVModelArrayList = weatherRVModelArrayList;
    }

    @NonNull
    @Override
    public WeatherRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.weather_rv_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherRVAdapter.ViewHolder holder, int position) {
        WeatherRVModel model = weatherRVModelArrayList.get(position);

        double tempC = Double.parseDouble(model.getTemperature());
        double tempF = Double.parseDouble(model.getTemperature());
        String temperature = isFahrenheit ? String.format("%.1f°F", tempF) : String.format("%.1f°C", tempC);

        holder.temperatureTV.setText(temperature);

        double windKmph = Double.parseDouble(model.getWindSpeed());
        double windMph = Double.parseDouble(model.getWindSpeed());
        String windSpeed = isFahrenheit ? String.format("%.1f MPH", windMph) : String.format("%.1f Km/h", windKmph);

        holder.windTV.setText(windSpeed);

        // Setting the condition icon using Picasso
        Picasso.get().load("http:".concat(model.getIcon())).into(holder.conditionIV);

        // Formatting the time string
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        SimpleDateFormat output = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        try {
            Date t = input.parse(model.getTime());
            holder.timeTV.setText(output.format(t));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        AnimatorSet flipAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.flip_animator);
        flipAnimation.setTarget(holder.itemView);
        flipAnimation.start();
        // YoYo.with(Techniques.FlipInY).duration(2000).repeat(0).playOn(holder.itemView);
    }


    @Override
    public int getItemCount() {
        return weatherRVModelArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView windTV, temperatureTV, timeTV;
        private ImageView conditionIV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            windTV = itemView.findViewById(R.id.idTVWindSpeed);
            temperatureTV = itemView.findViewById(R.id.idTVTemperature);
            timeTV = itemView.findViewById(R.id.idTVTime);
            conditionIV = itemView.findViewById(R.id.idTVCondition);
        }
    }
}
