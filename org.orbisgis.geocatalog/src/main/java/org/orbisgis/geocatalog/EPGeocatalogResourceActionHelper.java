package org.orbisgis.geocatalog;

import org.orbisgis.core.resourceTree.IResource;
import org.orbisgis.core.resourceTree.ResourceTreeActionExtensionPointHelper;
import org.orbisgis.pluginManager.ExtensionPointManager;

public class EPGeocatalogResourceActionHelper extends
		ResourceTreeActionExtensionPointHelper {

	public static void executeAction(Catalog catalog, String actionId,
			IResource[] selectedResources) {
		ExtensionPointManager<IResourceAction> epm = new ExtensionPointManager<IResourceAction>(
				"org.orbisgis.geocatalog.ResourceAction");
		IResourceAction action = epm.instantiateFrom("/extension/action[@id='"
				+ actionId + "']", "class");
		if (selectedResources.length == 0) {
			action.execute(catalog, null);
		} else {
			for (IResource resource : selectedResources) {
				action.execute(catalog, resource);
			}
		}
	}

}
