package com.example.source11_api30;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class PeopleAdapter extends BaseDiffAdapter<People> {




    @Override
    protected boolean areItemsTheSame(People oldItem, People newItem) {
        boolean result = oldItem.getId() == newItem.getId();
        L.i("areItemsTheSame " + "-oldID:" + oldItem.getId() + "-newID:" + newItem.getId() + ",结果:" + result);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected boolean areContentsTheSame(People oldItem, People newItem) {
        boolean result = oldItem.getAge() == newItem.getAge() &&
                Objects.equals(oldItem.getName(), newItem.getName());
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        People people = getItem(position);
        MyHolder myHolder = (MyHolder) holder;
        myHolder.tv.setText("姓名:" + people.getName() + ",年龄:" + people.getAge() + ",id:" + people.getId());
    }

    private static class MyHolder extends RecyclerView.ViewHolder {

        TextView tv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv);
        }
    }
}
