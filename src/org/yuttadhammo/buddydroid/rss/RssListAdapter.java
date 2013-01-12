package org.yuttadhammo.buddydroid.rss;

import org.yuttadhammo.buddydroid.*;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RssListAdapter extends ArrayAdapter<JSONObject> {

	private Activity activity;


	public RssListAdapter(Activity activity, List<JSONObject> imageAndTexts) {
		super(activity, 0, imageAndTexts);
		this.activity = activity;
	}


	@SuppressLint("NewApi")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final Activity activity = (Activity) getContext();
		LayoutInflater inflater = activity.getLayoutInflater();

		// Inflate the views from XML
		View rowView = inflater.inflate(R.layout.image_text_layout, null);
		final JSONObject jsonImageText = getItem(position);
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////
		//The next section we update at runtime the text - as provided by the JSON from our REST call
		////////////////////////////////////////////////////////////////////////////////////////////////////
		TextView textView = (TextView) rowView.findViewById(R.id.job_text);
		
        try {
        	
        	Spanned text = (Spanned)jsonImageText.get("text");
        	textView.setText(text);
    		textView.setOnClickListener(new OnClickListener(){

    			@Override
    			public void onClick(View arg0) {
    				String url = "";
					try {
						url = jsonImageText.getString("link");
	    				Intent i = new Intent(Intent.ACTION_VIEW);
	    				i.setData(Uri.parse(url));
	    				activity.startActivity(i);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    			}
    			
    		});
    		@SuppressWarnings("deprecation")
    		int api = Integer.parseInt(Build.VERSION.SDK);
    		
    		if (api >= 11) {
    			textView.isTextSelectable();
    		}
        }
        catch (JSONException e) {
        	textView.setText("JSON Exception");
        }
        textView.setTextColor(0xFF000000);
		return rowView;

	} 

}