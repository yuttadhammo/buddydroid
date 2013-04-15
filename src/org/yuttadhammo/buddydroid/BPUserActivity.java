package org.yuttadhammo.buddydroid;

import java.util.HashMap;
import org.yuttadhammo.buddydroid.interfaces.BPRequest;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class BPUserActivity extends Activity {

	protected String TAG = "BPUserActivity";
	private BPUserActivity activity;
	private LinearLayout listView;
	public static final int MSG_USER = 4;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user);

		listView = (LinearLayout) findViewById(R.id.user_list);

		activity = this;
		
		HashMap<String, Object> data = new HashMap<String, Object>();

		if(getIntent().hasExtra("user_id")) { // stream click
			Log.d(TAG,"getting user id " + getIntent().getStringExtra("user_id"));
			data.put("user_id", getIntent().getStringExtra("user_id"));
			
			BPRequest stream = new BPRequest(this, mHandler, "bp.getMemberInfo", data, MSG_USER);
			stream.execute();

		}
			
	}

	/** Handler for the message from the timer service */
	private Handler mHandler = new Handler() {
		
		@Override
        public void handleMessage(Message msg) {
			Log.i(TAG ,"got message");

			HashMap<?, ?> map;
			Object obj;
			Object[] list;
			LayoutInflater inflater = activity.getLayoutInflater();
			
			String toast = null;
			
			boolean fromNotify = getIntent().hasExtra("notification");
			if(fromNotify)
				getIntent().removeExtra("notification");
			
			switch(msg.what) {
				case MSG_USER:
					if(!(msg.obj instanceof HashMap)) 
						break;
					
					map = (HashMap<?, ?>) msg.obj;
					obj = map.get("profile_groups");
					
					if(!(obj instanceof Object[])) 
						break;

					LinearLayout header = (LinearLayout) inflater.inflate(R.layout.user_header, null);
					TextView title = (TextView) header.findViewById(R.id.user_name);
					TextView login = (TextView) header.findViewById(R.id.user_login);
					TextView active = (TextView) header.findViewById(R.id.user_active);
					TextView status = (TextView) header.findViewById(R.id.user_activity);

					title.setText(String.format(getString(R.string.sprofile),(String) map.get("display_name")));
					login.setText("@"+(String) map.get("user_nicename"));
					active.setText((String) map.get("last_active"));
					status.setText(Html.fromHtml((String) map.get("last_status")));

					ImageView iv = (ImageView) header.findViewById(R.id.user_avatar);
					HashMap<?,?> avatars = (HashMap<?, ?>) map.get("avatar");
					String imgurl = (String) avatars.get("full");
					UrlImageViewHelper.setUrlDrawable(iv, imgurl);
					
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
							String vs = (String) field.get("value");
							vs = vs.replaceAll("</*p>", "");
							value.setText(Html.fromHtml(vs));
							listView.addView(ll);
						}
					}
					
					
					break;
				default: 
					if(msg.obj instanceof String)
						toast = (String) msg.obj;
					else
						toast = getString(R.string.error);
					break;
			}
			
			if(toast != null)
				Toast.makeText(activity, (CharSequence) toast, Toast.LENGTH_LONG).show();

			
		}
    };
	
}
