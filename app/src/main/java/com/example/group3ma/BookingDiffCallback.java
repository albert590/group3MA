package com.example.group3ma;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class BookingDiffCallback extends DiffUtil.Callback {

    private final List<Booking> oldList;
    private final List<Booking> newList;

    public BookingDiffCallback(List<Booking> oldList, List<Booking> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        String oldId = oldList.get(oldItemPosition).bookingId;
        String newId = newList.get(newItemPosition).bookingId;
        return oldId != null && oldId.equals(newId);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Booking oldBooking = oldList.get(oldItemPosition);
        Booking newBooking = newList.get(newItemPosition);
        if (oldBooking.status == null) return newBooking.status == null;
        return oldBooking.status.equals(newBooking.status);
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        // You can implement this to get more granular updates, but it's optional.
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}