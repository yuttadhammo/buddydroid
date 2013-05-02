package org.yuttadhammo.buddydroid.interfaces;

import org.yuttadhammo.buddydroid.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.AlignmentSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MessageListAdapter extends ArrayAdapter<Object> {

	protected String TAG = "MessageListAdapter";
	public SparseIntArray expanded = new SparseIntArray();
	
	public MessageListAdapter(Activity activity, Object[] rss) {
		super(activity, 0, rss);
	}


	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		final Activity activity = (Activity) getContext();
		LayoutInflater inflater = activity.getLayoutInflater();
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);

		// Inflate the views from XML
		View rowView = inflater.inflate(R.layout.message_item, null);
		final HashMap<?,?> entryMap = (HashMap<?, ?>) getItem(position);
		
		TextView titleView = (TextView) rowView.findViewById(R.id.sender);
		TextView subjectView = (TextView) rowView.findViewById(R.id.subject);
		TextView textView = (TextView) rowView.findViewById(R.id.text);
		TextView dateView = (TextView) rowView.findViewById(R.id.date);
        try {
        	String text =  sanitizeText((String) entryMap.get("message"));
        	String title = sanitizeText((String)entryMap.get("from"));
        	String subject = sanitizeText((String)entryMap.get("subject"));
        	
        	String dates = (String)entryMap.get("date_sent");

        	int unread = Integer.parseInt((String) entryMap.get("unread_count"));
        	
        	//Log.d(TAG,title+" "+subject+" ");
        	
        	// add text
        	
        	textView.setText(text);

        	// add sender to title
        	
        	titleView.setText(title);

        	// add subject to subtitle
        	
        	subjectView.setText(subject);

        	// add date
        	//2013-03-11 20:32:01
        	
        	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        	simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        	Date date = simpleDateFormat.parse(dates);
        	
        	
        	if(prefs.getBoolean("relative_date",true)) {
            	CharSequence dateString = DateUtils.getRelativeTimeSpanString(date.getTime(), new Date().getTime(), DateUtils.SECOND_IN_MILLIS);      	
            	dateView.setText(dateString);
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
				
        		dateView.setText(df.format(date));
        	}
        	
        	if(unread == 0)
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