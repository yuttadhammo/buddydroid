package org.yuttadhammo.buddydroid;

import java.util.HashMap;

import org.yuttadhammo.buddydroid.interfaces.BPAnimations;
import org.yuttadhammo.buddydroid.interfaces.BPRequest;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class BPUserActivity extends Activity {

	protected String TAG = "BPUserActivity";
	private BPUserActivity activity;
	private LinearLayout listView;
	public static final int MSG_USER = 1;
	public static final int MSG_DELETE = 2;
	public static final int MSG_MESSAGE = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user);

		listView = (LinearLayout) findViewById(R.id.user_list);

		activity = this;
		
		HashMap<String, Object> data = new HashMap<String, Object>();

		if(getIntent().hasExtra("user_id")) { // stream click
			Log.d(TAG,"got userid: "+getIntent().getStringExtra("user_id"));
			if(getIntent().hasExtra("action")) {
				data.put("action", getIntent().getStringExtra("action"));
				data.put("action_id", getIntent().getStringExtra("action_id"));
				Log.d(TAG,"got action: "+getIntent().getStringExtra("action"));
			}
			data.put("user_id", getIntent().getStringExtra("user_id"));
			
			BPRequest stream = new BPRequest(this, mHandler, "bp.getMemberInfo", data, MSG_USER);
			stream.execute();

		}
			
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// don't do refresh on back to exit
		
		if (keyCode == KeyEvent.KEYCODE_BACK) 
		    setResult(Activity.RESULT_OK, new Intent());

		return super.onKeyDown(keyCode, event);
	}

	
	/** Handler for the message from the timer service */
	@SuppressLint("HandlerLeak")
	public Handler mHandler = new Handler() {
		
		@Override
        public void handleMessage(Message msg) {
			Log.i(TAG ,"got message");

			final HashMap<?, ?> map;
			Object obj;
			final LayoutInflater inflater = activity.getLayoutInflater();
			
			String error = null;
			
			boolean fromNotify = getIntent().hasExtra("notification");
			if(fromNotify)
				getIntent().removeExtra("notification");
			
			switch(msg.what) {
				case MSG_USER:
					if(!(msg.obj instanceof HashMap)) {
						if(msg.obj instanceof String)
							error = (String) msg.obj;
						break;
					}
					map = (HashMap<?, ?>) msg.obj;
					obj = map.get("message");
					if(!(obj instanceof HashMap)) {
						if(obj instanceof String)
							error = (String) obj;
						else error = getString(R.string.error);
						break;
					}
					
					
					showUser(obj);
					
					break;
				case MSG_DELETE:
					if(!(msg.obj instanceof HashMap)) 
						break;
					
					map = (HashMap<?, ?>) msg.obj;
					obj = map.get("confirmation");
					if(obj instanceof Boolean && (Boolean)obj) {
						Toast.makeText(activity, R.string.user_deleted, Toast.LENGTH_LONG).show();
						finish();
					}
					else
						Toast.makeText(activity, R.string.error, Toast.LENGTH_LONG).show();
					break;
				case MSG_MESSAGE:
					if(!(msg.obj instanceof HashMap)) 
						break;
					
					map = (HashMap<?, ?>) msg.obj;
					obj = map.get("confirmation");

					if(obj instanceof Boolean && (Boolean)obj)
						Toast.makeText(activity, R.string.sent, Toast.LENGTH_LONG).show();
					else
						Toast.makeText(activity, R.string.error, Toast.LENGTH_LONG).show();
					
					break;
				default: 
					if(msg.obj instanceof String)
						error = (String) msg.obj;
					else
						error = getString(R.string.error);
					break;
			}
			
			if(error != null) {
				TextView errorView = (TextView) inflater.inflate(R.layout.user_group_title, null);
				errorView.setText(error);
				listView.addView(errorView);				
			}
			
			Animation slideDown = BPAnimations.slideDown(); 
			listView.startAnimation(slideDown);
			listView.setVisibility(View.VISIBLE);
		}
    };

	protected void showUser(Object user) {
		
		final HashMap<?, ?> map = (HashMap<?, ?>) user;
		Object obj = map.get("profile_groups");
		
		if(!(obj instanceof Object[])) 
			return;

		Object[] list;
		final LayoutInflater inflater = activity.getLayoutInflater();
		
		LinearLayout header = (LinearLayout) inflater.inflate(R.layout.user_header, null);
		TextView title = (TextView) header.findViewById(R.id.user_name);
		TextView login = (TextView) header.findViewById(R.id.user_login);
		TextView active = (TextView) header.findViewById(R.id.user_active);
		TextView status = (TextView) header.findViewById(R.id.user_activity);

		title.setText(String.format(getString(R.string.sprofile),(String) map.get("display_name")));
		
		if(map.get("user_nicename") instanceof String)
			login.setText("@"+(String) map.get("user_nicename"));

		if(map.get("last_active") instanceof String)
			active.setText((String) map.get("last_active"));

		if(map.get("last_status") instanceof String)
			status.setText(Html.fromHtml((String) map.get("last_status")));

		
		status.setMovementMethod(LinkMovementMethod.getInstance());
		
		ImageView iv = (ImageView) header.findViewById(R.id.user_avatar);
		HashMap<?,?> avatars = (HashMap<?, ?>) map.get("avatar");
		String imgurl = (String) avatars.get("full");
		UrlImageViewHelper.setUrlDrawable(iv, imgurl);
		
		iv.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				String link = (String)map.get("primary_link");
				Uri url = Uri.parse(link);
				Intent i = new Intent(Intent.ACTION_VIEW, url);
				activity.startActivity(i);
			}
			
		});

		// buttons
		
		Button message = (Button) header.findViewById(R.id.message);
		message.setVisibility(View.VISIBLE);
		message.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				LinearLayout messageLayout = (LinearLayout) inflater.inflate(R.layout.message, null);
				final EditText subject = (EditText) messageLayout.findViewById(R.id.subject);
				final EditText body = (EditText) messageLayout.findViewById(R.id.body);

				new AlertDialog.Builder(activity)
			    .setTitle(R.string.send_message)
			    .setView(messageLayout)
			    .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int whichButton) {
						HashMap<String, Object> data = new HashMap<String, Object>();
						data.put("thread_id", false);
						data.put("subject", subject.getText().toString());
						data.put("recipients", getIntent().getStringExtra("user_id"));
						data.put("content", body.getText().toString());
						BPRequest stream = new BPRequest(activity, mHandler, "bp.sendMessage", data, MSG_MESSAGE);
						stream.execute();
			        }
			    }).setNegativeButton(android.R.string.no, null).show();	
				
			}
		});

		Button close = (Button) header.findViewById(R.id.close);
		close.setVisibility(View.VISIBLE);
		close.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
			    setResult(Activity.RESULT_OK, new Intent());
				finish();
			}
		});

		// add friend button
		
		if(map.containsKey("friendship") && map.get("friendship") instanceof String) {
			Button friend = (Button) header.findViewById(R.id.friend);
			LinearLayout friendl = (LinearLayout) header.findViewById(R.id.friend_l);
			
			String friendship = (String) map.get("friendship");
			Log.d(TAG,"Friendship: "+friendship);
			
			if(friendship.equals("is_friend")) {
				friend.setText(getString(R.string.unfriend));
				friend.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(activity, BPUserActivity.class);
						intent.putExtra("action", "unfriend");
						intent.putExtra("action_id", getIntent().getStringExtra("user_id"));
						intent.putExtra("user_id", getIntent().getStringExtra("user_id"));
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						activity.startActivity(intent);
					}
					
				});
			}
			else if(friendship.equals("not_friends")) {
				friend.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(activity, BPUserActivity.class);
						intent.putExtra("action", "friend");
						intent.putExtra("action_id", getIntent().getStringExtra("user_id"));
						intent.putExtra("user_id", getIntent().getStringExtra("user_id"));
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						activity.startActivity(intent);
					}
					
				});
			}
			else { // request pending
				friend.setText(getString(R.string.friending));
				friend.setEnabled(false);
			}
			friendl.setVisibility(View.VISIBLE);
			
		}

		// add admin button
		
		if(map.containsKey("can_delete_user") && (Boolean)map.get("can_delete_user")) { 
			LinearLayout admin = (LinearLayout) header.findViewById(R.id.admin);
			admin.setVisibility(View.VISIBLE);
			Button delete = (Button) admin.findViewById(R.id.delete); 
			delete.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {

			        new AlertDialog.Builder(activity)
			        .setIcon(android.R.drawable.ic_dialog_alert)
			        .setTitle(R.string.delete)
			        .setMessage(R.string.really_delete_user)
			        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

			            @Override
			            public void onClick(DialogInterface dialog, int which) {
							HashMap<String, Object> data = new HashMap<String, Object>();
							data.put("user_id", getIntent().getStringExtra("user_id"));
							
							BPRequest stream = new BPRequest(activity, mHandler, "bp.deleteMember", data, MSG_DELETE);
							stream.execute();
						}

			        })
			        .setNegativeButton(android.R.string.no, null)
			        .show();	

					
					
				}
				
			});
		}
		
		
		listView.addView(header);
		
		list = (Object[]) obj;
		
		for(Object gobj : list) {
			HashMap<?, ?> group = (HashMap<?, ?>) gobj;
			TextView gtitle = (TextView) inflater.inflate(R.layout.user_group_title, null);
			gtitle.setText((CharSequence) group.get("label"));
			listView.addView(gtitle);
			Object[] fields = (Object[]) group.get("fields");
			for(Object fobj : fields) {
				HashMap<?, ?> field = (HashMap<?, ?>) fobj;
				LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.user_entry, null);
				TextView label = (TextView) ll.findViewById(R.id.user_entry_label);
				TextView value = (TextView) ll.findViewById(R.id.user_entry_value);
				
				label.setText((CharSequence) field.get("label"));
				if(field.get("value") instanceof String) {
					String vs = (String) field.get("value");
					vs = vs.replaceAll("</*p>", "");
					value.setText(Html.fromHtml(vs));
					value.setMovementMethod(LinkMovementMethod.getInstance());
				}
				listView.addView(ll);
			}
		}		
	}
	
}
