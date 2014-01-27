package com.budworks.todoapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.RenderScript.Priority;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.budworks.todoapp.crud.ITodoCRUDOperations;
import com.budworks.todoapp.event.ChangeListener;
import com.budworks.todoapp.model.Todo;
import com.budworks.todoapp.model.User;

public class TodoOverviewActivity extends ListActivity implements
		ChangeListener<TodoApplication> {

	public static interface Constants {
		public static final int SORTBY_NONE = 0;

		public static final int SORTBY_DONE = 1;

		public static final int SORTBY_DATE = 2;

		public static final int SORTBY_PRIO = 4;

	}

	public static final String ARG_DELEGATE_CLASS = "businessDelegateClass";

	private static final String LOG_TAG = TodoOverviewActivity.class.getName();

	private static boolean warningShown = false;

	private int REL_SWIPE_MIN_DISTANCE;

	private int REL_SWIPE_MAX_OFF_PATH;

	private int REL_SWIPE_THRESHOLD_VELOCITY;

	private ITodoCRUDOperations todoSqliteDb;

	private List<Integer> sorting = new ArrayList<Integer>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_overview);
		// get the TodoApplication to have access to the crud services and
		// initialization and connectivity status
		TodoApplication app = (TodoApplication) getApplication();
		// If the application is not yet initialized add this activity as a change
		// listener.
		if (!app.isInitialized() || app.getRemoteUser() == null) {
			Log.i(LOG_TAG,
					"Adding OverviewActivity as Changelistener cause TodoApplication is not initialized!");
			if (app.addChangeListener(this)) {
				Log.i(LOG_TAG,
						"Successfully added OverviewActivity as ChangeListener");
			} else {
				Log.w(LOG_TAG, "Could not add OverviewActivity as ChangeListener");
			}
		} else {
			// If the application is initialized check if the remote service is
			// available. If we have not shown the warning display it
			Log.i(LOG_TAG,
					"TodoApplication is initialized, checking remote service availability ...");
			if (!app.isRemoteServiceAvailable()) {
				if (!warningShown) {
					showRemoteWarningDialog();
				}
			} else {
				// we know that we have remote connection and if the user has not
				// been set ask the user to login
				if (app.getRemoteUser() == null) {
					showLoginDecisionDialog();
					Log.i(LOG_TAG, "Remote service in TodoApplication is available!");
				}
			}
		}
		// always sort by done status of a todo
		sorting.add(Constants.SORTBY_DONE);
		// after the connectivity check, because this is the launcher activity, we
		// can load the local todos
		List<Todo> todos = new ArrayList<Todo>();

		// use the TodoListAdapter to show the elements in a ListView
		setListAdapter(new TodoListAdapter(this, todos));
		// get the local crud service from Application
		todoSqliteDb = app.getLocalTodoService();
		// first load all items
		doLoadItems();
		// then check if the new or edit activity set the new or edited todo in
		// extras so we connect to the crud services if available and save or
		// update the todo
		Todo newTodo =
				(Todo) getIntent().getSerializableExtra(
						NewTodoActivity.KEY_NEW_TODO);
		Todo editTodo =
				(Todo) getIntent().getSerializableExtra(
						EditTodoActivity.KEY_EDIT_TODO);
		Todo deleteTodo =
				(Todo) getIntent().getSerializableExtra(
						EditTodoActivity.KEY_DELETE_TODO);
		// only one can be set
		if (newTodo != null && editTodo != null) {
			String msg = "Something went wrong: got new AND edited todo!";
			Log.e(LOG_TAG, msg);
			throw new RuntimeException(msg);
		}
		if (newTodo != null) {
			Log.i(LOG_TAG, "Got new Todo ... adding it!");
			doCreateItem(newTodo);
		} else if (editTodo != null) {
			Log.i(LOG_TAG, "Got edited Todo ... setting new values!");
			doUpdateItem(editTodo);
		} else if (deleteTodo != null) {
			Log.i(LOG_TAG, "Got Todo to delete ... !");
			doDeleteItem(deleteTodo);
		}

		// initialize the swipe parameters used for deleting items
		DisplayMetrics dm = getResources().getDisplayMetrics();
		REL_SWIPE_MIN_DISTANCE = (int) (120.0f * dm.densityDpi / 160.0f + 0.5);
		REL_SWIPE_MAX_OFF_PATH = (int) (250.0f * dm.densityDpi / 160.0f + 0.5);
		REL_SWIPE_THRESHOLD_VELOCITY =
				(int) (200.0f * dm.densityDpi / 160.0f + 0.5);
	}

	private void showLoginDecisionDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Question")
				.setMessage("Do you want to login and use remote synchronization?")
				.setPositiveButton("Yes", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						startLoginActivity();
					}

				}).setNegativeButton("No", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).setCancelable(true);
		builder.create().show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		ListView list = getListView();
		if (list == null) {
			new Throwable("Listview not set exception");
		}

		@SuppressWarnings("deprecation")
		final GestureDetector gestureDetector =
				new GestureDetector(getBaseContext(), new MyGestureDetector());

		View.OnTouchListener gestureListener = new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		};
		list.setOnTouchListener(gestureListener);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.overview, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_new:
			startNewTodoActivity();
			return true;
		case R.id.action_login:
			startLoginActivity();
			return true;
		case R.id.action_sort_date_first:
			sorting.clear();
			sorting.add(Constants.SORTBY_DONE);
			sorting.add(Constants.SORTBY_DATE);
			sorting.add(Constants.SORTBY_PRIO);
			doLoadItems();
			return true;
		case R.id.action_sort_prio_first:
			sorting.clear();
			sorting.add(Constants.SORTBY_DONE);
			sorting.add(Constants.SORTBY_PRIO);
			sorting.add(Constants.SORTBY_DATE);
			doLoadItems();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void startLoginActivity() {
		Log.v(LOG_TAG, "Clicked Login.");
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
	}

	private void startNewTodoActivity() {
		Log.v(LOG_TAG, "Clicked new Todo.");
		Intent intent = new Intent(this, NewTodoActivity.class);
		startActivity(intent);
	}

	private void startEditTodoActivity(Todo todo) {
		Intent intent = new Intent(this, EditTodoActivity.class);
		intent.putExtra("todo", todo);
		startActivity(intent);
	}

	private void onItemClick(int position) {
		// Object group = getListAdapter().getItem(position);
		// Toast.makeText(this,
		// "Single tap on item position " + position + " got group: " + group,
		// Toast.LENGTH_SHORT).show();
		if (position >= 0 && position < getListAdapter().getCount()) {
			startEditTodoActivity((Todo) getListAdapter().getItem(position));
		}
	}

	private void performSwipe(boolean isRight, int position) {
		Toast.makeText(this,
				"Swipe to " + (isRight ? "right" : "left") + " direction",
				Toast.LENGTH_SHORT).show();
		// left swipe causes delete
		if (!isRight) {
			final Todo todo = (Todo) getListAdapter().getItem(position);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder
					.setTitle("Delete")
					.setMessage(
							"Do you want to delete Todo \"" + todo.getName() + "\"")
					.setCancelable(true)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							doDeleteItem(todo);
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
	}

	private class MyGestureDetector extends SimpleOnGestureListener {

		private int temp_position = -1;

		// Detect a single-click and call my own handler.
		@Override
		public boolean onSingleTapUp(MotionEvent e) {

			int pos =
					getListView().pointToPosition((int) e.getX(), (int) e.getY());
			onItemClick(pos);
			return true;
		}

		@Override
		public boolean onDown(MotionEvent e) {

			temp_position =
					getListView().pointToPosition((int) e.getX(), (int) e.getY());
			return super.onDown(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			// TODO can cause NullPointerException; not yet clear why
			if (Math.abs(e1.getY() - e2.getY()) > REL_SWIPE_MAX_OFF_PATH) {
				return false;
			}
			if (e1.getX() - e2.getX() > REL_SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) {

				int pos =
						getListView().pointToPosition((int) e1.getX(),
								(int) e2.getY());

				if (pos >= 0 && temp_position == pos) {
					performSwipe(false, pos);
				}
			} else if (e2.getX() - e1.getX() > REL_SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) {

				int pos =
						getListView().pointToPosition((int) e1.getX(),
								(int) e2.getY());
				if (pos >= 0 && temp_position == pos) {
					performSwipe(true, pos);
				}

			}
			return false;
		}

	}

	/*
	 * we use AsyncTask for all data access operations (we use minimalistic code
	 * here)
	 */
	protected void doLoadItems() {
		Log.i(LOG_TAG, "Loading items from local database ...");
		// just take the stuff from the local database
		// we expect all local database accesses to be fast enough to not need to
		// display a progress dialog
		new AsyncTask<Void, Void, List<Todo>>() {

			@Override
			protected List<Todo> doInBackground(Void... params) {
				// for the local database we don't need to support a userId
				return todoSqliteDb.readAllTodos(null);
			}

			@Override
			protected void onPostExecute(List<Todo> todos) {
				Log.i(LOG_TAG, "Got local Todos: " + todos);
				TodoListAdapter adapter = (TodoListAdapter) getListAdapter();
				adapter.clear();
				adapter.addAll(todos);
				applySortingOnAdapter();
			}

		}.execute();
	}

	private void applySortingOnAdapter() {

		TodoListAdapter adapter = (TodoListAdapter) getListAdapter();
		List<Todo> todos = adapter.getAllTodos();
		// sort the todos depending on the sorting before adding it to the
		// adapter
		Collections.sort(todos, new Comparator<Todo>() {

			@Override
			public int compare(Todo lhs, Todo rhs) {
				int ordinal = 0;
				for (int i = 0; i < sorting.size(); i++) {
					// depending on the position of a sorting flag in the list
					// the flag has more or less influence on the sorting
					// first elements has highest influence
					int factor = sorting.size() - i;
					// multiply the factor by itself so that lower sorting flags in
					// the list cannot nullify the first sorting entry
					factor *= factor;
					int sortingFlag = sorting.get(i);
					if (sortingFlag == Constants.SORTBY_DONE) {
						ordinal +=
								(lhs.isDone() ? factor : 0)
										- (rhs.isDone() ? factor : 0);
					} else if (sortingFlag == Constants.SORTBY_DATE) {
						int value = lhs.getDate().compareTo(rhs.getDate());
						if (value != 0) {
							value /= Math.abs(value);
						}
						ordinal += value * factor;
					} else if (sortingFlag == Constants.SORTBY_PRIO) {
						// Priority is sorted with higher value = lower position in
						// list
						int nrPriorities = Priority.values().length;
						int value =
								(nrPriorities - lhs.getPriority().ordinal())
										- (nrPriorities - rhs.getPriority().ordinal());
						if (value != 0) {
							value /= Math.abs(value);
						}
						ordinal += value * factor;
					}
				}
				return ordinal;
			}
		});
		adapter.notifyDataSetChanged();
		// getListView().invalidate();
	}

	protected void doCreateItem(Todo todo) {

		new AsyncTask<Todo, Void, Todo>() {

			@Override
			protected Todo doInBackground(Todo... params) {
				// TODO id's can differ here because we get an id from remote and
				// one id from local. The logic for calculating the id can be
				// different.
				// do not add items until we have loaded all items
				TodoApplication app = (TodoApplication) getApplication();
				// first save it local
				Todo todo = todoSqliteDb.createTodo(params[0]);
				if (app.isRemoteServiceAvailable()) {
					// create the todo if the remote crud service is available and
					// the user is logged in
					User user = app.getRemoteUser();
					if (user != null) {
						todo = params[0];
						String userId = user.getId();
						Log.i(LOG_TAG, "Setting userId " + userId + " for todo "
								+ todo);
						todo.setUserId(userId);
						todo = app.getRemoteTodoService().createTodo(todo);
					} else {
						String msg =
								"Could not create todo remote because not logged in!";
						Log.w(LOG_TAG, msg);
					}
				}
				if (todo == null) {
					Log.w(LOG_TAG, "Could not save todo remote ...");
				}
				return todo;
			}

			@Override
			protected void onPostExecute(Todo item) {
				// add the new todo to the list adapter if not null
				if (item != null) {
					TodoListAdapter adapter = (TodoListAdapter) getListAdapter();
					adapter.add(item);
					applySortingOnAdapter();
				} else {
					String msg =
							"Could not save the new todo to the local database!";
					Log.e(LOG_TAG, msg);
					Toast.makeText(TodoOverviewActivity.this, msg, Toast.LENGTH_LONG)
							.show();
				}
			}

		}.execute(todo);
	}

	protected void doDeleteItem(final Todo todo) {
		new AsyncTask<Todo, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Todo... params) {
				// first delete the local todo and then remote
				TodoApplication app = (TodoApplication) getApplication();
				if (app.isRemoteServiceAvailable()) {
					User user = app.getRemoteUser();
					if (user != null) {
						if (!app.getRemoteTodoService().deleteTodo(user.getId(),
								todo.getId())) {
							Log.w(LOG_TAG, "Could not delete todo remote!");
						}
					} else {
						String msg =
								"Could not delete todo remote because not logged in!";
						Log.w(LOG_TAG, msg);
					}
				}
				return todoSqliteDb.deleteTodo(null, todo.getId());
			}

			@Override
			protected void onPostExecute(Boolean deleted) {
				if (deleted) {
					TodoListAdapter adapter = (TodoListAdapter) getListAdapter();
					adapter.remove(todo);
					applySortingOnAdapter();
				} else {
					Toast.makeText(
							TodoOverviewActivity.this,
							"The item " + todo.getName()
									+ " could not be deleted remote and/or local!",
							Toast.LENGTH_LONG).show();
				}
			}

		}.execute(todo);
	}

	protected void doUpdateItem(Todo item) {
		new AsyncTask<Todo, Void, Todo>() {

			@Override
			protected Todo doInBackground(Todo... params) {
				// first update the local item and then remote
				Todo remoteTodo = params[0];
				TodoApplication app = (TodoApplication) getApplication();
				if (app.isRemoteServiceAvailable()) {
					User user = app.getRemoteUser();
					if (user != null) {
						remoteTodo.setUserId(user.getId());
						remoteTodo =
								app.getRemoteTodoService().updateTodo(remoteTodo);
					} else {
						String msg =
								"Could not update todo remote because not logged in!";
						Log.w(LOG_TAG, msg);
					}
				}
				return todoSqliteDb.updateTodo(params[0]);
			}

			@Override
			protected void onPostExecute(Todo result) {

				TodoListAdapter adapter = (TodoListAdapter) getListAdapter();
				Todo updatedAdapterTodo = adapter.get(adapter.indexOf(result));
				updatedAdapterTodo.updateFrom(result);
				Log.d(LOG_TAG, "Displaying updated todo: " + updatedAdapterTodo);
				applySortingOnAdapter();
				Log.d(LOG_TAG, "Updated todo after sorting: " + updatedAdapterTodo);
			}

		}.execute(item);
	}

	@Override
	public void onChange(int what, TodoApplication source) {
		Log.i(LOG_TAG, "onChange(" + what + "): Got notified by TodoApplication!");
		if (what == TodoApplication.Constants.ON_CHANGE_INITIALIZED) {
			if (!source.isRemoteServiceAvailable()) {
				if (!warningShown) {
					showRemoteWarningDialog();
				}
			}
		} else if (what == TodoApplication.Constants.ON_CHANGE_USER) {
			// user logged in and we have a remote
			// connection so synchronize with remote service
			doLoadItems();
		}
	}

	private void showRemoteWarningDialog() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set title
		alertDialogBuilder.setTitle("Warning");

		// set dialog message
		alertDialogBuilder
				.setMessage(
						"Remote service is not available, using local database!")
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// if this button is clicked, close
						// current activity
						dialog.cancel();
						warningShown = true;
					}
				});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

}
