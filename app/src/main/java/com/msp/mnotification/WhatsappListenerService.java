package com.msp.mnotification;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class WhatsappListenerService extends NotificationListenerService {

   private static final class ApplicationPackageNames {
      public static final String FACEBOOK_PACK_NAME = "com.facebook.katana";
      public static final String FACEBOOK_MESSENGER_PACK_NAME = "com.facebook.orca";
      public static final String WHATSAPP_PACK_NAME = "com.whatsapp";
      public static final String INSTAGRAM_PACK_NAME = "com.instagram.android";
   }

   public static final class InterceptedNotificationCode {
      public static final int FACEBOOK_CODE = 1;
      public static final int WHATSAPP_CODE = 2;
      public static final int INSTAGRAM_CODE = 3;
      public static final int OTHER_NOTIFICATIONS_CODE = 4; // игнорируем все уведомления с кодом == 4
   }

   @Override
   public IBinder onBind(Intent intent) {
      return super.onBind(intent);
   }

   @Override
   public void onNotificationPosted(StatusBarNotification sbn){
      int notificationCode = matchNotificationCode(sbn);
//      Notification sbnNotification = sbn.getNotification();
//      String str = String.valueOf(sbnNotification.extras.getCharSequence(Notification.EXTRA_TITLE));
//      Notification.EXTRA_TITLE  имя отправителя сообщения
//      String str = String.valueOf(sbnNotification.extras.getCharSequence(Notification.EXTRA_MESSAGES));
      String str = sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT).toString();

      if(notificationCode != InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE){
         Intent intent = new  Intent("com.msp.mnotification");
         intent.putExtra("Notification Code", notificationCode);
         intent.putExtra("Message", str);
         sendBroadcast(intent);
      }
   }

   @Override
   public void onNotificationRemoved(StatusBarNotification sbn){
      int notificationCode = matchNotificationCode(sbn);

      if(notificationCode != InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE) {

         StatusBarNotification[] activeNotifications = this.getActiveNotifications();

         if(activeNotifications != null && activeNotifications.length > 0) {
            for (int i = 0; i < activeNotifications.length; i++) {
               if (notificationCode == matchNotificationCode(activeNotifications[i])) {
                  Intent intent = new  Intent("com.msp.mnotification");
                  intent.putExtra("Notification Code", notificationCode);
                  sendBroadcast(intent);
                  break;
               }
            }
         }
      }
   }

   private int matchNotificationCode(StatusBarNotification sbn) {
      String packageName = sbn.getPackageName();

      if(packageName.equals(ApplicationPackageNames.FACEBOOK_PACK_NAME)
              || packageName.equals(ApplicationPackageNames.FACEBOOK_MESSENGER_PACK_NAME)){
         return(InterceptedNotificationCode.FACEBOOK_CODE);
      }
      else if(packageName.equals(ApplicationPackageNames.INSTAGRAM_PACK_NAME)){
         return(InterceptedNotificationCode.INSTAGRAM_CODE);
      }
      else if(packageName.equals(ApplicationPackageNames.WHATSAPP_PACK_NAME)){
         return(InterceptedNotificationCode.WHATSAPP_CODE);
      }
      else{
         return(InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE);
      }
   }
}