package com.budworks.todoapp.crud;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.BooleanNode;
import org.codehaus.jackson.node.ObjectNode;

import android.util.Log;

import com.budworks.todoapp.model.Todo;
import com.budworks.todoapp.rest.json.JsonIO;

public class HttpURLConnectionTodoCRUDOperationsImpl implements
		ITodoCRUDOperationsWithURL {

	private static String LOG_TAG =
			HttpURLConnectionTodoCRUDOperationsImpl.class.getSimpleName();

	private String baseUrl;

	@Override
	public List<Todo> readAllTodos(String userId) {

		Log.i(LOG_TAG, "readAllTodos()");

		try {
			// obtain a http url connection from the base url
			HttpURLConnection con =
					(HttpURLConnection) (new URL(baseUrl + "/" + userId))
							.openConnection();
			Log.d(LOG_TAG, "readAllItems(): got connection: " + con);
			// set the request method (GET is default anyway...)
			con.setRequestMethod("GET");
			// check the response code
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				// then initiate sending the request...
				InputStream is = con.getInputStream();
				// and create a json node from the input stream
				JsonNode json = JsonIO.readJsonNodeFromInputStream(is);
				// then transform the node into a list of DataItem objects
				List<Todo> todos =
						JsonIO.createTodoListFromArrayNode((ArrayNode) json);
				Log.i(LOG_TAG, "readAllTodos(): " + todos);

				return todos;
			} else {
				Log.e(LOG_TAG,
						"readAllItems(): got response code: " + con.getResponseCode());
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "readAllItems(): got exception: " + e);
		}

		return new ArrayList<Todo>();

	}

	@Override
	public boolean hasConnection() {
		try {
			// obtain a http url connection from the base url
			Log.i(LOG_TAG, "checking connnection for url " + baseUrl);
			HttpURLConnection con =
					(HttpURLConnection) (new URL(baseUrl).openConnection());
			con.connect();
			Log.i(LOG_TAG, "Got connection for url " + baseUrl);
			return true;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public Todo createTodo(Todo todo) {
		HttpURLConnection con = null;
		try {
			// obtain a http url connection from the base url
			con = (HttpURLConnection) (new URL(baseUrl)).openConnection();
			Log.d(LOG_TAG, "createTodo(): got connection: " + con);
			// set the request method
			con.setRequestMethod("POST");
			// indicate that we want to send a request body
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
			con.setRequestProperty("Accept", "*/*");
			con.setDoOutput(true);
			con.setInstanceFollowRedirects(false);
			// obtain the output stream and write the item as json object
			// to it
			OutputStream os = con.getOutputStream();
			os.write(createJsonStringFromTodo(todo).getBytes("UTF-8"));
			// os.flush();

			// check the response code
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				Log.i(LOG_TAG,
						"createTodo(): got response: " + con.getResponseMessage());
				// then initiate sending the request...
				InputStream is = con.getInputStream();
				// and create a json node from the input stream
				JsonNode json = JsonIO.readJsonNodeFromInputStream(is);
				Log.d(LOG_TAG, "createItem(): got json: " + json);
				// then transform the node into a DataItem object
				todo = JsonIO.createTodoFromObjectNode((ObjectNode) json);
				Log.i(LOG_TAG, "createTodo(): " + todo);
				return todo;
			} else {
				Log.e(LOG_TAG,
						"createItem(): got response code: " + con.getResponseCode());
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "createItem(): got exception: " + e);
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
		return null;
	}

	@Override
	public boolean deleteTodo(String userId, long todoId) {
		Log.i(LOG_TAG, "deleteTodo(): " + todoId);

		try {
			// obtain a http url connection from the base url
			HttpURLConnection con =
					(HttpURLConnection) (new URL(baseUrl + "/" + userId + "/"
							+ todoId)).openConnection();
			Log.d(LOG_TAG, "deleteTodo(): got connection: " + con);
			// set the request method
			con.setRequestMethod("DELETE");
			// then initiate sending the request...
			InputStream is = con.getInputStream();
			// check the response code
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				// and create a json node from the input stream
				JsonNode json = JsonIO.readJsonNodeFromInputStream(is);
				Log.d(LOG_TAG, "deleteTodo(): got json: " + json + " of class: "
						+ json.getClass());
				// then transform the node into a DataItem object

				return ((BooleanNode) json).getBooleanValue();
			} else {
				Log.e(LOG_TAG,
						"deleteTodo(): got response code: " + con.getResponseCode());
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "deleteTodo(): got exception: " + e);
		}

		return false;
	}

	/**
	 * the only difference from create is the PUT method, i.e. the common
	 * functionality could be factored out...
	 */
	@Override
	public Todo updateTodo(Todo todo) {
		Log.i(LOG_TAG, "updateTodo(): " + todo);

		try {
			// obtain a http url connection from the base url
			HttpURLConnection con =
					(HttpURLConnection) (new URL(baseUrl)).openConnection();
			Log.d(LOG_TAG, "updateTodo(): got connection: " + con);
			// set the request method
			con.setRequestMethod("PUT");
			// indicate that we want to send a request body
			con.setRequestProperty("Content-Type", "application/json");
			con.setDoOutput(true);
			// obtain the output stream and write the item as json object to it
			OutputStream os = con.getOutputStream();
			os.write(createJsonStringFromTodo(todo).getBytes());
			// then initiate sending the request...
			InputStream is = con.getInputStream();
			// check the response code
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				// and create a json node from the input stream
				JsonNode json = JsonIO.readJsonNodeFromInputStream(is);
				Log.d(LOG_TAG, "updateTodo(): got json: " + json);
				// then transform the node into a DataItem object
				todo = JsonIO.createTodoFromObjectNode((ObjectNode) json);
				Log.i(LOG_TAG, "updateTodo(): " + todo);

				return todo;
			} else {
				Log.e(LOG_TAG,
						"updateTodo(): got response code: " + con.getResponseCode());
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "updateTodo(): got exception: " + e);
		}

		return null;
	}

	/**
	 * create a string from the data item's json representation
	 * 
	 * @param item
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	protected String createJsonStringFromTodo(Todo todo)
			throws UnsupportedEncodingException, IOException {

		// create a json node from the
		ObjectNode jsonNode = JsonIO.createObjectNodeFromTodo(todo);
		Log.i(LOG_TAG, "created jsonNode: " + jsonNode + " from item: " + todo);
		// serialise the object
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JsonIO.writeJsonNodeToOutputStream(jsonNode, os);

		// create a string entity from the output stream, using utf-8 character
		// encoding
		return os.toString();
	}

	@Override
	public Todo readTodo(String userId, long dateItemId) {
		throw new UnsupportedOperationException(
				"readDataItem() currently not supported by " + this.getClass());
	}

	@Override
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl + "todo";
	}

	@Override
	public boolean deleteAllTodos(String userId) {
		Log.i(LOG_TAG, "deleteAllTodos() ...");
		// obtain a http url connection from the base url
		HttpURLConnection con;
		try {
			con =
					(HttpURLConnection) new URL(baseUrl + "/" + userId)
							.openConnection();
			Log.d(LOG_TAG, "deleteAllTodos(): got connection: " + con);
			// set the request method
			con.setRequestMethod("DELETE");
			// check the response code
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				// then initiate sending the request...
				InputStream is = con.getInputStream();
				// and create a json node from the input stream
				JsonNode json = JsonIO.readJsonNodeFromInputStream(is);
				Log.d(LOG_TAG, "deleteAllTodos(): got json: " + json
						+ " of class: " + json.getClass());
				// then transform the node into a DataItem object
				return ((BooleanNode) json).getBooleanValue();
			} else {
				Log.e(LOG_TAG,
						"deleteAllTodos(): got response code: "
								+ con.getResponseCode());
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "deleteAllTodos(): got exception: " + e);
		}
		return false;
	}
}
