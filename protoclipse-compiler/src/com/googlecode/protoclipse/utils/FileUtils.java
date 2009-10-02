package com.googlecode.protoclipse.utils;


public class FileUtils {

	public static final String FILE_SEPARATOR = System.getProperty("file.separator");

	public static String joinPaths(Object... paths) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < paths.length; ++i) {
			if (buffer.lastIndexOf(FileUtils.FILE_SEPARATOR) != (buffer.length() - 1)) {
				buffer.append(FileUtils.FILE_SEPARATOR);
			}
			buffer.append(paths[i]);
		}
		return buffer.toString();
	}

}
