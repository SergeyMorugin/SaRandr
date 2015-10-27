package com.example.smartautorec;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.widget.TextView;

public final class ServiceClass {
	// Версия
	public static final String Version_number = "3.1.1.24";
	// Уникальный номер
	public static final String Serial_number = "004";

	// Дирректории
	public static final String Prog_directory = "/storage/sdcard1/SmartAutoRec/";
	public static final String Data_directory = "tmp/";
	public static final String Accel_directory = Prog_directory
			+ Data_directory + Serial_number + "/Accel/";
	public static final String Gps_directory = Prog_directory + Data_directory
			+ Serial_number + "/GPS/";
	public static final String Video_directory = Prog_directory
			+ Data_directory + Serial_number + "/Video/";
	public static final String Audio_directory = Prog_directory
			+ Data_directory + Serial_number + "/Audio/";
	public static final String Audio_recoder_file = Prog_directory
			+ "SaRa.3gpp";
	public static final String Log_directory = Prog_directory + Data_directory
			+ Serial_number + "/Log/";

	// Параметры обновления
	public static final String Update_directory = Prog_directory + "Update/";
	public static final String Update_file = Update_directory + "update.apk";
	public static final String Update_delete_file = Update_directory
			+ "delete.apk";
	// Параметры GPS
	public static final String ServerGPSIPAddress = "92.242.70.233";
	public static final char ServerGPSIPPort = 3055;
	public static final int GPSDistanseUpdate = 50;
	public static final int GPSTimeUpdate = 1000;

	//
	public static final String Date_format1 = "yyyy-MM-dd_HH-mm-ss";
	
	public static final String Date_format2 = "yyyy-MM-dd";
	public static final String Date_format3 = "yyyy-MM-dd_HH";
	
	
	private static String fLastLogName = "1.txt";
	public static TextView LogText = null;
	
	// Время записи одного файла
	public static final int MAX_TIME_RECORD_DURATION = 300000;
	// Время на затухание вибрации при ударе
	public static final int ACCEL_ATTENUATION_DURATION = 10000;
	//
	public static final float ACCEL_MAX_VALUE = 1;
	
	//
	public static final String ServerRailsAddress = "http://192.168.1.105:3000/api/v1/jconfig.json";
	
	
	// формирует строку текущего времени по маске
	public static String currentDateToString(String df) {
		SimpleDateFormat sdf = new SimpleDateFormat(df);
		Calendar cal = Calendar.getInstance();
		return sdf.format(cal.getTime());
	}

	public static synchronized void WriteLog(int flag, String message) {

		try {

			
			// String filename = Log_directory+fLastLogName;
			File appDir = new File(Log_directory);
			if (!appDir.exists())
				appDir.mkdirs();
			File file = new File(appDir, fLastLogName);
			if (!file.exists()) {
				fLastLogName = currentDateToString(Date_format1) + ".txt";
				file = new File(appDir, fLastLogName);
			}

			BufferedOutputStream outStream = new BufferedOutputStream(
					new FileOutputStream(file, true));
			// OutputStreamWriter sWriter = new OutputStreamWriter(outStream);

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					outStream));

			// Выводим сообщение что запустилось
			String data = currentDateToString(Date_format1) + "["
					+ String.valueOf(flag) + "] " + message + "\r\n";
			bw.write(data);			
			bw.close();
			//
			if (flag > 1)
				if (LogText != null)
				{
					//LogText.setText(data);//  + LogText.getText());
					LogText.append(data);
				}

			// sWriter.write(data);
			// outStream.flush();
			// sWriter.close();
			// outStream.close();

		} catch (Throwable t1) {

		}

	}

	public static String toHEX(char w, int r) {
		String s = Integer.toHexString(w);
		while (s.length() < r)
			s = '0' + s;
		return s;
	}

	public static String StrToHex(String s) {
		String s1 = "";
		for (int i = 0; i < s.length(); i++) {
			s1 = s1 + toHEX(s.charAt(i), 2);
		}
		// s = '0' + s;
		return s1;
	}

	public static String StrToHex2(String s) {
		String s1 = "";
		for (int i = 0; i < s.length(); i++) {
			if ((i == 4) || (i == 7) || (i == 11) || (i == 15) || (i == 21)
					|| (i == 25) || (i == 31))
				s1 = s1 + " ";
			s1 = s1 + toHEX(s.charAt(i), 2);
		}
		// s = '0' + s;
		return s1;
	}

	public static String StrToHex3(String s) {
		String s1 = "";
		for (int i = 0; i < s.length(); i++) {
			if ((i == 2) || (i == 6) || (i == 11) || (i == 16) || (i == 20))
				s1 = s1 + " ";
			s1 = s1 + toHEX(s.charAt(i), 2);
		}
		// s = '0' + s;
		return s1;
	}
}
