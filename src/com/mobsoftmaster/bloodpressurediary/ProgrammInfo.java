package com.mobsoftmaster.bloodpressurediary;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ProgrammInfo extends TrackedActivity {

	String TAG = "myLogs";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.programm_info);

		ImageView imageViewReview = (ImageView) findViewById(R.id.imageViewReview);
		imageViewReview.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.mobsoftmaster.bloodpressurediary");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});
	}
}