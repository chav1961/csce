module chav1961.csce {
	requires transitive chav1961.purelib;
	requires java.base;
	requires java.desktop;
	requires java.datatransfer;
	requires jdk.javadoc;
	requires chav1961.bt.paint;
	
	exports chav1961.csce to chav1961.purelib; 
	opens chav1961.csce.swing to chav1961.purelib; 

	uses chav1961.purelib.i18n.interfaces.DefaultLocalizerProvider;
	provides chav1961.purelib.i18n.interfaces.DefaultLocalizerProvider with chav1961.csce.DefaultLocalizerRepo; 	
	
	uses chav1961.purelib.ui.swing.interfaces.SwingItemRenderer;
	provides chav1961.purelib.ui.swing.interfaces.SwingItemRenderer with chav1961.csce.swing.renderers.ProjectNavigatorItemRenderer;
}
