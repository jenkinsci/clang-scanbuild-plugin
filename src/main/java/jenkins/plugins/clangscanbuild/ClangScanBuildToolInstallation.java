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

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.EnvironmentSpecific;
import hudson.model.TaskListener;
import hudson.model.Node;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolProperty;
import hudson.tools.ToolInstallation;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.util.List;

import jenkins.model.Jenkins;
import jenkins.security.MasterToSlaveCallable;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class ClangScanBuildToolInstallation extends ToolInstallation implements NodeSpecific<ClangScanBuildToolInstallation>, EnvironmentSpecific<ClangScanBuildToolInstallation>{

	private static final long serialVersionUID = -2485377492741518511L;
	
	@DataBoundConstructor
    public ClangScanBuildToolInstallation( 
    		String name, 
    		String home,
    		List<? extends ToolProperty<?>> properties ){
		
		super( Util.fixEmptyAndTrim( name ), 
			   removeTrailingSlashes( Util.fixEmptyAndTrim( home ) ),
			   properties );
    }

    private static String removeTrailingSlashes( String home ) {
    	if( home == null ) return "";
        if( home.endsWith( "/" ) || home.endsWith( "\\" ) ){
            return home.substring( 0, home.length() - 1 );
        } else {
            return home;
        }
    }

    public String getExecutable( Launcher launcher ) throws IOException, InterruptedException {
        return launcher.getChannel().call( new MasterToSlaveCallable<String,IOException>() {

			private static final long serialVersionUID = 5437036131007277280L;

			public String call() throws IOException {
                File scanbuild = new File( getHome(), "scan-build" );
                if( scanbuild.exists() ) return scanbuild.getPath();
                return null;
            }
        	
        });
    }

    public ClangScanBuildToolInstallation forEnvironment( EnvVars environment ){
        return new ClangScanBuildToolInstallation( getName(), getHome(), getProperties().toList() );
    }

    public ClangScanBuildToolInstallation forNode( Node node, TaskListener log ) throws IOException, InterruptedException {
        return new ClangScanBuildToolInstallation( getName(), translateFor( node, log ), getProperties().toList() );
    }

    @Extension
    public static class ClangStaticAnalyzerToolDescriptor extends ToolDescriptor<ClangScanBuildToolInstallation> {

        @Override
        public String getDisplayName() {
            return "Clang Static Analyzer";
        }

        @Override
        public ClangScanBuildToolInstallation[] getInstallations() {
            return locateMainDescriptor().getInstallations();
        }

        @Override
        public void setInstallations( ClangScanBuildToolInstallation... installations ){
        	locateMainDescriptor().setInstallations( installations );
        }
        
        private ClangScanBuildDescriptor locateMainDescriptor(){
        	return Jenkins.getInstance().getDescriptorByType( ClangScanBuildDescriptor.class );
        }

        /**
         * Checks if the clang directory is valid.
         */
        public FormValidation doCheckHome( @QueryParameter File value ){

            // don't reveal file existence information to non-administrators
        	// some one could brute force files paths to determine what is on a server
        	if( !Jenkins.getInstance().hasPermission( Jenkins.ADMINISTER ) ) return FormValidation.ok();

            if( value.getPath().equals( "" ) ) return FormValidation.ok(); // can be blank for master configurations and overriden on nodes

            if( !value.isDirectory() ) return FormValidation.error( "Please enter the path to the folder which contains the Clang static analyzer.  If this is your master and you will be overriding this value on a node you can leave this value blank." );

            File scanBuild = new File( value, "scan-build" );
            if( !scanBuild.exists() ) return FormValidation.warning( "Unable to locate 'scan-build' in the provided home directory." );

            return FormValidation.ok();
        }
    }

}
