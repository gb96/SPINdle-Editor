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

import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;



import spindle.core.dom.Theory;
import spindle.editor.action.EditorAction;
import spindle.editor.action.impl.*;
import spindle.listener.EditorListener;
import spindle.sys.Conf;
import spindle.sys.EditorConst;

public abstract class EditorFrameCore extends ApplicationWindow {
	private static final int MESSAGE_PANEL_MIN_HEIGHT = 27;

	private List<EditorListener> listeners = null;

	public void addEditorListener(EditorListener listener) {
		if (null == listeners) listeners = new ArrayList<EditorListener>();
		if (!listeners.contains(listener)) listeners.add(listener);
	}

	public void removeEditorListener(EditorListener listener) {
		if (null == listeners) return;
		listeners.remove(listener);
	}

	private StatusLineManager slm = null;
	protected Display display = null;

	protected SashForm leftSash = null;
	protected CTabFolder desktop = null;
	protected CTabFolder messagePanel = null;
	private ScreenManager screenManager = null;
	private int[] origSashHeight = null;

	protected EditorAction groundLiteralVariables=new GroundLiteralVariables();
	protected EditorAction transformToRegularForm = new TransformRegularForm();
	protected EditorAction transformRemoveSuperiorities = new TransformRemoveSuperiority();
	protected EditorAction transformRemoveDefeaters = new TransformRemoveDefeater();
	protected EditorAction getConclusions = new GetConclusions();
	protected EditorAction printTheory = new PrintTheory();
	protected EditorAction printConclusions = new PrintConclusions();
	protected EditorAction saveTheory = new SaveTheory();
	
	protected EditorAction isWellFoundedSemantics=new IsWellFoundedSemantics();
	protected EditorAction isAmbiguityPropagation=new IsAmbiguityPropagation();

	public EditorFrameCore(Shell parent) {
		super(parent);

		display = parent.getDisplay();

		addMenuBar();
		addCoolBar(SWT.FLAT | SWT.WRAP);
		addStatusLine();
	}

	protected StatusLineManager createStatusLineManager() {
		slm = new StatusLineManager();
		slm.setMessage("Welcome to Spindle");
		slm.add(isAmbiguityPropagation);
		slm.add(isWellFoundedSemantics);
		// b=new Button(getShell(),SWT.CHECK);
		
		return slm;
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(EditorConst.TITLE);
		shell.setSize(EditorConst.DEFAULT_WINDOW_WIDTH, EditorConst.DEFAULT_WINDOW_HEIGHT);

		Monitor primary = display.getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shell.setLocation(x, y);
	}

	protected CoolBarManager createCoolBarManager(int style) {
		ToolBarManager mainToolbar = new ToolBarManager(style);
		mainToolbar.add(new NewTheory());
		mainToolbar.add(new LoadTheory());
		mainToolbar.add(new Separator());
		mainToolbar.add(saveTheory);
		mainToolbar.add(new Separator());
		mainToolbar.add(printTheory);
		mainToolbar.add(printConclusions);
		mainToolbar.add(new Separator());
		if (!EditorConst.isDeploy) {
			mainToolbar.add(groundLiteralVariables);
			mainToolbar.add(transformToRegularForm);
			mainToolbar.add(transformRemoveSuperiorities);
			mainToolbar.add(transformRemoveDefeaters);
			mainToolbar.add(new Separator());
		}
		mainToolbar.add(getConclusions);

		CoolBarManager coolbar = new CoolBarManager(style);
		coolbar.add(mainToolbar);
		return coolbar;
	}

	protected MenuManager createMenuManager() {
		final EditorAction saveTheoryAs = new SaveTheoryAs();
		final EditorAction saveConclusions = new SaveConclusions();
		final EditorAction saveConclusionsAs = new SaveConclusionsAs();
		final EditorAction closeTheory = new CloseTheory();
		final EditorAction closeAllTheories = new CloseAllTheories();

		final EditorAction newLiteralVariable=new NewLiteralVariable();
		final EditorAction newRule = new NewRule();
		final EditorAction newSuperiority = new NewSuperiority();
		final EditorAction newModeRule = new NewModeRule();

		MenuManager fileMenu = new MenuManager("&File");
		fileMenu.add(new NewTheory());
		fileMenu.add(new LoadTheory());
		fileMenu.add(new Separator());
		fileMenu.add(saveTheory);
		fileMenu.add(saveTheoryAs);
		fileMenu.add(saveConclusions);
		fileMenu.add(saveConclusionsAs);
		fileMenu.add(new Separator());
		fileMenu.add(closeTheory);
		fileMenu.add(closeAllTheories);
		fileMenu.add(new Separator());
		fileMenu.add(printTheory);
		fileMenu.add(printConclusions);
		fileMenu.add(new Separator());

		if (!SWT.getPlatform().equals("cocoa")) fileMenu.add(new Exit());

		fileMenu.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager menu) {
				TheoryFrame theoryFrame = (TheoryFrame) desktop.getSelection();
				if (desktop.getItemCount() == 0 || null == theoryFrame) {
					saveTheory.setEnabled(false);
					saveTheoryAs.setEnabled(false);
					closeTheory.setEnabled(false);
					closeAllTheories.setEnabled(false);
					printTheory.setEnabled(false);

					saveConclusions.setEnabled(false);
					saveConclusionsAs.setEnabled(false);
					printConclusions.setEnabled(false);
				} else {
					Theory theory = theoryFrame.getTheory();
					saveTheory.setEnabled(!theory.isEmpty());
					saveTheoryAs.setEnabled(!theory.isEmpty());
					closeTheory.setEnabled(true);
					closeAllTheories.setEnabled(true);
					printTheory.setEnabled(!theory.isEmpty());

					if (theoryFrame.getConclusionsCount() > 0) {
						saveConclusions.setEnabled(true);
						saveConclusionsAs.setEnabled(true);
						printConclusions.setEnabled(true);
					} else {
						saveConclusions.setEnabled(false);
						saveConclusionsAs.setEnabled(false);
						printConclusions.setEnabled(false);
					}
				}
			}
		});

		MenuManager editMenu = new MenuManager("&Edit");
		editMenu.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(IMenuManager menu) {
				if (desktop.getItemCount() == 0) {
					newLiteralVariable.setEnabled(false);
					newRule.setEnabled(false);
					newSuperiority.setEnabled(false);
					newModeRule.setEnabled(false);
				} else {
					newLiteralVariable.setEnabled(true);
					newRule.setEnabled(true);
					newSuperiority.setEnabled(true);
					newModeRule.setEnabled(true);
				}
			}
		});
		editMenu.add(newLiteralVariable);
		editMenu.add(newRule);
		editMenu.add(newSuperiority);
		editMenu.add(newModeRule);
		if (!EditorConst.isDeploy) {
			editMenu.add(new Separator());
			editMenu.add(new Preference());
		}

		MenuManager runMenu = new MenuManager("&Run");

		if (!EditorConst.isDeploy) {
			final MenuManager transformMenu = new MenuManager("T&ransform");
			transformMenu.add(groundLiteralVariables);
			transformMenu.add(transformToRegularForm);
			transformMenu.add(transformRemoveSuperiorities);
			transformMenu.add(transformRemoveDefeaters);
			transformMenu.add(new Separator());

			runMenu.add(transformMenu);
		}

		runMenu.add(isAmbiguityPropagation);
		runMenu.add(isWellFoundedSemantics);
		runMenu.add(new Separator());

		runMenu.add(getConclusions);

		runMenu.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager menu) {
				if (desktop.getItemCount() == 0) {
					groundLiteralVariables.setEnabled(false);
					transformToRegularForm.setEnabled(false);
					transformRemoveSuperiorities.setEnabled(false);
					transformRemoveDefeaters.setEnabled(false);
					getConclusions.setEnabled(false);
				} else {
					Theory theory = getCurrentTheoryFrame().getTheory();
					transformToRegularForm.setEnabled(!theory.isEmpty());
					switch (Conf.getReasonerVersion()) {
					case 2:
						transformRemoveSuperiorities.setEnabled(false);
						break;
					case 1:
						transformRemoveSuperiorities.setEnabled(theory.getSuperiorityCount() > 0);
						break;
					}
					transformRemoveDefeaters.setEnabled(theory.getDefeatersCount() > 0);
					getConclusions.setEnabled(!theory.isEmpty());
					groundLiteralVariables.setEnabled((theory.getLiteralVariablesInRulesCount()>0||theory.getLiteralBooleanFunctionCount()>0));
				}
			}
		});

		MenuManager helpMenu = new MenuManager("&Help");
		if (!EditorConst.isDeploy) {
			helpMenu.add(new Welcome());
			helpMenu.add(new Separator());
		}
		helpMenu.add(new AboutEditor());

		MenuManager mainMenu = new MenuManager("");
		mainMenu.add(fileMenu);
		mainMenu.add(editMenu);
		mainMenu.add(runMenu);
		mainMenu.add(helpMenu);
		return mainMenu;
	}

	protected Control createContents(final Composite parent) {
		Composite contentWrapper = new Composite(parent, SWT.NONE);
		contentWrapper.setLayout(new FillLayout());

		SashForm outterSash = new SashForm(contentWrapper, SWT.HORIZONTAL);

		leftSash = new SashForm(outterSash, SWT.VERTICAL);
		leftSash.setLayout(new FillLayout());

		Composite c1 = new Composite(leftSash, SWT.NONE);
		c1.setLayout(new FillLayout());
		(new Label(c1, SWT.NONE)).setText("Label in pane 1");

		// Composite c2 = new Composite(leftSash, SWT.NONE);
		// c2.setLayout(new FillLayout());

		desktop = new CTabFolder(leftSash, SWT.BORDER);
		desktop.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				currentTheoryFrameModified();
			}
		});

		// final Composite c3 = new Composite(leftSash, SWT.BORDER);
		// c3.setLayout(new FillLayout());
		// messagePanel = new CTabFolder(c3, SWT.BORDER);
		messagePanel = new CTabFolder(leftSash, SWT.BORDER);
		messagePanel.setLayout(new FillLayout());
		messagePanel.setMinimizeVisible(true);
		messagePanel.setMaximized(true);
		messagePanel.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void minimize(CTabFolderEvent event) {
				messagePanel.setMaximizeVisible(true);
				messagePanel.setMinimizeVisible(false);
				messagePanel.setMinimized(true);
				int ratio = getRatio();
				leftSash.setWeights(new int[] { 0, 100 - ratio, ratio });
				getShell().layout(true);
			}

			public void maximize(CTabFolderEvent event) {
				messagePanel.setMaximizeVisible(false);
				messagePanel.setMinimizeVisible(true);
				messagePanel.setMaximized(true);
				int ratio = getRatio();
				leftSash.setWeights(new int[] { 0, 100 - ratio, ratio });
				getShell().layout(true);
			}
		});

		Composite rightSash = new Composite(outterSash, SWT.NONE);
		rightSash.setLayout(new FillLayout());
		new Button(rightSash, SWT.PUSH).setText("left sash");

		leftSash.setWeights(new int[] { 0, 70, 30 });
		outterSash.setWeights(new int[] { 100 - EditorConst.WINDOW_LEFT_SASH_WIDTH, //
				EditorConst.WINDOW_LEFT_SASH_WIDTH });

		screenManager = new ScreenManager(getShell(), desktop, messagePanel);

		desktop.setFocus();
		return contentWrapper;
	}

	private TheoryFrame getCurrentTheoryFrame() {
		if (desktop.getItemCount() == 0) return null;
		TheoryFrame theoryFrame = (TheoryFrame) desktop.getSelection();
		return theoryFrame;
	}

	private int getRatio() {
		int windowHeight = 0;
		int messagePanelHeight = 0;
		if (messagePanel.getMinimized()) {
			origSashHeight = leftSash.getWeights().clone();
			messagePanelHeight = MESSAGE_PANEL_MIN_HEIGHT;
			windowHeight = leftSash.getSize().y;
		} else {
			for (int i = 0; i < 3; i++) {
				windowHeight += origSashHeight[i];
			}
			messagePanelHeight = origSashHeight[2];
		}
		return getRatio(windowHeight, messagePanelHeight);
	}

	private int getRatio(int total, int part) {
		return (int) (100.0 * part / total);
	}

	public ScreenManager getScreenManager() {
		return screenManager;
	}

	public void run() {
		// don't return from open() until window closes
		setBlockOnOpen(true);

		// open the main window
		open();

		// dispose the display
		close();
		Display.getDefault().dispose();
		display = null;
	}

	public abstract void currentTheoryFrameModified();

}
