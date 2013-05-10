package org.yuttadhammo.buddydroid.interfaces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.yuttadhammo.buddydroid.R;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class FiltersExpandableListAdapter implements ExpandableListAdapter {

	private ArrayList<Integer> filters;
	private Activity activity;
	private ArrayList<String> activities;

	public FiltersExpandableListAdapter(Activity _activity, ArrayList<Integer> _filters, ArrayList<String> _activities) {
		super();
		activity = _activity;
		filters = _filters;
		activities = _activities;
	}
	
	@Override
	public boolean areAllItemsEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		int id = filters.get(groupPosition);
		String text = "";
		switch(id) {
			case R.string.activity:
				text = activities.get(childPosition);
				break;
			case R.string.friends:
				text = activity.getResources().getStringArray(R.array.friends_filters)[childPosition];
				break;
			case R.string.groups:
				text = activity.getResources().getStringArray(R.array.group_filters)[childPosition];
				break;
			case R.string.messages:
				text = activity.getResources().getStringArray(R.array.message_filters)[childPosition];
				break;
		}
		return text;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		LayoutInflater inflater = activity.getLayoutInflater();
		TextView tv = (TextView) inflater.inflate(R.layout.filter_child_item, null);
        tv.setText(getChild(groupPosition, childPosition).toString());
        return tv;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		int id = filters.get(groupPosition);
		int count = 0;
		switch(id) {
			case R.string.activity:
				count = activities.size();
				break;
			case R.string.friends:
				count = activity.getResources().getStringArray(R.array.friends_filters).length;
				break;
			case R.string.groups:
				count = activity.getResources().getStringArray(R.array.group_filters).length;
				break;
			case R.string.messages:
				count = activity.getResources().getStringArray(R.array.message_filters).length;
				break;
		}
		return count;
	}

	@Override
	public long getCombinedChildId(long groupId, long childId) {
		// TODO Auto-generated method stub
		return childId;
	}

	@Override
	public long getCombinedGroupId(long groupId) {
		// TODO Auto-generated method stub
		return groupId;
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return activity.getString(filters.get(groupPosition));
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return filters.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		LayoutInflater inflater = activity.getLayoutInflater();
		TextView tv = (TextView) inflater.inflate(R.layout.filter_item, null);
        tv.setText(getGroup(groupPosition).toString());
        
        Drawable left = null;
        switch(filters.get(groupPosition)) {
			case R.string.activity:
				left = activity.getResources().getDrawable(R.drawable.icon_rss);
				break;
			case R.string.friends:
				left = activity.getResources().getDrawable(R.drawable.icon_friends);
				break;
			case R.string.groups:
				left = activity.getResources().getDrawable(R.drawable.icon_groups);
				break;
			case R.string.messages:
				left = activity.getResources().getDrawable(R.drawable.icon_email);
				break;

        }
        left.setBounds(-12,2,12,26);
        tv.setCompoundDrawables(left, null,null,null);
		return tv;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onGroupCollapsed(int groupPosition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGroupExpanded(int groupPosition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub

	}
}
