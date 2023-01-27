package chav1961.csce.swing;

import java.net.URI;
import java.util.Locale;
import java.util.Set;

import javax.swing.JTextPane;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;

public class ScreenLogger extends JTextPane implements LoggerFacade, LocaleChangeListener {
	private static final long serialVersionUID = 1L;

	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean canServe(URI resource) throws NullPointerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LoggerFacade newInstance(URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoggerFacade message(Severity level, String format, Object... parameters) throws NullPointerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoggerFacade message(Severity level, LoggerCallbackInterface callback) throws NullPointerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoggerFacade message(Severity level, Throwable exception, String format, Object... parameters) throws NullPointerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoggerFacade message(Severity level, Throwable exception, LoggerCallbackInterface callback) throws NullPointerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLoggedNow(Severity level) throws NullPointerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<Reducing> getReducing() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoggerFacade setReducing(Set<Reducing> reducing) throws NullPointerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoggerFacade pushReducing(Set<Reducing> reducing) throws NullPointerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoggerFacade popReducing() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoggerFacade transaction(String mark) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoggerFacade transaction(String mark, Class<?> root) throws NullPointerException, IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void rollback() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
