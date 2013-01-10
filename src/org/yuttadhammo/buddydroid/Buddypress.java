
package org.yuttadhammo.buddydroid;

import java.net.URI;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;



/**
 * The main activity which shows the timer and allows the user to set the time
 * @author Ralph Gootee (rgootee@gmail.com)
 */
public class Buddypress extends Activity {
	

	public static String versionName = "1";
	private static SharedPreferences prefs;
	private TextView textContent;
	private Button submitButton;


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
			textContent.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
    	}
	}
	
	@Override
	public void onResume(){
		super.onResume();
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
		return URI.create(prefs.getString("website", "")+"wp-content/plugins/bpxmlrpc/bp-xmlrpc.php");
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
	
}
