package il.ac.shenkar.doitsimple;

import il.ac.shenkar.doitsimple.common.AppConsts;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class NotificationBroadCastReciever extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// Create the Notification.
		Bundle extras = intent.getExtras();
		// Fetch the message from the bundle.
		String message = extras.getString(AppConsts.Extra_Message);
		int taskId = extras.getInt(AppConsts.Extra_ID_ALARM);
		// crate the notification.
		createNotification(context, message, taskId);
	}

	/*
	 * Crate notification with a specific message.
	 */
	public void createNotification(Context context, String message, int taskId) {

		// get the notification manager service.
		NotificationManager nofiManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		// Prepare intent which is triggered if the
		// notification is selected
		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// Build notification
		Notification noti = new Notification.Builder(context)
				.setContentTitle("Remind you to do this task!")
				.setContentText(message)
				.setSmallIcon(R.drawable.logo_do_it_simple).setContentIntent(pIntent)
				.build();
		// hide the notification after its selected
		noti.flags |= Notification.FLAG_INSISTENT;
		nofiManager.notify("ALARM",taskId, noti);
	}
}
