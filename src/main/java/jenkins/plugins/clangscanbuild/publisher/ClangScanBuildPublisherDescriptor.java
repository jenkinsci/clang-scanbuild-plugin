package jenkins.plugins.clangscanbuild.publisher;

import jenkins.plugins.clangscanbuild.ClangScanBuildUtils;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;


public class ClangScanBuildPublisherDescriptor extends BuildStepDescriptor<Publisher>{

	public ClangScanBuildPublisherDescriptor(){
		super( ClangScanBuildPublisher.class );
		load();
	}
	
	
	@Override
	public Publisher newInstance(StaplerRequest arg0, JSONObject json ) throws hudson.model.Descriptor.FormException {

		boolean markBuildUnstable = false;
		int bugThreshold = 0;
		String excludedPaths = "";
		String reportFolderName = ClangScanBuildUtils.REPORT_OUTPUT_FOLDERNAME;
		
		JSONObject failWhenThresholdExceeded = json.optJSONObject( "failWhenThresholdExceeded" );
		if( failWhenThresholdExceeded != null ){
			markBuildUnstable = true;
			bugThreshold = failWhenThresholdExceeded.getInt( "bugThreshold" );
			excludedPaths = failWhenThresholdExceeded.getString( "clangexcludedpaths" );
		}
		
		reportFolderName = json.getString("reportFolderName");
    		
		return new ClangScanBuildPublisher( markBuildUnstable, bugThreshold, excludedPaths, reportFolderName );
	}
	
	@Override
	public String getDisplayName() {
		return "Publish Clang Scan-Build Results";
	}

	@Override
	public boolean isApplicable( @SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType ){
		if( !FreeStyleProject.class.isAssignableFrom( jobType ) ){
			System.err.println( "Clang scan-build ERROR: Expected FreeStyleProject but was: " + jobType + " at Publisher Descriptor" );
		}
		return AbstractProject.class.isAssignableFrom( jobType );
	}

}
