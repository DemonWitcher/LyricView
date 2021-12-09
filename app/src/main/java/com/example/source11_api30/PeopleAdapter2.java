package com.example.source11_api30;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class PeopleAdapter2 extends RecyclerView.Adapter<PeopleAdapter2.MyHolder> {

    private List<People> list;

    public PeopleAdapter2(List<People> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public PeopleAdapter2.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my, parent, false));
    }

    @Override
    public void onViewAttachedToWindow(@NonNull PeopleAdapter2.MyHolder holder) {
        super.onViewAttachedToWindow(holder);
        People people = (People) holder.tv.getTag();
        L.i("attachWindow " + people.getId());
    }

    @Override
    public void onBindViewHolder(@NonNull PeopleAdapter2.MyHolder holder, int position) {
        People people = list.get(position);
        L.i("bindView " + people.getId());
        holder.tv.setTag(people);
        holder.tv.setText("姓名:" + people.getName() + ",年龄:" + people.getAge() + ",id:" + people.getId());
    }

    @Override
    public int getItemCount() {
        return list==null?0:list.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {

        TextView tv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv);
        }
    }
}
