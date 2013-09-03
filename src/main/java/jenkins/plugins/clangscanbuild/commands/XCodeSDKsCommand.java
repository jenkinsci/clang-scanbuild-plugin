package jenkins.plugins.clangscanbuild.commands;

import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.model.AbstractBuild;
import hudson.util.ArgumentListBuilder;
import hudson.util.ListBoxModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XCodeSDKsCommand extends ScanBasicCommand {
	
	public int execute( BuildContext context ) {
		return 1;
	}
	
	protected ListBoxModel identifyAvailableSDKs( AbstractBuild<?,?> build, Launcher launcher, PrintStream logger ){
		
		ListBoxModel model = new ListBoxModel();
		try {

    		ByteArrayOutputStream stdOutStream = new ByteArrayOutputStream();
    		ByteArrayOutputStream stdErrStream = new ByteArrayOutputStream();
    		
    		ProcStarter starter = launcher.launch();
    		starter.pwd( build.getWorkspace() );
    		starter.stdout( stdOutStream );
    		starter.stderr( stdErrStream );
    		
    		ArgumentListBuilder args = new ArgumentListBuilder();
    		args.add( "xcodebuild" );
    		args.add( "-showsdks" );
    		
    		starter.cmds( args );
    		
    		int rc = starter.join();
    		
    		if( rc == 0 ){
    			Pattern matchSDK = Pattern.compile( "\\t.*-sdk\\s(.*)", Pattern.CASE_INSENSITIVE );
    			
    			Matcher m = matchSDK.matcher( stdOutStream.toString() );
    			
    			boolean result = m.find();
    			while( result ){
    				model.add( m.group( 1 ),  m.group( 1 ) );
    				result = m.find();
    			}

    		}

			
		} catch ( IOException e ){
			logger.println( "Exception occurred invoking command 'ls':" + e.getMessage() );
		} catch (InterruptedException e) {
			logger.println( "Exception occurred invoking command 'ls':" + e.getMessage() );
		}
		
		return model;
	}
	
}
