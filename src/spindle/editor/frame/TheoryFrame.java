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
package spindle.editor.frame;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.TreeItem;

import spindle.core.dom.LiteralVariable;
import spindle.core.dom.Rule;
import spindle.core.dom.RuleType;
import spindle.core.dom.Superiority;
import spindle.core.dom.TheoryException;
import spindle.exception.EditorException;
import spindle.io.outputter.DflTheoryConst;
import spindle.sys.ConfigurationException;

public class TheoryFrame extends TheoryFrameCore {

	public TheoryFrame(final String id, final String label,//
			final CTabFolder parent) throws ConfigurationException {
		super(id, label, parent, SWT.NONE);

		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent event) {
				EditorFrame.getApp().getScreenManager().closeTheory(id);
			}
		});
	}

	@Override
	public void add(LiteralVariable literalVariableName, LiteralVariable literalVariableValue) throws EditorException {
		// System.out.println("addLiteralVariable:-"+literalVariableName+":"+literalVariableValue);
		if (literalVariableName == null) throw new EditorException("literal variable name cannot be null");
		if (literalVariableValue == null) throw new EditorException("literal variable value cannot be null");
		try {
			theory.addLiteralVariable(literalVariableName, literalVariableValue);
			addTheoryTreeItem(literalVariableName, literalVariableValue);
			literalVariables.put(literalVariableName, literalVariableValue);
		} catch (TheoryException e) {
			e.printStackTrace();
			throw new EditorException(e);
		}
	}

	@Override
	public void remove(LiteralVariable literalVariableName) throws EditorException {
		if (literalVariableName == null) throw new EditorException("literal variable name cannot be null");
		try {
			theory.removeLiteralVariable(literalVariableName);
			TreeItem item = theoryTreeItems.get(literalVariableName.toString());
			if (null == item) throw new EditorException("Tree item [" + literalVariableName.toString() + "] not found");
			item.dispose();
			literalVariables.remove(literalVariableName);
		} catch (TheoryException e) {
			e.printStackTrace();
			throw new EditorException(e);
		}
	}

	@Override
	public void add(Rule rule) throws EditorException {
		if (null == rule) throw new EditorException("superiority is null");
		try {
			theory.addRule(rule);
			addTheoryTreeItem(rule);
		} catch (Exception e) {
			throw new EditorException(e);
		}
	}

	@Override
	public void remove(final Rule rule) throws EditorException {
		try {
			theory.removeRule(rule.getLabel());
			TreeItem item = theoryTreeItems.get(rule.getLabel());
			if (null == item) throw new EditorException("Tree item [" + rule.getLabel() + "] not found");
			item.dispose();
		} catch (Exception e) {
			throw new EditorException(e);
		}
	}

	@Override
	public void add(Superiority superiority) throws EditorException {
		try {
			if (null == superiority) throw new EditorException("superiority is null");
			theory.add(superiority);
			addTheoryTreeItem(superiority);
		} catch (Exception e) {
			throw new EditorException(e);
		}
	}

	@Override
	public void remove(Superiority superiority) throws EditorException {
		try {
			theory.remove(superiority);
			TreeItem item = theoryTreeItems.get(superiority.toString());
			if (null == item) throw new EditorException("Tree item [" + superiority.toString() + "] not found");
			item.dispose();
		} catch (Exception e) {
			throw new EditorException(e);
		}
	}

	@Override
	public void add(final String modeRule) throws EditorException {
		if (null == modeRule || "".equals(modeRule.trim())) onTheoryFrameException(new EditorException("mode rule is null"));
		// System.out.println("addModeRule.modeRule=" + modeRule);

		int l = -1;
		RuleType ruleType = null;
		List<String> modesList = new ArrayList<String>();
		if ((l = modeRule.indexOf(DflTheoryConst.SYMBOL_MODE_CONVERSION)) > 0) {
			ruleType = RuleType.MODE_CONVERSION;
			String origMode = modeRule.substring(0, l);
			for (String mode : modeRule.substring(l + DflTheoryConst.SYMBOL_MODE_CONVERSION.length()).split(
					"" + DflTheoryConst.LITERAL_SEPARATOR)) {
				if (!"".equals(mode.trim())) modesList.add(mode);
			}
			if (modesList.size() > 0) {
				String[] m = new String[modesList.size()];
				modesList.toArray(m);
				theory.addModeConversionRules(origMode, m);

				TreeItem treeItem = conversionModeTreeItems.remove(origMode);
				if (null != treeItem) treeItem.dispose();

				Set<String> convertModes = theory.getModeConversionRules(origMode);
				addTheoryTreeItem(ruleType, origMode, convertModes.toString());
			} else throw new EditorException("no conversion mode found!");
		} else if ((l = modeRule.indexOf(DflTheoryConst.SYMBOL_MODE_CONFLICT)) > 0) {
			ruleType = RuleType.MODE_CONFLICT;
			String origMode = modeRule.substring(0, l);
			for (String mode : modeRule.substring(l + DflTheoryConst.SYMBOL_MODE_CONVERSION.length()).split(
					"" + DflTheoryConst.LITERAL_SEPARATOR)) {
				if (!"".equals(mode.trim())) modesList.add(mode);
			}
			if (modesList.size() > 0) {
				String[] m = new String[modesList.size()];
				modesList.toArray(m);
				theory.addModeConflictRules(origMode, m);

				TreeItem treeItem = conflictModeTreeItems.remove(origMode);
				if (null != treeItem) treeItem.dispose();

				Set<String> conflictModes = theory.getModeConflictRules(origMode);
				addTheoryTreeItem(ruleType, origMode, conflictModes.toString());
			} else throw new EditorException("no conflict mode found!");
		} else throw new EditorException("Rule mode cannot be interpreted!");
	}

	@Override
	public void remove(final String modeRule) throws EditorException {
		// System.out.println("removeModeRule.modeRule=" + modeRule);

		int l = -1;
		List<String> modesList = new ArrayList<String>();
		if ((l = modeRule.indexOf(DflTheoryConst.SYMBOL_MODE_CONVERSION)) > 0) {
			// System.out.println("removeModeRule.conversionModeTreeItems=" +
			// conversionModeTreeItems.keySet());

			String origMode = modeRule.substring(0, l);
			for (String mode : modeRule.substring(l + DflTheoryConst.SYMBOL_MODE_CONVERSION.length()).split(
					"" + DflTheoryConst.LITERAL_SEPARATOR)) {
				if (!"".equals(mode.trim())) modesList.add(mode);
			}
			if (modesList.size() > 0) {
				try {
					for (String mode : modesList) {
						theory.removeModeConversionRule(origMode, mode);
					}

					TreeItem item = conversionModeTreeItems.remove(origMode);
					if (null == item) throw new EditorException("Tree item [" + modeRule + "] not found");
					item.dispose();

					Set<String> modes = theory.getModeConversionRules(origMode);
					if (null != modes && modes.size() > 0) addTheoryTreeItem(RuleType.MODE_CONVERSION, origMode, modes.toString());
				} catch (Exception e) {
					throw new EditorException(e);
				}
			} else throw new EditorException("no conversion mode found!");
		} else if ((l = modeRule.indexOf(DflTheoryConst.SYMBOL_MODE_CONFLICT)) > 0) {
			// System.out.println("removeModeRule.conflictModeTreeItems=" + conflictModeTreeItems.keySet());

			String origMode = modeRule.substring(0, l);
			for (String mode : modeRule.substring(l + DflTheoryConst.SYMBOL_MODE_CONVERSION.length()).split(
					"" + DflTheoryConst.LITERAL_SEPARATOR)) {
				if (!"".equals(mode.trim())) modesList.add(mode);
			}
			if (modesList.size() > 0) {
				try {
					for (String mode : modesList) {
						theory.removeModeConflictRule(origMode, mode);
					}

					TreeItem item = conflictModeTreeItems.remove(origMode);
					if (null == item) throw new EditorException("Tree item [" + modeRule + "] not found");
					item.dispose();

					Set<String> modes = theory.getModeConflictRules(origMode);
					if (null != modes && modes.size() > 0) addTheoryTreeItem(RuleType.MODE_CONFLICT, origMode, modes.toString());
				} catch (Exception e) {
					throw new EditorException(e);
				}
			} else throw new EditorException("no conflict mode found!");
		} else throw new EditorException("Rule mode cannot be interpreted!");
	}

}
