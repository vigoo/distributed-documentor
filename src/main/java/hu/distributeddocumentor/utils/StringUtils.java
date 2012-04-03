package hu.distributeddocumentor.utils;


public class StringUtils {
    public static String convertSpaces(String str) {
        return str.replaceAll(" ", "%20");
    }
}
