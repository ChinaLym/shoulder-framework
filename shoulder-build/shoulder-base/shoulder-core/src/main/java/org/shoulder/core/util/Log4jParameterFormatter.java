package org.shoulder.core.util;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.shoulder.core.context.AppInfo;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;

/**
 * 采用log4j的文本格式化工具
 * 支持 {} 占位符
 *
 * @see org.apache.logging.log4j.message.ParameterFormatter 非 public 类，这里拿出来用
 */
final class Log4jParameterFormatter {
    /**
     * Prefix for recursion.
     */
    static final String RECURSION_PREFIX = "[...";
    /**
     * Suffix for recursion.
     */
    static final String RECURSION_SUFFIX = "...]";

    /**
     * Prefix for errors.
     */
    static final String ERROR_PREFIX = "[!!!";
    /**
     * Separator for errors.
     */
    static final String ERROR_SEPARATOR = "=>";
    /**
     * Separator for error messages.
     */
    static final String ERROR_MSG_SEPARATOR = ":";
    /**
     * Suffix for errors.
     */
    static final String ERROR_SUFFIX = "!!!]";

    private static final char DELIMIT_START = '{';
    private static final char DELIMIT_STOP = '}';
    private static final char ESCAPE_CHAR = '\\';

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(AppInfo.dateTimeFormat());

    public static String formatMessage(String format, Object... arguments) {
        final int len = Math.max(1, format == null ? 0 : format.length() >> 1);
        int[] indices = new int[len];
        final int placeholders = countArgumentPlaceholders2(format, indices);

        int usedCount = Math.min(placeholders, arguments == null ? 0 : arguments.length);
        StringBuilder stringBuilder = new StringBuilder();
        formatMessage2(stringBuilder, format, arguments, usedCount, indices);

        String message = stringBuilder.toString();
        if (arguments != null) {
            final int argCount = arguments.length;
            if (placeholders < argCount && arguments[argCount - 1] instanceof Throwable throwable) {
                message += System.lineSeparator() + ExceptionUtils.getStackTrace(throwable);
            }
        }
        return message;
    }

    /**
     * Counts the number of unescaped placeholders in the given messagePattern.
     *
     * @param messagePattern the message pattern to be analyzed.
     * @return the number of unescaped placeholders.
     */
    private static int countArgumentPlaceholders2(final String messagePattern,
                                                  final int[] indices) {
        if (messagePattern == null) {
            return 0;
        }
        final int length = messagePattern.length();
        int result = 0;
        boolean isEscaped = false;
        for (int i = 0; i < length - 1; i++) {
            final char curChar = messagePattern.charAt(i);
            if (curChar == ESCAPE_CHAR) {
                isEscaped = !isEscaped;
                indices[0] = -1; // escaping means fast path is not available...
                result++;
            } else if (curChar == DELIMIT_START) {
                if (!isEscaped && messagePattern.charAt(i + 1) == DELIMIT_STOP) {
                    indices[result] = i;
                    result++;
                    i++;
                }
                isEscaped = false;
            } else {
                isEscaped = false;
            }
        }
        return result;
    }

    /**
     * Replace placeholders in the given messagePattern with arguments.
     *
     * @param buffer         the buffer to write the formatted message into
     * @param messagePattern the message pattern containing placeholders.
     * @param arguments      the arguments to be used to replace placeholders.
     */
    private static void formatMessage2(final StringBuilder buffer, final String messagePattern,
                                       final Object[] arguments, final int argCount,
                                       final int[] indices) {
        if (messagePattern == null || arguments == null || argCount == 0) {
            buffer.append(messagePattern);
            return;
        }
        int previous = 0;
        for (int i = 0; i < argCount; i++) {
            buffer.append(messagePattern, previous, indices[i]);
            previous = indices[i] + 2;
            recursiveDeepToString(arguments[i], buffer, null);
        }
        buffer.append(messagePattern, previous, messagePattern.length());
    }

    /**
     * This method performs a deep toString of the given Object.
     * Primitive arrays are converted using their respective Arrays.toString methods while
     * special handling is implemented for "container types", i.e. Object[], Map and Collection because those could
     * contain themselves.
     * <p>
     * dejaVu is used in case of those container types to prevent an endless recursion.
     * </p>
     * <p>
     * It should be noted that neither AbstractMap.toString() nor AbstractCollection.toString() implement such a
     * behavior.
     * They only check if the container is directly contained in itself, but not if a contained container contains the
     * original one. Because of that, Arrays.toString(Object[]) isn't safe either.
     * Confusing? Just read the last paragraph again and check the respective toString() implementation.
     * </p>
     * <p>
     * This means, in effect, that logging would produce a usable output even if an ordinary System.out.println(o)
     * would produce a relatively hard-to-debug StackOverflowError.
     * </p>
     *
     * @param o      the Object to convert into a String
     * @param str    the StringBuilder that o will be appended to
     * @param dejaVu a list of container identities that were already used.
     */
    private static void recursiveDeepToString(final Object o, final StringBuilder str,
                                              final Set<String> dejaVu) {
        if (appendSpecialTypes(o, str)) {
            return;
        }
        if (isMaybeRecursive(o)) {
            appendPotentiallyRecursiveValue(o, str, dejaVu);
        } else {
            tryObjectToString(o, str);
        }
    }

    private static boolean appendSpecialTypes(final Object o, final StringBuilder str) {
        if (o == null || o instanceof String) {
            str.append((String) o);
            return true;
        } else if (o instanceof CharSequence) {
            str.append((CharSequence) o);
            return true;
        } else if (o instanceof Integer) { // LOG4J2-1415 unbox auto-boxed primitives to avoid calling toString()
            str.append(((Integer) o).intValue());
            return true;
        } else if (o instanceof Long) {
            str.append(((Long) o).longValue());
            return true;
        } else if (o instanceof Double) {
            str.append(((Double) o).doubleValue());
            return true;
        } else if (o instanceof Boolean) {
            str.append(((Boolean) o).booleanValue());
            return true;
        } else if (o instanceof Character) {
            str.append(((Character) o).charValue());
            return true;
        } else if (o instanceof Short) {
            str.append(((Short) o).shortValue());
            return true;
        } else if (o instanceof Float) {
            str.append(((Float) o).floatValue());
            return true;
        }
        return appendDate(o, str);
    }

    private static boolean appendDate(final Object o, final StringBuilder str) {
        if (!(o instanceof Date date)) {
            return false;
        }
        str.append(DATE_TIME_FORMATTER.format(date.toInstant().atZone(ZoneId.systemDefault())));
        return true;
    }

    /**
     * Returns {@code true} if the specified object is an array, a Map or a Collection.
     */
    private static boolean isMaybeRecursive(final Object o) {
        return o.getClass().isArray() || o instanceof Map || o instanceof Collection;
    }

    private static void appendPotentiallyRecursiveValue(final Object o, final StringBuilder str,
                                                        final Set<String> dejaVu) {
        final Class<?> oClass = o.getClass();
        if (oClass.isArray()) {
            appendArray(o, str, dejaVu, oClass);
        } else if (o instanceof Map) {
            appendMap(o, str, dejaVu);
        } else if (o instanceof Collection) {
            appendCollection(o, str, dejaVu);
        }
    }

    private static void appendArray(final Object o, final StringBuilder str, Set<String> dejaVu,
                                    final Class<?> oClass) {
        if (oClass == byte[].class) {
            str.append(Arrays.toString((byte[]) o));
        } else if (oClass == short[].class) {
            str.append(Arrays.toString((short[]) o));
        } else if (oClass == int[].class) {
            str.append(Arrays.toString((int[]) o));
        } else if (oClass == long[].class) {
            str.append(Arrays.toString((long[]) o));
        } else if (oClass == float[].class) {
            str.append(Arrays.toString((float[]) o));
        } else if (oClass == double[].class) {
            str.append(Arrays.toString((double[]) o));
        } else if (oClass == boolean[].class) {
            str.append(Arrays.toString((boolean[]) o));
        } else if (oClass == char[].class) {
            str.append(Arrays.toString((char[]) o));
        } else {
            if (dejaVu == null) {
                dejaVu = new HashSet<>();
            }
            // special handling of container Object[]
            final String id = identityToString(o);
            if (dejaVu.contains(id)) {
                str.append(RECURSION_PREFIX).append(id).append(RECURSION_SUFFIX);
            } else {
                dejaVu.add(id);
                final Object[] oArray = (Object[]) o;
                str.append('[');
                boolean first = true;
                for (final Object current : oArray) {
                    if (first) {
                        first = false;
                    } else {
                        str.append(", ");
                    }
                    recursiveDeepToString(current, str, new HashSet<>(dejaVu));
                }
                str.append(']');
            }
            //str.append(Arrays.deepToString((Object[]) o));
        }
    }

    private static void appendMap(final Object o, final StringBuilder str, Set<String> dejaVu) {
        // special handling of container Map
        if (dejaVu == null) {
            dejaVu = new HashSet<>();
        }
        final String id = identityToString(o);
        if (dejaVu.contains(id)) {
            str.append(RECURSION_PREFIX).append(id).append(RECURSION_SUFFIX);
        } else {
            dejaVu.add(id);
            final Map<?, ?> oMap = (Map<?, ?>) o;
            str.append('{');
            boolean isFirst = true;
            for (final Entry<?, ?> o1 : oMap.entrySet()) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    str.append(", ");
                }
                final Object key = o1.getKey();
                final Object value = o1.getValue();
                recursiveDeepToString(key, str, new HashSet<>(dejaVu));
                str.append('=');
                recursiveDeepToString(value, str, new HashSet<>(dejaVu));
            }
            str.append('}');
        }
    }

    private static void appendCollection(final Object o, final StringBuilder str,
                                         Set<String> dejaVu) {
        // special handling of container Collection
        if (dejaVu == null) {
            dejaVu = new HashSet<>();
        }
        final String id = identityToString(o);
        if (dejaVu.contains(id)) {
            str.append(RECURSION_PREFIX).append(id).append(RECURSION_SUFFIX);
        } else {
            dejaVu.add(id);
            final Collection<?> oCol = (Collection<?>) o;
            str.append('[');
            boolean isFirst = true;
            for (final Object anOCol : oCol) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    str.append(", ");
                }
                recursiveDeepToString(anOCol, str, new HashSet<>(dejaVu));
            }
            str.append(']');
        }
    }

    private static void tryObjectToString(final Object o, final StringBuilder str) {
        // it's just some other Object, we can only use toString().
        try {
            str.append(o.toString());
        } catch (final Throwable t) {
            handleErrorInObjectToString(o, str, t);
        }
    }

    private static void handleErrorInObjectToString(final Object o, final StringBuilder str,
                                                    final Throwable t) {
        str.append(ERROR_PREFIX);
        str.append(identityToString(o));
        str.append(ERROR_SEPARATOR);
        final String msg = t.getMessage();
        final String className = t.getClass().getName();
        str.append(className);
        if (!className.equals(msg)) {
            str.append(ERROR_MSG_SEPARATOR);
            str.append(msg);
        }
        str.append(ERROR_SUFFIX);
    }

    /**
     * This method returns the same as if Object.toString() would not have been
     * overridden in obj.
     * <p>
     * Note that this isn't 100% secure as collisions can always happen with hash codes.
     * </p>
     * <p>
     * Copied from Object.hashCode():
     * </p>
     * <blockquote>
     * As much as is reasonably practical, the hashCode method defined by
     * class {@code Object} does return distinct integers for distinct
     * objects. (This is typically implemented by converting the internal
     * address of the object into an integer, but this implementation
     * technique is not required by the Java&#8482; programming language.)
     * </blockquote>
     *
     * @param obj the Object that is to be converted into an identity string.
     * @return the identity string as also defined in Object.toString()
     */
    private static String identityToString(final Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(obj));
    }

}
