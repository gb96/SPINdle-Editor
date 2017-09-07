/**
 * SPINdle Defeasible Theory Editor (version 2.2.2)
 * Copyright (C) 2009-2013 NICTA Ltd.
 *
 * This file is part of SPINdle Defeasible Theory Editor project.
 * 
 * SPINdle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * SPINdle is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with SPINdle.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author H.-P. Lam (oleklam@gmail.com), National ICT Australia - Queensland Research Laboratory 
 */
package spindle.editor.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import spindle.editor.frame.ScreenManager;
import spindle.io.ParserException;

public abstract class SimpleDialog extends Dialog {
	private String title = null;
	private Composite contentWrapper = null;

	public SimpleDialog(Shell parent, String title) {
		super(parent);
		this.title = (null == title) ? "" : title.trim();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		contentWrapper = (Composite) super.createDialogArea(parent);
		setTitle(title);
		GridLayout layout = (GridLayout) contentWrapper.getLayout();
		layout.marginWidth = 10;
		layout.marginHeight = 10;

		createDialogContent(contentWrapper);
		return contentWrapper;
	}

	public void setTitle(final String title) {
		this.title = (null == title) ? "" : title.trim();
		if (null != contentWrapper) contentWrapper.getShell().setText(this.title);
	}

	public String getTitle() {
		return this.title;
	}

	protected void onDialogException(final String message) {
		ScreenManager.getApp().onTheoryException("", new ParserException(message));
		MessageBox messageBox = new MessageBox(getShell(), SWT.OK | SWT.ICON_ERROR);
		messageBox.setText(getTitle());
		messageBox.setMessage(message);
		messageBox.open();
	}

	protected abstract void createDialogContent(Composite parent);

	public abstract Object getRule();
}
