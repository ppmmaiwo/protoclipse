package com.googlecode.protoclipse.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

import com.googlecode.protoclipse.Messages;
import com.googlecode.protoclipse.builder.ProtoBufNature;

public class ToggleNatureContributionItem extends CompoundContributionItem {

	@Override
	protected IContributionItem[] getContributionItems() {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		final CommandContributionItemParameter contributionParameter = new CommandContributionItemParameter(
				activeWorkbenchWindow,
				"com.googlecode.protoclipse.commands.toggleNatureContributionItem", //$NON-NLS-1$
				"com.googlecode.protoclipse.commands.toggleNature", //$NON-NLS-1$
				SWT.NONE);
		contributionParameter.label = getContributionItemLabel(activeWorkbenchWindow
				.getSelectionService().getSelection());
		return new IContributionItem[] { new CommandContributionItem(
				contributionParameter) };
	}

	private String getContributionItemLabel(ISelection selection) {
		String label = Messages.getString("ToggleNatureContributionItem.toggle"); //$NON-NLS-1$
		IProject[] projects = getSelectedProjects(selection);
		int projectsWithNature = countProjectsWithNature(projects);
		if (projectsWithNature == projects.length) {
			label = Messages.getString("ToggleNatureContributionItem.remove"); //$NON-NLS-1$
		}
		else if (projectsWithNature == 0) {
			label = Messages.getString("ToggleNatureContributionItem.add"); //$NON-NLS-1$
		}
		return label;
	}

	private int countProjectsWithNature(IProject[] projects) {
		int projectsWithNature = 0;
		if (projects != null) {
			for (int i = 0; i < projects.length; ++i) {
				if (ProtoBufNature.hasNature(projects[i])) {
					projectsWithNature++;
				}
			}
		}
		return projectsWithNature;
	}
	
	private IProject[] getSelectedProjects(ISelection selection) {
		List<IProject> projects = new ArrayList<IProject>();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			for (Iterator<?> it = structuredSelection.iterator(); it.hasNext();) {
				Object element = it.next();
				if (element instanceof IProject) {
					projects.add((IProject) element);
				} else if (element instanceof IAdaptable) {
					IProject project = (IProject) ((IAdaptable) element)
							.getAdapter(IProject.class);
					if (project != null) {
						projects.add(project);
					}
				}
			}
		}
		if (projects.isEmpty()) {
			return null;
		}
		else {
			IProject[] array = new IProject[projects.size()];
			return projects.toArray(array);
		}
	}
}
