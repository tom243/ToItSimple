package il.ac.shenkar.doitsimple;

import il.ac.shenkar.doitsimple.common.AppConsts;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GeofencingReceiverIntentService extends
		ReceiveGeofenceTransitionBaseIntentService {

	private NotificationManager notificationManager;

	@Override
	protected void onEnteredGeofences(String[] strings) {
		Log.d(GeofencingReceiverIntentService.class.getName(), "onEnter");
		int taskId = Integer.parseInt(strings[0]);
		Log.d(GeofencingReceiverIntentService.class.getName(), "The id task is: "+strings[0]);
		CreateNotification("Geofence was recieved",taskId);
	}

	@Override
	protected void onExitedGeofences(String[] strings) {
		Log.d(GeofencingReceiverIntentService.class.getName(), "onExit");
	}

	@Override
	protected void onError(int i) {
		Log.e(GeofencingReceiverIntentService.class.getName(), "Error: " + i);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	private void CreateNotification(String text, int taskId) {
		Intent intent = new Intent(this, MainActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, taskId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		SimpleGeofenceStore mGeofenceStorage = 	new SimpleGeofenceStore(this); 
		String strId = Integer.toString(taskId);
		String desc = mGeofenceStorage.getmPrefs().getString(mGeofenceStorage.getGeofenceFieldKey(strId, AppConsts.KEY_DESCRIPTION),
         		AppConsts.INVALID_STRING_VALUE);
		
		// build notification
		// the addAction re-use the same intent to keep the example short
		Notification n = new Notification.Builder(this)
				.setContentTitle(text)
				.setContentText(desc)
				.setSmallIcon(R.drawable.logo_do_it_simple).setContentIntent(pIntent)
				.setAutoCancel(true).build();
		notificationManager.notify("GEO",taskId, n);
	}
	
}
