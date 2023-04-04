package chav1961.csce.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.useful.JCreoleEditor;

class FindPanel extends JPanel implements LocaleChangeListener {
	private static final long serialVersionUID = -2811483115549162659L;
	
	private static final String	KEY_FIND_CAPTION = "FindPanel.find.caption";
	private static final String	KEY_IGNORE_CASE = "FindPanel.ignore.case";
	private static final String	KEY_IGNORE_CASE_TT = "FindPanel.ignore.case.tt";
	private static final String	KEY_WHOLE_WORDS = "FindPanel.whole.words";
	private static final String	KEY_WHOLE_WORDS_TT = "FindPanel.whole.words.tt";
	private static final String	KEY_USE_REGEX = "FindPanel.use.regex";
	private static final String	KEY_USE_REGEX_TT = "FindPanel.use.regex.tt";
	private static final String	KEY_FORWARD = "FindPanel.forward";
	private static final String	KEY_FORWARD_TT = "FindPanel.forward.tt";
	private static final String	KEY_BACKWARD = "FindPanel.backward";
	private static final String	KEY_BACKWARD_TT = "FindPanel.backward.tt";
	private static final String	KEY_CLOSE_TT = "FindPanel.close";
	
	private static final Icon	ICON_CLOSE = new ImageIcon(FindPanel.class.getResource("icon_close_16.png"));

	private final Localizer		localizer;
	private final JCreoleEditor	editor;
	private final Consumer<FindPanel>	onClose;
	private final JLabel		findCaption = new JLabel();
	private final JTextField	findString = new JTextField();
	private final Color			ordinalColor = findString.getForeground();
	private final JCheckBox		ignoreCase = new JCheckBox();
	private final JCheckBox		wholeWords = new JCheckBox();
	private final JCheckBox		useRegex = new JCheckBox();
	private final JButton		forward = new JButton();
	private final JButton		backward = new JButton();
	private final JButton		close = new JButton();
	
	FindPanel(final Localizer localizer, final JCreoleEditor editor, final Consumer<FindPanel> onClose) {
		this.localizer = localizer;
		this.editor = editor;
		this.onClose = onClose;
		
		forward.addActionListener((e)->searchForward());
		backward.addActionListener((e)->searchBackward());
		close.setIcon(ICON_CLOSE);
		close.setPreferredSize(new Dimension(20,20));
		close.addActionListener((e)->onClose.accept(FindPanel.this));
		
		final JPanel		innerContent = new JPanel();
		final JPanel		innerClose = new JPanel();
		final GroupLayout	layout = new GroupLayout(innerContent);
		 
		innerContent.setLayout(layout);
		layout.setHorizontalGroup(
				   layout.createSequentialGroup()
		           		.addComponent(findCaption)
		           		.addComponent(findString)
		           		.addComponent(backward)
		           		.addComponent(forward)
		           		.addComponent(ignoreCase)
		           		.addComponent(wholeWords)
		           		.addComponent(useRegex)
		);
		layout.setVerticalGroup(
				   layout.createSequentialGroup()
				      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				           .addComponent(findCaption)
				           .addComponent(findString)
				           .addComponent(backward)
				           .addComponent(forward)
				           .addComponent(ignoreCase)
				           .addComponent(wholeWords)
				           .addComponent(useRegex)
				           )
		);
		innerClose.add(close);
		setLayout(new BorderLayout(10,0));
		add(innerContent, BorderLayout.CENTER);
		add(innerClose, BorderLayout.EAST);
		
		fillLocalizedStrings();
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	@Override
	public boolean requestFocusInWindow() {
		return findString.requestFocusInWindow();
	}
	
	public void searchForward() {
		highlightSearchString(search(editor,findString.getText(),true,ignoreCase.isSelected(),wholeWords.isSelected(),useRegex.isSelected()));
	}

	public void searchBackward() {
		highlightSearchString(search(editor,findString.getText(),false,ignoreCase.isSelected(),wholeWords.isSelected(),useRegex.isSelected()));
	}
	
	private void highlightSearchString(final boolean highlight) {
		findString.setForeground(highlight ? ordinalColor : Color.RED);
	}
	
	private void fillLocalizedStrings() {
		findCaption.setText(localizer.getValue(KEY_FIND_CAPTION));
		ignoreCase.setText(localizer.getValue(KEY_IGNORE_CASE));
		ignoreCase.setToolTipText(localizer.getValue(KEY_IGNORE_CASE_TT));
		wholeWords.setText(localizer.getValue(KEY_WHOLE_WORDS));
		wholeWords.setToolTipText(localizer.getValue(KEY_WHOLE_WORDS_TT));
		useRegex.setText(localizer.getValue(KEY_USE_REGEX));
		useRegex.setToolTipText(localizer.getValue(KEY_USE_REGEX_TT));
		forward.setText(localizer.getValue(KEY_FORWARD));
		forward.setToolTipText(localizer.getValue(KEY_FORWARD_TT));
		backward.setText(localizer.getValue(KEY_BACKWARD));
		backward.setToolTipText(localizer.getValue(KEY_BACKWARD_TT));
		close.setToolTipText(localizer.getValue(KEY_CLOSE_TT));
	}

	static boolean search(final JCreoleEditor editor, final String text, boolean forward, final boolean ignoreCase, final boolean wholeWords, final boolean useRegex) {
		try{final Pattern	p = Pattern.compile(useRegex ? text : "\\Q"+text+"\\E", Pattern.DOTALL | (ignoreCase ? Pattern.CASE_INSENSITIVE : 0));
			final int		pos = editor.getCaretPosition();
			final int		posMin = Math.min(editor.getSelectionStart(), editor.getSelectionEnd());
			final int		posMax = Math.max(editor.getSelectionStart(), editor.getSelectionEnd());
			final String	content = forward ? editor.getText().substring(posMax) : (posMin > 0 ? editor.getText().substring(0, posMin) : "");
			final Matcher	m = p.matcher(content); 
			
			if (content.isEmpty()) {
				return false;
			}
			else if (forward) {
				if (m.find()) {
					editor.setSelectionStart(posMax + m.start());
					editor.setSelectionEnd(posMax + m.end());
					return true;
				}
				else {
					editor.setCaretPosition(pos);
					return false;
				}
			}
			else {
				int	from = -1, to = 0;
				
				while (m.find(from + 1)) {
					from = m.start();
					to = m.end();
				}
				if (from >= 0) {
					editor.setSelectionStart(from);
					editor.setSelectionEnd(to);
					return true;
				}
				else {
					editor.setCaretPosition(pos);
					return false;
				}
			}
		} catch (PatternSyntaxException exc) {
			SwingUtils.getNearestLogger(editor).message(Severity.warning, exc.getLocalizedMessage());
			return false;
		} finally {
			editor.requestFocusInWindow();
		}
	}
}
