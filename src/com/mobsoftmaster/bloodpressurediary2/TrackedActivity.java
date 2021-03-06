package com.mobsoftmaster.bloodpressurediary2;

import com.google.android.gms.analytics.GoogleAnalytics;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class TrackedActivity extends FragmentActivity {

	protected void onCreate(Bundle savedInstanceState, String tag) {
		super.onCreate(savedInstanceState);
		// Get a Tracker (should auto-report)
		((Locales) getApplication())
				.getTracker(Locales.TrackerName.APP_TRACKER);

	}

	@Override
	protected void onStart() {
		super.onStart();
		// Get an Analytics tracker to report app starts & uncaught exceptions
		// etc.
		GoogleAnalytics.getInstance(this).reportActivityStart(this);

	}

	@Override
	protected void onStop() {

		// Stop the analytics tracking
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
		super.onStop();
	}
}