package com.example.smartautorec;


public class TrashFile {

}

// Сохранение все в файл из текстового документа
/*
 * private void saveFile(String fileName) { try{
 * 
 * File file = new File(dir,fileName); FileOutputStream fos = new
 * FileOutputStream(file); fos.write(mainBuff.getBytes()); fos.close(); } catch
 * (Exception e){ Toast.makeText(this, e.toString(),Toast.LENGTH_LONG).show(); }
 * 
 * }
 */

/*
 * @Override // Деструктор главного класса public void onDestroy(){
 * super.onDestroy(); //camera.release();
 * 
 * //saveFile(myfileName+getCurrentTime()+".txt"); try{ fos.flush();
 * fos.close(); fos = null; } catch (Exception e){ Toast.makeText(this,
 * "(OnClose) "+e.toString(),Toast.LENGTH_LONG).show(); } }
 */

/*
 * // обработчик событий от акселерометра private SensorEventListener listener1
 * = new SensorEventListener(){
 * 
 * @Override public void onSensorChanged(SensorEvent event) { // TODO
 * Auto-generated method stub text2.setText(String.valueOf(event.values[0])+"|"
 * + String.valueOf(event.values[1])+"|" + String.valueOf(event.values[2])+"|");
 * //text5.append("<a>"+text2.getText().toString() +"<r>"); mainBuff
 * ="<a>"+text2.getText().toString() +"<r>"; saveData(); }
 * 
 * @Override public void onAccuracyChanged(Sensor sensor, int accuracy) { //
 * TODO Auto-generated method stub
 * 
 * } };
 */

/*
 * @Override // Конструктор главного класса public void onStart(){
 * super.onStart();
 * 
 * manager1 = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
 * 
 * // ищем акселерометр sensors =
 * manager1.getSensorList(Sensor.TYPE_ACCELEROMETER); if(sensors.size() !=0){
 * manager1.registerListener(listener1, sensors.get(0),
 * SensorManager.SENSOR_DELAY_FASTEST); } else { AlertDialog.Builder builder =
 * new AlertDialog.Builder(this);
 * 
 * builder.setTitle(R.string.app_name); builder.setMessage("Нет акселерометра");
 * builder.setPositiveButton( "OK", new DialogInterface.OnClickListener() {
 * 
 * @Override public void onClick(DialogInterface dialog, int which) { // TODO
 * Auto-generated method stub MainActivity.this.finish(); } }); builder.show();
 * } }
 */

/*
 * private LocationListener locListener = new LocationListener(){
 * 
 * @Override public void onLocationChanged(Location location) { // TODO
 * Auto-generated method stub printLocation(location); }
 * 
 * @Override public void onStatusChanged(String provider, int status, Bundle
 * extras) {// TODO Auto-generated method stub }
 * 
 * @Override public void onProviderEnabled(String provider) { // TODO
 * Auto-generated method stub }
 * 
 * @Override public void onProviderDisabled(String provider) { // TODO
 * Auto-generated method stub printLocation(null); }
 * 
 * };
 * 
 * private void printLocation(Location loc){ if (loc != null) {
 * text4.setText(loc.getLongitude()+"|"+loc.getLatitude()); mainBuff =
 * "<l>"+getCurrentTime()+"|"+text4.getText().toString() +"<n>";
 * //text5.append("<l>"+getCurrentTime()+"|"+text4.getText().toString() +"<n>");
 * saveData(); } else{ text4.setText("Location unavaible"); } }
 * 
 * private void saveData(){ try{ if (fos != null){
 * fos.write(mainBuff.getBytes()); } } catch (Exception e){ Toast.makeText(this,
 * "(OnWrite) "+e.toString(),Toast.LENGTH_LONG).show(); } }
 */

/*
 * @Override // Конструктор protected void onCreate(Bundle savedInstanceState) {
 * super.onCreate(savedInstanceState); setContentView(R.layout.activity_main);
 * 
 * if (savedInstanceState == null) {
 * getSupportFragmentManager().beginTransaction() .add(R.id.container, new
 * PlaceholderFragment()) .commit(); } //
 * 
 * // определяем указатели на тектовые поля text7 =
 * (TextView)findViewById(R.id.textView7); text8 =
 * (TextView)findViewById(R.id.textView8); text9 =
 * (TextView)findViewById(R.id.textView9); // text4 =
 * (TextView)findViewById(R.id.textView4); text5 =
 * (TextView)findViewById(R.id.textView5); et1 =
 * (EditText)findViewById(R.id.editText1); // button_rec_off =
 * (Button)findViewById(R.id.button2); button_rec_on =
 * (Button)findViewById(R.id.button1); // cb1 =
 * (CheckBox)findViewById(R.id.checkBox1); cb2 =
 * (CheckBox)findViewById(R.id.checkBox2); cb3 =
 * (CheckBox)findViewById(R.id.checkBox3); //text5.setText(""); mainBuff = new
 * String(); // startServiceIntent = new
 * Intent(this,SensorLoggerService.class).putExtra
 * (APP_DIR,getString(R.string.log_directory));// appDirString); // //manager2 =
 * (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 * //manager2.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
 * locListener); //Location loc =
 * manager2.getLastKnownLocation(LocationManager.GPS_PROVIDER); // // находим
 * директорию и если ее нету создаем //dir =
 * Environment.getExternalStorageDirectory().toString() + DIRECTORY_DOCUMENTS;
 * File folger = new File(dir);
 * 
 * if(!folger.exists()){ folger.mkdir(); }
 * 
 * text5.setText(dir+"/"+myfileName+getCurrentTime()+".txt"); try{
 * 
 * File file = new File(dir,myfileName+getCurrentTime()+".txt"); fos = new
 * FileOutputStream(file); } catch (Exception e){ Toast.makeText(this,
 * "(OnOpen) "+e.toString(),Toast.LENGTH_LONG).show(); } // printLocation(loc);
 * // camera = Camera.open(); preview =
 * (SurfaceView)findViewById(R.id.surfaceView1); surfaceHolder =
 * preview.getHolder(); try { camera.setPreviewDisplay(surfaceHolder); } catch
 * (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); }
 * camera.setDisplayOrientation(90); camera.startPreview(); //
 * //prepareVideoRecorder(); //
 * 
 * }
 */

// private SensorManager manager1;
// private List<Sensor> sensors;

// GPS
// private LocationManager manager2;

//
// FileOutputStream fos;

/*
 * private String getCurrentTime() { Calendar calendar = Calendar.getInstance();
 * int d = calendar.get(Calendar.DAY_OF_MONTH); int m =
 * calendar.get(Calendar.MONTH); int y = calendar.get(Calendar.YEAR); int hour =
 * calendar.get(Calendar.HOUR_OF_DAY); int minute =
 * calendar.get(Calendar.MINUTE); int second = calendar.get(Calendar.SECOND);
 * return String.format("%02d_%02d_%02d_%02d_%02d_%02d",d,m,y, hour, minute,
 * second); // ЧЧ:ММ:СС - формат времени }
 * 
 * 
 * private void SetBrigtness(int value) { WindowManager.LayoutParams params =
 * getWindow().getAttributes(); params.screenBrightness = (float)value/100;
 * getWindow().setAttributes(params); }
 * 
 * public void UpdateProg() { String PATH = MyServiceClass.Update_directory;
 * File appDir = new File(MyServiceClass.Update_directory +
 * "SmartAutoRec1.apk"); if (appDir.exists()) { try { //Intent promptInstall =
 * new Intent(Intent.ACTION_VIEW).setData( //Uri.parse(PATH +
 * "SmartAutoRec1.apk")).setType( //"application/android.com.app");
 * //startActivity(promptInstall);// installation is not working
 * 
 * Intent intent = new Intent(Intent.ACTION_VIEW);
 * intent.setDataAndType(Uri.fromFile(appDir),
 * "application/vnd.android.package-archive");
 * intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(intent);
 * 
 * } catch (Throwable t1) { Toast.makeText(getApplicationContext(),
 * "Exception: " + t1.toString(), Toast.LENGTH_LONG) .show(); } } }
 * 
 * 
 * 
 * private void SetNonStopService() { //Settings.System.putInt
 * (getContentResolver (), Settings.System.SCREEN_OFF_TIMEOUT, -1); PowerManager
 * pm = (PowerManager) getSystemService(Context.POWER_SERVICE); wl =
 * pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, //|
 * //PowerManager.ACQUIRE_CAUSES_WAKEUP, //PowerManager.PARTIAL_WAKE_LOCK,
 * "MyScreenLock"); wl.acquire();
 * 
 * }
 */


