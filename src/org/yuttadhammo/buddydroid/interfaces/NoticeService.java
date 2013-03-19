package org.yuttadhammo.buddydroid.interfaces;

import android.app.IntentService;
import android.content.Intent;

public class NoticeService extends IntentService {
  public static final String BROADCAST=
    "org.yuttadhammo.buddydroid.interfaces.NoticeService.BROADCAST";
  private static Intent broadcast=new Intent(BROADCAST);
  
  public NoticeService() {
    super("NoticeService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    sendOrderedBroadcast(broadcast, null);
  }
}