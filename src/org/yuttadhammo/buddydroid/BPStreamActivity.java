package org.yuttadhammo.buddydroid;

import java.util.ArrayList;
import java.util.HashMap;
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
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class BPStreamActivity extends ListActivity {

	private String TAG = "BPStreamActivity";

	private RssListAdapter adapter;
	private BPStreamActivity activity;
	private SharedPreferences prefs;
	private ProgressDialog downloadProgressDialog;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this;
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		View contentView =  View.inflate(this, R.layout.stream_activity, null);
        setContentView(contentView);
		
		@SuppressWarnings("deprecation")
		int api = Integer.parseInt(Build.VERSION.SDK);
		
		if (api >= 14) {
			getActionBar().setHomeButtonEnabled(true);
		}
    	if(prefs.getString("website", "").length() > 0) {
    		refreshStream();
    	}
    	else {
			Toast.makeText(this, getString(R.string.noWebsite),
					Toast.LENGTH_LONG).show();
    	}


	}

	
	public void refreshStream() {
        downloadProgressDialog = new ProgressDialog(activity);
        downloadProgressDialog.setCancelable(true);
        downloadProgressDialog.setMessage(activity.getString(R.string.updating));
        downloadProgressDialog.setIndeterminate(true);
        downloadProgressDialog.show();
        
		BPStream stream = new BPStream(this, mHandler, Buddypress.getStreamScope(), Buddypress.getStreamMax());
		stream.get();
	}
	
	/** Handler for the message from the timer service */
	private Handler mHandler = new Handler() {
		
		@Override
        public void handleMessage(Message msg) {
			if(msg.what == Buddypress.MSG_SUCCESS ) {
				
				Log.i(TAG ,"got message");
				
				HashMap<?, ?> rss = (HashMap<?, ?>) msg.obj;
				Object obj = rss.get("activities");
				
				Object[] list = (Object[]) obj;
				
				adapter = new RssListAdapter(activity,list);
				if (adapter.isEmpty())
					Toast.makeText(activity, activity.getString(R.string.checkSetupInternet),
							Toast.LENGTH_LONG).show();
				setListAdapter(adapter);
			}
			Toast.makeText(activity, (CharSequence) getString(msg.arg1),
					Toast.LENGTH_LONG).show();
			if(downloadProgressDialog.isShowing())
				downloadProgressDialog.dismiss();		
		}
    };

	
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
