package com.example.group3ma;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MarketplaceAdapter extends RecyclerView.Adapter<MarketplaceAdapter.ViewHolder> {
    private List<MarketplaceItem> itemList;

    public MarketplaceAdapter(List<MarketplaceItem> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_marketplace, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MarketplaceItem item = itemList.get(position);
        holder.tvName.setText(item.itemName);
        holder.tvPrice.setText("KSh " + item.price);
        holder.tvCategory.setText(item.category);

        Glide.with(holder.itemView.getContext())
                .load(item.imageUrl)
                .placeholder(R.drawable.zzz)
                .into(holder.ivItemImage);

        holder.itemView.setOnClickListener(v -> showItemDetails(v, item));
    }

    private void showItemDetails(View v, MarketplaceItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_marketplace_detail, null);
        
        TextView tvName = dialogView.findViewById(R.id.tvDetailName);
        TextView tvPrice = dialogView.findViewById(R.id.tvDetailPrice);
        TextView tvDesc = dialogView.findViewById(R.id.tvDetailDescription);
        Button btnContact = dialogView.findViewById(R.id.btnContactSeller);
        ImageView ivItem = dialogView.findViewById(R.id.ivDetailImage);

        tvName.setText(item.itemName);
        tvPrice.setText("Price: KSh " + item.price);
        tvDesc.setText(item.description);
        
        Glide.with(v.getContext())
                .load(item.imageUrl)
                .placeholder(R.drawable.zzz)
                .into(ivItem);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        btnContact.setOnClickListener(view -> {
            try {
                String contact = item.contactInfo;
                if (contact != null && !contact.isEmpty()) {
                    String phone = contact.replaceAll("[^0-9]", "");
                    String url = "https://api.whatsapp.com/send?phone=" + (phone.startsWith("0") ? "254" + phone.substring(1) : phone);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    v.getContext().startActivity(i);
                } else {
                    Toast.makeText(v.getContext(), "Contact info missing", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(v.getContext(), "Error opening WhatsApp", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public int getItemCount() { return itemList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivItemImage;
        TextView tvName, tvPrice, tvCategory;
        ViewHolder(View itemView) {
            super(itemView);
            ivItemImage = itemView.findViewById(R.id.ivItemImage);
            tvName = itemView.findViewById(R.id.tvMarketItemName);
            tvPrice = itemView.findViewById(R.id.tvMarketItemPrice);
            tvCategory = itemView.findViewById(R.id.tvMarketItemCategory);
        }
    }
}
