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

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

import spindle.sys.EditorConf;

public class AboutSpindleEditor extends TitleAreaDialog {
	private static final int DIALOG_WIDTH = 500;
	private static final int DIALOG_HEIGHT = 380;
	private String title = null;
	private String[] items = null;

	public AboutSpindleEditor(Shell parent, String title, String[] items) {
		super(parent);
		this.title = (null == title) ? "About " + EditorConf.getAppTitle() : title.trim();
		this.items = items;
	}

	/**
	 * @see org.eclipse.jface.window.Window#create() We complete the dialog with a title and a message
	 */
	public void create() {
		super.create();
		setTitle(title);
		// setMessage("You have mail! \n It could be vital for this evening...");
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog# createDialogArea(org.eclipse.swt.widgets.Composite) Here we fill the
	 *      center area of the dialog
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		final ScrolledComposite scrolledContentWrapper = new ScrolledComposite(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledContentWrapper.setLayoutData(new GridData(DIALOG_WIDTH - 38, DIALOG_HEIGHT - 175));

		StringBuilder sb = new StringBuilder();
		for (String s : items)
			sb.append(s).append("\n");

		final Link l = new Link(scrolledContentWrapper, SWT.NONE);
		l.setText(sb.toString());
		l.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

		scrolledContentWrapper.setContent(l);
		scrolledContentWrapper.setExpandVertical(true);
		scrolledContentWrapper.setExpandHorizontal(true);
		scrolledContentWrapper.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				Rectangle r = scrolledContentWrapper.getClientArea();
				scrolledContentWrapper.setMinSize(l.computeSize(r.width, SWT.DEFAULT));
			}
		});

		return composite;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		// Create Close button
		Button closeBtn = createButton(parent, SWT.CLOSE, "Close", true);
		// Add a SelectionListener
		closeBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setReturnCode(SWT.CLOSE);
				close();
			}
		});
	}

	@Override
	public Point getInitialSize() {
		return new Point(DIALOG_WIDTH, DIALOG_HEIGHT);
	}
}
