package com.example.smartautorec;

import java.lang.Thread.UncaughtExceptionHandler;

import com.example.smartautorec.ServiceClass;

public class ExceptionClass implements UncaughtExceptionHandler{

	Thread.UncaughtExceptionHandler oldHandler;
	
	public ExceptionClass() {
        oldHandler = Thread.getDefaultUncaughtExceptionHandler(); // �������� ����� ������������� ����������
    }
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		// TODO Auto-generated method stub   z ,
		
		ServiceClass.WriteLog(-1, "Thread Exception:" + ex.toString());
		if(oldHandler != null) // ���� ���� ����� �������������...
            oldHandler.uncaughtException(thread, ex); // ...������� ���
		 ;
	}

}
