package com.googlecode.protoclipse.compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;


import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.googlecode.protoclipse.utils.FileUtils;

public class ProtoBufCompiler {
	
	public static final String PROTOC = "protoc";
	
	private static final String JAVA_FILE_EXTENSION = ".java";

	private final String compilerPath;
	
	public ProtoBufCompiler(IPath compilerPath) {
		this.compilerPath = compilerPath.toString();
	}
	
	private static IJavaProject getJavaProject(IResource resource) {
		IProject project = resource.getProject();
		IJavaProject javaProject = (IJavaProject)project.getAdapter(IJavaProject.class);
		if (javaProject != null) {
			return javaProject;
		}
		return JavaCore.create(project);
	}
	
	public static boolean isMavenStyleJavaSourceFolder(IPath path) {
		String[] segments = path.segments();
		if (segments.length >= 3) {
			if (segments[segments.length - 1].equalsIgnoreCase("java")
					&& segments[segments.length - 2].equalsIgnoreCase("main")
					&& segments[segments.length - 3].equalsIgnoreCase("src")) {
				return true;
			}
		}
		return false;
	}
	
	private static IPath getJavaSourceFolderForFile(IFile file) {
		IPath path = null;
		IJavaProject javaProject = getJavaProject(file);
		if (javaProject != null) {
			try {
				IPackageFragmentRoot[] roots = javaProject.getAllPackageFragmentRoots();
				for (int i = 0; i < roots.length; ++i) {
					if (roots[i].getKind() != IPackageFragmentRoot.K_SOURCE) {
						continue;
					}
					IPath srcPath = roots[i].getCorrespondingResource().getFullPath();
					if (srcPath.isPrefixOf(file.getFullPath())) {
						return roots[i].getCorrespondingResource().getLocation();
					}
					else if (isMavenStyleJavaSourceFolder(srcPath)
							&& roots[i].getJavaProject().equals(javaProject)) {
						path = roots[i].getCorrespondingResource().getLocation();
					}
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
		return path;
	}

	public List<CompileError> compile(IFile file) throws IOException, InterruptedException {
		IPath srcFolder = getJavaSourceFolderForFile(file);
		if (srcFolder == null) {
			ArrayList<CompileError> list = new ArrayList<CompileError>();
			list.add(new CompileError(file.toString(), 0, 0, String.format(
					"Unable to find an appropriate Java source folder for %s",
					file.getName())));
			return list;
		}
		IPath location = file.getLocation();
		return compile(location.toFile(), srcFolder);
	}

	private List<CompileError> compile(File file, IPath outputPath) throws IOException, InterruptedException {
		if (!file.exists()) {
			throw new RuntimeException("File \"" + file.getAbsoluteFile() + "\" not found");
		}
		
		String commandLine = buildCommandLine(file, outputPath);
		
		Process p = Runtime.getRuntime().exec(commandLine);
		
		List<CompileError> errors = new ArrayList<CompileError>();
		
		BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		
		String line;
		while ((line = err.readLine()) != null) {
			CompileError error = CompileError.parse(line);
			if (!error.equals(CompileError.NO_ERROR)) {
				errors.add(error);
			}
		}
		
		err.close();
		
		p.waitFor();
		
		return errors;
	}
	
	private String buildCommandLine(File file, IPath outputPath) {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(FileUtils.joinPaths(getCompilerPath(), PROTOC));
		buffer.append(" --java_out=").append(outputPath);
		
		String parent = file.getParent();
		if (parent != null) {
			buffer.append(" --proto_path=").append(parent);
		}
		
		buffer.append(" ").append(file);
		
		return buffer.toString();
	}

	private String getCompilerPath() {
		return compilerPath;
	}

	public static String changeFileExtension(String fileName, String extension) {
		String newExtension = extension.startsWith(".") ? extension : "." + extension;
		return fileName.replaceFirst("\\.[^\\.]*$", newExtension);
	}

	public static IFile getGeneratedJavaFile(IFile protoFile) {
		try {
			IFile generatedJavaFile = getGeneratedJavaFileFromPackageOption(protoFile);
			if (generatedJavaFile != null) {
				return generatedJavaFile;
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String generatedJavaName = changeFileExtension(protoFile
				.getProjectRelativePath().toString(), JAVA_FILE_EXTENSION);
		return protoFile.getProject().getFile(generatedJavaName);
	}

	private static IFile getGeneratedJavaFileFromPackageOption(IFile protoFile)
			throws CoreException, IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				protoFile.getContents()));
		for (String line = reader.readLine(); line != null; line = reader.readLine()) {
			line = line.trim();
			if (!line.startsWith("option")) {
				continue;
			}
			String p = getJavaPackageOptionFromString(line);
			if (p != null) {
				IPath loc = getJavaSourceFolderForFile(protoFile);
				if (loc != null) {
					StringBuffer buffer = new StringBuffer();
					buffer.append(loc.addTrailingSeparator());
					String replaceAll = p.replaceAll("\\.", "/");
					buffer.append(replaceAll);
					buffer.append("/");
					buffer.append(protoFile.getName());
					String generatedJavaName = changeFileExtension(buffer.toString(), JAVA_FILE_EXTENSION);
					return protoFile.getProject().getFile(generatedJavaName);
				}
			}
		}
		return null;
	}

	public static String getJavaPackageOptionFromString(String string) {
		Scanner s = new Scanner(string);
		s.findInLine("^\\s*option\\s+java_package\\s*=\\s*\"(.*)\"\\s*;\\s*");
		try {
			MatchResult result = s.match();
			return result.group(1);
		}
		catch (IllegalStateException e) {
			return null;
		}
	}

}
