package org.yuttadhammo.buddydroid.interfaces;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class NotifyStarter extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent i) {
		final Intent intent = new Intent("org.yuttadhammo.buddydroid.SYNC");
		final PendingIntent pending = PendingIntent.getService(context, 0, intent, 0);
		final AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarm.cancel(pending);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if(!prefs.getBoolean("interval_sync", false))
			return;
		
		long interval = Long.parseLong(prefs.getString("sync_interval", "60"));
		interval = interval*1000*60;
		
		alarm.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),interval, pending);

	}

}
