package chav1961.csce.swing;

import java.util.Locale;
import java.util.Map.Entry;

import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;

public class ProjectPropertiesTable extends JTable implements LocaleChangeListener {
	private static final long serialVersionUID = 7062587052136890314L;

	public ProjectPropertiesTable(final Localizer localizer, final SubstitutableProperties props, final String... excludes) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (props == null) {
			throw new NullPointerException("Project properties can't be null"); 
		}
		else {
			setModel(new InnerTableModel(localizer, props, excludes));
		}
	}

	public void storeProperties(final SubstitutableProperties props) {
		if (props == null) {
			throw new NullPointerException("Properties to store can't be null");
		}
		else {
			((InnerTableModel)getModel()).storeProperties(props);
		}
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		((InnerTableModel)getModel()).fireTableStructureChanged();
	}
	
	private static class InnerTableModel extends DefaultTableModel {
		private static final long serialVersionUID = 5478266226637197500L;
		
		private final Localizer					localizer;
		private final SubstitutableProperties	props;
		private final Object[][]				content;
		
		private InnerTableModel(final Localizer localizer, final SubstitutableProperties props, final String... excludes) {
			this.localizer = localizer;
			this.props = props;
			this.content = new Object[props.size() - excludes.length][];
			
			int index = 0;
			
loop:		for(Entry<Object, Object> item : props.entrySet()) {
				for (String name : excludes) {
					if (name.equals(item.getKey())) {
						continue loop;
					}
				}
				content[index] = new Object[SupportedLanguages.values().length + 1];
				content[index][0] = item.getKey();
				
				for(SupportedLanguages lang : SupportedLanguages.values()) {
					content[index][lang.ordinal()+1] = item.getValue();
				}
				index++;
			}
		}

		@Override
		public int getRowCount() {
			return props == null ? 0 : content.length;
		}

		@Override
		public int getColumnCount() {
			return SupportedLanguages.values().length + 1;
		}

		@Override
		public String getColumnName(final int columnIndex) {
			if (columnIndex == 0) {
				return localizer.getValue("parameters");
			}
			else {
				return SupportedLanguages.values()[columnIndex-1].name();
			}
		}

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			return String.class;
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			return columnIndex > 0;
		}

		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			return content[rowIndex][columnIndex];
		}

		@Override
		public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
			content[rowIndex][columnIndex] = aValue;
			fireTableCellUpdated(rowIndex, columnIndex);
		}

		private void storeProperties(final SubstitutableProperties props) {
			for(Object[] item : content) {
				props.setProperty(item[0].toString(), item[1].toString());
			}
		}
	}
}
