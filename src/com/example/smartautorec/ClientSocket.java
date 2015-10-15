package com.example.smartautorec;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Calendar;

import android.os.AsyncTask;

public class ClientSocket extends AsyncTask<SocketData, SocketData, Integer> {

	Socket ClientSocket = null;
	SocketData fSocketData;
	BufferedReader SocketReader = null;
	//BufferedWriter SocketWriter = null;
	OutputStream SocketWriter = null;
	//private OutputStream os = null;
	//ObjectOutputStream ous;
	int fSocketStatus = 0;

	@Override
	protected Integer doInBackground(SocketData... params) {
		// TODO Auto-generated method stub
		fSocketStatus = 0;
		fSocketData = params[0];

		while (true) {
			if (StartSocket()) {
				while (ClientSocket != null) {
					if (!SocketExecute())
						StopSocket();
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						ServiceClass.WriteLog(-1, "Sleep100");
					}
				}
			}// StopSocket();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				ServiceClass.WriteLog(-1, "Sleep1000");
			}
		}

		// return null;
	}

	private boolean SocketExecute() {
		// int i = SocketReader.
		if (ClientSocket == null)
			return false;
		if (!ClientSocket.isConnected())
			return false;
		String buff = "";

		switch (fSocketStatus) {
		case 0:
			break;
		case 1:
			try {
				buff = SocketReader.readLine();
			} catch (IOException e) {
				ServiceClass.WriteLog(-1, "ReadSocket1. IO Exception!");
				return false;
			}
			if (buff.length() == 0)
				return true;
			if (buff.indexOf("+v") > -1)
				if (!WriteBuff(">REV 07.502.508"))
					return false;
			if (buff.indexOf("+complex") > -1)
				if (!WriteBuff("complex>"
						+ ServiceClass.toHEX(fSocketData.fControlNum, 4)
						+ fSocketData.fPass
						+ ServiceClass.toHEX(fSocketData.fBuffNum, 4)
						+ "00003F"))
					return false;
			if (buff.indexOf("+typepan") > -1) {
				if (!WriteBuff("typepan>010444"))
					return false;
				fSocketStatus = 2;
				ServiceClass.WriteLog(2, "HandshakeSocket");
			}
			break;
		case 2: // Ожидание новых координат
			if (!fSocketData.fFifoLocation.isEmpty()) {
				String s = fSocketData.fFifoLocation.peek();
				if (SendLocate(s)) {
					fSocketData.fFifoLocation.remove();
				} else
					return false;
			}

			// отвечаем на команды
			/*try {
				buff = SocketReader.readLine();
			} catch (IOException e) {
				ServiceClass.WriteLog(-1, "ReadSocket2. IO Exception!");
				return false;
			}
			if (buff.length() == 0)
				return true;
			if (buff.indexOf("+PGPS") > -1)
				WriteBuff("");*/

			break;
		}
		return true;

	}

	// Отправляет текст
	private boolean WriteBuff(String s) {
		try {
			SocketWriter.write((s + "\n\rOK\n\r").getBytes());
			SocketWriter.flush();
			return true;
		} catch (UnknownHostException e) {
			ServiceClass.WriteLog(-1, "WriteSocket1. Unknown Host Exception!");
		} catch (IOException e) {
			ServiceClass.WriteLog(-1, "WriteSocket2. IO Exception!");
		} catch (Exception e) {
			ServiceClass.WriteLog(-1,
					"WriteSocket. Exception: " + e.getMessage());
		}
		return false;
	}

	// Формирует пакет и ожидает ответ
	private Boolean SendLocate(String buff) {
		// номер координат
		char c = (char) (((int) buff.charAt(0) << 8) + ((int) buff.charAt(1)));
		String buff2 = (ServiceClass.toHEX(c, 4)).toUpperCase() + (char)0x3E + (char)0x01 + (char)0x3E + buff.substring(2) + "\r\nOK\r\n";
		//
		try {			
			for (int i = 0; i < buff2.length(); i++) 			
				SocketWriter.write(buff2.charAt(i) & 0xFF);	
			SocketWriter.flush();			
		} catch (UnknownHostException e) {
			ServiceClass.WriteLog(-1, "WriteSocket3. Unknown Host Exception!");
			return false;
		} catch (IOException e) {
			ServiceClass.WriteLog(-1, "WriteSocket4. IO Exception!");
			return false;
		} catch (Exception e) {
			ServiceClass.WriteLog(-1,
					"WriteSocket. Exception: " + e.getMessage());
			return false;
		}
		// Ожидаем ответа
		Calendar cal1 = Calendar.getInstance();
		long l = cal1.getTimeInMillis();
		String buff3 = "";
		while ((cal1.getTimeInMillis() - l) < 10000) { // ждем ответ 10 секунд ответа		
			try {
				buff3 = buff3 + SocketReader.readLine();
			} catch (IOException e) {
				ServiceClass.WriteLog(-1, "ReadSocket3. IO Exception!");
				return false;
			}
			if (buff3.indexOf("send ok") > -1)
				return true;
			cal1 = Calendar.getInstance();
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				ServiceClass.WriteLog(-1, "ReadSocket4. InterruptedException!");
			}
		}

		return false;
	}

	private boolean StartSocket() {
		fSocketStatus = 0;
		ClientSocket = null;
		try {
			ClientSocket = new Socket(fSocketData.fIPAddress, fSocketData.fPort);
			SocketReader = new BufferedReader(new InputStreamReader(
					ClientSocket.getInputStream()));
			SocketWriter = ClientSocket.getOutputStream();
			fSocketStatus = 1;
			ServiceClass.WriteLog(2, "OpenSocket");			
		} catch (UnknownHostException e) {
			ServiceClass.WriteLog(-1, "OpenSocket1. Unknown Host Exception!");
			return false;
		} catch (IOException e) {
			ServiceClass.WriteLog(-1, "OpenSocket2. IO Exception!");
			return false;
		} catch (Exception e) {
			ServiceClass
					.WriteLog(-1, "OpenSocket. Exception: " + e.getMessage());
			return false;
		}
		return true;

	}

	private void StopSocket() {
		if (ClientSocket != null) {
			try {
				ClientSocket.close();
				ClientSocket = null;
				fSocketStatus = 0;
			} catch (IOException e) {
				ServiceClass.WriteLog(-1, "CloseSocket1. IO Exception!");
			}

		}
	}

	@Override
	protected void onProgressUpdate(SocketData... progress) {
	}

	@Override
	protected void onPostExecute(Integer result) {
		// Это выполнится после завершения работы потока
		StopSocket();
	}

	@Override
	protected void onPreExecute() {
		// Это выполнится после завершения работы потока
	}

}

//BufferedOutputStream outStream = new BufferedOutputStream(new
			// OutputStreamWriter(ClientSocket.getOutputStream()));
			// sWriter = new OutputStreamWriter(outStream);
			// ClientSocket.
			//SocketWriter = new BufferedWriter(new OutputStreamWriter(
					//ClientSocket.getOutputStream()));
			//ous = new ObjectOutputStream(ClientSocket.getOutputStream());

			// SocketWriter.
			// h.sendEmptyMessageDelayed(READ_SOCKET_TASK, READ_SOCKET_DELAY);
