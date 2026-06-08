package com.locationtracker.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private final List<LocationEntry> entries;
    private final Context context;
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault());

    public LocationAdapter(List<LocationEntry> entries, Context context) {
        this.entries = entries;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_location, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocationEntry entry = entries.get(position);

        holder.tvCoords.setText(String.format(Locale.getDefault(),
                "%.6f,  %.6f", entry.getLatitude(), entry.getLongitude()));
        holder.tvTime.setText(sdf.format(new Date(entry.getTimestamp())));
        holder.tvAccuracy.setText(String.format(Locale.getDefault(), "±%.1f m", entry.getAccuracy()));
        holder.tvIndex.setText(String.valueOf(entries.size() - position));

        // Click to open in maps
        holder.itemView.setOnClickListener(v -> {
            String geo = "geo:" + entry.getLatitude() + "," + entry.getLongitude()
                    + "?q=" + entry.getLatitude() + "," + entry.getLongitude()
                    + "&z=15";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geo));
            intent.setPackage("com.google.android.apps.maps");
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                // fallback to any maps app
                intent.setPackage(null);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCoords, tvTime, tvAccuracy, tvIndex;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCoords = itemView.findViewById(R.id.tv_coords);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvAccuracy = itemView.findViewById(R.id.tv_accuracy);
            tvIndex = itemView.findViewById(R.id.tv_index);
        }
    }
}
