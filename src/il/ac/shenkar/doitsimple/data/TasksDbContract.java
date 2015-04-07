package il.ac.shenkar.doitsimple.data;

import android.provider.BaseColumns;

/**
 * Defines table and column names for the tasks database.
 */

public class TasksDbContract {
	  /* Inner class that defines the table contents of all tasks */
		 public static final class TaskEntry implements BaseColumns {

		        // Table name
		        public static final String TABLE_NAME = "tasks";
		        
		        // Column names
		        public static final String COLUMN_TASK_DESC = "task_description";
		        public static final String COLUMN_DateTime = "date_and_time";
		        public static final String COLUMN_Status = "status";
		        public static final String COLUMN_Alarm = "alarm";
		        public static final String COLUMN_Importance = "importance";
		        public static final String COLUMN_Geo = "location";
		        public static final String COLUMN_Address = "address";

		    }
 }
