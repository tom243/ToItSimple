package il.ac.shenkar.doitsimple.common;

import android.net.Uri;

import com.google.android.gms.location.Geofence;

public class AppConsts {
	public static final String SharedPrefsName = "co.il.shenkar.todoapp.common";
	
	public static final int INVALID = 9999;

	public static final String DONE_TASK_INTENT = "il.ac.shenkar.todoapp.common.1";
	public static final String EXTRA_MSG_ID_TASK = "removingTaskId";
	
	public static final int GET_TASK_REQUEST = 1;
	public static final int GET_TASK_EDIT = 2;
	
	public final static String EXTRA_MESSAGE_DESC = "MESSAGE_DESCRIPTION";
	public final static String EXTRA_MESSAGE_MILLIS = "MESSAGE_DATE_TIME";
	public final static String EXTRA_MESSAGE_ALARM = "MESSAGE_ALARM";
	public final static String EXTRA_MESSAGE_IMPORTANCE = "MESSAGE_IMPORTANCE";
	public final static String EXTRA_MESSAGE_LOCATION = "MESSAGE_LOCATION";
	public static final String EXTRA_MESSAGE_LATITUDE = "MESSAGE_LATITUDE";
    public static final String EXTRA_MESSAGE_LONGITUDE = "MESSAGE_LONGITUDE";
    public static final String EXTRA_MESSAGE_ADDRESS = "MESSAGE_ADDRESS";
    
    public static final String EXTRA_DONE = "DONE";
	
	public static final String Extra_Message = "Message";
	public static final String ACTION_ALARM = "il.ac.shenkar.ALARM";
	public static final String Extra_ID_ALARM = "alarm id";
	
    // Request code to attempt to resolve Google Play services connection failures.
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    // Timeout for making a connection to GoogleApiClient (in milliseconds).
    public static final long CONNECTION_TIME_OUT_MS = 100;

    // For the purposes of this demo, the geofences are hard-coded and should not expire.
    // An app with dynamically-created geofences would want to include a reasonable expiration time.
    public static final long GEOFENCE_EXPIRATION_TIME = Geofence.NEVER_EXPIRE;

    // Geofence parameters 
    public static final float BUILDING_RADIUS_METERS = 80;

    // The constants below are less interesting than those above.

    // Path for the DataItem containing the last geofence id entered.
    public static final String GEOFENCE_DATA_ITEM_PATH = "/geofenceid";
    public static final Uri GEOFENCE_DATA_ITEM_URI =
            new Uri.Builder().scheme("wear").path(GEOFENCE_DATA_ITEM_PATH).build();
    public static final String KEY_GEOFENCE_ID = "geofence_id";

    // Keys for flattened geofences stored in SharedPreferences.
    public static final String KEY_LATITUDE = "com.example.wearable.geofencing.KEY_LATITUDE";
    public static final String KEY_LONGITUDE = "com.example.wearable.geofencing.KEY_LONGITUDE";
    public static final String KEY_RADIUS = "com.example.wearable.geofencing.KEY_RADIUS";
    public static final String KEY_EXPIRATION_DURATION =
            "com.example.wearable.geofencing.KEY_EXPIRATION_DURATION";
    public static final String KEY_TRANSITION_TYPE =
            "com.example.wearable.geofencing.KEY_TRANSITION_TYPE";
    // The prefix for flattened geofence keys.
    public static final String KEY_PREFIX = "com.example.wearable.geofencing.KEY";
    public static final String KEY_DESCRIPTION = "com.example.wearable.geofencing.KEY_DESC";
    

    // Invalid values, used to test geofence storage when retrieving geofences.
    public static final long INVALID_LONG_VALUE = -999l;
    public static final float INVALID_FLOAT_VALUE = -999.0f;
    public static final int INVALID_INT_VALUE = -999;
    public static final String INVALID_STRING_VALUE = "";
    
    
	public static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
	public static final String TYPE_AUTOCOMPLETE = "/autocomplete";
	public static final String OUT_JSON = "/json";

	//------------ make your specific key ------------
	public static final String API_KEY = "AIzaSyCRfMf6k0mnYTWGMEJj1m6dw4nebhEJo7U";
    
}
