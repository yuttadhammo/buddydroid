
package org.yuttadhammo.buddydroid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.yuttadhammo.buddydroid.interfaces.BPRequest;
import org.yuttadhammo.buddydroid.interfaces.NoticeService;
import org.yuttadhammo.buddydroid.interfaces.RssListAdapter;

import com.actionbarsherlock.app.SherlockListActivity;
import android.app.NotificationManager;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Buddypress extends SherlockListActivity {
	
	// set this if you are hardcoding a website into your app
	public final static String CUSTOM_WEBSITE = null;
	
	protected String TAG = "Buddypress";

	public static String versionName = "1";
	private static SharedPreferences prefs;
	private EditText activeEditText;
	private Button submitButton;
	private Buddypress activity;
	private RssListAdapter adapter;
	private String website;

	private ListView listView;

	private TextView nf;

	private SlidingDrawer submitDrawer;

	private RelativeLayout submitPane;

	private Button submitDrawerButton;

	protected static String scope;

	private static Spinner filters;

	public static int NOTIFY_ID = 0;

	private AlarmManager mgr=null;
	private PendingIntent pi=null;

	private EditText textContent;

	private EditText textDrawer;

	private Intent intent;

	private LinearLayout filterPane;

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
		       
		textContent = (EditText) findViewById(R.id.text_content);
		textDrawer =  (EditText) findViewById(R.id.text_drawer);
		
		
		intent = this.getIntent();
		
    	if(intent.hasExtra(Intent.EXTRA_TEXT)) {
    		Log.i("Buddypress","Got text: "+intent.getStringExtra(Intent.EXTRA_TEXT));
    		textContent.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
    		intent.removeExtra(Intent.EXTRA_TEXT);
    	}
    	
    	filterPane = (LinearLayout)findViewById(R.id.filter_pane);
    	
    	LinearLayout header = (LinearLayout) getLayoutInflater().inflate(R.layout.list_header, null);
    	LinearLayout footer = (LinearLayout) getLayoutInflater().inflate(R.layout.list_footer, null);
    	listView = (ListView)findViewById(android.R.id.list);
    	listView.addHeaderView(header);
    	listView.addFooterView(footer);
    	listView.setOnScrollListener(new OnScrollListener(){

        	int fv = 0;
        	@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if(firstVisibleItem > fv)
					doSlideUp(filterPane);
				else if(firstVisibleItem < fv || firstVisibleItem == 0)
					doSlideDown(filterPane);
				fv = firstVisibleItem;
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				
			}
		});
    	
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

		nf = (TextView) findViewById(R.id.notifications);
    	
    	registerForContextMenu(listView);
    	
    	activity = this;
    	website = getWebsite();

    	adjustLayout();
    	
    	if(prefs.getBoolean("auto_update", true) && !getIntent().hasExtra("notification"))
   			refreshStream();
    	
	}
	
	@Override
	public void onResume(){
		super.onResume();


    	// set up notification
		((NotificationManager)getSystemService(NOTIFICATION_SERVICE))
		.cancelAll();
		
		mgr=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
		
		Intent i=new Intent(this, NoticeService.class);
		
		pi=PendingIntent.getService(this, 0, i, 0);
		
		cancelAlarm(null);

		// start cancelling receiver

		IntentFilter filter=new IntentFilter(NoticeService.BROADCAST);
		
		filter.setPriority(2);
		registerReceiver(onNotice, filter);
		
    	this.activity = this;
    	String newWebsite = getWebsite();
    	adjustLayout();

    	if(intent.hasExtra(Intent.EXTRA_TEXT)) {
    		Log.i("Buddypress","Got text: "+intent.getStringExtra(Intent.EXTRA_TEXT));
			activeEditText.setText(activeEditText.getText()+intent.getStringExtra(Intent.EXTRA_TEXT));
    		intent.removeExtra(Intent.EXTRA_TEXT);
    	}
    	
    	if(prefs.getBoolean("interval_sync", false)) {
    		Long interval = Long.parseLong(prefs.getString("sync_interval", "60"))*60*1000;
			Log.i(TAG,interval+"");
    		mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime()+interval,
				interval,
				pi);
    	}
    	
    	// if website changed
    	
    	if(website != null && !website.equals(newWebsite)) {
    		website = newWebsite;
    		if(prefs.getBoolean("auto_update", true))
    			refreshStream();
    	}

    	if(getIntent().hasExtra("notification"))
			refreshStream();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(onNotice);
	}
	
	@Override
	protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
    	if(intent.hasExtra(Intent.EXTRA_TEXT)) {
    		String text = activeEditText.getText().toString();
    		String add = "";
    		if(text.length() > 0)
    			add = "\n";
			activeEditText.setText(text+add+intent.getStringExtra(Intent.EXTRA_TEXT));
    	}
    		
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getSupportMenuInflater();
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
		    	if(getWebsite() == null) {
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
		android.view.MenuInflater inflater = getMenuInflater();
       	inflater.inflate(R.menu.stream_longclick, menu);
        
	    menu.setHeaderTitle(getString(R.string.post_options));
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
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
	    final Buddypress activity = this;
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
						startActivity(i);
						removeDialog(DIALOG_NOTIFY);
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
	    switch(id) {
    		case DIALOG_NOTIFY:
    			ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this,android.R.layout.simple_list_item_1, notificationStrings);
                AlertDialog ad = (AlertDialog) dialog;
                ad.getListView().setAdapter(adapter);
                break;
	    }
	}
	private void redirectTo(String string) {
		String site = getWebsite();
		if(site == null)
			return;
		Uri url = Uri.parse(site+"index.php?bp_xmlrpc=true&bp_xmlrpc_redirect="+string);
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(url);
		activity.startActivity(i);
	}
 
	
	private ProgressDialog downloadProgressDialog;

	protected ArrayList<CharSequence> notificationStrings;

	protected ArrayList<String> notificationLinks;

	public void refreshStream() {
		
		if(getWebsite() == null || prefs.getString("username", null) == null || prefs.getString("api_key", null) == null)
			return;
		
		Log.i(TAG ,"refreshing stream");
		
    	downloadProgressDialog = new ProgressDialog(activity);
        downloadProgressDialog.setCancelable(true);
        downloadProgressDialog.setIndeterminate(true);
        
		if(scope == null)
			scope = filters.getSelectedItem().toString().replace(" ","_");

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("scope", scope);
		data.put("user_data", "true");
		data.put("max", Integer.parseInt(prefs.getString("stream_max", "20")));
		
		BPRequest stream = new BPRequest(activity, mHandler, "bp.getActivity", data, MSG_STREAM);
		stream.execute();
		
		showDialog(DIALOG_REFRESH);
	}
	
	public static final int MSG_STREAM = 1;
	public static final int MSG_DELETE = 2;
	public static final int MSG_COMMENT = 3;
	public static final int MSG_STATUS = 4;
	public static final int MSG_SYNC = 5;
	
	/** Handler for the message from the timer service */
	private Handler mHandler = new Handler() {
		

		@Override
        public void handleMessage(Message msg) {
			Log.i(TAG ,"got message");
			removeDialog(DIALOG_REFRESH);
			removeDialog(DIALOG_COMMENTING);
			removeDialog(DIALOG_STATUS);
			removeDialog(DIALOG_DELETING);

			HashMap<?, ?> map;
			Object obj;
			
			String toast = null;
			boolean shouldRefresh = false;
			
			boolean fromNotify = getIntent().hasExtra("notification");
			if(fromNotify)
				getIntent().removeExtra("notification");
			
			switch(msg.what) {
				case MSG_STREAM:
					if(!(msg.obj instanceof HashMap)) 
						break;
					
					map = (HashMap<?, ?>) msg.obj;
					obj = map.get("activities");
					
					Object[] list = (Object[]) obj;
					
					adapter = new RssListAdapter(activity,list);
					setListAdapter(adapter);
					toast = getString(R.string.updated);
					
					if(map.containsKey("user_data") && map.get("user_data") instanceof HashMap){
						Log.i(TAG ,"got user data");
						Map<?,?> user = (HashMap<?, ?>) map.get("user_data");
						Object nfoo = user.get("notifications");
						processNotifications(nfoo);
					}

					if(fromNotify)
						showDialog(DIALOG_NOTIFY);

					break;
				case MSG_SYNC:
					if(!(msg.obj instanceof HashMap)) 
						break;
						
					map = (HashMap<?, ?>) msg.obj;
					obj = map.get("message");
					
					processNotifications(obj);
					return;
				case MSG_STATUS:
					activeEditText.setText("");
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
					if(msg.obj instanceof String)
						toast = (String) msg.obj;
					else
						toast = getString(R.string.error);
					break;
			}
			Toast.makeText(activity, (CharSequence) toast,
					Toast.LENGTH_LONG).show();
			
			if(shouldRefresh)
				refreshStream();
		}
    };

	private boolean isLandscape = true;


	private void adjustLayout() {
		DisplayMetrics metrics = new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(metrics);
    	int width = metrics.widthPixels; 	
    	boolean land = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && width > 600;

    	if(land) {
    		if(land != isLandscape)
    			textContent.setText(textDrawer.getText());
    		submitPane.setVisibility(View.VISIBLE);
    		submitDrawer.setVisibility(View.GONE);
    		activeEditText = textContent;
    	}
    	else {
    		if(land != isLandscape)
    			textDrawer.setText(textContent.getText());
    		submitPane.setVisibility(View.GONE);
    		submitDrawer.setVisibility(View.VISIBLE);
    		activeEditText = textDrawer;
    	}
    	isLandscape  = land;
	}

	
	protected void processNotifications(Object nfoo) {
		notificationStrings = new ArrayList<CharSequence>();
		notificationLinks = new ArrayList<String>();
		if(nfoo instanceof Object[] && !(((Object[])nfoo)[0] instanceof Boolean)) {
			Object[] nfo = (Object[]) nfoo;
			String nfs = Integer.toString(nfo.length);
			nf.setBackgroundColor(0xFF00DD00);
			nf.setText(nfs);
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


	private OnClickListener mSubmitListener = new OnClickListener() {

		@SuppressWarnings("deprecation")
		public void onClick(View v)
		{
			submitDrawer.close();
			String text = activeEditText.getText().toString();
			if(text.length() == 0)
				return;

			if(prefs.getString("api_key", null) == null) {
				Toast.makeText(Buddypress.this, R.string.error,
						Toast.LENGTH_LONG).show();
				return;
			}
				
			HashMap<String, Object> data = new HashMap<String, Object>();
			data.put("status", text);
			BPRequest bpc = new BPRequest(activity, mHandler, "bp.updateProfileStatus", data, MSG_STATUS);
			bpc.execute();
			showDialog(DIALOG_STATUS);
		}
	};
	
	public void cancelAlarm(View v) {
		mgr.cancel(pi);
	}

	private BroadcastReceiver onNotice = new BroadcastReceiver() {
		public void onReceive(Context ctxt, Intent i) {
			Log.i(TAG, "notification sync");
			HashMap<String, Object> data = new HashMap<String, Object>();
	    	BPRequest bpr = new BPRequest(activity, mHandler, "bp.getNotifications", data, MSG_SYNC);
	    	bpr.execute();
	    	
			abortBroadcast();
		}
	};


	private static String getWebsite() {
		String website = Buddypress.CUSTOM_WEBSITE  != null ? Buddypress.CUSTOM_WEBSITE : prefs.getString("website", null);
		if(website.length() == 0)
			website = null;
		return website;
	}

	public void doSlideDown(View view){
		if(view.getVisibility() == View.VISIBLE || view.getAnimation() != null)
			return;
		view.setVisibility(View.VISIBLE);
		Animation slideDown = setLayoutAnim_slidedown(); 
		view.startAnimation(slideDown);
	}

	public void doSlideUp(View view){
		if(view.getVisibility() == View.GONE || view.getAnimation() != null)
			return;

		Animation slideUp = setLayoutAnim_slideup(view); 
		view.startAnimation(slideUp);
	}

	public Animation setLayoutAnim_slidedown() {

	        AnimationSet set = new AnimationSet(true);

	        Animation animation = new TranslateAnimation(
	                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
	                0.0f, Animation.RELATIVE_TO_SELF, -1.0f,
	                Animation.RELATIVE_TO_SELF, 0.0f);
	        animation.setDuration(200);
	        animation.setAnimationListener(new AnimationListener() {

	            @Override
	            public void onAnimationStart(Animation animation) {
	                // TODO Auto-generated method stub
	                // MapContacts.this.mapviewgroup.setVisibility(View.VISIBLE);

	            }

	            @Override
	            public void onAnimationRepeat(Animation animation) {
	                // TODO Auto-generated method stub

	            }

	            @Override
	            public void onAnimationEnd(Animation animation) {
	                // TODO Auto-generated method stub
	                Log.d("LA","sliding down ended");

	            }
	        });
	        set.addAnimation(animation);

	        return animation;
	    }

	public Animation setLayoutAnim_slideup(final View view) {

	        AnimationSet set = new AnimationSet(true);

	        Animation animation = new TranslateAnimation(
	                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
	                0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
	                Animation.RELATIVE_TO_SELF, -1.0f);
	        animation.setDuration(200);
	        animation.setAnimationListener(new AnimationListener() {

	            @Override
	            public void onAnimationStart(Animation animation) {
	                // TODO Auto-generated method stub

	            }

	            @Override
	            public void onAnimationRepeat(Animation animation) {
	                // TODO Auto-generated method stub

	            }

	            @Override
	            public void onAnimationEnd(Animation animation) {
	                // TODO Auto-generated method stub
	                view.clearAnimation();
	                view.setVisibility(View.GONE);
	            }
	        });
	        set.addAnimation(animation);

	        return animation;

	}


	
}
