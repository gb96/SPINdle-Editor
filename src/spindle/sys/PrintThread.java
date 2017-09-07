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

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.Printer;

import spindle.editor.frame.ScreenManager;

public class PrintThread implements Runnable {
	private String theoryName = null;
	private String textToPrint = null;
	private String caption = null;

	private Printer printer = null;
	private int leftMargin, rightMargin, topMargin, bottomMargin;
	private int tabWidth, lineHeight;
	private int x, y;
	private int index, end;
	private GC gc = null;
	private String tabs = null;
	private StringBuilder stringBuffer = null;
	private int pageNo = 0;

	public PrintThread(Printer printer) {
		this.printer = printer;
		setTheoryName("");
		setTextToPrint("");
		setCaption("");
	}

	public void setTheoryName(final String theoryName) {
		this.theoryName = (null == theoryName) ? "" : theoryName.trim();
	}

	public void setTextToPrint(final String textToPrint) {
		this.textToPrint = (null == textToPrint) ? "" : textToPrint.trim();
	}

	public void setCaption(final String caption) {
		this.caption = (null == caption) ? "" : caption.trim();
	}

	@Override
	public void run() {
		try {
			printTextToPrinter();
		} catch (Exception e) {
			ScreenManager.getApp().onTheoryException(theoryName, e);
		} finally {
			if (null != printer) printer.dispose();
		}
	}

	private void printTextToPrinter() {
		if (printer.startJob(theoryName)) {
			Rectangle clientArea = printer.getClientArea();
			Rectangle trim = printer.computeTrim(0, 0, 0, 0);
			Point dpi = printer.getDPI();
			leftMargin = dpi.x + trim.x; // one inch from left side of paper
			rightMargin = clientArea.width - dpi.x + trim.x + trim.width; // one inch from right side of
			// paper
			topMargin = dpi.y + trim.y; // one inch from top edge of paper
			bottomMargin = clientArea.height - dpi.y + trim.y + trim.height; // one inch from bottom edge of
			// paper

			StringBuilder tabBuffer = new StringBuilder(EditorConst.TAB_SIZE);
			for (int i = 0; i < EditorConst.TAB_SIZE; i++)
				tabBuffer.append(' ');
			tabs = tabBuffer.toString();

			/* Create printer GC, and create and set the printer font & foreground color. */
			gc = new GC(printer);
			Font printerFont = gc.getFont();
			FontData[] printerFontData = printerFont.getFontData();
			for (int i = 0; i < printerFontData.length; i++) {
				printerFontData[i].setHeight(EditorConst.PRINT_FONT_SIZE);
			}
			printerFont = new Font(printer, printerFontData);
			gc.setFont(printerFont);
			gc.setLineWidth(1);

			tabWidth = gc.stringExtent(tabs).x;
			lineHeight = gc.getFontMetrics().getHeight();

			bottomMargin -= (lineHeight + 2 * EditorConst.HEADER_FOOTER_MARGIN);

			/* Print text to current gc using word wrap */
			printText();
			printer.endJob();

			/* Cleanup graphics resources used in printing */
			gc.dispose();
		}
	}

	private void printHeader() {
		int wordWidth = gc.stringExtent(theoryName).x;
		x = rightMargin - wordWidth;
		if (x < leftMargin) x = leftMargin;
		gc.drawString(theoryName, x, y, false);
		y += lineHeight;
		y += EditorConst.HEADER_FOOTER_MARGIN;
		gc.drawLine(leftMargin, y, rightMargin, y);
		x = leftMargin;
		y += EditorConst.HEADER_FOOTER_MARGIN;

		x = leftMargin;
		if (pageNo == 1 && !"".equals(caption)) {
			wordWidth = gc.stringExtent(caption).x;
			gc.drawString(caption, x, y, false);
			y += lineHeight;
			gc.drawLine(leftMargin, y, leftMargin + wordWidth, y);
			y += EditorConst.HEADER_FOOTER_MARGIN;
		}
	}

	private void printFooter() {
		if (y < bottomMargin) y = bottomMargin;

		y += EditorConst.HEADER_FOOTER_MARGIN;
		gc.drawLine(leftMargin, y, rightMargin, y);
		y += EditorConst.HEADER_FOOTER_MARGIN;

		String footer = "Page " + (pageNo++);
		int wordWidth = gc.stringExtent(footer).x;

		x = leftMargin + (rightMargin - leftMargin + wordWidth) / 2;
		gc.drawString(footer, x, y, false);

		x = leftMargin;
		y += lineHeight;
	}

	private void printText() {
		printer.startPage();
		stringBuffer = new StringBuilder();

		x = leftMargin;
		y = topMargin;
		index = 0;
		end = textToPrint.length();
		pageNo = 1;

		while (index < end) {
			char c = textToPrint.charAt(index);
			index++;
			if (c != 0) {
				if (c == 0x0a || c == 0x0d) {
					if (c == 0x0d && index < end && textToPrint.charAt(index) == 0x0a) {
						index++; // if this is cr-lf, skip the lf
					}
					printWordBuffer();
					newline();
				} else {
					if (c != '\t') {
						stringBuffer.append(c);
					}
					if (Character.isWhitespace(c)) {
						printWordBuffer();
						if (c == '\t') {
							x += tabWidth;
						}
					}
				}
			}
		}
		if (stringBuffer.length() > 0) printWordBuffer();
		if (y + lineHeight <= bottomMargin) {
			printFooter();
			printer.endPage();
		}
	}

	private void printWordBuffer() {
		if (stringBuffer.length() > 0) {
			if (y == topMargin) printHeader();

			String word = stringBuffer.toString();
			int wordWidth = gc.stringExtent(word).x;
			if (x + wordWidth > rightMargin) {
				/* word doesn't fit on current line, so wrap */
				newline();
			}
			gc.drawString(word, x, y, false);
			x += wordWidth;
			stringBuffer = new StringBuilder();
		}
	}

	private void newline() {
		x = leftMargin;
		y += lineHeight;
		if (y + lineHeight > bottomMargin) {
			printFooter();
			printer.endPage();
			if (index + 1 < end) {
				y = topMargin;
				printer.startPage();
			}
		}
	}
}
