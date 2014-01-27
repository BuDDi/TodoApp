package com.budworks.todoapp.crud;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.budworks.todoapp.model.Todo;

@Path("/todo")
@Consumes({ "application/json" })
@Produces({ "application/json" })
public interface ITodoCRUDOperations {

	/*
	 * the operations
	 */
	@POST
	public Todo createTodo(Todo item);

	@GET
	@Path("/{userId}")
	public List<Todo> readAllTodos(@PathParam("userId") String userId);

	@GET
	@Path("/{userId}/{itemId}")
	public Todo readTodo(@PathParam("userId") String userId,
			@PathParam("itemId") long dateItemId);

	@PUT
	public Todo updateTodo(Todo item);

	@DELETE
	@Path("/{userId}/{itemId}")
	public boolean deleteTodo(@PathParam("userId") String userId,
			@PathParam("itemId") long dataItemId);

	@DELETE
	@Path("/{userId}")
	public boolean deleteAllTodos(@PathParam("userId") String userId);

}
