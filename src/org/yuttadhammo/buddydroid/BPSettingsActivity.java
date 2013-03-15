package org.yuttadhammo.buddydroid;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;

import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;


public class BPSettingsActivity extends PreferenceActivity {
	
	private Context context;
	private BPSettingsActivity activity;
	private SharedPreferences prefs;
	private Preference apiPref;
	private Preference profilePref;
	
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
		        
				ApiRequest rf = new ApiRequest();
				rf.execute(Buddypress.getUrl());
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

		final EditTextPreference maxPref = (EditTextPreference)findPreference("stream_max");
		final EditTextPreference servicePref = (EditTextPreference)findPreference("service_name");

		this.setupEditTextPreference(websitePref,"");
		this.setupEditTextPreference(userPref,"");
		this.setupEditTextPreference(apiPref,null);

		this.setupEditTextPreference(maxPref,"20");
		this.setupEditTextPreference(servicePref,getString(R.string.app_name));

		maxPref.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		websitePref.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_URI);
		
		profilePref = (Preference)findPreference("profile_url");
		profilePref.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference preference) {

				String website = prefs.getString("website", null);
				String username = prefs.getString("username", null);
				String apikey = prefs.getString("api_key", null);
				String profileURL = prefs.getString("profile_url", null);
				
				if(website != null && username != null && apikey != null && profileURL != null) {
					
					Uri url = Uri.parse(profileURL+"?time="+(new Date().getTime()));
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(url);
					activity.startActivity(i);
				}
				return false;
			}
		});
		
		int api = Build.VERSION.SDK_INT;	
		
		if (api >= 14) {
			getActionBar().setHomeButtonEnabled(true);
		}		
	}
    private ProgressDialog downloadProgressDialog;

    private class ApiRequest extends AsyncTask<URI, Integer, String> {
		private Editor editor;
		private String apikey = null;
		private String error = null;
		private String profileURL;

		@Override
        protected String doInBackground(URI... sUrl) {
            boolean success = false;
            HashMap<?, ?> result = null;
			
			Object[] params = new Object[] {Buddypress.getUsername(), Buddypress.getServiceName()};
			XMLRPCClient client = new XMLRPCClient(Buddypress.getUrl(),
					Buddypress.getHttpuser(), Buddypress.getHttppassword());
			try {
				Object obj = client.call("bp.requestApiKey", params);
				if(obj.equals(false)) {
					error = activity.getString(R.string.connectionRejected);
				}
				else {
					result = (HashMap<?, ?>) obj;
					success = true;
				}
			} catch (final XMLRPCException e) {
				e.printStackTrace();
				error = e.getMessage();
			} catch (Exception e) {
				e.printStackTrace();
				error = e.getMessage();
			}
			if(success) {
				String confirm = result.get("confirmation").toString();
				if(confirm.equals("true") && result.containsKey("apikey")) {
					apikey = result.get("apikey").toString();
					editor = prefs.edit();
					if(result.containsKey("url")) {
						profileURL = result.get("url").toString();
						editor.putString("profile_url", profileURL);
					}
					editor.putString("api_key", apikey);
					editor.commit();
				}
			}
			return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            downloadProgressDialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
			if(downloadProgressDialog.isShowing()) {
				downloadProgressDialog.dismiss();
			}
			if(apikey != null) {
				apiPref.setSummary(apikey);
				profilePref.setSummary(profileURL != null?profileURL:"Please update your wordpress plugin");
			}
			else if(error != null) {
    			Toast.makeText(activity, error,
    					Toast.LENGTH_LONG).show();
			}
        }

    }
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
	
	public void setupEditTextPreference(final EditTextPreference etp, String def) {
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

				etp.setSummary((String) newValue);

				return true;
			}
			
		});			
	}
	
}
