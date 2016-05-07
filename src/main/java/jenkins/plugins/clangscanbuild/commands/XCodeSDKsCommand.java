/**
 * Copyright (c) 2011 Joshua Kennedy, http://deadmeta4.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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

public class XCodeSDKsCommand implements Command{
	
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
