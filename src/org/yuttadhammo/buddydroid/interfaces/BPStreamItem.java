package org.yuttadhammo.buddydroid.interfaces;

import java.util.HashMap;
import java.util.Map;

import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;
import org.yuttadhammo.buddydroid.Buddypress;
import org.yuttadhammo.buddydroid.R;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BPStreamItem {

	private HashMap<?, ?> entryMap;
	private static Activity activity;
	public static String TAG = "BPStreamItem";
	private static Handler handler;
	public static String error;

	public BPStreamItem(Activity atv, Handler h, HashMap<?, ?> entryMap) {
		this.entryMap = entryMap;
		this.activity = atv;
		this.handler = h;
	}

	public void delete() {
		String id = (String)entryMap.get("activity_id");
		DeleteItemTask dit = new DeleteItemTask();
		dit.execute(id);
	}

	private static class DeleteItemTask extends
			AsyncTask<String, Boolean, Boolean> {

		private boolean success = false;
		private Object rss;

		@Override
		protected void onPostExecute(Boolean result) {

			Message msg = new Message();
			if (success) {
				msg.arg1 = R.string.updated;
				msg.obj = rss;
				msg.what = Buddypress.MSG_DELETE;
			}
			else {
				msg.arg1 = R.string.error;
				msg.obj = error;
				msg.what = Buddypress.MSG_ERROR;
			}
			handler.sendMessage(msg);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... ids) {

			String id = ids[0];
			Object[] params;

			Map<String, Object> data = new HashMap<String, Object>();
			data.put("activityid", id);

			params = new Object[] { Buddypress.getUsername(),
					Buddypress.getServiceName(),
					Buddypress.getApiKey(), data };
			Log.i(TAG  , "params: " + params[0]+" | "+params[1]+" | "+params[2]);
			Log.i(TAG , "data: " + data.get("activityid"));
			XMLRPCClient client = new XMLRPCClient(Buddypress.getUrl(),
					Buddypress.getHttpuser(), Buddypress.getHttppassword());
			try {
				rss = client.call("bp.deleteProfileStatus", params);
				success = true;
				return true;
			} catch (final XMLRPCException e) {
				if(e.getCause() != null)
					error = e.getCause().toString();
				else
					error = activity.getString(R.string.error);
				e.printStackTrace();
			}
			return false;
		}

	}

}
