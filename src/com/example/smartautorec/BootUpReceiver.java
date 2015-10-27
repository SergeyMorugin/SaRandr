package com.example.smartautorec;

import com.example.smartautorec.ServiceClass;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootUpReceiver extends BroadcastReceiver{
	//private Intent SupervisorServiceIntent;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		//для Service
		ServiceClass.WriteLog(1, "BootAutoStart");
		Intent SupervisorServiceIntent = new Intent(context, SupervisorService.class);
		context.startService(SupervisorServiceIntent);
		
	}

}
