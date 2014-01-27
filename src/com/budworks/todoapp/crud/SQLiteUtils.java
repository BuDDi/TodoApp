package com.budworks.todoapp.crud;

import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;

import com.budworks.todoapp.model.Todo;
import com.budworks.todoapp.model.Todo.Priority;

public class SQLiteUtils {

	protected static Todo getTodoFromCursor(Cursor cursor) {
		return new Todo(cursor.getLong(0), null, cursor.getString(1),
				cursor.getString(2), cursor.getShort(3) > 0,
				Priority.getForOrdinal(cursor.getShort(4)), new Date(
						cursor.getLong(5)));
	}

	protected static ContentValues getTodoContentValues(Todo todo) {
		ContentValues values = new ContentValues();
		values.put(Todo.KEY_NAME, todo.getName()); // Todo Name
		values.put(Todo.KEY_DESC, todo.getDescription());
		values.put(Todo.KEY_DONE, todo.isDone());
		values.put(Todo.KEY_PRIO, todo.getPriority().ordinal());
		values.put(Todo.KEY_DATE, todo.getDate().getTime());
		return values;
	}
}
