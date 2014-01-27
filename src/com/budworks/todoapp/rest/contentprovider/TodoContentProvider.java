package com.budworks.todoapp.rest.contentprovider;

import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.budworks.todoapp.crud.ITodoCRUDOperations;
import com.budworks.todoapp.model.Todo;

public class TodoContentProvider extends ContentProvider {

	private static final String LOG_TAG = TodoContentProvider.class
			.getSimpleName();

	/**
	 * the baseUrl
	 */
	private String baseUrl = "http://10.0.2.2:8080/TodolistWebapp/";

	// the accessor for the data source
	private ITodoCRUDOperations datasourceAccessor;

	public TodoContentProvider() {
		Log.i(LOG_TAG, "creating DataItemContentProvider()");
	}

	// construct
	public TodoContentProvider(ITodoCRUDOperations datasourceAccessor) {
		Log.i(LOG_TAG,
				"creating DataItemContentProvider() using datasourceAccessor: "
						+ datasourceAccessor);
		this.datasourceAccessor = datasourceAccessor;
	}

	@Override
	public int delete(Uri uri, String arg1, String[] arg2) {
		Log.d(LOG_TAG, "delete(): uri is: " + uri);
		if (isTodoListUri(uri)) {
			// we only use single row delete, using the last path segment
			List<String> uriPathSegments = uri.getPathSegments();
			String userIdSegment = uriPathSegments.get(uriPathSegments.size() - 2);
			String lastSegment = uriPathSegments.get(uriPathSegments.size() - 1);
			Log.d(LOG_TAG, "delete(): user id: " + userIdSegment
					+ " last path segment: " + lastSegment);

			if (lastSegment != null) {
				if (this.datasourceAccessor.deleteTodo(userIdSegment,
						Long.parseLong(lastSegment))) {
					return 1;
				}
			}

			return 0;
		}

		String err = "cannot handle unknown uri: " + uri;
		throw new RuntimeException(err);
	}

	@Override
	public String getType(Uri uri) {
		Log.d(LOG_TAG, "getType() for uri with path: " + uri.getPath());
		if (TodoContract.URI_TODO_LIST_AUTHORITY.equals(uri.getAuthority())
				&& (uri.getPath() == null || "".equals(uri.getPath()))) {
			return TodoContract.MIMETYPE_TODO_LIST;
		} else {
			return TodoContract.MIMETYPE_TODO_ITEM;
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.d(LOG_TAG, "insert(): uri is: " + uri);
		Log.d(LOG_TAG, "insert(): values are: " + values);

		if (isTodoListUri(uri)) {
			// create a DataItem object
			Todo todo = createTodoFromContentValues(values);
			Log.d(LOG_TAG, "insert(): item is: " + todo);

			todo = datasourceAccessor.createTodo(todo);

			// now we read out the id and create the uri from it
			uri =
					new Uri.Builder()
							.authority(TodoContract.URI_TODO_LIST_AUTHORITY)
							.appendPath(TodoContract.URI_TODO_LIST_PATH)
							.appendPath(String.valueOf(todo.getId())).build();
			Log.d(LOG_TAG, "insert(): will return uri: " + uri);

			return uri;
		}

		String err = "cannot handle unknown uri: " + uri;
		throw new RuntimeException(err);
	}

	@Override
	public boolean onCreate() {
		Log.i(LOG_TAG, "onCreate()");

		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] arg1, String arg2, String[] arg3,
			String arg4) {
		Log.i(LOG_TAG, "query() on uri: " + uri);

		if (uri == null || isTodoListUri(uri)) {
			List<String> uriPathSegments = uri.getPathSegments();
			String userIdSegment = uriPathSegments.get(uriPathSegments.size() - 1);
			// we always query the full content set
			return new TodoListCursor(
					this.datasourceAccessor.readAllTodos(userIdSegment),
					this.datasourceAccessor);
		}

		String err = "cannot handle unknown uri: " + uri;
		throw new RuntimeException(err);
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return 0;
	}

	private boolean isTodoListUri(Uri uri) {
		return TodoContract.URI_TODO_LIST_AUTHORITY.equals(uri.getAuthority())
				&& TodoContract.URI_TODO_LIST_PATH.equals(uri.getPathSegments()
						.get(0));
	}

	private Todo createTodoFromContentValues(ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

}
