package il.ac.shenkar.doitsimple;

import il.ac.shenkar.doitsimple.common.AppConsts;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

public class TaskActivity extends Activity {

	private EditText descriptionEt;
	private EditText txtDate, txtAddress;
	private Geocoder geocoder;
	private List<Address> addresses;
	private long millis = AppConsts.INVALID;
	private Calendar myCalendar;
	private MenuItem item_alarm = null, item_importance = null,item_location = null;
	private Intent mainActiviryIntent;
	private Bundle extras = null;
	private String chosenAddress = null;
	private ShareButton shareBt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FacebookSdk.sdkInitialize(getApplicationContext());
		setContentView(R.layout.activity_add_task);
		myCalendar = Calendar.getInstance();
		mainActiviryIntent = new Intent(this, MainActivity.class);
		extras = getIntent().getExtras();
		txtDate = (EditText) findViewById(R.id.txtDate);
		txtDate.setEnabled(false);
		txtAddress = (EditText) findViewById(R.id.address);
		txtAddress.setEnabled(false);
		descriptionEt = (EditText) findViewById(R.id.task_description);
		
		Button bt = (Button) findViewById(R.id.create_activity_button);
		if (extras != null) { // We are in edit mode

			if (extras.getInt(AppConsts.EXTRA_DONE) == 1) { // the task is done
				setTitle("Details Task");
				bt.setText("Back");
				descriptionEt.setEnabled(false);
				descriptionEt.setText(extras
						.getString(AppConsts.EXTRA_MESSAGE_DESC));
			} else {
				setTitle("Edit Task");
				bt.setText("Edit");
				descriptionEt.setText(extras.getString(AppConsts.EXTRA_MESSAGE_DESC));
			}
		}
		geocoder = new Geocoder(this.getApplicationContext());
		shareBt = (ShareButton) findViewById(R.id.facbookshare);
		
		descriptionEt.addTextChangedListener(new TextWatcher() {

	          public void afterTextChanged(Editable s) {
	        	  checkForFacebook(s.toString());
	          }
	          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	          public void onTextChanged(CharSequence s, int start, int before, int count) {}
       });
		checkForFacebook("");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.add_task, menu);
		item_importance = menu.getItem(0);
		item_location = menu.getItem(1);
		item_alarm = menu.getItem(2);
		if (!isOnline())
			item_location.setEnabled(false);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		if (extras != null) { // We are in edit mode

			boolean alarmOn = extras.getBoolean(AppConsts.EXTRA_MESSAGE_ALARM);
			if (item_alarm != null) {
				if (alarmOn) {
					alarm_on();
					millis = extras.getLong(AppConsts.EXTRA_MESSAGE_MILLIS);
					myCalendar.setTimeInMillis(millis);
					updateLabel();
				} else {
					alarm_off();
				}
			}
			boolean imp = extras.getBoolean(AppConsts.EXTRA_MESSAGE_IMPORTANCE);
			if (item_importance != null) {
				if (imp)
					set_important();
				else
					set_not_important();
			}
			boolean geo = extras.getBoolean(AppConsts.EXTRA_MESSAGE_LOCATION);
			if (item_location != null) {
				if (geo) {
					chosenAddress = extras
							.getString(AppConsts.EXTRA_MESSAGE_ADDRESS);
					location_on();
				} else {
					location_off();
				}
			}

			if (extras.getInt(AppConsts.EXTRA_DONE) == 1) { // the task is done
				item_alarm.setEnabled(false);
				item_importance.setEnabled(false);
				item_location.setEnabled(false);
			}
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case R.id.action_alarm:
			alarmButtonClicked(item);
			return true;
		case R.id.action_location:
			locationButtonClicked(item);
			return true;
		case R.id.action_importance:
			importantButtonClicked(item);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void alarmButtonClicked(MenuItem item) {

		DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				myCalendar.set(Calendar.YEAR, year);
				myCalendar.set(Calendar.MONTH, monthOfYear);
				myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			}
		};
		TimePickerDialog.OnTimeSetListener time = new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				// save Selected time
				myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
				myCalendar.set(Calendar.MINUTE, minute);
				myCalendar.set(Calendar.SECOND, 0);
				updateAlarmWasChanged(item_alarm);
				updateLabel();
			}
		};
		if (item.getTitle().equals("Alarm Off")) {
			new TimePickerDialog(TaskActivity.this, time,
					myCalendar.get(Calendar.HOUR_OF_DAY),
					myCalendar.get(Calendar.MINUTE), false).show();
			new DatePickerDialog(TaskActivity.this, date,
					myCalendar.get(Calendar.YEAR),
					myCalendar.get(Calendar.MONTH),
					myCalendar.get(Calendar.DAY_OF_MONTH)).show();
			alarm_on();
		} else {
			alarm_off();
		}
	}

	private void updateLabel() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy  HH:mm",
				Locale.US);
		txtDate.setText(sdf.format(myCalendar.getTime()));
	}

	private void updateAlarmWasChanged(MenuItem item) {
		if (item.getTitle().equals("Alarm Off"))
			alarm_on();
		else
			alarm_off();
	}

	public void alarm_on() {
		item_alarm.setTitle("Alarm On");
		item_alarm.setIcon(R.drawable.alarm_on);
		mainActiviryIntent.putExtra(AppConsts.EXTRA_MESSAGE_ALARM, true);
		txtDate.setVisibility(View.VISIBLE);
	}

	public void alarm_off() {
		item_alarm.setTitle("Alarm Off");
		item_alarm.setIcon(R.drawable.alarm_off);
		mainActiviryIntent.putExtra(AppConsts.EXTRA_MESSAGE_ALARM, false);
		txtDate.setVisibility(View.GONE);
	}

	public void importantButtonClicked(MenuItem item) {
		if (item.getTitle().equals("not important task"))
			set_important();
		else
			set_not_important();
	}

	public void set_important() {
		item_importance.setTitle("important task");
		item_importance.setIcon(R.drawable.importance_on);
		mainActiviryIntent.putExtra(AppConsts.EXTRA_MESSAGE_IMPORTANCE, true);
	}

	public void set_not_important() {
		item_importance.setTitle("not important task");
		item_importance.setIcon(R.drawable.importance_off);
		mainActiviryIntent.putExtra(AppConsts.EXTRA_MESSAGE_IMPORTANCE, false);
	}

	public void locationButtonClicked(MenuItem item) {

		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Choose Address");
		final AutoCompleteTextView autoCompView = new AutoCompleteTextView(this);
		alert.setView(autoCompView);
		autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(this,
				R.layout.list_item));
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				chosenAddress = autoCompView.getText().toString();
				Log.i("TASK- geo dialog", chosenAddress);
				if (chosenAddress != null && !chosenAddress.isEmpty()) { 
					// verify the user select address
					location_on();
				}
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
						location_off();
					}
				});

		if (item_location.getTitle().equals("geofence not active"))
			alert.show();
		else
			location_off();
	}

	public void location_on() {
		item_location.setTitle("geofence active");
		item_location.setIcon(R.drawable.location_on);
		mainActiviryIntent.putExtra(AppConsts.EXTRA_MESSAGE_LOCATION, true);
		txtAddress.setText(chosenAddress);
		txtAddress.setVisibility(View.VISIBLE);
	}

	public void location_off() {
		item_location.setTitle("geofence not active");
		item_location.setIcon(R.drawable.location_off);
		mainActiviryIntent.putExtra(AppConsts.EXTRA_MESSAGE_LOCATION, false);
		txtAddress.setVisibility(View.GONE);
	}

	public void createTask(View view) {
		Log.i("AddTask", "create task");
		double latitude = 0;
		double longitude = 0;
		String descriptionTaskToPass = descriptionEt.getText().toString();
		if (chosenAddress != null) {
			try {
				addresses = geocoder.getFromLocationName(chosenAddress, 1);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (addresses.size() > 0) {
				latitude = addresses.get(0).getLatitude();
				longitude = addresses.get(0).getLongitude();
			}
		}
		boolean alarm;
		if (item_alarm.getTitle().equals("Alarm Off"))
			alarm = false;
		else
			alarm = true;
		boolean imp;
		if (item_importance.getTitle().equals("not important task"))
			imp = false;
		else
			imp = true;
		boolean geo;
		if (item_location.getTitle().equals("geofence not active"))
			geo = false;
		else
			geo = true;

		mainActiviryIntent.putExtra(AppConsts.EXTRA_MESSAGE_DESC,
				descriptionTaskToPass);
		mainActiviryIntent.putExtra(AppConsts.EXTRA_MESSAGE_MILLIS,
				myCalendar.getTimeInMillis());
		mainActiviryIntent.putExtra(AppConsts.EXTRA_MESSAGE_ALARM, alarm);
		mainActiviryIntent.putExtra(AppConsts.EXTRA_MESSAGE_IMPORTANCE, imp);
		mainActiviryIntent.putExtra(AppConsts.EXTRA_MESSAGE_LOCATION, geo);
		mainActiviryIntent.putExtra(AppConsts.EXTRA_MESSAGE_ADDRESS,
				chosenAddress);
		mainActiviryIntent.putExtra(AppConsts.EXTRA_MESSAGE_LATITUDE, latitude);
		mainActiviryIntent.putExtra(AppConsts.EXTRA_MESSAGE_LONGITUDE,
				longitude);
		if (getParent() == null) {
			setResult(Activity.RESULT_OK, mainActiviryIntent);
		} else {
			getParent().setResult(Activity.RESULT_OK, mainActiviryIntent);
		}
		finish();
	}

	/**
	 * @return true if Internet connection is available otherwise it returns
	 *         false
	 */
	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnectedOrConnecting();
	}
	
	public void checkForFacebook(String str) {
		String desc = str;
		desc = descriptionEt.getText().toString();
		if (desc != null && !desc.isEmpty() && isOnline() ) {
			shareBt.setShareContent(new ShareLinkContent.Builder()
			.setContentTitle("Do It Simple")
			.setContentDescription("Look at my task: "+desc)
			.setContentUrl(Uri.parse("http://play.google.com/store/apps/collection/editors_choice")).build());
			
			shareBt.setEnabled(true);
		} else 
			shareBt.setEnabled(false);
	}

}
