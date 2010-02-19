package org.orbisgis.core.ui.pluginSystem.workbench;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;

import org.gdms.sql.customQuery.CustomQuery;
import org.gdms.sql.customQuery.QueryManager;
import org.gdms.sql.function.Function;
import org.gdms.sql.function.FunctionManager;
import org.orbisgis.core.language.I18N;
import org.orbisgis.core.ui.pluginSystem.AbstractPlugIn;
import org.orbisgis.core.ui.pluginSystem.PlugIn;
import org.orbisgis.core.ui.pluginSystem.PlugInContext;
import org.orbisgis.core.ui.pluginSystem.ViewPlugIn;
import org.orbisgis.core.ui.pluginSystem.menu.Menu;
import org.orbisgis.core.ui.pluginSystem.utils.CollectionUtil;
import org.orbisgis.core.ui.pluginSystem.utils.StringUtil;

public class FeatureInstaller {

	private interface MenuOG {

		void insert(JMenuItem menuItem, int i);

		String getText();

		int getItemCount();

		void add(JMenuItem menuItem);

	}

	private WorkbenchContext workbenchContext;

	public FeatureInstaller(WorkbenchContext workbenchContext) {
		this.workbenchContext = workbenchContext;
	}

	/**
	 * @return the menu with the given name, or null if no such menu exists
	 */
	public JMenu menuBarMenu(String childName) {
		MenuElement[] subElements = menuBar().getSubElements();
		for (int i = 0; i < subElements.length; i++) {
			if (!(subElements[i] instanceof JMenuItem)) {
				continue;
			}
			JMenuItem menuItem = (JMenuItem) subElements[i];
			if (menuItem.getText().equals(childName)) {
				return (JMenu) menuItem;
			}
		}
		return null;
	}
	
	public String[] behead(String[] a1) {
		String[] a2 = new String[a1.length - 1];
		System.arraycopy(a1, 1, a2, 0, a2.length);
		return a2;
	}

	/**
	 * @return the leaf
	 */
	public JMenu createMenusIfNecessary(PlugIn plugIn, JMenu parent, String[] menuPath) {
		if (menuPath.length == 0) {
			return parent;
		}
		JMenu child = (JMenu) childMenuItem(I18N.translate(plugIn,menuPath[0]), parent);
		if (child == null) {
			child = (JMenu) installMnemonic(new JMenu(I18N.translate(plugIn,menuPath[0])), parent);
			parent.add(child);
		}
		return createMenusIfNecessary(plugIn, child, behead(menuPath));
	}

	public JMenuBar menuBar() {
		return workbenchContext.getWorkbench().getFrame().getJMenuBar();
	}

	public static JMenuItem childMenuItem(String childName, MenuElement menu) {
		if (menu instanceof JMenu) {
			return childMenuItem(childName, ((JMenu) menu).getPopupMenu());
		}
		MenuElement[] childMenuItems = menu.getSubElements();
		for (int i = 0; i < childMenuItems.length; i++) {
			if (childMenuItems[i] instanceof JMenuItem
					&& ((JMenuItem) childMenuItems[i]).getText().equals(
							childName)) {
				return ((JMenuItem) childMenuItems[i]);
			}
		}
		return null;
	}

	public static JMenuItem installMnemonic(JMenuItem menuItem,
			MenuElement parent) {
		String text = menuItem.getText();
		StringUtil.replaceAll(text, "&&", "##");
		int ampersandPosition = text.indexOf('&');
		if (-1 < ampersandPosition && ampersandPosition + 1 < text.length()) {
			menuItem.setMnemonic(text.charAt(ampersandPosition + 1));
			text = StringUtil.replace(text, "&", "", false);
		} else {
			installDefaultMnemonic(menuItem, parent);
		}
		StringUtil.replaceAll(text, "##", "&");
		menuItem.setText(text);
		return menuItem;
	}

	private static void installDefaultMnemonic(JMenuItem menuItem,
			MenuElement parent) {
		outer: for (int i = 0; i < menuItem.getText().length(); i++) {
			char candidate = Character
					.toUpperCase(menuItem.getText().charAt(i));
			if (!Character.isLetter(candidate)) {
				continue;
			}
			for (Iterator j = menuItems(parent).iterator(); j.hasNext();) {
				JMenuItem other = (JMenuItem) j.next();
				if (other.getMnemonic() == candidate) {
					continue outer;
				}
			}
			menuItem.setMnemonic(candidate);
			return;
		}
		menuItem.setMnemonic(menuItem.getText().charAt(0));
	}

	private static Collection menuItems(MenuElement element) {
		ArrayList menuItems = new ArrayList();
		if (element instanceof JMenuBar) {
			for (int i = 0; i < ((JMenuBar) element).getMenuCount(); i++) {
				CollectionUtil.addIfNotNull(((JMenuBar) element).getMenu(i),
						menuItems);
			}
		} else if (element instanceof JMenu) {
			for (int i = 0; i < ((JMenu) element).getItemCount(); i++) {
				CollectionUtil.addIfNotNull(((JMenu) element).getItem(i),
						menuItems);
			}
		} else if (element instanceof JPopupMenu) {
			MenuElement[] children = ((JPopupMenu) element).getSubElements();
			for (int i = 0; i < children.length; i++) {
				if (children[i] instanceof JMenuItem) {
					menuItems.add(children[i]);
				}
			}
		} else {
			// Assert.shouldNeverReachHere(element.getClass().getName());
		}
		return menuItems;
	}

	/**
	 * Workaround for Java Bug 4809393: "Menus disappear prematurely after
	 * displaying modal dialog" Evidently fixed in Java 1.5. The workaround is
	 * to wrap #actionPerformed with SwingUtilities#invokeLater.
	 */

	public JMenuItem addMainMenuItem(PlugIn plugIn, String[] menuPath,
			String menuItemName, boolean checkBox, ImageIcon icon,
			String[] editors, JComponent panel, PlugInContext plugInContext) {	
		WorkbenchContext wbContext = plugInContext.getWorkbenchContext(); 
		// If PlugIn is a View PlugIn get is panel Component
		if (wbContext != null && panel != null)
			((ViewPlugIn) plugIn).createPlugInContext(panel,
					menuItemName, icon, editors, wbContext);
		else
			((AbstractPlugIn) plugIn).setPlugInContext(plugInContext);
		JMenuItem menuItem = installMenuItem(plugIn, menuPath,
				I18N.translate(plugIn,menuItemName), checkBox, icon);
		return menuItem;
	}

	public JMenuItem installMenuItem(PlugIn plugIn, String[] menuPath,
			String menuItemName, boolean checkBox, Icon icon) {
		JMenu menu = menuBarMenu(I18N.translate(plugIn,menuPath[0]));
		if (menu == null) {
			menu = (JMenu) installMnemonic(new JMenu(I18N.translate(plugIn,menuPath[0])), menuBar());
			addToMenuBar(menu);
		}
		JMenu parent = createMenusIfNecessary(plugIn,menu, behead(menuPath));
		final JMenuItem menuItem = installMnemonic(
				checkBox ? new JCheckBoxMenuItem(I18N.translate(plugIn,menuItemName)) : new JMenuItem(
						I18N.translate(plugIn,menuItemName)), parent);
		menuItem.setIcon(icon);
		associate(menuItem, plugIn);
		insert(menuItem, createMenu(parent), null);
		return menuItem;
	}

	private void addToMenuBar(JMenu menu) {
		menuBar().add(menu);
		// Ensure Window and Help are placed at the end.
		JMenu windowMenu = menuBarMenu("Window");
		JMenu helpMenu = menuBarMenu("Help");
		// Customized workbenches may not have Window or Help menus
		if (windowMenu != null) {
			menuBar().remove(windowMenu);
		}
		if (helpMenu != null) {
			menuBar().remove(helpMenu);
		}
		if (windowMenu != null) {
			menuBar().add(windowMenu);
		}
		if (helpMenu != null) {
			menuBar().add(helpMenu);
		}
	}

	private MenuOG createMenu(final JMenu menu) {
		return new MenuOG() {

			public void insert(JMenuItem menuItem, int i) {
				menu.insert(menuItem, i);
			}

			public String getText() {
				return menu.getText();
			}

			public int getItemCount() {
				return menu.getItemCount();
			}

			public void add(JMenuItem menuItem) {
				menu.add(menuItem);
			}
		};
	}

	private void insert(final JMenuItem menuItem, MenuOG parent, Map properties) {
		parent.add(menuItem);
	}

	private void associate(JMenuItem menuItem, PlugIn plugIn) {
		menuItem.addActionListener(AbstractPlugIn.toActionListener(plugIn,
				workbenchContext));		
	}

	public JMenu addPopupMenuItem(WorkbenchFrame frame,
			AbstractPlugIn plugIn, String[] menuPath, String group,
			boolean checkBox, ImageIcon icon, WorkbenchContext wbContext) {
		plugIn.createPlugInContext(wbContext);
		Menu mymenu = null;
		for (int i = 0; i < menuPath.length; i++) {
			String parent = i == 0 ? null : I18N.translate(plugIn,menuPath[i - 1]);
			mymenu = new Menu(parent, I18N.translate(plugIn,menuPath[i]),
					i != menuPath.length - 1 ? null : group, I18N.translate(plugIn,menuPath[i]),
					i != menuPath.length - 1 ? null : icon,
					i != menuPath.length - 1 ? null : plugIn,
					checkBox);
			frame.getMenuTreePopup().addMenu(mymenu);
		}
		return null;
	}

	public void addRegisterCustomQuery(Class<? extends CustomQuery> queryClass) {
		if(QueryManager.getQuery(queryClass.getSimpleName())==null)
			QueryManager.registerQuery(queryClass);
	}

	public void addRegisterFunction(Class<? extends Function> functionClass) {
		if(FunctionManager.getFunction(functionClass.getSimpleName())==null)
			FunctionManager.addFunction(functionClass);
	}

	/*
	 * public void addFilter(WorkbenchFrame frame, PlugIn executable,
	 * WorkbenchContext wbContext) {
	 * frame.getSourceListModel().getFilters().add(executable); }
	 */
}
