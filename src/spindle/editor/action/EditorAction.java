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
package spindle.editor.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

public abstract class EditorAction extends Action {
	public EditorAction(final String label, final String tooltip, final int accelerator, final String icon) {
		super(label);
		setAction(tooltip, accelerator, icon);
	}

	public EditorAction(final String label, final String tooltip, final int accelerator, final String icon, int style) {
		super(label, style);
		setAction(tooltip, accelerator, icon);
	}

	private void setAction(final String tooltip, final int accelerator, final String icon) {
		if (!"".equals(tooltip)) setToolTipText(tooltip);
		if (accelerator >= 0) this.setAccelerator(accelerator);
		if (!"".equals(icon)) {
			try {
				setImageDescriptor(ImageDescriptor.createFromFile(getClass(), icon));
			} catch (Exception e) {
				throw new IllegalArgumentException("icon [" + icon + "] not found");
			}
		}
	}
}
