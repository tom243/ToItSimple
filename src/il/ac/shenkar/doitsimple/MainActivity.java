package il.ac.shenkar.doitsimple;

import java.util.Calendar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

import il.ac.shenkar.doitsimple.common.AppConsts;
import il.ac.shenkar.doitsimple.common.OnDataSourceChangeListener;
import il.ac.shenkar.doitsimple.common.Task;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.nhaarman.listviewanimations.appearance.simple.ScaleInAnimationAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

public class MainActivity extends Activity implements OnDataSourceChangeListener,ConnectionCallbacks,
	OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, ResultCallback<Status>{

	private MainController controller;
	private TaskListBaseAdapter adapter;
	private long currentTaskPosition;
	private Calendar calendar;
	public static final String TAG="MainActivity";
	private MenuItem item_tasks = null;
	private PendingIntent mGeofenceRequestIntent;
	private GoogleApiClient mApiClient;
	private Task currentTask ;
	private EasyTracker easyTracker = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        easyTracker = EasyTracker.getInstance(MainActivity.this);
        easyTracker.send(MapBuilder.createEvent("TrackEventMainAcitivity", "Welcome To The APP", "track_event", null).build());
        calendar = Calendar.getInstance();
        // Create the controller
        controller = new MainController(this);
        // register for OnDataSourceChangedListener event.
     	controller.registerOnDataSourceChanged(this);
        DynamicListView lv = (DynamicListView) findViewById(R.id.dynamiclistview);
        
        if (lv != null) {
        	adapter = new TaskListBaseAdapter(this, controller.getAllTasks(true));
        	ScaleInAnimationAdapter animationAdapter = new ScaleInAnimationAdapter(adapter);
        	animationAdapter.setAbsListView(lv);
        	lv.setAdapter(animationAdapter);
        	lv.enableSwipeToDismiss(
        		    new OnDismissCallback() {
						@Override
						public void onDismiss(ViewGroup arg0, int[] arg1) {
							 for (int position : arg1) {
								 if (item_tasks.getTitleCondensed().equals("undone")) {
									 currentTask = controller.getAllTasks(true).get(position);
									 currentTask.setStatus(1); // set task to done
							 		 controller.doneTask(currentTask);
							 		 showUndoneTasks(item_tasks);
							 		 Toast.makeText(getApplicationContext(),"Task: "+currentTask.getItemDescription()+" set to done",Toast.LENGTH_SHORT).show();
							 	 } else {
							 		 currentTask  = controller.getAllTasks(false).get(position);
							 		 currentTask.setStatus(0);
							 		 controller.undoneTask(currentTask);
							 		 showDoneTasks(item_tasks);
							 		 Toast.makeText(getApplicationContext(),"Task: "+currentTask.getItemDescription()+" set to undone",Toast.LENGTH_SHORT).show();
							 	 }
					          }
							 mApiClient.connect();
						}
        		    }
        	);
      
        }
        registerForContextMenu(lv);

        if (!isGooglePlayServicesAvailable()) {
			Log.e(TAG, "Google Play services unavailable.");
			Toast.makeText(this, "The application cannot work without google play service" , Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
        
		mApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).build();
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        item_tasks = menu.getItem(0);
        return true;
    }

    @Override
  	public boolean onPrepareOptionsMenu(Menu menu) { // maybe delete
    	super.onPrepareOptionsMenu(menu);
    	item_tasks = menu.getItem(0);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
        case R.id.action_add_task:
        	newTaskClicked();
        	showUndoneTasks(item_tasks);
        	return true;
        case R.id.action_show_done_tasks:
        	showTasksAfterClicked(item);
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    /*
     * button adding new task clicked, starting intent for add task.
     */
    public void newTaskClicked() {
    	Intent intent = new Intent(this, TaskActivity.class);
    	startActivityForResult(intent, AppConsts.GET_TASK_REQUEST);
    }

    public void showTasksAfterClicked(MenuItem item) {
    	if (item.getTitleCondensed().equals("undone"))  
    		showDoneTasks(item);
    	else 
    		showUndoneTasks(item);
    	adapter.notifyDataSetChanged();
    }

    public void showTasks() {
    	if (item_tasks.getTitleCondensed().equals("undone"))  
    		showUndoneTasks(item_tasks);
    	else
    		showDoneTasks(item_tasks);
    	adapter.notifyDataSetChanged();
    }
    public void showUndoneTasks(MenuItem item) {
    	item.setTitleCondensed("undone");
    	item.setIcon(R.drawable.donetasks);
		adapter.UpdateDataSource(controller.getAllTasks(true));
    }
    
    public void showDoneTasks(MenuItem item) {
    	item.setTitleCondensed("done");
		item.setIcon(R.drawable.donetasks_on);
		adapter.UpdateDataSource(controller.getAllTasks(false));
    }
    
    /*
     * after chosen all fields for creating new task in addTask activity
     * create the task and save it in controller.
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data == null)
			return;
		String descPass= data.getStringExtra(AppConsts.EXTRA_MESSAGE_DESC);
		long millis = data.getLongExtra(AppConsts.EXTRA_MESSAGE_MILLIS, 0);
		boolean on = data.getBooleanExtra(AppConsts.EXTRA_MESSAGE_ALARM, false);
		boolean imp = data.getBooleanExtra(AppConsts.EXTRA_MESSAGE_IMPORTANCE, false);
		boolean geo = data.getBooleanExtra(AppConsts.EXTRA_MESSAGE_LOCATION, false);
		double latitude = data.getDoubleExtra(AppConsts.EXTRA_MESSAGE_LATITUDE, AppConsts.INVALID);
		double longtude = data.getDoubleExtra(AppConsts.EXTRA_MESSAGE_LONGITUDE, AppConsts.INVALID);
		String address = data.getStringExtra(AppConsts.EXTRA_MESSAGE_ADDRESS);
		
		if (requestCode == AppConsts.GET_TASK_REQUEST && resultCode == RESULT_OK) {
			Log.i(TAG,"On Result- ADD" );
			if (descPass.length() >= 1) {
				// create new task with id=0
				currentTask = new Task(descPass, millis, on, imp, geo, address);
				// add task to date base and get the new task id
				int newId = controller.addTask(currentTask);
				currentTask.setId(newId);
				Log.i(TAG,"Task id= " + newId);
				// set alarm to this task with this date & time
				Log.i("mainactivity- ALARM", "the message is :" + descPass);

				long currentMillisTime = Calendar.getInstance().getTimeInMillis();
				if (on && millis >= currentMillisTime) { 
					calendar.setTimeInMillis(millis);
					controller.CreateAlarm(descPass, calendar.getTime(), newId);
				}
				adapter.UpdateDataSource(controller.getAllTasks(true));
				if ( geo && latitude != 0 && longtude != 0) { // check if the user select geofence
					controller.createGeofences(Integer.toString(newId), latitude,longtude);
					mApiClient.connect();
				}
			}
			else
				Toast.makeText(this, "The task was not added." , Toast.LENGTH_SHORT).show();
		}
		else{
			if (requestCode == AppConsts.GET_TASK_EDIT && resultCode == RESULT_OK
					&& item_tasks.getTitleCondensed().equals("undone") ) {
				
				currentTask = controller.getAllTasks(true).get((int) currentTaskPosition);
				if (descPass.length() >= 1 ) {
					currentTask.setItemDescription(descPass);
				} else 
					Toast.makeText(this, "Description cannot be empty." , Toast.LENGTH_SHORT).show();
				
				currentTask.setCalendarInMillis(millis);
				currentTask.setAlarm(on);
				currentTask.setImportance(imp);
				boolean oldGeoStatus=currentTask.isLocation();
				currentTask.setLocation(geo);
				String oldAddress=currentTask.getAddress(); // save the current address before the edit 
				currentTask.setAddress(address);
				controller.updateTask(currentTask);
    			// set alarm to this task with this date & time
    			long currentMillisTime = Calendar.getInstance().getTimeInMillis();
    			if (on && millis >=currentMillisTime) {
    				calendar.setTimeInMillis(millis);
    				controller.CreateAlarm(descPass, calendar.getTime(),currentTask.getId());
    				Log.i(TAG,"edit task.. "+currentTask.getId());
    			} else {
    				controller.CancelAlarm(currentTask.getId());
    				Log.i("mainActivity","cancel alarm on edit: "+currentTask.getId());
    			}
				if (geo && latitude != 0 && longtude != 0) { // check if the user select geofence
					if ( oldAddress == null || !oldAddress.equals(address)){ // it means that address was changed or geo first time
						Log.d(TAG, "address was changed in edit task");
						controller.createGeofences(Integer.toString(currentTask.getId()), latitude,longtude);
						mApiClient.connect();
					}
				}
				else{
					if (!geo && oldGeoStatus){
						controller.CancelGeofences(currentTask);
						mApiClient.connect(); 
					}
				}
    			adapter.UpdateDataSource(controller.getAllTasks(true));
			}
			
		}
	}
	
	@Override
	public void DataSourceChanged() {
		if (adapter != null) {
			adapter.UpdateDataSource(controller.getAllTasks(true));
			adapter.notifyDataSetChanged();
		}
	}
		
	/** This will be invoked when an item in the listview is long pressed */
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) 
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.actions, menu);
		if (item_tasks.getTitleCondensed().equals("done")) {
			MenuItem item = menu.getItem(0);
			item.setTitle("View Details");
		}
	}	
	
	/** This will be invoked when a menu item is selected */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
 
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        Log.i("debug",Long.toString(info.id));
        currentTaskPosition = info.id;
        if (item_tasks.getTitleCondensed().equals("undone"))   
        	currentTask = controller.getAllTasks(true).get((int) currentTaskPosition);
        else 
        	currentTask = controller.getAllTasks(false).get((int) currentTaskPosition);
        switch(item.getItemId()){
            case R.id.cnt_mnu_edit:
        		Intent intent = new Intent(this, TaskActivity.class);
        		intent.putExtra(AppConsts.EXTRA_MESSAGE_DESC, currentTask.getItemDescription());
        		intent.putExtra(AppConsts.EXTRA_MESSAGE_MILLIS, currentTask.getCalendarInMillis());
            	intent.putExtra(AppConsts.EXTRA_MESSAGE_ALARM, currentTask.isAlarm());
            	intent.putExtra(AppConsts.EXTRA_MESSAGE_IMPORTANCE, currentTask.isImportance());
            	intent.putExtra(AppConsts.EXTRA_MESSAGE_LOCATION, currentTask.isLocation());
            	intent.putExtra(AppConsts.EXTRA_MESSAGE_ADDRESS, currentTask.getAddress());
            	if (currentTask.getStatus()==1) // the task is done
            		intent.putExtra(AppConsts.EXTRA_DONE, 1);
            	else
            		intent.putExtra(AppConsts.EXTRA_DONE, 0);
        		startActivityForResult(intent, AppConsts.GET_TASK_EDIT);
                break;
            case R.id.cnt_mnu_delete:
            	controller.removeTask(currentTask);
                if (currentTask.isLocation()) {
                	currentTask.setLocation(false);
					mApiClient.connect();
                }
              	showTasks();
                Toast.makeText(this,currentTask.getItemDescription()+" was Deleted"   , Toast.LENGTH_SHORT).show();
                break;
	        default:
	            return super.onContextItemSelected(item);
        }
        return true;
    }

	/**
	 * Once the connection is available, send a request to add the Geofences.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		// Get the PendingIntent for the geofence monitoring request.
		// Send a request to add the current geofences.
		mGeofenceRequestIntent = getGeofenceTransitionPendingIntent();
		
		if (currentTask.isLocation() && currentTask.getStatus()==0){
			LocationServices.GeofencingApi.addGeofences(mApiClient, controller.getmGeofenceList(), mGeofenceRequestIntent);
			Toast.makeText(this, "Start geofence service", Toast.LENGTH_SHORT).show();
		} else {
			LocationServices.GeofencingApi.removeGeofences(mApiClient,mGeofenceRequestIntent).setResultCallback(this); // Result processed in onResult().;
		}
		mApiClient.disconnect();
	}
    
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// If the error has a resolution, start a Google Play services activity
		// to resolve it.
		if (connectionResult.hasResolution()) {
			try {
				connectionResult.startResolutionForResult(this,
						AppConsts.CONNECTION_FAILURE_RESOLUTION_REQUEST);
			} catch (IntentSender.SendIntentException e) {
				Log.e(TAG, "Exception while resolving connection error.", e);
			}
		} else {
			int errorCode = connectionResult.getErrorCode();
			Log.e(TAG,
					"Connection to Google Play services failed with error code "
							+ errorCode);
		}
	}

	@Override
	public void onDisconnected() {
		
	}
	
	@Override
	public void onConnectionSuspended(int i) {
		if (null != mGeofenceRequestIntent) {
			LocationServices.GeofencingApi.removeGeofences(mApiClient,
					mGeofenceRequestIntent);
		}
	}

	/**
	 * Checks if Google Play services is available.
	 * 
	 * @return true if it is.
	 */
	private boolean isGooglePlayServicesAvailable() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (ConnectionResult.SUCCESS == resultCode) {
			if (Log.isLoggable(TAG, Log.DEBUG)) {
				Log.d(TAG, "Google Play services is available.");
			}
			return true;
		} else {
			Log.e(TAG, "Google Play services is unavailable.");
			return false;
		}
	}

	/**
	 * Create a PendingIntent that triggers GeofenceTransitionIntentService when
	 * a geofence transition occurs.
	 */
	private PendingIntent getGeofenceTransitionPendingIntent() {
		Intent intent = new Intent(this, GeofencingReceiverIntentService.class);
		return PendingIntent.getService(this, currentTask.getId(), intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

	}

	@Override
	public void onResult(Status result) {
	//	Task task = controller.getAllTasks(true).get((int) currentTaskPosition);
		Log.d(TAG, " DEACTIVATED THE GEO OF: "+ currentTask.getItemDescription() + "" + currentTask.getId());
	}
    
    @Override
    protected void onStart() {
    	super.onStart();
    	EasyTracker.getInstance(this).activityStart(this);
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	EasyTracker.getInstance(this).activityStop(this);
    }
	
}
