package il.ac.shenkar.doitsimple;

import il.ac.shenkar.doitsimple.common.AppConsts;
import il.ac.shenkar.doitsimple.common.SimpleGeofence;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Storage for geofence values, implemented in SharedPreferences.
 */
public class SimpleGeofenceStore {

    // The SharedPreferences object in which geofences are stored.
    private final SharedPreferences mPrefs;
    // The name of the SharedPreferences.
    private static final String SHARED_PREFERENCES = "SharedPreferences";

    /**
     * Create the SharedPreferences storage with private access only.
     */
    public SimpleGeofenceStore(Context context) {
        mPrefs = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    /**
     * Returns a stored geofence by its id, or returns null if it's not found.
     * @param id The ID of a stored geofence.
     * @return A SimpleGeofence defined by its center and radius, or null if the ID is invalid.
     */
    public SimpleGeofence getGeofence(String id) {
        // Get the latitude for the geofence identified by id, or INVALID_FLOAT_VALUE if it doesn't
        // exist (similarly for the other values that follow).
        double lat = mPrefs.getFloat(getGeofenceFieldKey(id, AppConsts.KEY_LATITUDE),
        		AppConsts.INVALID_FLOAT_VALUE);
        double lng = mPrefs.getFloat(getGeofenceFieldKey(id, AppConsts.KEY_LONGITUDE),
        		AppConsts.INVALID_FLOAT_VALUE);
        float radius = mPrefs.getFloat(getGeofenceFieldKey(id, AppConsts.KEY_RADIUS),
        		AppConsts.INVALID_FLOAT_VALUE);
        long expirationDuration =
                mPrefs.getLong(getGeofenceFieldKey(id, AppConsts.KEY_EXPIRATION_DURATION),
                		AppConsts.INVALID_LONG_VALUE);
        int transitionType = mPrefs.getInt(getGeofenceFieldKey(id, AppConsts.KEY_TRANSITION_TYPE),
        		AppConsts.INVALID_INT_VALUE);
        // If none of the values is incorrect, return the object.
        if (lat != AppConsts.INVALID_FLOAT_VALUE
                && lng != AppConsts.INVALID_FLOAT_VALUE
                && radius != AppConsts.INVALID_FLOAT_VALUE
                && expirationDuration != AppConsts.INVALID_LONG_VALUE
                && transitionType != AppConsts.INVALID_INT_VALUE) {
            return new SimpleGeofence(id, lat, lng, radius, expirationDuration, transitionType);
        }
        // Otherwise, return null.
        return null;
    }

    /**
     * Save a geofence.
     * @param geofence The SimpleGeofence with the values you want to save in SharedPreferences.
     */
    public void setGeofence(String id, SimpleGeofence geofence, String descTask) {
        // Get a SharedPreferences editor instance. Among other things, SharedPreferences
        // ensures that updates are atomic and non-concurrent.
        SharedPreferences.Editor prefs = mPrefs.edit();
        // Write the Geofence values to SharedPreferences.
        prefs.putFloat(getGeofenceFieldKey(id, AppConsts.KEY_LATITUDE), (float) geofence.getLatitude());
        prefs.putFloat(getGeofenceFieldKey(id, AppConsts.KEY_LONGITUDE), (float) geofence.getLongitude());
        prefs.putFloat(getGeofenceFieldKey(id, AppConsts.KEY_RADIUS), geofence.getRadius());
        prefs.putLong(getGeofenceFieldKey(id, AppConsts.KEY_EXPIRATION_DURATION),
                geofence.getExpirationDuration());
        prefs.putInt(getGeofenceFieldKey(id, AppConsts.KEY_TRANSITION_TYPE),
                geofence.getTransitionType());
        // add description to prefs
        prefs.putString(getGeofenceFieldKey(id,AppConsts.KEY_DESCRIPTION), descTask);
        // Commit the changes.
        prefs.commit();
    }

    /**
     * Remove a flattened geofence object from storage by removing all of its keys.
     */
    public void clearGeofence(String id) {
        SharedPreferences.Editor prefs = mPrefs.edit();
        prefs.remove(getGeofenceFieldKey(id, AppConsts.KEY_LATITUDE));
        prefs.remove(getGeofenceFieldKey(id, AppConsts.KEY_LONGITUDE));
        prefs.remove(getGeofenceFieldKey(id, AppConsts.KEY_RADIUS));
        prefs.remove(getGeofenceFieldKey(id, AppConsts.KEY_EXPIRATION_DURATION));
        prefs.remove(getGeofenceFieldKey(id, AppConsts.KEY_TRANSITION_TYPE));
        prefs.remove(getGeofenceFieldKey(id, AppConsts.KEY_DESCRIPTION));
        prefs.commit();
    }

    /**
     * Given a Geofence object's ID and the name of a field (for example, KEY_LATITUDE), return
     * the key name of the object's values in SharedPreferences.
     * @param id The ID of a Geofence object.
     * @param fieldName The field represented by the key.
     * @return The full key name of a value in SharedPreferences.
     */
    public String getGeofenceFieldKey(String id, String fieldName) {
        return AppConsts.KEY_PREFIX + "_" + id + "_" + fieldName;
    }

	public SharedPreferences getmPrefs() {
		return mPrefs;
	}
    
}