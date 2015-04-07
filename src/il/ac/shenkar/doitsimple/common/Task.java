package il.ac.shenkar.doitsimple.common;

public class Task {
	
	private int id;
	private String itemDescription;
	private long calendarInMillis;
	private int status;
	private boolean importance;
	private boolean alarm;
	private boolean location;
	private String address;
	
	public Task() {
		super();
		this.status = 0; //Means that the task is undone
		this.alarm = false;
		this.importance = false;
		this.location = false;
	}
	
	public Task(String itemDescription,long calendarInMillis, boolean alarm, boolean importance, boolean location, String address) {
		super();
		this.itemDescription = itemDescription;
		this.calendarInMillis = calendarInMillis;
		this.status = 0;
		this.alarm = alarm;
		this.importance = importance;
		this.location = location;
		this.address = address;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getItemDescription() {
		return itemDescription;
	}

	public void setItemDescription(String itemDescription) {
		this.itemDescription = itemDescription;
	}
	
	public long getCalendarInMillis() {
		return calendarInMillis;
	}

	public void setCalendarInMillis(long calendarInMillis) {
		this.calendarInMillis = calendarInMillis;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	public boolean isImportance() {
		return importance;
	}

	public void setImportance(boolean importance) {
		this.importance = importance;
	}

	public boolean isAlarm() {
		return alarm;
	}

	public void setAlarm(boolean alarm) {
		this.alarm = alarm;
	}

	public boolean isLocation() {
		return location;
	}

	public void setLocation(boolean location) {
		this.location = location;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

}

