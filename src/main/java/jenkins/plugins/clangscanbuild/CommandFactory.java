package jenkins.plugins.clangscanbuild;

import jenkins.plugins.clangscanbuild.commands.Command;
import jenkins.plugins.clangscanbuild.commands.ScanBuildCommand;
import jenkins.plugins.clangscanbuild.commands.ScanMakeBuildCommand;

public final class CommandFactory {

	public static Command get(String cmd){
		System.err.println("Command requested: '" + cmd + "'");
		//if (cmd == "make")
			return new ScanMakeBuildCommand();
		
		//return new ScanBuildCommand();
	}
}
