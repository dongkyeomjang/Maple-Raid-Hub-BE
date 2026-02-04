package com.mapleraid.core.utility;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 날짜 및 시간 관련 유틸리티 클래스
 */
public class DateTimeUtil {

    public static final DateTimeFormatter ISODateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter ISODateTimeSecondFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter ISODateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter ISOTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter KORDateFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
    public static final DateTimeFormatter KORDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    public static final DateTimeFormatter KORDateTimeMinuteFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    public static final DateTimeFormatter KORDayHourFormatter = DateTimeFormatter.ofPattern("dd일 HH시");
    public static final DateTimeFormatter ISODateKorHourFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH시");
    public static final DateTimeFormatter ISODateKorHourMinuteFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH시 mm분");
    public static final DateTimeFormatter KORMonthDayFormatter = DateTimeFormatter.ofPattern("MM월 dd일");
    public static final DateTimeFormatter KORMonthFormatter = DateTimeFormatter.ofPattern("MM월");
    public static final DateTimeFormatter ISOYearMonthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
    public static final DateTimeFormatter KORDateDayOfWeekFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 EEEE");
    public static final DateTimeFormatter DayOfMonthFormatter = DateTimeFormatter.ofPattern("MM.dd");
    public static final DateTimeFormatter YearFormatter = DateTimeFormatter.ofPattern("yyyy");

    /**
     * String을 LocalDateTime으로 변환
     *
     * @param date String
     * @return LocalDateTime
     */
    public static LocalDateTime convertStringToLocalDateTime(String date) {
        return LocalDateTime.parse(date, ISODateTimeFormatter);
    }

    /**
     * LocalDateTime을 String으로 변환
     *
     * @param date LocalDateTime
     * @return String
     */
    public static String convertLocalDateTimeToString(LocalDateTime date) {
        return date.format(ISODateTimeFormatter);
    }

    /**
     * LocalDateTime을 String(초 단위 포함)으로 변환
     *
     * @param date LocalDateTime
     * @return String
     */
    public static String convertLocalDateTimeToStringWithSeconds(LocalDateTime date) {
        return date.format(ISODateTimeSecondFormatter);
    }

    /**
     * String을 LocalTime으로 변환
     *
     * @param time String
     * @return LocalTime
     */
    public static LocalTime convertStringToLocalTime(String time) {
        return LocalTime.parse(time, ISOTimeFormatter);
    }

    /**
     * LocalTime을 String으로 변환
     *
     * @param time LocalTime
     * @return String
     */
    public static String convertLocalTimeToString(LocalTime time) {
        return time.format(ISOTimeFormatter);
    }

    /**
     * String을 LocalDate로 변환
     *
     * @param date String
     * @return LocalDate
     */
    public static LocalDate convertStringToLocalDate(String date) {
        return LocalDate.parse(date, ISODateFormatter);
    }

    /**
     * LocalDate를 String으로 변환
     *
     * @param date LocalDate
     * @return String
     */
    public static String convertLocalDateToString(LocalDate date) {
        return date.format(ISODateFormatter);
    }

    /**
     * LocalDate를 한국어 날짜 형식으로 변환 (yyyy년 MM월 dd일)
     *
     * @param date LocalDate
     * @return String
     */
    public static String convertLocalDateToKORString(LocalDateTime date) {
        return date.format(KORDateFormatter);
    }

    /**
     * String(한국어 날짜 형식, yyyy년 MM월 dd일)을 LocalDate로 변환
     *
     * @param date String
     * @return LocalDate
     */
    public static LocalDateTime convertKORStringToLocalDate(String date) {
        return LocalDateTime.parse(date, KORDateFormatter);
    }

    /**
     * 두 날짜 사이의 일 수 계산
     *
     * @param startDate 시작 날짜
     * @param endDate   종료 날짜
     * @return Integer
     */
    public static Integer calculateDaysBetween(LocalDate startDate, LocalDate endDate) {
        return (int) (endDate.toEpochDay() - startDate.toEpochDay());
    }

    /**
     * String(YYYYMMDD) 형식의 날짜를 LocalDate로 변환
     *
     * @param date String
     * @return LocalDate
     */
    public static LocalDate convertStringToLocalDateYYYYMMDD(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    /**
     * Unix 타임스탬프(long)를 LocalDate로 변환
     *
     * @param unixTime Unix 타임스탬프 (초 단위)
     * @return LocalDate
     */
    public static LocalDate convertUnixTimeToLocalDate(long unixTime) {
        return Instant.ofEpochSecond(unixTime)
                .atZone(ZoneId.of("Asia/Seoul"))
                .toLocalDate();
    }

    /**
     * LocalDateTime을 한국어 날짜 형식으로 변환 (dd일 HH시)
     *
     * @param date LocalDateTime
     * @return String
     */
    public static String convertLocalDateTimeToKORDayHour(LocalDateTime date) {
        return date.format(KORDayHourFormatter);
    }

    /**
     * LocalDateTime을 ISO 형식의 날짜 형식으로 변환 (yyyy-MM-dd HH시)
     *
     * @param date LocalDateTime
     * @return String
     */
    public static String convertLocalDateTimeToISODateKorHour(LocalDateTime date) {
        return date.format(ISODateKorHourFormatter);
    }

    /**
     * LocalDateTime을 ISO 형식의 날짜 형식으로 변환 (yyyy-MM-dd HH시 mm분)
     *
     * @param date LocalDateTime
     * @return String
     */
    public static String convertLocalDateTimeToISODateKorHourMinute(LocalDateTime date) {
        return date.format(ISODateKorHourMinuteFormatter);
    }

    /**
     * LocalDateTime을 ISO 형식의 날짜 형식으로 변환 (yyyy-MM-dd)
     *
     * @param date LocalDateTime
     * @return String
     */
    public static String convertLocalDateTimeToISODate(LocalDateTime date) {
        return date.format(ISODateFormatter);
    }

    /**
     * LocalDateTime을 한국어 월일 형식으로 변환 (MM월 dd일)
     *
     * @param date LocalDateTime
     * @return String
     */
    public static String convertLocalDateTimeToKORMonthDay(LocalDateTime date) {
        return date.format(KORMonthDayFormatter);
    }

    /**
     * LocalDateTime을 한국어 월 형식으로 변환 (MM월)
     *
     * @param date LocalDateTime
     * @return String
     */
    public static String convertLocalDateTimeToKORMonth(LocalDateTime date) {
        return date.format(KORMonthFormatter);
    }

    /**
     * LocalDateTime을 ISO 형식의 년월 형식으로 변환 (yyyy-MM)
     *
     * @param date LocalDateTime
     * @return String
     */
    public static String convertLocalDateTimeToISOYearMonth(LocalDateTime date) {
        return date.format(ISOYearMonthFormatter);
    }

    /**
     * LocalDateTime을 yyyy-MM-dd EEEE 형식으로 변환
     *
     * @param date LocalDateTime
     * @return String
     */
    public static String convertLocalDateTimeToKORDateDayOfWeek(LocalDateTime date) {
        return date.format(KORDateDayOfWeekFormatter);
    }

    /**
     * LocalDate를 yyyy.MM.dd 형식으로 변환
     *
     * @param date LocalDate
     * @return String
     */
    public static String convertLocalDateToKORDateTime(LocalDate date) {
        return date.format(KORDateTimeFormatter);
    }

    /**
     * LocalDateTime을 yyyy.MM.dd 형식으로 변환
     *
     * @param date LocalDateTime
     * @return String
     */
    public static String convertLocalDateTimeToKORDateTime(LocalDateTime date) {
        return date.format(KORDateTimeFormatter);
    }

    /**
     * LocalDateTime을 yyyy.MM.dd HH:mm 형식으로 변환
     *
     * @param date LocalDateTime
     * @return String
     */
    public static String convertLocalDateTimeToKORDateTimeMinute(LocalDateTime date) {
        return date.format(KORDateTimeMinuteFormatter);
    }

    /**
     * LocalDateTime을 MM.dd 형식으로 변환
     *
     * @param date
     * @return
     */
    public static String convertLocalDateTimeToDayOfMonth(LocalDateTime date) {
        return date.format(DayOfMonthFormatter);
    }

    /**
     * LocalDateTime을 Year 형식으로 변환
     *
     * @param date
     * @return
     */
    public static String convertLocalDateTimeToYear(LocalDateTime date) {
        return date.format(YearFormatter);
    }
}
