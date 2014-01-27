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
import android.widget.Toast;

import com.budworks.todoapp.model.Todo;
import com.budworks.todoapp.model.Todo.Priority;

public class EditTodoActivity extends Activity {

	public static final String KEY_EDIT_TODO = "edit";

	private Todo todo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_newtodo);
		todo = (Todo) getIntent().getExtras().get("todo");
		Toast.makeText(getBaseContext(),
				"Got todo from Bundle: " + todo.getName(), Toast.LENGTH_SHORT)
				.show();
		final EditText nameTxtField = (EditText) findViewById(R.id.edit_todo_name);
		final EditText descTxtField = (EditText) findViewById(R.id.todo_desc);
		final RadioGroup prioGroup = (RadioGroup) findViewById(R.id.radioPrio);
		final TimePicker timePicker = (TimePicker) findViewById(R.id.todo_time);
		final DatePicker datePicker = (DatePicker) findViewById(R.id.todo_date);

		nameTxtField.setText(todo.getName());
		descTxtField.setText(todo.getDescription());
		int button2Select = -1;
		switch (todo.getPriority()) {
		case Low:
			button2Select = R.id.prioLow;
			break;
		case Normal:
			button2Select = R.id.prioNormal;
			break;
		case High:
			button2Select = R.id.prioHigh;
			break;
		}
		prioGroup.check(button2Select);
		Date date = todo.getDate();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		timePicker.setCurrentHour(calendar.get(Calendar.HOUR));
		timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
		datePicker.updateDate(calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
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
				todo.setName(name);
				todo.setDescription(desc);
				todo.setPriority(priority);
				todo.setDate(date);
				// datePicker.getDayOfMonth();
				showMainActivity(todo);
			}
		});

		final Button cancelButton = (Button) findViewById(R.id.buttonCancel);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showMainActivity(null);
			}

		});
	}

	private void showMainActivity(Todo todo) {
		Intent showOverviewIntent = new Intent(getBaseContext(),
				TodoOverviewActivity.class);
		showOverviewIntent.putExtra(KEY_EDIT_TODO, todo);
		startActivity(showOverviewIntent);
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
