/**
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information.
 *
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.view.main;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.WindowListener;
import java.beans.EventHandler;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;
import org.orbisgis.core.Services;
import org.orbisgis.core.context.main.MainContext;
import org.orbisgis.core.workspace.CoreWorkspace;
import org.orbisgis.progress.ProgressMonitor;
import org.orbisgis.sif.UIFactory;
import org.orbisgis.view.background.BackgroundManager;
import org.orbisgis.view.background.Job;
import org.orbisgis.view.background.JobQueue;
import org.orbisgis.view.docking.DockingManager;
import org.orbisgis.view.edition.EditableElement;
import org.orbisgis.view.edition.EditorManager;
import org.orbisgis.view.edition.dialogs.SaveDocuments;
import org.orbisgis.view.geocatalog.Catalog;
import org.orbisgis.view.icons.OrbisGISIcon;
import org.orbisgis.view.joblist.JobsPanel;
import org.orbisgis.view.main.frames.LoadingFrame;
import org.orbisgis.view.main.frames.MainFrame;
import org.orbisgis.view.map.MapEditorFactory;
import org.orbisgis.view.output.OutputManager;
import org.orbisgis.view.sql.MapContext_AddLayer;
import org.orbisgis.view.sql.MapContext_BBox;
import org.orbisgis.view.sql.MapContext_Share;
import org.orbisgis.view.sql.MapContext_ZoomTo;
import org.orbisgis.view.sqlconsole.SQLConsoleFactory;
import org.orbisgis.view.table.TableEditorFactory;
import org.orbisgis.view.toc.TocEditorFactory;
import org.orbisgis.view.workspace.ViewWorkspace;
import org.orbisgis.view.workspace.WorkspaceSelectionDialog;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 * The core manage the view of the application.
 * This is the main UIContext
 */
public class Core {
    private static final I18n I18N = I18nFactory.getI18n(Core.class);        
    private static final Logger LOGGER = Logger.getLogger(Core.class);
    /** Buffer to copy resource to file */
    private static final int BUFFER_LENGTH = 4096;
    /////////////////////
    //view package
    private EditorManager editors;         /*!< Management of editors */
    private MainFrame mainFrame = null;     /*!< The main window */
    private Catalog geoCatalog= null;      /*!< The GeoCatalog frame */
    private ViewWorkspace viewWorkspace;
    private OutputManager loggerCollection;    /*!< Loggings panels */     
    private BackgroundManager backgroundManager;
             
    public static final Dimension MAIN_VIEW_SIZE = new Dimension(800,600);/*!< Bounds of mainView, x,y and width height*/
    private DockingManager dockManager = null; /*!< The DockStation manager */
    
   
    /////////////////////
    //base package :
    private MainContext mainContext; /*!< The larger surrounding part of OrbisGis base */
    /**
     * Core constructor, init Model instances
     * @param debugMode Show additional information for debugging purposes
     * @note Call startup() to init Swing
     */
    public Core(CoreWorkspace coreWorkspace,boolean debugMode,ProgressMonitor progressInfo) throws InvocationTargetException, InterruptedException {
        MainContext.initConsoleLogger(debugMode);
        progressInfo.init(I18N.tr("Loading GDMS.."),100);
        initMainContext(debugMode,coreWorkspace);
        progressInfo.progressTo(10);
        this.viewWorkspace = new ViewWorkspace(this.mainContext.getCoreWorkspace());        
        Services.registerService(ViewWorkspace.class, I18N.tr("Contains view folders path"),
                        viewWorkspace);        
        progressInfo.init(I18N.tr("Register GUI Sql functions.."),100);
        addSQLFunctions();
        progressInfo.progressTo(15);
        initSwingJobs();
        progressInfo.progressTo(18);
        initSIF();
        progressInfo.progressTo(20);
    }
    
    /**
     * Find the workspace folder or show a dialog to select one
     */
    private void initMainContext(boolean debugMode,CoreWorkspace coreWorkspace) throws InterruptedException, InvocationTargetException {
        File defaultWorkspace = coreWorkspace.readDefaultWorkspacePath();
        if(!debugMode && (defaultWorkspace==null || !ViewWorkspace.isWorkspaceValid(defaultWorkspace))) {
                SwingUtilities.invokeAndWait(new promptUserForSelectingWorkspace(coreWorkspace ));
        }
        this.mainContext = new MainContext(debugMode,coreWorkspace,true);
    }
    
    private class promptUserForSelectingWorkspace implements Runnable {
            CoreWorkspace coreWorkspace;

                public promptUserForSelectingWorkspace(CoreWorkspace coreWorkspace) {
                        this.coreWorkspace = coreWorkspace;
                }
            
                @Override
                public void run() {
                        // Ask the user to select a workspace folder
                        File newWorkspace = WorkspaceSelectionDialog.showWorkspaceFolderSelection(coreWorkspace);
                        if(newWorkspace==null) {
                                throw new RuntimeException(I18N.tr("Invalid workspace"));
                        }
                        coreWorkspace.setWorkspaceFolder(newWorkspace.getAbsolutePath());
                }            
    }
    /**
     * For UnitTest purpose
     * @return The Catalog instance
     */
    public Catalog getGeoCatalog() {
        return geoCatalog;
    }
    
    /**
     * Init the SIF ui factory
     */
    private void initSIF() {
        UIFactory.setDefaultImageIcon(OrbisGISIcon.getIcon("mini_orbisgis"));
    }
    
    /**
     * 
     * @return Instance of main context
     */
    public MainContext getMainContext() {
        return mainContext;
    }
    /**
     * Instance of main frame, null if startup() has not be called.
     * @return MainFrame instance
     */
    public MainFrame getMainFrame() {
        return mainFrame;
    }
    
    /**
     * Create the Instance of the main frame
     */
    private void makeMainFrame() {
        mainFrame = new MainFrame();
        //When the user ask to close OrbisGis it call
        //the shutdown method here, 
        // Link the Swing Events with the MainFrame event
        //Thanks to EventHandler we don't have to build a listener class
        mainFrame.addWindowListener(EventHandler.create(
                WindowListener.class, //The listener class
                this,                 //The event target object
                "onMainWindowClosing",//The event target method to call
                null,                 //the event parameter to pass(none)
                "windowClosing"));    //The listener method to use
        UIFactory.setMainFrame(mainFrame);
    }
    
    /**
     * Create the logging panels
     * All,Info,Warning,Error
     */
    private void makeLoggingPanels() {
        loggerCollection = new OutputManager(mainContext.isDebugMode());
        //Show Panel
        dockManager.show(loggerCollection.getPanel());
    }
    /**
     * Create the GeoCatalog view
     */
    private void makeGeoCatalogPanel() {
        //The geocatalog view content is read from the SourceContext
        geoCatalog = new Catalog();
        //Add the view as a new Docking Panel
        dockManager.show(geoCatalog);
    }
    
    /**
     * Create the Job processing information and control panel
     */
    private void makeJobsPanel() {
            dockManager.show(new JobsPanel());
    }
    /**
     * Load the built-ins editors factories
     */
    private void loadEditorFactories() {
            editors.addEditorFactory(new TocEditorFactory());
            editors.addEditorFactory(new MapEditorFactory());
            editors.addEditorFactory(new SQLConsoleFactory());
            editors.addEditorFactory(new TableEditorFactory());
    }
    /**
     * Initialisation of the BackGroundManager Service
     */
    private void initSwingJobs() {   
        backgroundManager = new JobQueue();
        Services.registerService(
                BackgroundManager.class,
                I18N.tr("Execute tasks in background processes, showing progress bars. Gives access to the job queue"),
               backgroundManager);
    }
    /**
     * The user want to close the main window
     * Then the application has to be closed
     */
    public void onMainWindowClosing() {
        this.shutdown(true);
    }
    
    /**
     * Create the central place for editor factories.
     * This manager will retrieve panel editors and
     * use the docking manager to show them
     * @param dm Instance of docking manager
     */
    private void makeEditorManager(DockingManager dm) {
        editors = new EditorManager(dm);
        Services.registerService(EditorManager.class,
                                 I18N.tr("Use this instance to open an editable element (map,data source..)"),
                                 editors);
        
    }
    
    /**
    * Starts the application. This method creates the {@link MainFrame},
    * and manage the Look And Feel declarations
    */
    public void startup(ProgressMonitor progress) throws InterruptedException, InvocationTargetException{
        // Show the application when Swing will be ready
        initialize(progress);
        SwingUtilities.invokeLater(new ShowSwingApplication(progress));
    }
    private void initialize(ProgressMonitor progress) {
            
        if(mainFrame!=null) {
            return;//This method can't be called twice
        }
        
        progress.init(I18N.tr("Loading the main window"), 100);
        makeMainFrame();
        progress.progressTo(30);
        
        progress.init(I18N.tr("Loading docking system and frames"), 100);
        //Initiate the docking management system
        dockManager = new DockingManager(mainFrame);
        mainFrame.setDockingManager(dockManager);
        progress.progressTo(35);
        
        //Set the main frame position and size
	mainFrame.setSize(MAIN_VIEW_SIZE);
        
        // Try to set the frame at the center of the default screen
        try {
                GraphicsDevice device = GraphicsEnvironment.
                        getLocalGraphicsEnvironment().getDefaultScreenDevice();                        
                Rectangle screenBounds = device.getDefaultConfiguration().getBounds();
                mainFrame.setLocation(screenBounds.x+screenBounds.width/2-MAIN_VIEW_SIZE.width/2,
                screenBounds.y+screenBounds.height/2-MAIN_VIEW_SIZE.height/2);
        } catch(Throwable ex) {
                LOGGER.error(ex.getLocalizedMessage(),ex);
        }
        //Load the log panels
        makeLoggingPanels();
        progress.progressTo(40);
        
        //Load the Job Panel
        makeJobsPanel();
        progress.progressTo(45);
        
        //Load the editor factories manager
        makeEditorManager(dockManager);
        progress.progressTo(50);
        
        //Load the GeoCatalog
        makeGeoCatalogPanel();
        progress.progressTo(55);
        
        //Load the Map And view panels
        //makeTocAndMap();
        //Load Built-ins Editors
        loadEditorFactories();
        progress.progressTo(60);
        
        progress.init(I18N.tr("Restore the former layout.."), 100);
        //Load the docking layout and editors opened in last OrbisGis instance
        File savedDockingLayout = new File(viewWorkspace.getDockingLayoutPath());
        if(!savedDockingLayout.exists()) {
                // Copy the default docking layout
                // First OrbisGIS start
                copyDefaultDockingLayout(savedDockingLayout);
        }
        dockManager.setDockingLayoutPersistanceFilePath(viewWorkspace.getDockingLayoutPath());
        progress.progressTo(70);        
    }
    /**
     * Copy the default docking layout to the specified file path.
     * @param savedDockingLayout 
     */
    private void copyDefaultDockingLayout(File savedDockingLayout) {
                InputStream xmlFileStream = DockingManager.class.getResourceAsStream("default_docking_layout.xml");
                if (xmlFileStream != null) {
                        try {
                                FileOutputStream writer = new FileOutputStream(savedDockingLayout);
                                try {
                                        byte[] buffer = new byte[BUFFER_LENGTH];
                                        for (int n; (n = xmlFileStream.read(buffer)) != -1;) {
                                                writer.write(buffer, 0, n);
                                        }
                                } finally {
                                        writer.close();
                                }
                        } catch (FileNotFoundException ex) {
                                LOGGER.error(I18N.tr("Unable to save the docking layout."), ex);
                        } catch (IOException ex) {
                                LOGGER.error(I18N.tr("Unable to save the docking layout."), ex);
                        } finally {
                                try {
                                        xmlFileStream.close();
                                } catch (IOException ex) {
                                        LOGGER.error(I18N.tr("Unable to save the docking layout."), ex);
                                }
                        }
                }
        }

    /**
     * Add SQL functions to interact with OrbisGIS UI
     */
        private void addSQLFunctions() {               
                mainContext.getDataSourceFactory().getFunctionManager().addFunction(MapContext_AddLayer.class);
                mainContext.getDataSourceFactory().getFunctionManager().addFunction(MapContext_BBox.class);
                mainContext.getDataSourceFactory().getFunctionManager().addFunction(MapContext_ZoomTo.class);
                mainContext.getDataSourceFactory().getFunctionManager().addFunction(MapContext_Share.class);
        }
    
    /**
     * Change the state of the main frame in the swing thread
     */
    private class ShowSwingApplication implements Runnable {
        ProgressMonitor progress;

        public ShowSwingApplication(ProgressMonitor progress) {
                this.progress = progress;
        }

        /**
        * Change the state of the main frame in the swing thread
        */
        @Override
        public void run(){
                try {
                        initialize(progress);
                        mainFrame.setVisible( true );
                } finally {
                        progress.setCancelled(true);
                }
        }
    }

    /**
     * Return the docking manager. This function is used by Unit Tests.
     * @return The Docking Manager
     */
    public DockingManager getDockManager() {
        return dockManager;
    }
    
    /**
     * Free all resources allocated by this object
     */
    public void dispose() {
        //Close all running jobs
        for(Job job : backgroundManager.getActiveJobs()) {
                try {
                        job.cancel();
                } catch (Throwable ex) {
                        LOGGER.error(ex);
                        //Cancel the next job
                }
        }
        //Remove all listeners created by this object
        
        //Free UI resources
        editors.dispose();
        geoCatalog.dispose();
        mainFrame.dispose();
        dockManager.dispose();
        loggerCollection.dispose();
        
        //Free libraries resources
        mainContext.dispose();
        
    }
    /**
     * Save or discard editable element modification.
     * Show a dialog if there is at least one unsaved editable element.
     * @return True if the application must cancel the close shutdown operation
     */
        private boolean isShutdownVetoed() {
                List<EditableElement> modifiedElements = new ArrayList<EditableElement>();
                Collection<EditableElement> editableElement = editors.getEditableElements();
                for(EditableElement editable : editableElement) {
                        if(editable.isModified()) {
                                modifiedElements.add(editable);
                        }
                }
                if (!modifiedElements.isEmpty()) {
                        SaveDocuments.CHOICE userChoice = SaveDocuments.showModal(mainFrame, modifiedElements);
                        return userChoice==SaveDocuments.CHOICE.CANCEL;
                } else {
                        return false;
                }
        }

        /**
         * Stops this application, closes the {@link MainFrame} and saves all
         * properties if the application is not in a {@link #isSecure() secure environment}.
         * This method is called through the MainFrame.MAIN_FRAME_CLOSING event
         * listener.
         */
        public boolean shutdown(boolean stopVM) {
                if (!isShutdownVetoed()) {
                        try {
                                mainContext.saveStatus(); //Save the services status
                                this.dispose();
                        } finally {
                                //While Plugins are not implemented do not close the VM in finally clause
                                // if(stopVM) {
                                //SwingUtilities.invokeLater( new Runnable(){
                                //   /** If an error occuring while unload resources, java machine
                                //    * may continue to run. In this case, the following command
                                //    * would terminate the application.
                                //    */
                                //    public void run(){
                                //            System.exit(0);
                                //    }
                                //   } );
                                // }
                        }
                        return true;
                } else {
                        return false;
                }                
        }
}
