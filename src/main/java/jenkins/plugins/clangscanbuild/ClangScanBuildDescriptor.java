package jenkins.plugins.clangscanbuild;

import hudson.CopyOnWrite;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tools.ToolInstallation;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.io.IOException;

import javax.servlet.ServletException;

import jenkins.plugins.clangscanbuild.ClangScanBuildToolInstallation.ClangStaticAnalyzerToolDescriptor;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * This class is the BuildStepDescriptor for the Clang Scan-Build build step.  Jenkins will
 * call the doCheck* methods below to validate the user entered values on the job configuration
 * screen.  Each of the doCheck* methods corresponds to a field defined in the config.jelly
 * file in the resources directory.  Jenkins uses the field names to reflectively locate doCheck*,
 * doFill*Items methods.
 * 
 * @author Joshua Kennedy
 */
public class ClangScanBuildDescriptor extends BuildStepDescriptor<Builder>{
	
    @CopyOnWrite
    private volatile ClangScanBuildToolInstallation[] installations = new ClangScanBuildToolInstallation[0];
	
	public ClangScanBuildDescriptor(){
		// Need to provide the class that Descriptor is pair to the BuildStepDescriptor
		// super class since I am not following the convention of static inner classes.  
		// Typically, the Descriptor is an inner class and Jenkins introspects this type
		// from the outer class.
		super( ClangScanBuildBuilder.class ); 
		load();
	}
	
    public ClangScanBuildToolInstallation.ClangStaticAnalyzerToolDescriptor getToolDescriptor(){
        return ToolInstallation.all().get( ClangScanBuildToolInstallation.ClangStaticAnalyzerToolDescriptor.class );
    }

    public ClangScanBuildToolInstallation[] getInstallations(){
        return installations;
    }
    
    public ClangScanBuildToolInstallation getNamedInstallation( String name ){
    	if( name == null ) return null;
    	
    	for( ClangScanBuildToolInstallation installation : installations ){
            if( name.equals( installation.getName() ) ){
                return installation;
            }
    	}
    	return null;
    }
    
    public void setInstallations( ClangScanBuildToolInstallation[] clangInstallations ){
        this.installations = clangInstallations;
        save();
    }

    public FormValidation doCheckTargetSdk( @QueryParameter String value ) throws IOException, ServletException {
    	if( value.length() == 0 ) return FormValidation.error( "You must provide a target SDK.  You can execute 'xcodebuild -showsdks' from Terminal.app to see allowed values." );
        return FormValidation.ok();
    }
	
    // CONFIG
    public FormValidation doCheckConfig( @QueryParameter String value ) throws IOException, ServletException {
        if( value.length() == 0 ) return FormValidation.warning( "If no build configuration is provided, the project's 'active' build configuration will be used automatically." );
        return FormValidation.ok();
    }

    public String getDisplayName() {
        return "Clang Scan-Build";
    }

    /**
     * This method is invoked when the user finally clicks 'Save' on the 
     * 'Configure System' global configuration screen.  This method should move 
     * data from the JSONObject which is populated with the submitted form data 
     * for the Clang Scan-Build Plugin from the 'Configure System' screen.  The options
     * for this plugin are rendered using the global.jelly file from the resources 
     * directory.  Each field in the global.jelly file will be mapped to the JSONObject
     * by field name.  This method should move each value for each field from the 
     * JSONObject into private fields with getters/setters in this class.  Once all
     * the fields are marshalled to an instance of this class, this method should invoke
     * 'save()' to persist the data for the user.
     */
    @Override
    public boolean configure( StaplerRequest req, JSONObject formData ) throws FormException{
    	// This is called from a save of the global settings
        save();
        
        return super.configure( req, formData );
    }

    public boolean isApplicable( @SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType ){
        return AbstractProject.class.isAssignableFrom( jobType );
    }

    public ListBoxModel doFillBuildcommandItems() {
    	ListBoxModel items = new ListBoxModel();
    	String [] tools = CommandFactory.getToolchains();
    	
    	for (String s : tools) {
    		items.add(s, s);
    	}
    	return items;
    }
}
