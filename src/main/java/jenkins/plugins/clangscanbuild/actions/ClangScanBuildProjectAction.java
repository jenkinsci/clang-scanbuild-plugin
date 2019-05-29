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
package jenkins.plugins.clangscanbuild.actions;

import hudson.model.Action;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.util.ChartUtil;

import java.io.IOException;
import java.util.List;

import jenkins.plugins.clangscanbuild.ClangScanBuildUtils;
import jenkins.plugins.clangscanbuild.history.ClangScanBuildHistoryGatherer;
import jenkins.plugins.clangscanbuild.history.ClangScanBuildHistoryGathererImpl;
import jenkins.plugins.clangscanbuild.reports.ClangBuildGraph;
import jenkins.plugins.clangscanbuild.reports.GraphPoint;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * This contributes the menu to the left used to access reports/whatever from inside a 
 * project.  This is called a "ProjectAction" because it contributes
 * a link to the left and a URL to a specific projects dashboard.
 * 
 * @author Josh Kennedy
 */
public class ClangScanBuildProjectAction implements Action{

	private static final String DEFAULT_IMAGE = "/images/headless.png";
	private ClangScanBuildHistoryGatherer gatherer = new ClangScanBuildHistoryGathererImpl();

	public final Job<?,?> project;

	public ClangScanBuildProjectAction(Job<?,?> project ) {
		this.project = project;
	}
	  
	@Override
	public String getIconFileName() {
		return ClangScanBuildUtils.getIconsPath() + "scanbuild-32x32.png";
	}

	@Override
	public String getDisplayName() {
		return "Clang scan-build trend";
	}

	@Override
	public String getUrlName() {
		return "clangScanBuildTrend";
	}

	public Job<?, ?> getProject() {
		return project;
	}

	/**
	 * Doing this wastefully because i do not know the lifecycle of this object.  Is it a singleton?
	 */
	public ClangBuildGraph getGraph(){
		return new ClangBuildGraph( gatherer.gatherHistoryDataSet( project.getLastBuild() ) );
	}

    public void doGraph( StaplerRequest req, StaplerResponse rsp ) throws IOException {
        if( ChartUtil.awtProblemCause != null ){
            rsp.sendRedirect2( req.getContextPath() + DEFAULT_IMAGE );
            return;
        }

    	getGraph().doPng( req, rsp );
    }
    
    public void doMap( StaplerRequest req, StaplerResponse rsp ) throws IOException {
    	getGraph().doMap( req, rsp );
    }
    
    public boolean buildDataExists() {
		List<GraphPoint> points = gatherer.gatherHistoryDataSet(project.getLastBuild());
		return points.size() > 0;
	}
}
