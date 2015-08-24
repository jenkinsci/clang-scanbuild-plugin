package jenkins.plugins.clangscanbuild;

import hudson.FilePath;
import hudson.model.AbstractBuild;


public class ClangScanBuildUtils{
	
	public static final String REPORT_OUTPUT_FOLDERNAME = "clangScanBuildReports";
	
	public static String getIconsPath(){
		return "/plugin/" + PluginImpl.SHORTNAME + "/icons/";
	}
	
	public static String getTransparentImagePath(){
		return "/plugin/" + PluginImpl.SHORTNAME + "/transparent.png";
	}
	
	public static FilePath locateClangScanBuildReportFolder( AbstractBuild<?,?> build, String folderName ){
		if( build == null ) return null;
		return new FilePath( new FilePath( build.getRootDir() ), folderName );
	}
	
}
