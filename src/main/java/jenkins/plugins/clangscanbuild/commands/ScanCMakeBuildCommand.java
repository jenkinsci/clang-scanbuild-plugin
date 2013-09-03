package jenkins.plugins.clangscanbuild.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hudson.util.ArgumentListBuilder;
import jenkins.plugins.clangscanbuild.CommandExecutor;

public class ScanCMakeBuildCommand extends ScanBasicCommand {

	private String getCMakeExecutable() {
		return "cmake";
	}

	public int execute(BuildContext context) throws Exception {

		File build = new File(getProjectDirectory().toURI());
		File src = new File(context.getWorkspace().getRemote(), getWorkspace());

		/* Remove old build folder, but only if we build out of tree. */
		if (!src.equals(build)) {
			if (build.exists())
				deleteFolder(build);
			build.mkdirs();
		}

		/* Create the CMake configuration for the target. */
		ArgumentListBuilder prepargs = new ArgumentListBuilder();
		prepargs.add(getCMakeExecutable());

		/*
		 * Build the CMake generation string. Use separate add for arguments, as
		 * they may contain spaces and additional quoting is not desired.
		 */
		prepargs.add("-G" + getTargetSdk());
		prepargs.add("-B" + getProjectDirectory().getRemote());
		prepargs.add("-H" + src.getAbsolutePath());
		prepargs.add("-DCMAKE_C_COMPILER=" + getClangCompilerExecutable());
		prepargs.add("-DCMAKE_CXX_COMPILER=" + getClangXXCompilerExecutable());

		String additionalBuildArgs = getAdditionalBuildArguments();
		if (isNotBlank(additionalBuildArgs)) {
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
			prepargs.addTokenized(escapeSpacesInPath(additionalBuildArgs));
		}

		int rc = context.waitForProcess(getProjectDirectory(), prepargs);
		if (rc == CommandExecutor.SUCCESS)
			context.log("CMake successfully generated configuration.");
		else {
			context.log("CMake failed to generate configuration.");
			return rc;
		}

		ArgumentListBuilder args = executeCommon(context);
		args.add(getCMakeExecutable());

		args.add("--build", getProjectDirectory().getRemote());
		if (isNotBlank(getTarget())) {
			args.add("--target", getTarget());
		}
		if (isNotBlank(getConfig()))
			args.add("--config", getConfig());

		rc = context.waitForProcess(getProjectDirectory(), args);

		if (rc == CommandExecutor.SUCCESS) {
			context.log("CMake SUCCESS");
		} else {
			context.log("CMake ERROR");
		}

		return rc;

	}
}
