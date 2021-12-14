package com.tflow.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateTimeUtil {
    private static final Logger log = LoggerFactory.getLogger(DateTimeUtil.class);
    public static final String DEFAULT_ZONE = "Asia/Bangkok";
    public static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";
    public static final String DEFAULT_DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss";

    public static final BigDecimal MANDAYS_HOUR = new BigDecimal(8);
    public static final BigDecimal MANDAYS_MINUTE = new BigDecimal(60);
    public static final int DEFAULT_SCALE = 2;
    public static final int NATURAL_SCALE = 8;

    public static Date now() {
        return Date.from(Instant.now());
    }

    public static Date currentDate() {
        Instant instant = LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    public static Date yesterdayDate() {
        Instant instant = LocalDate.now().atStartOfDay().minusDays(1).atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    public static Date getDatePlusHoursAndMinutes(Date date, long hours, long minutes) {
        Instant instant = LocalDate.ofInstant(date.toInstant(), ZoneId.of(DEFAULT_ZONE)).atStartOfDay().plusHours(hours).plusMinutes(minutes).atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    public static Date getDatePlusDays(Date date, int days) {
        Instant instant = LocalDate.ofInstant(date.toInstant(), ZoneId.of(DEFAULT_ZONE)).atStartOfDay().plusDays(days).atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    public static Date getDatePlusMonths(Date date, int months) {
        Instant instant = LocalDate.ofInstant(date.toInstant(), ZoneId.of(DEFAULT_ZONE)).atStartOfDay().plusMonths(months).atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    public static Date getDatePlusYears(Date date, int years) {
        Instant instant = LocalDate.ofInstant(date.toInstant(), ZoneId.of(DEFAULT_ZONE)).atStartOfDay().plusYears(years).atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    public static int getYear(Date date) {
        return LocalDate.ofInstant(date.toInstant(), ZoneId.of(DEFAULT_ZONE)).getYear();
    }

    public static Date setTime(Date date, int hour, int minute, int second) {
        Instant instant = LocalDate.ofInstant(date.toInstant(), ZoneId.of(DEFAULT_ZONE)).atTime(hour, minute, second).atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    public static String getDateStr(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(),
                ZoneId.of(DEFAULT_ZONE)).format(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT));
    }

    public static String getDateStr(Date date, String pattern) {
        return LocalDateTime.ofInstant(date.toInstant(),
                ZoneId.of(DEFAULT_ZONE)).format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String getDateTimeStr(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(),
                ZoneId.of(DEFAULT_ZONE)).format(DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT));
    }

    public static Date getFirstDateOfMonth(Date month) {
        LocalDate initial = month.toInstant().atZone(ZoneId.of(DEFAULT_ZONE)).toLocalDate();
        return Date.from(initial.withDayOfMonth(1).atStartOfDay(ZoneId.of(DEFAULT_ZONE)).toInstant());
    }

    public static Date getLastDateOfMonth(Date month) {
        LocalDate initial = month.toInstant().atZone(ZoneId.of(DEFAULT_ZONE)).toLocalDate();
        return Date.from(initial.withDayOfMonth(initial.lengthOfMonth()).atStartOfDay(ZoneId.of(DEFAULT_ZONE)).toInstant());
    }

    public static Date getFirstDateOfYear(Date year) {
        LocalDate initial = year.toInstant().atZone(ZoneId.of(DEFAULT_ZONE)).toLocalDate();
        return Date.from(initial.withDayOfYear(1).atStartOfDay(ZoneId.of(DEFAULT_ZONE)).toInstant());
    }

    public static Date getLastDateOfYear(Date year) {
        LocalDate initial = year.toInstant().atZone(ZoneId.of(DEFAULT_ZONE)).toLocalDate();
        return Date.from(initial.withDayOfYear(initial.lengthOfYear()).atStartOfDay(ZoneId.of(DEFAULT_ZONE)).toInstant());
    }

    public static Date getFirstDateOfYear(int year) {
        return Date.from(new GregorianCalendar(year, Calendar.JANUARY, 1).toInstant());
    }

    public static Date getLastDateOfYear(int year) {
        return Date.from(new GregorianCalendar(year, Calendar.DECEMBER, 31).toInstant());
    }

    public static boolean isSameDate(Date date1, Date date2) {
        Instant instant1 = LocalDate.ofInstant(date1.toInstant(), ZoneId.of(DEFAULT_ZONE)).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        Instant instant2 = LocalDate.ofInstant(date2.toInstant(), ZoneId.of(DEFAULT_ZONE)).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        return instant1.compareTo(instant2) == 0;
    }

    public static Duration diffTime(Date time1, Date time2) {
        LocalDateTime t1 = time1.toInstant().atZone(ZoneId.of(DEFAULT_ZONE)).toLocalDateTime();
        LocalDateTime t2 = time2.toInstant().atZone(ZoneId.of(DEFAULT_ZONE)).toLocalDateTime();

        return Duration.between(t1, t2);
    }

    public static String durationToString(Duration duration) {
        if (duration == null) {
            return "00:00";
        }

        long hour = duration.toHours();
        long minute = duration.toMinutesPart();

        return String.format("%02d:%02d", hour, minute);
    }

    /**
     * Hours:Minutes to Duration
     *
     * @param str in this pattern 'HH:mm'
     */
    public static Duration stringToDuration(String str) {
        if (str == null) {
            return Duration.parse("PT0M");
        }

        String[] tmp = str.split(":");
        if (tmp.length == 1) {
            return Duration.parse("PT" + tmp[0] + "M");
        } else {
            return Duration.parse("PT" + tmp[0] + "H" + tmp[1] + "M");
        }
    }

    public static BigDecimal getManDays(long minutes) {
        BigDecimal m = new BigDecimal(minutes);
        BigDecimal result = m.divide(MANDAYS_MINUTE, DEFAULT_SCALE, RoundingMode.HALF_UP)
                .divide(MANDAYS_HOUR, DEFAULT_SCALE, RoundingMode.HALF_UP);
//        log.debug("getManDay. (minutes: {}, manDays: {} MD.)",minutes,result);
        return result;
    }

    public static BigDecimal getTotalMD(Long... minutes) {
        long total = getTotalMinute(minutes);
        return getManDays(total);
    }

    public static Duration getTotalDuration(Long... minutes) {
        Duration duration = Duration.ZERO;
        for (Long l : minutes) {
            duration = duration.plus(Duration.ofMinutes(l));
        }
        return duration;
    }

    public static long getTotalMinute(Long... minutes) {
        long total = 0L;
        for (Long l : minutes) {
            total = total + l;
        }
        return total;
    }

    public static long countWorkingDay(final Date month) {
        final LocalDate start = month.toInstant().atZone(ZoneId.of(DEFAULT_ZONE)).toLocalDate().withDayOfMonth(1);
        final LocalDate end = month.toInstant().atZone(ZoneId.of(DEFAULT_ZONE)).withDayOfMonth(start.lengthOfMonth()).plusDays(1).toLocalDate();
//        log.debug("start: {}, end: {}",start,end);

        final long days = start.lengthOfMonth();
//        log.debug("days: {}",days);

//        log.debug("start.getDayOfWeek(): {}",start.getDayOfWeek());
//        log.debug("days - 2 * ((days + start.getDayOfWeek().getValue())/7)");
//        log.debug("{} - 2 * (({} + {})/{})",days,days,start.getDayOfWeek().getValue(),7);
        final long daysWithoutWeekends = days - 2 * ((days + start.getDayOfWeek().getValue()) / 7);
        return daysWithoutWeekends + (start.getDayOfWeek() == DayOfWeek.SUNDAY ? 1 : 0) + (end.getDayOfWeek() == DayOfWeek.SUNDAY ? 1 : 0);

//        Set<DayOfWeek> weekend = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
//        return start.datesUntil(end)
//                .filter(d -> !weekend.contains(d.getDayOfWeek()))
//                .count();
    }

    /**
     * This code is modified version of: DateTimeUtil.countWorkingDay()
     */
    public static long countWorkingDay(Date startDate, Date endDate) {
        final LocalDate start = startDate.toInstant().atZone(ZoneId.of(DEFAULT_ZONE)).toLocalDate().withDayOfMonth(1);
        final LocalDate end = endDate.toInstant().atZone(ZoneId.of(DEFAULT_ZONE)).withDayOfMonth(start.lengthOfMonth()).plusDays(1).toLocalDate();

        final long days = countDay(startDate, endDate);
        final long daysWithoutWeekends = days - 2 * ((days + start.getDayOfWeek().getValue()) / 7);
        return daysWithoutWeekends + (start.getDayOfWeek() == DayOfWeek.SUNDAY ? 1 : 0) + (end.getDayOfWeek() == DayOfWeek.SUNDAY ? 1 : 0);
    }

    public static long countDay(Date startDate, Date endDate) {
        return (endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24) + 1;
    }


    public static boolean isHoliday(final Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.of(DEFAULT_ZONE)).toLocalDate();
        return localDate.getDayOfWeek() == DayOfWeek.SUNDAY || localDate.getDayOfWeek() == DayOfWeek.SATURDAY;
    }

    public static Duration getWorkHour(Date timeIn, Date timeOut) {
        log.debug("getWorkHour. (timeIn: {}, timeOut: {})", timeIn, timeOut);

        long mid = 1300;
        long in = Long.parseLong(DateTimeUtil.getDateStr(timeIn, "Hmm"));
        long out = Long.parseLong(DateTimeUtil.getDateStr(timeOut, "Hmm"));
        Duration duration = DateTimeUtil.diffTime(timeIn, timeOut);
        log.debug("getWorkHour. in:{}, out:{}, mid:{}", in, out, mid);

        /* KUDU-22: use this table for WorkHour/Duration (mid = 13:00, before mid is < mid, after mid is => mid)
         *
         * | In         | Out        |WorkHour/Duration|
         * | ---------- | ---------- | -------------- |
         * | before mid | before mid | = duration     |
         * | before mid | after mid  | = duration - 1 |
         * | after mid  | after mid  | = duration     |
         *
         * | In         | Out        | WorkHour       |
         * | ---------- | ---------- | -------------- |
         * | < mid      | < mid      | = duration     |
         * | < mid      | >= mid     | = duration - 1 |
         * | >= mid     | >= mid     | = duration     |
         **/
        if (in < mid && out >= mid) {
            duration = duration.plusHours(-1);
        }

        log.debug("getWorkHour = {}", duration);
        return duration;
    }

    public static long mandaysToMinutes(BigDecimal mandays) {
        return mandays.multiply(MANDAYS_HOUR).multiply(MANDAYS_MINUTE).longValue();
    }
}
