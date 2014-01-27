package com.budworks.todoapp.crud;

public interface ITodoCRUDOperationsWithURL extends ITodoCRUDOperations {

	public void setBaseUrl(String baseUrl);

	boolean hasConnection();

}
