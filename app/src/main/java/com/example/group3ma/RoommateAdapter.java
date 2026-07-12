package com.example.group3ma;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RoommateAdapter extends RecyclerView.Adapter<RoommateAdapter.ViewHolder> {
    private List<User> matches;
    private User currentUser;

    public RoommateAdapter(List<User> matches, User currentUser) {
        this.matches = matches;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_roommate_match, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = matches.get(position);
        
        holder.tvMatchName.setText(user.fullName);
        holder.tvMatchYear.setText(user.yearOfStudy + " Student");
        holder.tvMatchHabits.setText(user.studyHabits);
        holder.tvMatchSmoking.setText(user.smokingPreference);
        holder.tvMatchBudget.setText(user.budgetRange);

        // Calculate Match Percentage
        int score = 0;
        int totalCriteria = 3;
        
        if (user.budgetRange != null && user.budgetRange.equals(currentUser.budgetRange)) score++;
        if (user.studyHabits != null && user.studyHabits.equals(currentUser.studyHabits)) score++;
        if (user.smokingPreference != null && user.smokingPreference.equals(currentUser.smokingPreference)) score++;
        
        int percentage = (score * 100) / totalCriteria;
        holder.tvMatchPercentage.setText(percentage + "% Match");

        holder.btnContactMatch.setOnClickListener(v -> {
            try {
                String phone = user.phoneNumber;
                if (phone != null && !phone.isEmpty()) {
                    String url = "https://api.whatsapp.com/send?phone=" + (phone.startsWith("0") ? "254" + phone.substring(1) : phone);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    v.getContext().startActivity(i);
                } else {
                    Toast.makeText(v.getContext(), "Phone number not available", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(v.getContext(), "WhatsApp not installed or error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() { return matches.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMatchName, tvMatchPercentage, tvMatchYear, tvMatchHabits, tvMatchSmoking, tvMatchBudget;
        Button btnContactMatch;

        ViewHolder(View itemView) {
            super(itemView);
            tvMatchName = itemView.findViewById(R.id.tvMatchName);
            tvMatchPercentage = itemView.findViewById(R.id.tvMatchPercentage);
            tvMatchYear = itemView.findViewById(R.id.tvMatchYear);
            tvMatchHabits = itemView.findViewById(R.id.tvMatchHabits);
            tvMatchSmoking = itemView.findViewById(R.id.tvMatchSmoking);
            tvMatchBudget = itemView.findViewById(R.id.tvMatchBudget);
            btnContactMatch = itemView.findViewById(R.id.btnContactMatch);
        }
    }
}
