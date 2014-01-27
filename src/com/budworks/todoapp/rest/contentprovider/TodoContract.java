package com.budworks.todoapp.rest.contentprovider;

import android.net.Uri;

import com.budworks.todoapp.model.Todo;

public interface TodoContract {

	// we always use a full representation of the DataItems, i.e. we will always
	// have all attributes available and can use constants for the column
	// indices
	public static final String[] COLUMN_NAMES = { Todo.KEY_ID, Todo.KEY_NAME,
			Todo.KEY_DESC, Todo.KEY_DONE, Todo.KEY_PRIO, Todo.KEY_DATE };

	/*
	 * the mime types for single and multiple todos
	 */

	public static final String MIMETYPE_TODO_ITEM = "vnd.android.cursor.item/org.dieschnittstelle.mobile.android.todo";

	public static final String MIMETYPE_TODO_LIST = "vnd.android.cursor.dir/org.dieschnittstelle.mobile.android.todo";

	/*
	 * the uri prefix for todo
	 */
	public static final String URI_TODO_LIST_AUTHORITY = "org.dieschnittstelle.mobile.android.todolist.model";

	public static final String URI_TODO_LIST_PATH = "todo";

	public static final Uri CONTENT_URI = new Uri.Builder().scheme("content")
			.authority(URI_TODO_LIST_AUTHORITY).appendPath(URI_TODO_LIST_PATH)
			.build();

	public static final String METHOD_SET_BASE_URL = "setBaseUrl";

}
