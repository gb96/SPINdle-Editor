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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import spindle.core.dom.Superiority;
import spindle.io.ParserException;
import spindle.io.parser.DflTheoryParser2;

public class SuperiorityDialog extends SimpleDialog {

	private Text txtSuperiority = null;
	private Superiority superiority = null;

	public SuperiorityDialog(final Shell parent, final String title, final Superiority superiority) {
		super(parent, title);
		this.superiority = superiority;
	}

	@Override
	protected void createDialogContent(Composite parent) {
		GridLayout layout = (GridLayout) parent.getLayout();
		layout.numColumns = 2;

		Label lblSuperiority = new Label(parent, SWT.NONE);
		lblSuperiority.setText("Superiority");

		txtSuperiority = new Text(parent, SWT.SINGLE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		txtSuperiority.setLayoutData(data);
		txtSuperiority.setText((null == superiority) ? "" : superiority.toString());
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.OK_ID == buttonId) {
			try {
				String supStr = txtSuperiority.getText().trim();
				Superiority newSuperiority = ("".equals(supStr)) ? null : DflTheoryParser2.extractSuperiorityStr(supStr);
				superiority = newSuperiority;
				super.buttonPressed(buttonId);
			} catch (ParserException e) {
				superiority = null;
				String message = "Superiority string cannot be interpreted!";
				onDialogException(message);
			}
		} else super.buttonPressed(buttonId);
	}

	@Override
	public Object getRule() {
		return superiority;
	}
}
