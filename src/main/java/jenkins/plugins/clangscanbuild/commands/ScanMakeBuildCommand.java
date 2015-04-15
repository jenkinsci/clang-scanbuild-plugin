package jenkins.plugins.clangscanbuild.commands;

import java.io.File;

import hudson.util.ArgumentListBuilder;
import jenkins.plugins.clangscanbuild.CommandExecutor;

public class ScanMakeBuildCommand extends ScanBasicCommand {
	
	public int execute( BuildContext context ) throws Exception {

		File build = new File(getProjectDirectory().toURI());
		File src = new File(context.getWorkspace().getRemote(), getWorkspace());

		/* Remove old build folder, but only if we build out of tree. */
		if (!src.equals(build)) {
			if (build.exists())
				deleteFolder(build);
			build.mkdirs();
		}
		
		/* Basic autotools support, run configure before make. */
		File cfg = new File(getProjectDirectory().getRemote(), "configure");
		if (cfg.exists()) {
			ArgumentListBuilder prep = new ArgumentListBuilder();
			prep.add("./configure");
			
			/* TODO: Append configure arguments here. */
			int rc = context.waitForProcess( getProjectDirectory(), prep );

			if( rc == CommandExecutor.SUCCESS ){
				context.log( "MAKE SUCCESS" );
			}else{
				context.log( "MAKE ERROR" );
				return rc;
			}
		}

		ArgumentListBuilder args = executeCommon(context);
		args.add( "make" );
		
		/* By default clean and build everything. */
		if( isNotBlank( getTarget() ) ){
			args.addTokenized( getTarget() );
		}else{
			args.add( "clean" );
			args.add( "all" );
		}

		String additionalBuildArgs = getAdditionalBuildArguments();
		if( isNotBlank( additionalBuildArgs ) ){
			// This is a hack.  I can't call the standard ArgumentListBuilder.add() method because it checks for spaces with-in
			// the arg and quotes the arg if a space exists.  Since user's can pass commands like
			// '--use-cc=`which clang`' or multiple commands...we cannot allow the quotes to be 
			// inserted when spaces exist.  The ArgumentListBuilder.addTokenized() splits the arg on spaces and adds each piece 
			// which ends up reinserting the spaces when the command is assembled.
			args.addTokenized( additionalBuildArgs );
		}

		int rc = context.waitForProcess( getProjectDirectory(), args );

		if( rc == CommandExecutor.SUCCESS ){
			context.log( "MAKE SUCCESS" );
		}else{
			context.log( "MAKE ERROR" );
		}
		
		return rc;

	}
}
