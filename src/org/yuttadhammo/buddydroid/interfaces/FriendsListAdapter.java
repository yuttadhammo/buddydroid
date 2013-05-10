package org.yuttadhammo.buddydroid.interfaces;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import org.yuttadhammo.buddydroid.R;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FriendsListAdapter extends ArrayAdapter<Object> {

	protected String TAG = "GroupsListAdapter";
	public SparseIntArray expanded = new SparseIntArray();
	private Activity activity;
	
	public FriendsListAdapter(Activity _activity, Object[] rss) {
		super(_activity, 0, rss);
		activity = _activity;
	}


	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = activity.getLayoutInflater();
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);

		// Inflate the views from XML
		View rowView = inflater.inflate(R.layout.friend_item, null);
		final HashMap<?,?> entryMap = (HashMap<?, ?>) getItem(position);
		
		TextView titleView = (TextView) rowView.findViewById(R.id.title);
		TextView descView = (TextView) rowView.findViewById(R.id.description);
		TextView dateView = (TextView) rowView.findViewById(R.id.date);
        try {
        	String text =  sanitizeText((String) entryMap.get("latest_update"));
        	String title = sanitizeText((String)entryMap.get("display_name")).replaceFirst(".*\"(.*)\".*", "$1");
        	String dates = (String)entryMap.get("last_activity");

        	// add text
        	
        	descView.setText(text);
        	titleView.setText(title);

        	if(entryMap.containsKey("avatar")) {
        		HashMap<?,?> avatars = (HashMap<?, ?>) entryMap.get("avatar");
        		String imgurl = (String)avatars.get("full");
            	ImageView iv = (ImageView) rowView.findViewById(R.id.feed_image);
            	UrlImageViewHelper.setUrlDrawable(iv, imgurl);
        	}
        	
        	// add date
        	//2013-03-11 20:32:01
        	
        	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        	simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        	Date date = simpleDateFormat.parse(dates);
        	
        	
        	if(prefs.getBoolean("relative_date",true)) {
            	CharSequence dateString = DateUtils.getRelativeTimeSpanString(date.getTime(), new Date().getTime(), DateUtils.SECOND_IN_MILLIS);      	
        		dateView.setText(String.format(activity.getString(R.string.active_x), dateString));
        	}
        	else {
        		
        		// check if today or not
        		
        		Calendar now = Calendar.getInstance();
        		Calendar calendar = Calendar.getInstance();
        		calendar.setTime(date);
        		DateFormat df;
				
        		if(now.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) && now.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) && now.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH))
            		df = android.text.format.DateFormat.getTimeFormat(activity);
        		else 	
        			df = android.text.format.DateFormat.getMediumDateFormat(activity);
				
        		dateView.setText(String.format(activity.getString(R.string.active_x), df.format(date)));
        	}
        	
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
