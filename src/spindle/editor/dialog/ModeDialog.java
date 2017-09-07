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

import spindle.core.dom.RuleType;
import spindle.io.ParserException;
import spindle.io.outputter.DflTheoryConst;

public class ModeDialog extends SimpleDialog {
	private Text txtModeRule = null;
	private String modeRule = null;

	public ModeDialog(final Shell parent, final String title, final String modeRule) {
		super(parent, title);
		this.modeRule = (null == modeRule) ? "" : modeRule.trim();
	}

	@Override
	protected void createDialogContent(Composite parent) {
		GridLayout layout = (GridLayout) parent.getLayout();
		layout.numColumns = 2;

		Label lblMode = new Label(parent, SWT.NONE);
		lblMode.setText("Mode rule");

		txtModeRule = new Text(parent, SWT.SINGLE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		txtModeRule.setLayoutData(data);
		txtModeRule.setText(modeRule);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.OK_ID == buttonId) {
			try {
				String modeRuleStr = txtModeRule.getText().trim();
				verifyModeRule(modeRuleStr);
				modeRule = "".equals(modeRuleStr) ? null : modeRuleStr.trim().toUpperCase();
				super.buttonPressed(buttonId);
			} catch (Exception e) {
				modeRule = null;
				String message = "Mode rule cannot be interpreted!";
				onDialogException(message);
			}
		} else super.buttonPressed(buttonId);
	}

	@Override
	public Object getRule() {
		return modeRule;
	}

	private void verifyModeRule(final String modeRule) throws ParserException {
		if ("".equals(modeRule)) return;

		int l = -1;
		RuleType ruleType = null;

		if ((l = modeRule.indexOf(DflTheoryConst.SYMBOL_MODE_CONVERSION)) > 0) {
			ruleType = RuleType.MODE_CONVERSION;
		} else if ((l = modeRule.indexOf(DflTheoryConst.SYMBOL_MODE_CONFLICT)) > 0) {
			ruleType = RuleType.MODE_CONFLICT;
		} else throw new ParserException("Rule mode cannot be interpreted!");

		try {
			String[] modes = modeRule.substring(l + DflTheoryConst.SYMBOL_MODE_CONVERSION.length()).split(""+DflTheoryConst.LITERAL_SEPARATOR);
			for (String mode : modes) {
				if (!"".equals(mode.trim())) return;
			}
			throw new ParserException("no " + ruleType.getLabel().toLowerCase() + " mode found!");
		} catch (ParserException e) {
			throw e;
		} catch (Exception e) {
			throw new ParserException(e.getMessage());
		}
	}
}
