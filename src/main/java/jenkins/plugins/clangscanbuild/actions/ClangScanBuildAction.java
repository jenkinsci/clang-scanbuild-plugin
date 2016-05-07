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
package jenkins.plugins.clangscanbuild.actions;

import hudson.FilePath;
import hudson.model.Action;
import hudson.model.ModelObject;
import hudson.model.AbstractBuild;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.logging.Logger;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Level.FINEST;

import jenkins.plugins.clangscanbuild.ClangScanBuildUtils;
import jenkins.plugins.clangscanbuild.history.ClangScanBuildBugSummary;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * This contributes the menu to the left used to access reports/whatever from inside a 
 * particular job's results.  This is called a "BuildAction" because it contributes
 * a link to the left and a URL to a specific build.
 * 
 * @author Josh Kennedy
 */
public class ClangScanBuildAction implements Action, StaplerProxy, ModelObject{
	private static final Logger LOGGER = Logger.getLogger(ClangScanBuildAction.class.getName());
	
	public static final String BUILD_ACTION_URL_NAME = "clangScanBuildBugs";
	private int bugThreshold;
	private FilePath bugSummaryXML;
	private boolean markBuildUnstable;
	private int bugCount;
	private String outputFolderName;
	
	private Pattern APPROVED_REPORT_REQUEST_PATTERN = Pattern.compile( "[^.\\\\/]*\\.html|StaticAnalyzer.*\\.html" );
	
	public ClangScanBuildAction( AbstractBuild<?,?> build, int bugCount, boolean markBuildUnstable, 
			int bugThreshold, FilePath bugSummaryXML, String outputFolderName ){
		this.bugThreshold = bugThreshold;
		this.bugCount = bugCount;
		this.bugSummaryXML = bugSummaryXML;
		this.markBuildUnstable = markBuildUnstable;
		this.build = build;
		this.outputFolderName = outputFolderName;
	}

	public AbstractBuild<?,?> build;
	
	public boolean buildFailedDueToExceededThreshold(){
		if( !markBuildUnstable ) return false;
		return getBugCount() > bugThreshold;
	}
	
	public int getBugThreshhold(){
		return bugThreshold;
	}
	
	/**
	 * The only thing stored in the actual builds in the bugCount and bugThreshold.  This was done in order to make the
	 * build XML smaller to reduce load times.  The counts are need in order to render the trend charts.
	 * 
	 * This method actually loads the XML file that was generated at build time and placed alongside the clang output files
	 * This XML contains the list of bugs and is used to render the report which links to the clang files.
	 * 
	 * DON'T CALL THIS UNLESS YOU NEED THE ACTUAL BUG SUMMARY
	 */
	public ClangScanBuildBugSummary loadBugSummary(){
		if( bugSummaryXML == null ) return null;
		
		try{
		    if( bugSummaryXML.length() != 0 )
		    {
			    return (ClangScanBuildBugSummary) AbstractBuild.XSTREAM.fromXML( bugSummaryXML.read() );
		    }
		    else
		    {
		        return null;
		    }
	    }catch( java.lang.InterruptedException ie ){
			LOGGER.log(FINEST, "", ie);
			return null;
		}catch( IOException ioe ){
			LOGGER.log(FINEST, "", ioe);
			return null;
		}
	}
	
	public int getBugCount(){
		return bugCount;
	}
	
	/**
	 * Indicates which icon should be displayed next to the link
	 */
	@Override
	public String getIconFileName() {
		return ClangScanBuildUtils.getIconsPath() + "scanbuild-32x32.png";
	}

	/**
	 * Title of link display on job results screen.
	 */
	@Override
	public String getDisplayName() {
		return "Clang scan-build bugs";
	}

	/**
	 * This object will be reference if a request comes into the following url:
	 * http://[jenkins]/job/[job name]/[job number]/clangBugReport
	 */
	@Override
	public String getUrlName() {
		return BUILD_ACTION_URL_NAME;
	}

	/**
	 * This method needs to return the object that is responsible
	 * for handling web requests.  This file defines a new url
	 * strategy to use which can provide custom urls for this plugin
	 * 
	 */
	@Override
	public Object getTarget(){
		return this;
	}
	
	/**
	 * This method is used to serve up report HTML files from the hidden build folder.  It essentially exposes
	 * the reports to the web.
	 */
    public void doBrowse( StaplerRequest req, StaplerResponse rsp ) throws IOException {
    	
    	String requestedPath = trimFirstSlash( req.getRestOfPath() );
    	if( requestedPath == null ) rsp.sendError( 404 );
    
    	if( !APPROVED_REPORT_REQUEST_PATTERN.matcher( requestedPath ).matches() ){
    		LOGGER.log(FINEST, "Someone is requesting unapproved content: %s", requestedPath);
    		rsp.sendError( 404 );
    		return;
    	}
    	
    	FilePath reports = ClangScanBuildUtils.locateClangScanBuildReportFolder( build, outputFolderName );
    	FilePath requestedFile = new FilePath( reports, trimFirstSlash( requestedPath ) );
    	
    	try{
	    	if( !requestedFile.exists() ){
	    		LOGGER.log(FINEST, "Unable to locate report: %s", req.getRestOfPath());
	    		rsp.sendError( 404 );
	    		return;
	    	}
	    	rsp.serveFile( req, requestedFile.toURI().toURL() );
    	}catch( Exception e ){
    		LOGGER.log(FINEST, "FAILED TO SERVE FILE: %s -> %s", new Object[]{req.getRestOfPath(), e.getLocalizedMessage()});
    		rsp.sendError( 500 );
    	}

    }
    
    private String trimFirstSlash( String path ){
    	if( path == null ) return null;
    	if( !path.startsWith("/") ) return path.trim();
    	return path.substring(1).trim();
    }
    
}
