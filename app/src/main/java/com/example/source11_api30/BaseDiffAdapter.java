package com.example.source11_api30;


import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseDiffAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final DiffUtil.ItemCallback<T> mDiffCallback = new DiffUtil.ItemCallback<T>() {
        @Override
        public boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem) {
            return BaseDiffAdapter.this.areItemsTheSame(oldItem, newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull T oldItem, @NonNull T newItem) {
            return BaseDiffAdapter.this.areContentsTheSame(oldItem, newItem);
        }
    };

    private final AsyncListDiffer<T> mAsyncListDiffer = new AsyncListDiffer<>(this, mDiffCallback);

    @Override
    public int getItemCount() {
        return mAsyncListDiffer.getCurrentList().size();
    }

    public void submitData(List<T> list) {
        mAsyncListDiffer.submitList(list);
    }

    public T getItem(int position) {
        List<T> list = mAsyncListDiffer.getCurrentList();
        if (position < 0 || position >= list.size()) {
            return null;
        }
        return list.get(position);
    }

    protected abstract boolean areItemsTheSame(T oldItem, T newItem);

    protected abstract boolean areContentsTheSame(T oldItem, T newItem);

}
