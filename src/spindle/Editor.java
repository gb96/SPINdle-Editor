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
package spindle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.app.exception.InvalidArgumentException;
import com.app.utils.Utilities;

import spindle.core.ReasonerUtilities;
import spindle.editor.frame.EditorFrame;
import spindle.exception.EditorException;
import spindle.sys.AppConst;


public class Editor {
	private static Map<String, String> _args = null;
	private static List<String> _nonArgs=null;

	private static void extractArgs(String[] args) throws EditorException, InvalidArgumentException {
		_args = new TreeMap<String, String>();
		_nonArgs=new ArrayList<String>();
		
		Utilities.extractArguments(args, AppConst.ARGUMENT_PREFIX, _args, _nonArgs);		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			extractArgs(args);

			// print application message
			boolean isPrintAppMessage = false;
			try {
				isPrintAppMessage = ReasonerUtilities.printAppMessage(_args);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}

			if (!isPrintAppMessage) {
				Display display = Display.getDefault();
				if (null == display) display = Display.getDefault();
				Shell shell = new Shell(display);

				EditorFrame editor = new EditorFrame(shell, _args);
				editor.run();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}

}
