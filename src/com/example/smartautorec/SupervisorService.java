package com.example.smartautorec;

import java.io.File;

import com.example.smartautorec.R;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;

//������� ����� ������� ��������� �������������� �������
public class SupervisorService extends Service {
	// ��� ��� ����� ��� �������� �������
	private Intent RecordServiceIntent;
	private Intent AccelServiceIntent;
	private Intent LocationServiceIntent;

	private IntentFilter intentFlt;
	private BroadcastReceiver br;
	public final static String BROADCAST_ACCEL_ACTION = "AccelServiceRecieve";
	public final static String BROADCAST_RECORD_ACTION = "RecordServiceRecieve";

	public static final String VAL1 = "VAL1";
	public static final String VAL2 = "VAL2";
	public static final String VAL3 = "VAL3";
	// ���� ����� ��� ����������
	final int UPDATE_TASK = 1;
	final long UPDATE_TASK_DELAY = 30000; // �������� ��� �������� ���������� ��
	final int RESTART_SERVICE_TASK = 2;
	final long RESTART_SERVICE_TASK_DELAY = 20000;
	final int SHOW_BLACK_ACTIVITY = 3;
	final long SHOW_BLACK_ACTIVITY_DELAY = 60000; // �������� ������ �������
													// ������
	final int DELAY_1 = 4;
	final int DELAY_2 = 5;
	//
	WakeLock wl;
	//
	int fRecordStatus = 0;

	public static Boolean fBlackActivityOnTop = false;
	Handler h;
	Handler.Callback hc = new Handler.Callback() {
		public boolean handleMessage(Message msg) {
			// Log.d(LOG_TAG, "what = " + msg.what);
			if (h == null) {
				return false;
			}
			switch (msg.what) {
			case UPDATE_TASK: // ���������� �������� ��� �� ����������
				//ServiceClass.WriteLog(1, "Update Task");
				CheckUpdate();
				h.sendEmptyMessageDelayed(UPDATE_TASK, UPDATE_TASK_DELAY);
				break;
			case RESTART_SERVICE_TASK: // ���������� ������������ ��������
				ServiceClass.WriteLog(1, "Restart Service Task");

				h.sendEmptyMessageDelayed(RESTART_SERVICE_TASK,
						RESTART_SERVICE_TASK_DELAY);
				break;
			case SHOW_BLACK_ACTIVITY: // ����� ������� ����
				if (!fBlackActivityOnTop) {
					fBlackActivityOnTop = true;
					ServiceClass.WriteLog(1, "Show Black Activity");
					ShowBlackActivity();
				}
				h.sendEmptyMessageDelayed(SHOW_BLACK_ACTIVITY,
						SHOW_BLACK_ACTIVITY_DELAY);
				break;
			case DELAY_1: // �������� 1
				fRecordStatus = 1;				
				break;

			case DELAY_2: // �������� 2
				// ���� �� ������� ������ �� ���������
				// ��� ���� ������� ��������
				if (fRecordStatus == 1)	{
					// ������������� ������
					fRecordStatus = 0;
					AccelService.StopAccelRecord();
					RecordServiceStop();
					ServiceClass.WriteLog(2, "Stop Record");
				}
				// ���� �� ������� ������ ��������� ���
				// ���� ������� ��������
				if (fRecordStatus == 3) 				
				{	// ���������� ������
					fRecordStatus = 1;
					h.sendEmptyMessageDelayed(DELAY_2, ServiceClass.MAX_TIME_RECORD_DURATION);
					ServiceClass.WriteLog(2, "Go Record");
				}								
				break;

			}
			return false;
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// ������ ����������� ������
		ForForeground();
		RecordServiceIntent = new Intent(this, RecordService.class);
		AccelServiceIntent = new Intent(this, AccelService.class);
		LocationServiceIntent = new Intent(this, LocationService.class);
		ServiceClass.WriteLog(2, "Start SupervisorService v"
				+ ServiceClass.Version_number);
		LocationServiceStart();
		AccelServiceStart();
		//RecordServiceStart();
		// ������� ������� ���������� ������� �� ����������
		CreateDelayTasks();
		return super.onStartCommand(intent, flags, startId);
	}

	public void CreateDelayTasks() {
		h = new Handler(hc);
		h.sendEmptyMessageDelayed(UPDATE_TASK, UPDATE_TASK_DELAY);
		h.sendEmptyMessageDelayed(SHOW_BLACK_ACTIVITY,
				SHOW_BLACK_ACTIVITY_DELAY);
		// h.sendEmptyMessageDelayed(RESTART_SERVICE_TASK,
		// RESTART_SERVICE_TASK_DELAY);
	}

	// ������ ����������� ������
	public void ForForeground() {
		// ������������� ���������� ������
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionClass());
		// ������ ���� ���� �� �������
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, // |
				// PowerManager.ACQUIRE_CAUSES_WAKEUP,
				// PowerManager.PARTIAL_WAKE_LOCK,
				"MyScreenLock");
		wl.acquire();
		// ���������� �����������
		Notification notif = new Notification(R.drawable.ic_launcher,
				"SaR runing", System.currentTimeMillis());
		// �������
		Intent it = new Intent(this, MainActivity.class);
		it.setAction(Intent.ACTION_MAIN);
		it.addCategory(Intent.CATEGORY_LAUNCHER);

		// it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, it, 0);
		//
		notif.setLatestEventInfo(this, "SaR", "SaR runing", pIntent);

		startForeground(1337, notif);
		/*
		 * int i = 1; int b = 2; if (true) b = 0; i = i/b;
		 */
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	// ������ ������� ������ ���������
	public void LocationServiceStart() {
		// ����� ������ � 1 ���� �� N ����������
		int l = ServiceClass.MAX_TIME_RECORD_DURATION;
		LocationServiceIntent.putExtra(LocationService.GPS_LOG_MAX_DURATION, l);
		// ��������� ������
		startService(LocationServiceIntent);
	}

	public void LocationServiceStop() {
		stopService(LocationServiceIntent);
		// rec = false;

	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	// ������ ������� ������ �����������
	public void RecordServiceStart() {
		// ��� ����������� ������������ ��������
		RecordServiceIntent.putExtra(RecordService.SERVICE_TYPE,
				RecordService.AUDIO_RECORD_SERVICE_TYPE);
		// �������� ��������
		String s = "";
		RecordServiceIntent.putExtra(RecordService.DATA_CONT, s);
		// ����� ������ � 1 ���� �����������- �� N ����������
		int l = ServiceClass.MAX_TIME_RECORD_DURATION;
		RecordServiceIntent.putExtra(RecordService.RECORD_FILE_MAX_DURATION, l);
		// RecordServiceIntent.putExtra(ACCEL_MAX_VALUE, 10);
		// ��������� ������
		startService(RecordServiceIntent);
		// rec = true;
		// �������� �����
		/*
		 * intentFlt = new IntentFilter(BROADCAST_RECORD_ACTION); br = new
		 * BroadcastReceiver() {
		 * 
		 * @Override public void onReceive(Context context, Intent intent) {
		 * AccelServiceStop(); AccelServiceStart(); } }; registerReceiver(br,
		 * intentFlt);
		 */
	}

	public void RecordServiceStop() {
		stopService(RecordServiceIntent);
		// rec = false;

	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////
	// ������ ������� ������ ����� ���������
	public void AccelServiceStart() {
		// �������� ��������
		String s = "";
		AccelServiceIntent.putExtra(AccelService.DATA_CONT, s);
		// ����� ������ � 1 ���� ���� - �� N ����������
		int l = ServiceClass.MAX_TIME_RECORD_DURATION;
		AccelServiceIntent.putExtra(AccelService.ACCEL_FILE_MAX_DURATION, l);
		//
		AccelServiceIntent.putExtra(AccelService.ACCEL_MAX_VALUE, 0.3);
		// ��������� ������
		startService(AccelServiceIntent);
		// rec = true;
		intentFlt = new IntentFilter(BROADCAST_ACCEL_ACTION);

		br = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				switch (fRecordStatus) {
				case 0: // ������ ��������, ������ ������
					fRecordStatus = 2;
					h.sendEmptyMessageDelayed(DELAY_1, ServiceClass.ACCEL_ATTENUATION_DURATION);
					h.sendEmptyMessageDelayed(DELAY_2, ServiceClass.MAX_TIME_RECORD_DURATION-1000);
					ServiceClass.WriteLog(3, "Start Record");
					AccelService.StartAccelRecord();
					RecordServiceStart();					
					break;
				case 1:// �� ����� ������ ������ ��������, ������ �������� ������ ��� �� 1 �������� �������
					ServiceClass.WriteLog(3, "��������");
					fRecordStatus = 3;
					break;					
				}

				// RecordServiceStop();
				// RecordServiceStart();
				
				// float val1 = intent.getFloatExtra(VAL1, 0); 
				// float val2 = intent.getFloatExtra(VAL2, 0); 
				 //float val3 = intent.getFloatExtra(VAL3, 0);
				 //ServiceClass.WriteLog(3,"X: " + String.valueOf(val1)+ " Y: " + String.valueOf(val2) + "Z: " + String.valueOf(val3));
				 /* 
				 * text7.setText("X: " + String.valueOf(val1));
				 * text8.setText("Y: " + String.valueOf(val2));
				 * text9.setText("Z: " + String.valueOf(val3));
				 */

			}

		};
		registerReceiver(br, intentFlt);
	}

	public void AccelServiceStop() {
		stopService(AccelServiceIntent);
		// rec = false;

	}

	// ///////////////////////////////////////////////////////////////////////

	private void CheckUpdate() {
		String filename1 = ServiceClass.Update_delete_file;
		File file1 = new File(filename1);
		// ��������� ��������� �� ���������� � ���� �� ���� �� ��������
		if (file1.exists()) {
			file1.delete();
			ServiceClass.WriteLog(2, "Update file deleted");
		}
		String filename2 = ServiceClass.Update_file;
		File file2 = new File(filename2);
		if (file2.exists()) {
			ServiceClass.WriteLog(2, "Update file founded");
			// ���� ���� ���������� ��������������� ��� �� �������� �
			// �����������
			file2.renameTo(file1);
			try {
				final String command = "pm install -r "
						+ file1.getAbsolutePath()
						+ "; am broadcast -a com.example.smartautorec_2_0.StartSupervisor";
				Process proc = Runtime.getRuntime().exec(
						new String[] { "su", "-c", command });
				proc.waitFor();
			} catch (Exception t1) {
				// e.printStackTrace();
				ServiceClass.WriteLog(-1, "UpdateException: " + t1.toString());
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onDestroy() {
		RecordServiceStop();
		AccelServiceStop();
		// LocationServiceStop();
		h = null;
		ServiceClass.WriteLog(2, "Stop SupervisorService");
	}

	public void ShowBlackActivity() {
		Intent intent = new Intent(this, BlackActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
