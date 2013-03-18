package org.yuttadhammo.buddydroid.interfaces;

import java.util.HashMap;
import java.util.Vector;
import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;
import org.yuttadhammo.buddydroid.Buddypress;
import org.yuttadhammo.buddydroid.R;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BPRequest {


	public Vector<String> imageUrl = new Vector<String>();
	Vector<String> selectedCategories = new Vector<String>();
	private static String protocol;
	private static int what;
	private static HashMap<String, Object> data;
	public static Object error;
	public static Activity activity;
	public static String TAG = "BPRequest";
	private static Handler handler;



	public BPRequest(Activity atv, Handler h, String _protocol, HashMap<String, Object> _data, int _what) {
		handler = h;
		activity = atv;
		data = _data;
		protocol = _protocol;
		what = _what;
	}

	public void execute() {

		new getStreamTask().execute(this);

	}
	
	private static class getStreamTask extends
			AsyncTask<BPRequest, Boolean, Boolean> {

		private boolean success = false;
		private Object rss;

		@Override
		protected void onPostExecute(Boolean result) {

			Message msg = new Message();
			if (success) {
				msg.arg1 = 0;
				msg.obj = rss;
				msg.what = what;
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
		protected Boolean doInBackground(BPRequest... streams) {
			
			Object[] params;

			params = new Object[] { Buddypress.getUsername(),
					Buddypress.getServiceName(),
					Buddypress.getApiKey(), data };
			Log.i(TAG , "params: " + params[0]+" | "+params[1]+" | "+params[2]);
			Log.i(TAG , "data: " + data.get("scope")+" | "+data.get("max"));
			XMLRPCClient client = new XMLRPCClient(Buddypress.getUrl(),
					Buddypress.getHttpuser(), Buddypress.getHttppassword());
			try {
				rss = client.call(protocol, params);
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
