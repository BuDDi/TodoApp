package com.budworks.todoapp;

import java.util.Calendar;
import java.util.Date;

import android.widget.DatePicker;
import android.widget.TimePicker;

public class UtilsUI {

	/**
	 * 
	 * @param datePicker
	 * @return a java.util.Date
	 */
	public static Date getDateFromDatePicker(DatePicker datePicker,
			TimePicker timePicker) {
		int minute = timePicker.getCurrentMinute();
		int hour = timePicker.getCurrentHour();
		int day = datePicker.getDayOfMonth();
		int month = datePicker.getMonth();
		int year = datePicker.getYear();

		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, day, hour, minute, 0);
		return calendar.getTime();
	}
}
