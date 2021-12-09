package com.example.source11_api30;

import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    RecyclerView rv;
    private List<People> list = new ArrayList<>();
    private List<People> newList = new ArrayList<>();
    private PeopleAdapter2 adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rv = findViewById(R.id.rv);

        Random random = new Random();
        for (int i = 0; i < 200; i++) {
            int age = random.nextInt(100);
            list.add(new People("name" + i, i, age));
            newList.add(new People("name" + i, i, age));
        }
        for (int i = 30; i < 50; i++) {
            newList.add(new People("name" + i, i, random.nextInt(100)+100));
        }

        rv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        adapter = new PeopleAdapter2(list);
        rv.setItemViewCacheSize(3);
        rv.setAdapter(adapter);
        rv.setViewCacheExtension(new RecyclerView.ViewCacheExtension() {
            @Nullable
            @Override
            public View getViewForPositionAndType(@NonNull RecyclerView.Recycler recycler, int position, int type) {
                return null;
            }
        });
//        adapter.submitData(list);
        findViewById(R.id.bt1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                newList.get(2).setName("改过了");
//                adapter.submitData(newList);
                list.add(10,new People("新来的",1111,2222));
                adapter.notifyItemInserted(10);
            }
        });
        findViewById(R.id.bt2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int size = list.size();
                list.addAll(newList);
                adapter.notifyItemRangeInserted(size,newList.size());
//                newList.get(3).setName("后改的");
            }
        });
        findViewById(R.id.bt3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.remove(6);
                adapter.notifyItemRemoved(6);
//                newList.get(3).setName("后改的");
            }
        });
    }

}