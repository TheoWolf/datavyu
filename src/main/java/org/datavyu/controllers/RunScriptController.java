/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.datavyu.controllers;

import com.github.rcaller.rstuff.RCallerOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavyu.Datavyu;
import org.datavyu.FileHistory;
import org.datavyu.models.db.*;
import org.datavyu.util.FileFilters.RbFilter;
import org.datavyu.views.ConsoleV;
import org.datavyu.views.DatavyuFileChooser;
import org.jruby.embed.AttributeName;
import com.github.rcaller.rstuff.RCaller;
import com.github.rcaller.rstuff.RCode;

import javax.script.*;
import javax.swing.*;
import java.io.*;
import java.util.*;


/**
 * Controller for running scripts.
 */
public final class RunScriptController extends SwingWorker<Object, String> {

    /** The logger instance for this class */
    private static Logger logger = LogManager.getLogger(RunScriptController.class);

    /** The path to the script file we are executing */
    private final File scriptFile;

    /** The View that the results of the scripting engine are displayed too */
    private JTextArea console = null;

    /**
     * output stream for messages coming from the scripting engine.
     */
    private PipedInputStream consoleOutputStream;
    private PipedInputStream consoleOutputStreamAfter;

    /**
     * input stream for displaying messages from the scripting engine.
     */
    private OutputStreamWriter consoleWriter;
    private OutputStreamWriter consoleWriterAfter;

    private OutputStream sIn;

    private StringBuilder outString = new StringBuilder("");

    /**
     * Constructs and invokes the run script controller.
     *
     * @throws IOException If Unable to create the run script controller.
     */
    public RunScriptController(JFrame parent) throws IOException {
        DatavyuFileChooser fileChooser = new DatavyuFileChooser();
        fileChooser.addChoosableFileFilter(RbFilter.INSTANCE);
        fileChooser.setFileFilter(RbFilter.INSTANCE);

        int result = fileChooser.showOpenDialog(parent);

        if (result == JFileChooser.APPROVE_OPTION) {
            scriptFile = fileChooser.getSelectedFile();
            init();
        } else {
            scriptFile = null;
        }
    }

    public RunScriptController(File scriptFile) throws IOException {
        this.scriptFile = scriptFile;
        init();
    }

    /**
     * Constructs and invokes the runscript controller.
     *
     * @param file The absolute path to the script file you wish to invoke.
     * @throws IOException If unable to create the run script controller.
     */
    public RunScriptController(final String file) throws IOException {
        scriptFile = new File(file);
        init();
    }

    public String getScriptFilePath() {
        return this.scriptFile.getAbsolutePath();
    }

    /**
     * Initialises the controller for running scripts
     *
     * @throws IOException If unable to initialise the controller for running scripts
     */
    private void init() throws IOException {
        Datavyu.getApplication().show(ConsoleV.getInstance());
        console = ConsoleV.getInstance().getConsole();

        consoleOutputStream = new PipedInputStream();
        consoleOutputStreamAfter = new PipedInputStream();
        sIn = new PipedOutputStream(consoleOutputStream);
        OutputStream sIn2 = new PipedOutputStream(consoleOutputStreamAfter);
        consoleWriter = new OutputStreamWriter(sIn);
        consoleWriterAfter = new OutputStreamWriter(sIn2);
    }

    @Override
    protected Object doInBackground() {
        logger.info("run script");

        ReaderThread t = new ReaderThread();
        t.start();

        FileHistory.rememberScript(scriptFile);

        if (scriptFile.getName().endsWith(".rb")) {
            runRubyScript(scriptFile);
        } else if (scriptFile.getName().endsWith(".r") || scriptFile.getName().endsWith(".R")) {
            runRScript(scriptFile);
        }

        // Close the output stream to kill our reader thread
        try {
            consoleWriterAfter.close();
        } catch (Exception e) {
            logger.error("Write Failed! Error: ", e);
        }

        return null;
    }

    private static boolean rubyScriptIsRunning = false;

    private void runRubyScript(File scriptFile) {
        // The scripting engine factory that we use with Datavyu

        if (rubyScriptIsRunning) {
            JOptionPane.showMessageDialog(null, "A script is running. One script at a time!");
            return;
        }
        rubyScriptIsRunning = true;
        outString = new StringBuilder("");
        // init script engine
        System.setProperty("org.jruby.embed.localvariable.behavior", "transient");
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngineFactory scriptEngineFactory = scriptEngineManager.getEngineByName("jruby").getFactory();
        ScriptEngine rubyEngine = scriptEngineFactory.getScriptEngine();
        ScriptContext rubyContext = new SimpleScriptContext();
//        rubyContext.setAttribute("db", Datavyu.getProjectController().getDataStore(), ScriptContext.ENGINE_SCOPE);

        try {
            try {
                consoleWriter.write("\n*************************");
                consoleWriter.write("\nRunning Script: " + scriptFile.getName() + " on project: " +  Datavyu.getProjectController().getProjectNamePretty());
                consoleWriter.write("\n*************************\n");

                consoleWriter.flush();

                // Place reference to various Datavyu functionality.

                logger.info("Project controller uses data store: " + Datavyu.getProjectController().getDataStore());
                String path = System.getProperty("user.dir") + File.separator;
                
                rubyEngine.put("path", path);

                FileReader scriptReader = new FileReader(scriptFile);
                LineNumberReader lineReader = new LineNumberReader(
                        fileReaderIntoStringReader(scriptReader));

                rubyEngine.setContext(rubyContext);
                rubyEngine.getContext().setWriter(consoleWriter);
                rubyEngine.getContext().setErrorWriter(consoleWriter);
                rubyEngine.getContext().setAttribute(AttributeName.TERMINATION.toString(), true,
                        ScriptContext.ENGINE_SCOPE);
                rubyEngine.getContext().setAttribute(AttributeName.CLEAR_VARAIBLES.toString(), true,
                        ScriptContext.ENGINE_SCOPE);
                try{
                    rubyEngine.eval("load 'Datavyu_API.rb'\n");
                    rubyEngine.eval(lineReader);
                    consoleWriter.close();

                    consoleWriterAfter.write("\nScript has finished running.");
                    consoleWriterAfter.flush();
                    consoleWriterAfter.close();
                    lineReader.close();
                }
                catch (ScriptException e) {
                    //unfortunately the above seems to always be final line not line of error. still, no noticeable
                    // performance difference, so im leaving the LineNumberReader wrap
                    String msg = makeFriendlyRubyErrorMsg(outString.toString(), e);
                    consoleWriter.flush();
                    consoleWriter.close();
                    consoleWriterAfter.write("\n\n***** SCRIPT ERROR *****\n");
                    consoleWriterAfter.write(msg);
                    consoleWriterAfter.write("\n*************************\n");
                    consoleWriterAfter.flush();
                    logger.error("Unable to execute script: ", e);
                }
                finally {
                    lineReader.close();
                }
            } catch (FileNotFoundException e) {
                consoleWriter.close();
                consoleWriterAfter.write("File not found: " + e.getMessage());;
                
                consoleWriterAfter.flush();
                logger.error("Unable to execute script: ", e);
            }
        } catch (IOException ioe) {
            logger.error("IO Exception occurred when executing the ruby script", ioe);
        } finally{
            rubyScriptIsRunning = false;
        }
        Datavyu.getView().getSpreadsheetPanel().redrawCells();
    }
    
    private StringReader fileReaderIntoStringReader(FileReader fr) throws IOException
    {
        BufferedReader br = new BufferedReader(fr);
        StringBuilder sb = new StringBuilder("");
        
        String cur = br.readLine();
        while(cur != null)
        {
            sb.append(cur);
            sb.append('\n'); //newlines in string are always '\n', never '\r'. Bug 193
            cur = br.readLine();
        }
        br.close();
        fr.close();
        return new StringReader(sb.toString());
    }

    private String makeFriendlyRubyErrorMsg(String out, ScriptException e) {
        try {
            if (out.lastIndexOf("<script>") == -1) return e.getMessage();
            String s = "";
            //s should begin with ruby-relevant error portion, NOT full java stack
            //which would be of little interest to the user and only obscures what matters
            int endIndex = out.indexOf("org.jruby.embed.EvalFailedException:");
            if (endIndex == -1) s = out.substring(out.lastIndexOf('*') + 1);
            else s = out.substring(out.lastIndexOf('*') + 1, endIndex);

            //for each script error print the relevant line number. these are listed
            //in OPPOSITE of stack order so that the top of the stack (most likely to be
            //where the actual error lies), is the last thing shown and most apparent to user
            String linesOut = "";
            StringTokenizer outputTokenizer = new StringTokenizer(out, "\n");
            while (outputTokenizer.hasMoreTokens()) {
                String curLine = outputTokenizer.nextToken();
                int scriptTagIndex = curLine.lastIndexOf("<script>:");
                if (scriptTagIndex != -1) {
                    int errorLine = Integer.parseInt(curLine.substring(scriptTagIndex).replaceAll("[^0-9]", ""));
                    LineNumberReader scriptLNR = new LineNumberReader(new FileReader(scriptFile));
                    while (scriptLNR.getLineNumber() < errorLine - 1) scriptLNR.readLine(); //advance to errorLine
                    linesOut = "\nSee line " + errorLine + " of " + scriptFile + ":" + "\n" + scriptLNR.readLine() + linesOut;
                }
            }
            s += linesOut;
            assert(!s.trim().isEmpty()); //this should ensure we never get a blank message - e.getMessage at the very least
            return s;
        } catch (Exception e2) //if <script>: is not found in previous output, or other error occurs, default to exception's message
        {
            return e.getMessage();
        }
    }

    private void runRScript(File scriptFile) {
        // Initialize RCaller, auto detects installed R in standard locations
        // On windows these are C:/Program Files/R or C:/Program Files (x86)/R
        RCaller caller = RCaller.create();
        logger.info("Using installed R: " + caller.getRCallerOptions().getrScriptExecutable());

        caller.redirectROutputToStream(sIn);

        // Initialize our code buffer and database string representation
        RCode code = RCode.create();
        HashMap db = convertDbToColStrings();
        HashMap<String, File> tempFiles = new HashMap<>();

        // Write the database out to temporary files
        Iterator it = db.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();

            // Now write this out to a temporary file
            File outfile = new File(System.getProperty("java.io.tmpdir"), (String) pairs.getKey());
            try {
                BufferedWriter output = new BufferedWriter(new FileWriter(outfile));
                output.write((String) pairs.getValue());
                output.close();
                tempFiles.put((String) pairs.getKey(), outfile);
            } catch (IOException e) {
                logger.error("Writing output to temporary files failed. Error: ", e);
            }
            it.remove(); // avoids a ConcurrentModificationException
        }

        // Create the R code to read in the temporary db files into a structure
        // called db
        code.addRCode("db <- list()");
        try {
            // Load each of the temporary files created above into R
            it = tempFiles.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();

                String load = "db[[\"" + ((String) pairs.getKey()).toLowerCase() + "\"]] <- read.csv(\""
                        + ((File) pairs.getValue()).getPath() + "\",header=TRUE, sep=',')";
                code.addRCode(load);

                it.remove(); // avoids a ConcurrentModificationException
            }
        } catch (Exception e) {
            logger.error("Loading of temporary files failed. Error: ", e);
        }

        // Set up plotting. If something gets plotted, display it. Otherwise, just run the code.
        try {
            File plt = code.startPlot();
            code.R_source(scriptFile.getPath());
            caller.setRCode(code);
            caller.runOnly();
            code.endPlot();
            if (plt.length() > 0) {
                code.showPlot(plt);
            }
        } catch (Exception e) {
            logger.error("Failed when plotting. Error: ", e);
        }
    }

    private void updateDbFromR() {
        // TODO: Fill in the code here
    }

    /**
     * Convert the Datavyu database to a csv file per column.
     * This allows for easy reading into R.
     */
    private HashMap<String, String> convertDbToColStrings() {
        // TODO: Use StringBuilder here
        DataStore db = Datavyu.getProjectController().getDataStore();
        HashMap<String, String> str_db = new HashMap<>();

        String str_var;
        for (Variable v : db.getAllVariables()) {
            str_var = "ordinal,onset,offset";
            if (v.getRootNode().type == Argument.Type.MATRIX) {
                for (Argument a : v.getRootNode().childArguments) {
                    str_var += "," + a.name;
                }
            } else {
                str_var += ",arg";
            }
            str_var += "\n";
            for (int i = 0; i < v.getCellsTemporally().size(); i++) {
                Cell c = v.getCellsTemporally().get(i);

                String row = String.format("%d,%d,%d", i + 1, c.getOnset(), c.getOffset());
                if (v.getRootNode().type == Argument.Type.MATRIX) {
                    for (CellValue val : ((MatrixCellValue) c.getCellValue()).getArguments()) {
                        row += ",";
                        if (!val.isEmpty())
                            row += val.toString();
                    }
                } else {
                    row += ",";
                    if (!c.getCellValue().isEmpty()) {
                        row += c.getCellValue().toString();
                    }
                }
                str_var += row + "\n";
            }
            str_db.put(v.getName(), str_var);
        }

        return str_db;
    }

    @Override
    protected void done() {}

    @Override
    protected void process(final List<String> chunks) {
        for (String chunk : chunks) {
            console.append(chunk);
            // Make sure the last line is always visible
            console.setCaretPosition(console.getDocument().getLength());
        }
    }

    /**
     * Separate thread for polling the incoming data from the scripting engine.
     * The data from the scripting engine gets placed directly into the
     * consoleOutput and also kept in outString for revisiting during
     * error reporting
     */
    class ReaderThread extends Thread {

        /**
         * The size of the buffer to use while ingesting data.
         */
        private static final int BUFFER_SIZE = 32 * 1024;

        /**
         * The method to invoke when the thread is started.
         */
        @Override
        public void run() {
            final byte[] buf = new byte[BUFFER_SIZE];

            int len;

            try {
                while ((len = consoleOutputStream.read(buf)) != -1) {
                    if (len > 0) {
                        // Publish output from script in the console.
                        String s = new String(buf, 0, len);
                        outString.append(s);
                        publish(s);
                    }

                    // Allow other threads to do stuff.
                    Thread.yield();

                }
                consoleOutputStream.close();
                while ((len = consoleOutputStreamAfter.read(buf)) != -1) {
                    if (len > 0) {
                        // Publish output from script in the console.
                        String s = new String(buf, 0, len);
                        outString.append(s);
                        publish(s);
                    }
                    // Allow other threads to do stuff.
                    Thread.yield();
                }
                consoleOutputStreamAfter.close();
            } catch (IOException e) {
                logger.error("Unable to run console thread. Error: ", e);
            }
        }
    }
}
