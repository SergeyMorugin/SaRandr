package com.example.smartautorec;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.TimeZone;

import com.example.smartautorec.ServiceClass;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class LocationService extends Service {
	public static final String GPS_LOG_MAX_DURATION = "GPS_LOG_MAX_DURATION";
	private LocationManager lm;
	private Calendar cal1;
	private BufferedOutputStream outStream;
	private OutputStreamWriter sWriter;

	BufferedWriter bw;
	// private String appDirString;
	private Long fStartTime;
	private boolean fNewStart = true;
	private boolean fLogFileOpen = false;
	// Время записи в один файл
	private int fLogMaxDuration = 300000;
	//
	final long WRITE_SOCKET_DELAY = 120000;
	final int WRITE_SOCKET_TASK = 1;
	SocketData fSocketData = new SocketData();
	Location fLocation;
	//
	ClientSocket fClientSocket;
	//
	Handler h;
	Handler.Callback hc = new Handler.Callback() {
		public boolean handleMessage(Message msg) {
			// Log.d(LOG_TAG, "what = " + msg.what);
			if (h == null) {
				return false;
			}

			switch (msg.what) {
			case WRITE_SOCKET_TASK: // Регулярная последней координаты
				if (fSocketData.fFifoLocation.isEmpty()) {
					fLocation = lm
							.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					if (fLocation == null)
						fLocation = lm
								.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					LocRecordToStr(fLocation);
				}
				h.sendEmptyMessageDelayed(WRITE_SOCKET_TASK, WRITE_SOCKET_DELAY);
				break;
			}
			return false;
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		fLogMaxDuration = intent.getIntExtra(GPS_LOG_MAX_DURATION, 30000);
		lm = (LocationManager) getSystemService(LOCATION_SERVICE);
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				ServiceClass.GPSTimeUpdate, ServiceClass.GPSDistanseUpdate,
				locationListener);
		//
		int i = Integer.parseInt(ServiceClass.Serial_number);
		fSocketData.fControlNum = (char) (i & 0xFFFF);
		fSocketData.fIPAddress = ServiceClass.ServerGPSIPAddress;
		fSocketData.fPort = ServiceClass.ServerGPSIPPort;
		fSocketData.fFifoLocation = new LinkedList<String>();
		//
		h = new Handler(hc);
		h.sendEmptyMessageDelayed(WRITE_SOCKET_TASK, 2000);
		//
		fClientSocket = new ClientSocket();
		fClientSocket.execute(fSocketData);
		//
		ServiceClass.WriteLog(1, "Start GPSService");
		// StartThread();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		lm.removeUpdates(locationListener);
		// CloseLogFile();
		fClientSocket.cancel(true);
		ServiceClass.WriteLog(1, "Stop GPSService");
	}

	// формируем запись по полученным координатам
	private String LocRecordToStr(Location l) {
		if (l == null)
			return "";
		String s = LocationToStr(l.getLatitude())
				+ LocationToStr(l.getLongitude()) + DateTimeToStr(l.getTime())
				+ SpeedToStr(l.getSpeed());
		char ch = (char) ((int) (fSocketData.fBuffNum) & 0xFF);
		s = ch + s;
		ch = (char) ((int) (fSocketData.fBuffNum) >> 8 & 0xFF);
		s = ch + s;
		s = s + (char) (0x00) + (char) (0x00) + (char) (0x00) + (char) (0x00)
				+ (char) (0x00) + (char) (0x00) + (char) (0x3F);
		String s1 = l.getProvider();
		if (s1.indexOf(LocationManager.GPS_PROVIDER) > -1) {
			s = s + (char) (0x02);
		} else
			s = s + (char) (0x01);
		s = s + Crc8(s);
		fSocketData.fBuffNum++;
		fSocketData.fFifoLocation.add(s);
		ServiceClass.WriteLog(2, ServiceClass.StrToHex3(s));
		return s;
	}

	// подсчет контрольной суммы
	private char Crc8(String s) {
		int j, crc = 0;
		for (int i = 0; i < s.length(); i++) {
			j = s.charAt(i);
			if ((crc - j) < 0) {
				crc = 256 + crc - j;
			} else {
				crc = crc - j;
			}
		}
		return (char) (crc & 0xFF);
	}

	// кодирование координат
	private String LocationToStr(double d) {
		String s = "";
		int i1 = (int) d;
		int i = (i1 * 10000000);
		//
		double d1 = (d - i1) * 60;
		i1 = (int) (d1 * 100000);
		i = i + (i1);
		//

		char ch = (char) (i & 0xFF);
		s = ch + s;
		//
		ch = (char) ((i >> 8) & 0xFF);
		s = ch + s;
		//
		ch = (char) ((i >> 16) & 0xFF);
		s = ch + s;
		//
		ch = (char) ((i >> 24) & 0xFF);
		s = ch + s;
		return s;
	}

	// кодирование скорости
	private String SpeedToStr(float f) {
		String s = "";
		int i = (int) (f / 0.01852);
		//
		char ch = (char) (i & 0xFF);
		s = ch + s;
		//
		ch = (char) ((i >> 8) & 0xFF);
		s = ch + s;
		//
		ch = (char) ((i >> 16) & 0xFF);
		s = ch + s;
		//
		ch = (char) ((i >> 24) & 0xFF);
		s = ch + s;
		return s;
	}

	// кодирование даты-время
	private String DateTimeToStr(Long l) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(l);
		cal.setTimeZone(TimeZone.getTimeZone("GMT+0"));
		// cal.
		// Date d = new Date(l);
		String s = "";
		// переводим дату
		int i = cal.get(Calendar.DATE) * 10000 + (cal.get(Calendar.MONTH) + 1)
				* 100 + (cal.get(Calendar.YEAR) - 2000);
		char ch = (char) ((i >> 16) & 0xFF);
		s = s + ch;
		ch = (char) ((i >> 8) & 0xFF);
		s = s + ch;
		ch = (char) (i & 0xFF);
		s = s + ch;
		//
		// переводим время
		i = cal.get(Calendar.HOUR_OF_DAY) * 10000 + cal.get(Calendar.MINUTE)
				* 100 + cal.get(Calendar.SECOND);
		ch = (char) ((i >> 16) & 0xFF);
		s = s + ch;
		ch = (char) ((i >> 8) & 0xFF);
		s = s + ch;
		ch = (char) (i & 0xFF);
		s = s + ch;
		//
		return s;
	}

	private LocationListener locationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			// showLocation(location);
			LocRecordToStr(location);
			// WriteData(location);
		}

		@Override
		public void onProviderDisabled(String provider) {
			// checkEnabled();
		}

		@Override
		public void onProviderEnabled(String provider) {
			// checkEnabled();
			// showLocation(locationManager.getLastKnownLocation(provider));
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			if (provider.equals(LocationManager.GPS_PROVIDER)) {
				// tvStatusGPS.setText("Status: " + String.valueOf(status));
			} else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
				// tvStatusNet.setText("Status: " + String.valueOf(status));
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private boolean CloseLogFile() {
		if (!fLogFileOpen)
			return fLogFileOpen;
		try {
			bw.close();
			fLogFileOpen = false;
			// sWriter = null;
		} catch (Exception t1) {
			ServiceClass.WriteLog(-1, "GPSException2: " + t1.toString());
		}
		return !fLogFileOpen;
	}

	private boolean OpenLogFile() {
		if (fLogFileOpen)
			return !fLogFileOpen;
		try {

			String filename = ServiceClass
					.currentDateToString(ServiceClass.Date_format1) + ".txt";
			File appDir = new File(ServiceClass.Gps_directory
			/*
			 * + ServiceClass .currentDateToString(ServiceClass.Date_format2) +
			 * "/"
			 */);
			if (!appDir.exists())
				appDir.mkdirs();
			File file = new File(appDir, filename);
			outStream = new BufferedOutputStream(new FileOutputStream(file));
			// sWriter = new OutputStreamWriter(outStream);
			bw = new BufferedWriter(new OutputStreamWriter(outStream));
			//
			ServiceClass.WriteLog(1, "GPSLogFile: " + appDir + filename);
			//
			fLogFileOpen = true;

		} catch (Throwable t1) {
			// Toast.makeText(getApplicationContext(),
			// "Exception: " + t1.toString(), Toast.LENGTH_LONG).show();
			ServiceClass.WriteLog(-1, "GPSException3: " + t1.toString());
		}
		return fLogFileOpen;
	}

	public void WriteData(Location location) {
		try {
			// проверяем не надо ли начать новый файл
			cal1 = Calendar.getInstance();
			if (fNewStart) {
				fNewStart = false;
				fStartTime = cal1.getTimeInMillis();
				CloseLogFile();
				OpenLogFile();
				// Intent intent = new Intent(
				// SupervisorService.BROADCAST_ACCEL_ACTION);
				// intent.putExtra(MainActivity.VAL1, values[0]);
				// intent.putExtra(MainActivity.VAL2, values[1]);
				// intent.putExtra(MainActivity.VAL3, values[2]);
				// sendBroadcast(intent);
			}
			Long currentTime = cal1.getTimeInMillis() - fStartTime;
			// проверяем не идет ли превышение записи по времени
			if (currentTime > fLogMaxDuration) {
				fNewStart = true;
			}

			String data = "<l>"
					+ ServiceClass
							.currentDateToString(ServiceClass.Date_format1)
					+ "|" // String.valueOf(location.getTime()) + "|"
					+ String.valueOf(location.getLatitude()) + "|"
					+ String.valueOf(location.getLongitude()) + "|"
					+ String.valueOf(location.getSpeed()) + "|"
					+ String.valueOf(location.getAltitude()) + "|" + "<n>"
					+ "\r\n";

			// outStream.write(data., 1, 3);
			// sWriter.write(data);
			bw.write(data);
		} catch (Throwable t1) {
			ServiceClass.WriteLog(-1, "GPSException1: " + t1.toString());
		}
	}

}

// /////////////////////////////////////////////////////////////////////////////////////////////////////////
/*
 * private void StartThread() { Thread thread = new Thread(null,
 * doBackgroundThreadProcessing, "Background"); thread.start(); }
 * 
 * private Runnable doBackgroundThreadProcessing = new Runnable() { public void
 * run() { StartSocket(); while (true) { SocketExecute(); } } };
 * 
 * private void StartSocket() { fSocketStatus = 0; ClientSocket = null; try {
 * ClientSocket = new Socket("95.79.54.148", 3055); SocketReader = new
 * BufferedReader(new InputStreamReader( ClientSocket.getInputStream()));
 * SocketWriter = new BufferedWriter(new OutputStreamWriter(
 * ClientSocket.getOutputStream()));
 * //h.sendEmptyMessageDelayed(READ_SOCKET_TASK, READ_SOCKET_DELAY);
 * ServiceClass.WriteLog(2, "OpenSocket"); // String sendString = ((EditText)
 * findViewById(R.id.etText)) // .getText().toString(); //
 * bufferedWriter.write(sendString); // bufferedWriter.flush();
 * 
 * // String textOut = bufferedReader.readLine(); // ((TextView)
 * findViewById(R.id.tvRecvData)).setText("Recv ok:\n" // + textOut); //
 * ClientSocket.close(); fSocketStatus = 1; } catch (UnknownHostException e) {
 * ServiceClass.WriteLog(-1, "OpenSocket. Unknown Host Exception!"); } catch
 * (IOException e) { ServiceClass.WriteLog(-1, "OpenSocket. IO Exception!"); }
 * catch (Exception e) { ServiceClass .WriteLog(3, "OpenSocket. Exception: " +
 * e.getMessage()); }
 * 
 * }
 * 
 * private void StopSocket() { if (ClientSocket != null) { try {
 * ClientSocket.close(); ClientSocket = null; fSocketStatus = 0; } catch
 * (IOException e) { ServiceClass.WriteLog(3, "CloseSocket. IO Exception!"); }
 * 
 * } }
 * 
 * private void SocketExecute() { // int i = SocketReader. if (ClientSocket ==
 * null) return; if (!ClientSocket.isConnected()) return; String buff = ""; try
 * { buff = SocketReader.readLine(); } catch (IOException e) {
 * ServiceClass.WriteLog(3, "ReadSocket. IO Exception!"); } if
 * (buff.length()==0) return;
 * 
 * switch (fSocketStatus) { case 0: break; case 1: if(buff.indexOf("+v")>-1)
 * WriteBuff(">REV 07.502.508"); if(buff.indexOf("+complex")>-1)
 * WriteBuff("complex>0023TestTest000100003F"); if(buff.indexOf("+typepan")>-1){
 * WriteBuff("typepan>010444"); fSocketStatus = 2; ServiceClass.WriteLog(2,
 * "HandshakeSocket"); } break; case 2:
 * 
 * break; }
 * 
 * }
 * 
 * 
 * 
 * private void WriteBuff(String s){ try { SocketWriter.write(s+"\n\rOK\n\r");
 * 
 * SocketWriter.flush(); } catch (UnknownHostException e) {
 * ServiceClass.WriteLog(3, "WriteSocket. Unknown Host Exception!"); } catch
 * (IOException e) { ServiceClass.WriteLog(3, "WriteSocket. IO Exception!"); }
 * catch (Exception e) { ServiceClass .WriteLog(3, "WriteSocket. Exception: " +
 * e.getMessage()); } }
 * 
 * private void PopLocate() { //
 * 
 * }
 */
