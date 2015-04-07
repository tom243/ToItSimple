package il.ac.shenkar.doitsimple;

import il.ac.shenkar.doitsimple.common.Alarm;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmHelper {
		
	private static Intent activityIntent = new Intent(); 
	
	public void setAlarm(Context c, Alarm alarm) {
		if (c == null || alarm == null)
			return;
		// create the intent, with the receiver that should handle the alarm.
		activityIntent = new Intent(c.getApplicationContext(), alarm.getReciever());
		// set the action.
		activityIntent.setAction(alarm.getAction());
		// set the extras.
		activityIntent.putExtras(alarm.getExtras());
		
		PendingIntent pendingInent = PendingIntent.getBroadcast(c,
				alarm.getId(), activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) c
				.getSystemService(Context.ALARM_SERVICE);
		if (alarm.getIntervalMillis() > 0)
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
					alarm.getTriggerAtMillis(), alarm.getIntervalMillis(),
					pendingInent);
		else {
			alarmManager.set(AlarmManager.RTC_WAKEUP,
					alarm.getTriggerAtMillis(), pendingInent);
		}
	}

	public void cancelAlarm(Context c, int taskId) {
		Log.i("ALARM-HELPER","notification canceled: "+taskId);
		// get the notification manager service.
		NotificationManager nofiManager = (NotificationManager) c
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nofiManager.cancel("ALARM",taskId);
	}

}
