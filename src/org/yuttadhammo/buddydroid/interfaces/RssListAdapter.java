package org.yuttadhammo.buddydroid.interfaces;

import org.yuttadhammo.buddydroid.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RssListAdapter extends ArrayAdapter<Object> {

	private Activity activity;


	public RssListAdapter(Activity activity, Object[] rss) {
		super(activity, 0, rss);
		this.activity = activity;
	}


	@SuppressLint("NewApi")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final Activity activity = (Activity) getContext();
		LayoutInflater inflater = activity.getLayoutInflater();

		// Inflate the views from XML
		View rowView = inflater.inflate(R.layout.stream_item_layout, null);
		final HashMap<?,?> entryMap = (HashMap<?, ?>) getItem(position);
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////
		//The next section we update at runtime the text - as provided by the JSON from our REST call
		////////////////////////////////////////////////////////////////////////////////////////////////////
		TextView textView = (TextView) rowView.findViewById(R.id.job_text);
		WebView wv = (WebView) rowView.findViewById(R.id.feed_image);
        try {
        	
        	String text = (String)entryMap.get("content");
        	String title = (String)entryMap.get("action");
        	
        	title = title.replace("posted an update", "posted an <a href=\""+((String) entryMap.get("primary_link"))+"\">update</a>");
        	
        	String dates = (String)entryMap.get("date_recorded");
        	
        	String imgurl = (String)entryMap.get("user_avatar");
        	wv.loadData(imgurl, "text/html", "UTF-8");
        	
        	//2013-03-11 20:32:01
        	
        	Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH).parse(dates);
        	DateFormat df = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.ENGLISH);
        	
        	Spanned out = Html.fromHtml("<b>"+title+(text.length() > 0?":</b><br/><br/>"+text:"</b>")+"<br/><br/><i>"+df.format(date)+"</i>");
        	
        	textView.setText(out);
    		textView.setMovementMethod(LinkMovementMethod.getInstance());


        }
        catch (Exception e) {
        	e.printStackTrace();
        }
        textView.setTextColor(0xFF000000);
		return rowView;

	} 

}