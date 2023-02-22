package chav1961.csce.utils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map.Entry;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.MutableLocalizedString;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.json.JsonNode;
import chav1961.purelib.json.JsonUtils;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public class SimpleLocalizedString implements MutableLocalizedString {
	private String		key;
	
	private EnumMap<SupportedLanguages, String>	values = new EnumMap<>(SupportedLanguages.class);
	
	public SimpleLocalizedString(final String key, final String value) throws SyntaxException {
		if (Utils.checkEmptyOrNullString(key)) {
			throw new IllegalArgumentException("String key can't be null or empty");
		}
		else if (Utils.checkEmptyOrNullString(value)) {
			throw new IllegalArgumentException("String value can't be null or empty");
		}
		else {
			this.key = key;
			
			try(final Reader			rdr = new StringReader(value);
				final JsonStaxParser	parser = new JsonStaxParser(rdr)) {
				
				parser.next();				
				for(JsonNode item : JsonUtils.loadJsonTree(parser).children()) {
					values.put(SupportedLanguages.valueOf(item.getName()), item.getStringValue());
				}
			} catch (IOException e) {
				throw new SyntaxException(0, 0, e.getLocalizedMessage(), e);
			}
		}
	}

	private SimpleLocalizedString(final SimpleLocalizedString another) {
		this.key = another.key;
		this.values.putAll(another.values);
	}
	
	public String toStringValue() throws PrintingException {
		try(final Writer			wr = new StringWriter();
			final JsonStaxPrinter 	prn = new JsonStaxPrinter(wr)) {
			boolean		theSameFirst = true;
			
			prn.startObject();
			for (Entry<SupportedLanguages, String> item : values.entrySet()) {
				if (theSameFirst) {
					theSameFirst = false;
				}
				else {
					prn.splitter();
				}
				prn.name(item.getKey().name()).value(item.getValue());
			}
			prn.endObject();
			
			prn.flush();
			return wr.toString();
		} catch (IOException e) {
			throw new PrintingException(e.getLocalizedMessage(), e); 
		}
	}

	@Override
	public String getId() throws LocalizationException {
		return key;
	}

	@Override
	public String getValue() throws LocalizationException {
		return getValue(getLocalizer().currentLocale().getLocale());
	}

	@Override
	public String getValue(final Locale lang) throws LocalizationException {
		if (lang == null) {
			throw new NullPointerException("Locale to add value for cant be null");
		}
		else {
			return values.get(SupportedLanguages.of(lang)); 
		}
	}

	@Override
	public String getValueOrDefault(final Locale lang) throws LocalizationException {
		return getValue(lang);
	}

	@Override
	public boolean isLanguageSupported(final Locale lang) throws LocalizationException {
		if (lang == null) {
			throw new NullPointerException("Locale to add value for cant be null");
		}
		else {
			return values.containsKey(SupportedLanguages.of(lang));
		}
	}

	@Override
	public Localizer getLocalizer() {
		return PureLibSettings.PURELIB_LOCALIZER;
	}

	@Override
	public void setId(final String id) throws LocalizationException {
		if (Utils.checkEmptyOrNullString(id)) {
			throw new IllegalArgumentException("String id to set can't be null or empty");
		}
		else {
			this.key = id;
		}
	}

	@Override
	public void addValue(final Locale lang, final String value) throws LocalizationException {
		if (lang == null) {
			throw new NullPointerException("Locale to add value for cant be null");
		}
		else if (Utils.checkEmptyOrNullString(value)) {
			throw new IllegalArgumentException("String id to set can't be null or empty");
		}
		else {
			values.putIfAbsent(SupportedLanguages.of(lang), value);
		}
	}

	@Override
	public void setValue(final Locale lang, final String value) throws LocalizationException {
		if (lang == null) {
			throw new NullPointerException("Locale to set value for cant be null");
		}
		else if (Utils.checkEmptyOrNullString(value)) {
			throw new IllegalArgumentException("String id to set can't be null or empty");
		}
		else {
			values.put(SupportedLanguages.of(lang), value);
		}
	}

	@Override
	public void removeValue(final Locale lang) throws LocalizationException {
		if (lang == null) {
			throw new NullPointerException("Locale to set value for cant be null");
		}
		else {
			values.remove(SupportedLanguages.of(lang));
		}
	}

	@Override
	public  Object clone() throws CloneNotSupportedException {
		return new SimpleLocalizedString(this);
	}

	@Override
	public String toString() {
		return "SimpleLocalizedString [key=" + key + ", values=" + values + "]";
	}
}
