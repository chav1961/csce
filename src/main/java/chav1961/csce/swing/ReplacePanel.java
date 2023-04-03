package chav1961.csce.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Locale;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.swing.useful.JCreoleEditor;

class ReplacePanel extends JPanel implements LocaleChangeListener {
	private static final long serialVersionUID = -1785401640185755817L;

	private static final String	KEY_FIND_CAPTION = "ReplacePanel.find.caption";
	private static final String	KEY_REPLACE_CAPTION = "ReplacePanel.replace.caption";
	private static final String	KEY_IGNORE_CASE = "ReplacePanel.ignore.case";
	private static final String	KEY_IGNORE_CASE_TT = "ReplacePanel.ignore.case.tt";
	private static final String	KEY_WHOLE_WORDS = "ReplacePanel.whole.words";
	private static final String	KEY_WHOLE_WORDS_TT = "ReplacePanel.whole.words.tt";
	private static final String	KEY_USE_REGEX = "ReplacePanel.use.regex";
	private static final String	KEY_USE_REGEX_TT = "ReplacePanel.use.regex.tt";
	private static final String	KEY_BACKWARD = "ReplacePanel.backward";
	private static final String	KEY_BACKWARD_TT = "ReplacePanel.backward.tt";
	private static final String	KEY_FIND = "ReplacePanel.find";
	private static final String	KEY_FIND_TT = "ReplacePanel.find.tt";
	private static final String	KEY_REPLACE = "ReplacePanel.replace";
	private static final String	KEY_REPLACE_TT = "ReplacePanel.replace.tt";
	private static final String	KEY_REPLACE_ALL = "ReplacePanel.replaceAll.tt";
	private static final String	KEY_REPLACE_ALL_TT = "ReplacePanel.replaceAll.tt";
	private static final String	KEY_CLOSE_TT = "ReplacePanel.close";
	
	private static final Icon	ICON_CLOSE = new ImageIcon(FindPanel.class.getResource("icon_close_16.png"));

	private final Localizer		localizer;
	private final JCreoleEditor	editor;
	private final Consumer<ReplacePanel>	onClose;
	private final JLabel		findCaption = new JLabel();
	private final JTextField	findString = new JTextField();
	private final JLabel		replaceCaption = new JLabel();
	private final JTextField	replaceString = new JTextField();
	private final Color			ordinalColor = findString.getForeground();
	private final JCheckBox		backward = new JCheckBox();
	private final JCheckBox		ignoreCase = new JCheckBox();
	private final JCheckBox		wholeWords = new JCheckBox();
	private final JCheckBox		useRegex = new JCheckBox();
	private final JButton		find = new JButton();
	private final JButton		replace = new JButton();
	private final JButton		replaceAll = new JButton();
	private final JButton		close = new JButton();
	
	ReplacePanel(final Localizer localizer, final JCreoleEditor editor, final Consumer<ReplacePanel> onClose) {
		this.localizer = localizer;
		this.editor = editor;
		this.onClose = onClose;

		find.addActionListener((e)->highlightSearchString(FindPanel.search(editor,findString.getText(),backward.isSelected(),ignoreCase.isSelected(),wholeWords.isSelected(),useRegex.isSelected())));
		replace.setEnabled(false);
		replace.addActionListener((e)->{
			if (editor.getSelectedText() != null) {
				editor.replaceSelection(replaceString.getText());
				highlightSearchString(FindPanel.search(editor,findString.getText(),backward.isSelected(),ignoreCase.isSelected(),wholeWords.isSelected(),useRegex.isSelected()));
			}
		});
		replaceAll.setEnabled(false);
		replaceAll.addActionListener((e)->{
			while (FindPanel.search(editor,findString.getText(),backward.isSelected(),ignoreCase.isSelected(),wholeWords.isSelected(),useRegex.isSelected())) {
				editor.replaceSelection(replaceString.getText());
			}
		});
		close.setIcon(ICON_CLOSE);
		close.setPreferredSize(new Dimension(20,20));
		close.addActionListener((e)->onClose.accept(ReplacePanel.this));
		
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		final JPanel	captions = new JPanel(new GridLayout(2, 1));
		final JPanel	strings = new JPanel(new GridLayout(2, 1));
		final JPanel	buttons = new JPanel(new GridLayout(2, 2));
		final JPanel	advanced = new JPanel(new GridLayout(2, 2));
		
		captions.add(findCaption);
		captions.add(replaceCaption);
		add(captions);		
		add(Box.createHorizontalStrut(5));
		strings.add(findString);
		strings.add(replaceString);
		add(strings);		
		add(Box.createHorizontalStrut(5));
		buttons.add(find);
		buttons.add(backward);
		buttons.add(replace);
		buttons.add(replaceAll);
		add(buttons);		
		add(Box.createHorizontalStrut(5));
		advanced.add(ignoreCase);
		advanced.add(wholeWords);
		advanced.add(useRegex);
		add(advanced);		
		add(Box.createHorizontalStrut(15));
		add(close);		
		
		setPreferredSize(new Dimension(60,60));
		fillLocalizedStrings();
	}	
	
	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	private final void highlightSearchString(final boolean highlight) {
		findString.setForeground(highlight ? Color.RED : ordinalColor);
		replace.setEnabled(!highlight);
		replaceAll.setEnabled(!highlight);
	}
	
	private void fillLocalizedStrings() {
		findCaption.setText(localizer.getValue(KEY_FIND_CAPTION));
		replaceCaption.setText(localizer.getValue(KEY_REPLACE_CAPTION));
		ignoreCase.setText(localizer.getValue(KEY_IGNORE_CASE));
		ignoreCase.setToolTipText(localizer.getValue(KEY_IGNORE_CASE_TT));
		wholeWords.setText(localizer.getValue(KEY_WHOLE_WORDS));
		wholeWords.setToolTipText(localizer.getValue(KEY_WHOLE_WORDS_TT));
		useRegex.setText(localizer.getValue(KEY_USE_REGEX));
		useRegex.setToolTipText(localizer.getValue(KEY_USE_REGEX_TT));
		backward.setText(localizer.getValue(KEY_BACKWARD));
		backward.setToolTipText(localizer.getValue(KEY_BACKWARD_TT));
		find.setText(localizer.getValue(KEY_FIND));
		find.setToolTipText(localizer.getValue(KEY_FIND_TT));
		replace.setText(localizer.getValue(KEY_REPLACE));
		replace.setToolTipText(localizer.getValue(KEY_REPLACE_TT));
		replaceAll.setText(localizer.getValue(KEY_REPLACE_ALL));
		replaceAll.setToolTipText(localizer.getValue(KEY_REPLACE_ALL_TT));
		close.setToolTipText(localizer.getValue(KEY_CLOSE_TT));
	}
	
}
