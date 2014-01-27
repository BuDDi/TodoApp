package com.budworks.todoapp.crud;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.budworks.todoapp.model.User;

@Path("/user")
@Consumes({ "application/json" })
@Produces({ "application/json" })
public interface IUserOperations {

	@PUT
	@Path("/auth")
	public boolean authenticateUser(User user);

}
