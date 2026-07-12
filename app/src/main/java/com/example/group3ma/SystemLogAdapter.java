package com.example.group3ma;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SystemLogAdapter extends RecyclerView.Adapter<SystemLogAdapter.LogViewHolder> {

    private List<SystemLog> logList;

    public SystemLogAdapter(List<SystemLog> logList) {
        this.logList = logList;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_system_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        SystemLog log = logList.get(position);
        holder.tvLogUser.setText("User: " + log.email);
        holder.tvLogType.setText(log.userType);
        holder.tvLogAction.setText("Action: " + log.action);
        holder.tvLogTime.setText("Time: " + log.timestamp);
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView tvLogUser, tvLogType, tvLogAction, tvLogTime;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLogUser = itemView.findViewById(R.id.tvLogUser);
            tvLogType = itemView.findViewById(R.id.tvLogType);
            tvLogAction = itemView.findViewById(R.id.tvLogAction);
            tvLogTime = itemView.findViewById(R.id.tvLogTime);
        }
    }
}
