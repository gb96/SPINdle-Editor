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
package spindle.editor.panel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import spindle.sys.EditorConst;

public class ConsoleMessagePanel extends GenericMessagePanel {
	private static final String CAPTION = "Console";
	protected Text text = null;

	public ConsoleMessagePanel(CTabFolder parent) {
		super(parent, CAPTION);
		Control contents = createContents(parent);
		setControl(contents);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite contentWrapper = new Composite(parent, SWT.NONE);
		contentWrapper.setLayout(new FillLayout());

		text = new Text(contentWrapper, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		text.setEditable(false);

		Font font = text.getFont();
		FontData[] fontData = font.getFontData();
		for (int i = 0; i < fontData.length; i++) {
			fontData[i].setHeight(EditorConst.FONT_SIZE);
		}
		text.setFont(new Font(getDisplay(), fontData));

		// text.setText("Text for item " + "\n\none, two, three\n\nabcdefghijklmnop");
		return contentWrapper;
	}

	@Override
	public void clearMessage() {
		setText("");
	}

	@Override
	public void addMessage(final String message) {
		addMessage(null, message);
	}

	@Override
	public void addMessage(String theoryId, String message) {
		if (null == text) {
			System.err.println("text is null");
			return;
		}
		text.append(message + "\n");
	}

}
