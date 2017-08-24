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
package jenkins.plugins.clangscanbuild.reports;

import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import hudson.util.ColorPalette;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;
import hudson.util.ShiftedCategoryAxis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import jenkins.model.Jenkins;
import jenkins.plugins.clangscanbuild.actions.ClangScanBuildAction;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleInsets;

/**
 * This is called from the report page
 * 
 * @author Joshua Kennedy
 */
public class ClangBuildGraph extends Graph{
	
	private List<GraphPoint> points;
	
	public ClangBuildGraph( List<GraphPoint> points ){
		super( Calendar.getInstance(), 350, 150 );
		this.points = points;
	}

	@Override
	protected JFreeChart createGraph(){
		
		DataSetBuilder<String, NumberOnlyBuildLabel> dataSetBuilder = new DataSetBuilder<String, NumberOnlyBuildLabel>();
		for( GraphPoint point : points ){
			dataSetBuilder.add( point.getBugCount(), "bugcount", new NumberOnlyBuildLabel( point.getRun() ) );
		}
		
        final JFreeChart chart = ChartFactory.createLineChart(
            null, // chart title
            null, // unused
            "Bugs", // range axis label
            dataSetBuilder.build(), // data
            PlotOrientation.VERTICAL, // orientation
            false, // include legend
            true, // tooltips
            true // urls
        );

        chart.setBackgroundPaint( Color.white );

        final CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.black);
        
        CategoryAxis domainAxis = new ShiftedCategoryAxis( "Build Number" );
        plot.setDomainAxis( domainAxis );
        domainAxis.setCategoryLabelPositions( CategoryLabelPositions.UP_90 );
        domainAxis.setLowerMargin( 0.0 );
        domainAxis.setUpperMargin( 0.0 );
        domainAxis.setCategoryMargin( 0.0 );
  
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits( NumberAxis.createIntegerTickUnits() );
        
        // Using a custom render here so i can control how URLs and tooltips are added to the imagemap
        URLAndTooltipBuilder builder = new URLAndTooltipBuilder( points );
        URLAndTooltipRenderer urlRenderer = new URLAndTooltipRenderer( builder );
        urlRenderer.setBaseStroke( new BasicStroke( 4.0f ) );
        urlRenderer.setSeriesShapesVisible( 0,  true );
        ColorPalette.apply( urlRenderer );
        plot.setRenderer( urlRenderer );

        // crop extra space around the graph
        plot.setInsets( new RectangleInsets( 5.0, 0, 0, 5.0 ) );
        return chart;
	}
	
	private static class URLAndTooltipRenderer extends LineAndShapeRenderer{
		
		private static final long serialVersionUID = 1L;
		private URLAndTooltipBuilder urlAndTooltipBuilder;

		public URLAndTooltipRenderer( URLAndTooltipBuilder urlAndTooltipBuilder ){
			super();
			this.urlAndTooltipBuilder = urlAndTooltipBuilder;
		}
		
		@Override
    	public CategoryToolTipGenerator getToolTipGenerator( int row, final int columnOuter ){
    		return urlAndTooltipBuilder;
    	}
		
    	@Override
    	public CategoryURLGenerator getItemURLGenerator( int row, final int column ){
    		return urlAndTooltipBuilder;
    	}
		
	}
	
	private static class URLAndTooltipBuilder implements CategoryURLGenerator, CategoryToolTipGenerator{
        
		// For some reason when the renderer gets called, it walks the points backwards,
        // This was causing the points on the map not to match the URLs and tooltips.  By reversing
        // the list of points for the renderer, the data stays in sync.
        private List<GraphPoint> reversedPoints;
        
        public URLAndTooltipBuilder( List<GraphPoint> points ){
        	reversedPoints = new ArrayList<GraphPoint>( points );
            Collections.reverse( reversedPoints );
        }
        
		@Override
		public String generateURL( CategoryDataset dataset, int series, int category ){
			GraphPoint point = reversedPoints.get( category );
			if( point == null ) return "";
			return Jenkins.getInstance().getRootUrl() + point.getRun().getUrl() + "/" + ClangScanBuildAction.BUILD_ACTION_URL_NAME;
		}

		@Override
		public String generateToolTip( CategoryDataset dataset, int row, int column ){
			GraphPoint point = reversedPoints.get( column );
			if( point == null ) return "";
			return "Build #" + point.getRun().number + " - " + point.getBugCount() + " bugs";
		}
		
	}

}
