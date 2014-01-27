package com.budworks.todoapp.rest.contentprovider;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.budworks.todoapp.crud.ITodoCRUDOperations;
import com.budworks.todoapp.model.Todo;

public class TodoListCursor implements Cursor {

	private static final String LOG_TAG = TodoListCursor.class.getSimpleName();

	// we use a list of items
	private List<Todo> todos;

	// the current element position
	private int currentItemPosition = -1;

	// the accessor that we can use to access the data source
	private ITodoCRUDOperations dataItemAccessor;

	// observers
	private List<DataSetObserver> dataSetObservers =
			new ArrayList<DataSetObserver>();

	private List<ContentObserver> contentObservers =
			new ArrayList<ContentObserver>();

	// instantiate the cursor with a list of items and an accessor which we can
	// use for requerying...
	public TodoListCursor(List<Todo> todos, ITodoCRUDOperations dataItemAccessor) {
		this.todos = todos;
		this.dataItemAccessor = dataItemAccessor;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void copyStringToBuffer(int arg0, CharArrayBuffer arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deactivate() {
		// TODO Auto-generated method stub

	}

	@Override
	public byte[] getBlob(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getColumnCount() {
		return TodoContract.COLUMN_NAMES.length;
	}

	@Override
	public int getColumnIndex(String column) {
		for (int i = 0; i < TodoContract.COLUMN_NAMES.length; i++) {
			if (TodoContract.COLUMN_NAMES[i].equals(column)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public int getColumnIndexOrThrow(String arg0)
			throws IllegalArgumentException {
		return getColumnIndex(arg0);
	}

	@Override
	public String getColumnName(int columnIndex) {
		// Log.d(logger, "getColumnName(): " + columnIndex);
		Log.e(LOG_TAG, "got unknown column index: " + columnIndex);
		return TodoContract.COLUMN_NAMES[columnIndex];
	}

	@Override
	public String[] getColumnNames() {
		return TodoContract.COLUMN_NAMES;
	}

	@Override
	public int getCount() {
		return todos.size();
	}

	@Override
	public double getDouble(int columnIndex) {
		return (Double) getColumnValue(columnIndex);
	}

	@Override
	public Bundle getExtras() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getFloat(int columnIndex) {
		return (Float) getColumnValue(columnIndex);
	}

	@Override
	public int getInt(int columnIndex) {
		return (Integer) getColumnValue(columnIndex);
	}

	@Override
	public long getLong(int columnIndex) {
		return (Long) getColumnValue(columnIndex);
	}

	@Override
	public int getPosition() {
		return currentItemPosition;
	}

	@Override
	public short getShort(int columnIndex) {
		return (Short) getColumnValue(columnIndex);
	}

	@Override
	public String getString(int columnIndex) {
		return String.valueOf(getColumnValue(columnIndex));
	}

	@Override
	public int getType(int columnIndex) {
		Object obj = getColumnValue(columnIndex);

		if (obj == null) {
			return FIELD_TYPE_NULL;
		}

		if (obj instanceof Float) {
			return FIELD_TYPE_FLOAT;
		} else if (obj instanceof Integer) {
			return FIELD_TYPE_INTEGER;
		}

		// we will use String as default
		return FIELD_TYPE_STRING;
	}

	@Override
	public boolean getWantsAllOnMoveCalls() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAfterLast() {
		return currentItemPosition >= this.getCount();
	}

	@Override
	public boolean isBeforeFirst() {
		return currentItemPosition < 0;
	}

	@Override
	public boolean isClosed() {
		return false;
	}

	@Override
	public boolean isFirst() {
		return currentItemPosition == 0;
	}

	@Override
	public boolean isLast() {
		return currentItemPosition == this.getCount() - 1;
	}

	@Override
	public boolean isNull(int columnIndex) {
		return getColumnValue(columnIndex) == null;
	}

	@Override
	public boolean move(int offset) {
		currentItemPosition += offset;
		if (isBeforeFirst()) {
			currentItemPosition = -1;
			return false;
		}
		if (isAfterLast()) {
			currentItemPosition = this.getCount();
			return false;
		}

		return true;
	}

	@Override
	public boolean moveToFirst() {
		currentItemPosition = 0;
		return true;
	}

	@Override
	public boolean moveToLast() {
		currentItemPosition = this.getCount() - 1;
		return true;
	}

	@Override
	public boolean moveToNext() {
		if (isLast() || isAfterLast()) {
			return false;
		}

		currentItemPosition++;
		return true;
	}

	@Override
	public boolean moveToPosition(int position) {
		if (position >= 0 && position < getCount()) {
			this.currentItemPosition = position;
			return true;
		}

		return false;
	}

	@Override
	public boolean moveToPrevious() {
		if (currentItemPosition <= 0) {
			return false;
		}
		currentItemPosition--;
		return true;
	}

	@Override
	public void registerContentObserver(ContentObserver observer) {
		Log.i(LOG_TAG, "registerContentObserver(): " + observer);
		this.contentObservers.add(observer);
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		Log.i(LOG_TAG, "registerDataSetObserver(): " + observer);
		this.dataSetObservers.add(observer);
	}

	@Override
	public boolean requery() {
		Log.i(LOG_TAG, "requery()... " + dataItemAccessor);

		// we use the accessor to load the full item list again
		this.todos = this.dataItemAccessor.readAllTodos(null);
		this.currentItemPosition = -1;

		for (DataSetObserver observer : this.dataSetObservers) {
			observer.onChanged();
		}
		return true;
	}

	@Override
	public Bundle respond(Bundle extras) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNotificationUri(ContentResolver cr, Uri uri) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregisterContentObserver(ContentObserver observer) {
		Log.i(LOG_TAG, "unregisterContentObserver(): " + observer);
		this.contentObservers.remove(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		Log.i(LOG_TAG, "unregisterDataSetObserver(): " + observer);
		this.dataSetObservers.remove(observer);
	}

	private Todo getCurrentItem() {
		return this.todos.get(currentItemPosition);
	}

	// get the value of some field of the current item
	private Object getColumnValue(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return getCurrentItem().getId();
		case 1:
			return getCurrentItem().getName();
		case 2:
			return getCurrentItem().getDescription();
		case 3:
			return getCurrentItem().isDone();
		case 4:
			return getCurrentItem().getPriority();
		case 5:
			return getCurrentItem().getDate();
		default:
			Log.e(LOG_TAG, "got unknown column index: " + columnIndex);
			return null;
		}

	}
}
