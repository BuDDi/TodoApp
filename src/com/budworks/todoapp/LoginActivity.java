package com.budworks.todoapp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.budworks.todoapp.model.User;

public class LoginActivity extends Activity implements OnKeyListener {

	private static final Pattern EMAIL_PATTERN = Pattern
			.compile("([-0-9a-zA-Z.+_]+)@([-0-9a-zA-Z.+_]+\\.[a-zA-Z]{2,4})");

	private static final Pattern PASSWORD_PATTERN = Pattern.compile("\\d{6}");

	private static final String LOG_TAG = LoginActivity.class.getName();

	private String warningMsg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		// add KeyListeners to text fields
		EditText emailField = (EditText) this.findViewById(R.id.txt_email);
		emailField.setOnKeyListener(this);
		EditText passwordField = (EditText) findViewById(R.id.txt_pwd);
		passwordField.setOnKeyListener(this);
		Button loginBtn = (Button) findViewById(R.id.btn_login);
		loginBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				doAuthentication();
			}
		});
		Button cancelBtn = (Button) findViewById(R.id.btn_cancel);
		cancelBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startOverviewActivity();
			}
		});
		updateWarning();
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// This is the filter
		if (event.getAction() == KeyEvent.ACTION_UP) {
			updateWarning();
			if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
				doAuthentication();
			}
		}
		return false;
	}

	private void updateWarning() {

		String emailWarning = getEmailWarning();
		String passwordWarning = getPasswordWarning();
		warningMsg =
				(emailWarning != null && emailWarning.length() > 0) ? emailWarning
						: passwordWarning;
		Button loginBtn = (Button) findViewById(R.id.btn_login);
		loginBtn.setEnabled(warningMsg == null);
		TextView warningLabel = (TextView) findViewById(R.id.warning_label);
		warningLabel.setText(warningMsg);
		ImageView warningIcon = (ImageView) findViewById(R.id.warning_icon);
		int warningIconVisibility =
				warningMsg != null ? ImageView.VISIBLE : ImageView.INVISIBLE;
		warningIcon.setVisibility(warningIconVisibility);
	}

	private String getPasswordWarning() {
		EditText passwordField = (EditText) findViewById(R.id.txt_pwd);
		String password = passwordField.getText().toString();
		Matcher pwdMatcher = PASSWORD_PATTERN.matcher(password);
		if (password == null || password.length() == 0) {
			return "No password entered";
		} else if (!pwdMatcher.matches()) {
			return "No valid password:\nOnly 6 digits allowed!";
		}
		return null;
	}

	private String getEmailWarning() {
		EditText emailField = (EditText) findViewById(R.id.txt_email);
		String email = emailField.getText().toString();
		Matcher emailMatcher = EMAIL_PATTERN.matcher(email);
		if (email == null || email.length() == 0) {
			return "No email address entered";
		} else if (!emailMatcher.matches()) {
			return "No valid email address entered";
		}
		return null;
	}

	private void doAuthentication() {
		// authentication only possible if we have no warning
		if (warningMsg == null) {
			Log.i(LOG_TAG, "doAuthentication() ...");
			EditText emailField = (EditText) findViewById(R.id.txt_email);
			EditText passwordField = (EditText) findViewById(R.id.txt_pwd);
			String email = emailField.getText().toString();
			String password = passwordField.getText().toString();
			final TodoApplication app = (TodoApplication) getApplication();
			final User user = new User(email, password);
			if (!app.isRemoteServiceAvailable()) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Error").setMessage("No remote connection!")
						.setCancelable(true)
						.setPositiveButton("OK", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}
						});
				AlertDialog dialog = builder.create();
				dialog.show();
				return;
			} else {
				final ProgressDialog progressDialog =
						ProgressDialog.show(this, "", "Loading ...");
				new AsyncTask<User, Void, Boolean>() {

					@Override
					protected Boolean doInBackground(User... params) {
						return app.getRemoteUserService().authenticateUser(params[0]);
					}

					@Override
					protected void onPostExecute(Boolean result) {
						progressDialog.dismiss();
						if (result) {
							String msg = "Login successful " + user;
							Log.i(LOG_TAG, msg);
							Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG)
									.show();
							// set the user for the application so that other
							// activities can get
							// access to the logged in user
							app.setRemoteUser(user);
							startOverviewActivity();
						} else {
							AlertDialog.Builder builder =
									new AlertDialog.Builder(LoginActivity.this);
							builder.setTitle("Error")
									.setMessage("No valid user information!")
									.setCancelable(true)
									.setPositiveButton("OK", new OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog,
												int which) {
											dialog.cancel();
										}
									});
							AlertDialog dialog = builder.create();
							dialog.show();
						}
					}
				}.execute(user);
			}
		}
	}

	private void startOverviewActivity() {
		Intent overviewIntent = new Intent(this, TodoOverviewActivity.class);
		startActivity(overviewIntent);
	}
}
