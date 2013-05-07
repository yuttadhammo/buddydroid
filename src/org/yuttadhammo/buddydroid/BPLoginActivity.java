package org.yuttadhammo.buddydroid;

import java.net.URI;

import org.yuttadhammo.buddydroid.interfaces.BPWebsite;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class BPLoginActivity extends Activity {

	private SharedPreferences prefs;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		final Activity activity = this;
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		final EditText website = (EditText) findViewById(R.id.website);
		final EditText username = (EditText) findViewById(R.id.username);
		final EditText password = (EditText) findViewById(R.id.password);
		
		
		if(BPWebsite.CUSTOM_WEBSITE != null)
			website.setEnabled(false);
		
		website.setText(BPWebsite.getWebsite(this));
		username.setText(prefs.getString("username", ""));
		password.setText(prefs.getString("password", ""));
		
		Button cancel = (Button) findViewById(R.id.cancel);
		Button login = (Button) findViewById(R.id.login);
		Button register = (Button) findViewById(R.id.register);
		register.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				String link = website.getText().toString();
				if(link.equals("") || link.contains(" ")) {
					Toast.makeText(activity, R.string.missing_website, Toast.LENGTH_LONG).show();
					return;
				}
				
				link = BPWebsite.sanitizeWebsite(link);
				
				website.setText(link);
				
				link = link+"index.php?bp_xmlrpc=true&bp_xmlrpc_redirect=register";

				if(!BPWebsite.isValidWebsite(link)) {
					Toast.makeText(activity, R.string.missing_website, Toast.LENGTH_LONG).show();
					return;
				}
				
				Uri url = Uri.parse(link);
				Intent i = new Intent(Intent.ACTION_VIEW, url);
				activity.startActivity(i);
			}
		});


		cancel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager)getSystemService(
					      Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				finish();
			}
		});
		login.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				
				InputMethodManager imm = (InputMethodManager)getSystemService(
					      Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
					
				String website_string = website.getText().toString();
				if(website_string.equals("") || username.getText().toString().equals("") || password.getText().toString().equals("")) {
					Toast.makeText(activity, R.string.missing_value,
							Toast.LENGTH_LONG).show();
					return;
				}
				
				if(!BPWebsite.isValidWebsite(website_string)) {
					Toast.makeText(activity, R.string.missing_website, Toast.LENGTH_LONG).show();
					return;
				}
				
				Intent i = new Intent();
				i.putExtra("website", website.getText().toString());
				i.putExtra("username", username.getText().toString());
				i.putExtra("password", password.getText().toString());
				setResult(Activity.RESULT_OK, i);
				finish();
			}
		});
		
	}
	

	
}
