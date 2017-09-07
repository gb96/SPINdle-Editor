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

import spindle.core.dom.Rule;
import spindle.io.outputter.DflTheoryConst;
import spindle.io.parser.DflTheoryParser2;

public class RuleDialog extends SimpleDialog {

	private Text txtRuleId = null;
	private Text txtRule = null;

	private Rule rule = null;

	public RuleDialog(final Shell parent, final String title, final Rule rule) {
		super(parent, title);
		this.rule = rule;
	}

	@Override
	protected void createDialogContent(Composite parent) {
		GridLayout layout = (GridLayout) parent.getLayout();
		layout.numColumns = 2;

		Label lblRuleId = new Label(parent, SWT.NONE);
		lblRuleId.setText("Rule id");

		txtRuleId = new Text(parent, SWT.SINGLE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		txtRuleId.setLayoutData(data);
		txtRuleId.setText((null == rule) ? "" : rule.getLabel());

		Label lblRule = new Label(parent, SWT.NONE);
		data = new GridData(SWT.BOLD);
		lblRule.setLayoutData(data);
		lblRule.setText("Rule");

		txtRule = new Text(parent, SWT.SINGLE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		txtRule.setLayoutData(data);
		txtRule.setText((null == rule) ? "" : rule.getRuleAsString());
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.OK_ID == buttonId) {
			String ruleId = txtRuleId.getText().trim();
			String ruleString = txtRule.getText().trim();

			if ("".equals(ruleId) && "".equals(ruleString)) {
				rule = null;
			} else {
				String r = ruleId + DflTheoryConst.RULE_LABEL_SEPARATOR + ruleString;
				try {
					Rule newRule = DflTheoryParser2.extractRuleStr(r);
					rule = newRule;
					super.buttonPressed(buttonId);
				} catch (Exception e) {
					String message = "Rule string cannot be interpreted!";
					onDialogException(message);
				}
			}
		} else super.buttonPressed(buttonId);
	}

	@Override
	public Object getRule() {
		return rule;
	}
}
