package jenkins.plugins.clangscanbuild.commands;

import hudson.util.ArgumentListBuilder;
import jenkins.plugins.clangscanbuild.CommandExecutor;

public class ScanBuildCommand extends ScanBasicCommand {
	
	public int execute( BuildContext context ) throws Exception {

		ArgumentListBuilder args = executeCommon(context);
		
		args.add( "xcodebuild" );
		
		if( isNotBlank( getWorkspace() ) ){ 
			// Xcode 4 workspace
			args.add( "-workspace", getWorkspace() );
			args.add( "-scheme", getScheme() );
			
			if( isNotBlank( getTarget() ) ){
				context.log( "Ignoring build target '" + getTarget() + "' because a workspace & scheme was provided" );
			}
		}else{ 
			// Xcode 3,4 standalone project
			if( isNotBlank( getTarget() ) ){
				args.add( "-target", getTarget() );
			}else{
				args.add( "-activetarget" );
			}
		}

		//These items can be provided with a target or can be used to override a workspace/scheme
		if( isNotBlank( getConfig() ) ) args.add( "-configuration", getConfig() );  // Defaults to Debug
		if( isNotBlank( getTargetSdk() ) ) args.add( "-sdk", getTargetSdk() );
		
		args.add( "clean" ); // clang scan requires a clean
		args.add( "build" );
                
		String additionalXcodeBuildArgs = getAdditionalBuildArguments();
		if( isNotBlank( additionalXcodeBuildArgs ) ){
			// This is a hack.  I can't call the standard ArgumentListBuilder.add() method because it checks for spaces with-in
			// the arg and quotes the arg if a space exists.  Since user's can pass commands like
			// '--use-cc=`which clang`' or multiple commands...we cannot allow the quotes to be 
			// inserted when spaces exist.  The ArgumentListBuilder.addTokenized() splits the arg on spaces and adds each piece 
			// which ends up reinserting the spaces when the command is assembled.
			args.addTokenized( additionalXcodeBuildArgs );
		}

		int rc = context.waitForProcess( getProjectDirectory(), args );

		if( rc == CommandExecutor.SUCCESS ){
			context.log( "XCODEBUILD SUCCESS" );
		}else{
			context.log( "XCODEBUILD ERROR" );
		}
		
		return rc;

	}
}
