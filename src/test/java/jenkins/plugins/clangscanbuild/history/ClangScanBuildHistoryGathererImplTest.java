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

import hudson.model.FreeStyleBuild;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;

import java.util.List;

import jenkins.plugins.clangscanbuild.actions.ClangScanBuildAction;
import jenkins.plugins.clangscanbuild.reports.GraphPoint;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class ClangScanBuildHistoryGathererImplTest {

	private ClangScanBuildHistoryGathererImpl classUnderTest = new ClangScanBuildHistoryGathererImpl( 5 );

	@Rule
	public JenkinsRule j = new JenkinsRule();

	@Test
	public void testBuildSummaryForBuildsExceedingThresholdNotReturned() throws Exception{
		FreeStyleProject project = j.createFreeStyleProject( "Test Project" );
		
		// The test instance is set to a threshold of 5...builds 2 and 7 should be excluded
		performBuildWithClangAction( project, 1, "outputFolderName-1" );
		performBuildWithOutClangAction( project );
		FreeStyleBuild lastBuild = performBuildWithClangAction( project, 3, "outputFolderName-3" );

		List<GraphPoint> summaries = classUnderTest.gatherHistoryDataSet( lastBuild );
	
		Assert.assertEquals( 2, summaries.size() );
	}

	@Test
	public void testFirstBuildDoesNotFail() throws Exception{
		FreeStyleProject project = j.createFreeStyleProject( "Test Project" );
		
		FreeStyleBuild build1 = performBuildWithClangAction( project, 1, "outputFolderName-1" );
		
		List<GraphPoint> summaries = classUnderTest.gatherHistoryDataSet( build1 );
		
		Assert.assertEquals( 1, summaries.size() );
	}
	
	private class TestClangScanBuildAction extends ClangScanBuildAction{

		public TestClangScanBuildAction( AbstractBuild<?,?> build, int bugCount, int threshold, String outputFolderName ){
			super( build, bugCount, true, threshold, null, outputFolderName );
		}

	}
	
	private FreeStyleBuild performBuildWithClangAction( FreeStyleProject project, int bugCount, String outputFolderName ) throws Exception {
		FreeStyleBuild build = project.scheduleBuild2(0).get();
		build.addAction( new TestClangScanBuildAction( build, bugCount, 0, outputFolderName ) );
		return build;
	}
	
	private FreeStyleBuild performBuildWithOutClangAction( FreeStyleProject project ) throws Exception {
		return project.scheduleBuild2(0).get();
	}

}
