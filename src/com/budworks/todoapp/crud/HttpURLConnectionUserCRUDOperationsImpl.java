package com.budworks.todoapp.crud;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.BooleanNode;
import org.codehaus.jackson.node.ObjectNode;

import android.util.Log;

import com.budworks.todoapp.model.User;
import com.budworks.todoapp.rest.json.JsonIO;

public class HttpURLConnectionUserCRUDOperationsImpl implements
		IUserOperationsWithURL {

	private static final String LOG_TAG = HttpURLConnection.class.getName();

	private String baseUrl;

	@Override
	public boolean hasConnection() {
		try {
			HttpURLConnection con = (HttpURLConnection) new URL(baseUrl)
					.openConnection();
			con.connect();
			return true;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean authenticateUser(User user) {
		Log.i(LOG_TAG, "authenticating user ...");
		try {
			HttpURLConnection con = (HttpURLConnection) new URL(baseUrl + "/auth")
					.openConnection();
			Log.d(LOG_TAG, "authenticateUser(): got connection: " + con);
			// set the request method
			con.setRequestMethod("PUT");
			// indicate that we want to send a request body
			con.setRequestProperty("Content-Type", "application/json");
			// con.setRequestProperty("Cache-Control", "no-cache");
			con.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
			con.setRequestProperty("Accept", "*/*");
			con.setDoOutput(true);
			con.setInstanceFollowRedirects(false);
			// obtain the output stream and write the item as json object
			// to it
			OutputStream os = con.getOutputStream();
			os.write(createJsonStringFromUser(user).getBytes("UTF-8"));
			// check the response code
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				// then initiate sending the request...
				InputStream is = con.getInputStream();
				// and create a json node from the input stream
				JsonNode json = JsonIO.readJsonNodeFromInputStream(is);
				Log.d(LOG_TAG, "authenticateUser(): got json: " + json);
				// then transform the node into a DataItem object
				return ((BooleanNode) json).getBooleanValue();
			} else {
				Log.e(LOG_TAG,
						"authenticateUser(): got response code: "
								+ con.getResponseCode());
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "authenticateUser(): got exception: " + e.getMessage());
		}
		return false;
	}

	private String createJsonStringFromUser(User user) throws IOException {
		// create a json node from the
		ObjectNode jsonNode = JsonIO.createObjectNodeFromUser(user);
		Log.i(LOG_TAG, "created jsonNode: " + jsonNode + " from item: " + user);
		// serialise the object
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JsonIO.writeJsonNodeToOutputStream(jsonNode, os);

		// create a string entity from the output stream, using utf-8 character
		// encoding
		return os.toString();
	}

	@Override
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl + "user";
		Log.i(LOG_TAG, "initialized baseUrl " + baseUrl);
	}

}
