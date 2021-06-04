package com.mcal.studio.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.mcal.studio.R;
import com.mcal.studio.adapter.AnalyzeAdapter;
import com.mcal.studio.fragment.analyze.AnalyzeFileFragment;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AnalyzeActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.analyze_tabs)
    TabLayout analyzeTabs;
    @BindView(R.id.analyze_pager)
    ViewPager analyzePager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze);
        ButterKnife.bind(this);
        toolbar.setTitle(new File(getIntent().getStringExtra("project_file")).getName());
        setSupportActionBar(toolbar);
        setupPager(analyzePager);
        analyzeTabs.setupWithViewPager(analyzePager);
    }

    private void setupPager(ViewPager analyzePager) {
        AnalyzeAdapter adapter = new AnalyzeAdapter(getSupportFragmentManager());
        adapter.addFragment(new AnalyzeFileFragment(), "FILES", getIntent().getExtras());
        analyzePager.setAdapter(adapter);
    }
}
