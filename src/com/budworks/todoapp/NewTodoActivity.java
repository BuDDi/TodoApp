package com.budworks.todoapp;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TimePicker;

import com.budworks.todoapp.model.Todo;
import com.budworks.todoapp.model.Todo.Priority;

public class NewTodoActivity extends Activity {

	public static final String KEY_NEW_TODO = "new";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_newtodo);

		final EditText nameTxtField =
				(EditText) findViewById(R.id.edit_todo_name);
		final EditText descTxtField = (EditText) findViewById(R.id.todo_desc);
		final RadioGroup prioGroup = (RadioGroup) findViewById(R.id.radioPrio);
		final TimePicker timePicker = (TimePicker) findViewById(R.id.todo_time);
		final DatePicker datePicker = (DatePicker) findViewById(R.id.todo_date);

		prioGroup.check(R.id.prioNormal);
		final Button okButton = (Button) findViewById(R.id.buttonOk);
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// save the todo
				// dbHelper = new TodoDatabaseHelper(this);
				String name = nameTxtField.getText().toString();
				String desc = descTxtField.getText().toString();
				int radioBtnId = prioGroup.getCheckedRadioButtonId();
				View checkedRadioBtn = findViewById(radioBtnId);
				int selectedRadioIndex = prioGroup.indexOfChild(checkedRadioBtn);
				Priority priority = Priority.getForOrdinal(selectedRadioIndex);
				Date date = getDateFromDatePicket(timePicker, datePicker);
				// we don't need the user id for local stuff
				Todo newTodo = new Todo(null, name, desc, false, priority, date);
				// datePicker.getDayOfMonth();
				showOverviewActivity(newTodo);
			}
		});

		final Button cancelButton = (Button) findViewById(R.id.buttonCancel);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showOverviewActivity(null);
			}

		});
	}

	private void showOverviewActivity(Todo todo) {
		Intent showOveriewIntent =
				new Intent(getBaseContext(), TodoOverviewActivity.class);
		// add the new todo to the bundle of the main activity
		showOveriewIntent.putExtra(KEY_NEW_TODO, todo);
		startActivity(showOveriewIntent);
	}

	/**
	 * 
	 * @param datePicker
	 * @return a java.util.Date
	 */
	private static Date getDateFromDatePicket(TimePicker timePicker,
			DatePicker datePicker) {
		int minute = timePicker.getCurrentMinute();
		int hour = timePicker.getCurrentHour();
		int day = datePicker.getDayOfMonth();
		int month = datePicker.getMonth();
		int year = datePicker.getYear();

		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, day, hour, minute);

		return calendar.getTime();
	}

}
