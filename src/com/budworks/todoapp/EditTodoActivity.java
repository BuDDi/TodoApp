package com.budworks.todoapp;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import com.budworks.todoapp.model.Todo;
import com.budworks.todoapp.model.Todo.Priority;

public class EditTodoActivity extends Activity {

	public static final String KEY_EDIT_TODO = "edit";

	public static final String KEY_DELETE_TODO = "delete";

	private Todo todo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edittodo);
		todo = (Todo) getIntent().getExtras().get("todo");
		Toast.makeText(getBaseContext(),
				"Got todo from Bundle: " + todo.getName(), Toast.LENGTH_SHORT)
				.show();
		final CheckBox doneCheckBox =
				(CheckBox) findViewById(R.id.edit_todo_done);
		final EditText nameTxtField =
				(EditText) findViewById(R.id.edit_todo_name);
		final EditText descTxtField = (EditText) findViewById(R.id.todo_desc);
		final RadioGroup prioGroup = (RadioGroup) findViewById(R.id.radioPrio);
		final TimePicker timePicker = (TimePicker) findViewById(R.id.todo_time);
		final DatePicker datePicker = (DatePicker) findViewById(R.id.todo_date);

		doneCheckBox.setChecked(todo.isDone());
		nameTxtField.setText(todo.getName());
		if (todo.isDone()) {
			nameTxtField.setPaintFlags(nameTxtField.getPaintFlags()
					| Paint.STRIKE_THRU_TEXT_FLAG);
		} else {
			nameTxtField.setPaintFlags(nameTxtField.getPaintFlags()
					& (~Paint.STRIKE_THRU_TEXT_FLAG));
		}
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
		doneCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					nameTxtField.setPaintFlags(nameTxtField.getPaintFlags()
							| Paint.STRIKE_THRU_TEXT_FLAG);
				} else {
					nameTxtField.setPaintFlags(nameTxtField.getPaintFlags()
							& (~Paint.STRIKE_THRU_TEXT_FLAG));
				}
			}
		});

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
				Date date = UtilsUI.getDateFromDatePicker(datePicker, timePicker);
				todo.setDone(doneCheckBox.isChecked());
				todo.setName(name);
				todo.setDescription(desc);
				todo.setPriority(priority);
				todo.setDate(date);
				// datePicker.getDayOfMonth();
				showMainActivity(KEY_EDIT_TODO, todo);
			}
		});

		final Button deleteButton = (Button) findViewById(R.id.buttonDelete);
		deleteButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder builder =
						new AlertDialog.Builder(EditTodoActivity.this);
				builder
						.setTitle("Delete")
						.setMessage(
								"Do you want to delete Todo \"" + todo.getName() + "\"")
						.setCancelable(true)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										showMainActivity(KEY_DELETE_TODO, todo);
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int id) {
										dialog.cancel();
									}
								});
				AlertDialog alert = builder.create();
				alert.show();
			}
		});

		final Button cancelButton = (Button) findViewById(R.id.buttonCancel);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showMainActivity(null, null);
			}

		});
	}

	private void showMainActivity(String actionKey, Todo todo) {
		Intent showOverviewIntent =
				new Intent(getBaseContext(), TodoOverviewActivity.class);
		if (actionKey != null && todo != null) {
			showOverviewIntent.putExtra(actionKey, todo);
		}
		startActivity(showOverviewIntent);
	}

}
