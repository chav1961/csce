package chav1961.csce;

import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JLabel;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.ui.swing.useful.JDropTargetPlaceholder;

class FirstScreen extends JDropTargetPlaceholder {
	private static final long serialVersionUID = 1L;

	private static final String		KEY_FIRST_SCREEN_TITLE = "chav1961.csce.FirstScreen.title";
	
	private final Application	parent;

	public FirstScreen(final Application parent) {
		super(parent.getLocalizer(), KEY_FIRST_SCREEN_TITLE, DataFlavor.javaFileListFlavor);
		setHorizontalAlignment(JLabel.CENTER);
		this.parent = parent;
	}

	@Override
	protected boolean processDropOperation(final DataFlavor flavor, final Object content) throws ContentException, IOException {
		if (DataFlavor.javaFileListFlavor.equals(flavor)) {
			@SuppressWarnings("unchecked")
			final List<File>	files = (List<File>)content;
			final File			f = files.get(0);
			
			if (f.exists() && f.isFile() && f.getName().endsWith('.'+Application.PROJECT_SUFFIX)) {
				parent.loadLRU(f.getAbsoluteFile().toURI().getSchemeSpecificPart());
				return true;
			}
		}
		return false;
	}
}
