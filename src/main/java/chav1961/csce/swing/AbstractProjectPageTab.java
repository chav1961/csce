package chav1961.csce.swing;

import java.util.Locale;

import javax.swing.JPanel;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.swing.useful.JCloseableTab;

abstract class AbstractProjectPageTab extends JPanel implements LocaleChangeListener {
	private static final long 	serialVersionUID = -6796652230128308990L;
	
	private final JCloseableTab	tab;

	AbstractProjectPageTab(final Localizer localizer, final String tabName) {
		tab = new JCloseableTab(localizer, tabName);
	}
	
	public JCloseableTab getTab() {
		return tab;
	}
	
	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
		
	}
}
