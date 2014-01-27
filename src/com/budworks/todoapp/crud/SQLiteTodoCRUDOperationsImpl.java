package com.budworks.todoapp.crud;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.budworks.todoapp.model.Todo;

public class SQLiteTodoCRUDOperationsImpl implements
		ITodoCRUDOperationsWithContext {

	private static final String LOG_TAG = SQLiteTodoCRUDOperationsImpl.class
			.getName();

	/**
	 * the where clause for item deletion
	 */
	private static final String WHERE_IDENTIFY_ITEM = Todo.KEY_ID + "=?";

	// Database Version
	private static final int INITIAL_DBVERSION = 0;

	// Database Name
	private static final String DATABASE_NAME = "todo_db";

	private static final String CREATE_TODO_TABLE = "CREATE TABLE "
			+ Todo.TABLE_NAME + "(" + Todo.KEY_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT," + Todo.KEY_NAME
			+ " TEXT NOT NULL," + Todo.KEY_DESC + " TEXT," + Todo.KEY_DONE
			+ " INTEGER DEFAULT 0," + Todo.KEY_PRIO + " INTEGER DEFAULT 1,"
			+ Todo.KEY_DATE + " INTEGER" + ")";

	private Context context;

	private SQLiteDatabase db;

	// Adding new todo
	@Override
	public Todo createTodo(Todo todo) {
		ContentValues values = SQLiteUtils.getTodoContentValues(todo);

		// Inserting Row
		long newTodoId = db.insert(Todo.TABLE_NAME, null, values);
		Log.i(LOG_TAG, "addTodoToDb(): got new item id after insertion: "
				+ newTodoId);
		// db.close(); // Closing database connection
		todo.setId(newTodoId);
		return todo;
	}

	// Getting single todo
	@Override
	public Todo readTodo(String userIdNotUsed, long id) {
		Cursor cursor =
				db.query(Todo.TABLE_NAME, new String[] { Todo.KEY_ID,
						Todo.KEY_NAME, Todo.KEY_DESC, Todo.KEY_DONE, Todo.KEY_PRIO,
						Todo.KEY_DATE }, Todo.KEY_ID + "=?",
						new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}

		// return todo
		return SQLiteUtils.getTodoFromCursor(cursor);
	}

	// Getting All Todos
	@Override
	public List<Todo> readAllTodos(String userIdNotUsed) {
		List<Todo> todoList = new ArrayList<Todo>();

		// we make a query, which possibly will return an empty list
		SQLiteQueryBuilder querybuilder = new SQLiteQueryBuilder();
		querybuilder.setTables(Todo.TABLE_NAME);
		// we specify all columns
		String[] asColumsToReturn =
				{ Todo.KEY_ID, Todo.KEY_NAME, Todo.KEY_DESC, Todo.KEY_DONE,
						Todo.KEY_PRIO, Todo.KEY_DATE };
		// we specify an ordering
		String ordering = Todo.KEY_ID + " ASC";

		Cursor c =
				querybuilder.query(this.db, asColumsToReturn, null, null, null,
						null, ordering);

		Log.i(LOG_TAG, "getAdapter(): got a cursor: " + c);

		c.moveToFirst();
		while (!c.isAfterLast()) {
			// create a new todo and add it to the list
			todoList.add(SQLiteUtils.getTodoFromCursor(c));
			c.moveToNext();
		}

		Log.i(LOG_TAG, "readOutItemsFromDatabase(): read out items: " + todoList);

		return todoList;
	}

	// Getting todos Count
	public int getTodosCount() {
		String countQuery = "SELECT  * FROM " + Todo.TABLE_NAME;
		Cursor cursor = db.rawQuery(countQuery, null);
		cursor.close();

		// return count
		return cursor.getCount();
	}

	/**
	 * 
	 * @param todo
	 * @return the number of rows affected
	 */
	@Override
	public Todo updateTodo(Todo todo) {
		Log.i(LOG_TAG, "updateTodo(): " + todo);

		// do the update in the db
		this.db.update(Todo.TABLE_NAME, SQLiteUtils.getTodoContentValues(todo),
				WHERE_IDENTIFY_ITEM, new String[] { String.valueOf(todo.getId()) });
		Log.i(LOG_TAG, "updateTodoInDb(): update has been carried out");

		return todo;
	}

	// Deleting single contact
	@Override
	public boolean deleteTodo(String userIdNotUsed, long id) {
		Log.i(LOG_TAG, "deleteTodo(): " + id);

		// we first delete the item
		this.db.delete(Todo.TABLE_NAME, WHERE_IDENTIFY_ITEM,
				new String[] { String.valueOf(id) });
		Log.i(LOG_TAG, "deleteTodo(): deletion in db done");
		return true;
	}

	@Override
	public void setContext(Context context) {
		this.context = context;
		prepareSQLiteDatabase();
	}

	@Override
	public void finalise() {
		this.db.close();
		Log.i(LOG_TAG, "db has been closed");
	}

	private void prepareSQLiteDatabase() {
		this.db =
				context.openOrCreateDatabase(DATABASE_NAME,
						SQLiteDatabase.CREATE_IF_NECESSARY, null);

		// we need to check whether it is empty or not...
		Log.d(LOG_TAG, "db version is: " + db.getVersion());
		if (this.db.getVersion() == INITIAL_DBVERSION) {
			Log.i(LOG_TAG,
					"the db has just been created. Need to create the table...");
			db.setLocale(Locale.getDefault());
			// db.setLockingEnabled(true);
			db.setVersion(INITIAL_DBVERSION + 1);
			db.execSQL(CREATE_TODO_TABLE);
		} else {
			Log.i(LOG_TAG, "the db exists already. No need for table creation.");
		}
	}

	@Override
	public boolean deleteAllTodos(String userIdNotUsed) {
		throw new RuntimeException("Not yet implemented");
	}
}
