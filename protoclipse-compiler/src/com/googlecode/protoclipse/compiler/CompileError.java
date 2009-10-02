package com.googlecode.protoclipse.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class CompileError {

	private final String fileName;
	private final int lineNumber;
	private final int characterStart;
	private final String message;
	
	private static final String SEPARATOR = ":";
	
	public static final int NO_NUMBER = -1;
	public static final CompileError NO_ERROR = new CompileError("", NO_NUMBER, NO_NUMBER, "");
	
	CompileError(String fileName, int lineNumber, int characterStart,
			String message) {
		if (fileName == null) {
			throw new NullPointerException("fileName");
		}
		this.fileName = fileName;
		this.lineNumber = lineNumber;
		this.characterStart = characterStart;
		this.message = message;
	}
	
	private static ArrayList<String> split(String string, String regex) {
		String[] parts = string.split(regex);
		ArrayList<String> array = new ArrayList<String>(parts.length);
		array.addAll(Arrays.asList(parts));
		return array;
	}

	// Parse string in format "filename.extension:line:character: message"
	public static CompileError parse(String string) {
		ArrayList<String> parts = split(string, SEPARATOR);
		if (parts == null || parts.isEmpty()) {
			throw new RuntimeException("Unable to parse protoc error string");
		}
		
		String message = parts.get(parts.size() - 1).trim();
		parts.remove(parts.size() - 1);
		
		int charStart = NO_NUMBER;
		try {
			charStart = Integer.parseInt(parts.get(parts.size() - 1));
			parts.remove(parts.size() - 1);
		}
		catch (NumberFormatException nfe) {
		}
		
		int lineNumber = NO_NUMBER;
		try {
			lineNumber = Integer.parseInt(parts.get(parts.size() - 1));
			parts.remove(parts.size() - 1);
		}
		catch (NumberFormatException nfe) {
		}

		StringBuffer fileNameBuffer = new StringBuffer();
		Iterator<String> iter = parts.iterator();
		while (iter.hasNext()) {
			if (fileNameBuffer.length() > 0) {
				fileNameBuffer.append(SEPARATOR);
			}
			fileNameBuffer.append(iter.next());
		}
		String fileName = fileNameBuffer.toString();
		if (fileName.isEmpty()) {
			return NO_ERROR;
		}
		else {
			return new CompileError(fileName, lineNumber, charStart, message);
		}
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getFileName()).append(SEPARATOR);
		if (getLineNumber() != NO_NUMBER) {
			buffer.append(getLineNumber()).append(SEPARATOR);
		}
		if (getCharacterStart() != NO_NUMBER) {
			buffer.append(getCharacterStart()).append(SEPARATOR);
		}
		buffer.append(" ");
		buffer.append(getMessage());
		return buffer.toString();
	}

	public String getFileName() {
		return fileName;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public int getCharacterStart() {
		return characterStart;
	}

	public String getMessage() {
		return message;
	}

}
