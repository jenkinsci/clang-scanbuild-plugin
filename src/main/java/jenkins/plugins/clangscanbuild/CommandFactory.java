package jenkins.plugins.clangscanbuild;

import jenkins.plugins.clangscanbuild.commands.Command;
import jenkins.plugins.clangscanbuild.commands.ScanBuildCommand;
import jenkins.plugins.clangscanbuild.commands.ScanCMakeBuildCommand;
import jenkins.plugins.clangscanbuild.commands.ScanMakeBuildCommand;

public final class CommandFactory {

	public static Command get(String cmd){
	
		if (cmd.startsWith("make"))
			return new ScanMakeBuildCommand();
		if (cmd.startsWith("cmake"))
			return new ScanCMakeBuildCommand();
		
		return new ScanBuildCommand();
	}
}
