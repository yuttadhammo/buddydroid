
package org.yuttadhammo.buddydroid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.yuttadhammo.buddydroid.interfaces.BPRequest;
import org.yuttadhammo.buddydroid.interfaces.MessageListAdapter;
import org.yuttadhammo.buddydroid.interfaces.NoticeService;
import org.yuttadhammo.buddydroid.interfaces.StreamListAdapter;

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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
	private static Buddypress activity;
	private StreamListAdapter adapter;
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

	private ImageView msgs;

	private MenuItem refreshItem;
	private boolean refreshing;

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

        	int lastScroll = 0;
        	int lvi = 0;
        	@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
        		
        		int TOLERANCE = 10;
        		
        		View fc = listView.getChildAt(0);
        		if(fc == null)
        			return;

        		int newScroll = fc.getTop();
        		
        		// small movements add up but don't count alone
        		
        		if(Math.abs(lastScroll - newScroll) < TOLERANCE)
        			return;
        		
        		// different child or reversal
        		
        		if(newScroll * lastScroll < 0 || firstVisibleItem != lvi) {
        			lvi = firstVisibleItem;
        			lastScroll = newScroll;
        		}
        		
        		if(newScroll < lastScroll || firstVisibleItem == 0) {
					doSlideDown(filterPane);
        		}
				else if(newScroll > lastScroll) {
					doSlideUp(filterPane);
				}
				lastScroll = newScroll;
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				if(scrollState == OnScrollListener.SCROLL_STATE_IDLE && listView.getFirstVisiblePosition() == 0)
					doSlideDown(filterPane);
			}
		});
    	
    	scope = "sitewide";
    	
    	filters = (Spinner) findViewById(R.id.filters);
    	filters.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				Log.d(TAG,"changed");
				if (!filters.getSelectedItem().toString().equals(scope)) {
					scope = filters.getSelectedItem().toString().replace(" ","_");
					
					if(scope.equals("messages"))
						getMessages(new HashMap<String, Object>());
					else
						refreshStream(new HashMap<String, Object>());
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
   			refreshStream(new HashMap<String, Object>());
    	
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
		
    	activity = this;
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
    			refreshStream(new HashMap<String, Object>());
    	}

    	if(getIntent().hasExtra("notification"))
			refreshStream(new HashMap<String, Object>());
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
	    refreshItem = menu.findItem(R.id.menuStream);
	    if(refreshing) {
	    	refreshing = false;
	    	showRefresh();
	    }
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
			if(scope == "messages")
				getMessages(new HashMap<String, Object>());
			else
				refreshStream(new HashMap<String, Object>());
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
       	
		if(scope.equals("messages")) {
			inflater.inflate(R.menu.message_longclick, menu);
            menu.setHeaderTitle(getString(R.string.message_options));
		}
		else {
			inflater.inflate(R.menu.stream_longclick, menu);
            menu.setHeaderTitle(getString(R.string.post_options));
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    int index = info.position;
	    final HashMap<?,?> entryMap = (HashMap<?, ?>) getListView().getItemAtPosition(index);
		Intent i;
	    
	    String link;
		Uri url;
		final EditText input;
		HashMap<String, Object> data;
		switch (item.getItemId()) {
			case R.id.view:
				link = (String)entryMap.get("primary_link");
				url = Uri.parse(link);
				i = new Intent(Intent.ACTION_VIEW, url);
				activity.startActivity(i);
				return true;
			case R.id.comment:
				input = new EditText(this);
				new AlertDialog.Builder(this)
			    .setTitle(R.string.comment)
			    .setView(input)
			    .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
						input.clearFocus();
						HashMap<String, Object> data = new HashMap<String, Object>();
						data.put("action", "comment");
						data.put("action_id", entryMap.get("activity_id").toString());
						data.put("action_data", input.getText().toString());
						refreshStream(data);
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
						data.put("action", "delete");
						data.put("action_id", entryMap.get("activity_id").toString());
						refreshStream(data);						
					}

		        })
		        .setNegativeButton(android.R.string.no, null)
		        .show();	

				return true;
			case R.id.mdelete:
		        new AlertDialog.Builder(this)
		        .setIcon(android.R.drawable.ic_dialog_alert)
		        .setTitle(R.string.delete)
		        .setMessage(R.string.really_delete)
		        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

		            @Override
		            public void onClick(DialogInterface dialog, int which) {
						HashMap<String, Object> data = new HashMap<String, Object>();
						data.put("action", "delete");
						data.put("action_id", entryMap.get("thread_id").toString());
						getMessages(data);	
					}

		        })
		        .setNegativeButton(android.R.string.no, null)
		        .show();	

				return true;
			case R.id.read:
				data = new HashMap<String, Object>();
				data.put("action", "read");
				data.put("action_id", entryMap.get("thread_id").toString());
				getMessages(data);	
				return true;				
			case R.id.unread:
				data = new HashMap<String, Object>();
				data.put("action", "unread");
				data.put("action_id", entryMap.get("thread_id").toString());
				getMessages(data);	
				return true;				
			case R.id.reply:
				input = new EditText(this);
				new AlertDialog.Builder(this)
			    .setTitle(R.string.reply)
			    .setView(input)
			    .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
						input.clearFocus();
						HashMap<String, Object> data = new HashMap<String, Object>();
						data.put("action", "reply");
						data.put("action_id", entryMap.get("thread_id").toString());
						data.put("action_data", input.getText().toString());
						getMessages(data);
			        }
			    }).setNegativeButton(android.R.string.no, null).show();	
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
	private final int DIALOG_COMMENTING = 2;

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
	public void redirectTo(String string) {
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

	public void refreshStream(HashMap<String, Object> data) {
		if(getWebsite() == null || prefs.getString("username", null) == null || prefs.getString("password", null) == null) {
			Toast.makeText(Buddypress.this, R.string.error,
					Toast.LENGTH_LONG).show();
			return;
		}
		
		Log.i(TAG ,"refreshing stream");
		
		if(scope == null)
			scope = filters.getSelectedItem().toString().replace(" ","_");

		data.put("scope", scope);
		data.put("user_data", "true");
		data.put("max", Integer.parseInt(prefs.getString("stream_max", "20")));
		
		BPRequest stream = new BPRequest(activity, mHandler, "bp.getActivity", data, MSG_STREAM);
		stream.execute();
		showRefresh();
	}

	private void getMessages(HashMap<String, Object> data){

		data.put("box","inbox");
		data.put("type","all");
		data.put("pag_num",Integer.parseInt(prefs.getString("stream_max", "20")));
		data.put("pag_page",1);
		data.put("search_terms","");
		
		BPRequest stream = new BPRequest(activity, mHandler, "bp.getMessages", data, MSG_MESSAGES);
		stream.execute();

		showRefresh();

	}
	
	public static final int MSG_STREAM = 1;
	public static final int MSG_SYNC = 2;
	public static final int MSG_MESSAGES = 3;
	
	private Handler mHandler = new Handler() {
		

		@Override
        public void handleMessage(Message msg) {
			Log.i(TAG ,"got message");
			completeRefresh();

			HashMap<?, ?> map;
			Object obj;
			Object[] list;
			
			String toast = null;
			
			boolean fromNotify = getIntent().hasExtra("notification");
			if(fromNotify)
				getIntent().removeExtra("notification");
			
			switch(msg.what) {
				case MSG_STREAM:
					if(!(msg.obj instanceof HashMap)) 
						break;
					
					if(submitting)
						activeEditText.setText("");
					
					map = (HashMap<?, ?>) msg.obj;
					obj = map.get("activities");
					
					list = (Object[]) obj;
					
					adapter = new StreamListAdapter(activity,list);
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
				case MSG_MESSAGES:
					if(!(msg.obj instanceof HashMap)) 
						break;
					
					map = (HashMap<?, ?>) msg.obj;
					obj = map.get("message");

					int total = (Integer) map.get("total");
					
					if(total == 0) {
						if(obj instanceof String)
							toast = (String)obj;
						break;
					}
					
					list = (Object[]) obj;
					
					MessageListAdapter madapter = new MessageListAdapter(activity,list);
					setListAdapter(madapter);
					toast = String.format(getString(R.string.messages),total);
					
					break;
				case MSG_SYNC:
					if(!(msg.obj instanceof HashMap)) 
						break;
						
					map = (HashMap<?, ?>) msg.obj;
					obj = map.get("message");
					
					processNotifications(obj);
					return;
				default: 
					if(msg.obj instanceof String)
						toast = (String) msg.obj;
					else
						toast = getString(R.string.error);
					break;
			}
			submitting = false;

			Toast.makeText(activity, (CharSequence) toast,
					Toast.LENGTH_LONG).show();

			
		}
    };

	private boolean isLandscape = true;

	protected boolean submitting;


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

		public void onClick(View v)
		{
			submitDrawer.close();
			String text = activeEditText.getText().toString();
			if(text.length() == 0)
				return;

			if(getWebsite() == null || prefs.getString("password", null) == null || prefs.getString("username", null) == null) {
				Toast.makeText(Buddypress.this, R.string.error,
						Toast.LENGTH_LONG).show();
				return;
			}
			
			submitting = true;
			
			HashMap<String, Object> data = new HashMap<String, Object>();
			data.put("action", "update");
			data.put("action_data", text);
			refreshStream(data);
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

	private String getWebsite() {
		String website = CUSTOM_WEBSITE  != null ? CUSTOM_WEBSITE : prefs.getString("website", "");
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

	            }

	            @Override
	            public void onAnimationRepeat(Animation animation) {
	                // TODO Auto-generated method stub

	            }

	            @Override
	            public void onAnimationEnd(Animation animation) {
	                // TODO Auto-generated method stub
	                //Log.d(TAG,"sliding down ended");

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

	public void showRefresh() {
		if(refreshItem == null) {
			refreshing = true;
			return;
		}
		
		/* Attach a rotating ImageView to the refresh item as an ActionView */
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ImageView iv = (ImageView) inflater.inflate(R.layout.rotate, null);
		
		Animation rotation = AnimationUtils.loadAnimation(this, R.animator.rotate);
		rotation.setRepeatCount(Animation.INFINITE);
		iv.startAnimation(rotation);
		refreshItem.setActionView(iv);
	}
	public void completeRefresh() {
    	refreshing = false;
		if(refreshItem == null || refreshItem.getActionView() == null)
			return;	
		
		refreshItem.getActionView().clearAnimation();
		refreshItem.setActionView(null);
	}
	
}
