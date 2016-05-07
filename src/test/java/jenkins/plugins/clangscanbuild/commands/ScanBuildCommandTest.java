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
package jenkins.plugins.clangscanbuild.commands;

import hudson.FilePath;
import hudson.util.ArgumentListBuilder;

import java.io.File;

import org.junit.Assert;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Test;

public class ScanBuildCommandTest{

	private BuildContext context = EasyMock.createMock( BuildContext.class ); 
	
	@Test
	public void onlyRequiredOptionsSet() throws Exception{
		ScanBuildCommand command = new ScanBuildCommand();
		command.setClangOutputFolder( new FilePath( new File( "OutputFolder" ) ) );
		command.setClangScanBuildPath( "/ScanBuild" );
		command.setProjectDirectory( new FilePath( new File( "/ProjectDir" ) ) );
		command.setTarget( "myTarget" );

		String actual = buildCommandAndReturn( command );
		
		String expected = "/ScanBuild -k -v -v -o OutputFolder xcodebuild -target myTarget -configuration Debug clean analyze";
		Assert.assertEquals( expected, actual );
	}
	
	@Test
	public void xcode4WorkspaceSet() throws Exception{
		// XCode 4 workspace/scheme should override unnecessary target
		ScanBuildCommand command = new ScanBuildCommand();
		command.setClangOutputFolder( new FilePath( new File( "OutputFolder" ) ) );
		command.setClangScanBuildPath( "/ScanBuild" );
		command.setConfig( "myConfig" );
		command.setProjectDirectory( new FilePath( new File( "/ProjectDir" ) ) );
		command.setScheme( "myScheme" );
		command.setTarget( "myTarget" );
		command.setTargetSdk( "myTargetSdk" );
		command.setWorkspace( "myWorkspace" );

		String actual = buildCommandAndReturn( command );
		
		String expected = "/ScanBuild -k -v -v -o OutputFolder xcodebuild -scheme myScheme -workspace myWorkspace -configuration myConfig -sdk myTargetSdk clean analyze";
		Assert.assertEquals( expected, actual );
	}
	
	@Test
	public void xcode3TargetSet() throws Exception{
		// XCode 4 workspace/scheme should override unnecessary target
		ScanBuildCommand command = new ScanBuildCommand();
		command.setClangOutputFolder( new FilePath( new File( "OutputFolder" ) ) );
		command.setClangScanBuildPath( "/ScanBuild" );
		command.setConfig( "myConfig" );
		command.setProjectDirectory( new FilePath( new File( "/ProjectDir" ) ) );
		command.setTarget( "myTarget" );
		command.setTargetSdk( "myTargetSdk" );
		
		String actual = buildCommandAndReturn( command );
		
		String expected = "/ScanBuild -k -v -v -o OutputFolder xcodebuild -target myTarget -configuration myConfig -sdk myTargetSdk clean analyze";
		Assert.assertEquals( expected, actual );
	}
	
	private String buildCommandAndReturn( ScanBuildCommand command ) throws Exception{
		context.log( (String) EasyMock.notNull() );
		EasyMock.expectLastCall().anyTimes();
		
		Capture<ArgumentListBuilder> argumentListCapture = new Capture<ArgumentListBuilder>();
		EasyMock.expect( context.waitForProcess( EasyMock.same( command.getProjectDirectory() ), EasyMock.capture( argumentListCapture ) ) ).andReturn( 0 );
		
		EasyMock.replay( context );
		command.execute( context );
		EasyMock.verify( context );
		
		return argumentListCapture.getValue().toStringWithQuote();
	}
	
	@Test
	public void xcode4WorkspaceSetWithSingleScanBuildArgument() throws Exception{
		// XCode 4 workspace/scheme should override unnecessary target
		ScanBuildCommand command = new ScanBuildCommand();
		command.setClangOutputFolder( new FilePath( new File( "OutputFolder" ) ) );
		command.setClangScanBuildPath( "/ScanBuild" );
		command.setConfig( "myConfig" );
		command.setProjectDirectory( new FilePath( new File( "/ProjectDir" ) ) );
		command.setScheme( "myScheme" );
		command.setTarget( "myTarget" );
		command.setTargetSdk( "myTargetSdk" );
		command.setWorkspace( "myWorkspace" );
		command.setAdditionalScanBuildArguments( "--use-cc=`which clang`" );

		String actual = buildCommandAndReturn( command );
		
		// Jenkins core quotes this due to the space in between 'which' and 'clang' .  Not sure if this is OK or not... :(
		String expected = "/ScanBuild -k -v -v -o OutputFolder --use-cc=`which clang` xcodebuild -scheme myScheme -workspace myWorkspace -configuration myConfig -sdk myTargetSdk clean analyze";
		Assert.assertEquals( expected, actual );
	}

        	@Test
	public void xcode4WorkspaceSetWithSingleXcodeBuildArgument() throws Exception{
		// XCode 4 workspace/scheme should override unnecessary target
		ScanBuildCommand command = new ScanBuildCommand();
		command.setClangOutputFolder( new FilePath( new File( "OutputFolder" ) ) );
		command.setClangScanBuildPath( "/ScanBuild" );
		command.setConfig( "myConfig" );
		command.setProjectDirectory( new FilePath( new File( "/ProjectDir" ) ) );
		command.setScheme( "myScheme" );
		command.setTarget( "myTarget" );
		command.setTargetSdk( "myTargetSdk" );
		command.setWorkspace( "myWorkspace" );
		command.setAdditionalXcodeBuildArguments("VALID_ARCHS=i386" );

		String actual = buildCommandAndReturn( command );
		
		// Jenkins core quotes this due to the space in between 'which' and 'clang' .  Not sure if this is OK or not... :(
		String expected = "/ScanBuild -k -v -v -o OutputFolder xcodebuild -scheme myScheme -workspace myWorkspace -configuration myConfig -sdk myTargetSdk clean analyze VALID_ARCHS=i386";
		Assert.assertEquals( expected, actual );
	}

        @Test
	public void xcode4WorkspaceSetWithMultipleScanBuildArguments() throws Exception{
		// XCode 4 workspace/scheme should override unnecessary target
		ScanBuildCommand command = new ScanBuildCommand();
		command.setClangOutputFolder( new FilePath( new File( "OutputFolder" ) ) );
		command.setClangScanBuildPath( "/ScanBuild" );
		command.setConfig( "myConfig" );
		command.setProjectDirectory( new FilePath( new File( "/ProjectDir" ) ) );
		command.setScheme( "myScheme" );
		command.setTarget( "myTarget" );
		command.setTargetSdk( "myTargetSdk" );
		command.setWorkspace( "myWorkspace" );
		command.setAdditionalScanBuildArguments( "-h -x somevalue" );

		String actual = buildCommandAndReturn( command );
		
		String expected = "/ScanBuild -k -v -v -o OutputFolder -h -x somevalue xcodebuild -scheme myScheme -workspace myWorkspace -configuration myConfig -sdk myTargetSdk clean analyze";
		Assert.assertEquals( expected, actual );
	}

        @Test
	public void xcode4WorkspaceSetWithMultipleXcodeBuildArguments() throws Exception{
		// XCode 4 workspace/scheme should override unnecessary target
		ScanBuildCommand command = new ScanBuildCommand();
		command.setClangOutputFolder( new FilePath( new File( "OutputFolder" ) ) );
		command.setClangScanBuildPath( "/ScanBuild" );
		command.setConfig( "myConfig" );
		command.setProjectDirectory( new FilePath( new File( "/ProjectDir" ) ) );
		command.setScheme( "myScheme" );
		command.setTarget( "myTarget" );
		command.setTargetSdk( "myTargetSdk" );
		command.setWorkspace( "myWorkspace" );
		command.setAdditionalScanBuildArguments( "-h -x somevalue" );
                command.setAdditionalXcodeBuildArguments("THIS=1 THAT=2");

		String actual = buildCommandAndReturn( command );
		
		String expected = "/ScanBuild -k -v -v -o OutputFolder -h -x somevalue xcodebuild -scheme myScheme -workspace myWorkspace -configuration myConfig -sdk myTargetSdk clean analyze THIS=1 THAT=2";
		Assert.assertEquals( expected, actual );
	}
	
}
