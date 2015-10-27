package com.example.smartautorec;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import android.content.Context;
import android.widget.ListView;
import android.widget.Toast;

import com.example.smartautorec.ServiceClass;
import com.example.smartautorec.com.savagelook.android.*;



public class RestClient extends UrlJsonAsyncTask {
    public RestClient(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

    @Override
    protected void onPostExecute(JSONObject json) {
        try {
            JSONArray jsonTasks = json.getJSONObject("data").getJSONArray("tasks");
            int length = jsonTasks.length();
            List<String> tasksTitles = new ArrayList<String>(length);

            for (int i = 0; i < length; i++) {
                tasksTitles.add(jsonTasks.getJSONObject(i).getString("title"));
            }

            //ListView tasksListView = (ListView) findViewById (R.id.textView6);
            //if (tasksListView != null) {
              //  tasksListView.setAdapter(new ArrayAdapter<String>(HomeActivity.this,
                //  android.R.layout.simple_list_item_1, tasksTitles));
            //}
            ServiceClass.WriteLog(2, "Start GPSService");
        } catch (Exception e) {
        Toast.makeText(context, e.getMessage(),
            Toast.LENGTH_LONG).show();
    } finally {
        super.onPostExecute(json);
    }
}

}
