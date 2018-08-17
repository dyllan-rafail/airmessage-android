package me.tagavari.airmessage.composite;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("Registered")
public class AppCompatCompositeActivity extends AppCompatActivity {
	private final List<AppCompatActivityPlugin> pluginList = new ArrayList<>();
	
	public void addPlugin(AppCompatActivityPlugin activityPlugin) {
		pluginList.add(activityPlugin);
		activityPlugin.setActivity(this);
	}
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		for(AppCompatActivityPlugin plugin : pluginList) plugin.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		for(AppCompatActivityPlugin plugin : pluginList) plugin.onStart();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		for(AppCompatActivityPlugin plugin : pluginList) plugin.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		for(AppCompatActivityPlugin plugin : pluginList) plugin.onPause();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		for(AppCompatActivityPlugin plugin : pluginList) plugin.onStart();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		for(AppCompatActivityPlugin plugin : pluginList) plugin.onDestroy();
	}
}