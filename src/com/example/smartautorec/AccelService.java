package com.example.smartautorec;

import java.io.BufferedInputStream;
import java.lang.Math;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Calendar;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

public class AccelService extends Service implements SensorEventListener {
	// Тип запуска сервиса акселерометра
	// - запись показаний в файл
	public static final byte ACCEL_RECORD_TYPE = 1;
	// - мониторинг
	public static final byte ACCEL_MONITORING_TYPE = 2;
	//
	public static final String ACCEL_MAX_VALUE = "ACCEL_MAX_VALUE";
	public static final String ACCEL_FILE_MAX_DURATION = "ACCEL_LOG_MAX_DURATION";
	public static final String SERVICE_TYPE = "SERVICE_TYPE";
	public static final String DATA_CONT = "DATA_CONT";
	//
	private SensorManager sm;
	private BufferedOutputStream outStream;
	private OutputStreamWriter sWriter;
	// private String appDirString;

	private Calendar cal1;
	private Long fStartTime;

	// private byte fAccelType = 1;
	// Время записи в один файл
	private String fAppDataCont;
	private int fLogMaxDuration = 60000; //
	private double fMaxAccelValue = 1;

	private static boolean fNewStart = true;
	private boolean fLogFileOpen = false;
	private static boolean fWriteAccelData = false;	
	//private boolean fNewMaxAccelData = false;
	private SensorEvent fSenserEvent = null;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		fNewStart = true;
		fWriteAccelData = false;
		fLogFileOpen = false;
		//fNewMaxAccelData = false;
		// appDirString = intent.getStringExtra(MainActivity.APP_DIR);
		// Вычитываем параметры
		fAppDataCont = intent.getStringExtra(DATA_CONT);
		fLogMaxDuration = intent.getIntExtra(ACCEL_FILE_MAX_DURATION, 60000);
		fMaxAccelValue = 0.3;//intent.getFloatExtra(ACCEL_MAX_VALUE, 1);
		// регистрируем обработчик акселерометра
		try {
			sm = (SensorManager) getSystemService(SENSOR_SERVICE);
			sm.registerListener(this,
					sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					sm.SENSOR_DELAY_FASTEST);
		} catch (Throwable t1) {
			ServiceClass.WriteLog(-1, "AccelException2: " + t1.toString());
		}
		ServiceClass.WriteLog(1, "Start AccelService");

		return super.onStartCommand(intent, flags, startId);
	}
	
	public static void StartAccelRecord(){
		fNewStart = true;
		fWriteAccelData = true;
		return;		
	}
	
	public static void StopAccelRecord(){
		fNewStart = true;
		fWriteAccelData = false;		
		return;		
	}

	@Override
	public void onDestroy() {
		sm.unregisterListener(this);
		CloseLogFile();
		ServiceClass.WriteLog(1, "Stop AccelService");
	}

	private boolean IsMaxAccelValue(SensorEvent event) {
		//
		if (fSenserEvent == null){
			fSenserEvent = event;
			return false;
		}
		//ServiceClass.WriteLog(3,"X: " + String.valueOf(Math.abs(event.values[0]-fSenserEvent.values[0]))+ " Y: " + String.valueOf(Math.abs(event.values[1]-fSenserEvent.values[1])) + "Z: " + String.valueOf(Math.abs(event.values[2]-fSenserEvent.values[2])));
		if ( (Math.abs(event.values[0]-fSenserEvent.values[0]) > fMaxAccelValue)
				|| (Math.abs(event.values[1]-fSenserEvent.values[1]) > fMaxAccelValue)
				|| (Math.abs(event.values[2]-fSenserEvent.values[2]) > fMaxAccelValue) ) {
			
			fSenserEvent = event;
			return true;
		}
		
		fSenserEvent = event;
		return false;
	}



	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			if (IsMaxAccelValue(event)) {// произошло превышение значений
											// акселерометра, значит кто то
											// около машины или в машине есть
				Intent intent = new Intent(
				SupervisorService.BROADCAST_ACCEL_ACTION);
				//intent.putExtra(SupervisorService.VAL1, event.values[0]);
				//intent.putExtra(SupervisorService.VAL2, event.values[1]);
				//intent.putExtra(SupervisorService.VAL3, event.values[2]);
				sendBroadcast(intent);
				// ServiceClass.WriteLog(1, String.valueOf(event.values[0]) +
				// "|"
				// + String.valueOf(event.values[1]) + "|"				
				// + String.valueOf(event.values[2]));
				//fNewMaxAccelData = true;
				//if (!fWriteAccelData) { // если это первое срабатывание
					//fNewStart = true;
					//fWriteAccelData = true;
				//}
			}
			if (fWriteAccelData) { // если разрешена запись
				WriteData(event.values);
			}else
				if (fLogFileOpen)
					CloseLogFile();
		}
	}

	public void WriteData(float values[]) {
		try {

			cal1 = Calendar.getInstance();
			if (fNewStart) { // проверяем не надо ли начать новый файл
				fNewStart = false;
				fStartTime = cal1.getTimeInMillis();
				CloseLogFile();				
				OpenLogFile();
			}			
			Long currentTime = cal1.getTimeInMillis() - fStartTime;			
			// проверяем не идет ли превышение записи по времени
			if (currentTime > fLogMaxDuration) {
				fNewStart = true;
			}
			// формат записи в файл
			String data = "<a>" + String.valueOf(currentTime) + "|"
					+ String.valueOf(values[0]) + "|"
					+ String.valueOf(values[1]) + "|"
					+ String.valueOf(values[2]) + "|" + "<r>" + "\r\n";

			// outStream.write(data., 1, 3);
			sWriter.write(data);
		} catch (Throwable t1) {
			ServiceClass.WriteLog(-1, "AccelException3: " + t1.toString());
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private boolean CloseLogFile() {
		if (!fLogFileOpen)
			return fLogFileOpen;
		try {			
			sWriter.flush();
			outStream.flush();
			// sWriter.close();
			outStream.close();

			fLogFileOpen = false;
			// sWriter = null;
		} catch (Exception t1) {
			// Toast.makeText(this, "(OnClose) " + e.toString(),
			// Toast.LENGTH_LONG)
			// .show();
			ServiceClass.WriteLog(-1, "AccelException4: " + t1.toString());
		}
		return !fLogFileOpen;
	}

	private boolean OpenLogFile() {
		if (fLogFileOpen)
			return !fLogFileOpen;
		try {

			String filename = fAppDataCont
					+ '_'
					+ ServiceClass
							.currentDateToString(ServiceClass.Date_format1)
					+ ".txt";
			File appDir = new File(
					ServiceClass.Accel_directory
							+ ServiceClass
									.currentDateToString(ServiceClass.Date_format2)
							+ "/");
			if (!appDir.exists())
				appDir.mkdirs();
			File file = new File(appDir, filename);
			outStream = new BufferedOutputStream(new FileOutputStream(file));
			sWriter = new OutputStreamWriter(outStream);
			// outStream.w
			// Выводим сообщение что запустилось
			// ServiceClass.WriteLog(1, "AccelLogFile: " + appDir + filename);
			//
			fLogFileOpen = true;

		} catch (Throwable t1) {
			ServiceClass.WriteLog(-1, "AccelException5: " + t1.toString());
		}
		return fLogFileOpen;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	// Кладет 8 байт в буфферную строку
	public static String longToString(long value) {
		String result = "";
		for (int i = 0; i < 8; i++) {
			result = result + (char) (value & 0xFF);
			value >>>= 8;
		}
		return result;
	}

	// Кладет значение ускорения в буферную строку в сокращенном виде
	public static String AccelToString(float value) {
		String result = "";
		short val = (short) (value * 1000);
		for (int i = 0; i < 2; i++) {
			result = result + (char) (val & 0xFF);
			val >>>= 8;
		}
		return result;
	}
}
