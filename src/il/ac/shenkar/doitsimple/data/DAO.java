package il.ac.shenkar.doitsimple.data;

import il.ac.shenkar.doitsimple.common.Task;
import il.ac.shenkar.doitsimple.data.TasksDbContract.TaskEntry;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * the function purpose is to perform a requests to the data base 
 *
 */
public class DAO implements IDataAccess{

	private static DAO instance;
	private Context context;
	private TasksDBHelper dbHelper;
	private String[] tasksColumns = { TaskEntry._ID, TaskEntry.COLUMN_TASK_DESC,
			TaskEntry.COLUMN_DateTime,TaskEntry.COLUMN_Status,TaskEntry.COLUMN_Alarm, 
			TaskEntry.COLUMN_Importance, TaskEntry.COLUMN_Geo, TaskEntry.COLUMN_Address };
	// Database fields
	private SQLiteDatabase database;
	
	DAO(Context context) {
		this.context = context;
		dbHelper = new TasksDBHelper(context);
	}
	
	/**
	 * single tone of the DAO class 
	 * @param context the context that received 
	 * @return instance of the DAO
	 */
	public static DAO getInstance(Context context)
	{
		if(instance ==  null)
			instance = new DAO(context);
		return instance;
	}
	
	@Override
	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	@Override
	public void close() {
		dbHelper.close();
	}

	@Override
	public ArrayList<Task> getTaskList() {
		ArrayList<Task> tasks = new ArrayList<Task>();
		Cursor cursor = database.query(TaskEntry.TABLE_NAME, tasksColumns, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Task t = cursorToTask(cursor);
				tasks.add(t);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return tasks;
	}
	
	/**
	 * Create task object from the cursor.
	 * @param cursor the cursor the received
	 * @return the task we ask for 
	 */
	private Task cursorToTask(Cursor cursor) {
		Task t = new Task();
		t.setId(cursor.getInt(cursor.getColumnIndex(TaskEntry._ID)));
		Log.i("ID-TASK","IS :   "+cursor.getInt(cursor.getColumnIndex(TaskEntry._ID)));
		t.setItemDescription(cursor.getString(cursor.getColumnIndex(TaskEntry.COLUMN_TASK_DESC)));
		t.setCalendarInMillis(cursor.getLong(cursor.getColumnIndex(TaskEntry.COLUMN_DateTime)));
		t.setStatus(cursor.getInt(cursor.getColumnIndex(TaskEntry.COLUMN_Status)));
		if (cursor.getInt(cursor.getColumnIndex(TaskEntry.COLUMN_Alarm))==1)
			t.setAlarm(true);
		else 
			t.setAlarm(false);
		if (cursor.getInt(cursor.getColumnIndex(TaskEntry.COLUMN_Importance))==1)
			t.setImportance(true);
		else 
			t.setImportance(false);
		if (cursor.getInt(cursor.getColumnIndex(TaskEntry.COLUMN_Geo))==1) {
			t.setLocation(true);
			t.setAddress(cursor.getString(cursor.getColumnIndex(TaskEntry.COLUMN_Address)));
		}else 
			t.setLocation(false);
		return t;
	}
	
	@Override
	public Task addTask(Task task) {
		Log.i("DAO", "addTask");
		
		if (task == null)
			return null;
		//build the content values.
		ContentValues values = putValues(task);
		
		//do the insert.
		long insertId = database.insert(TaskEntry.TABLE_NAME, null, values);
		Log.i("DAO: addTask","insertId: "+insertId);
		
		//get the entity from the data base - extra validation, entity was insert properly.
		Cursor cursor = database.query(TaskEntry.TABLE_NAME, tasksColumns,
				TaskEntry._ID + " = " + insertId, null, null, null, null);
		cursor.moveToFirst();
		//create the task object from the cursor.
		Task newTask = cursorToTask(cursor);
		cursor.close();
		return newTask;
	}

	@Override
	public void removeTask(Task task) {
		long id = task.getId();
		database.delete(TaskEntry.TABLE_NAME, TaskEntry._ID + " = " + id,null);
	}
	
	@Override
	public void updateTask(Task task) {
		long id= task.getId();
		ContentValues values = putValues(task);
		database.update(TaskEntry.TABLE_NAME, values, TaskEntry._ID + " = " + id, null);
	}
	
	@Override
	public void changeTaskStatus(Task task){
		long id= task.getId();
		ContentValues values = new ContentValues();
		values.put(TaskEntry.COLUMN_Status,task.getStatus()); 
		database.update(TaskEntry.TABLE_NAME, values, TaskEntry._ID + " = " + id, null);
	}

	/**
	 * crate content values form a task parameters
	 * @param task the task we want  to get the parameters from it
	 * @return the values that we will need to crate in the table 
	 */
	public ContentValues putValues(Task task) {
		ContentValues values = new ContentValues();
		values.put(TaskEntry.COLUMN_TASK_DESC, task.getItemDescription());
		values.put(TaskEntry.COLUMN_DateTime, task.getCalendarInMillis());
		values.put(TaskEntry.COLUMN_Status,task.getStatus());
		values.put(TaskEntry.COLUMN_Alarm, task.isAlarm());
		values.put(TaskEntry.COLUMN_Importance, task.isImportance());
		values.put(TaskEntry.COLUMN_Geo, task.isLocation());
		values.put(TaskEntry.COLUMN_Address, task.getAddress());
		return values;
	}

}