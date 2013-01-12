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


public class BPSettingsActivity extends PreferenceActivity {
	
	private Context context;
	private BPSettingsActivity activity;
	private SharedPreferences prefs;
	private Preference apiPref;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		this.context = getApplicationContext();
		this.activity = this;
		addPreferencesFromResource(R.xml.preferences);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		final EditTextPreference websitePref = (EditTextPreference)findPreference("website");
		if(websitePref.getText() == null || websitePref.getText().equals(""))
			websitePref.setText("");
		websitePref.setSummary(websitePref.getText());
		
		websitePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference preference,
					final Object newValue) {
				websitePref.setSummary((String) newValue);

				return true;
			}
			
		});

		final EditTextPreference userPref = (EditTextPreference)findPreference("username");
		if(userPref.getText() == null || userPref.getText().equals(""))
			userPref.setText("");
		userPref.setSummary(userPref.getText());
		
		userPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference preference,
					final Object newValue) {
				userPref.setSummary((String) newValue);

				return true;
			}
			
		});
		
		final Preference regPref = (Preference)findPreference("register");
		regPref.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference arg0) {
		        downloadProgressDialog = new ProgressDialog(activity);
		        downloadProgressDialog.setCancelable(true);
		        downloadProgressDialog.setMessage(getString(R.string.registering));
		        downloadProgressDialog.setIndeterminate(true);
		        
				ReadFile rf = new ReadFile();
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
		
		final EditTextPreference servicePref = (EditTextPreference)findPreference("service_name");
		if(servicePref.getText() == null || servicePref.getText().equals(""))
			servicePref.setText(getString(R.string.app_name));
		
		final EditTextPreference memberPref = (EditTextPreference)findPreference("member_slug");
		if(memberPref.getText() == null || memberPref.getText().equals(""))
			memberPref.setText("members");
		
		
		@SuppressWarnings("deprecation")
		int api = Integer.parseInt(Build.VERSION.SDK);	
		
		if (api >= 14) {
			getActionBar().setHomeButtonEnabled(true);
		}		
	}
    private ProgressDialog downloadProgressDialog;

    private class ReadFile extends AsyncTask<URI, Integer, String> {
		private Editor editor;
		private String apikey;

		@Override
        protected String doInBackground(URI... sUrl) {
            boolean success = false;
            HashMap<?, ?> result = null;
			
			Object[] params = new Object[] {Buddypress.getUsername(), Buddypress.getServiceName()};
			XMLRPCClient client = new XMLRPCClient(Buddypress.getUrl(),
					Buddypress.getHttpuser(), Buddypress.getHttppassword());
			try {
				result = (HashMap<?, ?>) client.call("bp.requestApiKey", params);
				success = true;
			} catch (final XMLRPCException e) {
				e.printStackTrace();
			}
			if(success) {
				String confirm = result.get("confirmation").toString();
				if(confirm.equals("true")) {
					apikey = result.get("apikey").toString();
					editor = prefs.edit();
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
				String website = prefs.getString("website", null);
				String username = prefs.getString("username", null);
				if(website != null && username != null) {
					Uri url = Uri.parse(website+prefs.getString("member_slug", "members")+"/"+username+"/settings/remote-access/?time="+(new Date().getTime()));
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(url);
					activity.startActivity(i);
				}
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

}
