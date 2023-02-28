package chav1961.csce.swing;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.DefaultCaret;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;

public class ScreenLogger extends JTextPane implements LoggerFacade, LocaleChangeListener {
	private static final long serialVersionUID = 1L;

	private final List<MessageDesc>	messages = new ArrayList<>();
	
	public ScreenLogger() {
		setContentType("text/html");
		setEditable(false);
		((DefaultCaret)getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		addHyperlinkListener((e)->processHyperlink(e));
	}
	

	public void clear() {
		messages.clear();
		refresh();
	}
	
	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
	}
	
	@Override
	public boolean canServe(URI resource) throws NullPointerException {
		return false;
	}

	@Override
	public LoggerFacade newInstance(URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		return this;
	}

	@Override
	public LoggerFacade message(final Severity level, final String format, final Object... parameters) throws NullPointerException {
		printMessage(level, null, String.format(format, parameters));
		return this;
	}

	@Override
	public LoggerFacade message(final Severity level, final LoggerCallbackInterface callback) throws NullPointerException {
		printMessage(level, null, callback.process());
		return this;
	}

	@Override
	public LoggerFacade message(final Severity level, final Throwable exception, final String format, final Object... parameters) throws NullPointerException {
		printMessage(level, exception, String.format(format, parameters));
		return this;
	}

	@Override
	public LoggerFacade message(final Severity level, final Throwable exception, final LoggerCallbackInterface callback) throws NullPointerException {
		printMessage(level, exception, callback.process());
		return this;
	}

	@Override
	public boolean isLoggedNow(Severity level) throws NullPointerException {
		return true;
	}

	@Override
	public Set<Reducing> getReducing() {
		return Set.of();
	}

	@Override
	public LoggerFacade setReducing(Set<Reducing> reducing) throws NullPointerException {
		return this;
	}

	@Override
	public LoggerFacade pushReducing(final Set<Reducing> reducing) throws NullPointerException {
		return this;
	}

	@Override
	public LoggerFacade popReducing() {
		return this;
	}

	@Override
	public LoggerFacade transaction(String mark) throws IllegalArgumentException {
		return this;
	}

	@Override
	public LoggerFacade transaction(String mark, Class<?> root) throws NullPointerException, IllegalArgumentException {
		return this;
	}

	@Override
	public void rollback() {
	}

	@Override
	public void close() {
	}

	protected void processHyperlink(final HyperlinkEvent e) {
	}
	
	private void printMessage(final Severity severity, final Throwable t, final String message) {
		messages.add(new MessageDesc(severity, t, message));
		refresh();
	}

	private void refresh() {
		final StringBuilder	sb = new StringBuilder("<html><body>");
		
		for (MessageDesc item : messages) {
			switch (item.severity) {
				case error		:
					sb.append("<p><font color=red>"+item.message+"</p>");
					break;
				case note		:
					sb.append("<p><font color=green>"+item.message+"</p>");
					break;
				case warning	:
					sb.append("<p><font color=blue>"+item.message+"</p>");
					break;
				case severe		:
					sb.append("<p><font color=red><b>"+item.message+"</b></p>");
					break;
				case info: case debug: case tooltip: case trace:
					sb.append("<p><font color=black>"+item.message+"</p>");
					break;
				default:
					throw new UnsupportedOperationException("Message severity ["+item.severity+"] is not supported yet");
			}
		}
		setText(sb.append("</body></html>").toString());
		
	}
	
	private static class MessageDesc {
		private final Severity	severity;
		private final Throwable	t;
		private final String	message;
		
		public MessageDesc(Severity severity, Throwable t, String message) {
			this.severity = severity;
			this.t = t;
			this.message = message;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((message == null) ? 0 : message.hashCode());
			result = prime * result + ((severity == null) ? 0 : severity.hashCode());
			result = prime * result + ((t == null) ? 0 : t.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			MessageDesc other = (MessageDesc) obj;
			if (message == null) {
				if (other.message != null) return false;
			} else if (!message.equals(other.message)) return false;
			if (severity != other.severity) return false;
			if (t == null) {
				if (other.t != null) return false;
			} else if (!t.equals(other.t)) return false;
			return true;
		}

		@Override
		public String toString() {
			return "MessageDesc [severity=" + severity + ", t=" + t + ", message=" + message + "]";
		}
	}
}
