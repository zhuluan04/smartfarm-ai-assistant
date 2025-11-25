package com.linjiu.recognize;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.linjiu.recognize.adapter.ProductAdapter;
import com.linjiu.recognize.domain.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        recyclerView = findViewById(R.id.recyclerView);

        List<Product> products = new ArrayList<>();
        products.add(new Product("iPhone 15", 7999.0, "最新苹果手机"));
        products.add(new Product("MacBook Pro", 15999.0, "专业笔记本电脑"));
        products.add(new Product("AirPods Pro", 1999.0, "主动降噪耳机"));
        products.add(new Product("iPad Air", 4399.0, "轻薄平板电脑"));
        products.add(new Product("Apple Watch", 2999.0, "智能手表"));

        adapter = new ProductAdapter(products);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}