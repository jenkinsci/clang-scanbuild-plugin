package jenkins.plugins.clangscanbuild.commands;

import hudson.util.ArgumentListBuilder;
import jenkins.plugins.clangscanbuild.CommandExecutor;

public class ScanMakeBuildCommand extends ScanBasicCommand {
	
	public int execute( BuildContext context ) throws Exception {

		ArgumentListBuilder args = executeCommon(context);
		args.add( "make" );
		
		args.add( "clean" );
		if( isNotBlank( getTarget() ) ){
			args.add( getTarget() );
		}else{
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
