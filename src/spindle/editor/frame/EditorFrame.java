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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;

import spindle.core.dom.Theory;
import spindle.sys.Conf;
import spindle.sys.EditorConf;
import spindle.sys.EditorConst;
import spindle.sys.ConfTag;

public class EditorFrame extends EditorFrameCore {

	private static EditorFrame APP = null;

	public static final EditorFrame getApp() {
		if (null == APP) throw new RuntimeException("APP is null");
		return APP;
	}

	public EditorFrame(Shell parent, Map<String, String> userConfigs) {
		super(parent);
		APP = this;

		Map<String, String> config = new HashMap<String, String>();
		config.put(ConfTag.IS_SHOW_RESULT, "false");
		config.put(ConfTag.IS_SHOW_PROGRESS, "false");
		config.put(ConfTag.IS_CONSOLE_MODE, "true");
		if (null != userConfigs) config.putAll(userConfigs);
		Conf.initializeApplicationContext(config);

		isWellFoundedSemantics.setChecked(EditorConf.isReasoningWithWellFoundedSemantics());
		isAmbiguityPropagation.setChecked(EditorConf.isReasoningWithAmbiguityPropagation());
	}

	public boolean close() {
		Conf.terminateApplicationContext();
		return super.close();
	}

	public void currentTheoryFrameModified() {
		TheoryFrame theoryFrame = (TheoryFrame) desktop.getSelection();
		if (desktop.getChildren().length == 0 || null == theoryFrame) {
			transformToRegularForm.setEnabled(false);
			transformRemoveSuperiorities.setEnabled(false);
			transformRemoveDefeaters.setEnabled(false);
			getConclusions.setEnabled(false);
			printTheory.setEnabled(false);
			printConclusions.setEnabled(false);
			saveTheory.setEnabled(false);
		} else {
			Theory theory = theoryFrame.getTheory();

			transformToRegularForm.setEnabled(!theory.isEmpty());
			switch (Conf.getReasonerVersion()) {
			case 2:
				transformRemoveSuperiorities.setEnabled(false);
				if (!theory.isEmpty() && theory.getDefeatersCount() == 0) {
					getConclusions.setEnabled(true);
				} else {
					getConclusions.setEnabled(false);
				}
				break;
			case 1:
				transformRemoveSuperiorities.setEnabled(theory.getSuperiorityCount() > 0);
				if (!theory.isEmpty()) {
					if (EditorConst.isDeploy) {
						getConclusions.setEnabled(true);
					} else {
						if (theory.getSuperiorityCount() == 0 //
								&& theory.getDefeatersCount() == 0) {
							getConclusions.setEnabled(true);
						} else {
							getConclusions.setEnabled(false);
						}
					}
				} else {
					getConclusions.setEnabled(false);
				}
				break;
			}
			transformRemoveDefeaters.setEnabled(theory.getDefeatersCount() > 0);
			// getConclusions.setEnabled(theory.isEmpty());

			printTheory.setEnabled(!theory.isEmpty());
			printConclusions.setEnabled(theoryFrame.getConclusionsCount() > 0);
			saveTheory.setEnabled(!theory.isEmpty());
		}
	}
}
