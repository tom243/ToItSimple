package il.ac.shenkar.doitsimple;

import il.ac.shenkar.doitsimple.common.Alarm;
import il.ac.shenkar.doitsimple.common.AppConsts;
import il.ac.shenkar.doitsimple.common.OnDataSourceChangeListener;
import il.ac.shenkar.doitsimple.common.SimpleGeofence;
import il.ac.shenkar.doitsimple.common.Task;
import il.ac.shenkar.doitsimple.data.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.google.android.gms.location.Geofence;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public class MainController {
	// The data model, act as a cache 
	private ArrayList<Task> tasks;
	private Context context;	
	private IDataAccess dao;
	private AlarmHelper alarmHelper;
	
	// Internal List of Geofence objects. In a real app, these might be provided
	// by an API based on
	// locations within the user's proximity.
	private List<Geofence> mGeofenceList;

	// Persistent storage for geofences.
	private SimpleGeofenceStore mGeofenceStorage;
	
	//observers list.
	private List<OnDataSourceChangeListener> dataSourceChangedListenrs = 
			new ArrayList<OnDataSourceChangeListener>();


	public MainController(Context context) {
		this.context = context;
		dao = DAO.getInstance(context.getApplicationContext());
		alarmHelper = new AlarmHelper();
		// Instantiate a new geofence storage area.
		mGeofenceStorage = new SimpleGeofenceStore(context);
		// Instantiate the current List of geofences.
		mGeofenceList = new ArrayList<Geofence>();
	}

	public ArrayList<Task> getAllTasks(boolean flagUnDoneTasks) {
		// flag false --> only the done tasks
		// flag true --> only the unDone tasks
		try {
			if (tasks != null)
				return currentArray(tasks,flagUnDoneTasks);
			dao.open();
			ArrayList<Task> allTasks = dao.getTaskList();
			dao.close();
			PopulateTasksCache(allTasks);
			return currentArray(allTasks,flagUnDoneTasks);
		} catch (Exception e) {
			// in case of error, return empty list.
			return new ArrayList<Task>();
		}
	}
	
	public ArrayList<Task> currentArray (ArrayList<Task> allTasks ,boolean flagUnDoneTasks){
		ArrayList<Task> arrTemp = new ArrayList<Task>();
		if (flagUnDoneTasks) { // only the done tasks
			for (Task task : allTasks) {
				if (task.getStatus()==0) {
				arrTemp.add(task);
				}
			}
		}
		else { // only the unDone tasks
			for (Task task : allTasks) {
				if (task.getStatus()==1) {
					arrTemp.add(task);
				}
			}
		}
		return arrTemp;
	}

	private void PopulateTasksCache(List<Task> tasksList )
	{	 // create HashMap and inserts all tasks 
		tasks =  new ArrayList<Task>();
		for (Task task : tasksList) {
			tasks.add(task);
		}
	}

	public void refreshData() {
		tasks = null;
		getAllTasks(true);
	}

	/*
	 * Add task to the data source.
	 */
	public int addTask(Task t) {
		try {
			//open the connection to the DAO
			dao.open();
			//add the task to the data base and use the returned task and add it to the local cache.
			//the task that returned from the DAO contain the id of the entity.
			Task retTask = dao.addTask(t);
			dao.close();
			if ( retTask == null) return -1;
			if(tasks.contains(retTask)) 
				return retTask.getId();	// checking task that is already exist in our list
			tasks.add(retTask);
			//update what ever it will be.
			invokeDataSourceChanged();
			return retTask.getId();
		} catch (Exception e) {
			Log.e("MainController",e.getMessage());
			return -1;
		}
	}

	/*
	 * remove task from the data source.
	 */
	public void removeTask(Task t) {
		
		//open the database connection
		dao.open();
		//remove the task from the database.
		dao.removeTask(t);
		//remove from the local cache.
		removeFromCache(t);
		//close the connection.
		dao.close();
		invokeDataSourceChanged();
		if (t.isAlarm())
			CancelAlarm(t.getId()); 
		if (t.isLocation()){
			CancelGeofences(t);
			mGeofenceStorage.clearGeofence(String.valueOf(t.getId())); // clear from prefs
		}
	}

	/*
	 * done task from the data source.
	 */
	public void doneTask(Task t) {
		
		//open the database connection
		dao.open();
		//remove the task from the database.
		dao.changeTaskStatus(t);
		//close the connection.
		dao.close();
		if (tasks.contains(t)) { 
			int i = tasks.indexOf(t);
			tasks.get(i).setStatus(t.getStatus());
		}
		invokeDataSourceChanged();
		if (t.isAlarm())
			CancelAlarm(t.getId());
		if (t.isLocation()){
			CancelGeofences(t);
		}
	}
	

	/*
	 * done task from the data source.
	 */
	public void undoneTask(Task t) {
		
		//open the database connection
		dao.open();
		//remove the task from the database.
		dao.changeTaskStatus(t);
		//close the connection.
		dao.close();
		if (tasks.contains(t)) { 
			int i = tasks.indexOf(t);
			tasks.get(i).setStatus(t.getStatus());
		}
		invokeDataSourceChanged();
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(t.getCalendarInMillis());
		long currentMillisTime = Calendar.getInstance().getTimeInMillis();
		if (t.isAlarm() && t.getCalendarInMillis() >= currentMillisTime) 
			CreateAlarm(t.getItemDescription(), c.getTime(), t.getId());
		if (t.isLocation()){
			SimpleGeofence geofence=mGeofenceStorage.getGeofence(String.valueOf(t.getId()));
			double latitude=geofence.getLatitude();
			double longtitude=geofence.getLongitude();
			createGeofences(String.valueOf(t.getId()), latitude, longtitude);
		}
	}
	
	public void updateTask(Task task){
		dao.open();
		//update the task in the database.
		dao.updateTask(task);
		//close the connection.
		dao.close();
		//update the local cache.
		if(tasks.contains(task)){
			int i = tasks.indexOf(task);
			tasks.get(i).setItemDescription(task.getItemDescription());
			tasks.get(i).setAlarm(task.isAlarm());
			tasks.get(i).setCalendarInMillis(task.getCalendarInMillis());
			tasks.get(i).setStatus(task.getStatus());
			tasks.get(i).setImportance(task.isImportance());
			tasks.get(i).setLocation(task.isLocation());
			tasks.get(i).setAddress(task.getAddress());
		}
		invokeDataSourceChanged();
	}
	
	
	public void removeFromCache(Task t)
	{
		if ( tasks.contains(t) ) 
			tasks.remove(t);
	}
	
	public void registerOnDataSourceChanged(OnDataSourceChangeListener listener)
	{
		if(listener!=null)
			dataSourceChangedListenrs.add(listener);
	}
	
	public void unRegisterOnDataSourceChanged(OnDataSourceChangeListener listener)
	{
		if(listener!=null)
			dataSourceChangedListenrs.remove(listener);
	}
	
	public void invokeDataSourceChanged()
	{			
		for (OnDataSourceChangeListener listener : dataSourceChangedListenrs) {
			listener.DataSourceChanged();
		}
	}
	
	public void CreateAlarm(String message,Date when,int taskId)
	{
		Alarm alarm  =  new Alarm();
		alarm.setId(taskId);
		Log.i("CONTROLLER-create alarm","id alarm: "+taskId);
		Bundle extras = new Bundle();
		extras.putString(AppConsts.Extra_Message, message);
		extras.putInt(AppConsts.Extra_ID_ALARM, taskId);
		
		Log.i("CONTROLLER-create alarm","the message is: "+message);
		alarm.setExtras(extras);
		alarm.setAction(AppConsts.ACTION_ALARM);
		alarm.setIntervalMillis(0);
		alarm.setReciever(NotificationBroadCastReciever.class);
		alarm.setTriggerAtMillis(when.getTime());
		
		alarmHelper.setAlarm(context, alarm);
	}
	
	public void CancelAlarm(int taskId)
	{
		Log.i("CONTROLLER","cancel alarm : "+taskId);
		alarmHelper.cancelAlarm(context,taskId);
	}

	/**
	 * In this sample, the geofences are predetermined and are hard-coded here.
	 * A real app might dynamically create geofences based on the user's
	 * location.
	 */
	public void createGeofences(String geoID,double latitude, double longtitude) {
		SimpleGeofence newGeoFence;
		// Create internal "flattened" objects containing the geofence data.
		newGeoFence = new SimpleGeofence(
				geoID, // geofenceId.
				latitude, longtitude,
				AppConsts.BUILDING_RADIUS_METERS, AppConsts.GEOFENCE_EXPIRATION_TIME,
				Geofence.GEOFENCE_TRANSITION_ENTER| Geofence.GEOFENCE_TRANSITION_EXIT);
		// Store these flat versions in SharedPreferences and add them to the
		// geofence list.
		
		String descTask = null ;
		for (Task t : tasks){
			if (t.getId() == Integer.parseInt(geoID)) 
				descTask = t.getItemDescription();
		}
		mGeofenceStorage.setGeofence(geoID,newGeoFence, descTask);
		mGeofenceList.add(newGeoFence.toGeofence());
	}
	
	public void CancelGeofences(Task task) {
		Log.i("CONTROLLER","cancel GEO: "+task.getId());
		SimpleGeofence geofence=mGeofenceStorage.getGeofence(String.valueOf(task.getId()));
		
		for (Geofence geo : mGeofenceList){
			if(geo.getRequestId().equals(geofence.getId())) {
				Log.i("",""+geo.getRequestId());
				mGeofenceList.remove(geo); 
			}
		}
		// cancel GEO notification
		NotificationManager nofiManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nofiManager.cancel("GEO",task.getId());
	}
	
	public List<Geofence> getmGeofenceList() {
		return mGeofenceList;
	}

	public void setmGeofenceList(List<Geofence> mGeofenceList) {
		this.mGeofenceList = mGeofenceList;
	}
	
}
