package com.budworks.todoapp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.budworks.todoapp.model.Todo;

public class TodoListAdapter implements ListAdapter {

	private java.text.DateFormat dateFormatter = SimpleDateFormat
			.getDateTimeInstance();

	private Context context;

	private List<Todo> todoList;

	private List<DataSetObserver> observer = new ArrayList<DataSetObserver>();

	public TodoListAdapter(Context context, List<Todo> values) {
		this.context = context;
		this.todoList = values;
		init();
	}

	private void init() {
		// sort the todo list by date first
		Collections.sort(todoList, new Comparator<Todo>() {

			@Override
			public int compare(Todo lhs, Todo rhs) {
				return lhs.getDate().compareTo(rhs.getDate());
			}
		});
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public int getCount() {
		return todoList.size();
	}

	@Override
	public Object getItem(int index) {
		return todoList.get(index);
	}

	@Override
	public long getItemId(int index) {
		return index;
	}

	@Override
	public int getItemViewType(int index) {
		// get the marked view type for todos over the expiration date and still
		// not done
		return 0;
	}

	@Override
	public View getView(int index, View convertView, ViewGroup parent) {
		LayoutInflater inflater =
				(LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = null;
		if (convertView != null) {
			rowView = convertView;
		} else {
			rowView = inflater.inflate(R.layout.row_todo, parent, false);
		}
		Todo todo = todoList.get(index);
		CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.row_todo_done);
		TextView nameLabel = (TextView) rowView.findViewById(R.id.row_todo_name);
		TextView descriptionLabel =
				(TextView) rowView.findViewById(R.id.row_todo_desc);
		TextView prioLabel = (TextView) rowView.findViewById(R.id.row_todo_prio);
		TextView dateLabel = (TextView) rowView.findViewById(R.id.row_todo_date);
		checkBox.setOnCheckedChangeListener(null);
		checkBox.setChecked(todo.isDone());
		nameLabel.setText(todo.getName());
		if (todo.isDone()) {
			nameLabel.setPaintFlags(nameLabel.getPaintFlags()
					| Paint.STRIKE_THRU_TEXT_FLAG);
		} else {
			nameLabel.setPaintFlags(nameLabel.getPaintFlags()
					& (~Paint.STRIKE_THRU_TEXT_FLAG));
		}
		checkBox.setOnCheckedChangeListener(new DoneCheckedListener(nameLabel,
				todo));
		descriptionLabel.setText(todo.getDescription());
		prioLabel.setText("Priority: " + todo.getPriority().toString());
		dateLabel.setText(dateFormatter.format(todo.getDate()));
		// mark the rowView to display expired, and not yet done todos
		Date now = new Date();
		if (!todo.isDone() && todo.getDate().before(now)) {
			rowView.setBackgroundResource(R.drawable.dark_red_gradient);
		} else {
			rowView.setBackgroundColor(Color.parseColor("#00000000"));
		}
		return rowView;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return todoList.isEmpty();
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		this.observer.add(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		this.observer.remove(observer);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	private class DoneCheckedListener implements OnCheckedChangeListener {

		private TextView nameLabel;

		private Todo todo;

		private DoneCheckedListener(TextView nameLabel, Todo todo) {
			this.nameLabel = nameLabel;
			this.todo = todo;
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			todo.setDone(isChecked);
			if (isChecked) {
				nameLabel.setPaintFlags(nameLabel.getPaintFlags()
						| Paint.STRIKE_THRU_TEXT_FLAG);
			} else {
				nameLabel.setPaintFlags(nameLabel.getPaintFlags()
						& (~Paint.STRIKE_THRU_TEXT_FLAG));
			}
			TodoOverviewActivity overViewActivity = (TodoOverviewActivity) context;
			overViewActivity.doUpdateItem(todo);
		}

	}

	public void add(Todo todo) {
		todoList.add(todo);
		// SnotifyDataSetChanged();
	}

	public void addAll(Collection<? extends Todo> todos) {
		todoList.addAll(todos);
		// notifyDataSetChanged();
	}

	public void remove(Todo todo) {
		todoList.remove(todo);
		// notifyDataSetChanged();
	}

	public void clear() {
		todoList.clear();
		// notifyDataSetChanged();
	}

	public int indexOf(Todo todo) {
		return todoList.indexOf(todo);
	}

	public Todo get(int index) {
		return todoList.get(index);
	}

	public void notifyDataSetChanged() {
		for (DataSetObserver obs : observer) {
			obs.onChanged();
		}
	}

	public List<Todo> getAllTodos() {
		return todoList;
	}
}
