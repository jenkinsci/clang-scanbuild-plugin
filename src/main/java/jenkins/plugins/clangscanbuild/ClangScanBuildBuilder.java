package jenkins.plugins.clangscanbuild;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.Computer;
import hudson.tasks.Builder;

import java.io.IOException;

import jenkins.plugins.clangscanbuild.commands.BuildContextImpl;
import jenkins.plugins.clangscanbuild.commands.ScanBuildCommand;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * This builder provides a new build step for freestyle jobs.  Users can 
 * execute Clang scan-build against their XCode projects
 *
 * @author Joshua Kennedy
 */
public class ClangScanBuildBuilder extends Builder{
	
	@Extension
    public static final ClangScanBuildDescriptor DESCRIPTOR = new ClangScanBuildDescriptor();

	/**
	 * Each of the fields below corresponds to a field in the config.jelly file.
	 * The data for these fields is provided by the user on the build-step on the 
	 * job configuration screen.
	 */
	private String target;
    private String targetSdk;
    private String config;
    private String clangInstallationName;
    private String xcodeProjectSubPath;
    private String workspace;
    private String scheme;
    private String scanbuildargs;
    private String xcodebuildargs;
	private String outputFolderName;

    @DataBoundConstructor
    public ClangScanBuildBuilder( 
    		String target, 
    		String targetSdk, 
    		String config, 
    		String clangInstallationName,
    		String xcodeProjectSubPath,
    		String workspace,
    		String scheme,
    		String scanbuildargs,
    		String xcodebuildargs,
    		String outputFolderName){
    	
        this.target = Util.fixEmptyAndTrim( target );
        this.targetSdk = Util.fixEmptyAndTrim( targetSdk );
        this.config = Util.fixEmptyAndTrim( config );     
        this.clangInstallationName = Util.fixEmptyAndTrim( clangInstallationName );
        this.xcodeProjectSubPath = Util.fixEmptyAndTrim( xcodeProjectSubPath );
        this.workspace = Util.fixEmptyAndTrim( workspace );
        this.scheme = Util.fixEmptyAndTrim( scheme );
        this.scanbuildargs = Util.fixEmptyAndTrim( scanbuildargs );
        this.xcodebuildargs = Util.fixEmptyAndTrim( xcodebuildargs );
		this.outputFolderName = Util.fixEmptyAndTrim( outputFolderName );
    }

    public String getClangInstallationName(){
    	return clangInstallationName;
    }
    
    public String getTarget() {
		return target;
	}

	public String getTargetSdk() {
		return targetSdk;
	}

	public String getConfig() {
		return config;
	}
	
	public String getWorkspace(){
		return workspace;
	}
	
	public String getScheme(){
		return scheme;
	}
	
	public String getScanbuildargs(){
		return scanbuildargs;
	}

        public String getXcodebuildargs(){
		return xcodebuildargs;
	}

	public String getOutputFolderName(){
		return outputFolderName;
	}

	/**
	 * Removing slashes here in case the user adds a starting slash to the path.
	 */
	public String getXcodeProjectSubPath(){
		if( xcodeProjectSubPath == null ) return null;
		if( xcodeProjectSubPath.startsWith("/") || xcodeProjectSubPath.startsWith("\\") ){
			return xcodeProjectSubPath.substring(1);
		}
		return xcodeProjectSubPath;
	}
	
	/**
	 * This method is invoked when a job is actually executed.  It is the magic method.
	 * @return boolean - if 'false', build will be aborted
	 */
	@Override
    public boolean perform( @SuppressWarnings("rawtypes") AbstractBuild build, Launcher launcher, BuildListener listener ) throws IOException, InterruptedException {
		
		ClangScanBuildToolInstallation clangInstallation = DESCRIPTOR.getNamedInstallation( getClangInstallationName() );
		if( clangInstallation == null ){
			// somehow config has gotten out of whack.  User has a named clang installation that no longer exists in
			// the master hudson config.  We need it to get the path to clang.
			listener.fatalError( "Unable to locate the clang scan-build installation named '" + getClangInstallationName() + "'.  Please confirm a clang installation named '" + getClangInstallationName() + "' is defined in the jenkins global config and on each slave if the location is different than the master. " );
			return false;
		}

		// Convert the clang installation to the node specific clang installation
		clangInstallation = clangInstallation.forNode( Computer.currentComputer().getNode(), listener );
		EnvVars env = build.getEnvironment(listener);
		clangInstallation = clangInstallation.forEnvironment( env );
		
		ScanBuildCommand xcodebuild = new ScanBuildCommand();
		xcodebuild.setTarget( getTarget() );
		xcodebuild.setTargetSdk( getTargetSdk() );
		xcodebuild.setConfig( getConfig() );
		xcodebuild.setAdditionalScanBuildArguments( getScanbuildargs() );
		xcodebuild.setAdditionalXcodeBuildArguments( getXcodebuildargs() );
		xcodebuild.setClangOutputFolder( new FilePath( build.getWorkspace(), getOutputFolderName()) );
		xcodebuild.setWorkspace( getWorkspace() );
		xcodebuild.setScheme( getScheme() );
		
		if( getXcodeProjectSubPath() != null ){
			xcodebuild.setProjectDirectory( new FilePath( build.getWorkspace(), getXcodeProjectSubPath() ) );
		}else{
			xcodebuild.setProjectDirectory( build.getWorkspace() );
		}
		
		try {
			String path = clangInstallation.getExecutable( launcher ) ;
			if( path == null ){
				listener.fatalError( "Unable to locate 'scan-build' within '" + clangInstallation.getHome() + "' as configured in clang installation named '" + clangInstallation.getName() + "' in the global config." );
				return false;
			}
			xcodebuild.setClangScanBuildPath( path );
		} catch ( Exception e) {
			listener.fatalError( "Unable to locate 'scan-build' within '" + clangInstallation.getHome() + "' as configured in clang installation named '" + clangInstallation.getName() + "' in the global config.", e );
			return false;
		}
		
		int rc = CommandExecutor.execute( xcodebuild ).withContext( new BuildContextImpl( build, launcher, listener ) );
        return rc == CommandExecutor.SUCCESS;
    }

    public ClangScanBuildDescriptor getDescriptor() {
    	return DESCRIPTOR;
    }
    
}

