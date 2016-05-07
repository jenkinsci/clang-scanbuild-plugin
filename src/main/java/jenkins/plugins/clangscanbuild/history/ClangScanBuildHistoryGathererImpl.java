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

import hudson.model.AbstractBuild;

import java.util.ArrayList;
import java.util.List;

import jenkins.plugins.clangscanbuild.actions.ClangScanBuildAction;
import jenkins.plugins.clangscanbuild.reports.GraphPoint;

public class ClangScanBuildHistoryGathererImpl implements ClangScanBuildHistoryGatherer{

	private int numberOfBuildsToGather = 60;
	
	public ClangScanBuildHistoryGathererImpl(){
		super();
	}
	
	public ClangScanBuildHistoryGathererImpl( int numberOfBuildsToGather ){
		this();
		this.numberOfBuildsToGather = numberOfBuildsToGather;
	}
	
	public List<GraphPoint> gatherHistoryDataSet( AbstractBuild<?,?> latestBuild ){
		List<GraphPoint> points = new ArrayList<GraphPoint>();
		if( latestBuild == null ) return points;
		
		int gatheredBuilds = 0;
	    for( AbstractBuild<?,?> build = latestBuild; build != null; build = build.getPreviousBuild() ){
	    	if( gatheredBuilds >= numberOfBuildsToGather ) return points;
	    	
	    	ClangScanBuildAction action = build.getAction( ClangScanBuildAction.class );
	        if( action == null ) continue;
	        
	        points.add( new GraphPoint( build, action.getBugCount() ) );
	    }
		
	    return points;
	}

}
