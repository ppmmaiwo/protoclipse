package com.googlecode.protoclipse.preferences;

import java.io.File;
import java.io.FileFilter;


import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.googlecode.protoclipse.Activator;
import com.googlecode.protoclipse.compiler.ProtoBufCompiler;


/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class ProtoBufPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	private DirectoryFieldEditor protocPathEditor;

	public ProtoBufPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Google Protocol Buffers Compiler Plug-in Settings");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		protocPathEditor = new DirectoryFieldEditor(PreferenceConstants.P_PROTOC_PATH, 
				"&Compiler Path:", getFieldEditorParent());
		protocPathEditor.setValidateStrategy(DirectoryFieldEditor.VALIDATE_ON_KEY_STROKE);
		addField(protocPathEditor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	private boolean isValidProtocPath(String stringValue) {
		File path = new File(stringValue);
		if (!path.exists() || !path.isDirectory()) {
			return false;
		}
		File[] files = path.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				if (file.getName().startsWith(ProtoBufCompiler.PROTOC) && file.canExecute()) {
					return true;
				}
				return false;
			}
		});
		if (files.length < 1) {
			return false;
		}
		return true;
	}

	@Override
	protected void checkState() {
		super.checkState();
		String stringValue = protocPathEditor.getStringValue();
		if (!isValidProtocPath(stringValue)) {
			final String msg = "Could not find the Google ProtoBuf compiler executable";
			setErrorMessage(msg);
			setValid(false);
		}
		else {
			setErrorMessage(null);
			setValid(true);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event.getProperty().equals(FieldEditor.VALUE)) {
			checkState();
		}
	}
	
}
