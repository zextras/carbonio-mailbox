// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.perf.chart;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import com.zimbra.common.util.CsvReader;
import com.zimbra.common.util.Pair;
import com.zimbra.common.util.StringUtil;
import com.zimbra.perf.chart.ChartSettings.ImageType;

public class ChartUtil {

    private static final String OPT_HELP = "help";
    private static final String OPT_CONF = "conf";
    private static final String OPT_SRCDIR = "srcdir";
    private static final String OPT_DESTDIR = "destdir";
    private static final String OPT_TITLE = "title";
    private static final String OPT_START_AT = "start-at";
    private static final String OPT_END_AT = "end-at";
    private static final String OPT_AGGREGATE_START_AT = "aggregate-start-at";
    private static final String OPT_AGGREGATE_END_AT = "aggregate-end-at";
    private static final String OPT_NO_SUMMARY = "no-summary";

    private static final String GROUP_PLOT_SYNTHETIC = "group-plot-synthetic$";
    private static final String RATIO_PLOT_SYNTHETIC = "ratio-plot-synthetic$";

    private static final SimpleDateFormat[] sDateFormats = {
            new SimpleDateFormat("MM/dd/yyyy HH:mm:ss"),
            new SimpleDateFormat("MM/dd/yyyy HH:mm") };

    private static final String SUMMARY_CSV = SummaryConstants.SUMMARY_CSV;

    private final File[] mConfs;
    private final File[] mSrcDirs;
    private final File mDestDir;
    private final String mTitle;
    private Date mStartAt = null;
    private Date mEndAt = null;
    private Date mAggregateStartAt = null;
    private Date mAggregateEndAt = null;
    private final boolean mSkipSummary;

    private final List<ChartSettings> mSyntheticChartSettings =
        new ArrayList<>();
    private final List<JFreeChart> mCharts = new ArrayList<>();
    private final Set<DataColumn> mUniqueDataColumns;
    private final Set<DataColumn> mUniqueStringColumns;
    private final Map<String /* infile */, Set<Pair<String /* column */, DataSeries>>> mColumnsByInfile;
    private final Map<ChartSettings,JFreeChart> mChartMap =
        new HashMap<>();
    private final Map<DataColumn, DataSeries> mDataSeries;
    private final Map<DataColumn, StringSeries> mStringSeries;
    private final Map<DataColumn, Double> mAggregates;
    private final Map<String, Double> mStats;
    private final Aggregator mAggregator;

    private long mMinDate = Long.MAX_VALUE;
    private long mMaxDate = Long.MIN_VALUE;

    // uniquely identifies a data source
    private static class DataColumn {
        private final String mInfile;

        private final String mColumn;

        private final int mHashCode;

        public DataColumn(String infile, String column) {
            mInfile = infile;
            mColumn = column;
            String hashStr = mInfile + "#" + column;
            mHashCode = hashStr.hashCode();
        }

        public String getInfile() {
            return mInfile;
        }

        public String getColumn() {
            return mColumn;
        }

        @Override
        public int hashCode() {
            return mHashCode;
        }

        @Override
        public boolean equals(Object obj) {
            DataColumn other = (DataColumn) obj;
            return other != null && mInfile.equals(other.mInfile)
                    && mColumn.equals(other.mColumn);
        }
        @Override
        public String toString() {
            return mInfile + ":" + mColumn;
        }
    }

    private static Options getOptions() {
        Options opts = new Options();

        opts.addOption("h", OPT_HELP, false, "prints this usage screen");

        Option confOption = new Option("c", OPT_CONF, true,
                "chart configuration xml files");
        confOption.setArgs(Option.UNLIMITED_VALUES);
        confOption.setRequired(true);
        opts.addOption(confOption);

        Option srcDirOption = new Option("s", OPT_SRCDIR, true,
                "one or more directories where the csv files are located");
        srcDirOption.setArgs(Option.UNLIMITED_VALUES);
        opts.addOption(srcDirOption);

        Option destDirOption = new Option("d", OPT_DESTDIR, true,
                "directory where the generated chart files are saved");
        opts.addOption(destDirOption);

        opts.addOption(null, OPT_TITLE, true,
                "chart title; defaults to last directory name of --"
                        + OPT_SRCDIR + " value");

        opts.addOption(null, OPT_START_AT, true,
                "if specified, ignore all samples before this timestamp (MM/dd/yyyy HH:mm:ss)");
        opts.addOption(null, OPT_END_AT, true,
                "if specified, ignore all samples after this timestamp (MM/dd/yyyy HH:mm:ss)");

        opts.addOption(null, OPT_AGGREGATE_START_AT, true,
                "if specified, aggregate computation starts at this timestamp (MM/dd/yyyy HH:mm:ss)");
        opts.addOption(null, OPT_AGGREGATE_END_AT, true,
                "if specified, aggregate computation ends at this timestamp (MM/dd/yyyy HH:mm:ss)");

        opts.addOption(null, OPT_NO_SUMMARY, false,
                "skip summary data generation");

        return opts;
    }

    private static void usage(Options opts) {
        usage(opts, null);
    }

    private static void usage(Options opts, String msg) {
        if (msg != null)
            System.err.println(msg);
        String invocation = "Usage: zmstat-chart -c <arg> -s <arg> -d <arg> [options]";
        PrintWriter pw = new PrintWriter(System.err, true);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(pw, formatter.getWidth(), invocation, null, opts,
                formatter.getLeftPadding(), formatter.getDescPadding(), null);
        pw.flush();
        System.exit(1);
    }

    private static Date parseTimestampOption(CommandLine cl, Options opts,
            String opt) {
        Date date = null;
        String str = cl.getOptionValue(opt);
        if (str != null) {
            for (int i = 0; i < sDateFormats.length && date == null; i++) {
                try {
                    synchronized (sDateFormats[i]) {
                        date = sDateFormats[i].parse(str);
                    }
                } catch (ParseException e) {
                }
            }
            if (date == null)
                usage(opts, "Invalid --" + opt + "value \"" + str + "\"");
        }
        return date;
    }

    public static void main(String[] args) throws Exception {
        CommandLineParser clParser = new GnuParser();
        Options opts = getOptions();
        try {
            CommandLine cl = clParser.parse(opts, args);

            if (cl.hasOption('h'))
                usage(opts);
            if (!cl.hasOption('s') && !cl.hasOption('d'))
                usage(opts, "-s and -d options are required");
            if (!cl.hasOption('s'))
                usage(opts, "Missing required -s option");

            if (!cl.hasOption('d'))
                usage(opts, "Missing required -d option");

            String[] confs = cl.getOptionValues(OPT_CONF);
            if (confs == null || confs.length == 0)
                usage(opts, "Missing --" + OPT_CONF + " option");
            File[] confFiles = new File[confs.length];
            for (int i = 0; i < confs.length; i++) {
                File conf = new File(confs[i]);
                if (!conf.exists()) {
                    System.err.printf("Configuration file %s does not exist\n",
                            conf.getAbsolutePath());
                    System.exit(1);
                }
                confFiles[i] = conf;
            }

            String[] srcDirStrs = cl.getOptionValues(OPT_SRCDIR);
            if (srcDirStrs == null || srcDirStrs.length == 0)
                usage(opts, "Missing --" + OPT_SRCDIR + " option");
            List<File> srcDirsList = new ArrayList<>(srcDirStrs.length);
          for (String srcDirStr : srcDirStrs) {
            File srcDir = new File(srcDirStr);
            if (srcDir.exists())
              srcDirsList.add(srcDir);
            else
              System.err.printf("Source directory %s does not exist\n",
                  srcDir.getAbsolutePath());
          }
            if (srcDirsList.size() < 1)
                usage(opts, "No valid source directory found");
            File[] srcDirs = new File[srcDirsList.size()];
            srcDirsList.toArray(srcDirs);

            String destDirStr = cl.getOptionValue(OPT_DESTDIR);
            if (destDirStr == null)
                usage(opts, "Missing --" + OPT_DESTDIR + " option");
            File destDir = new File(destDirStr);
            if (!destDir.exists()) {
                boolean created = destDir.mkdirs();
                if (!created) {
                    System.err.printf(
                            "Unable to create destination directory %s\n", destDir
                                    .getAbsolutePath());
                    System.exit(1);
                }
            }
            if (!destDir.canWrite()) {
                System.err.printf("Destination directory %s is not writable\n",
                        destDir.getAbsolutePath());
                System.exit(1);
            }

            String title = cl.getOptionValue(OPT_TITLE);
            if (title == null)
                title = srcDirs[0].getAbsoluteFile().getName();

            Date startAt = parseTimestampOption(cl, opts, OPT_START_AT);
            Date endAt = parseTimestampOption(cl, opts, OPT_END_AT);
            Date aggStartAt = parseTimestampOption(cl, opts, OPT_AGGREGATE_START_AT);
            Date aggEndAt = parseTimestampOption(cl, opts, OPT_AGGREGATE_END_AT);

            boolean noSummary = cl.hasOption('n');
            ChartUtil app = new ChartUtil(confFiles, srcDirs, destDir, title,
                    startAt, endAt, aggStartAt, aggEndAt, noSummary);
            app.doit();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println();
            usage(opts);
        }
    }

    public ChartUtil(File[] confFiles, File[] srcDirs, File destDir,
            String title, Date startAt, Date endAt, Date aggregateStartAt,
            Date aggregateEndAt, boolean skipSummary) {
        mConfs = confFiles;
        mSrcDirs = srcDirs;
        mDestDir = destDir;
        mTitle = title;
        mStartAt = startAt != null ? startAt : new Date(Long.MIN_VALUE);
        mEndAt = endAt != null ? endAt : new Date(Long.MAX_VALUE);
        mAggregateStartAt = aggregateStartAt != null ? aggregateStartAt
                : new Date(Long.MIN_VALUE);
        mAggregateEndAt = aggregateEndAt != null ? aggregateEndAt : new Date(
                Long.MAX_VALUE);
        mSkipSummary = skipSummary;
        mUniqueDataColumns = new HashSet<>();
        mUniqueStringColumns = new HashSet<>();
        mColumnsByInfile = new HashMap<>();
        mStringSeries = new HashMap<>();
        mDataSeries = new HashMap<>();
        mAggregates = new HashMap<>();
        mAggregator = new Aggregator();
        mStats = new HashMap<>();
    }

    private void doit() throws Exception {
        List<ChartSettings> allSettings = getAllChartSettings(mConfs);
        readCsvFiles();
        List<ChartSettings> outDocSettings = new ArrayList<>();
        HashSet<String> outDocNames = new HashSet<>();
        for (Iterator<ChartSettings> i = allSettings.iterator();
                i.hasNext();) {
            ChartSettings cs = i.next();
            mCharts.addAll(createJFReeChart(cs));
            if (cs.getOutDocument() == null || cs.getGroupPlots().size() == 0)
                computeAggregates(cs, mAggregateStartAt, mAggregateEndAt);
            else if (cs.getOutDocument() != null) {
                outDocSettings.add(cs);
                outDocNames.add(cs.getOutDocument());
                i.remove();
            }
        }
        for (ChartSettings cs : mSyntheticChartSettings) {
            computeAggregates(cs, mAggregateStartAt, mAggregateEndAt);
            outDocNames.add(cs.getOutDocument());
        }
        outDocSettings.addAll(mSyntheticChartSettings);
        outDocNames.remove(null); // lazy, instead of checking for null above

        lineUpAxes();
        writeAllCharts(allSettings, outDocNames);
        writeOutDocCharts(mSyntheticChartSettings, outDocNames);
        if (!mSkipSummary)
            writeSummary(allSettings);
    }

    private List<ChartSettings> getAllChartSettings(File[] chartDefs)
            throws Exception {
        List<ChartSettings> allSettings = new ArrayList<>();
        for (File def : chartDefs) {
            List<ChartSettings> settings = XMLChartConfig.load(def);
            if (settings.size() == 0) {
                System.err.println("No chart settings found in "
                        + def.getAbsolutePath());
                System.exit(1);
            }

            // Figure out which columns in which input files are being charted.
            for (ChartSettings cs : settings) {
            	ArrayList<PlotSettings> plots = new ArrayList<>();
            	plots.addAll(cs.getPlots());
            	plots.addAll(cs.getGroupPlots());
                for (PlotSettings ps : plots) {
                    String infile = ps.getInfile();
                    String column = ps.getDataColumn();
                    if (column == null) {
                        String[] top = ps.getRatioTop().split("\\+");
                        String[] bottom = ps.getRatioBottom().split("\\+");
                        ArrayList<String> cols = new ArrayList<>();
                        cols.addAll(Arrays.asList(top));
                        cols.addAll(Arrays.asList(bottom));
                        for (String c : cols) {
                            DataColumn dc = new DataColumn(infile, c);
                            mUniqueDataColumns.add(dc);
                        }
                    } else {
                        DataColumn dc = new DataColumn(infile, column);
                        mUniqueDataColumns.add(dc);
                    }
                }
                for (GroupPlotSettings gps : cs.getGroupPlots()) {
                    String infile = gps.getInfile();
                    String column = gps.getGroupBy();
                    DataColumn dc = new DataColumn(infile, column);
                    mUniqueStringColumns.add(dc);
                    mStringSeries.put(dc, new StringSeries());
                }
            }
            allSettings.addAll(settings);
        }
        validateChartSettings(allSettings);

        // Figure out which column are being used in each infile.
        for (DataColumn dc : mUniqueDataColumns) {
            String infile = dc.getInfile();
            String column = dc.getColumn();
            Set<Pair<String, DataSeries>> cols = mColumnsByInfile.get(infile);
            if (cols == null) {
                cols = new HashSet<>();
                mColumnsByInfile.put(infile, cols);
            }
            DataSeries series = new DataSeries();
            mDataSeries.put(dc, series);
            Pair<String, DataSeries> colSeries = new Pair<>(
                column, series);
            cols.add(colSeries);
        }

        return allSettings;
    }

    private void computeAggregates(ChartSettings cs, Date startAt, Date endAt) {
        double agg = 0;
        List<PlotSettings> plots = cs.getPlots();
        for (PlotSettings ps : plots) {
            DataColumn dc = new DataColumn(ps.getInfile(), ps.getDataColumn());
            String key = ps.getInfile() + ":" + ps.getDataColumn() + ":"
                    + ps.getAggregateFunction();
            PlotDataIterator pdIter = new PlotDataIterator(ps, mDataSeries.get(dc));
            agg = mAggregator.compute(pdIter, ps.getAggregateFunction(), startAt, endAt, key);
            mAggregates.put(dc, agg);
        }
    }

    private void writeSummary(List<ChartSettings> allSettings)
            throws IOException {
        File resultCsv = new File(mDestDir, SUMMARY_CSV);
      try (FileWriter writer = new FileWriter(resultCsv)) {
        int count = 0;
        String key = "";
        Double val = null;
        for (ChartSettings cs : allSettings) {
          for (PlotSettings ps : cs.getPlots()) {
            DataColumn dc = new DataColumn(ps.getInfile(), ps
                .getDataColumn());
            key = ps.getInfile() + ":" + ps.getDataColumn() + ":"
                + ps.getAggregateFunction();
            val = mAggregates.get(dc);
            count++;
            mStats.put(key, val);
          }
        }
        Iterator<String> keyset = mStats.keySet().iterator();
        StringBuilder keys = new StringBuilder();
        StringBuilder vals = new StringBuilder();

        while (keyset.hasNext()) {
          key = keyset.next();
          keys.append(key).append(",");
          vals.append(formatDouble(mStats.get(key)))
              .append(",");
        }

        writer.write(keys.toString());
        writer.write("\n");
        writer.write(vals.toString());
        writer.write("\n");
      }

        SummaryAnalyzer analyzer = new SummaryAnalyzer(allSettings);
        analyzer.writeReport(resultCsv);
    }

    private void writeOutDocCharts(List<ChartSettings> outChartSettings,
            Set<String> linkedDocuments) throws IOException {
        // write all charts
        for (String filename : linkedDocuments) {
            File file = new File(mDestDir, filename);
          try (FileWriter writer = new FileWriter(file, false)) {
            writer.write("<html><head><title>");
            writer.write(StringUtil.escapeHtml(mTitle) + ": " + filename);
            writer.write("</title><body bgcolor=\"#eeeeee\"><h1>");
            writer.write(StringUtil.escapeHtml(mTitle) + ": " + filename);
            writer.write("</h1>\n");
          }
        }
        for (ChartSettings cs : outChartSettings) {
            JFreeChart chart = mChartMap.get(cs);
            if (chart != null && hasData(chart)) {
                writeChart(chart, cs);
              try (FileWriter writer = new FileWriter(
                  new File(mDestDir, cs.getOutDocument()), true)) {
                List<PlotSettings> plots = cs.getPlots();
                StringBuilder statString = new StringBuilder();
                boolean first = true;
                for (PlotSettings ps : plots) {
                  DataColumn dc = new DataColumn(ps.getInfile(),
                      ps.getDataColumn());
                  DataSeries ds = mDataSeries.get(dc);
                  if (ds == null || ds.size() == 0)
                    continue;
                  if (first)
                    first = false;
                  else
                    statString.append(" &nbsp;&nbsp; ");
                  statString.append(ps.getAggregateFunction()).append("(")
                      .append(ps.getLegend()).append(") = ")
                      .append(formatDouble(mAggregates.get(dc)));
                }

                writer.write("<a name=\"" + cs.getOutfile() + "\">");
                writer.write("<h3>" + cs.getTitle() + "</h3></a>\n");
                writer.write("<h5>" + statString + "</h5>\n");
                writer.write(String.format(
                    "<img src=\"%s\" width=\"%d\" height=\"%d\">\n",
                    cs.getOutfile(), cs.getWidth(), cs.getHeight()));
              }
            }
        }
        for (String filename : linkedDocuments) {
            File file = new File(mDestDir, filename);
          try (FileWriter writer = new FileWriter(file, true)) {
            writer.write("</body></html>\n");
          }
        }
    }
    private void writeAllCharts(List<ChartSettings> allSettings,
            Set<String> linkedDocuments) throws IOException {
        // write all charts
        for (ChartSettings cs : allSettings) {
            JFreeChart chart = mChartMap.get(cs);
            if (chart != null)
                writeChart(chart, cs);
        }

        writeIndexHtml(allSettings, linkedDocuments);
    }

    private void writeIndexHtml(List<ChartSettings> allSettings,
            Set<String> linkedDocuments) throws IOException {
        // Generate HTML
        System.out.println("Writing index.html");
      try (FileWriter writer = new FileWriter(new File(mDestDir, "index.html"))) {
        writer.write("<html>\n<head>\n<title>"
            + StringUtil.escapeHtml(mTitle) + " "
            + DateFormat.getDateInstance().format(mMinDate)
            + "</title>\n</head>\n<body bgcolor=\"#eeeeee\">\n");
        writer.write("<h1>" + StringUtil.escapeHtml(mTitle) + "</h1>\n");

        if (linkedDocuments.size() > 0) {
          writer.write("<h2>Additional charts</h2>");
          writer.write("<ul>");
          for (String document : linkedDocuments) {
            writer.write("<li><a href=\"" + document + "\">");
            writer.write(document);
            writer.write("</a></li>\n");
          }
          writer.write("</ul>\n");
        }
        List<String> noData = new ArrayList<>();

        int count = 0;
        for (ChartSettings cs : allSettings) {
          JFreeChart chart = mChartMap.get(cs);
          if (chart == null)
            continue;

          List<PlotSettings> plots = cs.getPlots();
          StringBuilder statString = new StringBuilder();
          boolean first = true;
          for (PlotSettings ps : plots) {
            DataColumn dc = new DataColumn(ps.getInfile(), ps.getDataColumn());
            DataSeries ds = mDataSeries.get(dc);
            if (ds == null || ds.size() == 0)
              continue;
            if (first)
              first = false;
            else
              statString.append(" &nbsp;&nbsp; ");
            statString.append(ps.getAggregateFunction()).append("(").append(ps.getLegend())
                .append(") = ").append(formatDouble(mAggregates.get(dc)));
            count++;
          }

          if (hasData(chart)) {
            writer.write("<a name=\"" + cs.getOutfile() + "\">");
            writer.write("<h3>" + cs.getTitle() + "</h3></a>\n");
            writer.write("<h5>" + statString + "</h5>\n");
            writer.write(String.format(
                "<img src=\"%s\" width=\"%d\" height=\"%d\">\n", cs
                    .getOutfile(), cs.getWidth(), cs
                    .getHeight()));
          } else {
            noData.add(cs.getTitle());
          }
        }

        if (noData.size() > 0) {
          writer.write("<h3>No data available for the following charts:</h3>\n");
          writer.write("<p>\n");
          for (String str : noData) {
            writer.write(StringUtil.escapeHtml(str));
            writer.write("\n");
          }
          writer.write("<p>\n");
        }

        if (!mSkipSummary)
          writer.write("<h4><a href=\"" + SummaryConstants.SUMMARY_TXT
              + "\">Summary</a></h4>\n");

        writer.write("</body>\n</html>\n");
      }
    }

    /**
     * Reader that combines files of same name in multiple directories. Both raw
     * file and gzipped version are considered.
     */
    private static class MultipleDirsFileReader extends Reader {
        private final String mFilename;
        private final File[] mDirs;
        private int mFileIndex;
        private Reader mReader;

        public MultipleDirsFileReader(String filename, File[] dirs)
                throws IOException {
            mFilename = filename;
            mDirs = dirs;
            mFileIndex = -1;
            openNextReader();
            if (mReader == null)
                throw new FileNotFoundException("File " + filename
                        + " not found in any of the source directories");
        }

        private void openNextReader() throws IOException {
            if (mReader != null) {
                mReader.close();
                mReader = null;
            }
            while (mFileIndex < mDirs.length - 1) {
                mFileIndex++;
                File dir = mDirs[mFileIndex];
                File file = new File(dir, mFilename);
                if (!file.exists()) {
                    if (!mFilename.endsWith(".gz"))
                        file = new File(dir, mFilename + ".gz");
                    else if (mFilename.length() > 3)
                        file = new File(dir, mFilename.substring(0, mFilename
                                .length() - 3));
                    else
                        // mFileame == ".gz"; bad input
                        continue;
                    if (!file.exists())
                        continue;
                }
                String filename = file.getName();
                if (!filename.endsWith(".gz")) {
                    mReader = new FileReader(file);
                } else {
                    FileInputStream fis = null;
                    GZIPInputStream gis = null;
                    try {
                        fis = new FileInputStream(file);
                        gis = new GZIPInputStream(fis);
                        mReader = new InputStreamReader(gis);
                    } finally {
                        if (mReader == null) {
                            try {
                                if (gis != null)
                                    gis.close();
                                else if (fis != null)
                                    fis.close();
                            } catch (IOException ee) {
                            }
                        }
                    }
                }
                break;
            }
        }

        @Override
        public void close() throws IOException {
            if (mReader != null) {
                mReader.close();
                mReader = null;
            }
        }

        @Override
        public int read(char[] cbuf, int offset, int len) throws IOException {
            if (mReader == null)
                return -1; // EOF
            int charsRead = mReader.read(cbuf, offset, len);
            if (charsRead != -1)
                return charsRead;

            // EOF on current mReader
            openNextReader();
            if (mReader == null)
                return -1;

            // Insert an LF to make sure the first line from new reader doesn't
            // get
            // accidentally appended to last incomplete line from previous
            // reader.
            cbuf[offset] = '\n';
            if (len == 1 || offset + 1 == cbuf.length)
                return 1;
            charsRead = mReader.read(cbuf, offset + 1, len - 1);
            if (charsRead >= 0)
                return charsRead + 1;
            else
                return charsRead;
        }
    }

    private Date readTS(CsvReader r, String ctx) throws ParseException {
        Date ts = null;
        String tstamp = r.getValue(PlotSettings.TSTAMP_COLUMN);
        if (tstamp == null) {
            throw new ParseException(ctx + ": no timestamp found.", 0);
        }

        // Try all date parsers until one succeeds
        ParseException lastException = null;
        for (int i = 0; i < sDateFormats.length && ts == null; i++) {
            try {
                synchronized (sDateFormats[i]) {
                    ts = sDateFormats[i].parse(tstamp);
                }
            } catch (ParseException e) {
                    lastException = e;
            }
            if (lastException != null) {
                throw lastException;
            }
        }
        return ts;
    }
    private void readCsvFiles() throws Exception {
        Date minDate = null;
        Date maxDate = null;

        // GROUP PLOT SUPPORT
        // the downside of this loop is that it will re-open the file once
        // for each column name, if more than one column name is specified
        // per-file--shouldn't happen much since this is only for groupplot
        for (DataColumn c : mUniqueStringColumns) {
            String inFilename = c.getInfile();
            Reader reader = null;
            StringSeries data = mStringSeries.get(c);
            try {
                reader = new MultipleDirsFileReader(inFilename, mSrcDirs);
            }
            catch (FileNotFoundException e) {
                System.out.printf("CSV file %s not found; Skipping...\n",
                        inFilename);
                continue;
            }
            CsvReader csv = null;

            try {
                csv = new CsvReader(reader);
                int line = 1;
                while (csv.hasNext()) {
                    line++;
                    String ctx = inFilename + ", line " + line;
                    Date ts = null;
                    try {
                        ts = readTS(csv, ctx);
                    }
                    catch (ParseException e) {
                        if (e.getMessage().compareTo(
                                "Unparseable date: \"timestamp\"") != 0) {
                            //bug 54626, ignored the repeat timestamp header
                            System.out.println(ctx + ": " + e);
                        }
                        continue;
                    }

                    if (ts.before(mStartAt) || ts.after(mEndAt))
                        continue;
                    if (minDate == null) {
                        minDate = ts;
                        maxDate = ts;
                    } else {
                        if (ts.compareTo(minDate) < 0)
                            minDate = ts;
                        if (ts.compareTo(maxDate) > 0)
                            maxDate = ts;
                    }

                    String value = csv.getValue(c.getColumn());
                    data.AddEntry(ts, value);
                }
            }
            finally {
                if (csv != null)
                    csv.close();
            }
        }
        // Read CSVs and populate data series.
      for (Map.Entry<String, Set<Pair<String, DataSeries>>> entry : mColumnsByInfile
          .entrySet()) {
        String inFilename = entry.getKey();
        Set<Pair<String, DataSeries>> columns = entry.getValue();

        System.out.println("Reading CSV " + inFilename);
        Reader reader = null;
        try {
          reader = new MultipleDirsFileReader(inFilename, mSrcDirs);
        } catch (FileNotFoundException e) {
          System.out.printf("CSV file %s not found; Skipping...\n",
              inFilename);
          continue;
        }

        CsvReader csv = null;
        try {
          csv = new CsvReader(reader);
          int line = 1;
          while (csv.hasNext()) {
            line++;
            String context = inFilename + ", line " + line;

            Date ts = null;
            try {
              ts = readTS(csv, context);
            } catch (ParseException e) {
              if (e.getMessage().compareTo(
                  "Unparseable date: \"timestamp\"") != 0) {
                //bug 54626, ignored the repeat timestamp header
                System.out.println(context + ": " + e);
              }
              continue;
            }

            if (ts.before(mStartAt) || ts.after(mEndAt))
              continue;

            // Set min/max date
            if (minDate == null) {
              minDate = ts;
              maxDate = ts;
            } else {
              if (ts.compareTo(minDate) < 0) {
                minDate = ts;
              }
              if (ts.compareTo(maxDate) > 0) {
                maxDate = ts;
              }
            }

            // Parse values
            for (Pair<String, DataSeries> colSeries : columns) {
              String column = colSeries.getFirst();
              DataSeries series = colSeries.getSecond();
              String val = csv.getValue(column);
              if (!StringUtil.isNullOrEmpty(val)) {
                try {
                  double d = Double.parseDouble(val);
                  try {
                    series.AddEntry(ts, d);
                  } catch (SeriesException e) {
                    System.out.printf(
                        "Can't add sample to series: timestamp=%s, value=%s\n",
                        ts, val);
                    e.printStackTrace(System.out);
                  }
                } catch (NumberFormatException e) {
                  System.out.printf(
                      "%s: unable to parse value '%s' for %s: %s%n",
                      context, val, column, e);
                }
              } else { // default an entry to 0 if string is empty
                series.AddEntry(ts, 0.0);
              }
            }
          }

          for (Pair<String, DataSeries> colSeries : columns) {
            String column = colSeries.getFirst();
            DataSeries series = colSeries.getSecond();
            System.out.format(
                "Adding %d %s points between %s and %s.\n\n",
                series.size(), column, minDate, maxDate);
          }
        } finally {
          if (csv != null)
            csv.close();
        }
      }

        adustSampleRange(minDate, maxDate);
    }

    private void adustSampleRange(Date minDate, Date maxDate) {
        if (minDate != null) {
            long chartMinDate = minDate.getTime();
            if (chartMinDate < mMinDate)
                mMinDate = chartMinDate;
        }
        if (maxDate != null) {
            long chartMaxDate = maxDate.getTime();
            if (chartMaxDate > mMaxDate)
                mMaxDate = chartMaxDate;
        }
    }

    private void validateChartSettings(List<ChartSettings> allSettings)
            throws ParseException {
        // Make sure we're not writing the same chart twice
        Set<String> usedFilenames = new HashSet<>();
        for (ChartSettings cs : allSettings) {
            String filename = cs.getOutfile();
            if (usedFilenames.contains(filename)) {
                throw new ParseException("Found two charts that write "
                        + filename, 0);
            }
            usedFilenames.add(filename);
        }
    }

    private static class PlotDataIterator implements Iterator<Pair<Date, Double>> {

        private static enum Func { IDENTITY, DIFF, SUM };

        private final PlotSettings mPlotSettings;
        private final DataSeries mDataSeries;
        private long mLastTstamp;
        private Func mFunc;
        private double mSum;
        private double mLast;
        private int mIndex;

        public PlotDataIterator(PlotSettings ps, DataSeries ds) {
            mPlotSettings = ps;
            mDataSeries = ds;
            mIndex = 0;
            mSum = 0.0;
            // for diff function, if NaN
            // then set mLast to current value and result is 0
            mLast = Double.NaN;
            mLastTstamp = 0;
            String func = mPlotSettings.getDataFunction();
            if (PlotSettings.DATA_FUNCTION_DIFF.equals(func))
                mFunc = Func.DIFF;
            else if (PlotSettings.DATA_FUNCTION_SUM.equals(func))
                mFunc = Func.SUM;
            else
                mFunc = Func.IDENTITY;
        }

        @Override
        public boolean hasNext() {
            return mIndex < mDataSeries.size();
        }

        @Override
        public Pair<Date, Double> next() {
            if (!hasNext())
                return null;
            Entry entry = mDataSeries.get(mIndex);
            mIndex++;

            double val = entry.getVal();
            val *= mPlotSettings.getMultiplier();
            double divisor = mPlotSettings.getDivisor();
            if (divisor != 0.0)
                val /= divisor;
            if (mFunc.equals(Func.DIFF)) {
                if (Double.isNaN(mLast)) // initialize
                    mLast = val;
                double diff = val - mLast;
                mLast = val;
                val = diff;
            } else if (mFunc.equals(Func.SUM)) {
                mSum += val;
                val = mSum;
            }
            // Non-negative setting is used to detect counter resets.
            // Counter gets reset on server restarts.
            if (val < 0 && mPlotSettings.getNonNegative())
                val = 0;
            Date t = entry.getTimestamp();
            long tl = t.getTime();
            if (mPlotSettings.getPercentTime()) {
                // val is in milliseconds.  Express it as percentage of time
                // since the last sample.
                if (mLastTstamp > 0) {
                    long dt = tl - mLastTstamp;
                    if (dt > 0)
                        val /= dt;
                    else
                        val = 0;
                } else
                    val = 0;
                val *= 100;  // percent
            }
            mLastTstamp = tl;
            return new Pair<>(t, val);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static class PlotAggregatePair implements Comparable<PlotAggregatePair> {
        private final PlotSettings ps;
        private final double aggregate;
        PlotAggregatePair(PlotSettings ps, double aggregate) {
            this.ps = ps; this.aggregate = aggregate;
        }
        @Override
        public int compareTo(PlotAggregatePair o) {
            double comparison = o.aggregate - aggregate;
            if (comparison == 0.0)
                return 0;
            return comparison > 0.0 ? -1 : 1;
        }
    }
    private List<JFreeChart> createJFReeChart(ChartSettings cs) {

        double minValue = Double.MAX_VALUE;
        double maxValue = Double.MIN_VALUE;
        double d = 0;
        double count = 0;
        double total = 0;
        TimeSeriesCollection data = new TimeSeriesCollection();

        ArrayList<ChartSettings> syntheticSettings =
            new ArrayList<>();
        for (GroupPlotSettings gps : cs.getGroupPlots()) {
            String groupBy = gps.getGroupBy();
            DataColumn dc = new DataColumn(gps.getInfile(), groupBy);
            StringSeries groupBySeries = mStringSeries.get(dc);
            dc = new DataColumn(gps.getInfile(), gps.getDataColumn());
            DataSeries ds = mDataSeries.get(dc);
            int idx = 0;
            Map<String,List<Integer>> groups =
                new HashMap<>();
            for (StringEntry e : groupBySeries.dataCollection) {
                String g = e.getVal();
                List<Integer> indices = groups.get(g);
                if (indices == null) {
                    indices = new ArrayList<>();
                    groups.put(g, indices);
                }
                indices.add(idx);
                idx++;
            }
            for (Map.Entry<String,List<Integer>> g : groups.entrySet()) {
                String groupByValue = g.getKey();
                if (gps.getIgnoreSet().contains(groupByValue))
                    continue;
                List<Integer> indices = g.getValue();
                DataSeries syntheticDS = new DataSeries();
                DataColumn c = new DataColumn(gps.getInfile(),
                        GROUP_PLOT_SYNTHETIC + groupByValue + ":" +
                        gps.getDataColumn());
                for (int i : indices) {
                    Entry e = ds.get(i);
                    syntheticDS.AddEntry(e.getTimestamp(), e.getVal());
                }
                mDataSeries.put(c, syntheticDS);
                PlotSettings syntheticPlot = new PlotSettings(
                        groupByValue, c.getInfile(), c.getColumn(),
                        gps.getShowRaw(),           gps.getShowMovingAvg(),
                        gps.getMovingAvgPoints(),   gps.getMultiplier(),
                        gps.getDivisor(),           gps.getNonNegative(),
                        gps.getPercentTime(),       gps.getDataFunction(),
                        gps.getAggregateFunction(), gps.getOptional(),
                        null, null);
                cs.addPlot(syntheticPlot);
                if (cs.getOutDocument() != null) {
                    ChartSettings s = new ChartSettings(
                            String.format(cs.getTitle(), groupByValue),
                            cs.getCategory(),
                            String.format(cs.getOutfile(), groupByValue),
                            cs.getXAxis(),         cs.getYAxis(),
                            cs.getAllowLogScale(), cs.getPlotZero(),
                            cs.getWidth(),         cs.getHeight(), null,
                            cs.getTopPlots(),      cs.getTopPlotsType());
                    s.addPlot(syntheticPlot);
                    syntheticSettings.add(s);
                }
            }
        }
        if (cs.getOutDocument() != null && cs.getGroupPlots().size() != 0) {
            ArrayList<JFreeChart> charts = new ArrayList<>();
            for (ChartSettings c : syntheticSettings) {
                charts.addAll(createJFReeChart(c));
                c.setOutDocument(cs.getOutDocument());
            }
            mSyntheticChartSettings.addAll(syntheticSettings);
            return charts;
        }

        List<PlotSettings> plots = cs.getPlots();
        if (cs.getTopPlots() > 0 && plots.size() > cs.getTopPlots()) {
            String aggregateFunction = cs.getTopPlotsType().name().toLowerCase();
            System.out.printf("Reducing %d to %d plots for chart '%s'%n",
                    plots.size(), cs.getTopPlots(), cs.getTitle());
            ArrayList<PlotAggregatePair> aggregates = new ArrayList<>();
            for (PlotSettings ps : plots) {
                DataColumn dc = new DataColumn(ps.getInfile(), ps.getDataColumn());
                String key = ps.getInfile() + ":" + ps.getDataColumn() + ":" +
                        ps.getAggregateFunction();
                PlotDataIterator pdIter = new PlotDataIterator(ps, mDataSeries.get(dc));
                double aggregate = mAggregator.compute(pdIter, aggregateFunction,
                        mAggregateStartAt, mAggregateEndAt, key);
                aggregates.add(new PlotAggregatePair(ps, aggregate));
            }
            Collections.sort(aggregates);
            while (aggregates.size() > cs.getTopPlots()) {
                PlotAggregatePair pair = aggregates.remove(0);
                plots.remove(pair.ps);
            }
        }
        for (PlotSettings ps : plots) {
            String columnName = ps.getDataColumn();
            if (columnName == null) {
                columnName = RATIO_PLOT_SYNTHETIC + ps.getRatioTop() +
                        "/" + ps.getRatioBottom();
                String infile = ps.getInfile();

                String[] top = ps.getRatioTop().split("\\+");
                String[] bottom = ps.getRatioBottom().split("\\+");

                DataColumn[] ratioTop = new DataColumn[top.length];
                DataColumn[] ratioBottom = new DataColumn[bottom.length];

                for (int i = 0, j = top.length; i < j; i++)
                    ratioTop[i] = new DataColumn(infile, top[i]);
                for (int i = 0, j = bottom.length; i < j; i++)
                    ratioBottom[i] = new DataColumn(infile, bottom[i]);

                DataSeries[] topData = new DataSeries[ratioTop.length];
                DataSeries[] bottomData = new DataSeries[ratioBottom.length];

                for (int i = 0, j = ratioTop.length; i < j; i++)
                    topData[i] = mDataSeries.get(ratioTop[i]);
                for (int i = 0, j = ratioBottom.length; i < j; i++)
                    bottomData[i] = mDataSeries.get(ratioBottom[i]);

                DataSeries ds = new DataSeries();
                for (int i = 0, j = topData[0].size(); i < j; i++) {
                    double topValue = 0.0;
                    double bottomValue = 0.0;
                    double ratio = 0.0;
                    Entry lastEntry = null;
                  for (DataSeries topDatum : topData) {
                    Entry e = topDatum.get(i);
                    topValue += e.getVal();
                  }
                  for (DataSeries bottomDatum : bottomData) {
                    Entry e = bottomDatum.get(i);
                    bottomValue += e.getVal();
                    lastEntry = e;
                  }
                    if (bottomValue != 0.0) {
                        ratio = topValue / bottomValue;
                    }
                    // should never be null
                    assert lastEntry != null;
                    ds.AddEntry(lastEntry.getTimestamp(), ratio);
                }
                mDataSeries.put(new DataColumn(infile, columnName), ds);
                ps.setDataColumn(columnName);
            }
            DataColumn dc = new DataColumn(ps.getInfile(), ps.getDataColumn());
            DataSeries ds = mDataSeries.get(dc);
            TimeSeries ts = new TimeSeries(ps.getLegend(),
                    FixedMillisecond.class);
            int numSamples = 0;
            for (PlotDataIterator pdIter = new PlotDataIterator(ps, ds); pdIter.hasNext();
                 numSamples++) {
                Pair<Date, Double> entry = pdIter.next();
                Date tstamp = entry.getFirst();
                double val = entry.getSecond();
                if (val != 0 || cs.getPlotZero()) {
                    if (d < minValue)
                        minValue = val;
                    if (d > maxValue)
                        maxValue = val;
                    count++;
                    total += val;
                    try {
                        ts.addOrUpdate(new FixedMillisecond(tstamp), val);
                    } catch (SeriesException e) {
                        e.printStackTrace(System.out);
                    }
                }
            }
            if (numSamples == 0 && ps.getOptional()) {
                System.out.format("Skipping optional plot %s (no data sample found)\n\n",
                                  ps.getLegend());
                continue;
            }
            System.out.format("Adding %d %s points to %s.\n\n", ds.size(), ps
                    .getLegend(), cs.getOutfile());
            if (ps.getShowRaw()) {
                data.addSeries(ts);
            }
            if (ps.getShowMovingAvg()) {
                int numPoints = ps.getMovingAvgPoints();
                if (numPoints == PlotSettings.DEFAULT_PLOT_MOVING_AVG_POINTS) {
                    // Display 200 points for moving average.
                    // Divide the total number of points by 200 to
                    // determine the number of samples to average
                    // for each point.
                    numPoints = ts.getItemCount() / 200;
                }
                if (numPoints >= 2) {
                    TimeSeries ma = MovingAverage.createPointMovingAverage(ts,
                            ps.getLegend() + " (moving avg)", numPoints);
                    data.addSeries(ma);
                } else {
                    System.out
                            .println("Not enough data to display moving average for "
                                    + ps.getLegend());
                    data.addSeries(ts);

                }
            }

        }
        // Create chart
        boolean legend = (data.getSeriesCount() > 1);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(null, cs
                .getXAxis(), cs.getYAxis(), data, legend, false, false);

        // Make Y-axis logarithmic if a spike was detected
        if (cs.getAllowLogScale() && (minValue > 0) && (maxValue > 0)
                && (maxValue > 20 * (total / count))) {
            if (maxValue / minValue > 100) {
                XYPlot plot = (XYPlot) chart.getPlot();
                ValueAxis oldAxis = plot.getRangeAxis();
                LogarithmicAxis newAxis = new LogarithmicAxis(oldAxis
                        .getLabel());
                plot.setRangeAxis(newAxis);
            }
        }

        mChartMap.put(cs, chart);
        return Arrays.asList(chart);

    }

    private boolean hasData(JFreeChart chart) {
        if (chart == null) {
            return false;
        }
        XYPlot plot = chart.getXYPlot();
        XYDataset data = plot.getDataset();

        int numPoints = 0;
        for (int i = 0; i < data.getSeriesCount(); i++) {
            numPoints += data.getItemCount(i);
        }
        if (numPoints == 0) {
            return false;
        }
        return true;
    }

    /**
     * Updates axes for all charts so that they display the same time interval.
     */
    private void lineUpAxes() {

        for (JFreeChart chart : mCharts) {
            XYPlot plot = (XYPlot) chart.getPlot();
            DateAxis axis = (DateAxis) plot.getDomainAxis();
            Date chartMinDate = axis.getMinimumDate();
            Date chartMaxDate = axis.getMaximumDate();

            if (chartMinDate != null && mMinDate < chartMinDate.getTime()) {
                axis.setMinimumDate(new Date(mMinDate));
            }
            if (chartMaxDate != null && mMaxDate > chartMaxDate.getTime()) {
                axis.setMaximumDate(new Date(mMaxDate));
            }
        }
    }

    private void writeChart(JFreeChart chart, ChartSettings cs)
            throws IOException {
        String filename = cs.getOutfile();
        System.out.println("Writing " + filename);
        File file = new File(mDestDir, filename);
        if (cs.getImageType() == ImageType.PNG) {
            ChartUtilities.saveChartAsPNG(file, chart, cs.getWidth(), cs
                    .getHeight());
        } else {
            ChartUtilities.saveChartAsJPEG(file, 90, chart, cs.getWidth(), cs
                    .getHeight());
        }
    }

    static class Entry {
        Date timestamp;

        double value;

        public Entry(Date t, double v) {
            this.timestamp = t;
            this.value = v;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public double getVal() {
            return value;
        }
    }

    static class DataSeries {
        List<Entry> dataCollection = new ArrayList<>();

        public void AddEntry(Date t, double d) {
            Entry entry = new Entry(t, d);
            dataCollection.add(entry);
        }

        public int size() {
            return dataCollection.size();
        }

        public Entry get(int index) {
            return dataCollection.get(index);
        }
    }
    static class StringEntry {
        Date timestamp;

        String value;

        public StringEntry(Date t, String v) {
            this.timestamp = t;
            this.value = v;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public String getVal() {
            return value;
        }
    }
    static class StringSeries {
        List<StringEntry> dataCollection = new ArrayList<>();

        public void AddEntry(Date t, String s) {
            StringEntry entry = new StringEntry(t, s);
            dataCollection.add(entry);
        }

        public int size() {
            return dataCollection.size();
        }

        public StringEntry get(int index) {
            return dataCollection.get(index);
        }
    }

    static class Aggregator {
        HashMap<String, Integer> set = new HashMap<>();

        public Aggregator() {
        }

        private double getAvg(PlotDataIterator pdIter, Date startAt, Date endAt) {
            int count = 0;
            double val = 0;
            while (pdIter.hasNext()) {
                Pair<Date, Double> entry = pdIter.next();
                Date date = entry.getFirst();
                if (!date.before(startAt) && !date.after(endAt)) {
                    val += entry.getSecond();
                    count++;
                }
            }
            return count > 0 ? val / count : 0;
        }

        private double getMax(PlotDataIterator pdIter, Date startAt, Date endAt) {
            double val = 0;
            while (pdIter.hasNext()) {
                Pair<Date, Double> entry = pdIter.next();
                Date date = entry.getFirst();
                if (!date.before(startAt) && !date.after(endAt))
                    val = Math.max(val, entry.getSecond());
            }
            return val;
        }

        private double getMaxPercentage(PlotDataIterator pdIter, Date startAt, Date endAt,
                                        String key) {
            double val = 0;
            while (pdIter.hasNext()) {
                Pair<Date, Double> entry = pdIter.next();
                Date date = entry.getFirst();
                if (!date.before(startAt) && !date.after(endAt)) {
                    val = entry.getSecond();
                    // TODO: What is this function supposed to do?
                    // What is "max percentage"?
                }
            }
            val = 0;
            return val;
        }

        private double getLast(PlotDataIterator pdIter, Date startAt, Date endAt) {
            double val = 0;
            while (pdIter.hasNext()) {
                Pair<Date, Double> entry = pdIter.next();
                Date date = entry.getFirst();
                if (!date.before(startAt) && !date.after(endAt))
                    val = entry.getSecond();
            }
            return val;
        }

        public double compute(PlotDataIterator pdIter, String aggFunc,
                              Date startAt, Date endAt, String key) {
            if (aggFunc.equalsIgnoreCase(PlotSettings.AGG_FUNCTION_LAST))
                return getLast(pdIter, startAt, endAt);
            else if (aggFunc.equalsIgnoreCase(PlotSettings.AGG_FUNCTION_MAX))
                return getMax(pdIter, startAt, endAt);
            else if (aggFunc.equalsIgnoreCase(PlotSettings.AGG_FUNCTION_MAX_PERCENTAGE))
                return getMaxPercentage(pdIter, startAt, endAt, key);
            else
                return getAvg(pdIter, startAt, endAt);
        }
    }

    private static String formatDouble(double d) {
        DecimalFormat formatter = new DecimalFormat("0.00");
        return formatter.format(d);
    }
}
