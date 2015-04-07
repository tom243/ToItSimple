package il.ac.shenkar.doitsimple.data;

import il.ac.shenkar.doitsimple.common.Task;

import java.util.ArrayList;

import android.database.SQLException;

/**
 * Interface for the DAO class
 *
 */
public interface IDataAccess {

	/**
	 * open a connection to the data base
	 * @throws SQLException
	 */
	void open() throws SQLException;
	
	/**
	 * close the connection to the data base
	 */
	void close();
	
	/**
	 * the function retruns the list from the data base 
	 * @return the list form the data base 
	 */
	ArrayList<Task> getTaskList();

	/**
	 * add task to the data base 
	 * @param task the task we want to add to the data base 
	 * @return the task we added
	 */
	Task addTask(Task task);
	
	/**
	 * remove task to the data base 
	 * @param task  the task we want to remove from the data base
	 */
	void removeTask(Task task);
	
	/**
	 * update the task in the data base 
	 * @param task the task that we want to update 
	 */
	void updateTask(Task task);
	
	/** 
	 * change the status of the task to done or undone in the data base
	 * @param task the task that we want to update 
	 */
	void changeTaskStatus(Task task);
}
