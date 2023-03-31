package chav1961.csce.swing;

import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.swing.useful.JCreoleEditor;

class FindPanel extends JPanel implements LocaleChangeListener {
	private static final long serialVersionUID = -2811483115549162659L;

	private final JLabel		findCaption = new JLabel();
	private final JTextField	findString = new JTextField();
	private final JCheckBox		ignoreCase = new JCheckBox();
	private final JCheckBox		wholeWords = new JCheckBox();
	private final JCheckBox		useRegex = new JCheckBox();
	private final JButton		forward = new JButton();
	private final JButton		backward = new JButton();
	private final JButton		close = new JButton();
	
	public FindPanel(final JCreoleEditor editor) {
		final SpringLayout		layout = new SpringLayout();
		setLayout(layout);
		
		add(findCaption);		add(findString);
		add(backward);			add(forward);
		add(ignoreCase);		add(wholeWords);
		add(useRegex);			add(close);
		
		layout.putConstraint(SpringLayout.NORTH, findString, 5, SpringLayout.NORTH, this);

		layout.putConstraint(SpringLayout.WEST, findCaption, 5, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.WEST, findString, 5, SpringLayout.WEST, findCaption);
		layout.putConstraint(SpringLayout.WEST, backward, 5, SpringLayout.EAST, findString);
		layout.putConstraint(SpringLayout.WEST, forward, 5, SpringLayout.EAST, backward);
		layout.putConstraint(SpringLayout.WEST, ignoreCase, 5, SpringLayout.EAST, forward);
		layout.putConstraint(SpringLayout.WEST, wholeWords, 5, SpringLayout.EAST, ignoreCase);
		layout.putConstraint(SpringLayout.WEST, useRegex, 5, SpringLayout.EAST, wholeWords);
		layout.putConstraint(SpringLayout.EAST, close, 5, SpringLayout.EAST, this);

		layout.putConstraint(SpringLayout.SOUTH, findString, 5, SpringLayout.SOUTH, this);
		
		findString.setColumns(30);
		
		fillLocalizedStrings();
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	private void fillLocalizedStrings() {
		// TODO Auto-generated method stub
		
	}
}
