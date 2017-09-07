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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.app.utils.ComparableEntry;

import spindle.core.dom.Conclusion;
import spindle.core.dom.DomConst;
import spindle.core.dom.LiteralVariable;
import spindle.core.dom.Rule;
import spindle.core.dom.RuleType;
import spindle.core.dom.Superiority;
import spindle.core.dom.Theory;
import spindle.editor.action.EditorAction;
import spindle.editor.action.impl.*;
import spindle.editor.dialog.*;
import spindle.exception.EditorException;
import spindle.io.parser.DflTheoryParser2;
import spindle.listener.TheoryFrameListener;
import spindle.sys.ConfigurationException;
import spindle.sys.EditorConst;
import spindle.sys.Messages;
import spindle.sys.message.ErrorMessage;

public abstract class TheoryFrameCore extends CTabItem {
	private static final int THEORY_TREE_RULE_ID_WIDTH = 160;
	private static final int THEORY_TREE_RULE_CONTENT_WIDTH = 600;

	private List<TheoryFrameListener> listeners = null;

	public void addTheoryFrameListener(TheoryFrameListener listener) {
		if (null == listeners) listeners = new ArrayList<TheoryFrameListener>();
		if (!listeners.contains(listener)) listeners.add(listener);
	}

	public void removeTheoryFrameListener(TheoryFrameListener listener) {
		if (null == listeners) return;
		listeners.remove(listener);
	}

	protected void onTheoryFrameException(final Throwable cause) {
		if (null == listeners) return;
		for (TheoryFrameListener listener : listeners) {
			listener.onTheoryException(id, cause);
		}
	}

	protected void onTheoryMessage(final String message) {
		if (null == listeners) return;
		for (TheoryFrameListener listener : listeners) {
			listener.onTheoryMessage(id, message);
		}
	}

	protected Shell shell = null;

	protected String id = null;
	protected boolean isModified = false;
	protected Theory theory = null;
	protected List<Conclusion> conclusions = null;
	protected File theoryFilename = null;
	protected File conclusionsFilename = null;

	protected Tree theoryTree = null;
	protected Map<RuleType, TreeItem> ruleCategoryTreeItem = new TreeMap<RuleType, TreeItem>();
	protected Map<String, TreeItem> theoryTreeItems = new TreeMap<String, TreeItem>();
	protected Map<String, TreeItem> conversionModeTreeItems = new TreeMap<String, TreeItem>();
	protected Map<String, TreeItem> conflictModeTreeItems = new TreeMap<String, TreeItem>();

	protected Object mouseSelectedItem = null;
	protected Map<LiteralVariable, LiteralVariable> literalVariables = null;

	public TheoryFrameCore(final String id, final String caption, final CTabFolder parent, final int style) throws ConfigurationException {
		super(parent, style);
		if (null == id || "".equals(id.trim())) throw new ConfigurationException("theory id  is null");
		this.id = id.trim();
		String lbl = (null == caption) ? this.id : caption;
		setText(lbl);
		setShowClose(true);

		Composite contentWrapper = createContents(parent);
		shell = getDisplay().getActiveShell();
		if (null != contentWrapper) setControl(contentWrapper);

		literalVariables = new TreeMap<LiteralVariable, LiteralVariable>();
	}

	protected Composite createContents(Composite parent) {
		Composite contentWrapper = new Composite(parent, SWT.NONE);
		FillLayout layout = new FillLayout();
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		contentWrapper.setLayout(layout);

		theoryTree = new Tree(contentWrapper, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		theoryTree.setHeaderVisible(true);
		theoryTree.setMenu(generateTheoryTreeMenu());

		Font font = theoryTree.getFont();
		FontData[] fontData = font.getFontData();
		for (int i = 0; i < fontData.length; i++) {
			fontData[i].setHeight(EditorConst.FONT_SIZE);
		}
		theoryTree.setFont(new Font(getDisplay(), fontData));

		TreeColumn column1 = new TreeColumn(theoryTree, SWT.LEFT);
		column1.setText("Rule Id");
		column1.setWidth(THEORY_TREE_RULE_ID_WIDTH);
		TreeColumn column2 = new TreeColumn(theoryTree, SWT.LEFT);
		column2.setText("Rule");
		column2.setWidth(THEORY_TREE_RULE_CONTENT_WIDTH);

		theoryTree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent event) {
				switch (event.keyCode) {
				case SWT.DEL:
					onDeleteMouseSelectedItem();
					break;
				case SWT.CR:
					onEditMouseSelectedItem();
					break;
				case SWT.ARROW_UP:
				case SWT.ARROW_DOWN:
					TreeItem[] selection = theoryTree.getSelection();
					if (selection.length == 0) return;
					mouseSelectedItem = selection[0].getData();
				}
			}
		});

		theoryTree.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent event) {
				mouseSelectedItem = null;
				Point pt = new Point(event.x, event.y);
				TreeItem itemx = theoryTree.getItem(pt);
				if (!EditorConst.isDeploy) {
					if (null == itemx) {
						System.out.println("mouseUp.itemx is null");
					} else {
						System.out.println("mouseUp.itemx=" + itemx.getData());
					}
				}
				if (null != itemx) mouseSelectedItem = itemx.getData();
			}

			@Override
			public void mouseDown(MouseEvent event) {
				mouseSelectedItem = null;
				Point pt = new Point(event.x, event.y);
				TreeItem itemx = theoryTree.getItem(pt);
				if (!EditorConst.isDeploy) {
					if (null == itemx) {
						System.out.println("mouseDown.itemx is null");
					} else {
						System.out.println("mouseDown.itemx=" + itemx.getData());
					}
				}
				if (null != itemx) mouseSelectedItem = itemx.getData();
			}

			@Override
			public void mouseDoubleClick(MouseEvent event) {
				onEditMouseSelectedItem();
			}
		});

		return contentWrapper;
	}

	private void onEditMouseSelectedItem() {
		TreeItem[] selection = theoryTree.getSelection();
		if (selection.length == 0) return;
		Object data = selection[0].getData();
		if (null == data) return;

		try {
			if (data instanceof LiteralVariable) {
				ScreenManager.getApp().update((LiteralVariable) data);
			} else if (data instanceof Rule) {
				ScreenManager.getApp().update((Rule) data);
			} else if (data instanceof Superiority) {
				ScreenManager.getApp().update((Superiority) data);
			} else {
				RuleType ruleType = DflTheoryParser2.getRuleType(data.toString());
				// System.out.println("ruleType=" + ruleType);
				switch (ruleType) {
				case MODE_CONVERSION:
				case MODE_CONFLICT:
					ScreenManager.getApp().update(data.toString());
				default:
				}
			}
		} catch (Exception e) {
			ScreenManager.getApp().onTheoryException(getText(), e);
		}
	}

	private void onDeleteMouseSelectedItem() {
		// System.out.println("onDeleteMouseSelectedItem=" + mouseSelectedItem);
		if (null == mouseSelectedItem) return;
		if (mouseSelectedItem instanceof LiteralVariable) {
			LiteralVariable literalVariable = (LiteralVariable) mouseSelectedItem;
			ScreenManager.getApp().remove(literalVariable);
			isModified = true;
		} else if (mouseSelectedItem instanceof Rule) {
			Rule rule = (Rule) mouseSelectedItem;
			ScreenManager.getApp().remove(rule);
			isModified = true;
		} else if (mouseSelectedItem instanceof Superiority) {
			Superiority superiority = (Superiority) mouseSelectedItem;
			ScreenManager.getApp().remove(superiority);
			isModified = true;
		} else if (mouseSelectedItem instanceof String) {
			String theoryStr = (String) mouseSelectedItem;
			ScreenManager.getApp().remove(theoryStr);
			isModified = true;
		}
	}

	private MenuItem generateNewMenuItem(final Menu menu, final EditorAction editorAction) {
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText(editorAction.getText());
		item.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				try {
					if (editorAction instanceof Remove) {
						onDeleteMouseSelectedItem();
					} else {
						editorAction.run();
					}
				} catch (Exception e) {
					onTheoryFrameException(e);
				}
			}
		});
		return item;
	}

	private Menu generateTheoryTreeMenu() {
		final EditorAction newLiteralVariable = new NewLiteralVariable();
		final EditorAction newRule = new NewRule();
		final EditorAction newSuperiority = new NewSuperiority();
		final EditorAction newModeRule = new NewModeRule();
		final EditorAction removeRule = new Remove();

		newRule.setEnabled(true);
		newSuperiority.setEnabled(true);

		Menu menu = new Menu(getParent().getShell(), SWT.POP_UP);

		MenuItem editMenu = new MenuItem(menu, SWT.CASCADE);
		editMenu.setText("Edit");

		Menu editSubMenu = new Menu(menu);
		editMenu.setMenu(editSubMenu);
		editSubMenu.addMenuListener(new MenuAdapter() {

			@Override
			public void menuHidden(MenuEvent event) {
				if (theory.getFactsAndAllRules().size() == 0) {
					removeRule.setEnabled(false);
				} else {
					removeRule.setEnabled(true);
				}
			}
		});

		generateNewMenuItem(editSubMenu, newLiteralVariable);
		generateNewMenuItem(editSubMenu, newRule);
		generateNewMenuItem(editSubMenu, newSuperiority);
		generateNewMenuItem(editSubMenu, newModeRule);
		new MenuItem(editSubMenu, SWT.SEPARATOR);
		generateNewMenuItem(editSubMenu, removeRule);

		return menu;
	}

	public String getId() {
		return id;
	}

	public boolean isModified() {
		return isModified;
	}

	public Object getMouseSelectedItem() {
		return mouseSelectedItem;
	}

	public Map<String, TreeItem> getTheoryTreeItems() {
		return theoryTreeItems;
	}

	public Map<String, TreeItem> getConversionModeTreeItems() {
		return conversionModeTreeItems;
	}

	public Map<String, TreeItem> getConflictModeTreeItems() {
		return conflictModeTreeItems;
	}

	public Theory getTheory() {
		return theory;
	}

	public void setTheory(final Theory theory) throws ConfigurationException {
		if (null == theory) throw new ConfigurationException("theory is null");
		this.theory = theory;
		showTheory();
		isModified = false;
	}

	private void disposeAllTreeItems(Collection<TreeItem> items) {
		for (TreeItem item : items)
			item.dispose();
	}

	private void showTheory() {
		disposeAllTreeItems(conversionModeTreeItems.values());
		disposeAllTreeItems(conflictModeTreeItems.values());
		disposeAllTreeItems(theoryTreeItems.values());
		disposeAllTreeItems(ruleCategoryTreeItem.values());
		for (Control c : theoryTree.getChildren())
			c.dispose();

		theoryTree.clearAll(true);
		ruleCategoryTreeItem.clear();
		theoryTreeItems.clear();
		conversionModeTreeItems.clear();
		conflictModeTreeItems.clear();
		conclusions = null;

		if (theory.isEmpty() //
				&& theory.getSuperiorityCount() == 0 //
				&& theory.getModeConflictRulesCount() == 0 //
				&& theory.getModeConversionRulesCount() == 0) return;

		for (RuleType ruleType : RuleType.values()) {
			switch (ruleType) {
			case FACT:
			case STRICT:
			case DEFEASIBLE:
				for (Rule rule : theory.getRules(ruleType).values()) {
					addTheoryTreeItem(rule);
				}
				break;
			default:
				break;
			}
		}
		if (theory.getSuperiorityCount() > 0) {
			for (Superiority superiority : theory.getAllSuperiority()) {
				addTheoryTreeItem(superiority);
			}
		}
		if (theory.getModeConversionRulesCount() > 0) {
			for (Entry<String, Set<String>> entry : theory.getAllModeConversionRules().entrySet()) {
				addTheoryTreeItem(RuleType.MODE_CONVERSION, entry.getKey(), entry.getValue().toString());
			}
		}
		if (theory.getModeConflictRulesCount() > 0) {
			for (Entry<String, Set<String>> entry : theory.getAllModeConflictRules().entrySet()) {
				addTheoryTreeItem(RuleType.MODE_CONFLICT, entry.getKey(), entry.getValue().toString());
			}
		}
	}

	public void setConclusions(List<Conclusion> conclusions) {
		this.conclusions = conclusions;
	}

	protected void addTheoryTreeItem(RuleType ruleType, String origMode, String modeRule) {
		TreeItem categoryItem = getTheoryTreeCategoryItem(ruleType);
		TreeItem item = new TreeItem(categoryItem, SWT.NONE);
		String m = modeRule.replaceAll("[\\[\\]]", "");
		String str = origMode + ruleType.getSymbol() + m;
		item.setText(0, str);
		item.setData(str);
		item.setExpanded(true);

		categoryItem.setExpanded(true);
		switch (ruleType) {
		case MODE_CONVERSION:
			conversionModeTreeItems.put(origMode, item);
			break;
		case MODE_CONFLICT:
			conflictModeTreeItems.put(origMode, item);
			break;
		default:
		}
		isModified = true;
	}

	protected void addTheoryTreeItem(Superiority superiority) {
		TreeItem categoryItem = getTheoryTreeCategoryItem(RuleType.SUPERIORITY);
		TreeItem item = new TreeItem(categoryItem, SWT.NONE);
		item.setText(0, superiority.toString());
		item.setData(superiority);
		item.setExpanded(true);

		categoryItem.setExpanded(true);
		theoryTreeItems.put(superiority.toString(), item);
		isModified = true;
	}

	protected void addTheoryTreeItem(LiteralVariable literalVariableName, LiteralVariable literalVariableValue) {
		TreeItem categoryItem = getTheoryTreeCategoryItem(RuleType.LITERAL_VARIABLE_SET);
		TreeItem item = new TreeItem(categoryItem, SWT.NONE);
		String t = RuleType.LITERAL_VARIABLE_SET.getSymbol() + " " + literalVariableName.toString() + DomConst.Literal.THEORY_EQUAL_SIGN
				+ literalVariableValue.toString();
		item.setText(0, t);
		item.setData(literalVariableName);
		item.setExpanded(true);

		categoryItem.setExpanded(true);
		theoryTreeItems.put(literalVariableName.toString(), item);
		isModified = true;
	}

	protected void addTheoryTreeItem(Rule rule) {
		TreeItem categoryItem = getTheoryTreeCategoryItem(rule.getRuleType());
		TreeItem item = new TreeItem(categoryItem, SWT.NONE);
		item.setText(0, rule.getLabel());
		item.setText(1, rule.getRuleAsString().trim());
		item.setData(rule);
		item.setExpanded(true);

		categoryItem.setExpanded(true);
		theoryTreeItems.put(rule.getLabel(), item);
		isModified = true;
	}

	private TreeItem getTheoryTreeCategoryItem(RuleType ruleType) {
		TreeItem categoryItem = ruleCategoryTreeItem.get(ruleType);
		if (null == categoryItem) {
			categoryItem = new TreeItem(theoryTree, SWT.NONE);
			categoryItem.setText(ruleType.getLabel());
			ruleCategoryTreeItem.put(ruleType, categoryItem);
		}
		return categoryItem;
	}

	public List<Conclusion> getConclusions() {
		return conclusions;
	}

	public long getConclusionsCount() {
		if (null == conclusions) return 0;
		return conclusions.size();
	}

	public File getTheoryFilename() {
		return theoryFilename;
	}

	public void setTheoryFilename(final String theoryFilename) {
		if (null == theoryFilename || "".equals(theoryFilename.trim())) throw new IllegalArgumentException(
				Messages.getErrorMessage(ErrorMessage.IO_EMPTY_FILENAME));
		setTheoryFilename(new File(theoryFilename));
	}

	public void setTheoryFilename(File theoryFilename) {
		if (null == theoryFilename) throw new IllegalArgumentException("filename is null");
		this.theoryFilename = theoryFilename;
		setText(this.theoryFilename.getName());
	}

	public File getConclusionsFilename() {
		return conclusionsFilename;
	}

	public void setConclusionsFilename(final String conclusionsFilename) {
		if (null == conclusionsFilename || "".equals(conclusionsFilename.trim())) throw new IllegalArgumentException(
				Messages.getErrorMessage(ErrorMessage.IO_EMPTY_FILENAME));
		setConclusionsFilename(new File(conclusionsFilename));
	}

	public void setConclusionsFilename(final File conclusionsFilename) {
		if (null == conclusionsFilename) throw new IllegalArgumentException("filename is null");
		this.conclusionsFilename = conclusionsFilename;
	}

	public void update(LiteralVariable literalVariableName) throws EditorException {
		try {
			SimpleDialog dialog = new LiteralVariableDialog(shell, "Modify rule", literalVariableName, theory.getLiteralVariables().get(
					literalVariableName));
			dialog.open();
			@SuppressWarnings("unchecked")
			ComparableEntry<LiteralVariable, LiteralVariable> entry = (ComparableEntry<LiteralVariable, LiteralVariable>) dialog
					.getRule();
			if (null != entry && null != entry.getKey()) {
				remove(entry.getKey());
				if (null != entry.getValue()) add(entry.getKey(), entry.getValue());
			}
		} catch (Exception e) {
			onTheoryFrameException(e);
		}
	}

	public void update(Rule rule) throws EditorException {
		try {
			SimpleDialog dialog = new RuleDialog(shell, "Modify rule", rule);
			dialog.open();
			Rule newRule = (Rule) dialog.getRule();

			if (!(rule.equals(newRule) && rule.getLabel().equals(newRule.getLabel()))) {
				if (null != rule) remove(rule);
				if (null != newRule) add(newRule);
			}
		} catch (Exception e) {
			onTheoryFrameException(e);
		}
	}

	public void update(Superiority superiority) throws EditorException {
		try {
			SimpleDialog dialog = new SuperiorityDialog(shell, "Modify superiority relation", superiority);
			dialog.open();
			Superiority newSuperiority = (Superiority) dialog.getRule();

			if (!superiority.equals(newSuperiority)) {
				if (null != superiority) remove(superiority);
				if (null != newSuperiority) add(newSuperiority);
			}
		} catch (Exception e) {
			onTheoryFrameException(e);
		}
	}

	public void update(String modeRule) throws EditorException {
		try {
			SimpleDialog dialog = new ModeDialog(shell, "Modify mode rule", modeRule);
			dialog.open();
			String newModeRule = (String) dialog.getRule();
			if (!modeRule.equals(newModeRule)) {
				if (null != modeRule && !"".equals(modeRule)) remove(modeRule);
				if (null != newModeRule && !"".equals(newModeRule.trim())) add(newModeRule);
			}
		} catch (Exception e) {
			onTheoryFrameException(e);
		}
	}

	public abstract void add(Rule rule) throws EditorException;

	public abstract void remove(Rule rule) throws EditorException;

	public abstract void add(LiteralVariable literalVariableName, LiteralVariable literalVariableValue) throws EditorException;

	public abstract void remove(LiteralVariable literalVariableName) throws EditorException;

	public abstract void add(Superiority superiority) throws EditorException;

	public abstract void remove(Superiority superiority) throws EditorException;

	public abstract void add(String modeRule) throws EditorException;

	public abstract void remove(String modeRule) throws EditorException;

}
