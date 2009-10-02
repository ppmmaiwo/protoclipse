package com.googlecode.protoclipse.compiler.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


import org.junit.Test;

import com.googlecode.protoclipse.compiler.CompileError;


public class CompileErrorTests {
	
	@Test
	public void testParse() {
		final String string = "foo.bar:77:9: message";
		CompileError error = CompileError.parse(string);
		assertNotNull(error);
		assertEquals("foo.bar", error.getFileName());
		assertEquals(77, error.getLineNumber());
		assertEquals(9, error.getCharacterStart());
		assertEquals("message", error.getMessage());
		assertEquals(string, error.toString());
	}
	
	@Test
	public void testParseWholePathFile() {
		final String string = "d:\\foo.bar:66:88: abc def ghi jkl mno pqr stuvwxyz";
		CompileError error = CompileError.parse(string);
		assertNotNull(error);
		assertEquals("d:\\foo.bar", error.getFileName());
		assertEquals(66, error.getLineNumber());
		assertEquals(88, error.getCharacterStart());
		assertEquals("abc def ghi jkl mno pqr stuvwxyz", error.getMessage());
		assertEquals(string, error.toString());
	}
	
	@Test
	public void testParseOnlyFileAndMessage() {
		final String string = "c:\\file.name: the message";
		CompileError error = CompileError.parse(string);
		assertNotNull(error);
		assertEquals("c:\\file.name", error.getFileName());
		assertEquals(-1, error.getLineNumber());
		assertEquals(-1, error.getCharacterStart());
		assertEquals("the message", error.getMessage());
		assertEquals(string, error.toString());
	}

}
