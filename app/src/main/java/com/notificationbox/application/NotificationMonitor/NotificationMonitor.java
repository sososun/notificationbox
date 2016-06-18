package com.notificationbox.application.NotificationMonitor;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.notificationbox.application.BaseContact;
import com.notificationbox.application.NotificationBoxMainActivity;
import com.notificationbox.application.R;
import com.notificationbox.application.app.AppAdapter;
import com.notificationbox.application.db.NotificationCancelListHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NotificationMonitor extends NotificationListenerService {
    private String notificationTitle;
    private String notificationText;
    private String notificationSubText;
    private String notificationtime;
    private Bitmap notificationLargeIcon;
    private Bitmap notificationSmallIcon;
    private String notificationAppName;
    private static final String TAG = "SevenNLS";
    private static final String TAG_PRE = "[" + NotificationMonitor.class.getSimpleName() + "] ";
    public static final String ACTION_NLS_CONTROL = "com.seven.notificationlistenerdemo.NLSCONTROL";
    public static int mCurrentNotificationsCounts = 0;
    public static  ArrayList<CharSequence> arrayList = new ArrayList<CharSequence>();
    public static StatusBarNotification mPostedNotification;
    public static StatusBarNotification mRemovedNotification;
    NotificationCancelListHelper notificationcancellisthelper = new NotificationCancelListHelper(this, NotificationCancelListHelper.TABLENAME, null, 1);
    
    @Override
    public void onCreate() {
        super.onCreate();
        logNLS("onCreate...");
        Log.i("SevenNLS", "调用onCreate");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NLS_CONTROL);
//        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(mReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        logNLS("onBind...");
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i("SevenNLS", "调用onNotificationPosted");
        onNotificationData(sbn);
    }
    
    private void onNotificationData(StatusBarNotification sbn){
//      updateCurrentNotifications();
      logNLS("onNotificationPosted...");
      logNLS("have " + mCurrentNotificationsCounts + " active notifications");
      mPostedNotification = sbn;
//      sbn.getNotification().extras.getString(Notification.);
      Bundle extras = sbn.getNotification().extras; 
      onNotificationInfo(extras,sbn);
      onNotificationCancel(sbn);
      
    }
    private void onNotificationInfo(Bundle extras,StatusBarNotification sbn){
        
        notificationTitle = extras.getString(Notification.EXTRA_TITLE);
        notificationLargeIcon = ((Bitmap)extras.getParcelable(Notification.EXTRA_LARGE_ICON)); 
        notificationSmallIcon = ((Bitmap)extras.getParcelable(Notification.EXTRA_SMALL_ICON)); 
        notificationText = extras.getString(Notification.EXTRA_TEXT);
        notificationSubText = extras.getString(Notification.EXTRA_SUB_TEXT);
        notificationtime = getTime(sbn.getPostTime());
        notificationAppName = getProgramNameByPackageName(this,sbn.getPackageName());
        Log.i("SevenNLS", "notificationTitle:"+notificationTitle);
        Log.i("SevenNLS", "notificationText:"+notificationText);
        Log.i("SevenNLS", "notificationSubText:"+notificationSubText);
        Log.i("SevenNLS", "time:"+notificationtime);
        Log.i("SevenNLS", "Appname:"+notificationAppName);
        NotificationInfo.getInstance().setIcon(notificationLargeIcon);
        NotificationInfo.getInstance().setTitle(notificationTitle);
        NotificationInfo.getInstance().setText(notificationText);
        NotificationInfo.getInstance().setSubtext(notificationSubText);
        NotificationInfo.getInstance().setTime(notificationtime);
        NotificationInfo.getInstance().setAppname(notificationAppName);
    }
    
    private void onNotificationCancel(StatusBarNotification sbn){
  
        for (int i = 0; i < BaseContact.cancellist.size(); i++) {
            if (sbn.getPackageName().equals(BaseContact.cancellist.get(i))) {
                if (notificationTitle != null && notificationText != null) {
                    Log.e("SOSO", "调用" + arrayList.toString());
                    Log.i("SevenNLScancel", "notificationTitle:" + notificationTitle);
                    Log.i("SevenNLScancel", "notificationText:" + notificationText);
                    Log.i("SevenNLScancel", "notificationSubText:" + notificationSubText);
                    Log.i("SevenNLScancel", "time:" + notificationtime);
                    arrayList.add(notificationTitle);
                    notificationcancellisthelper.insertDB();
                    if(android.os.Build.VERSION.SDK_INT > 20){
                        String key=sbn.getKey();
                        cancelNotification(key);
                    }else {
                        cancelNotification(sbn.getPackageName(), sbn.getTag(), sbn.getId());
                    }
                    createOngoingNotifications();
                }
            }
        }
    }
    private void createOngoingNotifications(){
        Notification.Builder ncBuilder = new Notification.Builder(this);
        ncBuilder.setContentTitle("NotificationBox");
        ncBuilder.setContentText("已收起"+ NotificationCancelListHelper.getInstance(getApplicationContext()).queryDBquantity()+"个消息");
//    ncBuilder.setTicker("Notification Listener Service Example");
        ncBuilder.setSmallIcon(R.mipmap.ic_launcher);
        ncBuilder.setOngoing(true);
        ncBuilder.setNumber(10);
//    ncBuilder.setSound(Uri.withAppendedPath(Audio.Media.INTERNAL_CONTENT_URI, "6"));
//        ncBuilder.setDefaults(100);
        ncBuilder.setAutoCancel(false);
//        ncBuilder.setLatestEventInfo(this, null, null, pendingIntent);
//        startForeground(100, ncBuilder);
        Intent notifyIntent = new Intent(this, NotificationBoxMainActivity.class);
//        notifyIntent.putExtra("11", notificationTitle);
        int requestCode = (int) SystemClock.uptimeMillis();
        PendingIntent pendIntent = PendingIntent.getActivity(this, requestCode,
                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        ncBuilder.setContentIntent(pendIntent);
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        manager.notify(BaseContact.NOTIFICATION_ID,ncBuilder.build());
    }
    public static ArrayList<CharSequence> GetData(){
        return arrayList;
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
//        updateCurrentNotifications();
        logNLS("removed...");
        logNLS("have " + mCurrentNotificationsCounts + " active notifications");
        mRemovedNotification = sbn;
    }
    
   

//    private void updateCurrentNotifications() {
//        try {
//            StatusBarNotification[] activeNos = getActiveNotifications();
//            if (mCurrentNotifications.size() == 0) {
//                mCurrentNotifications.add(null);
//            }
//            mCurrentNotifications.set(0, activeNos);
//            mCurrentNotificationsCounts = activeNos.length;
//        } catch (Exception e) {
//            logNLS("Should not be here!!");
//            e.printStackTrace();
//        }
//    }

//    public static StatusBarNotification[] getCurrentNotifications() {
//        if (mCurrentNotifications.size() == 0) {
//            logNLS("mCurrentNotifications size is ZERO!!");
//            return null;
//        }
//        return mCurrentNotifications.get(0);
//    }

    private static void logNLS(Object object) {
        Log.i(TAG, TAG_PRE + object);
    }
//    public void cancelNotification(Context context, boolean isCancelAll) {
//        Intent intent = new Intent();
//        intent.setAction(NotificationMonitor.ACTION_NLS_CONTROL);
//        if (isCancelAll) {
//            intent.putExtra("command", "cancel_all");
//        }else {
//            intent.putExtra("command", "cancel_last");
//        }
//        context.sendBroadcast(intent);
//    }
    /**
     * 获取今天日期
     * @return
     */
    public static String getTime(Long i) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(i);// 获取当前时间
        String str = formatter.format(curDate);
        return str;
    }
    


        /**
         * 通过包名获取应用程序的名称。
         * @param context
         *            Context对象。
         * @param packageName
         *            包名。
         * @return 返回包名所对应的应用程序的名称。
         */
        public static String getProgramNameByPackageName(Context context,
                String packageName) {
            PackageManager pm = context.getPackageManager();
            String name = null;
            try {
                name = pm.getApplicationLabel(
                        pm.getApplicationInfo(packageName,
                                PackageManager.GET_META_DATA)).toString();
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            return name;
        }
    
}
