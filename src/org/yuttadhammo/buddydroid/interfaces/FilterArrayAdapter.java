package org.yuttadhammo.buddydroid.interfaces;

import java.util.List;

import org.yuttadhammo.buddydroid.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FilterArrayAdapter<T> extends ArrayAdapter<String> {

	private Activity activity;
	
	public FilterArrayAdapter(Activity _activity, List<String> strings) {
		super(_activity, 0, strings);
		activity = _activity;
	}
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = activity.getLayoutInflater();
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);

		// Inflate the views from XML
		View rowView = inflater.inflate(R.layout.filter_item, null);
		final String filter = (String) getItem(position);
		TextView title = (TextView) rowView.findViewById(R.id.title);
		title.setText(filter);
		return rowView;
	}
}
