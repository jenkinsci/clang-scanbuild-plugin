package jenkins.plugins.clangscanbuild.commands;

import hudson.FilePath;

public interface Command {
	
	public int execute( BuildContext context ) throws Exception;

	public void setTarget(String target);
	
	public void setProjectDirectory(FilePath directory);
	
	public void setConfig(String cfg);

	public void setWorkspace(String workspace);

	public void setScheme(String scheme);

	public void setTargetSdk(String sdk);
	
	public void setAdditionalScanBuildArguments(String args);
	
	public void setClangOutputFolder(FilePath directory);

	public void setClangScanExecutable(String path);

	public void setAdditionalBuildArguments(String buildargs);
	
	public void setClangAnalyzerExecutable(String tool);

	public void setClangXXAnalyzerExecutable(String tool);

	public void setClangCompilerExecutable(String tool);

	public void setClangXXCompilerExecutable(String tool);
}