package com.budworks.todoapp.crud;

import android.content.Context;

public interface ITodoCRUDOperationsWithContext extends ITodoCRUDOperations {

	/*
	 * pass a context
	 */
	public void setContext(Context context);

	/*
	 * signal finalisation
	 */
	public void finalise();
}
