package jenkins.plugins.clangscanbuild.commands;

import java.io.File;

import hudson.FilePath;
import hudson.util.ArgumentListBuilder;

public abstract class ScanBasicCommand implements Command {

	protected String target;
	protected String config = "Debug";
	protected String workspace;
	protected String scheme;
	protected String targetSdk;
	protected String clangScanBuildPath;
	protected FilePath projectDirectory;
	protected FilePath clangOutputFolder;
	protected String additionalScanBuildArguments; // Passed directly to shell
	protected String additionalBuildArguments; // Passed directly to shel

	ArgumentListBuilder executeCommon(BuildContext context) throws Exception {
		if (clangOutputFolder.exists()) {
			// this should never happen because this folder is in the build
			// directory - famous last words
			context.log("Deleting '" + getClangOutputFolder().getRemote()
					+ "' contents from previous build.");
			clangOutputFolder.deleteContents();
		} else {
			clangOutputFolder.mkdirs();
		}

		ArgumentListBuilder args = new ArgumentListBuilder();
			
		args.add(getClangScanBuildExecutable());

		args.add("-k"); // keep going on failure
		args.add("-v"); // verbose
		args.add("-v"); // even more verbose

		args.add("-o"); // output folder
		args.add(escapeSpacesInPath(clangOutputFolder.getRemote()));
		args.add("--use-analyzer", getClangCompilerExecutable());

		String additionalArgs = getAdditionalScanBuildArguments();
		if (isNotBlank(additionalArgs)) {
			// This is a hack. I can't call the standard
			// ArgumentListBuilder.add() method because it checks for spaces
			// with-in
			// the arg and quotes the arg if a space exists. Since user's can
			// pass commands like
			// '--use-cc=`which clang`' or multiple commands...we cannot allow
			// the quotes to be
			// inserted when spaces exist. The
			// ArgumentListBuilder.addTokenized() splits the arg on spaces and
			// adds each piece
			// which ends up reinserting the spaces when the command is
			// assembled.
			args.addTokenized(additionalArgs);
		}

		return args;
	}

	@Override
	public void setTarget(String target) {
		this.target = target;
	}

	@Override
	public void setProjectDirectory(FilePath directory) {
		projectDirectory = directory;
	}

	@Override
	public void setConfig(String cfg) {
		config = cfg;
	}

	@Override
	public void setWorkspace(String workspace) {
		this.workspace = workspace;
	}

	@Override
	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	@Override
	public void setTargetSdk(String sdk) {
		targetSdk = sdk;
	}

	@Override
	public void setAdditionalScanBuildArguments(String args) {
		additionalScanBuildArguments = args;
	}

	@Override
	public void setClangOutputFolder(FilePath directory) {
		clangOutputFolder = directory;
	}

	@Override
	public void setClangScanBuildPath(String path) {
		clangScanBuildPath = path;
	}

	@Override
	public void setAdditionalBuildArguments(String buildargs) {
		additionalBuildArguments = buildargs;
	}
	
	protected boolean isBlank( String value ){
		if( value == null ) return true;
		return value.trim().length() <= 0;
	}
	
	protected String escapeSpacesInPath( String path ){
		if( path == null ) return "";
		return path.replaceAll( " ", "\\ " );
	}
	
	protected boolean isNotBlank( String value ){
		return !isBlank( value );
	}
	
	public String getTargetSdk() {
		return targetSdk;
	}

	public String getTarget() {
		return target;
	}

	public String getConfig() {
		return config;
	}

	public String getClangCompilerExecutable() {
		File base = new File(clangScanBuildPath);
		File clang = new File(base.getParent(), "clang");
		
		return clang.getAbsolutePath();
	}
	
	public String getClangXXCompilerExecutable() {
		File base = new File(clangScanBuildPath);
		File clang = new File(base.getParent(), "clang++");
		
		return clang.getAbsolutePath();
	}
	
	public String getClangScanBuildExecutable() {
		return clangScanBuildPath;
	}

	public FilePath getProjectDirectory() {
		return projectDirectory;
	}

	public FilePath getClangOutputFolder() {
		return clangOutputFolder;
	}

	public String getWorkspace() {
		return workspace;
	}

	public String getScheme() {
		return scheme;
	}

	public String getAdditionalScanBuildArguments() {
		return additionalScanBuildArguments;
	}

	public String getAdditionalBuildArguments() {
		return additionalBuildArguments;
	}
}
