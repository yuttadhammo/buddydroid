package org.yuttadhammo.buddydroid.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

import org.yuttadhammo.buddydroid.Buddypress;
import org.yuttadhammo.buddydroid.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Html;
import android.util.Log;

public class NoticeReceiver extends BroadcastReceiver {
	private static final int NOTIFY_ME_ID=1337;
	private Context context;
	private String TAG = "NoticeReceiver";
	protected SharedPreferences prefs;

	@Override
	public void onReceive(Context c, Intent intent) {
		this.context = c;
		Log.i(TAG,"notification sync");
		HashMap<String, Object> data = new HashMap<String, Object>();
    	BPRequest bpr = new BPRequest(context, nHandler, "bp.getNotifications", data, 0);
    	bpr.execute();
	}
	
    
	/** Handler for the message from the timer service */
	private Handler nHandler = new Handler() {
		

		@Override
        public void handleMessage(Message msg) {
			Log.i(TAG  ,"got message");

			switch(msg.what) {
				case 0 :
					NotificationManager mNotificationManager =
				    	(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

					if(!(msg.obj instanceof HashMap)) {
						mNotificationManager.cancelAll();
						return;
					}
					
					HashMap<?, ?> map = (HashMap<?, ?>) msg.obj;
					Object obj = map.get("message");
					
					if(obj instanceof Object[] && !(((Object[])obj)[0] instanceof Boolean)) {
						Object[] nfo = (Object[]) obj;
						int nfn = nfo.length;
						ArrayList<String> notificationLinks = new ArrayList<String>();
						ArrayList<String> notificationStrings = new ArrayList<String>();
						for(Object anf : nfo){
							notificationLinks.add(((String) anf).replaceFirst("^<a href=\"([^\"]*)\".*", "$1"));
							notificationStrings.add(Html.fromHtml((String) anf).toString());
						}
						
						if (nfn == 0) // shouldn't happen
							return;
						
						// create our notification
						
						Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.status_icon_large);
						
						NotificationCompat.Builder mBuilder =
						        new NotificationCompat.Builder(context)
						        .setSmallIcon(R.drawable.status_icon)
						        .setLargeIcon(bmp)
						        .setAutoCancel(true)
						        .setContentTitle(nfn == 1?context.getString(R.string.notification_title_1):String.format(context.getString(R.string.notification_title), nfn))
						        .setContentText(context.getString(R.string.notification_text))
						        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
						        .setContentInfo(Integer.toString(nfn));
						
						NotificationCompat.InboxStyle inboxStyle =
						        new NotificationCompat.InboxStyle();
						
						// Sets a title for the Inbox style big view
						inboxStyle.setBigContentTitle(nfn == 1?context.getString(R.string.notification_title_1):String.format(context.getString(R.string.notification_title), nfn));
						// Moves events into the big view
						for (String ans: notificationStrings) {
							Log.i(TAG,"notification: "+ans);
						    inboxStyle.addLine(ans);
						}
						// Moves the big view style object into the notification object.
						mBuilder.setStyle(inboxStyle);
						
						// Creates an explicit intent for an Activity in your app
						Intent resultIntent = new Intent(context, Buddypress.class);
						resultIntent.putExtra("notification", true);
						
						// The stack builder object will contain an artificial back stack for the
						// started Activity.
						// This ensures that navigating backward from the Activity leads out of
						// your application to the Home screen.
						TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
						// Adds the back stack for the Intent (but not the Intent itself)
						stackBuilder.addParentStack(Buddypress.class);
						// Adds the Intent that starts the Activity to the top of the stack
						stackBuilder.addNextIntent(resultIntent);
						PendingIntent resultPendingIntent =
						        stackBuilder.getPendingIntent(
						            0,
						            PendingIntent.FLAG_UPDATE_CURRENT
						        );

						mBuilder.setContentIntent(resultPendingIntent);
						Notification notification = mBuilder.build();
						notification.flags |= Notification.FLAG_AUTO_CANCEL;

						// mId allows you to update the notification later on.
						mNotificationManager.notify(Buddypress.NOTIFY_ID, notification);
						
					}
					else {
						mNotificationManager.cancelAll();
					}
					
					break;
				default: 
					break;
			}
		}
    };

}