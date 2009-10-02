package com.googlecode.protoclipse.builder;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;

import com.googlecode.protoclipse.Activator;
import com.googlecode.protoclipse.compiler.CompileError;
import com.googlecode.protoclipse.compiler.ProtoBufCompiler;
import com.googlecode.protoclipse.preferences.PreferenceConstants;


public class ProtoBufBuilder extends IncrementalProjectBuilder {

	private static final String PROTO_FILE_EXTENSION = ".proto";
	
	class SampleDeltaVisitor implements IResourceDeltaVisitor {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				compileProto(resource);
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				compileProto(resource);
				break;
			}
			//return true to continue visiting children.
			return true;
		}
	}

	class CompileResourceVisitor implements IResourceVisitor {
		@Override
		public boolean visit(IResource resource) {
			compileProto(resource);
			//return true to continue visiting children.
			return true;
		}
	}
	
	class CleanResourceVisitor implements IResourceVisitor {
		@Override
		public boolean visit(IResource resource) {
			if (resource instanceof IFile && resource.getName().endsWith(PROTO_FILE_EXTENSION)) {
				IFile file = (IFile) resource;
				deleteMarkers(file);
				IFile generatedJavaFile = ProtoBufCompiler.getGeneratedJavaFile(file);
				if (generatedJavaFile != null && generatedJavaFile.isDerived()) {
					try {
						generatedJavaFile.delete(true, false, null);
					} catch (CoreException ce) {
					}
				}
			}
			//return true to continue visiting children.
			return true;
		}
	}

	public static final String BUILDER_ID = "com.googlecode.protoclipse.protobufBuilder";

	private static final String MARKER_TYPE = "com.googlecode.protoclipse.protoBufProblem";

	private void addMarker(IFile file, String message, int lineNumber,
			int charStart, int severity) {
		try {
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e) {
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@SuppressWarnings("unchecked")
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}
	
	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		super.clean(monitor);
		try {
			getProject().accept(new CleanResourceVisitor());
		} catch (CoreException ce) {
		}
	}

	class SampleProgressMonitor extends NullProgressMonitor {
		
		private final IFile file;
		
		SampleProgressMonitor(IFile file) {
			this.file = file;
		}

		@Override
		public void done() {
			super.done();
			IFile generatedJavaFile = ProtoBufCompiler.getGeneratedJavaFile(this.file);
			try {
				generatedJavaFile.setDerived(true);
			} catch (CoreException ce) {
			}
			open(generatedJavaFile);
		}

		private void open(IFile generatedJavaFile) {
			if (generatedJavaFile != null) {
				IJavaElement generatedJavaElement = JavaCore.create(generatedJavaFile);
				if (generatedJavaElement != null) {
					ICompilationUnit generatedCompilationUnit = (ICompilationUnit)generatedJavaElement;
					if (generatedCompilationUnit != null) {
						try {
							generatedCompilationUnit.open(null);
						} catch (JavaModelException ce) {
						}
					}
				}
			}
		}
		
	}

	void compileProto(IResource resource) {
		if (resource instanceof IFile && resource.getName().endsWith(PROTO_FILE_EXTENSION)) {
			IFile file = (IFile) resource;
			deleteMarkers(file);
			ProtoBufCompiler compiler = new ProtoBufCompiler(getCompilerPath());
			try {
				List<CompileError> errors = compiler.compile(file);
				if (errors != null && !errors.isEmpty()) {
					reportErrors(file, errors);
				} else {
					// Ensure the generated java file becomes part of the project
					try {
						resource.getProject().refreshLocal(IResource.DEPTH_INFINITE, new SampleProgressMonitor(file));
					} catch (CoreException ce) {
					}
				}
			} catch (IOException e) {
				addMarker(file, e.getLocalizedMessage(), 0, 0, IMarker.SEVERITY_ERROR);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void reportErrors(IFile file, List<CompileError> errors) {
		Iterator<CompileError> iter = errors.iterator();
		while (iter.hasNext()) {
			CompileError error = iter.next();
			addMarker(file, error.getMessage(), error.getLineNumber(),
					error.getCharacterStart(), IMarker.SEVERITY_ERROR);
		}
	}

	private IPath getCompilerPath() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		return new Path(store.getString(PreferenceConstants.P_PROTOC_PATH));
	}

	private void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}

	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException {
		try {
			getProject().accept(new CompileResourceVisitor());
		} catch (CoreException e) {
		}
	}

	protected void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		delta.accept(new SampleDeltaVisitor());
	}
}
