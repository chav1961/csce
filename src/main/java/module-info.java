module chav1961.csce {
	requires transitive chav1961.purelib;
	requires java.base;
	requires java.desktop;
	requires java.datatransfer;
	
	exports chav1961.csce to chav1961.purelib; 
	opens chav1961.csce.swing to chav1961.purelib; 

	uses chav1961.purelib.ui.swing.interfaces.SwingItemRenderer;
	provides chav1961.purelib.ui.swing.interfaces.SwingItemRenderer with chav1961.csce.swing.renderers.ProjectNavigatorItemRenderer;
}
