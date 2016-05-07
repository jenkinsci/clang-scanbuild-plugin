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
package jenkins.plugins.clangscanbuild.history;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClangScanBuildBugSummary {

	private int buildNumber;
	public Set<ClangScanBuildBug> bugs = new HashSet<ClangScanBuildBug>();
	
	public boolean contains( ClangScanBuildBug bug ){
		for( ClangScanBuildBug candidate : bugs ){
			if( bug.getBugDescription().equals( candidate.getBugDescription() ) ) return true;
		}
		return false;
	}
	
	public ClangScanBuildBugSummary( int buildNumber ){
		this.buildNumber = buildNumber;
	}
	
	public boolean add( ClangScanBuildBug bug ){
		return bugs.add( bug );
	}
	
	public int getBugCount(){
		return bugs.size();
	}

	public List<ClangScanBuildBug> getBugs() {
		return new ArrayList<ClangScanBuildBug>( bugs );
	}

	public void addBugs( Collection<ClangScanBuildBug> bugs ) {
		this.bugs.addAll( bugs );
	}

	public int getBuildNumber() {
		return buildNumber;
	}

}
