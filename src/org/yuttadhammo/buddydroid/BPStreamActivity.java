package org.yuttadhammo.buddydroid;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.yuttadhammo.buddydroid.rss.RssListAdapter;
import org.yuttadhammo.buddydroid.rss.RssReader;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;

public class BPStreamActivity extends ListActivity {
	private RssListAdapter adapter;
	private BPStreamActivity activity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this;
		ReadFile rf = new ReadFile();
		rf.execute("");

	}
	private ProgressDialog downloadProgressDialog;

	private class ReadFile extends AsyncTask<String, Integer, String> {
		List<JSONObject> jobs = new ArrayList<JSONObject>();
		@Override
        protected String doInBackground(String... sUrl) {
            try {
    			jobs = RssReader.getLatestRssFeed(getBaseContext());

            } catch (Exception e) {
            	e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
	        downloadProgressDialog = new ProgressDialog(activity);
	        downloadProgressDialog.setCancelable(true);
	        downloadProgressDialog.setMessage(activity.getString(R.string.connecting));
	        downloadProgressDialog.setIndeterminate(true);
            downloadProgressDialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(downloadProgressDialog.isShowing())
            	downloadProgressDialog.dismiss();
    		adapter = new RssListAdapter(activity,jobs);
    		setListAdapter(adapter);
        }
    }
   @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }
}
