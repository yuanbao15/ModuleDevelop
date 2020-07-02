package com.example.uhf.tools;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtils {

    public static String replaceUrlWithPlus(String url) {
        // 1. 处理特殊字符
        // 2. 去除后缀名带来的文件浏览器的视图凌乱(特别是图片更�?��如此类似处理，否则有的手机打�?��库，全是我们的缓存图�?
        if (url != null) {
            return url.replaceAll("http://(.)*?/", "")
                    .replaceAll("[.:/,%?&=]", "+").replaceAll("[+]+", "+");
        }
        return null;
    }

    /**
     * 验证ip是否合法
     *
     * @param text ip地址
     * @return 验证信息
     */
    public static Boolean isIP(String text) {
        if (text != null && !text.isEmpty()) {
            // 定义正则表达式
            String regex = "^((25[0-5])|(2[0-4]\\d)|(1\\d\\d)|([1-9]\\d)|\\d)(\\.((25[0-5])|(2[0-4]\\d)|(1\\d\\d)|([1-9]\\d)|\\d)){3}$";
            // 判断ip地址是否与正则表达式匹配
            if (text.matches(regex)) {
                // 返回判断信息
                return true;
            } else {
                // 返回判断信息
                return false;
            }
        }
        // 返回判断信息
        return false;
    }

    /**
     * 验证域名是否合法
     *
     * @param text 域名
     * @return 验证信息
     */
    public static Boolean isDomain(String text) {
        if (text != null && !text.isEmpty()) {
            // 定义正则表达式
            String regex = "^([a-zA-Z0-9\\.\\-]+(\\:[a-zA-Z0-9\\.&amp;%\\$\\-]+)*@)*((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|localhost|([a-zA-Z0-9\\-]+\\.)*[a-zA-Z0-9\\-]+\\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))$";
            // 判断域名是否与正则表达式匹配
            if (text.matches(regex)) {
                // 返回判断信息
                return true;
            } else {
                // 返回判断信息
                return false;
            }
        }
        // 返回判断信息
        return false;
    }

    public static boolean isEmpty(CharSequence cs) {

        return cs == null || cs.length() == 0;

    }

    public static boolean isNotEmpty(CharSequence cs) {

        return !StringUtils.isEmpty(cs);

    }

    public static String trim(String str) {

        return str == null ? null : str.trim();

    }

    /**
     * 字符串转整数
     *
     * @param str
     * @param defValue
     * @return
     */
    public static int toInt(String str, int defValue) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
        }
        return defValue;
    }

    /**
     * 对象转整数
     *
     * @param obj
     * @return 转换异常返回 0
     */
    public static int toInt(Object obj) {
        if (obj == null)
            return 0;
        return toInt(obj.toString(), 0);
    }

    /**
     * 对象转整数
     *
     * @param obj
     * @return 转换异常返回 0
     */
    public static long toLong(String obj) {
        try {
            return Long.parseLong(obj);
        } catch (Exception e) {
        }
        return 0;
    }

    /**
     * 字符转double
     *
     * @param obj
     * @return 转换异常返回 0
     */
    public static double toDouble(String obj) {
        try {
            return Double.parseDouble(obj);
        } catch (Exception e) {
        }
        return 0;
    }

    /**
     * 字符串转布尔值
     *
     * @param b
     * @return 转换异常返回 false
     */
    public static boolean toBool(String b) {
        try {
            return Boolean.parseBoolean(b);
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 判断是否为整数 INT
     *
     * @param val
     * @return
     */
    public static Boolean isInt(String val) {
        try {
            Integer.parseInt(val);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getTimeString() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        return formatter.format(curDate);
    }

    public static String getTimeFormat(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
        Date curDate = new Date(time);// 获取当前时间
        return formatter.format(curDate);
    }

    /**
     * 判断是否是十六进制
     *
     * @param str
     * @return
     */
    public static boolean isHexNumber(String str) {
        boolean flag = false;
        for (int i = 0; i < str.length(); i++) {
            char cc = str.charAt(i);
            if (cc == '0' || cc == '1' || cc == '2' || cc == '3' || cc == '4'
                    || cc == '5' || cc == '6' || cc == '7' || cc == '8'
                    || cc == '9' || cc == 'A' || cc == 'B' || cc == 'C'
                    || cc == 'D' || cc == 'E' || cc == 'F' || cc == 'a'
                    || cc == 'b' || cc == 'c' || cc == 'c' || cc == 'd'
                    || cc == 'e' || cc == 'f') {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 十六进制字符串转换成char数组
     *
     * @param s
     * @return
     */
    public static char[] HexStringToChars(String s) {
        char[] bytes;
        bytes = new char[s.length() / 2];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (char) Integer.parseInt(s.substring(2 * i, 2 * i + 2),
                    16);
        }

        return bytes;
    }

}
