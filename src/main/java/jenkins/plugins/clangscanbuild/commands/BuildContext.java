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

import hudson.FilePath;
import hudson.util.ArgumentListBuilder;

/**
 * This interface abstracts the operations which the xcodebuild commands needs in order to
 * execute.  This was done so that unit tests could mock this interface and test command
 * building independently of jenkins.
 * 
 * @author Josh Kennedy
 */
public interface BuildContext {
	
	/**
	 * Returns the FilePath of the current executing build
	 * @return
	 */
	public FilePath getBuildFolder();

	/**
	 * Returns workspace location of current executing build
	 */
	public FilePath getWorkspace();
	
	/**
	 * This method starts a process and will not return control until
	 * either the process is complete or the process is interrupted.  
	 * Caught exceptions (IOException,InterrupredException) are logged 
	 * to the build listener and a return code of 1 will be returned. 
	 * Upon success, a return code of 0 is returned.
	 */
	public int waitForProcess( FilePath presentWorkingDirectory, ArgumentListBuilder command );
	
	/**
	 * Logs a message to the build listener.
	 */
	public void log( String message );
	
}
