package com.budworks.todoapp.event;

public interface ChangeListener<T> {

	void onChange(int what, T source);
}
