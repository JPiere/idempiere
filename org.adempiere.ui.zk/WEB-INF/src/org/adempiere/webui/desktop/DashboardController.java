/******************************************************************************
 * Copyright (C) 2012 Heng Sin Low                                            *
 * Copyright (C) 2012 Trek Global                 							  *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *****************************************************************************/
package org.adempiere.webui.desktop;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.webui.ClientInfo;
import org.adempiere.webui.Extensions;
import org.adempiere.webui.LayoutUtils;
import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.apps.BusyDialog;
import org.adempiere.webui.apps.DesktopRunnable;
import org.adempiere.webui.apps.WReport;
import org.adempiere.webui.apps.graph.IChartRendererService;
import org.adempiere.webui.apps.graph.WGraph;
import org.adempiere.webui.apps.graph.WPAWidget;
import org.adempiere.webui.apps.graph.WPerformanceDetail;
import org.adempiere.webui.apps.graph.WPerformanceIndicator;
import org.adempiere.webui.apps.graph.model.ChartModel;
import org.adempiere.webui.component.Anchorchildren;
import org.adempiere.webui.component.Anchorlayout;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.ToolBarButton;
import org.adempiere.webui.dashboard.DashboardPanel;
import org.adempiere.webui.dashboard.DashboardRunnable;
import org.adempiere.webui.event.DrillEvent;
import org.adempiere.webui.event.DrillEvent.DrillData;
import org.adempiere.webui.event.ZoomEvent;
import org.adempiere.webui.report.HTMLExtension;
import org.adempiere.webui.session.SessionManager;
import org.adempiere.webui.theme.ThemeManager;
import org.adempiere.webui.util.Icon;
import org.adempiere.webui.util.ServerPushTemplate;
import org.adempiere.webui.util.ZKUpdateUtil;
import org.adempiere.webui.util.ZkContextRunnable;
import org.adempiere.webui.window.Dialog;
import org.adempiere.webui.window.ZkReportViewerProvider;
import org.compiere.Adempiere;
import org.compiere.model.I_AD_Menu;
import org.compiere.model.MChart;
import org.compiere.model.MColumn;
import org.compiere.model.MDashboardContent;
import org.compiere.model.MDashboardContentAccess;
import org.compiere.model.MDashboardPreference;
import org.compiere.model.MGoal;
import org.compiere.model.MLookup;
import org.compiere.model.MLookupFactory;
import org.compiere.model.MLookupInfo;
import org.compiere.model.MMenu;
import org.compiere.model.MPInstance;
import org.compiere.model.MPInstancePara;
import org.compiere.model.MProcess;
import org.compiere.model.MProcessPara;
import org.compiere.model.MQuery;
import org.compiere.model.MRole;
import org.compiere.model.MStatusLine;
import org.compiere.model.MStyle;
import org.compiere.model.MSysConfig;
import org.compiere.model.MTable;
import org.compiere.print.MPrintFormat;
import org.compiere.print.ReportEngine;
import org.compiere.process.ProcessInfo;
import org.compiere.process.ServerProcessCtl;
import org.compiere.tools.FileUtil;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DefaultEvaluatee;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.Util;
import org.zkoss.json.JSONArray;
import org.zkoss.util.media.AMedia;
import org.zkoss.zhtml.Style;
import org.zkoss.zhtml.Text;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.AfterSizeEvent;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MaximizeEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.A;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Html;
import org.zkoss.zul.Iframe;
import org.zkoss.zul.Include;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Panelchildren;
import org.zkoss.zul.Separator;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Toolbar;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Vlayout;

/**
 * Dashboard renderer and controller
 * @author hengsin
 */
public class DashboardController implements EventListener<Event> {

	private final static CLogger logger = CLogger.getCLogger(DashboardController.class);
	//original parent and sibling for maximized gadget
	private Component prevParent;
	private Component prevNext;

	/** dashboard gadget panels */
	private List<Panel> panelList = new ArrayList<Panel>();
	/** For column orientation */
	private List<Anchorchildren> columnList;
	/** For row orientation */
	private List<Anchorchildren> rowList;
	private Anchorlayout dashboardLayout;
	private Anchorchildren maximizedHolder;
	/** Runnable for pooling refresh of dashboard gadgets */
	private DashboardRunnable dashboardRunnable;
	/** Timer for {@link #dashboardRunnable} */
	private Timer dashboardTimer;
	/** True for dashboard, false for left/right side panel */
	private boolean isShowInDashboard;
	/** number of columns for column oriented dashboard */
	private int noOfCols;

	private static final String PANEL_EMPTY_ATTRIBUTE = "panel.empty";
	private static final String COLUMN_NO_ATTRIBUTE = "ColumnNo";
	private static final String LINE_ATTRIBUTE = "Line";
	private static final String IS_ADDITIONAL_ROW_ATTRIBUTE = "IsAdditionalRow";
	private static final String IS_ADDITIONAL_COLUMN_ATTRIBUTE = "IsAdditionalColumn";
	private static final String IS_SHOW_IN_DASHBOARD_ATTRIBUTE = "IsShowInDashboard";
	private static final String FLEX_GROW_ATTRIBUTE = "FlexGrow";
	private static final String IMAGES_CONTEXT_HELP_PNG = "images/Help16.png";

	/** Default total width for column oriented layout (in percentage) */
	private final static int DEFAULT_DASHBOARD_WIDTH = 99;
	/** Column orientation */
	private final static String DASHBOARD_LAYOUT_COLUMNS = "C";
	/** Row orientation */
	private final static String DASHBOARD_LAYOUT_ROWS = "R";
	/** Max number of gadgets in a row. For row oriented layout. */
	private final static int MAX_NO_OF_PREFS_IN_ROW = 10;
	/** Default horizontal flex grow for dashboard gadget. For row oriented layout. */
	private final static int DEFAULT_FLEX_GROW = 1;
	
	/**
	 * default constructor
	 */
	public DashboardController() {
		dashboardLayout = new Anchorlayout();
		dashboardLayout.setSclass("dashboard-layout");
        ZKUpdateUtil.setVflex(dashboardLayout, "1");
        ZKUpdateUtil.setHflex(dashboardLayout, "1");
        
        maximizedHolder = new Anchorchildren();                
        maximizedHolder.setAnchor("100% 100%");
        maximizedHolder.setStyle("overflow: hidden; border: none; margin: 0; padding: 0;");
	}
	
	/**
	 * Render main or side dashboard
	 * @param parent Parent Component of dashboard
	 * @param desktopImpl IDesktop
	 * @param isShowInDashboard true for main/center dashboard, false for left/right side dashboard
	 */
	public void render(Component parent, IDesktop desktopImpl, boolean isShowInDashboard) {
		
		String layoutOrientation = MSysConfig.getValue(MSysConfig.DASHBOARD_LAYOUT_ORIENTATION, Env.getAD_Client_ID(Env.getCtx()));
        if(layoutOrientation.equals(DASHBOARD_LAYOUT_ROWS) && isShowInDashboard)
        	renderRows(parent, desktopImpl, isShowInDashboard, false);
        else
        	renderColumns(parent, desktopImpl, isShowInDashboard, false);
	}
	
	/**
	 * Render dashboard in column orientation
	 * @param parent Component
	 * @param desktopImpl IDesktop
	 * @param isShowInDashboard true for dashboard, false for left/right side panel
	 * @param update true for update, false for new 
	 */
	protected void renderColumns(Component parent, IDesktop desktopImpl, boolean isShowInDashboard, boolean update) {
		this.isShowInDashboard = isShowInDashboard;
		if (!update)
			parent.appendChild(dashboardLayout);
		if (!update && isShowInDashboard)
			((HtmlBasedComponent)parent).setStyle("overflow-x: auto;");
		dashboardLayout.getChildren().clear();
        
        if (!dashboardLayout.getDesktop().isServerPushEnabled())
        	dashboardLayout.getDesktop().enableServerPush(true);
        
        if (!update)
        	dashboardRunnable = new DashboardRunnable(parent.getDesktop());
        
        columnList = new ArrayList<Anchorchildren>();
        
        // Dashboard content
        Vlayout dashboardColumnLayout = null;
        int currentColumnNo = 0;

        int noOfCols = 0;
        int width = 0;

        try
		{
        	int AD_User_ID = Env.getAD_User_ID(Env.getCtx());
        	int AD_Role_ID = Env.getAD_Role_ID(Env.getCtx());
        	
        	MDashboardPreference[] dps = MDashboardPreference.getForSession(AD_User_ID, AD_Role_ID, true);
        	MDashboardContent [] dcs =  MDashboardContentAccess.get(Env.getCtx(), AD_Role_ID, AD_User_ID, null);
        	
        	if(dps.length == 0){
        	    createDashboardPreference(AD_User_ID, AD_Role_ID);
        	    dps = MDashboardPreference.getForSession(AD_User_ID, AD_Role_ID, true);
        	}else{
        		if(updatePreferences(dps, dcs,Env.getCtx())){        			
        			dps = MDashboardPreference.getForSession(AD_User_ID, AD_Role_ID, true);
        		}
        	}
        	               
        	noOfCols = MDashboardPreference.getForSessionColumnCount(isShowInDashboard, AD_User_ID, AD_Role_ID);        	
        	if (ClientInfo.isMobile() && isShowInDashboard) {
	        	if (ClientInfo.maxWidth(ClientInfo.MEDIUM_WIDTH-1)) {
	        		if (ClientInfo.maxWidth(ClientInfo.SMALL_WIDTH-1)) {
	        			noOfCols = 1;
	        		} else if (noOfCols > 2) {
	        			noOfCols = 2;
	        		}
	        	}
        	}
        	this.noOfCols = noOfCols;
            
        	int dashboardWidth = isShowInDashboard ? DEFAULT_DASHBOARD_WIDTH : 100;
            width = noOfCols <= 0 ? dashboardWidth : dashboardWidth / noOfCols;
            int extraWidth = 100 - (noOfCols <= 0 ? dashboardWidth : width * noOfCols) - (100 - dashboardWidth - 1);
            for (final MDashboardPreference dp : dps)            	
			{            	            	            	
            	if(!dp.isActive())
            		continue;
            	
            	if (dp.isShowInDashboard() != isShowInDashboard)
            		continue;
            	
            	MDashboardContent dc = new MDashboardContent(dp.getCtx(), dp.getPA_DashboardContent_ID(), dp.get_TrxName());
            	
	        	int columnNo = dp.getColumnNo();
	        	int effColumn = columnNo;
	        	if (effColumn+1 > noOfCols)
	        		effColumn = noOfCols-1;
	        	if(dashboardColumnLayout == null || currentColumnNo != effColumn)
	        	{
	        		dashboardColumnLayout = new Vlayout();
	        		dashboardColumnLayout.setSclass("dashboard-column");
					dashboardColumnLayout.setAttribute(COLUMN_NO_ATTRIBUTE, columnNo);
					dashboardColumnLayout.setAttribute(IS_SHOW_IN_DASHBOARD_ATTRIBUTE, isShowInDashboard);
					dashboardColumnLayout.setAttribute(IS_ADDITIONAL_COLUMN_ATTRIBUTE, false);
	        		Anchorchildren dashboardColumn = new Anchorchildren();
	        		dashboardColumn.setAnchor(width + "%" + " 100%");
	        		if (!ClientInfo.isMobile())
	        		{
		        		dashboardColumn.setDroppable("true");
		        		dashboardColumn.addEventListener(Events.ON_DROP, this);
	        		}
	        		dashboardColumn.appendChild(dashboardColumnLayout);
	        		columnList.add(dashboardColumn);
	                dashboardLayout.appendChild(dashboardColumn);
	                ZKUpdateUtil.setHflex(dashboardColumnLayout, "1");

	                currentColumnNo = effColumn;
	        	}

	        	Panel panel = null;
	        	if (update) {
	        		panel = findPanel(dp.getPA_DashboardContent_ID(), dp.getPA_DashboardPreference_ID());
	        	} else {
				panel = newGadgetPanel(dp, dc);
	        	}
	        	if (panel != null && panel.getAttribute(PANEL_EMPTY_ATTRIBUTE) == null)
	        		dashboardColumnLayout.appendChild(panel);
	        	if (!update) {
	        		final Panel fp = panel;
	        		ServerPushTemplate spt = new ServerPushTemplate(dashboardLayout.getDesktop());
	        		String contextPath = Executions.getCurrent().getContextPath();
	        		Panelchildren panelChildren = new Panelchildren();
	        		fp.appendChild(panelChildren);
	        		BusyDialog busyDialog = new BusyDialog();
	                busyDialog.setShadow(false);
	                panelChildren.appendChild(busyDialog);
	        		//must create zulfile component in foreground UI thread
	        		Component zComponent = null;
	        		if (!Util.isEmpty(dc.getZulFilePath(), true)) {
        	        	try {	        	        		
        	        		zComponent = Extensions.getDashboardGadget(dc.getZulFilePath(), panelChildren, dc);
        	        	} catch (Exception e) {
        	        		throw new AdempiereException(e);
        	        	}
	        		}
	        		final Component zulComponent = zComponent;
	        		ZkContextRunnable cr = new ZkContextRunnable() {
						@Override
						protected void doRun() {
							try {
								asyncRenderGadgetPanel(spt, dc, fp, contextPath, panelChildren, zulComponent);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					};	        		
	        		Adempiere.getThreadPoolExecutor().submit(new DesktopRunnable(cr, parent.getDesktop()));
	        	}
	        }
            
            if (dps.length == 0)
            {
            	dashboardColumnLayout = new Vlayout();
				dashboardColumnLayout.setAttribute(COLUMN_NO_ATTRIBUTE, "0");
				dashboardColumnLayout.setAttribute(IS_SHOW_IN_DASHBOARD_ATTRIBUTE, isShowInDashboard);
				dashboardColumnLayout.setAttribute(IS_ADDITIONAL_COLUMN_ATTRIBUTE, true);
        		Anchorchildren dashboardColumn = new Anchorchildren();
        		dashboardColumn.setAnchor((width-5) + "%" + " 100%");
        		if (!ClientInfo.isMobile())
        		{
        			dashboardColumn.setDroppable("true");
        			dashboardColumn.addEventListener(Events.ON_DROP, this);
        		}
        		dashboardColumn.appendChild(dashboardColumnLayout);
        		columnList.add(dashboardColumn);
                dashboardLayout.appendChild(dashboardColumn);
                ZKUpdateUtil.setWidth(dashboardColumnLayout, "100%");
            }
            else if (isShowInDashboard)
            {
            	// additional column
            	dashboardColumnLayout = new Vlayout();
            	ZKUpdateUtil.setWidth(dashboardColumnLayout, "100%");
				dashboardColumnLayout.setAttribute(COLUMN_NO_ATTRIBUTE, currentColumnNo + 1);
				dashboardColumnLayout.setAttribute(IS_SHOW_IN_DASHBOARD_ATTRIBUTE, isShowInDashboard);
				dashboardColumnLayout.setAttribute(IS_ADDITIONAL_COLUMN_ATTRIBUTE, true);
        		Anchorchildren dashboardColumn = new Anchorchildren();
        		dashboardColumn.setAnchor(extraWidth + "% 100%");
        		if (!ClientInfo.isMobile())
        		{
        			dashboardColumn.setDroppable("true");
        			dashboardColumn.addEventListener(Events.ON_DROP, this);
        		}
        		dashboardColumn.appendChild(dashboardColumnLayout);
        		columnList.add(dashboardColumn);
                dashboardLayout.appendChild(dashboardColumn);
                ZKUpdateUtil.setWidth(dashboardColumnLayout, "100%");
            }
		}
        catch (Exception e)
        {
			logger.log(Level.WARNING, "Failed to create dashboard content", e);
		}
        //
                
        if (!update)
        {
        	startDashboardRunnable(parent);		
		}
	}

	/**
	 * Create new gadget panel
	 * @param dp
	 * @param dc
	 * @return {@link Panel}
	 */
	private Panel newGadgetPanel(MDashboardPreference dp, MDashboardContent dc) {
		Panel panel;
		panel = new Panel();
		String dcName = dc.get_Translation(MDashboardContent.COLUMNNAME_Name);
		Caption caption = new Caption(dcName);
		panel.appendChild(caption);
		panel.setAttribute(MDashboardPreference.COLUMNNAME_PA_DashboardContent_ID, dp.getPA_DashboardContent_ID());
		panel.setAttribute(MDashboardPreference.COLUMNNAME_PA_DashboardPreference_ID, dp.getPA_DashboardPreference_ID());
		panelList.add(panel);
		panel.addEventListener(Events.ON_MAXIMIZE, this);
		panel.setSclass("dashboard-widget");
		panel.setMaximizable(dc.isMaximizable());

		String description = dc.get_Translation(MDashboardContent.COLUMNNAME_Description);
		String help = dc.get_Translation(MDashboardContent.COLUMNNAME_Help);
		if(!Util.isEmpty(description, true) || !Util.isEmpty(help, true) ) {
			renderHelpButton(caption, description, help);
		}
		
		panel.setCollapsible(dc.isCollapsible());
		panel.setOpen(!dc.isCollapsible() || !dp.isCollapsedByDefault());
		panel.addEventListener(Events.ON_OPEN, this);
		if (!ClientInfo.isMobile()) {
			panel.setDroppable("true");
			panel.getCaption().setDraggable("true");
			panel.addEventListener(Events.ON_DROP, this);
		}
		panel.setBorder("normal");
	
			return panel;
	}
	
	/**
	 * Render help button for individual dashboard gadget
	 * @param caption
	 * @param text
	 */
	private void renderHelpButton(Caption caption, String text, String help) {
		A icon = new A();
		icon.setSclass("dashboard-content-help-icon");
		if (ThemeManager.isUseFontIconForImage())
			icon.setIconSclass(Icon.getIconSclass(Icon.HELP));
		else
			icon.setImage(ThemeManager.getThemeResource(IMAGES_CONTEXT_HELP_PNG));
		icon.addEventListener(Events.ON_CLICK, this);
		icon.setAttribute("title", caption.getLabel());
		icon.setAttribute("description", text);
		icon.setAttribute("help", help);
		caption.appendChild(icon);
		Div popup = new Div();
		Text t = new Text(text);
		popup.setSclass("dashboard-content-help-popup");
		popup.appendChild(t);
		caption.appendChild(popup);
	}

	/**
	 * Render gadget panel in background thread
	 * @param spt ServerPushTemplate
	 * @param dashboardContent MDashboardContent
	 * @param panel Panel
	 * @param contextPath
	 * @param panelChildren Panelchildren
	 * @param zulComponent Component created from zul in Event Listener thread
	 * @throws Exception
	 */
	private void asyncRenderGadgetPanel(ServerPushTemplate spt, MDashboardContent dashboardContent, Panel panel, String contextPath, 
			Panelchildren panelChildren, Component zulComponent) throws Exception {
		List<Component> components = new ArrayList<>();
		asyncRenderComponents(dashboardContent, dashboardRunnable, contextPath, panelChildren, components, zulComponent, spt);
		if (components.size() > 0) {
			for(Component c : components) {
				if (c.getParent() != panelChildren) {
					spt.executeAsync(() -> panelChildren.appendChild(c));
				}
				if (c instanceof DashboardPanel) {
					DashboardPanel dpanel = (DashboardPanel) c;
					if (dpanel.isLazy()) {
						try {
							dpanel.refresh(spt);
							if(dpanel.isEmpty()) {
								spt.executeAsync(() -> {
									panel.detach();
									panel.setAttribute(PANEL_EMPTY_ATTRIBUTE, Boolean.TRUE);
								});
							}
						} catch (Exception e) {
							logger.log(Level.SEVERE, e.getMessage(), e);
						}
					} 					
				}				
			}						
			spt.executeAsync(() -> {
				if (panelChildren.getFirstChild() != null && panelChildren.getFirstChild() instanceof BusyDialog)
					panelChildren.getFirstChild().detach();
			});			
		} else {
			spt.executeAsync(() -> {
				panel.detach();
				panel.setAttribute(PANEL_EMPTY_ATTRIBUTE, Boolean.TRUE);
			});
		}		
	}
	
	/**
	 * Start {@link #dashboardRunnable} for pooling refresh of dashboard gadgets (using {@link #dashboardTimer})
	 * @param parent
	 */
	private void startDashboardRunnable(Component parent) {
		// default Update every one minutes
		int interval = MSysConfig.getIntValue(MSysConfig.ZK_DASHBOARD_REFRESH_INTERVAL, 60000);
		dashboardTimer = new Timer();
		dashboardTimer.setDelay(interval);
		dashboardTimer.setRepeats(true);
		dashboardTimer.addEventListener(Events.ON_TIMER, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				if (dashboardRunnable != null && !dashboardRunnable.isEmpty()) {
					dashboardRunnable.run();
				}
			}
		});
		dashboardTimer.setPage(parent.getPage());
	}

	/**
	 * Render dashboard in row orientation
	 * @param parent
	 * @param desktopImpl
	 * @param isShowInDashboard
	 * @param update
	 */
	protected void renderRows(Component parent, IDesktop desktopImpl, boolean isShowInDashboard, boolean update) {
		this.isShowInDashboard = isShowInDashboard;
		if (!update)
			parent.appendChild(dashboardLayout);
		if (!update && isShowInDashboard)
			((HtmlBasedComponent)parent).setStyle("overflow-x: auto;");
		dashboardLayout.getChildren().clear();
        
        if (!dashboardLayout.getDesktop().isServerPushEnabled())
        	dashboardLayout.getDesktop().enableServerPush(true);
        
        if (!update)
        	dashboardRunnable = new DashboardRunnable(parent.getDesktop());
        
        rowList = new ArrayList<Anchorchildren>();
        
        // Dashboard content
        Hlayout dashboardLineLayout = null;
        int currentLineNo = 0;
        int maxPerLine = 0;
        int width = 100;
        try
		{
        	int AD_User_ID = Env.getAD_User_ID(Env.getCtx());
        	int AD_Role_ID = Env.getAD_Role_ID(Env.getCtx());
        	
        	MDashboardPreference[] dps = MDashboardPreference.getForSession(AD_User_ID, AD_Role_ID, false);
        	MDashboardContent [] dcs =  MDashboardContentAccess.get(Env.getCtx(), AD_Role_ID, AD_User_ID, null);
        	
        	if(dps.length == 0){
        	    createDashboardPreference(AD_User_ID, AD_Role_ID);
        	    dps = MDashboardPreference.getForSession(AD_User_ID, AD_Role_ID, false);
        	}else{
        		if(updatePreferences(dps, dcs,Env.getCtx())){        			
        			dps = MDashboardPreference.getForSession(AD_User_ID, AD_Role_ID, false);
        		}
        	}
        	
        	if (ClientInfo.isMobile() && isShowInDashboard) {
	        	if (ClientInfo.maxWidth(ClientInfo.MEDIUM_WIDTH-1)) {
	        		if (ClientInfo.maxWidth(ClientInfo.SMALL_WIDTH-1)) {
	        			maxPerLine = 1;
	        		} else {
	        			maxPerLine = 2;
	        		}
	        	}
        	}
            
            for (final MDashboardPreference dp : dps)            	
			{            	            	            	
            	if(!dp.isActive())
            		continue;
            	
            	if (dp.isShowInDashboard() != isShowInDashboard)
            		continue;
            	
            	MDashboardContent dc = new MDashboardContent(dp.getCtx(), dp.getPA_DashboardContent_ID(), dp.get_TrxName());
            	
	        	int lineNo = dp.getLine().intValue();
	        	
	        	int flexGrow = (flexGrow = dp.getFlexGrow()) > 0 ? flexGrow : DEFAULT_FLEX_GROW;
	        	if(dashboardLineLayout == null || currentLineNo != lineNo || (maxPerLine > 0 && dashboardLineLayout.getChildren().size() == maxPerLine))
	        	{
	        		dashboardLineLayout = new Hlayout();
					dashboardLineLayout.setAttribute(LINE_ATTRIBUTE, lineNo);
					dashboardLineLayout.setAttribute(IS_SHOW_IN_DASHBOARD_ATTRIBUTE, isShowInDashboard);
					dashboardLineLayout.setAttribute(IS_ADDITIONAL_ROW_ATTRIBUTE, false);
	        		dashboardLineLayout.setSclass("dashboard-row");
	        		Anchorchildren dashboardLine = new Anchorchildren();
	        		dashboardLine.setAnchor(width + "%");
	        		ZKUpdateUtil.setHflex(dashboardLine, "min");
	        		if (!ClientInfo.isMobile())
	        		{
		        		dashboardLine.setDroppable("true");
		        		dashboardLine.addEventListener(Events.ON_DROP, this);
	        		}
	        		dashboardLine.appendChild(dashboardLineLayout);
	        		rowList.add(dashboardLine);
	                dashboardLayout.appendChild(dashboardLine);
	                currentLineNo = lineNo;
	        	}

	        	Panel panel = null;
	        	if (update) {
	        		panel = findPanel(dp.getPA_DashboardContent_ID(), dp.getPA_DashboardPreference_ID());
	        	} else {
					panel = newGadgetPanel(dp, dc);
					panel.setAttribute(FLEX_GROW_ATTRIBUTE, String.valueOf(flexGrow));
		        	ZKUpdateUtil.setHflex(panel, String.valueOf(flexGrow));
	        	}
	        	if (panel != null && panel.getAttribute(PANEL_EMPTY_ATTRIBUTE) == null) {
	        		dashboardLineLayout.appendChild(panel);
	        	}
	        	if (!update) {
	        		final Panel fp = panel;
	        		ServerPushTemplate spt = new ServerPushTemplate(dashboardLayout.getDesktop());
	        		String contextPath = Executions.getCurrent().getContextPath();
	        		Panelchildren panelChildren = new Panelchildren();
	        		fp.appendChild(panelChildren);
	        		BusyDialog busyDialog = new BusyDialog();
	                busyDialog.setShadow(false);
	                panelChildren.appendChild(busyDialog);
	        		//must create zulfile component in foreground UI thread
	        		Component zComponent = null;
	        		if (!Util.isEmpty(dc.getZulFilePath(), true)) {
        	        	try {	        	        		
        	        		zComponent = Extensions.getDashboardGadget(dc.getZulFilePath(), panelChildren, dc);
        	        	} catch (Exception e) {
        	        		throw new AdempiereException(e);
        	        	}
	        		}
	        		final Component zulComponent = zComponent;
	        		ZkContextRunnable cr = new ZkContextRunnable() {
						@Override
						protected void doRun() {
							try {
								asyncRenderGadgetPanel(spt, dc, fp, contextPath, panelChildren, zulComponent);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					};	        		
	        		Adempiere.getThreadPoolExecutor().submit(new DesktopRunnable(cr, parent.getDesktop()));				
	        	}
	        }
            
            if (dps.length == 0)
            {
            	dashboardLineLayout = new Hlayout();
				dashboardLineLayout.setAttribute(LINE_ATTRIBUTE, "0");
				dashboardLineLayout.setAttribute(IS_SHOW_IN_DASHBOARD_ATTRIBUTE, isShowInDashboard);
				dashboardLineLayout.setAttribute(IS_ADDITIONAL_ROW_ATTRIBUTE, true);
        		dashboardLineLayout.setSclass("dashboard-row");
        		Anchorchildren dashboardColumn = new Anchorchildren();
        		dashboardColumn.setAnchor((width-5) + "%" + " 100%");
        		if (!ClientInfo.isMobile())
        		{
        			dashboardColumn.setDroppable("true");
        			dashboardColumn.addEventListener(Events.ON_DROP, this);
        		}
        		dashboardColumn.appendChild(dashboardLineLayout);
        		rowList.add(dashboardColumn);
                dashboardLayout.appendChild(dashboardColumn);
                ZKUpdateUtil.setWidth(dashboardLineLayout, "100%");
            }
            else if (isShowInDashboard)
            {
            	// additional row
            	dashboardLineLayout = new Hlayout();
            	ZKUpdateUtil.setWidth(dashboardLineLayout, "100%");
				dashboardLineLayout.setAttribute(LINE_ATTRIBUTE, currentLineNo + 1);
				dashboardLineLayout.setAttribute(IS_SHOW_IN_DASHBOARD_ATTRIBUTE, isShowInDashboard);
				dashboardLineLayout.setAttribute(IS_ADDITIONAL_ROW_ATTRIBUTE, true);
        		dashboardLineLayout.setSclass("dashboard-row");
        		Anchorchildren dashboardLine = new Anchorchildren();
        		dashboardLine.setAnchor(width + "% 1%");
        		ZKUpdateUtil.setHflex(dashboardLine, "min");
        		if (!ClientInfo.isMobile())
        		{
        			dashboardLine.setDroppable("true");
        			dashboardLine.addEventListener(Events.ON_DROP, this);
        		}
        		dashboardLine.appendChild(dashboardLineLayout);
        		rowList.add(dashboardLine);
                dashboardLayout.appendChild(dashboardLine);
                ZKUpdateUtil.setWidth(dashboardLineLayout, "100%");
                ZKUpdateUtil.setHflex(dashboardLineLayout, "1");
            }
		}
        catch (Exception e)
        {
			logger.log(Level.WARNING, "Failed to create dashboard content", e);
		}
                
        if (!update)
        {
        	startDashboardRunnable(parent);
		}
	}
	
	/**
	 * Find dashboard gadget panel by PA_DashboardContent_ID and PA_DashboardPreference_ID
	 * @param PA_DashboardContent_ID
	 * @param PA_DashboardPreference_ID
	 * @return {@link Panel}
	 */
	private Panel findPanel(int PA_DashboardContent_ID, int PA_DashboardPreference_ID) {
		for(Panel panel : panelList) {
			Object value1 = panel.getAttribute(MDashboardPreference.COLUMNNAME_PA_DashboardContent_ID);
			Object value2 = panel.getAttribute(MDashboardPreference.COLUMNNAME_PA_DashboardPreference_ID);
			if (value1 != null && value1 instanceof Number && value2 != null && value2 instanceof Number) {
				int id1 = ((Number)value1).intValue();
				int id2 = ((Number)value2).intValue();
				if (id1 == PA_DashboardContent_ID && id2 == PA_DashboardPreference_ID)
					return panel;
			}
		}
		return null;
	}

	/**
	 * Create gadget components in background thread
	 * @param dashboardContent MDashboardContent
	 * @param dashboardRunnable DashboardRunnable
	 * @param contextPath
	 * @param parentComponent
	 * @param components list to add created Component
	 * @param zulComponent Component created from zul in Event Listener thread
	 * @param spt ServerPushTemplate
	 * @throws Exception
	 */
	private void asyncRenderComponents(MDashboardContent dashboardContent, DashboardRunnable dashboardRunnable, String contextPath, 
			HtmlBasedComponent parentComponent, List<Component> components, Component zulComponent, ServerPushTemplate spt) throws Exception {
		// HTML content
        String htmlContent = dashboardContent.get_ID() > 0 ? dashboardContent.get_Translation(MDashboardContent.COLUMNNAME_HTML) : null;
        if(!Util.isEmpty(htmlContent))
        {
            StringBuilder result = new StringBuilder("<html><head>");

    		URL url = getClass().getClassLoader().getResource("org/compiere/css/PAPanel.css");
			InputStreamReader ins;
			BufferedReader bufferedReader = null;
			try {
				ins = new InputStreamReader(url.openStream());
				bufferedReader = new BufferedReader( ins );
				String cssLine;
				result.append("<style type=\"text/css\">");
				while ((cssLine = bufferedReader.readLine()) != null)
					result.append(cssLine + "\n");
				result.append("</style>");
			} catch (Exception e1) {
				logger.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
			} finally{
				if (bufferedReader != null) {
					try {
						bufferedReader.close();
					} catch (Exception e) {}
					bufferedReader = null;
				}
			}
			result.append("</head><body><div class=\"content\">\n");
        	result.append(stripHtml(htmlContent, false) + "<br>\n");
        	result.append("</div>\n</body>\n</html>");

            Html html = new Html();
            html.setContent(result.toString());
            components.add(html);
        }

    	// Window
    	int AD_Window_ID = dashboardContent.getAD_Window_ID();
    	if(AD_Window_ID > 0)
    	{
        	int AD_Menu_ID = dashboardContent.getAD_Menu_ID();
        	Div div = new Div();
			ToolBarButton btn = new ToolBarButton(String.valueOf(AD_Menu_ID));
			I_AD_Menu menu = dashboardContent.getAD_Menu();
			btn.setLabel(menu.getName());
			btn.setAttribute("AD_Menu_ID", AD_Menu_ID);
			btn.addEventListener(Events.ON_CLICK, this);
			div.appendChild(btn);
			components.add(div);
    	}
    	
    	//Report & Process
    	int AD_Process_ID = dashboardContent.getAD_Process_ID();
    	if(AD_Process_ID > 0)
    	{
    		boolean systemAccess = false;
    		MProcess process = MProcess.get(Env.getCtx(), AD_Process_ID);
			String accessLevel = process.getAccessLevel();
			if (   MTable.ACCESSLEVEL_All.equals(accessLevel)
				|| MTable.ACCESSLEVEL_SystemOnly.equals(accessLevel)
				|| MTable.ACCESSLEVEL_SystemPlusClient.equals(accessLevel)) {
				systemAccess = true;
			}
    		int thisClientId = Env.getAD_Client_ID(Env.getCtx());
    		if((thisClientId == 0 && systemAccess) || thisClientId != 0) {
	        	String sql = "SELECT AD_Menu_ID FROM AD_Menu WHERE AD_Process_ID=?";
	        	int AD_Menu_ID = DB.getSQLValueEx(null, sql, AD_Process_ID);
				ToolBarButton btn = new ToolBarButton();
				MMenu menu = new MMenu(Env.getCtx(), AD_Menu_ID, null);					
				btn.setAttribute("AD_Menu_ID", AD_Menu_ID);
				btn.addEventListener(Events.ON_CLICK, this);					
				
				if (dashboardContent.isEmbedReportContent()) 
				{
	    			addDrillAcrossEventListener(AD_Process_ID, parentComponent);
					String processParameters = dashboardContent.getProcessParameters();
					ReportData reportData = generateReport(AD_Process_ID, dashboardContent.getAD_PrintFormat_ID(), processParameters, parentComponent, contextPath);
					
					Div layout = new Div();
					layout.setHeight("100%");
					layout.setStyle("display: flex;flex-direction: column;");
					components.add(layout);
					Iframe iframe = new Iframe();
					iframe.setSclass("dashboard-report-iframe");
					iframe.setStyle("flex-grow: 1;");
					iframe.setContent(reportData.getContent());
					if(iframe.getContent() != null)
						layout.appendChild(iframe);
					else
						layout.appendChild(createFillMandatoryLabel(dashboardContent));
	
					Toolbar toolbar = new Toolbar();
					LayoutUtils.addSclass("dashboard-report-toolbar", toolbar);
					layout.appendChild(toolbar);
					btn.setLabel(Msg.getMsg(Env.getCtx(), "OpenRunDialog"));
					toolbar.appendChild(btn);
					
					if(iframe.getContent() != null && reportData.getRowCount() >= 0) {
						btn = new ToolBarButton();
						btn.setAttribute("AD_Process_ID", AD_Process_ID);
						btn.setAttribute("ProcessParameters", processParameters);
						btn.setAttribute("AD_PrintFormat_ID", dashboardContent.getAD_PrintFormat_ID());
						btn.addEventListener(Events.ON_CLICK, this);
						btn.setLabel(Msg.getMsg(Env.getCtx(), "ViewReportInNewTab"));
						toolbar.appendChild(new Separator("vertical"));
						toolbar.appendChild(btn);
					}
					btn = new ToolBarButton();
					if (ThemeManager.isUseFontIconForImage()) {
						btn.setIconSclass(Icon.getIconSclass(Icon.REFRESH));
						btn.setSclass("trash-toolbarbutton");
					}
					else
						btn.setImage(ThemeManager.getThemeResource("images/Refresh16.png"));
					
					toolbar.appendChild(btn);	

					Label rowCountLabel = new Label(Msg.getMsg(Env.getCtx(), "RowCount", new Object[] {reportData.getRowCount()}));
					if(reportData.getRowCount() >= 0) {
						LayoutUtils.addSclass("rowcount-label", rowCountLabel);
						toolbar.appendChild(rowCountLabel);
					}
					
					btn.addEventListener(Events.ON_CLICK, e -> {
						ReportData refreshedData = generateReport(AD_Process_ID, dashboardContent.getAD_PrintFormat_ID(), processParameters, parentComponent, contextPath);
						iframe.setContent(refreshedData.getContent());
						if(refreshedData.getRowCount() >= 0)
							rowCountLabel.setValue(Msg.getMsg(Env.getCtx(), "RowCount", new Object[] {refreshedData.getRowCount()}));
					});			
				}
				else
				{
					btn.setLabel(menu.getName());
					components.add(btn);
				}
    		}
    	}

    	// Goal
    	int PA_Goal_ID = dashboardContent.getPA_Goal_ID();
    	if(PA_Goal_ID > 0)
    	{
            String goalDisplay = dashboardContent.getGoalDisplay();
            MGoal goal = new MGoal(Env.getCtx(), PA_Goal_ID, null);
            if(MDashboardContent.GOALDISPLAY_GaugeIndicator.equals(goalDisplay)) {
            	WPerformanceIndicator.Options options = new WPerformanceIndicator.Options();
            	options.colorMap = new HashMap<String, Color>();
            	options.colorMap.put(WPerformanceIndicator.DIAL_BACKGROUND, new Color(224, 224, 224, 1));
            	WPAWidget paWidget = new WPAWidget(goal, options, dashboardContent.isShowTitle());
            	components.add(paWidget);
            	spt.executeAsync(() -> LayoutUtils.addSclass("performance-gadget", parentComponent));
            } else {
            	//link to open performance detail
            	Div div = new Div();
            	Toolbarbutton link = new Toolbarbutton();
            	if (ThemeManager.isUseFontIconForImage())
            		link.setIconSclass(Icon.getIconSclass(Icon.ZOOM));
            	else
            		link.setImage(ThemeManager.getThemeResource("images/Zoom16.png"));
            	link.setAttribute("PA_Goal_ID", PA_Goal_ID);
            	link.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
            		public void onEvent(Event event) throws Exception {
            			int PA_Goal_ID = (Integer)event.getTarget().getAttribute("PA_Goal_ID");
            			MGoal goal = new MGoal(Env.getCtx(), PA_Goal_ID, null);
            			new WPerformanceDetail(goal);
            		}
            	});
            	div.appendChild(link);
            	components.add(div);
            	
            	WGraph graph = new WGraph(goal, 55, false, true,
	            		!(MDashboardContent.GOALDISPLAY_Chart.equals(goalDisplay)),
	            		MDashboardContent.GOALDISPLAY_Chart.equals(goalDisplay));
            	components.add(graph);
            }
    	}

    	// Component created from ZUL file url
    	if(zulComponent != null)
    	{
        	try {
            	if (zulComponent instanceof Include)
            		zulComponent = zulComponent.getFirstChild();
            	
            	if (zulComponent instanceof DashboardPanel)
            	{
                	DashboardPanel dashboardPanel = (DashboardPanel) zulComponent;
                	if (!dashboardPanel.getChildren().isEmpty()) {
                		components.add(dashboardPanel);
                		addDashboardPanel(dashboardPanel);
                	}
            	}
            	else
            	{
            		components.add(zulComponent);
            	}
			} catch (Exception e) {
				throw new AdempiereException(e);
			}
    	}
    	
    	//chart
    	final int AD_Chart_ID = dashboardContent.getAD_Chart_ID();
    	if (AD_Chart_ID > 0) {
    		final Div chartPanel = new Div();	        	
    		chartPanel.setSclass("chart-gadget");
    		final MChart chartModel = new MChart(Env.getCtx(), AD_Chart_ID, null);
    		components.add(chartPanel);
    		chartPanel.addEventListener(Events.ON_AFTER_SIZE, new EventListener<AfterSizeEvent>() {
				@Override
				public void onEvent(AfterSizeEvent event) throws Exception {
	        		int width = event.getWidth()*90/100;
	        		int height = event.getHeight();
	        		//set normal height
	        		if (height == 0) {
	        			height = width * 85 / 100;
	        			chartPanel.setHeight(height+"px");
	        		}
	        		chartPanel.getChildren().clear();
	        		ChartModel model = new ChartModel();
	        		model.chart = chartModel;
	        		renderChart(chartPanel, width, height, model, dashboardContent.isShowTitle());
				}
			});
    	}
    	
    	// Status Line
    	final int AD_StatusLine_ID = dashboardContent.getAD_StatusLine_ID();
    	if(AD_StatusLine_ID > 0) {
    		MStatusLine sl = new MStatusLine(Env.getCtx(), AD_StatusLine_ID, null);
    		final Html statusLineHtml = new Html();
    		statusLineHtml.setContent(sl.parseLine(0));
    		Div div = new Div();
    		if (sl.getAD_Style_ID() > 0) {
	    		MStyle style = MStyle.get(sl.getAD_Style_ID());
				String css = style.buildStyle(ThemeManager.getTheme(), new DefaultEvaluatee(), false);				
				if (!Util.isEmpty(css, true)) {
					Style htmlStyle = new Style();
					htmlStyle.setContent("@scope {\n"+css+"\n}\n");
					div.appendChild(htmlStyle);
				}			
    		}
    		div.appendChild(statusLineHtml);
    		div.setSclass("statusline-gadget");
    		components.add(div);
    		spt.executeAsync(() -> LayoutUtils.addSclass("statusline-wrapper", ((HtmlBasedComponent) parentComponent.getParent())));
    	}
	}
	
	/**
	 * Synchronous render of gadget content in foreground UI (Event Listener) thread
	 * @param content must be an instanceof {@link HtmlBasedComponent}
	 * @param dashboardContent MDashboardContent
	 * @param dashboardRunnable DashboardRunnable
	 * @return true if gadget dashboard is not empty
	 * @throws Exception
	 */
	public boolean render(Component content, MDashboardContent dashboardContent, DashboardRunnable dashboardRunnable) throws Exception {		
		List<Component> components = new ArrayList<>();
		Component zulComponent = null;
		if (!Util.isEmpty(dashboardContent.getZulFilePath(), true)) {
        	try {	        	        		
        		zulComponent = Extensions.getDashboardGadget(dashboardContent.getZulFilePath(), content, dashboardContent);
        	} catch (Exception e) {
        		throw new AdempiereException(e);
        	}
		}
		ServerPushTemplate spt = new ServerPushTemplate(content.getDesktop());
		HtmlBasedComponent parentComponent = (HtmlBasedComponent) content;
		asyncRenderComponents(dashboardContent, dashboardRunnable, Executions.getCurrent().getContextPath(), parentComponent, components, 
				zulComponent, spt);		
		boolean empty = components.isEmpty();		
		for(Component c : components) {
			if (c.getParent() != parentComponent) {
				parentComponent.appendChild(c);
			}
			if (c instanceof DashboardPanel) {
				DashboardPanel dpanel = (DashboardPanel) c;
				if (dpanel.isLazy()) {
					try {
						dpanel.refresh(spt);
					} catch (Exception e) {
						logger.log(Level.SEVERE, e.getMessage(), e);
					}
				}
			}
		}
		
    	return !empty;
	}
	
	/**
	 * Add onDrillAcross, onZoom and onDrillDown Event Listener to component
	 * @param processID AD_Process_ID
	 * @param component Component
	 */
	private void addDrillAcrossEventListener(int processID, Component component) {
		component.addEventListener(DrillEvent.ON_DRILL_ACROSS, new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				if (event instanceof DrillEvent) {
					Clients.clearBusy();
					DrillEvent de = (DrillEvent) event;
					if (de.getData() != null && de.getData() instanceof DrillData) {
						DrillData data = (DrillData) de.getData();
						if(data.getData() instanceof JSONArray) {
							JSONArray jsonData = (JSONArray) data.getData();
							if(jsonData.indexOf(String.valueOf(processID)) < 0)
								return;
						}
						AEnv.actionDrill(data, SessionManager.getAppDesktop().findWindowNo(component), processID);
					}
				}
			}
		});

		component.addEventListener("onZoom", event -> {
			Clients.clearBusy();
			if (event instanceof ZoomEvent) {
				ZoomEvent ze = (ZoomEvent) event;
				if (ze.getData() != null && ze.getData() instanceof MQuery) {
					AEnv.zoom((MQuery) ze.getData());
				}
			}
		});

		component.addEventListener(DrillEvent.ON_DRILL_DOWN, event -> {
			Clients.clearBusy();
			if (event instanceof DrillEvent) {
				DrillEvent de = (DrillEvent) event;
				if (de.getData() != null && de.getData() instanceof DrillData) {
					DrillData data = (DrillData) de.getData();
					MQuery query = data.getQuery();
					executeDrill(query);
				}
			}
		});
	}
	
	/**
	 * 	Execute Drill to Query
	 * 	@param query MQuery
	 */
	private void executeDrill (MQuery query)
	{
		int AD_Table_ID = MTable.getTable_ID(query.getTableName());
		if (!MRole.getDefault().isCanReport(AD_Table_ID))
		{
			Dialog.error(0, "AccessCannotReport", query.getTableName());
			return;
		}
		if (AD_Table_ID != 0)
			new WReport(AD_Table_ID, query);
	}	//	executeDrill

	@Override
	public void onEvent(Event event) throws Exception {
		Component comp = event.getTarget();
        String eventName = event.getName();
        String layoutOrientation = MSysConfig.getValue(MSysConfig.DASHBOARD_LAYOUT_ORIENTATION, Env.getAD_Client_ID(Env.getCtx()));
        
        if(!layoutOrientation.equals(DASHBOARD_LAYOUT_ROWS) && !layoutOrientation.equals(DASHBOARD_LAYOUT_COLUMNS))
        	layoutOrientation = DASHBOARD_LAYOUT_COLUMNS;
        
		if (event instanceof MaximizeEvent)
		{
			MaximizeEvent me = (MaximizeEvent) event;
			Panel panel = (Panel) event.getTarget();
	    	if (me.isMaximized()) {
	    		prevParent = panel.getParent();
	    		prevNext = panel.getNextSibling();
	    		panel.detach();
	    		if(columnList != null) {
		    		for (Anchorchildren anchorChildren : columnList) {
		    			anchorChildren.detach();
		    		}
	    		}
	    		else {
	    			for (Anchorchildren anchorChildren : rowList) {
		    			anchorChildren.detach();
		    		}
	    		}
	    		dashboardLayout.appendChild(maximizedHolder);
	    		maximizedHolder.appendChild(panel);
	    		panel.setSclass("dashboard-widget dashboard-widget-max");
	    	} else {
	    		maximizedHolder.detach();
	    		panel.detach();
	    		prevParent.insertBefore(panel, prevNext);
	    		if(columnList != null) {
		    		for (Anchorchildren anchorChildren : columnList) {
		    			dashboardLayout.appendChild(anchorChildren);
		    		}
	    		}
	    		else {
	    			for (Anchorchildren anchorChildren : rowList) {
		    			dashboardLayout.appendChild(anchorChildren);
		    		}
	    		}
	    		panel.setSclass("dashboard-widget");
	    		//following 2 line needed for restore to size the panel correctly
				ZKUpdateUtil.setHflex(panel, (String)panel.getAttribute(FLEX_GROW_ATTRIBUTE));
				ZKUpdateUtil.setHeight(panel, "100%");
				
				//notify panel content component
				if (panel.getPanelchildren() != null) {
					panel.getPanelchildren().getChildren().forEach(child -> {
						Executions.schedule(dashboardLayout.getDesktop(), e -> Events.postEvent(child, event), new Event("onPostRestore"));
					});
				}
	    	}
		}
		else if(eventName.equals(Events.ON_CLICK))
        {
            if(comp instanceof ToolBarButton)
            {
            	ToolBarButton btn = (ToolBarButton) comp;
            	
            	if (btn.getAttribute("AD_Menu_ID") != null)
            	{
	            	int menuId = (Integer)btn.getAttribute("AD_Menu_ID");
	            	if(menuId > 0) SessionManager.getAppDesktop().onMenuSelected(menuId);
            	}
            	else if (btn.getAttribute("AD_Process_ID") != null)
            	{
            		int processId = (Integer)btn.getAttribute("AD_Process_ID");
            		String parameters = (String)btn.getAttribute("ProcessParameters");
            		int printFormatId = (Integer)btn.getAttribute("AD_PrintFormat_ID");
            		if (processId > 0)
            			openReportInViewer(processId, printFormatId, parameters);
            	}
            }else if(comp instanceof A)
            {	
				String name = comp.getAttribute("title").toString();
				String description = comp.getAttribute("description")!=null ? comp.getAttribute("description").toString() : null;
				String help = comp.getAttribute("help")!=null ? comp.getAttribute("help").toString() : null;
            	SessionManager.getAppDesktop().updateHelpTooltip(name, description, help, null, null);
            }
        }
		else if (eventName.equals(Events.ON_DROP))
		{
			DropEvent de = (DropEvent) event;
    		Component dragged = de.getDragged();
        	
    		if(dragged instanceof Caption)
    		{
    			Caption caption = (Caption) dragged;
       			Panel panel = null;
       			if (caption.getParent() instanceof Panel)
       				panel = (Panel) caption.getParent();

       			if (panel == null)
    				;
       			else if(comp instanceof Panel)
	        	{
	        		Panel target = (Panel) comp;
	
	        		boolean isParentHVlayout = false;
	        		if(layoutOrientation.equals(DASHBOARD_LAYOUT_ROWS))
	        			isParentHVlayout = target.getParent() instanceof Hlayout;
	        		else
	        			isParentHVlayout = target.getParent() instanceof Vlayout;
	        		
        			if (target.getParent() != null && isParentHVlayout)
        			{
        				Component dashboardColumnLayout;
        				if(layoutOrientation.equals(DASHBOARD_LAYOUT_ROWS)) {
        					dashboardColumnLayout = (Hlayout) target.getParent();
        					List<Component> children = dashboardColumnLayout.getParent().getChildren();
        					if(children != null && (children.size() >= MAX_NO_OF_PREFS_IN_ROW))
        						return;
        				}
        				else
        					dashboardColumnLayout = (Vlayout) target.getParent();
        				Component prevParent = panel.getParent();
        				dashboardColumnLayout.insertBefore(panel, target);        				
        				saveDashboardPreference(dashboardColumnLayout, prevParent);
        			}        			
	        	}
	        	else if (comp instanceof Anchorchildren)
	        	{
	        		Anchorchildren target = (Anchorchildren) comp; 	
	        		
	        		boolean isFirstChildHVlayout = false;
	        		if(layoutOrientation.equals(DASHBOARD_LAYOUT_ROWS)) {
	        			isFirstChildHVlayout = target.getFirstChild() instanceof Hlayout;
	        			List<Component> children = target.getChildren();
	        			if(children != null && (children.size() >= MAX_NO_OF_PREFS_IN_ROW))
    						return;
	        		}
	        		else
	        			isFirstChildHVlayout = target.getFirstChild() instanceof Vlayout;
	        		
        			if (target.getFirstChild() != null && isFirstChildHVlayout)
        			{
        				Component dashboardColumnLayout;
        				if(layoutOrientation.equals(DASHBOARD_LAYOUT_ROWS))
        					dashboardColumnLayout = (Hlayout) target.getFirstChild();
        				else
        					dashboardColumnLayout = (Vlayout) target.getFirstChild();
        				Component prevParent = panel.getParent();
        				dashboardColumnLayout.appendChild(panel);
        				saveDashboardPreference(dashboardColumnLayout, prevParent);
        			}
	        	}
    		}
		}
		else if (eventName.equals(Events.ON_OPEN))
		{
			if(comp instanceof Panel)
    		{
    			Panel panel = (Panel) comp;
    			Object value = panel.getAttribute(MDashboardPreference.COLUMNNAME_PA_DashboardPreference_ID);
    			if (value != null)
    			{
    				int PA_DashboardPreference_ID = Integer.parseInt(value.toString());
    				MDashboardPreference preference = new MDashboardPreference(Env.getCtx(), PA_DashboardPreference_ID, null);
    				preference.setIsCollapsedByDefault(!panel.isOpen());
					if (!preference.saveCrossTenantSafe())
						logger.log(Level.SEVERE, "Failed to save dashboard preference " + preference.toString());
    			}
    			
    			//notify panel content component
    			if (panel.getPanelchildren() != null) {
    				for(Component c : panel.getPanelchildren().getChildren()) {
    					Events.postEvent(c, event);
    				}
    			}
    		}
		}
	}
	
	/**
	 * Create Fill Mandatory Process Parameters error label for the reports in dashboard
	 * @return Div
	 */
	private Div createFillMandatoryLabel(MDashboardContent dc) {
		Div wrapper = new Div();
		wrapper.setSclass("fill-mandatory-process-para-wrapper");
		
		Div msgText = new Div();
		msgText.appendChild(new Text(Msg.getMsg(Env.getCtx(), "FillMandatoryParametersDashboard", new Object[] {dc.getEmptyMandatoryProcessPara()})));
		LayoutUtils.addSclass("fill-mandatory-process-para-text", msgText);
		wrapper.appendChild(msgText);
		return wrapper;
	}
	
	/**
	 * Create and save dashboard preference (MDashboardPreference) to DB.
	 * @param AD_User_ID
	 * @param AD_Role_ID
	 */
	private void createDashboardPreference(int AD_User_ID, int AD_Role_ID)
	{
		MDashboardContent[] dcs = MDashboardContentAccess.get(Env.getCtx(),AD_Role_ID, AD_User_ID, null);
		for (MDashboardContent dc : dcs)
		{
			MDashboardPreference preference = new MDashboardPreference(Env.getCtx(), 0, null);
			preference.setAD_Org_ID(0);
			preference.setAD_Role_ID(AD_Role_ID);
			preference.setAD_User_ID(AD_User_ID);
			preference.setColumnNo(dc.getColumnNo());
			preference.setIsCollapsedByDefault(dc.isCollapsedByDefault());
			preference.setIsShowInDashboard(dc.isShowInDashboard());
			preference.setLine(dc.getLine());
			preference.setPA_DashboardContent_ID(dc.getPA_DashboardContent_ID());
			
			if (!preference.save())
				logger.log(Level.SEVERE, "Failed to create dashboard preference " + preference.toString());
		}
	}
	
	/**
	 * Update dashboard preference (MDashboardPreference) in DB.
	 * @param dps
	 * @param dcs
	 * @param ctx
	 * @return true if there are changes
	 */
	private boolean updatePreferences(MDashboardPreference[] dps,MDashboardContent[] dcs, Properties ctx) {
		boolean change = false;
		for (int i = 0; i < dcs.length; i++) {
			
			boolean isNew = true;
			for (int j = 0; j < dps.length; j++) {
				if (dps[j].getPA_DashboardContent_ID() == dcs[i].getPA_DashboardContent_ID()) {
					isNew = false;
				}
			}
			if (isNew) {
				MDashboardPreference preference = new MDashboardPreference(ctx,0, null);
				preference.setAD_Org_ID(0);
				preference.setAD_Role_ID(Env.getAD_Role_ID(ctx));
				preference.setAD_User_ID(Env.getAD_User_ID(ctx));
				preference.setColumnNo(dcs[i].getColumnNo());
				preference.setIsCollapsedByDefault(dcs[i].isCollapsedByDefault());
				preference.setIsShowInDashboard(dcs[i].isShowInDashboard());
				preference.setLine(dcs[i].getLine());
				preference.setPA_DashboardContent_ID(dcs[i].getPA_DashboardContent_ID());

				preference.saveEx();
				if (!change) change = true;
			}
		}
		for (int i = 0; i < dps.length; i++) {
			boolean found = false;
			for (int j = 0; j < dcs.length; j++) {
				if (dcs[j].getPA_DashboardContent_ID() == dps[i].getPA_DashboardContent_ID()) {
					found = true;
				}
			}
			if (!found) {
				dps[i].deleteEx(true);
				if (!change) change = true;
			}
		}
		return change;
	}
	
	/**
	 * Save dashboard preference (MDashboardPreference) to DB.
	 * @param layout
	 * @param prevLayout
	 */
	private void saveDashboardPreference(Component layout, Component prevLayout)
	{
		String layoutOrientation = MSysConfig.getValue(MSysConfig.DASHBOARD_LAYOUT_ORIENTATION, Env.getAD_Client_ID(Env.getCtx()));
		if(layoutOrientation.equals(DASHBOARD_LAYOUT_COLUMNS)) {
			Object value = layout.getAttribute(COLUMN_NO_ATTRIBUTE);
			if (value != null)
			{
				int columnNo = Integer.parseInt(value.toString());
				
				value = layout.getAttribute(IS_SHOW_IN_DASHBOARD_ATTRIBUTE);
				if (value != null)
				{
					boolean isShowInDashboard = Boolean.parseBoolean(value.toString());
							
					List<Component> children = layout.getChildren();
					int counter = 0;
					for (Component child : children)
					{
						if (child instanceof Panel)
						{
							Panel panel = (Panel) child;
							value = panel.getAttribute(MDashboardPreference.COLUMNNAME_PA_DashboardPreference_ID);
			    			if (value != null)
			    			{
			    				++counter;
			    				
			    				int PA_DashboardPreference_ID = Integer.parseInt(value.toString());
			    				MDashboardPreference preference = new MDashboardPreference(Env.getCtx(), PA_DashboardPreference_ID, null);
			    				preference.setColumnNo(columnNo);
			    				preference.setLine(new BigDecimal(counter * 10));
			    				preference.setIsShowInDashboard(isShowInDashboard);
			    				if (!preference.save())
			    					logger.log(Level.SEVERE, "Failed to save dashboard preference " + preference.toString());
			    			}
						}
					}
					
					if (isShowInDashboard)
					{
						value = layout.getAttribute(IS_ADDITIONAL_COLUMN_ATTRIBUTE);
						if (value != null)
						{
							boolean isAdditionalColumn = Boolean.parseBoolean(value.toString());
							if (isAdditionalColumn)
							{
								layout.setAttribute(IS_ADDITIONAL_COLUMN_ATTRIBUTE, false);
								
								int noOfCols = columnList.size(); 
					        	int dashboardWidth = DEFAULT_DASHBOARD_WIDTH;
					            int width = noOfCols <= 0 ? dashboardWidth : dashboardWidth / noOfCols;
					            int extraWidth = 100 - (noOfCols <= 0 ? dashboardWidth : width * noOfCols) - (100 - dashboardWidth - 1);
								
								for (Anchorchildren column : columnList)
									column.setAnchor(width + "%" + " 100%");
	
								// additional column
								Vlayout dashboardColumnLayout = new Vlayout();
								dashboardColumnLayout.setAttribute(COLUMN_NO_ATTRIBUTE, columnNo + 1);
								dashboardColumnLayout.setAttribute(IS_SHOW_IN_DASHBOARD_ATTRIBUTE, isShowInDashboard);
								dashboardColumnLayout.setAttribute(IS_ADDITIONAL_COLUMN_ATTRIBUTE, true);
				        		Anchorchildren dashboardColumn = new Anchorchildren();
				        		dashboardColumn.setAnchor(extraWidth + "% 100%");
				        		if (!ClientInfo.isMobile()) {
				        			dashboardColumn.setDroppable("true");
				        			dashboardColumn.addEventListener(Events.ON_DROP, this);
				        		}
				        		dashboardColumn.appendChild(dashboardColumnLayout);
				        		columnList.add(dashboardColumn);
				                dashboardLayout.appendChild(dashboardColumn);
				                ZKUpdateUtil.setWidth(dashboardColumnLayout, "100%");
				                
				                dashboardLayout.invalidate();			                
							}
						}
					}
					
	                if (!dashboardRunnable.isEmpty())
	                	dashboardRunnable.refreshDashboard(false);
				}
			}
		}
		else {
			
			// detach row if empty
			if(prevLayout != null) {
				if((prevLayout.getChildren() == null) || (prevLayout.getChildren().size() <= 0))
					prevLayout.getParent().detach();
			}
			
			Object value = layout.getAttribute(LINE_ATTRIBUTE);
			if (value != null)
			{
				int lineNo = Integer.parseInt(value.toString());
				
				value = layout.getAttribute(IS_SHOW_IN_DASHBOARD_ATTRIBUTE);
				if (value != null)
				{
					boolean isShowInDashboard = Boolean.parseBoolean(value.toString());
							
					List<Component> children = layout.getChildren();
					int counter = 0;
					for (Component child : children)
					{
						if (child instanceof Panel)
						{
							Panel panel = (Panel) child;
							value = panel.getAttribute(MDashboardPreference.COLUMNNAME_PA_DashboardPreference_ID);
			    			if (value != null)
			    			{
			    				int PA_DashboardPreference_ID = Integer.parseInt(value.toString());
			    				MDashboardPreference preference = new MDashboardPreference(Env.getCtx(), PA_DashboardPreference_ID, null);
			    				preference.setColumnNo(counter++);
			    				preference.setLine(new BigDecimal(lineNo));
			    				preference.setIsShowInDashboard(isShowInDashboard);
			    				if (!preference.save())
			    					logger.log(Level.SEVERE, "Failed to save dashboard preference " + preference.toString());
			    				if(layout instanceof Hlayout) {
			    					int flexGrow = (flexGrow = preference.getFlexGrow()) > 0 ? flexGrow : DEFAULT_FLEX_GROW;
			    					ZKUpdateUtil.setHflex(panel, String.valueOf(flexGrow));
			    				}
			    			}
						}
					}
					
					if (isShowInDashboard)
					{
						value = layout.getAttribute(IS_ADDITIONAL_ROW_ATTRIBUTE);
						if (value != null)
						{
							boolean isAdditionalRow = Boolean.parseBoolean(value.toString());
							if (isAdditionalRow)
							{
								if(layout instanceof Hlayout) {
									Anchorchildren anchorCh = ((Anchorchildren) layout.getParent());
									Component parent = anchorCh.getParent();
									rowList.remove(anchorCh);
									anchorCh.detach();
									anchorCh = new Anchorchildren("100%");
									ZKUpdateUtil.setHflex(anchorCh, "min");
					        		if (!ClientInfo.isMobile())
					        		{
					        			anchorCh.setDroppable("true");
					        			anchorCh.addEventListener(Events.ON_DROP, this);
					        		}
					        		rowList.add(anchorCh);
									anchorCh.appendChild(layout);
									parent.appendChild(anchorCh);
								}
								layout.setAttribute(IS_ADDITIONAL_ROW_ATTRIBUTE, false);
					            int width = 100;
					            
								// additional row
								Hlayout dashboardLineLayout = new Hlayout();
				            	ZKUpdateUtil.setWidth(dashboardLineLayout, "100%");
								dashboardLineLayout.setAttribute(LINE_ATTRIBUTE, lineNo + 1);
								dashboardLineLayout.setAttribute(IS_SHOW_IN_DASHBOARD_ATTRIBUTE, isShowInDashboard);
								dashboardLineLayout.setAttribute(IS_ADDITIONAL_ROW_ATTRIBUTE, true);
				        		dashboardLineLayout.setSclass("dashboard-row");
				        		Anchorchildren dashboardLine = new Anchorchildren();
				        		dashboardLine.setAnchor(width + "% 1%");
				        		ZKUpdateUtil.setHflex(dashboardLine, "min");
				        		if (!ClientInfo.isMobile())
				        		{
				        			dashboardLine.setDroppable("true");
				        			dashboardLine.addEventListener(Events.ON_DROP, this);
				        		}
				        		dashboardLine.appendChild(dashboardLineLayout);
				        		rowList.add(dashboardLine);
				                dashboardLayout.appendChild(dashboardLine);
				                ZKUpdateUtil.setWidth(dashboardLineLayout, "100%");
				                ZKUpdateUtil.setHflex(dashboardLineLayout, "1");
							}
						}
					}
					
	                if (!dashboardRunnable.isEmpty())
	                	dashboardRunnable.refreshDashboard(false);
				}
			}
		}
	}
	
	/**
	 * @param page
	 * @param desktop
	 */
	public void onSetPage(Page page, Desktop desktop) {
		if (dashboardTimer != null) {
			
			DashboardRunnable tmp = dashboardRunnable;			
			dashboardRunnable = new DashboardRunnable(tmp, desktop);
			dashboardTimer.setPage(page);
		}
	}
	
	/**
	 * clean up for logout
	 */
	public void onLogOut() {
		if (dashboardTimer != null) {
			dashboardTimer.detach();
			dashboardTimer = null;
		}
		if (dashboardRunnable != null) {			
			dashboardRunnable = null;
		}
		dashboardLayout.detach();
		dashboardLayout = null;
	}

	/**
	 * add dashboardPanel to {@link #dashboardRunnable}
	 * @param dashboardPanel
	 */
	private void addDashboardPanel(DashboardPanel dashboardPanel) {
		if (dashboardRunnable != null) {
			dashboardRunnable.add(dashboardPanel);
		}
	}
	
	/**
	 * Strip &lt;html&gt;, &lt;body&gt; and &lt;head&gt; tag
	 * @param htmlString
	 * @param all true to escape &lt; and &gt;
	 * @return stripped htmlString
	 */
	private String stripHtml(String htmlString, boolean all) {
		htmlString = htmlString
		.replace("<html>", "")
		.replace("</html>", "")
		.replace("<body>", "")
		.replace("</body>", "")
		.replace("<head>", "")
		.replace("</head>", "");
		
		if (all)
			htmlString = htmlString
			.replace(">", "&gt;")
			.replace("<", "&lt;");
		return htmlString;
	}
		
	/**
	 * Run report
	 * @param AD_Process_ID
	 * @param AD_PrintFormat_ID
	 * @param parameters Report parameters
	 * @return {@link ReportEngine}
	 */
	private ReportEngine runReport(int AD_Process_ID, int AD_PrintFormat_ID, String parameters) {
   		MProcess process = MProcess.get(Env.getCtx(), AD_Process_ID);
		if (!process.isReport() || process.getAD_ReportView_ID() == 0)
			 throw new IllegalArgumentException("Not a Report AD_Process_ID=" + process.getAD_Process_ID()
				+ " - " + process.getName());
		//	Process
		MPInstance pInstance = new MPInstance(Env.getCtx(), AD_Process_ID, 0, 0, null);
		if(AD_PrintFormat_ID > 0)
			pInstance.setAD_PrintFormat_ID(AD_PrintFormat_ID);
		pInstance.setIsProcessing(true);
		pInstance.saveEx();
		try {
			if(!fillParameter(pInstance, parameters))
				return null;
			//
			ProcessInfo pi = new ProcessInfo (process.getName(), process.getAD_Process_ID(), 0, 0);
			pi.setAD_User_ID(Env.getAD_User_ID(Env.getCtx()));
			pi.setAD_Client_ID(Env.getAD_Client_ID(Env.getCtx()));
			pi.setAD_PInstance_ID(pInstance.getAD_PInstance_ID());		
			if (!process.processIt(pi, null) && pi.getClassName() != null) 
				throw new IllegalStateException("Process failed: (" + pi.getClassName() + ") " + pi.getSummary());
		
			//	Report
			ReportEngine re = ReportEngine.get(Env.getCtx(), pi);
			if (re == null)
				throw new IllegalStateException("Cannot create Report AD_Process_ID=" + process.getAD_Process_ID()
					+ " - " + process.getName());
			return re;
		}
		finally {			
			pInstance.setIsProcessing(false);
			pInstance.saveEx();
		}
		
	}

	/**
	 * Generate report media (html)
	 * @param AD_Process_ID
	 * @param AD_PrintFormat_ID
	 * @param parameters
	 * @param component
	 * @param contextPath
	 * @return {@link AMedia}
	 * @throws Exception
	 */
	private ReportData generateReport(int AD_Process_ID, int AD_PrintFormat_ID, String parameters, Component component, String contextPath) throws Exception {
		MProcess process = MProcess.get(Env.getCtx(), AD_Process_ID);
		File file = null;
		if(process.getJasperReport() != null) {
			file = runJasperReport(process, parameters, AD_PrintFormat_ID);
			return new ReportData(new AMedia(process.getName(), "html", "text/html", file, false), -1);
		}
			
		ReportEngine re = runReport(AD_Process_ID, AD_PrintFormat_ID, parameters);
		if(re == null)
			return null;
		file = FileUtil.createTempFile(re.getName(), ".html");		
		re.createHTML(file, false, AEnv.getLanguage(Env.getCtx()), new HTMLExtension(contextPath, "rp", 
				component.getUuid(), String.valueOf(AD_Process_ID)));
		return new ReportData(new AMedia(process.getName(), "html", "text/html", file, false), re.getPrintData() != null ? re.getPrintData().getRowCount(false) : 0);
	}

	private File runJasperReport(MProcess process, String parameters, int AD_PrintFormat_ID) {
		MPInstance pInstance = new MPInstance(Env.getCtx(), process.getAD_Process_ID(), 0, 0, null);
		pInstance.setIsProcessing(true);
		pInstance.saveEx();
		try {
			if(!fillParameter(pInstance, parameters))
				return null;
			//
				
			ProcessInfo pi = new ProcessInfo (process.getName(), process.getAD_Process_ID(), 0, 0);
			pi.setExport(true);
			pi.setExportFileExtension("html");
			pi.setAD_User_ID(Env.getAD_User_ID(Env.getCtx()));
			pi.setAD_Client_ID(Env.getAD_Client_ID(Env.getCtx()));
			pi.setAD_PInstance_ID(pInstance.getAD_PInstance_ID());
			if(AD_PrintFormat_ID > 0) {
				MPrintFormat format = new MPrintFormat(Env.getCtx(), AD_PrintFormat_ID, null);
				pi.setTransientObject(format);
			}
		
			//	Report
			ServerProcessCtl.process(pi, null);
			
			return pi.getExportFile();
		}catch(Exception ex) {
			throw new IllegalStateException("Cannot create Report AD_Process_ID=" + process.getAD_Process_ID()
			+ " - " + process.getName());
		}
	}

	/**
	 * Run report and open in report viewer
	 * @param AD_Process_ID
	 * @param AD_PrintFormat_ID
	 * @param parameters
	 */
   	protected void openReportInViewer(int AD_Process_ID, int AD_PrintFormat_ID, String parameters) {
   		ReportEngine re = runReport(AD_Process_ID, AD_PrintFormat_ID, parameters);
   		new ZkReportViewerProvider().openViewer(re);
   	}

   	/**
   	 * Fill Parameters
   	 * @param pInstance
   	 * @param parameters
   	 * @return true if the parameters were filled successfully 
   	 */
	private boolean fillParameter(MPInstance pInstance, String parameters) {	
		MProcessPara[] processParams = pInstance.getProcessParameters();
		if (parameters != null && parameters.trim().length() > 0) {
			Map<String, String> paramMap = MDashboardContent.parseProcessParameters(parameters);
			for (int pi = 0; pi < processParams.length; pi++)
			{
				MPInstancePara iPara = new MPInstancePara (pInstance, processParams[pi].getSeqNo());
				iPara.setParameterName(processParams[pi].getColumnName());
				iPara.setInfo(processParams[pi].getName());
				
				MProcessPara sPara = processParams[pi];
				
				String variable = paramMap.get(iPara.getParameterName());

				if (Util.isEmpty(variable, true)) {
					if(sPara.isMandatory())
						return false;	// empty mandatory parameter
					else
						continue;
				}

				boolean isTo = false;

				for (String paramValue : variable.split(";")) {

					 //				Value - Constant/Variable
					 Object value = paramValue;
					 if (paramValue == null
							 || (paramValue != null && paramValue.length() == 0))
						 value = null;
					 else if (paramValue.startsWith(MColumn.VIRTUAL_UI_COLUMN_PREFIX)) {
						 String sql = paramValue.substring(5);
						 sql = Env.parseContext(Env.getCtx(), 0, sql, false, false);	//	replace variables
						 if (!Util.isEmpty(sql)) {
							 PreparedStatement stmt = null;
							 ResultSet rs = null;
							 try {
								 stmt = DB.prepareStatement(sql, null);
								 rs = stmt.executeQuery();
								 if (rs.next()) {
									 if (   DisplayType.isNumeric(iPara.getDisplayType()) 
										 || DisplayType.isID(iPara.getDisplayType()))
										 value = rs.getBigDecimal(1);
									 else if (DisplayType.isDate(iPara.getDisplayType()))
										 value = rs.getTimestamp(1);
									 else
										 value = rs.getString(1);
								 } else {
									 if (logger.isLoggable(Level.INFO))
										 logger.log(Level.INFO, "(" + iPara.getParameterName() + ") - no Result: " + sql);
								 }
							 }
							 catch (SQLException e) {
								 logger.log(Level.WARNING, "(" + iPara.getParameterName() + ") " + sql, e);
							 }
							 finally{
								 DB.close(rs, stmt);
								 rs = null;
								 stmt = null;
							 }
						 }
					 }	//	SQL Statement
					 else if (paramValue.indexOf('@') != -1)	//	we have a variable
					 {
						 value = Env.parseContext(Env.getCtx(), 0, paramValue, false, false);
					 }	//	@variable@

					 //	No Value
					 if (value == null)
					 {
						 if(sPara.isMandatory()) {
							 return false;	// empty mandatory parameter
						 }
						 else {
							 continue;
						 }
					 }
					 if( DisplayType.isText(iPara.getDisplayType())
								&& Util.isEmpty(String.valueOf(value))) {
						if (logger.isLoggable(Level.FINE)) logger.fine(iPara.getParameterName() + " - empty string");
							break;
					}

					 //	Convert to Type				
					 if (DisplayType.isNumeric(iPara.getDisplayType()))
					 {
						 BigDecimal bd = null;
						 if (value instanceof BigDecimal)
							 bd = (BigDecimal)value;
						 else if (value instanceof Integer)
							 bd = new BigDecimal (((Integer)value).intValue());
						 else
							 bd = new BigDecimal (value.toString());
						DecimalFormat decimalFormat = DisplayType.getNumberFormat(iPara.getDisplayType());
						String info = decimalFormat.format(iPara.getP_Number());
						 if (isTo) {
							 iPara.setP_Number_To(bd);
							 iPara.setInfo_To(info);
						 }
						 else {
							 iPara.setP_Number(bd);
							 iPara.setInfo(info);
						 }
					 }
					 else if (iPara.getDisplayType() == DisplayType.Search || iPara.getDisplayType() == DisplayType.Table || iPara.getDisplayType() == DisplayType.TableDir) {
						 int id = new BigDecimal (value.toString()).intValue();
						 if (isTo) {
							 iPara.setP_Number_To(new BigDecimal (value.toString()));
							 iPara.setInfo_To(getDisplay(pInstance, iPara, id));
						 }
						 else {
							 iPara.setP_Number(new BigDecimal (value.toString()));
							 iPara.setInfo(getDisplay(pInstance, iPara, id));
						 }
					 }
					 else if (DisplayType.isDate(iPara.getDisplayType()))
					 {
						 Timestamp ts = null;
						 if (value instanceof Timestamp)
							 ts = (Timestamp)value;
						 else
							 ts = Timestamp.valueOf(value.toString());
						 SimpleDateFormat dateFormat = DisplayType.getDateFormat(iPara.getDisplayType());
						 String info = dateFormat.format(ts);
						 if (isTo) {
							 iPara.setP_Date_To(ts);
							 iPara.setInfo_To(info);
						 }
						 else {
							 iPara.setP_Date(ts);
							 iPara.setInfo(info);
						 }
					 }
					 else
					 {
						 if (isTo) {
							 iPara.setP_String_To(value.toString());
							 iPara.setInfo_To(value.toString());
						 }
						 else if(DisplayType.isChosenMultipleSelection(iPara.getDisplayType())) {
							 iPara.setP_String(value.toString());
							 iPara.setInfo(getMultiSelectionDisplay(pInstance, iPara, value.toString()));
						 }
						 else {
							 iPara.setP_String(value.toString());
							 iPara.setInfo(value.toString());
						 }
					 }
					 iPara.saveEx();

					 isTo = true;
				 }
			}
		}
		else {
			for(MProcessPara processPara : processParams) {
				if(processPara.isMandatory()) {
					return false;	// empty mandatory parameter
				}
			}
		}
		return true;
	}

	/**
	 * Get display text for CSV values
	 * @param i
	 * @param ip
	 * @param values comma separated value
	 * @return display text
	 */
	private String getMultiSelectionDisplay(MPInstance i, MPInstancePara ip, String values) {
		String returnValue = "";
		String[] splittedValues = values.split("[,]");
		for(String value : splittedValues) {
			if(!Util.isEmpty(returnValue))
				returnValue += ", ";
			returnValue += getDisplay(i, ip, DisplayType.ChosenMultipleSelectionList == ip.getDisplayType() ? value : Integer.parseInt(value));
		}
		return returnValue;
	}
	
	/**
	 * Get display text for value
	 * @param i
	 * @param ip
	 * @param value
	 * @return display text
	 */
	private String getDisplay(MPInstance i, MPInstancePara ip, Object value) {
		try {
			MProcessPara pp = MProcess.get(i.getAD_Process_ID()).getParameter(ip.getParameterName());

			if (pp != null) {
				MLookupInfo mli = MLookupFactory.getLookupInfo(Env.getCtx(), 0, 0, pp.getAD_Reference_ID(), Env.getLanguage(Env.getCtx()), pp.getColumnName(), pp.getAD_Reference_Value_ID(), false, "");

				PreparedStatement pstmt = null;
				ResultSet rs = null;
				StringBuilder name = new StringBuilder("");
				try
				{
					pstmt = DB.prepareStatement(mli.QueryDirect, null);
					if(value instanceof Integer)
						pstmt.setInt(1, (Integer)value);
					else
						pstmt.setString(1, Objects.toString(value, ""));

					rs = pstmt.executeQuery();
					if (rs.next()) {
						name.append(rs.getString(3));
						boolean isActive = rs.getString(4).equals("Y");
						if (!isActive)
							name.insert(0, MLookup.INACTIVE_S).append(MLookup.INACTIVE_E);

						if (rs.next())
							logger.log(Level.SEVERE, "Error while displaying parameter for embedded report - Not unique (first returned) for SQL=" + mli.QueryDirect);
					}
				}
				catch (Exception e) {
					logger.log(Level.SEVERE, "Error while displaying parameter for embedded report - " + mli.KeyColumn + ": SQL=" + mli.QueryDirect + " : " + e);
				}
				finally {
					DB.close(rs, pstmt);
					rs = null;
					pstmt = null;
				}

				return name.toString();
			}
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Failed to retrieve data to display for embedded report " + MProcess.get(i.getAD_Process_ID()).getName() + " : " + ip.getParameterName(), e);
		}

		return Objects.toString(value, "");
	}

	/**
	 * @param clientInfo
	 */
	public void updateLayout(ClientInfo clientInfo) {
		if (isShowInDashboard) {
			if (ClientInfo.isMobile()) {
				int n = 0;
	        	if (ClientInfo.maxWidth(ClientInfo.MEDIUM_WIDTH-1)) {	        		
	        		if (ClientInfo.maxWidth(ClientInfo.SMALL_WIDTH-1)) {
	        			n = 1;
	        		} else {
	        			n = 2;
	        		}
	        	}
	        	if (noOfCols > 0 && n > 0 && noOfCols != n) {
	        		String layoutOrientation = MSysConfig.getValue(MSysConfig.DASHBOARD_LAYOUT_ORIENTATION, Env.getAD_Client_ID(Env.getCtx()));
	                if(layoutOrientation.equals(DASHBOARD_LAYOUT_ROWS))
	                	renderRows(null, null, true, true);
	                else
	                	renderColumns(null, null, true, true);
	        		dashboardLayout.invalidate();
	        	}
        	}
		}			
	}

	/**
	 * Render chart
	 * @param chartPanel
	 * @param width
	 * @param height
	 * @param model {@link ChartModel}
	 * @param showTitle
	 */
	private void renderChart(final Div chartPanel, int width, int height, ChartModel model, boolean showTitle) {
		List<IChartRendererService> list = Extensions.getChartRendererServices();
		for (IChartRendererService renderer : list) {
			if (renderer.renderChart(chartPanel, width, height, model, showTitle))
				break;
		}
	}
	
	/**
	 * Holds information about the report: Report Content, Row Count
	 */
	public class ReportData {
		/** Report content */
		private AMedia content;
		/** Report Row Count */
		private int rowCount = 0;
		
		/**
		 * Constructor
		 * @param content
		 * @param rowCount
		 */
		public ReportData(AMedia content, int rowCount) {
			this.content = content;
			this.rowCount = rowCount;
		}

		/**
		 * Get report content
		 * @return AMedia content
		 */
		public AMedia getContent() {
			return content;
		}

		/**
		 * Get report row count (function rows not included)
		 * @return int row count
		 */
		public int getRowCount() {
			return rowCount;
		}
	}
}
