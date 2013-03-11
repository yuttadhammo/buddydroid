package org.yuttadhammo.buddydroid;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.yuttadhammo.buddydroid.rss.RssListAdapter;
import org.yuttadhammo.buddydroid.rss.RssReader;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class BPStreamActivity extends ListActivity {
	private RssListAdapter adapter;
	private BPStreamActivity activity;
	private SharedPreferences prefs;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this;
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		View contentView =  View.inflate(this, R.layout.rss, null);
        setContentView(contentView);
		
		@SuppressWarnings("deprecation")
		int api = Integer.parseInt(Build.VERSION.SDK);
		
		if (api >= 14) {
			getActionBar().setHomeButtonEnabled(true);
		}
    	if(prefs.getString("website", "").length() > 0) {
    		ReadFile rf = new ReadFile();
    		rf.execute("");
    	}
    	else {
			Toast.makeText(this, getString(R.string.noWebsite),
					Toast.LENGTH_LONG).show();
    	}


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
	        downloadProgressDialog.setMessage(activity.getString(R.string.updating));
	        downloadProgressDialog.setIndeterminate(true);
            downloadProgressDialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(getListView() == null)
            	return;
            if(downloadProgressDialog.isShowing())
            	downloadProgressDialog.dismiss();
/*    		adapter = new RssListAdapter(activity,jobs);
    		if (adapter.isEmpty())
    			Toast.makeText(activity, activity.getString(R.string.checkSetupInternet),
    					Toast.LENGTH_LONG).show();
    		setListAdapter(adapter);*/
        }
    }
   @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }
	@Override
	public void onResume(){
		super.onResume();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.stream, menu);
	    return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		super.onOptionsItemSelected(item);
		
		//SharedPreferences.Editor editor = prefs.edit();
		Intent intent;
		switch (item.getItemId()) {
	        case android.R.id.home:
	            // app icon in action bar clicked; go home
	            finish();
	            break;
			case (int)R.id.menuPrefs:
				intent = new Intent(this, BPSettingsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				break;

			default:
				return false;
	    }
		return true;
	}	


}
