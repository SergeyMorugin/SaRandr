package com.example.smartautorec;

import com.example.smartautorec.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

public class BlackActivity extends Activity {
	 
	protected void onCreate(Bundle savedInstanceState) {
		    super.onCreate(savedInstanceState);
		    setContentView(R.layout.black_activity);
		    getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		    getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		  }
	@Override
	protected void onPause() {
		super.onPause();
		SupervisorService.fBlackActivityOnTop = false;

	}
}
