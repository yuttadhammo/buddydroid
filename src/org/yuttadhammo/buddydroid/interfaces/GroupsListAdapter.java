package org.yuttadhammo.buddydroid.interfaces;

import java.util.HashMap;
import org.yuttadhammo.buddydroid.R;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GroupsListAdapter extends ArrayAdapter<Object> {

	protected String TAG = "GroupsListAdapter";
	public SparseIntArray expanded = new SparseIntArray();
	private Activity activity;
	
	public GroupsListAdapter(Activity _activity, Object[] rss) {
		super(_activity, 0, rss);
		activity = _activity;
	}


	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = activity.getLayoutInflater();
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);

		// Inflate the views from XML
		View rowView = inflater.inflate(R.layout.group_item, null);
		final HashMap<?,?> entryMap = (HashMap<?, ?>) getItem(position);
		
		TextView titleView = (TextView) rowView.findViewById(R.id.title);
		TextView descView = (TextView) rowView.findViewById(R.id.description);
		TextView statusView = (TextView) rowView.findViewById(R.id.status);
		TextView membersView = (TextView) rowView.findViewById(R.id.members);
        try {
        	String text =  sanitizeText((String) entryMap.get("description"));
        	String title = sanitizeText((String)entryMap.get("name"));
        	String status = sanitizeText((String)entryMap.get("status"));
        	String count = (String)entryMap.get("total_member_count");
        	Boolean is_member = (Boolean)entryMap.get("is_member");

        	// add text
        	
        	descView.setText(text);
        	titleView.setText(title);
        	statusView.setText(status);

        	if(entryMap.containsKey("avatar")) {
        		HashMap<?,?> avatars = (HashMap<?, ?>) entryMap.get("avatar");
        		String imgurl = (String)avatars.get("full");
            	ImageView iv = (ImageView) rowView.findViewById(R.id.feed_image);
            	UrlImageViewHelper.setUrlDrawable(iv, imgurl);
        	}
        	
        	if(count.equals("1"))
        		membersView.setText(getContext().getString(R.string.one_member));
        	else
        		membersView.setText(String.format(getContext().getString(R.string.x_members),count));
        	
        	if(!is_member)
        		rowView.setBackgroundColor(0xFFEEEEEE);
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
		return rowView;

	}
	private String sanitizeText(String string) {
		string = string.replace("\\\"", "\"").replace("\\'", "'");
		return string;
	} 
}
