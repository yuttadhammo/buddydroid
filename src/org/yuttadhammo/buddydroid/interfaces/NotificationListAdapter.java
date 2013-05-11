package org.yuttadhammo.buddydroid.interfaces;

import java.util.HashMap;

import org.yuttadhammo.buddydroid.Buddypress;
import org.yuttadhammo.buddydroid.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class NotificationListAdapter extends ArrayAdapter<Object> {

	protected String TAG = "NotificationListAdapter";
	private Object[] nfs;
	private Handler handler;
	
	public NotificationListAdapter(Activity activity, Object[] rss, Handler mHandler) {
		super(activity, 0, rss);
		handler = mHandler;
	}


	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		final Activity activity = (Activity) getContext();
		LayoutInflater inflater = activity.getLayoutInflater();
		final HashMap<?,?> entryMap = (HashMap<?, ?>) getItem(position);

		// Inflate the views from XML
		View rowView = inflater.inflate(R.layout.notification_item, null);
		
		try{
			TextView tv = (TextView) rowView.findViewById(R.id.text);
		
			tv.setText((CharSequence) entryMap.get("content"));
			
			String component = (String) entryMap.get("component");
			String action = (String) entryMap.get("action");
			String ascope = (String) entryMap.get("href");
			
			if(component.equals("messages"))
				ascope = "messages";
			else if(component.equals("groups")) {
				if(action.equals("membership_request_rejected"))
					ascope = "groups_groups";
				else if(!action.equals("new_membership_request"))
					ascope = "groups_my_groups";
			}
			else if(component.equals("activity") && action.equals("new_at_mention"))
				ascope = "mentions";
			else if(component.equals("friends") && action.equals("friendship_request"))
				ascope = "friends_friend_requests";
			else if(component.equals("friends"))
				ascope = "friends_friends";
			
			final String scope = ascope; 
			rowView.setOnClickListener(new OnClickListener() {
	
				@Override
				public void onClick(View v) {
					Message msg = new Message();
					msg.obj = scope;
					msg.what = Buddypress.MSG_SCOPE;
	
					handler.sendMessage(msg);
				}
				
			});
			
			return rowView;
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}

	}


}
