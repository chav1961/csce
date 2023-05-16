package chav1961.csce;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.i18n.interfaces.DefaultLocalizerProvider;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;

public class DefaultLocalizerRepo implements DefaultLocalizerProvider {
	private final Localizer	localizer;
	
	public DefaultLocalizerRepo() {
		try(final InputStream	is = this.getClass().getResourceAsStream("application.xml")) {
			final ContentMetadataInterface	mdi = ContentModelFactory.forXmlDescription(is);
			
			this.localizer = Localizer.Factory.newInstance(mdi.getRoot().getLocalizerAssociated());
		} catch (IOException e) {
			throw new PreparationException(e.getLocalizedMessage(), e); 
		}		
	}

	@Override
	public Localizer getLocalizer() {
		return localizer;
	}

	@Override
	public boolean canServe(final URI resource) {
		if (resource == null) {
			throw new NullPointerException("Resource URI can't be null");
		}
		else {
			return URIUtils.canServeURI(resource, resource) || URIUtils.canServeURI(resource, resource); 
		}
	}

	@Override
	public DefaultLocalizerProvider newInstance(final URI resource) throws EnvironmentException {
		if (canServe(resource)) {
			return this;
		}
		else {
			throw new EnvironmentException("URI ["+resource+"] does not supported yet");
		}
	}

	@Override
	public Module getModule() {
		return getClass().getModule();
	}
}
