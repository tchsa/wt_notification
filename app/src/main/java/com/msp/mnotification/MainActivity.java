package com.msp.mnotification;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

   private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
   private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

   private ImageView interceptedNotificationImageView;
   private ImageChangeBroadcastReceiver imageChangeBroadcastReceiver;
   private AlertDialog enableNotificationListenerAlertDialog;
   private TextView inMessage;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      interceptedNotificationImageView = (ImageView) this.findViewById(R.id.intercepted_notification_logo);
      inMessage = (TextView) this.findViewById(R.id.image_change_explanation);

      // Если пользователь не включил службу WhatsappListenerService, предлагаем ему это сделать.
      if(!isNotificationServiceEnabled()){
         enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
         enableNotificationListenerAlertDialog.show();
      }

      // регистрируем BroadcastReceiver, чтобы сообщить MainActivity, когда придет уведомление.
      imageChangeBroadcastReceiver = new ImageChangeBroadcastReceiver();
      IntentFilter intentFilter = new IntentFilter();
      intentFilter.addAction("com.msp.mnotification");
      registerReceiver(imageChangeBroadcastReceiver,intentFilter);
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
      unregisterReceiver(imageChangeBroadcastReceiver);
   }

   // Изменить изображение перехваченного уведомления.
   // Изменяет изображение MainActivity в зависимости от того, какое уведомление было перехвачено
   private void changeInterceptedNotificationImage(int notificationCode, String str){
      switch(notificationCode){
         case WhatsappListenerService.InterceptedNotificationCode.TELEGRAM_CODE:
            interceptedNotificationImageView.setImageResource(R.drawable.facebook_logo);
            break;
         case WhatsappListenerService.InterceptedNotificationCode.INSTAGRAM_CODE:
            interceptedNotificationImageView.setImageResource(R.drawable.instagram_logo);
            break;
         case WhatsappListenerService.InterceptedNotificationCode.WHATSAPP_CODE:
            interceptedNotificationImageView.setImageResource(R.drawable.whatsapp_logo);
            inMessage.setText(str);
            break;
         case WhatsappListenerService.InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE:
            interceptedNotificationImageView.setImageResource(R.drawable.other_notification_logo);
            break;
      }
   }

   /* Включена ли служба уведомлений.
   Проверяет, включена ли служба WhatsappListenerService
   Возвращает True, если включено, иначе false. */
   private boolean isNotificationServiceEnabled(){
      String pkgName = getPackageName();
      final String flat = Settings.Secure.getString(getContentResolver(),
              ENABLED_NOTIFICATION_LISTENERS);
      if (!TextUtils.isEmpty(flat)) {
         final String[] names = flat.split(":");
         for (int i = 0; i < names.length; i++) {
            final ComponentName cn = ComponentName.unflattenFromString(names[i]);
            if (cn != null) {
               if (TextUtils.equals(pkgName, cn.getPackageName())) {
                  return true;
               }
            }
         }
      }
      return false;
   }

   // BroadcastReceiver
   public class ImageChangeBroadcastReceiver extends BroadcastReceiver {
      @Override
      public void onReceive(Context context, Intent intent) {
         int receivedNotificationCode = intent.getIntExtra("Notification Code",-1);
         String str = intent.getStringExtra("Message");
         changeInterceptedNotificationImage(receivedNotificationCode, str);
      }
   }


   /* Создает диалоговое окно  Listener Alert Dialog.
   Диалоговое окно предупреждения, которое появляется, если пользователь не включил
   Служба прослушивания уведомлений.
   Возвращает Диалоговое окно предупреждения, которое ведет к экрану включения уведомлений. */
   private AlertDialog buildNotificationServiceAlertDialog(){
      AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
      alertDialogBuilder.setTitle(R.string.notification_listener_service);
      alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation);
      alertDialogBuilder.setPositiveButton(R.string.yes,
              new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                    startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                 }
              });
      alertDialogBuilder.setNegativeButton(R.string.no,
              new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                    // Если вы решите не включать прослушиватель уведомлений
                    // приложение не будет работать, как ожидалось
                 }
              });
      return(alertDialogBuilder.create());
   }
}
