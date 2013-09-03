package jenkins.plugins.clangscanbuild;

import jenkins.plugins.clangscanbuild.commands.Command;
import jenkins.plugins.clangscanbuild.commands.ScanBuildCommand;
import jenkins.plugins.clangscanbuild.commands.ScanCMakeBuildCommand;
import jenkins.plugins.clangscanbuild.commands.ScanMakeBuildCommand;

public final class CommandFactory {

	private static final String[] toolchains = {"XCode", "make", "CMake"};
	
	public static Command get(String cmd){

		System.err.println("Using build command " + cmd);
		if (cmd.toLowerCase().startsWith("make"))
			return new ScanMakeBuildCommand();
		if (cmd.toLowerCase().startsWith("cmake"))
			return new ScanCMakeBuildCommand();
		
		return new ScanBuildCommand();
	}
	
	public static String[] getToolchains() {
		return toolchains;
	}
}
