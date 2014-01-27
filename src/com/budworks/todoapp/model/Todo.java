package com.budworks.todoapp.model;

import java.io.Serializable;
import java.util.Date;

public class Todo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2797198918829418651L;

	public static enum Priority {
		Low, Normal, High;

		public static Priority getForOrdinal(byte ordinal) {
			return getForOrdinal((long) ordinal);
		}

		public static Priority getForOrdinal(short ordinal) {
			return getForOrdinal((long) ordinal);
		}

		public static Priority getForOrdinal(int ordinal) {
			return getForOrdinal((long) ordinal);
		}

		public static Priority getForOrdinal(long ordinal) {
			Priority[] values = values();
			for (Priority prio : values) {
				if (prio.ordinal() == ordinal) {
					return prio;
				}
			}
			throw new IllegalArgumentException("Priority for ordinal number "
					+ ordinal + " could not be found!");
		}

	};

	// Contacts table name
	public static final String TABLE_NAME = "todo";

	// Contacts Table Columns names
	public static final String KEY_ID = "id";

	public static final String KEY_USER_ID = "userId";

	public static final String KEY_NAME = "name";

	public static final String KEY_DESC = "description";

	public static final String KEY_DONE = "done";

	public static final String KEY_PRIO = "priority";

	public static final String KEY_DATE = "date";

	private long id;

	private String userId;

	private String name;

	private String description;

	private boolean done;

	private Priority priority;

	private Date date;

	public Todo() {
		super();
	}

	public Todo(long id, String userId, String name, String description,
			boolean done, Priority priority, Date date) {
		super();
		this.id = id;
		this.userId = userId;
		this.name = name;
		this.description = description;
		this.done = done;
		this.priority = priority;
		this.date = date;
	}

	public Todo(String userId, String name, String description, boolean done,
			Priority priority, Date date) {
		super();
		this.userId = userId;
		this.name = name;
		this.description = description;
		this.done = done;
		this.priority = priority;
		this.date = date;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Todo)) {
			return false;
		}
		Todo otherTodo = (Todo) o;
		return this.id == otherTodo.id;
	}

	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return name;
	}

	public Todo updateFrom(Todo item) {
		setName(item.name);
		setDescription(item.description);
		setDone(item.done);
		setDate(item.date);
		setPriority(item.priority);
		return this;
	}

}
