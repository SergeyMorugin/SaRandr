package com.example.smartautorec;

import java.io.File;
import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.IBinder;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View.OnClickListener;


public class RecordService extends Service implements AutoFocusCallback,
		OnInfoListener {
	public static final String SERVICE_TYPE = "SERVICE_TYPE";
	public static final String RECORD_FILE_MAX_DURATION = "RECORD_FILE_MAX_DURATION";
	public static final String DATA_CONT = "DATA_CONT";
	//
	public static final byte VIDEO_RECORD_SERVICE_TYPE = 1;
	public static final byte AUDIO_RECORD_SERVICE_TYPE = 2;
	private byte fRecordType = 1;
	Camera camera;
	SurfaceHolder surfaceHolder;
	String fRecordFile;
	MediaRecorder mediaRecorder;
	int fFileMaxDuration = 60000;
	String fAppDataCont;

	// public SurfaceView preview;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		/*
		 * //sm = (SensorManager) getSystemService(SENSOR_SERVICE); appDirString
		 * = intent.getStringExtra(MainActivity.APP_DIR); appDataCont =
		 * intent.getStringExtra(MainActivity.DATA_CONT); rec_start();
		 */
		fRecordType = intent.getByteExtra(SERVICE_TYPE,
				VIDEO_RECORD_SERVICE_TYPE);
		fAppDataCont = intent.getStringExtra(DATA_CONT);
		// Максимальное время записи
		fFileMaxDuration = intent.getIntExtra(RECORD_FILE_MAX_DURATION,60000);
		switch (fRecordType) { // Запись видео
		case VIDEO_RECORD_SERVICE_TYPE:			
			// Подготавливаем камеру
			camera = Camera.open();
			surfaceHolder = MainActivity.preview.getHolder();
			try {
				camera.setPreviewDisplay(surfaceHolder);
				camera.setDisplayOrientation(90);
				camera.startPreview();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			//
			StartVideoRecord();
			break;
		case AUDIO_RECORD_SERVICE_TYPE: // Запись аудио				
			//
			StartAudioRecord();
			break;
		}
		ServiceClass.WriteLog(1, "Start RecordService");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		switch (fRecordType) {
		case VIDEO_RECORD_SERVICE_TYPE:// Запись видео
			StopVideoRecord();
			if (camera != null)
				camera.release();
			camera = null;
			break;
		case AUDIO_RECORD_SERVICE_TYPE: // Запись аудио
			StopAudioRecord();
			break;
		}

		ServiceClass.WriteLog(1, "Stop RecordService");
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////
	public boolean prepareAudioRecorder() {
		// Создаем ссылку на новый файл
		GetNewFile();
		mediaRecorder = new MediaRecorder();
		// mediaRecorder.set
		mediaRecorder = new MediaRecorder();
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mediaRecorder.setOutputFile(ServiceClass.Audio_recoder_file);//fRecordFile);
		//
		mediaRecorder.setMaxDuration(fFileMaxDuration);
		mediaRecorder.setOnInfoListener(this);
		// camera.autoFocus(this);
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		try {
			mediaRecorder.prepare();
		} catch (Exception e) {
			e.printStackTrace();
			releaseVideoRecorder();
			return false;
		}
		return true;
	}

	public void releaseAudioRecorder() {
		if (mediaRecorder != null) {
			mediaRecorder.reset();
			mediaRecorder.release();
			mediaRecorder = null;
		}
	}

	public void StartAudioRecord() {
		if (prepareAudioRecorder()) {
			mediaRecorder.start();
		} else {
			releaseAudioRecorder();
		}
	}

	public void StopAudioRecord() {
		if (mediaRecorder != null) {
			mediaRecorder.stop();
			releaseAudioRecorder();
			// переносим файл в папку для синхронизации
			File from = new  File(ServiceClass.Audio_recoder_file);
			File to = new  File(fRecordFile);
			from.renameTo(to);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////
	public boolean prepareVideoRecorder() {
		// Создаем ссылку на новый файл
		GetNewFile();
		//
		camera.unlock();
		mediaRecorder = new MediaRecorder();
		// mediaRecorder.set
		mediaRecorder.setCamera(camera);
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		mediaRecorder.setProfile(CamcorderProfile
				.get(CamcorderProfile.QUALITY_LOW));
		mediaRecorder.setOutputFile(fRecordFile);
		mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
		//

		// mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		mediaRecorder.setVideoFrameRate(5); // фреймрейт записи видео
		mediaRecorder.setVideoSize(144, 176); // размер картинки
		mediaRecorder.setMaxDuration(fFileMaxDuration);
		mediaRecorder.setOnInfoListener(this);
		// camera.autoFocus(this);
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		try {
			mediaRecorder.prepare();
		} catch (Exception e) {
			e.printStackTrace();
			releaseVideoRecorder();
			return false;
		}
		return true;
	}

	public void releaseVideoRecorder() {
		if (mediaRecorder != null) {
			mediaRecorder.reset();
			mediaRecorder.release();
			mediaRecorder = null;
			camera.lock();
		}
	}

	public void StartVideoRecord() {
		if (prepareVideoRecorder()) {
			mediaRecorder.start();
		} else {
			releaseVideoRecorder();
		}
	}

	public void StopVideoRecord() {
		if (mediaRecorder != null) {
			mediaRecorder.stop();
			releaseVideoRecorder();
		}
	}

	@Override
	public void onInfo(MediaRecorder mr, int what, int extra) {
		// TODO Auto-generated method stub
		if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) { // Завершение
			ServiceClass.WriteLog(1, "Restart RecordService");					// записи
																				// по
																				// времени
			switch (fRecordType) {
			case VIDEO_RECORD_SERVICE_TYPE:
				StopVideoRecord();
				StartVideoRecord();
				break;
			case AUDIO_RECORD_SERVICE_TYPE:
				StopAudioRecord();
				StartAudioRecord();
				break;
			}

			Intent intent = new Intent(SupervisorService.BROADCAST_RECORD_ACTION);
			sendBroadcast(intent);
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void GetNewFile() {
		// находим директорию и если ее нету создаем
		String s = "";
		String filename = fAppDataCont
				+ '_'
				+ ServiceClass
						.currentDateToString(ServiceClass.Date_format1);
		if (fRecordType == VIDEO_RECORD_SERVICE_TYPE) {
			s = ServiceClass.Video_directory;
			filename = filename + ".3gp";
		}
		if (fRecordType == AUDIO_RECORD_SERVICE_TYPE) {
			s = ServiceClass.Audio_directory;
			filename = filename + ".3gpp";
		}

		File appDir = new File(s/*+ServiceClass.currentDateToString(ServiceClass.Date_format2)+ "/"*/);
		if (!appDir.exists())
			appDir.mkdirs();
		fRecordFile = s + filename;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		// TODO Auto-generated method stub

	}

}

// Подготовка к включению камеры
/*
 * public Boolean OnCreateFunc(SurfaceView pr) { camera = Camera.open(); //
 * preview = (SurfaceView) findViewById(R.id.surfaceView1); // preview = pr;
 * surfaceHolder = pr.getHolder(); try {
 * camera.setPreviewDisplay(surfaceHolder); } catch (IOException e) { // TODO
 * Auto-generated catch block e.printStackTrace(); }
 * camera.setDisplayOrientation(90); camera.startPreview(); // // return true;
 * };
 * 
 * public void OnPauseFunc() { releaseMediaRecorder(); if (camera != null)
 * camera.release(); camera = null; }
 */