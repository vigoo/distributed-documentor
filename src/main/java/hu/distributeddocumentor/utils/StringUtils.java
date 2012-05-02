package hu.distributeddocumentor.utils;

public abstract class StringUtils {
    public static String convertSpaces(String str) {
        return str.replaceAll(" ", "%20");
    }
}
