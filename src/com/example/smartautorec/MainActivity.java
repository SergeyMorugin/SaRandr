package com.example.smartautorec;

import com.example.smartautorec.ServiceClass;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;




public class MainActivity extends ActionBarActivity implements OnClickListener {

	//

	// public static final String FILE_EXT = ".txt";
	// public static final String APP_DIR = "APP_DIR";// !!!!!!
	// public static final String FILE_NAME = "FILE_NAME";
	// public static final String PARAM1 = "PARAM1";
	// public static final String SERVICE_TYPE = "SERVICE_TYPE";
	// public static final String PARAM2 = "PARAM2";
	// public static final String PARAM3 = "PARAM3";
	// public static final String DATA_CONT = "DATA_CONT";

	// public static final String ACCEL_MAX_VALUE = "ACCEL_MAX_VALUE";
	// public static final String ACCEL_FILE_MAX_DURATION =
	// "ACCEL_LOG_MAX_DURATION";
	// public static final String RECORD_FILE_MAX_DURATION =
	// "RECORD_FILE_MAX_DURATION";

	// private String dir;
	// private int pos = 0;
	// private String mainBuff;
	
	private TextView text1;
	private TextView text7;
	private TextView text8;
	private TextView text9;
	//
	private TextView text4;
	private TextView text5;
	private EditText et1;
	//
	private Button button_rec_off;
	private Button button_rec_on;

	
	// private Boolean rec = false;
	
	//
	public static SurfaceView preview;
	//
	private Intent SupervisorServiceIntent;
	
	
	

	// public CameraService cs;

	@Override
	// Конструктор
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Это все было до меня
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

		// определяем указатели на тектовые поля
		text1 = (TextView) findViewById(R.id.textView1);
		text7 = (TextView) findViewById(R.id.textView7);		
		text8 = (TextView) findViewById(R.id.textView8);
		text9 = (TextView) findViewById(R.id.textView9);
			
		//
		//text4 = (TextView) findViewById(R.id.textView4);
		text5 = (TextView) findViewById(R.id.textView5);
		et1 = (EditText) findViewById(R.id.editText1);
		ServiceClass.LogText = (TextView) findViewById(R.id.textView6);
		
		//
		text1.setText("");
		text7.setText("");
		text8.setText("");
		text9.setText("");	
		ServiceClass.LogText.setText("");
		//
		button_rec_off = (Button) findViewById(R.id.button2);
		button_rec_on = (Button) findViewById(R.id.button1);
		//
		preview = (SurfaceView) findViewById(R.id.surfaceView1);

		//
		// text5.setText("");
		// mainBuff = new String();
		ServiceClass.WriteLog(2, "Start Activity v"+ServiceClass.Version_number);
		
		SupervisorServiceIntent = new Intent(this, SupervisorService.class);
		// .putExtra(APP_DIR, msc.Accel_directory);
		
	}

	@Override
	// Конструктор
	public void onStart() {
		super.onStart();

	}

	@Override
	// Деструктор
	public void onDestroy() {
		super.onDestroy();
		// camera.release();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// camera = Camera.open();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// cs.OnPauseFunc();

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.button1:
			button_rec_off.setEnabled(true);
			button_rec_on.setEnabled(false);
			startService(SupervisorServiceIntent);			
			//RecordServiceStart();
			//AccelServiceStart();
			break;
		case R.id.button2:
			 //RecordServiceStop();
			 //AccelServiceStop();
			 stopService(SupervisorServiceIntent);
			 button_rec_off.setEnabled(false);
			 button_rec_on.setEnabled(true);			
			break;
		case R.id.button3:
			//CheckUpdate(); 
			//UpdateProg();
			//ShowBlackActivity();
			//Intent intent = new Intent(this, BlackActivity.class);
		    //startActivity(intent);
			double  b = 65.5768;
			String s = LocationToStr(b);
			s = ServiceClass.StrToHex(s);
			text1.setText(s + "   ;");
			int i = 1;
			//int b = 2;
			if (true){
				i = 0;
			}
			//b = b/i;
			break;	
		case R.id.button4:			
			
			break;	
		}		
	}		
	
	private String LocationToStr(Double d){
		String s = "";
		int i = (int)(d*100000);
		//
		char ch = (char)(i & 0xFF);		
		s = ch + s;
		//
		ch = (char)((i >> 8) & 0xFF);
		s = ch + s;
		//
		ch = (char)((i >> 16) & 0xFF);
		s = ch + s;
		//
		ch = (char)((i >> 24) & 0xFF);
		s = ch + s;		
		return s;
	}

	
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}			
	

}
