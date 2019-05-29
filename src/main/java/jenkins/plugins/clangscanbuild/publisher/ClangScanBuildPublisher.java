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
package jenkins.plugins.clangscanbuild.publisher;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.*;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jenkins.plugins.clangscanbuild.ClangScanBuildUtils;
import jenkins.plugins.clangscanbuild.actions.ClangScanBuildAction;
import jenkins.plugins.clangscanbuild.actions.ClangScanBuildProjectAction;
import jenkins.plugins.clangscanbuild.history.ClangScanBuildBug;
import jenkins.plugins.clangscanbuild.history.ClangScanBuildBugSummary;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

public class ClangScanBuildPublisher extends Recorder implements SimpleBuildStep {

	private static final Logger LOGGER = Logger.getLogger( ClangScanBuildPublisher.class.getName() );

	@Extension
	public static final ClangScanBuildPublisherDescriptor DESCRIPTOR = new ClangScanBuildPublisherDescriptor();

	private static final Pattern BUG_TYPE_PATTERN = Pattern.compile( "<!--\\sBUGTYPE\\s(.*)\\s-->" );
	private static final Pattern BUG_DESC_PATTERN = Pattern.compile( "<!--\\sBUGDESC\\s(.*)\\s-->" );
	private static final Pattern BUGFILE_PATTERN = Pattern.compile( "<!--\\sBUGFILE\\s(.*)\\s-->" );
	private static final Pattern BUGCATEGORY_PATTERN = Pattern.compile( "<!--\\sBUGCATEGORY\\s(.*)\\s-->" );
	private static final Pattern FUNCTIONNAME_PATTERN = Pattern.compile( "<!--\\sFUNCTONNAME\\s(.*)\\s-->" );
	private static final Pattern BUGLINE_PATTERN = Pattern.compile( "<!--\\sBUGLINE\\s(.*)\\s-->" );
	private static final Pattern BUGCOLUMN_PATTERN = Pattern.compile( "<!--\\sBUGCOLUMN\\s(.*)\\s-->" );
	private static final Pattern BUGPATHLENGTH_PATTERN = Pattern.compile( "<!--\\sBUGPATHLENGTH\\s(.*)\\s-->" );

	private int bugThreshold;
	private String clangexcludedpaths; 
	private String reportFolderName;

	private boolean markBuildUnstableWhenThresholdIsExceeded;

	@DataBoundConstructor
	public ClangScanBuildPublisher( 
			boolean markBuildUnstableWhenThresholdIsExceeded, 
			int bugThreshold,
			String clangexcludedpaths,
			String reportFolderName
      ){
		this.markBuildUnstableWhenThresholdIsExceeded = markBuildUnstableWhenThresholdIsExceeded;
		this.bugThreshold = bugThreshold;
		this.clangexcludedpaths = Util.fixNull(clangexcludedpaths);
		this.reportFolderName = Util.fixNull(reportFolderName);
	}

	public int getBugThreshold() {
		return bugThreshold;
	}

	public boolean isMarkBuildUnstableWhenThresholdIsExceeded(){
		return markBuildUnstableWhenThresholdIsExceeded;
	}

	public String getReportFolderName(){
		return reportFolderName;
	}

	public String getClangexcludedpaths(){
		return clangexcludedpaths;
	}
	
	@Override
	public Action getProjectAction(AbstractProject<?, ?> project ){
		return new ClangScanBuildProjectAction( project );
	}

	@Override
	public ClangScanBuildPublisherDescriptor getDescriptor() {
		return DESCRIPTOR;
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.BUILD;
	}

	@Override
	public void perform(@Nonnull Run<?, ?> build, @Nonnull FilePath workspace, @Nonnull Launcher launcher,
						@Nonnull TaskListener listener) throws InterruptedException, IOException {
		listener.getLogger().println( "Publishing Clang scan-build results" );

		// Expand build variables in the reportFolderName
		EnvVars env;
		if (build instanceof AbstractBuild) {
			env = build.getEnvironment(listener);
			env.overrideAll(((AbstractBuild<?,?>) build).getBuildVariables());
		} else {
			env = new EnvVars();
		}
		String expandedReportFolderName = env.expand(reportFolderName);

		FilePath reportOutputFolder = new FilePath(workspace, expandedReportFolderName);
		FilePath reportMasterOutputFolder = ClangScanBuildUtils.locateClangScanBuildReportFolder(build, expandedReportFolderName);

		// This copies the reports out of the generate date sub folder to the root of the reports folder and then deletes the clang generated folder
		copyClangReportsOutOfGeneratedSubFolder( reportOutputFolder, listener );

		// This copies the report dir to master
		copyClangReportsToMaster( reportOutputFolder, reportMasterOutputFolder, listener );

		// this digs into the clang results looking for the subfolder created by clang
		List<FilePath> clangReports = locateClangBugReports( reportOutputFolder );

		// this loads the previous bug summary for the last build.  it is need to identify bugs added since last build
		ClangScanBuildBugSummary previousBugSummary = getBugSummaryForLastBuild( build );

		// this builds and new bug summary and populates it with bugs
		ClangScanBuildBugSummary newBugSummary = new ClangScanBuildBugSummary( build.number );

		String[] tokens = new String[0];
		if(getClangexcludedpaths().length() > 0){
			tokens = getClangexcludedpaths().split(",");
		}

		for( FilePath report : clangReports ){
			// bugs are parsed inside this method:
			ClangScanBuildBug bug = createBugFromClangScanBuildHtml( build.getParent().getName(), report, previousBugSummary, workspace.getRemote() );
			boolean validBug = true;
			for(String token:tokens){
				String trimmedToken = token.trim().toLowerCase();
				if(bug.sourceFile.toLowerCase().contains(trimmedToken)){
					listener.getLogger().println( "Skipping file: " + bug.sourceFile + " because it matches exclusion pattern: " + trimmedToken );
					validBug = false;
					break;
				}
			}
			if(validBug) {
				newBugSummary.add( bug );
			}
		}

		// this line dumps a bugSummary.xml file to the build artifacts.  did this instead of using job config xml for performance
		//FilePath bugSummaryXMLFile = new FilePath( reportOutputFolder, "bugSummary.xml" );
		FilePath bugSummaryXMLFile = new FilePath( new FilePath( build.getRootDir() ), "bugSummary.xml" );
		String bugSummaryXML = AbstractBuild.XSTREAM.toXML( newBugSummary );
		bugSummaryXMLFile.write( bugSummaryXML, "UTF-8" );

		// this adds a build actions which records the bug count into the build results.  This count is used to generate the trend charts
		final ClangScanBuildAction action = new ClangScanBuildAction( build, newBugSummary.getBugCount(), markBuildUnstableWhenThresholdIsExceeded, bugThreshold, bugSummaryXMLFile, expandedReportFolderName );
        build.addAction( action );

        // this checks if the build should be failed due to an increase in bugs
        if( action.buildFailedDueToExceededThreshold() ){
        	listener.getLogger().println( "Clang scan-build threshhold exceeded." );
            build.setResult( Result.UNSTABLE );
        }
	}

	private ClangScanBuildBug createBugFromClangScanBuildHtml( String projectName, FilePath report, ClangScanBuildBugSummary previousBugSummary, String workspacePath ) throws InterruptedException {
		ClangScanBuildBug bug = createBugInstance( projectName, report, workspacePath );

		// this checks to see if the bug is new since the last build
		if(bug != null && previousBugSummary != null ){
			// This marks bugs as new if they did not exist in the last build report
			bug.setNewBug( !previousBugSummary.contains( bug ) );
		}
		return bug;
	}

	private ClangScanBuildBugSummary getBugSummaryForLastBuild(Run<?, ?> build) {
		if( build.getPreviousBuild() != null ){
			ClangScanBuildAction previousAction = build.getPreviousBuild().getAction( ClangScanBuildAction.class );
			if( previousAction != null ){
				return previousAction.loadBugSummary();
			}
		}
		return null;
	}

	/**
	 * Clang always creates a subfolder within the specified output folder that has a unique name.
         *
         * The report summary uses all report-*.html files in the scan-build output folder to generate
         * the bug entries. Scan build may generate multiple dated folders with reports in for a
         * build.
         *
         * Only the reports in the build archive folder are published so it is therefore necessary
         * to copy the reports from all the dated folders into the reports folder.
         *
	 * This method locates the subfolders of the output folder and copies their contents
	 * to the build archive folder.
	 */
	private void copyClangReportsOutOfGeneratedSubFolder(FilePath reportsFolder, TaskListener listener ){
		try{
			List<FilePath> subFolders = reportsFolder.listDirectories();
			if( subFolders.isEmpty() ){
				listener.getLogger().println( "Could not locate a unique scan-build output folder in: " + reportsFolder );
				return;
			}

			for (FilePath clangDataFolder : subFolders) {
                            clangDataFolder.copyRecursiveTo( reportsFolder );
                            clangDataFolder.deleteRecursive();
                        }
		}catch( Exception e ){
			listener.fatalError( "Unable to copy Clang scan-build output (" + reportsFolder + ") to build archive folder." );
		}
	}
	/**
	 * Copy clang output folder to have access to html files from the master
	 */
	private void copyClangReportsToMaster(FilePath reportsFolder, FilePath materPath, TaskListener listener ){
		try{
			reportsFolder.copyRecursiveTo( materPath );
		}catch( Exception e ){
			listener.fatalError( "Unable to copy Clang scan-build output to master." );
		}
	}

	/**
	 * This method creates a bug instance from scan-build HMTL report.  It does this by using reg-ex searches
	 * to located HTML comments in the file which are easily parseable and appear in every HTML bug report from
	 * scan-build.  If scan-build ever adds an XML option, this functionality can be replaced with an XML parsing
	 * routine.
	 */
	private ClangScanBuildBug createBugInstance( String projectName, FilePath report, String workspacePath) throws InterruptedException {
		// the report parameter is the file the points to an HTML report generated by clang
		ClangScanBuildBug instance = new ClangScanBuildBug();
		String basePath = "StaticAnalyzer/";
		String reportPath = report.getRemote();

		int baseIdx = reportPath.indexOf(basePath);
		if(baseIdx > -1) {
			reportPath = reportPath.substring(baseIdx, reportPath.length());
		} else {
			reportPath = report.getName();
		}

		instance.setReportFile( reportPath );//( report.getName() );

		String contents = null;
		try {
			// this code digs into the HTML report content to locate the bug markers using regex
			contents = report.readToString();
			instance.setBugDescription( getMatch( BUG_DESC_PATTERN, contents ) );
			instance.setBugType( getMatch( BUG_TYPE_PATTERN, contents ) );
			instance.setBugCategory( getMatch( BUGCATEGORY_PATTERN, contents ) );
                        instance.setFunctionName( getMatch( FUNCTIONNAME_PATTERN, contents ) );
                        instance.setBugLine( getMatch( BUGLINE_PATTERN, contents ) );
                        instance.setBugColumn( getMatch( BUGCOLUMN_PATTERN, contents ) );
                        instance.setBugPathLength( getMatch( BUGPATHLENGTH_PATTERN, contents ) );

			String sourceFile = getMatch( BUGFILE_PATTERN, contents );

			// This attempts to shorten the file path by removing the workspace path and
			// leaving only the path relative to the workspace.
			int position = sourceFile.lastIndexOf( workspacePath );
			if( position >= 0 ){
				sourceFile = sourceFile.substring( position + workspacePath.length() );
			}

			instance.setSourceFile( sourceFile );
		}catch( IOException e ){
			LOGGER.log( Level.ALL, "Unable to read file or locate clang markers in content: " + report );
		}

		return instance;
	}

	private String getMatch( Pattern pattern, String contents ){
		Matcher matcher = pattern.matcher( contents );
		while( matcher.find() ){
			return matcher.group(1);
		}
		return null;
	}

	/**
	 * This locates all the generated HTML bug reports from scan-build and returns them as a list.
	 */
	private List<FilePath> locateClangBugReports( FilePath clangOutputFolder ) throws IOException, InterruptedException {
		List<FilePath> files = new ArrayList<FilePath>();
		if( !clangOutputFolder.exists() ) return files;
        files.addAll( Arrays.asList( clangOutputFolder.list( "**/report-*.html" ) ) );
        return files;
	}
}
