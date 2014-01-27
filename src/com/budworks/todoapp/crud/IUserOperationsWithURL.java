package com.budworks.todoapp.crud;

public interface IUserOperationsWithURL extends IUserOperations {

	void setBaseUrl(String baseURL);

	boolean hasConnection();

}
