// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.perf.chart;

import java.util.ArrayList;
import java.util.List;

class ChartSettings {

    public static final String DEFAULT_CHART_XAXIS = "Time";
    public static final boolean DEFAULT_CHART_ALLOW_LOG_SCALE = true;
    public static final boolean DEFAULT_CHART_PLOT_ZERO = false;
    public static final int DEFAULT_CHART_WIDTH = 1200;
    public static final int DEFAULT_CHART_HEIGHT = 200;
    public static final int MINIMUM_CHART_HEIGHT = 200;

    public static enum ImageType { JPG, PNG };
    public static enum TopPlotsType { AVG, MAX };

    private String mDocument;
    private String mTitle;
    private String mCategory;
    private String mOutfile;
    private String mXAxis;
    private String mYAxis;
    private int mTopPlots;
    private TopPlotsType mTopPlotsType;
    private boolean mAllowLogScale;
    private int mWidth = 1200;
    private int mHeight = 200;
    private ImageType mImageType;
    private List<PlotSettings> mPlots;
    private List<GroupPlotSettings> mGroupPlots;

    /**
     * <code>LogarithmicAxis</code> can't handle zero values. Setting this
     * property to <code>false</code> tells <code>ChartFromCsv</code> to
     * ignore zero values, which allows a logarithmic axis chart to be rendered.
     */
    private boolean mPlotZero = false;

    public ChartSettings(String title, String category, String outfile, String xAxis,
                         String yAxis, boolean allowLogScale, boolean plotZero, int width,
                         int height, String outDoc, int topPlots, TopPlotsType topType) {
        mTitle = title;
        mCategory = category;
        mOutfile = outfile;
        String lower = mOutfile.toLowerCase();
        if (lower.endsWith(".png"))
            mImageType = ImageType.PNG;
        else if (lower.endsWith(".jpg"))
            mImageType = ImageType.JPG;
        else
            throw new IllegalArgumentException(String.format(
                    "Unexpected file type '%s' for outfile in chart %s.  " +
                    "Only .jpg and .png are supported.", mOutfile, title));
        mXAxis = xAxis;
        mYAxis = yAxis;
        mAllowLogScale = allowLogScale;
        mPlotZero = plotZero;
        mWidth = width;
        mHeight = height;
        mPlots = new ArrayList<PlotSettings>();
        mGroupPlots = new ArrayList<GroupPlotSettings>();
        mDocument = outDoc;
        mTopPlots = topPlots;
        mTopPlotsType = topType;
    }

    public void addPlot(PlotSettings p) {
        mPlots.add(p);
    }
    public void addPlot(GroupPlotSettings p) {
    	mGroupPlots.add(p);
    }

    public String getTitle() { return mTitle; }
    public String getCategory() { return mCategory; }             
    public ChartSettings() {
    }
    
    /**
     * With the introduction of LDAP csv stats (Bug 69751), some of the outfile lengths  can exceed 255 characters. 
     * This is due to the filename being constructed from the actual metric being measured and LDAP queries can 
     * be pretty long when also coupled with the FQHN which proceeds the metric name.
     * For now, check to ensure that the output filename does not cause an exception to be thrown
     * due to long filenames.  Truncate the filename and add the actual image type at the end.
     * Ideally the metric should be formatted so that a more pleasant value is presented to the user on the generated
     * chart, with the ability to view the actual full length LDAP query details with some additional user action 
     * such as a mouse-over, etc.  
     * This needs to be cleaned up to either hash the filename for all files, or based on Phoebe's comments in Bug 69751,
     * use a combination of a formatted "command" name as the filename and add a lookup table to be able to gain more details on 
     * the full length query.
     * 
     */
    public String getOutfile() { return mOutfile.length() <= 255 ? mOutfile : mOutfile.substring(0, 250) + "." + mImageType.name().toLowerCase(); }
    
    
    public String getXAxis() { return mXAxis; }
    public String getYAxis() { return mYAxis; }
    public boolean getAllowLogScale() { return mAllowLogScale; }
    public boolean getPlotZero() { return mPlotZero; }
    public int getWidth() { return mWidth; }
    public ImageType getImageType() { return mImageType; }
    public List<PlotSettings> getPlots() { return mPlots; }
    public List<GroupPlotSettings> getGroupPlots() { return mGroupPlots; }
    public int getTopPlots() { return mTopPlots; }
    public TopPlotsType getTopPlotsType() { return mTopPlotsType; }

    public int getHeight() {
        int h = Math.max(mHeight, MINIMUM_CHART_HEIGHT);
        if (mPlots.size() <= 1)
            h -= 40;
        return h;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<chart\n");
        sb.append("  ").append("title=\"").append(mTitle).append("\"\n");
        sb.append("  ").append("outfile=\"").append(mOutfile).append("\"\n");
        sb.append("  ").append("xAxis=\"").append(mXAxis).append("\"\n");
        sb.append("  ").append("yAxis=\"").append(mYAxis).append("\"\n");
        sb.append("  ").append("allowLogScale=\"").append(mAllowLogScale).append("\"\n");
        sb.append("  ").append("plotZero=\"").append(mPlotZero).append("\"\n");
        sb.append("  ").append("width=\"").append(mWidth).append("\"\n");
        sb.append("  ").append("height=\"").append(mHeight).append("\"\n");
        sb.append("  ").append("outDocument=\"").append(mDocument).append("\"\n");
        sb.append(">\n");
        for (PlotSettings plot : mPlots)
            sb.append(plot.toString());
        sb.append("</chart>\n");
        return sb.toString();
    }

    public String getOutDocument() {
        return mDocument;
    }
    public void setOutDocument(String doc) {
        mDocument = doc;
    }
}
