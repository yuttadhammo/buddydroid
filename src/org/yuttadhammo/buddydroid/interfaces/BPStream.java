package org.yuttadhammo.buddydroid.interfaces;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;
import org.yuttadhammo.buddydroid.Buddypress;
import org.yuttadhammo.buddydroid.R;
import org.yuttadhammo.buddydroid.R.string;
import org.yuttadhammo.buddydroid.interfaces.BPStatus.uploadStatusTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class BPStream {


	public Vector<String> imageUrl = new Vector<String>();
	Vector<String> selectedCategories = new Vector<String>();
	public static Object error;
	public static Activity activity;
	public static String TAG = "BPStream";
	private static Handler handler;
	private static String scope;
	private static int max;


	public BPStream(Activity atv, Handler h, String ascope, int amax) {
		handler = h;
		activity = atv;
		scope = ascope;
		max = amax;
	}

	public void get() {

		new getStreamTask().execute(this);

	}
	
	private static class getStreamTask extends
			AsyncTask<BPStream, Boolean, Boolean> {

		private boolean success = false;
		private Object rss;

		@Override
		protected void onPostExecute(Boolean result) {

			Message msg = new Message();
			if (success) {
				msg.arg1 = R.string.updated;
				msg.obj = rss;
				msg.what = Buddypress.MSG_STREAM;
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
		protected Boolean doInBackground(BPStream... streams) {

			Object[] params;

			Map<String, Object> data = new HashMap<String, Object>();
			data.put("scope", scope);
			data.put("max", max);

			params = new Object[] { Buddypress.getUsername(),
					Buddypress.getServiceName(),
					Buddypress.getApiKey(), data };
			Log.i(TAG , "params: " + params[0]+" | "+params[1]+" | "+params[2]);
			Log.i(TAG , "data: " + data.get("scope")+" | "+data.get("max"));
			XMLRPCClient client = new XMLRPCClient(Buddypress.getUrl(),
					Buddypress.getHttpuser(), Buddypress.getHttppassword());
			try {
				rss = client.call("bp.getActivity", params);
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

	public void setScope(String _scope) {
		scope = _scope;
	}

}
