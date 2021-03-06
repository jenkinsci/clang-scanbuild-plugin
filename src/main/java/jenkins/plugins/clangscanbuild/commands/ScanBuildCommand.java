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
import hudson.model.Failure;
import hudson.util.ArgumentListBuilder;
import jenkins.plugins.clangscanbuild.CommandExecutor;

public class ScanBuildCommand implements Command{
	
	private String clangScanBuildPath;
	private FilePath projectDirectory;
    private FilePath clangOutputFolder;
    
    private String targetSdk;
    private String config = "Debug";
    
    private String target;
    
    private String additionalScanBuildArguments; // Passed directly to shell

    private String additionalXcodeBuildArguments; // Passed directly to shell

    private String workspace;
    private String scheme;
	
	public int execute( BuildContext context ) throws Exception {

		if( clangOutputFolder.exists() ){
			// this should never happen because this folder is in the build directory - famous last words
			context.log( "Deleting '" + getClangOutputFolder().getRemote() + "' contents from previous build." );
			clangOutputFolder.deleteContents();
		}else{
			clangOutputFolder.mkdirs();
		}

		ArgumentListBuilder args = new ArgumentListBuilder();
		args.add( getClangScanBuildPath() );
		
		args.add( "-k" ); // keep going on failure
		args.add( "-v" ); // verbose
		args.add( "-v" ); // even more verbose
		
		args.add( "-o" ); // output folder
		args.add( escapeSpacesInPath( clangOutputFolder.getRemote() ) );
		
		String additionalArgs = getAdditionalScanBuildArguments();
		if( isNotBlank( additionalArgs ) ){
			// This is a hack.  I can't call the standard ArgumentListBuilder.add() method because it checks for spaces with-in
			// the arg and quotes the arg if a space exists.  Since user's can pass commands like
			// '--use-cc=`which clang`' or multiple commands...we cannot allow the quotes to be 
			// inserted when spaces exist.  The ArgumentListBuilder.addTokenized() splits the arg on spaces and adds each piece 
			// which ends up reinserting the spaces when the command is assembled.
			args.addTokenized( additionalArgs );
		}
		
		args.add( "xcodebuild" );
		
		if( isNotBlank( getScheme() ) ){
            args.add( "-scheme", getScheme() );
			// Xcode workspace
            if( isNotBlank( getWorkspace() ) ) {
                args.add("-workspace", getWorkspace());
            }else{
                context.log("Using \'"+projectDirectory+"\' as workspace auto search directory");
            }

			if( isNotBlank( getTarget() ) ){
				context.log( "Ignoring build target '" + getTarget() + "' because scheme/workspace was provided" );
			}
		}else{
			// Xcode standalone project
			if( isBlank( getTarget() ) ){
				throw new Failure("No target specified");
			}
			args.add( "-target", getTarget() );
		}

		//These items can be provided with a target or can be used to override a workspace/scheme
		if( isNotBlank( getConfig() ) ) args.add( "-configuration", getConfig() );  // Defaults to Debug
		if( isNotBlank( getTargetSdk() ) ) args.add( "-sdk", getTargetSdk() );
		
		args.add( "clean" ); // clang scan requires a clean
		args.add( "analyze" );
                
		String additionalXcodeBuildArgs = getAdditionalXcodeBuildArguments();
		if( isNotBlank( additionalXcodeBuildArgs ) ){
			// This is a hack.  I can't call the standard ArgumentListBuilder.add() method because it checks for spaces with-in
			// the arg and quotes the arg if a space exists.  Since user's can pass commands like
			// '--use-cc=`which clang`' or multiple commands...we cannot allow the quotes to be 
			// inserted when spaces exist.  The ArgumentListBuilder.addTokenized() splits the arg on spaces and adds each piece 
			// which ends up reinserting the spaces when the command is assembled.
			args.addTokenized( additionalXcodeBuildArgs );
		}

		int rc = context.waitForProcess( getProjectDirectory(), args );

		if( rc == CommandExecutor.SUCCESS ){
			context.log( "XCODEBUILD SUCCESS" );
		}else{
			context.log( "XCODEBUILD ERROR" );
		}
		
		return rc;

	}

	private boolean isBlank( String value ){
		if( value == null ) return true;
		return value.trim().length() <= 0;
	}
	
	private String escapeSpacesInPath( String path ){
		if( path == null ) return "";
		return path.replaceAll( " ", "\\ " );
	}
	
	private boolean isNotBlank( String value ){
		return !isBlank( value );
	}
	
	public String getTargetSdk() {
		return targetSdk;
	}

	public void setTargetSdk(String targetSdk) {
		this.targetSdk = targetSdk;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public String getClangScanBuildPath() {
		return clangScanBuildPath;
	}

	public void setClangScanBuildPath(String clangScanBuildPath) {
		this.clangScanBuildPath = clangScanBuildPath;
	}

	public FilePath getProjectDirectory() {
		return projectDirectory;
	}

	public void setProjectDirectory(FilePath projectDirectory) {
		this.projectDirectory = projectDirectory;
	}

	public FilePath getClangOutputFolder() {
		return clangOutputFolder;
	}

	public void setClangOutputFolder(FilePath clangOutputFolder) {
		this.clangOutputFolder = clangOutputFolder;
	}

	public String getWorkspace() {
		return workspace;
	}

	public void setWorkspace(String workspace) {
		this.workspace = workspace;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public String getAdditionalScanBuildArguments() {
		return additionalScanBuildArguments;
	}

	public void setAdditionalScanBuildArguments(String additionalScanBuildArguments) {
		this.additionalScanBuildArguments = additionalScanBuildArguments;
	}

        public String getAdditionalXcodeBuildArguments() {
		return additionalXcodeBuildArguments;
	}

	public void setAdditionalXcodeBuildArguments(String additionalXcodeBuildArguments) {
		this.additionalXcodeBuildArguments = additionalXcodeBuildArguments;
	}

}
