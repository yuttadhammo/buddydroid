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

public class BPComment {

	private HashMap<?, ?> entryMap;
	private int activityid;
	private String[] comment;
	private static Activity activity;
	public static String TAG = "BPStreamItem";
	private static Handler handler;
	public static String error;

	public BPComment(String aid, String c, Handler h, Activity atv) {
		activity = atv;
		String[] ca = {aid,c};
		comment = ca;
		handler = h;
	}

	public void upload() {
		UploadItemTask uit = new UploadItemTask();
		uit.execute(comment);
	}

	private static class UploadItemTask extends
			AsyncTask<String[], Boolean, Boolean> {

		private boolean success = false;
		private Object json;

		@Override
		protected void onPostExecute(Boolean result) {

			Message msg = new Message();
			if (success) {
				msg.arg1 = R.string.updated;
				msg.obj = json;
				msg.what = Buddypress.MSG_COMMENT;
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
		protected Boolean doInBackground(String[]... comments) {

			String[] comment = comments[0];
			Object[] params;

			Map<String, Object> data = new HashMap<String, Object>();
			data.put("activity_id", comment[0]);
			data.put("comment", comment[1]);

			params = new Object[] { Buddypress.getUsername(),
					Buddypress.getServiceName(),
					Buddypress.getApiKey(), data };
			Log.i(TAG  , "params: " + params[0]+" | "+params[1]+" | "+params[2]);
			Log.i(TAG , "data: " + data.get("activity_id")+" | " + data.get("comment"));
			XMLRPCClient client = new XMLRPCClient(Buddypress.getUrl(),
					Buddypress.getHttpuser(), Buddypress.getHttppassword());
			try {
				json = client.call("bp.postComment", params);
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
