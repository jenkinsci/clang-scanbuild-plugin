package jenkins.plugins.clangscanbuild.history;

import hudson.model.FreeStyleBuild;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;

import java.util.List;

import jenkins.plugins.clangscanbuild.actions.ClangScanBuildAction;
import jenkins.plugins.clangscanbuild.reports.GraphPoint;

import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

public class ClangScanBuildHistoryGathererImplTest extends HudsonTestCase{

	private ClangScanBuildHistoryGathererImpl classUnderTest = new ClangScanBuildHistoryGathererImpl( 5 );

	@Test
	public void testBuildSummaryForBuildsExceedingThresholdNotReturned() throws Exception{
		FreeStyleProject project = createFreeStyleProject( "Test Project" );
		
		// The test instance is set to a threshold of 5...builds 2 and 7 should be excluded
		performBuildWithClangAction( project, 1, "outputFolderName-1" );
		performBuildWithOutClangAction( project );
		FreeStyleBuild lastBuild = performBuildWithClangAction( project, 3, "outputFolderName-3" );

		List<GraphPoint> summaries = classUnderTest.gatherHistoryDataSet( lastBuild );
	
		Assert.assertEquals( 2, summaries.size() );
	}

	@Test
	public void testFirstBuildDoesNotFail() throws Exception{
		FreeStyleProject project = createFreeStyleProject( "Test Project" );
		
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
