package com.budworks.todoapp;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import com.budworks.todoapp.crud.HttpURLConnectionTodoCRUDOperationsImpl;
import com.budworks.todoapp.crud.HttpURLConnectionUserCRUDOperationsImpl;
import com.budworks.todoapp.crud.ITodoCRUDOperations;
import com.budworks.todoapp.crud.ITodoCRUDOperationsWithContext;
import com.budworks.todoapp.crud.ITodoCRUDOperationsWithURL;
import com.budworks.todoapp.crud.IUserOperationsWithURL;
import com.budworks.todoapp.crud.SQLiteTodoCRUDOperationsImpl;
import com.budworks.todoapp.event.ChangeListener;
import com.budworks.todoapp.model.Todo;
import com.budworks.todoapp.model.User;

public class TodoApplication extends Application {

	public static interface Constants {
		public static final int ON_CHANGE_INITIALIZED = 1;

		public static final int ON_CHANGE_USER = 2;
	}

	private static final String LOG_TAG = TodoApplication.class.getName();

	private static final String BASE_URL =
			"http://10.0.2.2:8080/TodolistWebapp/";

	private User remoteUser;

	private ITodoCRUDOperationsWithContext localTodoService;

	private ITodoCRUDOperationsWithURL remoteTodoService;

	private IUserOperationsWithURL remoteUserService;

	// status variables that gets initialized just ones
	// other activities can use that informations so they do not have to ask the
	// remote service
	private boolean remoteServiceAvailable;

	private boolean initialized;

	private List<ChangeListener<TodoApplication>> changeListeners =
			new ArrayList<ChangeListener<TodoApplication>>();

	@Override
	public void onCreate() {
		super.onCreate();
		// initialize crud services
		init();
	}

	private void init() {
		// set the local crud service and the remotes for user and todo operation
		// just one remote service gets checked so we check only connectivity to
		// the todo service and set the appropriate flag
		Log.i(LOG_TAG, "Initializing TodoApplication ...");
		// set the local todo crud service to sqlite database implementation
		this.localTodoService = new SQLiteTodoCRUDOperationsImpl();
		this.localTodoService.setContext(this);
		// set the remote todo crud service to http url implementation
		this.remoteTodoService = new HttpURLConnectionTodoCRUDOperationsImpl();
		this.remoteTodoService.setBaseUrl(BASE_URL);
		// set the remote user crud service to http url implemetation
		this.remoteUserService = new HttpURLConnectionUserCRUDOperationsImpl();
		this.remoteUserService.setBaseUrl(BASE_URL);
		new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				// network communication in a separate thread
				return remoteTodoService.hasConnection();
			}

			@Override
			protected void onPostExecute(Boolean result) {
				remoteServiceAvailable = result;
				if (!remoteServiceAvailable) {
					Log.i(LOG_TAG, "remote service is not available!");
				} else {
					// try's to sync on startup; the remoteUser may not have been set
					// yet
					initialized = true;
					syncDatabases(Constants.ON_CHANGE_INITIALIZED);
				}
			}

		}.execute();
	}

	private void fireChange(int what) {
		for (ChangeListener<TodoApplication> listener : changeListeners) {
			listener.onChange(what, this);
		}
	}

	public boolean addChangeListener(ChangeListener<TodoApplication> listener) {
		return changeListeners.add(listener);
	}

	public boolean removeChangeListener(
			ChangeListener<TodoApplication> todoOverviewActivity) {
		return changeListeners.remove(todoOverviewActivity);
	}

	public ITodoCRUDOperations getLocalTodoService() {
		return localTodoService;
	}

	public ITodoCRUDOperations getRemoteTodoService() {
		return remoteTodoService;
	}

	/**
	 * 
	 * @return <code>true</code> if and only if the application is initialized
	 *         AND the remote service is available; <code>false</code> otherwise.
	 */
	public boolean isRemoteServiceAvailable() {
		return initialized && remoteServiceAvailable;
	}

	/**
	 * 
	 * @return <code>true</code> if the CRUD services have been initialized and
	 *         connectivity status has been set; <code>false</code> otherwise.
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * if we stop, we signal this to the accessor (which is necessary in order to
	 * avoid trouble when operating on dbs)
	 */
	@Override
	public void onTerminate() {
		Log.i(LOG_TAG, "onDestroy(): will signal finalisation to the delegate");
		localTodoService.finalise();
		super.onTerminate();
	}

	public IUserOperationsWithURL getRemoteUserService() {
		return remoteUserService;
	}

	/**
	 * When a new remote user has been logged in this method can be called. If
	 * the user differs from the one before the local and the remote database of
	 * that user asynchronously synchronizes. That means if we have local todos
	 * we delete the remote todos of that user. If we have no todos local we take
	 * the remote todos and save them to the local database.
	 * 
	 * @param user
	 */
	public void setRemoteUser(User user) {
		if (user != null && !user.equals(remoteUser)) {
			Log.i(LOG_TAG, "Remote user changed " + user);
			this.remoteUser = user;
			// sync the databases
			syncDatabases(Constants.ON_CHANGE_USER);
		}
	}

	/**
	 * 
	 * @return the currently logged in user; if not logged in <code>null</code>
	 */
	public User getRemoteUser() {
		return this.remoteUser;
	}

	/**
	 * 
	 * @param fireEvent
	 *           fire the onChange-event for the given argument when done
	 */
	private void syncDatabases(final int fireEvent) {
		if (remoteServiceAvailable) {
			// Remote service is available so do the synchronization.
			// Only if we have local todos here we delete all todos remote and save
			// the local ones. Otherwise we load the remote todos and save them to
			// the local database.
			new AsyncTask<Void, Void, List<Todo>>() {

				@Override
				protected List<Todo> doInBackground(Void... params) {
					// for the local database we don't need to support a userId
					return localTodoService.readAllTodos(null);
				}

				@Override
				protected void onPostExecute(List<Todo> todos) {
					if (todos == null || todos.isEmpty()) {
						// we know that we have a remote connection here so load the
						// remote todos async because the local todo list is empty
						new AsyncTask<Void, Void, List<Todo>>() {

							@Override
							protected List<Todo> doInBackground(Void... arg0) {
								Log.i(LOG_TAG, "No local todos ... loading remote!");
								// we need to be logged in to get the Todos from the
								// remote service
								if (remoteUser != null) {
									return remoteTodoService.readAllTodos(remoteUser
											.getId());
								}
								return null;
							}

							@Override
							protected void onPostExecute(List<Todo> result) {
								if (result == null) {
									Log.w(LOG_TAG,
											"Could not load remote todos because not logged in!");
									return;
								}
								Log.i(LOG_TAG, "Got todos from remote: " + result);
								if (result != null) {
									// now update the local database with the remote
									// todos
									for (Todo todo : result) {
										Todo localTodo =
												localTodoService.createTodo(todo);
										if (localTodo != null) {
											Log.d(LOG_TAG,
													"Successfully created remote todo on local database "
															+ localTodo);
										} else {
											Log.d(LOG_TAG,
													"Remote todo could not be saved on local database "
															+ todo);
										}
									}
								}
								// the services are now initialized so set flag and fire
								// change
								fireChange(fireEvent);
							}
						}.execute();

					} else {
						// we have a remote connection and local todos so delete all
						// remote todos and save all local todos after deletion was
						// successful
						// first of all add the local todos to the list adapter and do
						// the remote synchronization in background
						Log.i(LOG_TAG, "Got local Todos: " + todos);
						// async task for network communication
						new AsyncTask<Todo, Void, Boolean>() {

							@Override
							protected Boolean doInBackground(Todo... todos) {
								Log.i(LOG_TAG,
										"Got remote connection, deleting todos ...");
								// check deletion was succuessful
								if (remoteUser != null) {
									remoteTodoService.deleteAllTodos(remoteUser.getId());
									Log.i(LOG_TAG, "Saving local todos remote ...");
									boolean result = true;
									for (Todo todo : todos) {
										// set the userId for the remote storage
										todo.setUserId(remoteUser.getId());
										result &=
												(remoteTodoService.createTodo(todo) != null);
									}
									if (result) {
										Log.i(LOG_TAG,
												"All todos got saved remote. GREAT!!!");
									} else {
										Log.w(LOG_TAG,
												"At least one tode that needed to be saved remote was null!");
									}
									return result;
								} else {
									Log.w(LOG_TAG,
											"Could not sync local todos with the remote service because not logged in!");
								}
								return false;
							}

							@Override
							protected void onPostExecute(Boolean result) {
								// the services are now initialized so set flag and fire
								// change
								fireChange(fireEvent);
							}

						}.execute(todos.toArray(new Todo[] {}));
					}
				}

			}.execute();
		}
	}
}
