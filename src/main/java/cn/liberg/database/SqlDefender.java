package cn.liberg.database;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 防Sql注入辅助类
 *
 * @author Liberg
 */
public class SqlDefender {
    public static final Pattern emojiPattern = Pattern.compile(
            "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
            Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);

    /**
     * 过滤类型支持：Byte,Integer,Long,String
     * @param value
     * @return
     */
    public static String format(Object value) {
        if (value != null) {
            if (value instanceof Number) {
                return value.toString();
            } else {
                // 过滤String类型的数据，防止sql注入
                return format(value.toString());
            }
        }
        return null;
    }

    public static String format(String value) {
        if(value == null) {
            return null;
        } else {
            String result = value.replace("\\", "\\\\")
                    .replace("'", "\\'");
            return '\'' + result + '\'';
        }
    }

    public static String filterEmoji(String source) {
        if (source != null) {
            Matcher emojiMatcher = emojiPattern.matcher(source);
            if (emojiMatcher.find()) {
                source = emojiMatcher.replaceAll("");
            }
        }
        return source;
    }
}
