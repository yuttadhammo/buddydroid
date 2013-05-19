package org.yuttadhammo.buddydroid.interfaces;

import java.util.HashMap;

import org.yuttadhammo.buddydroid.Buddypress;
import org.yuttadhammo.buddydroid.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
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
			Drawable left = activity.getResources().getDrawable(R.drawable.icon_rss);
			
			if(component.equals("messages")) {
				left = activity.getResources().getDrawable(R.drawable.icon_email);
			}
			else if(component.equals("groups")) {
				left = activity.getResources().getDrawable(R.drawable.icon_groups);
			}
			else if(component.equals("friends")) {
				left = activity.getResources().getDrawable(R.drawable.icon_friends);
			}
			
			left.setBounds(0, 0, 32, 32);
			
			tv.setCompoundDrawables(left, null,null,null);
			return rowView;
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}

	}


}
