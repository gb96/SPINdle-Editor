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

import com.app.utils.ComparableEntry;

import spindle.core.dom.DomConst;
import spindle.core.dom.LiteralVariable;
import spindle.io.ParserException;
import spindle.io.parser.DflTheoryParser2;

public class LiteralVariableDialog extends SimpleDialog {

	private Text txtLiteralVariable = null;
	private LiteralVariable literalVariableName = null;
	private LiteralVariable literalVariableValue = null;

	public LiteralVariableDialog(Shell parent, String title, LiteralVariable literalVariableName, LiteralVariable literalVariableValue) {
		super(parent, title);
		this.literalVariableName = literalVariableName;
		this.literalVariableValue = literalVariableValue;
		if (this.literalVariableName == null && this.literalVariableValue != null) throw new IllegalArgumentException(
				"literal variable name cannot be null");
		if (this.literalVariableName != null && this.literalVariableValue == null) throw new IllegalArgumentException(
				"literal variable value cannot be null");
	}

	@Override
	protected void createDialogContent(Composite parent) {
		GridLayout layout = (GridLayout) parent.getLayout();
		layout.numColumns = 2;

		Label lblSuperiority = new Label(parent, SWT.NONE);
		lblSuperiority.setText("Literal variable");

		txtLiteralVariable = new Text(parent, SWT.SINGLE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		txtLiteralVariable.setLayoutData(data);
		if (null != literalVariableName) {
			txtLiteralVariable.setText(literalVariableName.toString() + DomConst.Literal.THEORY_EQUAL_SIGN
					+ literalVariableValue.toString());
		} else {
			txtLiteralVariable.setText("");
		}
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.OK_ID == buttonId) {
			try {
				String literalVariableStr = txtLiteralVariable.getText().trim();
				int l = literalVariableStr.indexOf("" + DomConst.Literal.THEORY_EQUAL_SIGN);
				if (l > 0) {
					String n = literalVariableStr.substring(0, l);
					String v = literalVariableStr.substring(l + 1);
					literalVariableName = DflTheoryParser2.extractLiteralVariable(n);
					literalVariableValue = DflTheoryParser2.extractLiteralVariable(v);
				} else {
					literalVariableName=null;
				}
				super.buttonPressed(buttonId);
			} catch (ParserException e) {
				literalVariableName = null;
				String message = "literal variable string cannot be interpreted!";
				onDialogException(message);
//			} catch (ComponentMismatchException e) {
//				literalVariableName = null;
//				String message = "literal variable string cannot be interpreted!";
//				onDialogException(message);
			}
		} else super.buttonPressed(buttonId);
	}

	@Override
	public Object getRule() {
		return (null == literalVariableName) ? null
				: new ComparableEntry<LiteralVariable, LiteralVariable>(literalVariableName, literalVariableValue);
	}

}
