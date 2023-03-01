package chav1961.csce.swing;

import java.awt.BorderLayout;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.swing.SwingUtils;

public class ProjectVariablesEditor extends JPanel implements LocaleChangeListener {
	private static final long 		serialVersionUID = 1249848430633827785L;
	private static final String					EDITOR_TOOLTIP = "ProjectVariablesEditor.tooltip";
	
	private final Localizer						localizer;
	private final ProjectVariablesTableEditor	table;
	private final JLabel						tooltip = new JLabel();
	
	public ProjectVariablesEditor(final Localizer localizer, final SubstitutableProperties props) {
		super(new BorderLayout(5, 5));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (props == null) {
			throw new NullPointerException("Project proerties can't be null");
		}
		else {
			this.localizer = localizer;
			this.table = new ProjectVariablesTableEditor(localizer, props);
			
			add(tooltip, BorderLayout.NORTH);
			add(new JScrollPane(table), BorderLayout.CENTER);
			
			fillLocalizationStrings();
		}
	}

	
	@Override
	public void setVisible(final boolean aFlag) {
		SwingUtilities.invokeLater(()->table.requestFocusInWindow());
		super.setVisible(aFlag);
	}
	
	public void storeProperties(final SubstitutableProperties props) throws PrintingException {
		if (props == null) {
			throw new NullPointerException("Propertis to set can't be null");
		}
		else {
			table.storeProperties(props);
		}
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		SwingUtils.refreshLocale(table, oldLocale, newLocale);
		fillLocalizationStrings();
	}

	private void fillLocalizationStrings() {
		tooltip.setText(localizer.getValue(EDITOR_TOOLTIP));
	}
}
