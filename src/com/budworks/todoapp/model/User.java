package com.budworks.todoapp.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class User implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7306724305413428761L;

	public static final String KEY_EMAIL = "email";

	public static final String KEY_PASSWORD = "password";

	private long id;

	private String password;

	private String email;

	public User() {

	}

	public User(String email, String password) {
		this.email = email;
		this.password = password;
	}

	/**
	 * @return the hexadecimal representation of MD5-hashed email
	 */
	public String getId() {
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(email.getBytes(), 0, email.length());
			return new BigInteger(1, m.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
