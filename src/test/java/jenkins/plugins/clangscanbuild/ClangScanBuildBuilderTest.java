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

import hudson.model.FreeStyleProject;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import com.gargoylesoftware.htmlunit.html.HtmlForm;

public class ClangScanBuildBuilderTest {
	@Rule
	public JenkinsRule j = new JenkinsRule();
	
	@Test
	public void testRoundTripConfiguration() throws Exception{
		
		FreeStyleProject p = j.createFreeStyleProject();
		
		ClangScanBuildBuilder builderBefore = new ClangScanBuildBuilder( "target", "sdk", "config", 
				"installName", "projPath", "workspace", "scheme", "someargs", "somexcodeargs", "someoutputfoldername" );
		p.getBuildersList().add( builderBefore );

		HtmlForm form = j.createWebClient().getPage( p, "configure" ).getFormByName( "config" );
		j.submit( form );

		ClangScanBuildBuilder builderAfter = p.getBuildersList().get( ClangScanBuildBuilder.class );

		j.assertEqualBeans( builderBefore, builderAfter, "target,config,targetSdk,xcodeProjectSubPath,workspace,scheme,scanbuildargs,xcodebuildargs,outputFolderName" );
	}
	
}
