
package org.yuttadhammo.buddydroid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.yuttadhammo.buddydroid.interfaces.BPAnimations;
import org.yuttadhammo.buddydroid.interfaces.BPRequest;
import org.yuttadhammo.buddydroid.interfaces.BPWebsite;
import org.yuttadhammo.buddydroid.interfaces.MessageListAdapter;
import org.yuttadhammo.buddydroid.interfaces.NoticeService;
import org.yuttadhammo.buddydroid.interfaces.NotificationListAdapter;
import org.yuttadhammo.buddydroid.interfaces.StreamListAdapter;

import com.actionbarsherlock.app.SherlockListActivity;
import android.app.NotificationManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
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
import com.slidingmenu.lib.SlidingMenu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Buddypress extends SherlockListActivity {
	
	protected String TAG = "Buddypress";

	public static String versionName = "1";
	private static SharedPreferences prefs;
	private EditText activeEditText;
	private Button submitButton;
	private static Buddypress activity;
	private StreamListAdapter adapter;
	private String website;

	private ListView listView;

	private SlidingDrawer submitDrawer;

	private LinearLayout submitPane;

	private Button submitDrawerButton;

	protected static String scope;

	private static ListView filters;

	public static int NOTIFY_ID = 0;

	private AlarmManager mgr=null;
	private PendingIntent pi=null;

	private EditText textContent;

	private EditText textDrawer;

	private Intent intent;


	private MenuItem refreshItem;
	private boolean refreshing;

	protected ArrayList<CharSequence> notificationStrings;
	protected ArrayList<String> notificationLinks;

	protected HashMap<String,Boolean> adminRights = new HashMap<String,Boolean>();

	private boolean isLandscape = true;

	protected boolean submitting;

	private SlidingMenu slideMenu;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		if(prefs.getString("username", null) == null) {
			Intent i = new Intent(this, BPLoginActivity.class);
			startActivityForResult(i, RESULT_LOGIN);
		}
		
		submitDrawer = (SlidingDrawer)findViewById(R.id.drawer);
		submitPane = (LinearLayout)findViewById(R.id.submit_pane);
		submitButton = (Button)findViewById(R.id.submit);
		submitDrawerButton = (Button)findViewById(R.id.submit_drawer);

		submitButton.setOnClickListener(mSubmitListener);
		submitDrawerButton.setOnClickListener(mSubmitListener);
		       
		textContent = (EditText) findViewById(R.id.text_content);
		textDrawer =  (EditText) findViewById(R.id.text_drawer);
		
		
		intent = this.getIntent();
		
    	LinearLayout header = (LinearLayout) getLayoutInflater().inflate(R.layout.list_header, null);
    	LinearLayout footer = (LinearLayout) getLayoutInflater().inflate(R.layout.list_footer, null);
    	listView = (ListView)findViewById(android.R.id.list);
    	//listView.addHeaderView(header);
    	listView.addFooterView(footer);

    	
    	scope = "sitewide";
    	
        slideMenu = new SlidingMenu(this);
        slideMenu.setMode(SlidingMenu.LEFT);
        slideMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        //menu.setShadowWidthRes(0);
        //menu.setShadowDrawable(R.drawable.shadow);
        slideMenu.setBehindWidthRes(R.dimen.slide_width);
        slideMenu.setFadeDegree(0.35f);
        slideMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        slideMenu.setMenu(R.layout.slide);

        filters = (ListView) slideMenu.getMenu().findViewById(R.id.slide_l);
    	final String[] filterStrings = getResources().getStringArray(R.array.filters);
		ArrayAdapter<CharSequence> slideAdapter = new ArrayAdapter<CharSequence>(this,android.R.layout.simple_list_item_1, filterStrings);
		filters.setAdapter(slideAdapter);

    	filters.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Log.d(TAG,"changed");
				String which = filterStrings[arg2].replace(" ", "_");
				refreshStream(which);
			
			}
    		
    	});

		
    	registerForContextMenu(listView);
    	
    	activity = this;
    	website = BPWebsite.getWebsite(this);

    	adjustLayout();
    	
    	if(prefs.getBoolean("auto_update", true) && !getIntent().hasExtra("notification"))
			refreshStream(scope);
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
    	String newWebsite = BPWebsite.getWebsite(this);
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
    	}

    	if(getIntent().hasExtra("notification")) {
    		getIntent().removeExtra("notification");
			getNotifications(new HashMap<String, Object>());
    	}
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
			refreshStream(scope);
	    	showRefresh();
	    }
	    return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		super.onOptionsItemSelected(item);
		
		Intent intent;
		switch (item.getItemId()) {
	        case android.R.id.home:
	            // app icon in action bar clicked; go home
	            slideMenu.toggle();
	            return true;

			case (int)R.id.menuStream:
		    	if(BPWebsite.getWebsite(this) == null) {
					Toast.makeText(this, getString(R.string.noWebsite),
							Toast.LENGTH_LONG).show();
					return true;
		    	}
				refreshStream(scope);
				break;
			case (int)R.id.menuLogin:
				intent = new Intent(this, BPLoginActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivityForResult(intent, RESULT_LOGIN);
				break;
			case (int)R.id.menuPrefs:
				intent = new Intent(this, BPSettingsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				break;
			case (int)R.id.menuHelp:
				intent = new Intent(this, BPHelpActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				break;

			default:
				return false;
	    }
		return true;
	}	

	public static final int RESULT_USER = 0;
	public static final int RESULT_LOGIN = 1;
	
	protected void  onActivityResult (int requestCode, int resultCode, Intent  data) {
		
		if(requestCode == RESULT_USER && resultCode != Activity.RESULT_OK)		
			refreshStream(scope);
		else if(requestCode == RESULT_LOGIN && resultCode == Activity.RESULT_OK) {
			String ws = data.getStringExtra("website");
			String un = data.getStringExtra("username");
			String pw = data.getStringExtra("password");

			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("website", ws);
			editor.putString("username", un);
			editor.putString("password", pw);
			editor.commit();
			refreshStream(scope);
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
			
			// check if moderator
			
			if(adminRights.containsKey("can_moderate") && adminRights.get("can_moderate"))
				inflater.inflate(R.menu.stream_longclick_admin, menu);
			else { // check if self
				AdapterView.AdapterContextMenuInfo info =
			            (AdapterView.AdapterContextMenuInfo) menuInfo;
				Adapter adapter = getListAdapter();
				final HashMap<?,?> entryMap = (HashMap<?, ?>) adapter.getItem(info.position);

				if(entryMap.containsKey("self") && (Boolean) entryMap.get("self"))
					inflater.inflate(R.menu.stream_longclick_admin, menu);
				else
					inflater.inflate(R.menu.stream_longclick, menu);
			}
			
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
						getActivities(data,scope);
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
		        .setMessage(R.string.really_delete_post)
		        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

		            @Override
		            public void onClick(DialogInterface dialog, int which) {
						HashMap<String, Object> data = new HashMap<String, Object>();
						data.put("action", "delete");
						data.put("action_id", entryMap.get("activity_id").toString());
						getActivities(data,scope);						
					}

		        })
		        .setNegativeButton(android.R.string.no, null)
		        .show();	

				return true;
			case R.id.mdelete:
		        new AlertDialog.Builder(this)
		        .setIcon(android.R.drawable.ic_dialog_alert)
		        .setTitle(R.string.delete)
		        .setMessage(R.string.really_delete_message)
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


	protected void refreshStream(String which) {
		Log.d(TAG,"getting for scope of "+which);
		scope = which;
		
		if(which.equals("messages"))
			getMessages(new HashMap<String, Object>());
		else if(which.equals("notifications"))
			getNotifications(new HashMap<String, Object>());
		else
			getActivities(new HashMap<String, Object>(), which);

		slideMenu.showContent(true);

	}
	
	public void redirectTo(String string) {
		String site = BPWebsite.getWebsite(this);
		if(site == null)
			return;
		Uri url = Uri.parse(site+"index.php?bp_xmlrpc=true&bp_xmlrpc_redirect="+string);
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(url);
		activity.startActivity(i);
	}
 
	public void getActivities(HashMap<String, Object> data, String ascope) {
		if(BPWebsite.getWebsite(this) == null || prefs.getString("username", null) == null || prefs.getString("password", null) == null) {
			Toast.makeText(Buddypress.this, R.string.error,
					Toast.LENGTH_LONG).show();
			return;
		}
		
		Log.i(TAG ,"refreshing stream");
		
		if(ascope == null)
			ascope = filters.getSelectedItem().toString().replace(" ","_");

		data.put("scope", ascope);
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


	private void getNotifications(HashMap<String, Object> data){
		data.put("type","object");
		data.put("status","is_new");
		BPRequest stream = new BPRequest(activity, mHandler, "bp.getNotifications", data, MSG_SYNC);
		stream.execute();

		showRefresh();

	}
	
	public static final int MSG_STREAM = 1;
	public static final int MSG_SYNC = 2;
	public static final int MSG_MESSAGES = 3;
	public static final int MSG_SCOPE = 4;

	private Handler mHandler = new Handler() {
		

		@Override
        public void handleMessage(Message msg) {
			Log.i(TAG ,"got message");
			completeRefresh();

			HashMap<?, ?> map;
			Object obj;
			Object[] list;
			
			String toast = null;
			
			switch(msg.what) {
				case MSG_STREAM:
					if(!(msg.obj instanceof HashMap)) { 
						if(msg.obj instanceof String)
							toast = (String) msg.obj;
						break;
					}
					
					if(submitting)
						activeEditText.setText("");

					map = (HashMap<?, ?>) msg.obj;
					obj = map.get("message");
					
					if(!(obj instanceof Object[])) {
						if(obj instanceof String)
							toast = (String) obj;
						break;
					}
					
					list = (Object[]) obj;
					
					if(map.containsKey("user_data") && map.get("user_data") instanceof HashMap){
						Log.i(TAG ,"got user data");
						Map<?,?> user = (HashMap<?, ?>) map.get("user_data");

						if(user.get("can_delete_user") instanceof Boolean)
							adminRights.put("can_delete_user", (Boolean) user.get("can_delete_user"));
						if(user.get("can_moderate") instanceof Boolean)
							adminRights.put("can_moderate", (Boolean) user.get("can_moderate"));
							 
						Object nfoo = user.get("notifications");
						//processNotifications(nfoo);
					}

					adapter = new StreamListAdapter(activity,list);
					setListAdapter(adapter);
					toast = getString(R.string.updated);

					break;
				case MSG_MESSAGES:
					if(!(msg.obj instanceof HashMap)) 
						break;
					
					map = (HashMap<?, ?>) msg.obj;
					obj = map.get("message");
					if(!(obj instanceof Object[])) {
						if(obj instanceof String)
							toast = (String) obj;
						break;
					}
					
					int total = 0;
					
					try {
						total = (Integer) map.get("total");
						
						if(total == 0) {
							if(obj instanceof String)
								toast = (String)obj;
							break;
						}
					}
					catch(Exception e){
						
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
					if(!(obj instanceof Object[])) {
						if(obj instanceof String)
							toast = (String) obj;
						break;
					}

					processNotifications(obj);
					return;
				case MSG_SCOPE:
					if((msg.obj instanceof String)) 
						refreshStream((String) msg.obj);
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
			//String nfs = Integer.toString(nfo.length);
			NotificationListAdapter madapter = new NotificationListAdapter(activity,nfo, mHandler);
			setListAdapter(madapter);
			//String toast = String.format(getString(R.string.notifications),nfs);
		}
		else {
			setEmptyList();
		}
	}


	private void setEmptyList() {
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1));
	}

	private OnClickListener mSubmitListener = new OnClickListener() {

		public void onClick(View v)
		{
			InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE); 

			inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                       InputMethodManager.HIDE_NOT_ALWAYS);
			
			submitDrawer.close();
			String text = activeEditText.getText().toString();
			if(text.length() == 0)
				return;

			if(BPWebsite.getWebsite(activity) == null || prefs.getString("password", null) == null || prefs.getString("username", null) == null) {
				Toast.makeText(Buddypress.this, R.string.error,
						Toast.LENGTH_LONG).show();
				return;
			}
			
			submitting = true;
			
			HashMap<String, Object> data = new HashMap<String, Object>();
			data.put("action", "update");
			data.put("action_data", text);
			refreshStream(scope);
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

	public void doSlideDown(View view){
		if(view.getVisibility() == View.VISIBLE || view.getAnimation() != null)
			return;
		view.setVisibility(View.VISIBLE);
		Animation slideDown = BPAnimations.slideDown(); 
		view.startAnimation(slideDown);
	}

	public void doSlideUp(View view){
		if(view.getVisibility() == View.GONE || view.getAnimation() != null)
			return;

		Animation slideUp = BPAnimations.slideUp(view); 
		view.startAnimation(slideUp);
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
