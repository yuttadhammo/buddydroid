
package org.yuttadhammo.buddydroid;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.yuttadhammo.buddydroid.interfaces.BPRequest;
import org.yuttadhammo.buddydroid.interfaces.RssListAdapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Buddypress extends ListActivity {


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
		return scope;
	}
	

	protected String TAG = "Buddypress";

	public static String versionName = "1";
	private static SharedPreferences prefs;
	private TextView textContent;
	private Button submitButton;
	private Buddypress activity;
	private RssListAdapter adapter;
	private LinearLayout listPane;
	private String website;

	private ListView listView;

	private TextView nf;

	private SlidingDrawer submitDrawer;

	private RelativeLayout submitPane;

	private Button submitDrawerButton;

	protected static String scope;

	private static Spinner filters;
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

		submitDrawer = (SlidingDrawer)findViewById(R.id.drawer);
		submitPane = (RelativeLayout)findViewById(R.id.submit_pane);
		submitButton = (Button)findViewById(R.id.submit);
		submitDrawerButton = (Button)findViewById(R.id.submit_drawer);

		submitButton.setOnClickListener(mSubmitListener);
		submitDrawerButton.setOnClickListener(mSubmitListener);
		       
		textContent = (TextView) findViewById(R.id.text_content);
		
		Intent intent = this.getIntent();
		
    	if(intent.hasExtra(Intent.EXTRA_TEXT)) {
    		Log.i("Buddypress","Got text: "+intent.getStringExtra(Intent.EXTRA_TEXT));
			textContent.setText(textContent.getText()+intent.getStringExtra(Intent.EXTRA_TEXT));
    	}
    	
    	listPane = (LinearLayout) findViewById(R.id.list_pane);
		
    	listView = (ListView)findViewById(android.R.id.list);
    	
    	filters = (Spinner) findViewById(R.id.filters);
    	filters.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if (scope != null && !filters.getSelectedItem().toString().equals(scope)) {
					scope = filters.getSelectedItem().toString().replace(" ","_");
					refreshStream();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
    		
    	});

		TextView vs = (TextView) findViewById(R.id.visit_site);
		vs.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				redirectTo("stream");
			}

		});

		nf = (TextView) findViewById(R.id.notifications);
    	
    	registerForContextMenu(listView);
    	
    	this.activity = this;
    	
    	website = prefs.getString("website", "");
    	
    	if(website.length() > 0 && prefs.getBoolean("auto_update", true))
    			refreshStream();
	}
	
	@Override
	public void onResume(){
		super.onResume();

    	this.activity = this;
    	String newWebsite = prefs.getString("website", "");
    	adjustLayout();

    	if(newWebsite.length() > 0 && !website.equals(newWebsite)) {
    		website = newWebsite;
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
	    inflater.inflate(R.menu.menu_main, menu);
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
				refreshStream();
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
			case R.id.comment:
				final EditText input = new EditText(this);
				new AlertDialog.Builder(this)
			    .setTitle(R.string.comment)
			    .setView(input)
			    .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
						input.clearFocus();
						HashMap<String, Object> data = new HashMap<String, Object>();
						data.put("activity_id", entryMap.get("activity_id").toString());
						data.put("comment", input.getText().toString());
						BPRequest bpc = new BPRequest(activity, mHandler, "bp.postComment", data, MSG_COMMENT);
						bpc.execute();
						showDialog(DIALOG_COMMENTING);
			        }
			    }).setNegativeButton(android.R.string.no, null).show();	
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
						HashMap<String, Object> data = new HashMap<String, Object>();
						data.put("activity_id", entryMap.get("activity_id").toString());
						BPRequest bpc = new BPRequest(activity, mHandler, "bp.deleteProfileStatus", data, MSG_DELETE);
						bpc.execute();
						showDialog(DIALOG_DELETING);
						
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

        adjustLayout();
    }


	private final int DIALOG_NOTIFY = 0;
	private final int DIALOG_REFRESH = 1;
	private final int DIALOG_COMMENTING = 2;
	private final int DIALOG_STATUS = 3;
	private final int DIALOG_DELETING = 4;

	@Override
	protected Dialog onCreateDialog(int id) {
	    super.onCreateDialog (id);
	    final Activity activity = this;
		AlertDialog alertDialog;
    	final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	downloadProgressDialog = new ProgressDialog(activity);
        downloadProgressDialog.setCancelable(true);
        downloadProgressDialog.setIndeterminate(true);

		switch(id) {
		    case DIALOG_NOTIFY:
		    	 builder.setTitle(R.string.notify_prompt);
		    	 builder.setAdapter(new ArrayAdapter<CharSequence>(this,android.R.layout.simple_list_item_1, notificationStrings), 
		    			 new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Uri url = Uri.parse(notificationLinks.get(which));
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setData(url);
						activity.startActivity(i);
					}
		    	 });
		    	 
			    return builder.create();

		    case DIALOG_REFRESH:
		        downloadProgressDialog.setMessage(getString(R.string.updating));
		        return downloadProgressDialog;
		    case DIALOG_COMMENTING:
		        downloadProgressDialog.setMessage(getString(R.string.commenting));
		        return downloadProgressDialog;
		    case DIALOG_STATUS:
		        downloadProgressDialog.setMessage(getString(R.string.posting));
		        return downloadProgressDialog;
		    case DIALOG_DELETING:
		        downloadProgressDialog.setMessage(activity.getString(R.string.deleting));
		        return downloadProgressDialog;
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, final Dialog dialog) {
	    super.onPrepareDialog (id, dialog);
	    final Activity activity = this;
	    switch(id) {
    		case DIALOG_NOTIFY:
    			ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this,android.R.layout.simple_list_item_1, notificationStrings);
                AlertDialog ad = (AlertDialog) dialog;
                ad.getListView().setAdapter(adapter);
                break;
	    }
	}
	private void redirectTo(String string) {
		if(prefs.getString("website", "").length() == 0)
			return;
		Uri url = Uri.parse(prefs.getString("website", "")+"index.php?bp_xmlrpc=true&bp_xmlrpc_redirect="+string);
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(url);
		activity.startActivity(i);
	}
 
	
	private ProgressDialog downloadProgressDialog;

	protected ArrayList<CharSequence> notificationStrings;

	protected ArrayList<String> notificationLinks;

	public void refreshStream() {
    	downloadProgressDialog = new ProgressDialog(activity);
        downloadProgressDialog.setCancelable(true);
        downloadProgressDialog.setIndeterminate(true);
        
		if(scope == null)
			scope = filters.getSelectedItem().toString().replace(" ","_");

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("scope", scope);
		data.put("user_data", "true");
		data.put("max", getStreamMax());
		
		BPRequest stream = new BPRequest(this, mHandler, "bp.getActivity", data, MSG_STREAM);
		stream.execute();
		
		showDialog(DIALOG_REFRESH);
	}
	
	public static final int MSG_ERROR = 0;
	public static final int MSG_STREAM = 1;
	public static final int MSG_DELETE = 2;
	public static final int MSG_COMMENT = 3;
	public static final int MSG_STATUS = 4;
	
	/** Handler for the message from the timer service */
	private Handler mHandler = new Handler() {
		

		@Override
        public void handleMessage(Message msg) {
			Log.i(TAG ,"got message");
			removeDialog(DIALOG_REFRESH);
			removeDialog(DIALOG_COMMENTING);
			removeDialog(DIALOG_STATUS);
			removeDialog(DIALOG_DELETING);


			String toast = null;
			boolean shouldRefresh = false;
			switch(msg.what) {
				case MSG_STREAM:
					
					HashMap<?, ?> map = (HashMap<?, ?>) msg.obj;
					Object obj = map.get("activities");
					
					Object[] list = (Object[]) obj;
					
					adapter = new RssListAdapter(activity,list);
					setListAdapter(adapter);
					toast = getString(R.string.updated);
					
					if(map.containsKey("user_data")){
						Log.i(TAG ,"got user data");
						Map<?,?> user = (HashMap<?, ?>) map.get("user_data");
						notificationStrings = new ArrayList<CharSequence>();
						Object nfoo = user.get("notifications");
						if(nfoo instanceof Object[] && !(((Object[])nfoo)[0] instanceof Boolean)) {
							Object[] nfo = (Object[]) user.get("notifications");
							String nfs = Integer.toString(nfo.length);
							nf.setBackgroundColor(0xFF00DD00);
							nf.setText(nfs);
							notificationLinks = new ArrayList<String>();
							for(Object anf : nfo){
								notificationLinks.add(((String) anf).replaceFirst("^<a href=\"([^\"]*)\".*", "$1"));
								notificationStrings.add(Html.fromHtml((String) anf).toString());
							}
							nf.setOnClickListener(new OnClickListener(){

								@Override
								public void onClick(View v) {
									showDialog(DIALOG_NOTIFY);
								}
								
							});
						}
						else {
							nf.setBackgroundColor(0xFF555555);
							nf.setText(R.string.zero);
							nf.setOnClickListener(null);
						}
						
					}
					
					break;
				case MSG_STATUS:
					textContent.setText("");
					toast = activity.getString(R.string.posted);
					shouldRefresh = true;
					break;
				case MSG_DELETE:
					toast = getString(R.string.deleted);
					shouldRefresh = true;
					break;
				case MSG_COMMENT:
					toast = getString(R.string.commented);
					shouldRefresh = true;
					break;
				default:
					toast = (String) msg.obj;
			}
			Toast.makeText(activity, (CharSequence) toast,
					Toast.LENGTH_LONG).show();
			
			if(shouldRefresh)
				refreshStream();
		}
    };


	private void adjustLayout() {
		DisplayMetrics metrics = new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(metrics);
    	int width = metrics.widthPixels; 	
    	boolean land = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && width > 600;

    	if(land) {
    		submitPane.setVisibility(View.VISIBLE);
    		submitDrawer.setVisibility(View.GONE);
    		textContent = (TextView) findViewById(R.id.text_content);
    	}
    	else {
    		submitPane.setVisibility(View.GONE);
    		submitDrawer.setVisibility(View.VISIBLE);
    		textContent = (TextView) findViewById(R.id.text_drawer);
    	}
	}

	
	private OnClickListener mSubmitListener = new OnClickListener() {

		@SuppressWarnings("deprecation")
		public void onClick(View v)
		{
			submitDrawer.close();
			String text = textContent.getText().toString();
			if(text.length() == 0)
				return;

			if(getApiKey().length() < 1) {
				Toast.makeText(Buddypress.this, "Please set up your account first...",
						Toast.LENGTH_SHORT).show();
				return;
			}
				
			HashMap<String, Object> data = new HashMap<String, Object>();
			data.put("status", text);
			BPRequest bpc = new BPRequest(activity, mHandler, "bp.updateProfileStatus", data, MSG_STATUS);
			bpc.execute();
			showDialog(DIALOG_STATUS);
		}
	};

	
}
