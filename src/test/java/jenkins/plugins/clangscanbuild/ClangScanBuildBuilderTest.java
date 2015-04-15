package jenkins.plugins.clangscanbuild;

import hudson.model.FreeStyleProject;

import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

import com.gargoylesoftware.htmlunit.html.HtmlForm;

public class ClangScanBuildBuilderTest extends HudsonTestCase{
	
	@Test
	public void testRoundTripConfiguration() throws Exception{
		
		FreeStyleProject p = createFreeStyleProject();
		
		ClangScanBuildBuilder builderBefore = new ClangScanBuildBuilder( "target", "sdk", "config", "installName", "projPath", "workspace", "xcode", "scheme", "someargs", "somexcodeargs" );
		p.getBuildersList().add( builderBefore );

		HtmlForm form = createWebClient().getPage( p, "configure" ).getFormByName( "config" );
		submit( form );

		ClangScanBuildBuilder builderAfter = p.getBuildersList().get( ClangScanBuildBuilder.class );

		assertEqualBeans( builderBefore, builderAfter, "target,config,targetSdk,xcodeProjectSubPath,workspace,scheme,scanbuildargs,buildargs" );
	}
	
}
