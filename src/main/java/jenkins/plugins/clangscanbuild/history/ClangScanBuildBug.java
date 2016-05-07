/**
 * Copyright (c) 2011 Joshua Kennedy, http://deadmeta4.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins.plugins.clangscanbuild.history;

public class ClangScanBuildBug {

	public String reportFile;
	public String sourceFile;
	public String bugType;
	public String bugDescription;
	public String bugCategory;
	public String functionName;
	public String bugLine;
	public String bugColumn;
	public String bugPathLength;
	public boolean newBug;
	
	public boolean isNewBug() {
		return newBug;
	}
	public void setNewBug(boolean newBug) {
		this.newBug = newBug;
	}
	public String getBugCategory() {
		return bugCategory;
	}
	public void setBugCategory(String bugCategory) {
		this.bugCategory = bugCategory;
	}
	public String getBugType() {
		return bugType;
	}
	public void setBugType(String bugType) {
		this.bugType = bugType;
	}
	public String getBugDescription() {
		return bugDescription;
	}
	public void setBugDescription(String bugDescription) {
		this.bugDescription = bugDescription;
	}
	public String getReportFile() {
		return reportFile;
	}
	public void setReportFile(String reportFile) {
		this.reportFile = reportFile;
	}
	public String getSourceFile() {
		return sourceFile;
	}
	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}
	public String getFunctionName() {
		return functionName;
	}
	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}
	public String getBugLine() {
		return bugLine;
	}
	public void setBugLine(String BugLine) {
		this.bugLine = bugLine;
	}
	public String getBugColumn() {
		return bugColumn;
	}
	public void setBugColumn(String bugColumn) {
		this.bugColumn = bugColumn;
	}
	public String getBugPathLength() {
		return bugPathLength;
	}
	public void setBugPathLength(String bugPathLength) {
		this.bugPathLength = bugPathLength;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((bugCategory == null) ? 0 : bugCategory.hashCode());
		result = prime * result
				+ ((bugDescription == null) ? 0 : bugDescription.hashCode());
		result = prime * result + ((bugType == null) ? 0 : bugType.hashCode());
		result = prime * result
				+ ((reportFile == null) ? 0 : reportFile.hashCode());
		result = prime * result
				+ ((sourceFile == null) ? 0 : sourceFile.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClangScanBuildBug other = (ClangScanBuildBug) obj;
		if (bugCategory == null) {
			if (other.bugCategory != null)
				return false;
		} else if (!bugCategory.equals(other.bugCategory))
			return false;
		if (bugDescription == null) {
			if (other.bugDescription != null)
				return false;
		} else if (!bugDescription.equals(other.bugDescription))
			return false;
		if (bugType == null) {
			if (other.bugType != null)
				return false;
		} else if (!bugType.equals(other.bugType))
			return false;
		if (reportFile == null) {
			if (other.reportFile != null)
				return false;
		} else if (!reportFile.equals(other.reportFile))
			return false;
		if (sourceFile == null) {
			if (other.sourceFile != null)
				return false;
		} else if (!sourceFile.equals(other.sourceFile))
			return false;
		return true;
	}
	
}
