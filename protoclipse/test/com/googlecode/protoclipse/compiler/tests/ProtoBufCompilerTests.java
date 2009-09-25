package com.googlecode.protoclipse.compiler.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.Path;
import org.junit.Test;

import com.googlecode.protoclipse.compiler.ProtoBufCompiler;


public class ProtoBufCompilerTests {
	
	@Test
	public void testIsMavenStyleJavaSourceFolder() {
		assertFalse(ProtoBufCompiler.isMavenStyleJavaSourceFolder(new Path("")));
		assertFalse(ProtoBufCompiler.isMavenStyleJavaSourceFolder(new Path("src")));
		assertFalse(ProtoBufCompiler.isMavenStyleJavaSourceFolder(new Path("src/main")));
		assertTrue(ProtoBufCompiler.isMavenStyleJavaSourceFolder(new Path("src/main/java")));
		assertTrue(ProtoBufCompiler.isMavenStyleJavaSourceFolder(new Path("src\\main\\java")));
		assertTrue(ProtoBufCompiler.isMavenStyleJavaSourceFolder(new Path("/a/b/c/src/main/java")));
		assertTrue(ProtoBufCompiler.isMavenStyleJavaSourceFolder(new Path("\\a\\b\\c\\src\\main\\java")));
	}

	@Test
	public void testChangeFileExtension() {
		String changed = ProtoBufCompiler.changeFileExtension("c:\\protobuf\\sample.proto", ".java");
		assertEquals("c:\\protobuf\\sample.java", changed);
	}

	@Test
	public void testChangeFileExtensionWithDotsInName() {
		String changed = ProtoBufCompiler.changeFileExtension("c:\\proto.buf\\..\\sample.proto", ".java");
		assertEquals("c:\\proto.buf\\..\\sample.java", changed);
	}
	
	@Test
	public void testGetJavaPackageOptionFromString() {
		assertEquals("net.danielpalma.gateway.protocol", ProtoBufCompiler.getJavaPackageOptionFromString("option java_package = \"net.danielpalma.gateway.protocol\";"));
	}
	
	@Test
	public void testGetJavaPackageOptionFromStringError() {
		assertNull(ProtoBufCompiler.getJavaPackageOptionFromString("java_package net.danielpalma.gateway.protocol"));
	}
	
}
