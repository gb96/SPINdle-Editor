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
package spindle.sys;

public interface EditorConst {

	boolean isDeploy = true;

	String TITLE = "SPINdle Defeasible Theory Editor";
	int DEFAULT_WINDOW_WIDTH = 700;
	int DEFAULT_WINDOW_HEIGHT = 600;

	int WINDOW_LEFT_SASH_WIDTH = 0;

	int FONT_SIZE = 10;
	int PRINT_FONT_SIZE = 6;
	int HEADER_FOOTER_MARGIN = 3;

	int TAB_SIZE = 4;

	String NEW_THEORY_ID_PREFIX = "NEW_THEORY_";
	String NEW_THEORY_CAPTION = "untitle";
	int NEW_THEORY_RANDOM_KEY_LENGTH = 10;

}
