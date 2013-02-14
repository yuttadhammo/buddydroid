
package org.yuttadhammo.buddydroid;

import java.net.URI;
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
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Buddypress extends ListActivity {
	

	public static String versionName = "1";
	private static SharedPreferences prefs;
	private TextView textContent;
	private Button submitButton;
	private Buddypress activity;
	private RssListAdapter adapter;
	private boolean land;
	private RelativeLayout listPane;
	private String website;
	private boolean manualRefresh;


	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		@SuppressWarnings("deprecation")
		int api = Integer.parseInt(Build.VERSION.SDK);
		
		if (api >= 14) {
			getActionBar().setHomeButtonEnabled(true);
		}
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		submitButton = (Button)findViewById(R.id.submit);
		submitButton.setOnClickListener(mSubmitListener);
		       
		textContent = (TextView) findViewById(R.id.text_content);
		
		Intent intent = this.getIntent();
		
    	if(intent.hasExtra(Intent.EXTRA_TEXT)) {
    		Log.i("Buddypress","Got text: "+intent.getStringExtra(Intent.EXTRA_TEXT));
			textContent.setText(textContent.getText()+intent.getStringExtra(Intent.EXTRA_TEXT));
    	}
    	
    	listPane = (RelativeLayout) findViewById(R.id.list_pane);
    	
    	DisplayMetrics metrics = new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(metrics);
    	int width = metrics.widthPixels; 
    	
    	land = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && width > 600;

    	this.activity = this;
    	
    	website = prefs.getString("website", "");
    	if(land && website.length() > 0) {
    		listPane.setVisibility(View.VISIBLE);
    		if(prefs.getBoolean("auto_update", true))
    			refreshStream();
    	}
	}
	
	@Override
	public void onResume(){
		super.onResume();
	
		land = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

    	this.activity = this;
    	String newWebsite = prefs.getString("website", "");
    	if(land && newWebsite.length() > 0 && (listPane.getVisibility() == View.GONE || !website.equals(newWebsite))) {
    		website = newWebsite;
    		listPane.setVisibility(View.VISIBLE);
    		if(prefs.getBoolean("auto_update", true))
    			refreshStream();
    	}
	}
	@Override
	protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
    	if(intent.hasExtra(Intent.EXTRA_TEXT)) {
    		String text = textContent.getText().toString();
    		String add = "";
    		if(text.length() > 0)
    			add = "\n";
			textContent.setText(text+add+intent.getStringExtra(Intent.EXTRA_TEXT));
    	}
    		
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
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
	            return true;

			case (int)R.id.menuStream:
		    	if(prefs.getString("website", "").length() == 0) {
					Toast.makeText(this, getString(R.string.noWebsite),
							Toast.LENGTH_LONG).show();
					return true;
		    	}
				if(listPane.getVisibility() == View.VISIBLE){
					refreshStream();
					manualRefresh = true;
		    		return true;
				}
				
				intent = new Intent(this, BPStreamActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
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


	
	private OnClickListener mSubmitListener = new OnClickListener()
	{

		public void onClick(View v)
		{
			String text = textContent.getText().toString();
			if(text.length() == 0)
				return;

			if(getApiKey().length() < 1) {
				Toast.makeText(Buddypress.this, "Please set up your account first...",
						Toast.LENGTH_SHORT).show();
				return;
			}
				
			BPStatus bpstatus = new BPStatus(text, getApplicationContext(), Buddypress.this);
			bpstatus.upload();
		}
	};

		
	protected void  onActivityResult (int requestCode, int resultCode, Intent  data) {
		
		if(data != null && data.hasExtra("123")) {

		}
	}
	
   @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
    		listPane.setVisibility(View.VISIBLE);
        }
        else
        	listPane.setVisibility(View.GONE);
    }

	public static class MessageHandler extends Handler {
		
		public MessageHandler() {
		}
	
		@Override
		public void handleMessage(Message msg) {
			Log.i("message",msg.what+"");

		}
	}

	public static URI getUrl() {
		return URI.create(prefs.getString("website", "")+"index.php?bp_xmlrpc=true");
	}

	public static String getHttpuser() {
		return prefs.getString("username", "");
	}

	public static String getHttppassword() {
		return prefs.getString("password", "");
	}

	public static String getUsername() {
		return prefs.getString("username", "");
	}

	public static String getPassword() {
		return prefs.getString("password", "");
	}

	public static String getApiKey() {
		return prefs.getString("api_key", "");
	}

	public static String getServiceName() {
		return prefs.getString("service_name", "BuddyDroid");
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
            if(jobs.size() == 0 && manualRefresh) {
            	manualRefresh = false;
				Toast.makeText(Buddypress.this, "Problem retrieving feed!",
						Toast.LENGTH_LONG).show();
            }
            if(downloadProgressDialog.isShowing())
            	downloadProgressDialog.dismiss();
    		adapter = new RssListAdapter(activity,jobs);
    		setListAdapter(adapter);
        }
    }
	
	public void refreshStream() {
		ReadFile rf = new ReadFile();
		rf.execute("");
	}

}
