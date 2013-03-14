package org.yuttadhammo.buddydroid;

import java.util.HashMap;
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
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

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
		
		int api = Build.VERSION.SDK_INT;
		
		if (api >= 14) {
			getActionBar().setHomeButtonEnabled(true);
		}
		
		registerForContextMenu(findViewById(android.R.id.list));
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
	
	/** Handler for the message from the timer service */
	private Handler mHandler = new Handler() {
		

		@Override
        public void handleMessage(Message msg) {
			String toast = null;
			if(msg.what == Buddypress.MSG_STREAM ) {
				
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
			else if(msg.what == Buddypress.MSG_DELETE ) {
				toast = getString(msg.arg1);
				refreshStream();
			}
			else {
				toast = (String) msg.obj;
			}
			Toast.makeText(activity, (CharSequence) toast,
					Toast.LENGTH_SHORT).show();
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
	

}
