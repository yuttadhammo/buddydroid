
package org.yuttadhammo.buddydroid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yuttadhammo.buddydroid.interfaces.BPAnimations;
import org.yuttadhammo.buddydroid.interfaces.BPRequest;
import org.yuttadhammo.buddydroid.interfaces.BPWebsite;
import org.yuttadhammo.buddydroid.interfaces.FilterArrayAdapter;
import org.yuttadhammo.buddydroid.interfaces.FiltersExpandableListAdapter;
import org.yuttadhammo.buddydroid.interfaces.FriendsListAdapter;
import org.yuttadhammo.buddydroid.interfaces.GroupsListAdapter;
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
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;
import com.slidingmenu.lib.SlidingMenu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Buddypress extends SherlockListActivity {
	
	protected String TAG = "Buddypress";

	public static String versionName = "2.7";
	
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

	protected static String currentScope;

	private static ExpandableListView filters;

	public static int NOTIFY_ID = 0;

	private AlarmManager mgr=null;
	private PendingIntent pi=null;

	private EditText textContent;

	private EditText textDrawer;

	private Intent intent;
	private int mStackLevel = 0;


	private MenuItem refreshItem;
	private boolean refreshing;

	protected ArrayList<CharSequence> notificationStrings;
	protected ArrayList<String> notificationLinks;

	protected HashMap<String,Boolean> adminRights = new HashMap<String,Boolean>();

	private boolean isLandscape = true;

	protected boolean submitting;

	private SlidingMenu slideMenu;
	private ArrayList<Integer> filterArray;

	private MenuItem notificationItem;

	private String lastScope;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		submitDrawer = (SlidingDrawer)findViewById(R.id.drawer);
		submitPane = (LinearLayout)findViewById(R.id.submit_pane);
		submitButton = (Button)findViewById(R.id.submit);
		submitDrawerButton = (Button)findViewById(R.id.submit_drawer);

		submitButton.setOnClickListener(mSubmitListener);
		submitDrawerButton.setOnClickListener(mSubmitListener);
		       
		textContent = (EditText) findViewById(R.id.text_content);
		textDrawer =  (EditText) findViewById(R.id.text_drawer);
		
		
		intent = this.getIntent();
		
    	LinearLayout footer = (LinearLayout) getLayoutInflater().inflate(R.layout.list_footer, null);
    	listView = (ListView)findViewById(android.R.id.list);
    	//listView.addHeaderView(header);
    	listView.addFooterView(footer);

    	
    	currentScope = "sitewide";
    	lastScope = "sitewide";
    	
        slideMenu = new SlidingMenu(this);
        slideMenu.setMode(SlidingMenu.LEFT);
        slideMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        //menu.setShadowWidthRes(0);
        //menu.setShadowDrawable(R.drawable.shadow);
        slideMenu.setBehindWidthRes(R.dimen.slide_width);
        slideMenu.setFadeDegree(0.35f);
        slideMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        slideMenu.setMenu(R.layout.slide);

        filters = (ExpandableListView) slideMenu.getMenu().findViewById(R.id.filters);
        
    	registerForContextMenu(listView);
    	
    	activity = this;
    	website = BPWebsite.getWebsite(this);

    	adjustLayout();
    	
    	if(prefs.getBoolean("auto_update", true) && !getIntent().hasExtra("notification"))
    		refreshStream(currentScope);
    	else if(BPWebsite.getWebsite(this) == null || prefs.getString("username", null) == null || prefs.getString("password", null) == null) {
			Intent i = new Intent(this, BPLoginActivity.class);
			startActivityForResult(i, RESULT_LOGIN);
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();


    	// set up notification
		((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancelAll();
		
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
	    
	    notificationItem = menu.findItem(R.id.menuNotification);
	    
	    refreshItem = menu.findItem(R.id.menuStream);
	    if(refreshing)
	    	showRefresh();
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
				refreshStream(currentScope);
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
			refreshStream(currentScope);
		else if(requestCode == RESULT_LOGIN && resultCode == Activity.RESULT_OK) {
			String ws = data.getStringExtra("website");
			String un = data.getStringExtra("username");
			String pw = data.getStringExtra("password");

			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("website", ws);
			editor.putString("username", un);
			editor.putString("password", pw);
			editor.commit();
			refreshStream(currentScope);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		android.view.MenuInflater inflater = getMenuInflater();
       	
		if(currentScope.equals("messages")) {
			inflater.inflate(R.menu.message_longclick, menu);
            menu.setHeaderTitle(getString(R.string.message_options));
		}
		else if(currentScope.equals("friends_friends")) {
        	inflater.inflate(R.menu.friends_my_longclick, menu);

	        menu.setHeaderTitle(getString(R.string.friend_options));
		}
		else if(currentScope.equals("friends_friend_requests")) {
        	inflater.inflate(R.menu.friends_request_longclick, menu);

	        menu.setHeaderTitle(getString(R.string.friend_request_options));
		}		
		else if(currentScope.startsWith("groups_")) {
			
	        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
	        int position = info.position;
		    final HashMap<?,?> entryMap = (HashMap<?, ?>) getListView().getItemAtPosition(position);
	        if((Boolean) entryMap.get("is_member"))
	        	inflater.inflate(R.menu.group_my_longclick, menu);
	        else 
	        	inflater.inflate(R.menu.group_longclick, menu);

			// check if moderator
			
			if(!adminRights.containsKey("can_moderate") || !adminRights.get("can_moderate"))
				menu.findItem(R.id.delete).setVisible(false);
	        
	        menu.setHeaderTitle(getString(R.string.group_options));
            
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
						getActivities(data,currentScope);
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
						getActivities(data,currentScope);						
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
						getMessages(data, "inbox");	
					}

		        })
		        .setNegativeButton(android.R.string.no, null)
		        .show();	

				return true;
			case R.id.read:
				data = new HashMap<String, Object>();
				data.put("action", "read");
				data.put("action_id", entryMap.get("thread_id").toString());
				getMessages(data, "inbox");	
				return true;				
			case R.id.unread:
				data = new HashMap<String, Object>();
				data.put("action", "unread");
				data.put("action_id", entryMap.get("thread_id").toString());
				getMessages(data, "inbox");	
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
						getMessages(data, "inbox");
			        }
			    }).setNegativeButton(android.R.string.no, null).show();	
				return true;
			case R.id.join:
				data = new HashMap<String, Object>();
				data.put("action", "join");
				data.put("action_id", entryMap.get("id").toString());
				getGroups(data, currentScope);	
				return true;					
			case R.id.leave:
				data = new HashMap<String, Object>();
				data.put("action", "leave");
				data.put("action_id", entryMap.get("id").toString());
				getGroups(data, currentScope);	
				return true;
			case R.id.gview:
				link = (String)entryMap.get("group_domain");
				url = Uri.parse(link);
				i = new Intent(Intent.ACTION_VIEW, url);
				activity.startActivity(i);
				return true;
			case R.id.gdelete:
		        new AlertDialog.Builder(this)
		        .setIcon(android.R.drawable.ic_dialog_alert)
		        .setTitle(R.string.delete)
		        .setMessage(R.string.really_delete_group)
		        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

		            @Override
		            public void onClick(DialogInterface dialog, int which) {
						HashMap<String, Object> data = new HashMap<String, Object>();
						data.put("action", "delete");
						data.put("action_id", entryMap.get("id").toString());
						getGroups(data, currentScope);	
					}

		        })
		        .setNegativeButton(android.R.string.no, null)
		        .show();	

				return true;
			case R.id.fview:
				link = (String)entryMap.get("user_domain");
				url = Uri.parse(link);
				i = new Intent(Intent.ACTION_VIEW, url);
				activity.startActivity(i);
				return true;
			case R.id.accept:
				data = new HashMap<String, Object>();
				data.put("action", "accept");
				data.put("action_id", entryMap.get("friendship_id").toString());
				getFriends(data, "friends_friends");	
				return true;
			case R.id.unfriend:
				data = new HashMap<String, Object>();
				data.put("action", "unfriend");
				data.put("action_id", entryMap.get("id").toString());
				getFriends(data, currentScope);	
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

	
	protected void updateFilters(HashMap<?,?> map) {
		Log.d(TAG,"updating filters");
		if(!map.containsKey("active_components"))
			return;

		filterArray = new ArrayList<Integer>();

		final ArrayList<String> activities = new ArrayList<String>();
		
		Object[] obj = (Object[]) map.get("active_components");
		for(Object comp : obj) {
			String cs = (String) comp;
			
			if(cs.equals("activity")) {
				filterArray.add(R.string.activity);
				String[] acts = getResources().getStringArray(R.array.main_filters);
				List<String> acta = Arrays.asList(acts);
				activities.addAll(acta);
			}
			else if(cs.equals("friends")) {
				filterArray.add(R.string.friends);
				activities.add("friends");

			}
			else if(cs.equals("groups")) {
				filterArray.add(R.string.groups);
				String[] acts = getResources().getStringArray(R.array.group_filters);
				activities.add(acts[0]);
				activities.add(acts[1]);
			}
			else if(cs.equals("messages")) {
				filterArray.add(R.string.messages);
			}
		}
		
		ExpandableListAdapter adapter = new FiltersExpandableListAdapter(this,filterArray, activities);
		filters.setAdapter(adapter);
		filters.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {

				int fid = filterArray.get(groupPosition);
				String child;
				String which;
				switch(fid) {
					case R.string.activity:
						child = activities.get(childPosition);
						which = child.replace(" ", "_");
						getActivities(new HashMap<String, Object>(), which);
						break;
					case R.string.friends:
						child = activity.getResources().getStringArray(R.array.friends_filters)[childPosition];
						which = "friends_"+child.replace(" ", "_");
						getFriends(new HashMap<String, Object>(), which);
						break;
					case R.string.groups:
						if(childPosition == 2) {
							// new group
							showNewGroupDialog();
							return false;
						}
						child = activity.getResources().getStringArray(R.array.group_filters)[childPosition];
						which = "groups_"+child.replace(" ", "_");
						getGroups(new HashMap<String, Object>(), which);
						break;
					case R.string.messages:
						
						if(childPosition == 2) {
							//compose
							showNewMessageDialog();
							return false;
						}
						
						child = activity.getResources().getStringArray(R.array.message_filters)[childPosition];
						which = child.replace(" ", "_");
						getMessages(new HashMap<String, Object>(), which);
						break;
				}
				slideMenu.showContent(true);
				
				return false;
			}
			
		});
	}

	protected void refreshStream(String which) {
		if(refreshing)
			return;

		if(BPWebsite.getWebsite(this) == null || prefs.getString("username", null) == null || prefs.getString("password", null) == null) {
			Intent i = new Intent(this, BPLoginActivity.class);
			startActivityForResult(i, RESULT_LOGIN);
			return;
		}
		
		Log.d(TAG,"getting for currentScope of "+which);
		lastScope = which;
		
		if(which.equals("messages"))
			getMessages(new HashMap<String, Object>(), "inbox");
		else if(which.equals("notifications"))
			getNotifications(new HashMap<String, Object>());
		else if(which.startsWith("groups_"))
			getGroups(new HashMap<String, Object>(),which);
		else if(which.startsWith("friends_"))
			getFriends(new HashMap<String, Object>(),which);
		else if(which.equals("notifications"))
			getNotifications(new HashMap<String, Object>());
		else
			getActivities(new HashMap<String, Object>(), which);

		slideMenu.showContent(true);

	}
	
	public void getActivities(HashMap<String, Object> data, String ascope) {
		
		if(refreshing)
			return;
		
		lastScope = ascope;

		Log.i(TAG ,"getting activities for "+currentScope);
		
		data.put("scope", ascope);
		data.put("user_data", "true");
		data.put("active_components", "true");
		data.put("max", Integer.parseInt(prefs.getString("stream_max", "20")));
		
		BPRequest stream = new BPRequest(activity, mHandler, "bp.getActivity", data, MSG_STREAM);
		stream.execute();
		showRefresh();
	}

	private void getMessages(HashMap<String, Object> data, String which){
		if(refreshing)
			return;

		lastScope = "messages";

		Log.i(TAG ,"getting messages");

		data.put("user_data", "true");
		data.put("active_components", "true");
		data.put("box",which);
		data.put("type","all");
		data.put("pag_num",Integer.parseInt(prefs.getString("stream_max", "20")));
		data.put("pag_page",1);
		data.put("search_terms","");
		
		BPRequest stream = new BPRequest(activity, mHandler, "bp.getMessages", data, MSG_MESSAGES);
		stream.execute();

		showRefresh();

	}


	private void getNotifications(HashMap<String, Object> data){
		if(refreshing)
			return;

		lastScope = "notifications";

		Log.i(TAG ,"getting notifications");

		data.put("user_data", "true");
		data.put("active_components", "true");
		data.put("type","object");
		data.put("status","is_new");
		BPRequest stream = new BPRequest(activity, mHandler, "bp.getNotifications", data, MSG_SYNC);
		stream.execute();

		showRefresh();

	}
	

	private void getFriends(HashMap<String, Object> data, String which){
		if(refreshing)
			return;

		lastScope = which;

		Log.i(TAG ,"getting friends");

		data.put("user_data", "true");
		data.put("active_components", "true");
		if(which.equals("friends_friend_requests"))
			data.put("requests","true");
		BPRequest stream = new BPRequest(activity, mHandler, "bp.getMyFriends", data, MSG_FRIENDS);
		stream.execute();

		showRefresh();

	}
		

	private void getGroups(HashMap<String, Object> data, String which){
		if(refreshing)
			return;

		lastScope = which;

		Log.i(TAG ,"getting groups");

		data.put("user_data", "true");
		data.put("active_components", "true");
		if(which.equals("groups_my_groups"))
			data.put("user",true);
		data.put("status","is_new");
		BPRequest stream = new BPRequest(activity, mHandler, "bp.getGroups", data, MSG_GROUPS);
		stream.execute();

		showRefresh();

	}
	
	
	public static final int MSG_STREAM = 1;
	public static final int MSG_SYNC = 2;
	public static final int MSG_MESSAGES = 3;
	public static final int MSG_MESSAGE = 4;
	public static final int MSG_SCOPE = 5;
	public static final int MSG_FRIENDS = 6;
	public static final int MSG_GROUPS = 7;

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
						setEmptyList();
						break;
					}
					
					if(submitting)
						activeEditText.setText("");

					map = (HashMap<?, ?>) msg.obj;
					obj = map.get("message");
					
					if(!(obj instanceof Object[])) {
						if(obj instanceof String)
							toast = (String) obj;
						setEmptyList();
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
						processNotifications(nfoo);
					}

					adapter = new StreamListAdapter(activity,list);
					setListAdapter(adapter);
					
					toast = getString(R.string.updated);
					
					currentScope = lastScope;
					
					break;
				case MSG_MESSAGES:
					if(!(msg.obj instanceof HashMap)) 
						break;
					
					map = (HashMap<?, ?>) msg.obj;
					obj = map.get("message");
					if(!(obj instanceof Object[])) {
						if(obj instanceof String)
							toast = (String) obj;
						setEmptyList();
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

					if(map.containsKey("user_data") && map.get("user_data") instanceof HashMap){
						Log.i(TAG ,"got user data");
						Map<?,?> user = (HashMap<?, ?>) map.get("user_data");

						if(user.get("can_delete_user") instanceof Boolean)
							adminRights.put("can_delete_user", (Boolean) user.get("can_delete_user"));
						if(user.get("can_moderate") instanceof Boolean)
							adminRights.put("can_moderate", (Boolean) user.get("can_moderate"));
							 
						Object nfoo = user.get("notifications");
						processNotifications(nfoo);
					}

					toast = String.format(getString(R.string.got_messages),total);
					
					currentScope = "messages";

					break;
				case MSG_MESSAGE:
					if(!(msg.obj instanceof HashMap)) 
						break;
					
					map = (HashMap<?, ?>) msg.obj;
					obj = map.get("confirmation");

					if(obj instanceof Boolean && (Boolean)obj)
						toast = getString(R.string.sent);
					else
						toast = getString(R.string.error);
					
					break;
				case MSG_SYNC:
					if(!(msg.obj instanceof HashMap)) 
						break;
					notificationItem.setVisible(false);
					map = (HashMap<?, ?>) msg.obj;
					obj = map.get("message");
					if(!(obj instanceof Object[])) {
						if(obj instanceof String)
							toast = (String) obj;
						setEmptyList();
						break;
					}
					notificationStrings = new ArrayList<CharSequence>();
					notificationLinks = new ArrayList<String>();
					if(obj instanceof Object[] && !(((Object[])obj)[0] instanceof Boolean)) {
						Object[] nfo = (Object[]) obj;
						NotificationListAdapter nadapter = new NotificationListAdapter(activity,nfo, mHandler);
						setListAdapter(nadapter);
						//String toast = String.format(getString(R.string.notifications),nfs);
						processNotifications(obj);
					}
					else {
						setEmptyList();
					}
					currentScope = "notifications";
					break;
				case MSG_FRIENDS:
					if(!(msg.obj instanceof HashMap)) 
						break;
						
					map = (HashMap<?, ?>) msg.obj;
					obj = map.get("message");
					if(!(obj instanceof Object[])) {
						if(obj instanceof String)
							toast = (String) obj;
						setEmptyList();
						break;
					}

					list = (Object[]) obj;
					
					FriendsListAdapter fadapter = new FriendsListAdapter(activity,list);
					setListAdapter(fadapter);

					if(map.containsKey("user_data") && map.get("user_data") instanceof HashMap){
						Log.i(TAG ,"got user data");
						Map<?,?> user = (HashMap<?, ?>) map.get("user_data");

						if(user.get("can_delete_user") instanceof Boolean)
							adminRights.put("can_delete_user", (Boolean) user.get("can_delete_user"));
						if(user.get("can_moderate") instanceof Boolean)
							adminRights.put("can_moderate", (Boolean) user.get("can_moderate"));
							 
						Object nfoo = user.get("notifications");
						processNotifications(nfoo);
					}
					
					currentScope = lastScope;
					break;					
				case MSG_GROUPS:
					if(!(msg.obj instanceof HashMap)) 
						break;
						
					map = (HashMap<?, ?>) msg.obj;
					obj = map.get("message");
					if(!(obj instanceof Object[])) {
						if(obj instanceof String)
							toast = (String) obj;
						setEmptyList();
						break;
					}

					list = (Object[]) obj;
					
					GroupsListAdapter gadapter = new GroupsListAdapter(activity,list);
					setListAdapter(gadapter);

					if(map.containsKey("user_data") && map.get("user_data") instanceof HashMap){
						Log.i(TAG ,"got user data");
						Map<?,?> user = (HashMap<?, ?>) map.get("user_data");

						if(user.get("can_delete_user") instanceof Boolean)
							adminRights.put("can_delete_user", (Boolean) user.get("can_delete_user"));
						if(user.get("can_moderate") instanceof Boolean)
							adminRights.put("can_moderate", (Boolean) user.get("can_moderate"));
							 
						Object nfoo = user.get("notifications");
						processNotifications(nfoo);
					}
					
					currentScope = lastScope;
					break;
				case MSG_SCOPE:
					if((msg.obj instanceof String)) { 
						if(((String) msg.obj).startsWith("http")) {
							Uri url = Uri.parse((String) msg.obj);
							Intent i = new Intent(Intent.ACTION_VIEW, url);
							activity.startActivity(i);							
						}
						else
							refreshStream((String) msg.obj);
					}
					return;
				default: 
					if(msg.obj instanceof String)
						toast = (String) msg.obj;
					else
						toast = getString(R.string.error);
					break;
			}
			submitting = false;
			
			if(toast != null)
				Toast.makeText(activity, (CharSequence) toast,
					Toast.LENGTH_LONG).show();

			if(msg.obj instanceof HashMap)
				updateFilters((HashMap<?, ?>) msg.obj);
				
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

	protected void doSlideToggle(View view) {
		if(view.getVisibility() == View.GONE)
			doSlideDown(view);
		else
			doSlideUp(view);
	}

	protected void showNewMessageDialog() {
		LayoutInflater inflater = activity.getLayoutInflater();
		LinearLayout messageLayout = (LinearLayout) inflater.inflate(R.layout.message, null);
		final EditText subject = (EditText) messageLayout.findViewById(R.id.subject);
		final EditText recipients = (EditText) messageLayout.findViewById(R.id.recipients);
		recipients.setVisibility(View.VISIBLE);
		final EditText body = (EditText) messageLayout.findViewById(R.id.body);
		new AlertDialog.Builder(activity)
	    .setTitle(R.string.send_message)
	    .setView(messageLayout)
	    .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {
				HashMap<String, Object> data = new HashMap<String, Object>();
				data.put("thread_id", false);
				data.put("subject", subject.getText().toString());
				data.put("recipients", recipients.getText().toString());
				data.put("content", body.getText().toString());
				BPRequest stream = new BPRequest(activity, mHandler, "bp.sendMessage", data, MSG_MESSAGE);
				stream.execute();
	        }
	    }).setNegativeButton(android.R.string.no, null).show();			
	}


	protected void showNewGroupDialog() {
		LayoutInflater inflater = activity.getLayoutInflater();
		LinearLayout groupLayout = (LinearLayout) inflater.inflate(R.layout.group_new, null);
		final Spinner status = (Spinner) groupLayout.findViewById(R.id.status);
		final EditText name = (EditText) groupLayout.findViewById(R.id.name);
		final EditText desc = (EditText) groupLayout.findViewById(R.id.desc);
		new AlertDialog.Builder(activity)
	    .setTitle(R.string.new_group)
	    .setView(groupLayout)
	    .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {
				HashMap<String, Object> data = new HashMap<String, Object>();
				data.put("action", "create");
				
				HashMap<String, Object> group = new HashMap<String, Object>();
				group.put("name", name.getText().toString());
				group.put("desc", desc.getText().toString());
				group.put("status", status.getSelectedItem().toString());
				
				data.put("group", group);
				getGroups(data,"groups_groups");
	        }
	    }).setNegativeButton(android.R.string.no, null).show();			
	}
	
	protected void processNotifications(Object obj) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.notifications, null);
		Button b = (Button) ll.findViewById(R.id.count);

		if(obj instanceof Object[] && !(((Object[])obj)[0] instanceof Boolean)) {
			Object[] nfo = (Object[]) obj;
			
			b.setText(Integer.toString(nfo.length));
			b.setBackgroundResource(R.drawable.notifications);
			b.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					getNotifications(new HashMap<String, Object>());
				}
				
			});
			notificationItem.setActionView(b).setVisible(true);
		}
		else {
			notificationItem.setVisible(false);
			//b.setText("0");
			//notificationItem.setActionView(b).setVisible(true);
		}
	}


	private void setEmptyList() {
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1));
	}

	private OnClickListener mSubmitListener = new OnClickListener() {

		public void onClick(View v)
		{
			InputMethodManager imm = (InputMethodManager)getSystemService(
				      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			
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
			getActivities(data,currentScope);
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
			Log.i(TAG,"not ready to show refresh");
			refreshing = true;
			return;
		}
		//Log.i(TAG,"showing refresh");
		
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

	public void redirectTo(String string) {
		String site = BPWebsite.getWebsite(this);
		if(site == null)
			return;
		Uri url = Uri.parse(site+"index.php?bp_xmlrpc=true&bp_xmlrpc_redirect="+string);
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(url);
		activity.startActivity(i);
	}
}
