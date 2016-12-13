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
package jenkins.plugins.clangscanbuild;

import hudson.FilePath;
import hudson.model.AbstractBuild;


public class ClangScanBuildUtils{
	public static final String SHORTNAME = "clang-scanbuild";
	public static final String REPORT_OUTPUT_FOLDERNAME = "clangScanBuildReports";
	
	public static String getIconsPath(){
		return "/plugin/" + SHORTNAME + "/icons/";
	}
	
	public static String getTransparentImagePath(){
		return "/plugin/" + SHORTNAME + "/transparent.png";
	}
	
	public static FilePath locateClangScanBuildReportFolder( AbstractBuild<?,?> build, String folderName ){
		if( build == null ) return null;
		return new FilePath( new FilePath( build.getRootDir() ), folderName );
	}
	
}
