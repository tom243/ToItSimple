package il.ac.shenkar.doitsimple;

import il.ac.shenkar.doitsimple.common.Task;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TaskListBaseAdapter extends BaseAdapter { 

	private ArrayList<Task> tasksList ;
	private Context context;
	
	static class ViewHolder {
		TextView tvDesc;
		ImageView imgAlarm, imgGeo, imgStar;
	}

	public TaskListBaseAdapter(Context context,ArrayList<Task> tasksList) {
		this.tasksList = tasksList;
		this.context = context;
	}

	@Override
	public int getCount() {
		return tasksList.size();
	}

	@Override
	public Object getItem(int position) {
		if (this.tasksList != null && this.tasksList.size() > position)  {
			return this.tasksList.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void UpdateDataSource(ArrayList<Task> tasksList) {
		if(tasksList == null) 
			return; 
		this.tasksList = tasksList;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.task_row, null);
			holder = new ViewHolder();
			holder.tvDesc = (TextView) convertView.findViewById(R.id.existing_task_description);
			holder.imgAlarm = (ImageView) convertView.findViewById(R.id.alarmPic);
			holder.imgGeo = (ImageView) convertView.findViewById(R.id.location);
			holder.imgStar = (ImageView) convertView.findViewById(R.id.importance);
			holder.tvDesc.setTextColor(Color.WHITE);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.tvDesc.setText(tasksList.get(position).getItemDescription());
		Task t = tasksList.get(position);
		// edit the alarm icon blue/grey
		if (t.isAlarm()) 
			holder.imgAlarm.setImageResource(R.drawable.alarm_on);
		else
			holder.imgAlarm.setImageResource(R.drawable.alarm_off);
		// edit the GEO icon green/grey
		if (t.isLocation()) // GEO added
			holder.imgGeo.setImageResource(R.drawable.location_on);
		else
			holder.imgGeo.setImageResource(R.drawable.location_off);
		// edit the importance icon orange/grey
		if (t.isImportance()) // is it important 
			holder.imgStar.setImageResource(R.drawable.importance_on);
		else
			holder.imgStar.setImageResource(R.drawable.importance_off);

		return convertView;
	}

}

