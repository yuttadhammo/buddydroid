
package org.yuttadhammo.buddydroid;

import java.net.URI;
import java.util.HashMap;


import org.yuttadhammo.buddydroid.interfaces.BPStatus;
import org.yuttadhammo.buddydroid.interfaces.BPStream;
import org.yuttadhammo.buddydroid.interfaces.BPStreamItem;
import org.yuttadhammo.buddydroid.interfaces.RssListAdapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Buddypress extends ListActivity {
	
	protected String TAG = "Buddypress";

	public static String versionName = "1";
	private static SharedPreferences prefs;
	private TextView textContent;
	private Button submitButton;
	private Buddypress activity;
	private RssListAdapter adapter;
	private boolean land;
	private RelativeLayout listPane;
	private String website;
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		int api = Build.VERSION.SDK_INT;
		
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
		registerForContextMenu(findViewById(android.R.id.list));
    	
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
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		MenuInflater inflater = getMenuInflater();
       	inflater.inflate(R.menu.stream_longclick, menu);
        
	    menu.setHeaderTitle(getString(R.string.post_options));
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    int index = info.position;
	    final HashMap<?,?> entryMap = (HashMap<?, ?>) getListView().getItemAtPosition(index);
		Intent i;
	    
	    switch (item.getItemId()) {
			case R.id.view:
				String link = (String)entryMap.get("primary_link");
				Uri url = Uri.parse(link);
				i = new Intent(Intent.ACTION_VIEW, url);
				activity.startActivity(i);
				return true;
			case R.id.share_link:
				i = new Intent(Intent.ACTION_SEND);
				i.putExtra(Intent.EXTRA_TEXT, (String)entryMap.get("primary_link"));
				i.setType("text/plain");
				startActivity(Intent.createChooser(i, getString(R.string.share_via)));
				return true;
			case R.id.share_text:
				i = new Intent(Intent.ACTION_SEND);
				i.putExtra(Intent.EXTRA_TEXT, (String)entryMap.get("content"));
				i.setType("text/plain");
				startActivity(Intent.createChooser(i, getString(R.string.share_via)));
				return true;
			case R.id.delete:
		        new AlertDialog.Builder(this)
		        .setIcon(android.R.drawable.ic_dialog_alert)
		        .setTitle(R.string.delete)
		        .setMessage(R.string.really_delete)
		        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

		            @Override
		            public void onClick(DialogInterface dialog, int which) {
						downloadProgressDialog = new ProgressDialog(activity);
				        downloadProgressDialog.setCancelable(true);
				        downloadProgressDialog.setMessage(activity.getString(R.string.deleting));
				        downloadProgressDialog.setIndeterminate(true);
				        downloadProgressDialog.show();
						BPStreamItem bpsi = new BPStreamItem(activity, mHandler, entryMap);
						bpsi.delete();		            
					}

		        })
		        .setNegativeButton(android.R.string.no, null)
		        .show();	

				return true;
			default:
				break;
		}
		
		return super.onContextItemSelected(item);
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
	
	public static int getStreamMax() {
		return 20;
	}
	
	public static String getStreamScope() {
		return prefs.getString("stream_scope", "sitewide");
	}
	
	private ProgressDialog downloadProgressDialog;

	public void refreshStream() {
		
        downloadProgressDialog = new ProgressDialog(activity);
        downloadProgressDialog.setCancelable(true);
        downloadProgressDialog.setMessage(getString(R.string.updating));
        downloadProgressDialog.setIndeterminate(true);
        downloadProgressDialog.show();
		
		BPStream stream = new BPStream(this, mHandler, getStreamScope(), getStreamMax());
		stream.get();
	}
	
	public static int MSG_ERROR = 0;
	public static int MSG_STREAM = 1;
	public static int MSG_DELETE = 2;
	
	/** Handler for the message from the timer service */
	private Handler mHandler = new Handler() {
		

		@Override
        public void handleMessage(Message msg) {
			String toast = null;
			boolean shouldRefresh = false;

			if(msg.what == MSG_STREAM ) {
				
				Log.i(TAG ,"got message");
				
				HashMap<?, ?> rss = (HashMap<?, ?>) msg.obj;
				Object obj = rss.get("activities");
				
				Object[] list = (Object[]) obj;
				
				adapter = new RssListAdapter(activity,list);
				if (adapter.isEmpty())
					Toast.makeText(activity, activity.getString(R.string.checkSetupInternet),
							Toast.LENGTH_LONG).show();
				setListAdapter(adapter);
				toast = getString(msg.arg1);
			}
			else if(msg.what == MSG_DELETE ) {
				toast = getString(msg.arg1);
				shouldRefresh = true;
			}
			else {
				toast = (String) msg.obj;
			}
			Toast.makeText(activity, (CharSequence) toast,
					Toast.LENGTH_LONG).show();
			if(downloadProgressDialog.isShowing())
				downloadProgressDialog.dismiss();
			
			if(shouldRefresh)
				refreshStream();
		}
    };
    
}
