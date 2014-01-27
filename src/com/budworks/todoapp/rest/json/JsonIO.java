package com.budworks.todoapp.rest.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import android.util.Log;

import com.budworks.todoapp.model.Todo;
import com.budworks.todoapp.model.User;

public class JsonIO {

	private static final DateFormat DATE_FORMATTER = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");

	/**
	 * ObjectMapper is able to read json objects from input streams and write
	 * json objects to output streams
	 */
	private static final ObjectMapper MAPPER = new ObjectMapper();

	/**
	 * JsonFactory is able to create json nodes and to provide json generators
	 * from output streams
	 */
	private static final JsonFactory JSONFACTORY = new JsonFactory(MAPPER);

	private static final String LOG_TAG = JsonIO.class.getName();

	public static JsonNode readJsonNodeFromInputStream(InputStream is)
			throws JsonParseException, JsonMappingException, IOException {

		// read a json node from the input stream
		return MAPPER.readValue(is, JsonNode.class);
	}

	public static void writeJsonNodeToOutputStream(JsonNode node, OutputStream os)
			throws IOException {

		// obtain a json generator for the output stream
		JsonGenerator generator =
				JSONFACTORY.createJsonGenerator(os, JsonEncoding.UTF8);

		// write the object to the stream, using the generator
		generator.writeObject(node);
	}

	/**
	 * this is for merely converting a json array node to a list of data items
	 * 
	 * @param arrayNode
	 * @return
	 */
	public static List<Todo> createTodoListFromArrayNode(ArrayNode arrayNode) {

		List<Todo> itemlist = new ArrayList<Todo>();

		for (int i = 0; i < arrayNode.size(); i++) {
			itemlist.add(createTodoFromObjectNode((ObjectNode) arrayNode.get(i)));
		}

		return itemlist;
	}

	/**
	 * this takes a json object nodes and created a DataItem using its attribute
	 * values
	 * 
	 * @param objectNode
	 * @return
	 */
	public static Todo createTodoFromObjectNode(ObjectNode objectNode) {
		Date date = null;
		try {
			date =
					DATE_FORMATTER.parse(objectNode.get(Todo.KEY_DATE)
							.getTextValue());
		} catch (ParseException e) {
			Log.e(LOG_TAG, "Could not parse date field ...", e);
		}
		return new Todo(
				objectNode.get(Todo.KEY_ID).getLongValue(),
				objectNode.get(Todo.KEY_USER_ID).getTextValue(),
				objectNode.get(Todo.KEY_NAME).getTextValue(),
				objectNode.get(Todo.KEY_DESC).getTextValue(),
				objectNode.get(Todo.KEY_DONE).getBooleanValue(),
				Todo.Priority.valueOf(objectNode.get(Todo.KEY_PRIO).getTextValue()),
				date);
	}

	/**
	 * this, reversely, takes a DataItem and creates a json object from it
	 * 
	 * @param item
	 * @return
	 */
	public static ObjectNode createObjectNodeFromTodo(Todo todo) {

		// JsonNodeFactory offers creation methods for each type of json node
		ObjectNode objectNode = JsonNodeFactory.instance.objectNode();

		objectNode.put(Todo.KEY_ID, todo.getId());
		objectNode.put(Todo.KEY_USER_ID, todo.getUserId());
		objectNode.put(Todo.KEY_NAME, todo.getName());
		objectNode.put(Todo.KEY_DESC, todo.getDescription());
		objectNode.put(Todo.KEY_DONE, todo.isDone());
		objectNode.put(Todo.KEY_PRIO, todo.getPriority().name());
		objectNode.put(Todo.KEY_DATE, DATE_FORMATTER.format(todo.getDate()));
		return objectNode;

	}

	public static ObjectNode createObjectNodeFromUser(User user) {
		// JsonNodeFactory offers creation methods for each type of json node
		ObjectNode objectNode = JsonNodeFactory.instance.objectNode();

		objectNode.put(User.KEY_EMAIL, user.getEmail());
		objectNode.put(User.KEY_PASSWORD, user.getPassword());
		return objectNode;
	}

	public static User createUserFromObjectNode(ObjectNode json) {
		return new User(json.get(User.KEY_EMAIL).getTextValue(), json.get(
				User.KEY_PASSWORD).getTextValue());
	}

}
