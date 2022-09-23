package util;

import content.adContent.content.enums.UserAtt;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

/**
 * Class to handel the conversions from AD time, time as millis, JDatePicker, Calender and Strings.
 */
public class TimeTransformations {
    public static final Long VERTRAGSENDE_NOT_SET_TIME = 9223372036854775807L;


    /**
     * From AD time to normal milliseconds
     * @param filetime The AD time
     * @return The AD time in milliseconds
     */
    public static long filetimeToMillis(long filetime) {
        return filetime / 10000L - 11644473600000L;
    }

    /**
     * From milliseconds to AD time
     * @param millis The milliseconds
     * @return the milliseconds in AD time
     */
    public static long millisToFiletime(long millis) {
        return (millis + 11644473600000L) * 10000L;
    }

    /**
     * From String dd.mm.yyyy to LocalDate
     * @param date the Date as a String
     * @return the date as a LocalDate
     */
    public static LocalDate stringToLocalDate(String date){
        if (date == null)
            return null;
        String[] split = date.split("\\.");
        if (split[2] == null || Integer.parseInt(split[2]) > LocalDate.now().getYear() + 10)
            return null;
        return LocalDate.of(Integer.parseInt(split[0]),Integer.parseInt(split[1]),Integer.parseInt(split[2]));
    }

    /**
     * Transforms a calendar into a string as the following: DD.MM.YYYY (month and day can be only 1 digit).
     * This will return {@link UserAtt#NICHT_GESETZT} if the year is 30828.
     * @param calendar The calendar to transform
     * @return The Date as a String
     */
    public static String calendarToString(Calendar calendar) {
        if ((calendar.get(Calendar.YEAR) == 30828))
            return UserAtt.NICHT_GESETZT;
        return calendar.get(Calendar.DAY_OF_MONTH) + "." + (calendar.get(Calendar.MONTH) + 1) + "." + calendar.get(Calendar.YEAR);
    }

    /**
     * Transforms a date into a calendar. If the String is null this will return a calendar with the year 30828.
     * @param attributeAsString A string in the following format: DD.MM.YYYY
     * @return The calendar
     */
    public static Calendar stringToCalendar(String attributeAsString) {
        Calendar calendar = Calendar.getInstance();
        if (attributeAsString == null){
            calendar.set(Calendar.YEAR, 30828);
            return calendar;
        }
        String[] temp = attributeAsString.split("\\.");
        if (temp[2] == null || Integer.parseInt(temp[2]) > calendar.get(Calendar.YEAR) + 10)
            calendar.set(Calendar.YEAR, 30828);
        else
            calendar.set(Integer.parseInt(temp[2]), Integer.parseInt(temp[1]) - 1, Integer.parseInt(temp[0]));
        return calendar;
    }

    /**
     * Transforms a date to a String. If the year is before 2000 or more than 50 years in the future this will return null
     * @param date The date to transform
     * @return A string in the following format: DD.MM.YYYY, or null
     */
    public static String dateToString(Date date) {
        if (date.getYear() + 1900 > 50 + Calendar.getInstance().get(Calendar.YEAR) || date.getYear() + 1900 <= 2000)
            return null;
        return date.getDate() + "." + (date.getMonth() + 1) + "." + (date.getYear() + 1900);
    }

//    /**
//     * Transforms a JDatePicker to an AD time.
//     * @param date The JDatePicker to transform
//     * @return The AD time as string
//     */
//    public static String jDatetoAD(JDatePicker date) {
//        if (date.getFormattedTextField().getText().equals(""))
//            return String.valueOf(TimeTransformations.VERTRAGSENDE_NOT_SET_TIME);
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(date.getModel().getYear(),date.getModel().getMonth(), date.getModel().getDay(), 0, 0 ,0);
//        return String.valueOf(millisToFiletime(calendar.getTimeInMillis()));
//    }
//
//    /**
//     * Transforms a JDatePicker to a String in the following format: DD.MM.YYYY. If there is nothing set instead returns {@link UserAtt#NICHT_GESETZT}
//     * @param date The JDatePicker to transform
//     * @return The Date as a String, or {@link UserAtt#NICHT_GESETZT}
//     */
//    public static String jDateToString(JDatePicker date) {
//        if (date.getFormattedTextField().getText().equals(""))
//            return UserAtt.NICHT_GESETZT;
//        return calendarToString(stringToCalendar(date.getFormattedTextField().getText()));
//    }

    /**
     * Transforms milliseconds into a string with the following format: DD.MM.YYYY
     * @param date the milliseconds to transform
     * @return The Date, or "Never" if the date equals 0
     */
    public static String millisToStringDate(Long date){
        if (date == 0)
            return "Never";
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);
        return new Date(date).toString();
    }
}
