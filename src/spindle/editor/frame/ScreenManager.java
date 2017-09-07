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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.app.utils.Converter;
import com.app.utils.ComparableEntry;
import com.app.utils.FileManager;
import com.app.utils.TextUtilities;
import com.app.utils.Utilities;

import spindle.Reasoner;
import spindle.core.dom.Conclusion;
import spindle.core.dom.LiteralVariable;
import spindle.core.dom.Rule;
import spindle.core.dom.RuleType;
import spindle.core.dom.Superiority;
import spindle.core.dom.Theory;
import spindle.editor.action.impl.*;
import spindle.editor.dialog.*;
import spindle.editor.panel.ConsoleMessagePanel;
import spindle.editor.panel.GenericMessagePanel;
import spindle.editor.panel.LogMessagePanel;
import spindle.engine.ReasoningEngineFactory;
import spindle.engine.TheoryNormalizer;
import spindle.io.IOManager;
import spindle.listener.TheoryFrameListener;
import spindle.sys.Conf;
import spindle.sys.EditorConst;
import spindle.sys.PrintThread;
//import spindle.tools.explanation.ExplanationLogger;

public class ScreenManager implements TheoryFrameListener {

	private static ScreenManager APP = null;

	public static final ScreenManager getApp() {
		if (null == APP) throw new RuntimeException("APP is null");
		return APP;
	}

	private Shell shell = null;
	private CTabFolder desktop = null;
	private CTabFolder messagePanel = null;

	private GenericMessagePanel logPanel = null;
	private GenericMessagePanel consolePanel = null;

	private Map<String, TheoryFrame> theories = null;
	private int theoryCounter = 0;

//	private ExplanationLogger explanationLogger = null;

	public ScreenManager(final Shell shell, final CTabFolder desktop, final CTabFolder messagePanel) {
		if (null == desktop) throw new IllegalArgumentException("desktop is null");
		if (null == messagePanel) throw new IllegalArgumentException("message panel is null");

		theories = new HashMap<String, TheoryFrame>();

		this.shell = shell;
		this.desktop = desktop;
		this.messagePanel = messagePanel;

		desktop.setMaximizeVisible(false);
		desktop.setMinimizeVisible(false);
		desktop.setSimple(false);
		desktop.setLayout(new FillLayout());

		logPanel = new LogMessagePanel(messagePanel);
		consolePanel = new ConsoleMessagePanel(messagePanel);

		this.messagePanel.setSelection(consolePanel);
		APP = this;

	}

	public void setAmbiguityPropagation(final boolean isAmbiguityPropagation) {
		Conf.setReasoningWithAmbiguityPropagation(isAmbiguityPropagation);
		onLogEditorCommand("Set preference", "Reasoning with AP=" + Conf.isReasoningWithAmbiguityPropagation());
	}

	public void setWellFoundedSemantics(final boolean isWellFoundedSemantics) {
		Conf.setReasoningWithWellFoundedSemantics(isWellFoundedSemantics);
		onLogEditorCommand("Set preference", "Reasoning with WF=" + Conf.isReasoningWithWellFoundedSemantics());
	}

	public void newTheory() {
		String theoryId = null;
		String caption = EditorConst.NEW_THEORY_CAPTION + (++theoryCounter);
		do {
			theoryId = EditorConst.NEW_THEORY_ID_PREFIX + Utilities.getRandomString(EditorConst.NEW_THEORY_RANDOM_KEY_LENGTH);
		} while (theories.containsKey(theoryId));
		try {
			TheoryFrame theoryFrame = new TheoryFrame(theoryId, caption, desktop);
			theoryFrame.addTheoryFrameListener(this);
			theoryFrame.setTheory(new Theory());
			theories.put(theoryId, theoryFrame);
			desktop.setSelection(theoryFrame);

			onLogEditorCommand("New theory", "");
			EditorFrame.getApp().currentTheoryFrameModified();
		} catch (Exception e) {
			onLogEditorCommand("New theory", "failed");
			onTheoryException(caption, e);
		}
	}

	public void loadTheory() {
		File filename = getFilename("Load theory", SWT.OPEN, null);
		if (null == filename) return;
		String theoryId = filename.toString();
		String caption = filename.getName();
		if (theories.containsKey(theoryId)) {
			MessageDialog.openInformation(shell, "Theory already loaded", "Theory [" + filename + "] already loaded!");
		} else {
			try {
				Theory theory = IOManager.getTheory(filename, null);
				TheoryFrame theoryFrame = new TheoryFrame(theoryId, caption, desktop);
				theoryFrame.addTheoryFrameListener(this);
				theoryFrame.setTheory(theory);
				theoryFrame.setTheoryFilename(filename);
				theories.put(theoryId, theoryFrame);
				desktop.setSelection(theoryFrame);
				onLogEditorCommand("Load theory", caption);
				EditorFrame.getApp().currentTheoryFrameModified();
			} catch (Exception e) {
				onLogEditorCommand("Load theory", caption + " - failed");
				onTheoryException(caption, e);
			}
		}
	}

	public void groundLiteralVariables() {
		TheoryFrame theoryFrame = (TheoryFrame) desktop.getSelection();
		try {
			Theory theory = theoryFrame.getTheory().clone();
			if (theory.getLiteralVariablesInRulesCount() > 0 || theory.getLiteralBooleanFunctionCount() > 0) {
//				theory = ReasoningEngineFactory.getLiteralVariablesEvaluator().evaluateLiteralVariables(theory, getExplanationLogger());
				theory = ReasoningEngineFactory.getLiteralVariablesEvaluator().evaluateLiteralVariables(theory);
				theoryFrame.setTheory(theory);
			}
			onLogEditorCommand("Remove literal variable from theory", theoryFrame.getText());
			EditorFrame.getApp().currentTheoryFrameModified();
		} catch (Exception e) {
			onLogEditorCommand("Remove literal variable from theory", theoryFrame.getText() + " - failed");
			onTheoryException("remove literal variables from theory", e);
		}
	}

	public void transformTheoryToRegularForm() {
		TheoryFrame theoryFrame = (TheoryFrame) desktop.getSelection();
		try {
			Theory theory = theoryFrame.getTheory().clone();
			TheoryNormalizer theoryNormalizer = ReasoningEngineFactory.getTheoryNormalizer(theory.getTheoryType());
			theoryNormalizer.setTheory(theory);
			theoryNormalizer.transformTheoryToRegularForm();
			theory = theoryNormalizer.getTheory();
			theoryFrame.setTheory(theory);
			onLogEditorCommand("Transform theory", theoryFrame.getText() + " - regular form");
			EditorFrame.getApp().currentTheoryFrameModified();
		} catch (Exception e) {
			onLogEditorCommand("Transform theory", theoryFrame.getText() + " - regular form - failed");
			onTheoryException("transform theory to regular form", e);
		}
	}

	public void transformRemoveSuperiority() {
		TheoryFrame theoryFrame = (TheoryFrame) desktop.getSelection();
		try {
			Theory theory = theoryFrame.getTheory().clone();
			if (theory.getSuperiorityCount() == 0) {
				onLogEditorCommand("Transform theory", theoryFrame.getText() + " - remove superiority - no superiority found");
			} else {
				TheoryNormalizer theoryNormalizer = ReasoningEngineFactory.getTheoryNormalizer(theory.getTheoryType());
				theoryNormalizer.setTheory(theory);
				theoryNormalizer.removeSuperiority();
				theory = theoryNormalizer.getTheory();
				theoryFrame.setTheory(theory);
				onLogEditorCommand("Transform theory", theoryFrame.getText() + " - remove superiority");
				EditorFrame.getApp().currentTheoryFrameModified();
			}
		} catch (Exception e) {
			onLogEditorCommand("Transform theory", theoryFrame.getText() + " - remove superiority - failed");
			onTheoryException("transform theory to regular form", e);
		}
	}

	public void transformRemoveDefeater() {
		TheoryFrame theoryFrame = (TheoryFrame) desktop.getSelection();
		try {
			Theory theory = theoryFrame.getTheory().clone();
			if (theory.getDefeatersCount() == 0) {
				onLogEditorCommand("Transform theory", theoryFrame.getText() + " - remove defeaters - no superiority found");
			} else {
				TheoryNormalizer theoryNormalizer = ReasoningEngineFactory.getTheoryNormalizer(theory.getTheoryType());
				theoryNormalizer.setTheory(theory);
				theoryNormalizer.removeDefeater();
				theory = theoryNormalizer.getTheory();
				theoryFrame.setTheory(theory);
				onLogEditorCommand("Transform theory", theoryFrame.getText() + " - remove defeaters");
				EditorFrame.getApp().currentTheoryFrameModified();
			}
		} catch (Exception e) {
			onLogEditorCommand("Transform theory", theoryFrame.getText() + " - remove defeaters - failed");
			onTheoryException("transform theory to regular form", e);
		}
	}

	public void generateConclusions() {
		TheoryFrame theoryFrame = (TheoryFrame) desktop.getSelection();
		try {
			Theory theory = theoryFrame.getTheory().clone();
			if (theory.isEmpty()) {
				onLogEditorCommand("Generate conclusions", theoryFrame.getId() + " - failed - theory is empty");
			} else {
				long startTime, endTime;
				Reasoner reasoner = new Reasoner();
				reasoner.loadTheory(theory);
				startTime = System.currentTimeMillis();
				reasoner.getConclusions();
				endTime = System.currentTimeMillis();

				List<Conclusion> conclusions = reasoner.getConclusionsAsList();
				theoryFrame.setConclusions(conclusions);
				StringBuilder sb = new StringBuilder(TextUtilities.repeatStringPattern("-", 30));
				sb.append("\nConclusions\n===========");
				for (Conclusion conclusion : conclusions) {
					sb.append("\n").append(conclusion.toString());
				}
				onConsoleMessage(theoryFrame.getText(), sb.toString());
				onConsoleMessage(theoryFrame.getText(), "time used=" + Converter.long2TimeString(endTime - startTime));

				onLogEditorCommand("Generate conclusions", theoryFrame.getId() + " - success");
				EditorFrame.getApp().currentTheoryFrameModified();
			}
		} catch (Exception e) {
			onLogEditorCommand("Generate conclusions", theoryFrame.getId() + " - failed");
			onTheoryException("generate conclusions", e);
		}
	}

	public void generateConclusionsWithTransformations() {
		TheoryFrame theoryFrame = (TheoryFrame) desktop.getSelection();
		try {
			Theory theory = theoryFrame.getTheory().clone();
			if (theory.isEmpty()) {
				onLogEditorCommand("Generate conclusions", theoryFrame.getId() + " - failed - theory is empty");
			} else {
				long startTime, endTime;
				Reasoner reasoner = new Reasoner();
				reasoner.loadTheory(theory);

				// if (theory.getLiteralVariableCount() > 0 || theory.getLiteralBooleanFunctionCount() > 0) {
				// reasoner.removeLiteralVariables();
				// onConsoleMessage(theoryFrame.getText(), "theory variables removal");
				// }

				reasoner.transformTheoryToRegularForm();
				onConsoleMessage(theoryFrame.getText(), "transform theory to regular form");

				if (theory.getDefeatersCount() > 0) {
					reasoner.removeDefeater();
					onConsoleMessage(theoryFrame.getText(), "defeaters removal");
				}

				switch (Conf.getReasonerVersion()) {
				case 1:
					if (theory.getSuperiorityCount() > 0) reasoner.removeSuperiority();
					onConsoleMessage(theoryFrame.getText(), "superiority relations removal");
					break;
				default:
				}

				startTime = System.currentTimeMillis();
				reasoner.getConclusions();
				endTime = System.currentTimeMillis();

				List<Conclusion> conclusions = reasoner.getConclusionsAsList();
				theoryFrame.setConclusions(conclusions);
				StringBuilder sb = new StringBuilder(TextUtilities.repeatStringPattern("-", 30));
				sb.append("\nConclusions\n===========");
				for (Conclusion conclusion : conclusions) {
					sb.append("\n").append(conclusion.toString());
				}
				onConsoleMessage(theoryFrame.getText(), sb.toString());
				onConsoleMessage(theoryFrame.getText(), "time used=" + Converter.long2TimeString(endTime - startTime));

				onLogEditorCommand("Generate conclusions", theoryFrame.getId() + " - success");
				EditorFrame.getApp().currentTheoryFrameModified();
			}
		} catch (Exception e) {
			onLogEditorCommand("Generate conclusions", theoryFrame.getId() + " - failed");
			onTheoryException("generate conclusions", e);
		}
	}

	public void saveTheory() {
		TheoryFrame theoryFrame = null;
		theoryFrame = (TheoryFrame) desktop.getSelection();
		saveTheory(SaveTheory.LABEL, theoryFrame, theoryFrame.getTheoryFilename());
	}

	public void saveTheoryAs() {
		TheoryFrame theoryFrame = (TheoryFrame) desktop.getSelection();
		saveTheory(SaveTheoryAs.LABEL, theoryFrame, null);
	}

	public void saveConclusions() {
		TheoryFrame theoryFrame = (TheoryFrame) desktop.getSelection();
		saveConclusions(SaveConclusions.LABEL, theoryFrame, theoryFrame.getConclusionsFilename());
	}

	public void saveConclusionsAs() {
		TheoryFrame theoryFrame = (TheoryFrame) desktop.getSelection();
		saveConclusions(SaveConclusionsAs.LABEL, theoryFrame, null);
	}

	private void saveTheory(final String commandName, final TheoryFrame theoryFrame, final File filename) {
		File f = null;
		try {
			f = (null == filename) ? getFilename(commandName, SWT.SAVE, null) : filename;
			if (FileManager.getFileExtension(f).equals("")) f = new File(f.getCanonicalPath() + ".dfl");
			if (null == f) {
				MessageDialog.openInformation(shell, commandName, "Theory save cancelled!");
				onConsoleMessage(theoryFrame.getId(), "Theory save cancelled!");
			} else {
				IOManager.save(f, theoryFrame.getTheory(), null);
				theoryFrame.setTheoryFilename(f);
				onLogEditorCommand("Save theory", f.getName());
			}
		} catch (Exception e) {
			onLogEditorCommand("Save theory", f.getName() + " - failed");
			onTheoryException(theoryFrame.getId(), e);
		}
	}

	private void saveConclusions(final String commandName, final TheoryFrame theoryFrame, final File filename) {
		File f = null;
		try {
			f = (null == filename) ? getFilename(commandName, SWT.SAVE, null) : filename;
			if (null == f) {
				MessageDialog.openInformation(shell, commandName, "Conclusions save cancelled!");
				onConsoleMessage(theoryFrame.getId(), "Conclusions save cancelled!");
			} else {
				IOManager.save(filename, theoryFrame.getConclusions(), null);
				theoryFrame.setConclusionsFilename(f);
				onLogEditorCommand("Save conclusions", f.getName());
			}
		} catch (Exception e) {
			onLogEditorCommand("Save conclusions", f.getName() + " - failed");
			onTheoryException(theoryFrame.getId(), e);
		}
	}

	public void closeCurrentTheory() {
		if (theories.size() == 0) return;
		TheoryFrame theoryFrame = (TheoryFrame) desktop.getSelection();
		String theoryId = (null == theoryFrame) ? null : theoryFrame.getId();
		if (null != theoryId) closeTheory(theoryId);
		EditorFrame.getApp().currentTheoryFrameModified();
	}

	public void closeAllTheories() {
		if (theories.size() == 0) return;
		for (String theoryId : theories.keySet()) {
			closeTheory(theoryId);
		}
	}

	public void closeTheory(final String theoryId) {
		TheoryFrame theoryFrame = theories.remove(theoryId);
		if (null == theoryFrame) return;

		if (theoryFrame.isModified()) {
			MessageBox messageBox = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_INFORMATION);
			messageBox.setText("Close theory");
			messageBox.setMessage("theory is modified!  save it?");
			if (SWT.YES == messageBox.open()) {
				saveTheoryAs();
			}
		}

		theoryFrame.dispose();
		String message = (null == theoryFrame.getTheoryFilename()) ? "" : theoryFrame.getTheoryFilename().getName();
		onLogEditorCommand("Close theory", message);
	}

	public void addLiteralVariable() {
		if (theories.size() == 0) return;
		addRule(RuleType.LITERAL_VARIABLE_SET);
	}

	public void addRule() {
		if (theories.size() == 0) return;
		addRule(RuleType.DEFEASIBLE);
	}

	public void addSuperiority() {
		if (theories.size() == 0) return;
		addRule(RuleType.SUPERIORITY);
	}

	public void addModeRule() {
		if (theories.size() == 0) return;
		addRule(RuleType.MODE_CONVERSION);
	}

	private void addRule(RuleType ruleType) {
		TheoryFrame theoryFrame = (TheoryFrame) desktop.getSelection();
		if (null == theoryFrame) return;

		SimpleDialog ruleDialog = null;
		switch (ruleType) {
		case LITERAL_VARIABLE_SET:
			ruleDialog = new LiteralVariableDialog(shell, "New literal variable", null, null);
			break;
		case FACT:
		case STRICT:
		case DEFEASIBLE:
		case DEFEATER:
			ruleDialog = new RuleDialog(shell, "New rule", null);
			break;
		case SUPERIORITY:
			ruleDialog = new SuperiorityDialog(shell, "New superiority", null);
			break;
		case MODE_CONVERSION:
		case MODE_CONFLICT:
			ruleDialog = new ModeDialog(shell, "New mode rule", null);
			break;
		default:
			break;
		}

		String theoryId = theoryFrame.getText();
		Theory theory = theoryFrame.getTheory();
		try {
			ruleDialog.open();
			Object result = ruleDialog.getRule();

			// System.out.println("ScreenManager.addRule.result=" + result);
			if (null == result) return;
			// System.out.println("ScreenManager.addRule.result.getClass()=" + result.getClass().getName());

			if (result instanceof Rule) {
				Rule rule = (Rule) result;
				String ruleLabel = rule.getLabel();
				boolean addRule = true;
				if (ruleLabel.startsWith(Theory.DEFAULT_RULE_LABEL_PREFIX)) {
					ruleLabel = theory.getUniqueRuleLabel();
					rule.setLabel(ruleLabel);
				} else if (theory.containsRuleLabel(ruleLabel)) {
					MessageBox messageBox = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_INFORMATION);
					messageBox.setText("Add rule");
					messageBox.setMessage("replacing rule [" + ruleLabel + "] with new rule");
					if (SWT.YES == messageBox.open()) {
						remove(theory.getRule(ruleLabel));
					} else {
						addRule = false;
					}
				}
				if (addRule) {
					theoryFrame.add(rule);
					onLogEditorCommand("New rule", theoryId + ": " + rule);
					EditorFrame.getApp().currentTheoryFrameModified();
				}
			} else if (result instanceof Superiority) {
				theoryFrame.add((Superiority) result);
				onLogEditorCommand("New superiority", theoryId + ": " + result.toString());
				EditorFrame.getApp().currentTheoryFrameModified();
			} else if (result instanceof ComparableEntry) {
				@SuppressWarnings("unchecked")
				ComparableEntry<LiteralVariable, LiteralVariable> entry = (ComparableEntry<LiteralVariable, LiteralVariable>) result;
				theoryFrame.add(entry.getKey(), entry.getValue());
				EditorFrame.getApp().currentTheoryFrameModified();
			} else if (result instanceof String) {
				String modeRule = (String) result;
				if ("".equals(modeRule.trim())) return;
				theoryFrame.add(modeRule);
				onLogEditorCommand("New mode rule", theoryId + ": " + modeRule);
				EditorFrame.getApp().currentTheoryFrameModified();
			}
		} catch (Exception e) {
			onTheoryException(theoryFrame.getText(), e);
		}
	}

	public void remove(LiteralVariable literalVariableName) {
		// System.out.println("remove: literalVariable="+literalVariableName);
		TheoryFrame theoryFrame = (TheoryFrame) desktop.getSelection();
		try {
			theoryFrame.remove(literalVariableName);
			onLogEditorCommand("Remove literal variable", theoryFrame.getText() + ": " + literalVariableName);
			EditorFrame.getApp().currentTheoryFrameModified();
		} catch (Exception e) {
			onTheoryException(theoryFrame.getText(), e);
		}
	}

	public void remove(Rule rule) {
		TheoryFrame theoryFrame = (TheoryFrame) desktop.getSelection();
		try {
			theoryFrame.remove(rule);
			onLogEditorCommand("Remove rule", theoryFrame.getText() + ": " + rule);
			EditorFrame.getApp().currentTheoryFrameModified();
		} catch (Exception e) {
			onTheoryException(theoryFrame.getText(), e);
		}
	}

	public void remove(Superiority superiority) {
		TheoryFrame theoryFrame = (TheoryFrame) desktop.getSelection();
		try {
			theoryFrame.remove(superiority);
			onLogEditorCommand("Remove superiority", theoryFrame.getText() + ": " + superiority);
			EditorFrame.getApp().currentTheoryFrameModified();
		} catch (Exception e) {
			onTheoryException(theoryFrame.getText(), e);
		}
	}

	public void remove(String modeRule) {
		TheoryFrame theoryFrame = (TheoryFrame) desktop.getSelection();
		try {
			theoryFrame.remove(modeRule);
			onLogEditorCommand("Remove mode rule", theoryFrame.getText() + ": " + modeRule);
			EditorFrame.getApp().currentTheoryFrameModified();
		} catch (Exception e) {
			onTheoryException(theoryFrame.getText(), e);
		}
	}

	public void update(LiteralVariable literalVariableName) {
		TheoryFrame theoryFrame = (TheoryFrame) desktop.getSelection();
		try {
			theoryFrame.update(literalVariableName);
			onLogEditorCommand("Update literal variable", theoryFrame.getText() + ": " + literalVariableName);
			EditorFrame.getApp().currentTheoryFrameModified();
		} catch (Exception e) {
			onLogEditorCommand("Update literal variable", theoryFrame.getText() + ": " + literalVariableName + " - failed");
		}
	}

	public void update(Rule rule) {
		TheoryFrame theoryFrame = (TheoryFrame) desktop.getSelection();
		try {
			theoryFrame.update(rule);
			onLogEditorCommand("Update rule", theoryFrame.getText() + ": " + rule);
			EditorFrame.getApp().currentTheoryFrameModified();
		} catch (Exception e) {
			onLogEditorCommand("Update rule", theoryFrame.getText() + ": " + rule + " - failed");
		}
	}

	public void update(Superiority superiority) {
		TheoryFrame theoryFrame = (TheoryFrame) desktop.getSelection();
		try {
			theoryFrame.update(superiority);
			onLogEditorCommand("Update superiority", theoryFrame.getText() + ": " + superiority);
			EditorFrame.getApp().currentTheoryFrameModified();
		} catch (Exception e) {
			onLogEditorCommand("Update superiority", theoryFrame.getText() + ": " + superiority + " - failed");
		}
	}

	public void update(String modeRule) {
		TheoryFrame theoryFrame = (TheoryFrame) desktop.getSelection();
		try {
			theoryFrame.update(modeRule);
			onLogEditorCommand("Update mode rule", theoryFrame.getText() + ": " + modeRule);
			EditorFrame.getApp().currentTheoryFrameModified();
		} catch (Exception e) {
			onLogEditorCommand("Update mode rule", theoryFrame.getText() + ": " + modeRule + " - failed");
		}
	}

	public void printTheory() {
		TheoryFrame theoryFrame = (TheoryFrame) desktop.getSelection();
		if (null == theoryFrame) return;

		Theory theory = theoryFrame.getTheory();

		print(theoryFrame.getText(), "", theory.toString());
	}

	public void printConclusions() {
		TheoryFrame theoryFrame = (TheoryFrame) desktop.getSelection();
		if (null == theoryFrame) return;

		List<Conclusion> conclusions = theoryFrame.getConclusions();
		if (null == conclusions) { return; }
		StringBuilder sb = new StringBuilder();
		for (Conclusion conclusion : conclusions) {
			sb.append("\n").append(conclusion.toString());
		}
		print(theoryFrame.getText(), "Conclusions", sb.toString());
	}

	private void print(String theoryName, String caption, String textToPrint) {
		PrintDialog dialog = new PrintDialog(shell, SWT.NONE);
		if ("".equals(textToPrint)) {
			MessageDialog.openInformation(dialog.getParent(), "Print...", "No text to print!");
			return;
		}

		PrinterData printerData = dialog.open();
		if (null == printerData) return;

		if (printerData.printToFile && (null == printerData.fileName || "".equals(printerData.fileName))) {
			FileDialog dlg = new FileDialog(dialog.getParent(), SWT.SAVE);
			dlg.setText("Print file");
			String filename = dlg.open();
			if (null == filename) return;
			printerData.fileName = filename;
		}
		Printer printer = new Printer(printerData);

		PrintThread printThread = new PrintThread(printer);
		printThread.setTheoryName(theoryName);
		printThread.setTextToPrint(textToPrint);
		printThread.setCaption(caption);
		Thread thread = new Thread(printThread);
		try {
			thread.start();
		} catch (Exception e) {
			onTheoryException(theoryName, e);
		}
	}

	private File getFilename(String caption, int style, String filename) {
		FileDialog dlg = new FileDialog(shell, style);
		dlg.setText(caption);
		if (null != filename && !"".equals(filename)) dlg.setFileName(filename);
		String newFilename = dlg.open();
		if (newFilename == null) return null;
		return new File(newFilename);
	}

//	private ExplanationLogger getExplanationLogger() {
//		if (null == explanationLogger) explanationLogger = new ExplanationLogger();
//		return explanationLogger;
//	}

	@Override
	public void onTheoryException(final String theoryId, final Throwable cause) {
		String message = TextUtilities.getExceptionMessage(cause);
		onConsoleMessage(theoryId, TextUtilities.repeatStringPattern("-", 30) + "\n" + message);
		cause.printStackTrace(System.err);
	}

	@Override
	public void onTheoryMessage(final String theoryId, final String message) {
		consolePanel.addMessage(theoryId, message);
	}

	private void onConsoleMessage(final String theoryId, final String message) {
		consolePanel.addMessage(theoryId, message);
	}

	public void onLogEditorCommand(final String theoryId, final String commandName) {
		logPanel.addMessage(theoryId, commandName);
	}
}
