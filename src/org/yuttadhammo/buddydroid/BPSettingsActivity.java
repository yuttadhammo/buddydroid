package org.yuttadhammo.buddydroid;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;
import org.yuttadhammo.buddydroid.interfaces.BPRequest;
import org.yuttadhammo.buddydroid.interfaces.StreamListAdapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Toast;


public class BPSettingsActivity extends PreferenceActivity {
	
	private Context context;
	private BPSettingsActivity activity;
	private static SharedPreferences prefs;
	private Preference apiPref;
	private Preference profilePref;
	public final static int MSG_API = 4567;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		this.context = getApplicationContext();
		this.activity = this;
		addPreferencesFromResource(R.xml.preferences);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		final Preference regPref = (Preference)findPreference("register");
		regPref.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference arg0) {
		        downloadProgressDialog = new ProgressDialog(activity);
		        downloadProgressDialog.setCancelable(true);
		        downloadProgressDialog.setMessage(getString(R.string.registering));
		        downloadProgressDialog.setIndeterminate(true);
		        showDialog(DIALOG_API);
		        
				HashMap<String, Object> data = new HashMap<String, Object>();
				BPRequest bpr = new BPRequest(context, mHandler, "bp.requestApiKey", data, MSG_API );
				bpr.execute();
				return false;
			}
			
		});
		
		apiPref = (Preference)findPreference("api_key");
		String api_key = prefs.getString("api_key", "");
		if(api_key.length() > 0)
			apiPref.setSummary(api_key);
		
		final Preference helpPref = (Preference)findPreference("help");
		helpPref.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				Intent intent = new Intent(context, BPHelpActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);

				return false;
			}
			
		});

		final EditTextPreference websitePref = (EditTextPreference)findPreference("website");
		final EditTextPreference userPref = (EditTextPreference)findPreference("username");
		final EditTextPreference apiPref = (EditTextPreference)findPreference("api_key");

		final EditTextPreference streamPref = (EditTextPreference)findPreference("stream_max");
		final EditTextPreference contentPref = (EditTextPreference)findPreference("content_max");
		
		final EditTextPreference intervalPref = (EditTextPreference)findPreference("sync_interval");
		final EditTextPreference servicePref = (EditTextPreference)findPreference("service_name");
		
		setupEditTextPreference(websitePref, getWebsite(), null);
		setupEditTextPreference(userPref,"", null);
		setupEditTextPreference(apiPref,null, getString(R.string.api_key_desc));

		setupEditTextPreference(streamPref,"20", null);
		setupEditTextPreference(contentPref,null, getString(R.string.contentMaxDesc));

		setupEditTextPreference(intervalPref,"60", null);
		setupEditTextPreference(servicePref,getString(R.string.app_name), null);

		intervalPref.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		streamPref.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		contentPref.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		websitePref.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_URI);
		
		profilePref = (Preference)findPreference("profile_url");

		profilePref.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference preference) {

				String website = getWebsite();
				String username = prefs.getString("username", null);
				String apikey = prefs.getString("api_key", null);
				
				if(website != null && username != null && apikey != null) {
					
					Uri url = Uri.parse(website+"index.php?bp_xmlrpc=true&bp_xmlrpc_redirect=remote_settings");
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(url);
					activity.startActivity(i);
				}
				return false;
			}
		});

		final CheckBoxPreference syncPref = (CheckBoxPreference)findPreference("interval_sync");

		if(prefs.getBoolean("interval_sync", false))
			intervalPref.setEnabled(true);
		else
			intervalPref.setEnabled(false);

		syncPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				if(!syncPref.isChecked())
					intervalPref.setEnabled(true);
				else
					intervalPref.setEnabled(false);
					
				return true;
			}
			
		});
		
		int api = Build.VERSION.SDK_INT;	
		
		if (api >= 14) {
			getActionBar().setHomeButtonEnabled(true);
		}
		
		if(Buddypress.CUSTOM_WEBSITE != null)
			websitePref.setEnabled(false);
		
	}
    private ProgressDialog downloadProgressDialog;
	private final int DIALOG_API = 0;
	protected String TAG = "BPSettingsActivity";


	@Override
	protected Dialog onCreateDialog(int id) {
	    super.onCreateDialog (id);
	    final Activity activity = this;
    	downloadProgressDialog = new ProgressDialog(activity);
        downloadProgressDialog.setCancelable(true);
        downloadProgressDialog.setIndeterminate(true);

		switch(id) {
		    case DIALOG_API :
		        downloadProgressDialog.setMessage(getString(R.string.registering));
		        return downloadProgressDialog;
		}
		return null;
	}

	/** Handler for the message from the timer service */
	private Handler mHandler = new Handler() {
		

		@Override
        public void handleMessage(Message msg) {
			Log.i(TAG  ,"got message");
			removeDialog(DIALOG_API);


			String toast = null;
			switch(msg.what) {
				case MSG_API:
					String apikey;
					
					HashMap<?, ?> result = (HashMap<?, ?>) msg.obj;
					if(result.containsKey("apikey")) {
						apikey = result.get("apikey").toString();
						SharedPreferences.Editor editor = prefs.edit();
						editor.putString("api_key", apikey);
						editor.commit();
						apiPref.setSummary(apikey);
						toast = getString(R.string.registered);
					}
					break;
				default:
					if(msg.obj instanceof String)
						toast = (String) msg.obj;
					else
						toast = getString(R.string.error);
					break;
			}
			Toast.makeText(activity, (CharSequence) toast,
					Toast.LENGTH_LONG).show();
		}
    };

	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		super.onOptionsItemSelected(item);
		
		switch (item.getItemId()) {
	        case android.R.id.home:
	            // app icon in action bar clicked; go home
	            finish();
	            return true;

			default:
				return false;
	    }
	}	
	
	public void setupEditTextPreference(final EditTextPreference etp, final String def, final String desc) {
		if(def != null) {
			if(etp.getText() == null || etp.getText().equals(""))
				etp.setText(def);
			etp.setSummary(etp.getText());
		}
		else if(etp.getText() != null && !etp.getText().equals("")) {
			etp.setSummary(etp.getText());
		}
		
		
		etp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference preference,
					final Object newValue) {
				if((newValue == null || ((String)newValue).length() == 0) && desc != null)
					etp.setSummary(desc);
				else
					etp.setSummary((String) newValue);

				return true;
			}
			
		});			
	}
	private static String getWebsite() {
		String website = Buddypress.CUSTOM_WEBSITE  != null ? Buddypress.CUSTOM_WEBSITE : prefs.getString("website", null);
		if(website.length() == 0)
			website = null;
		return website;
	}	
}
