package chav1961.csce.swing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.ui.swing.SwingUtils;

class ProjectVariablesTableEditor extends JTable implements LocaleChangeListener {
	private static final long serialVersionUID = 3429107053592752877L;
	private static final String				SUBST_PREFIX = "subst.";
	private static final Pattern			SUBST_PATTERN = Pattern.compile("subst\\..*");
	private static final FieldFormat		CELL_FORMAT = new FieldFormat(String.class, "30ms"); 

	private final Localizer					localizer;
	private final SubstitutableProperties	props = new SubstitutableProperties();
	private final InnerTableModel			model;
	
	public ProjectVariablesTableEditor(final Localizer localizer, final SubstitutableProperties props) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (props == null) {
			throw new NullPointerException("Project proerties can't be null");
		}
		else {
			this.localizer = localizer;
			
			for(String key : props.availableKeys(SUBST_PATTERN)) {
				this.props.setProperty(key.substring(key.indexOf('.')+1), props.getPropertyAsIs(key));
			}
			setModel(this.model = new InnerTableModel(this.props));
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			setRowSelectionAllowed(true);
			setRowHeight(20);
			setDefaultRenderer(String.class, SwingUtils.getCellRenderer(String.class, CELL_FORMAT, TableCellRenderer.class));
			setDefaultEditor(String.class, SwingUtils.getCellEditor(String.class, CELL_FORMAT, TableCellEditor.class));

			SwingUtils.assignActionKey(this, SwingUtils.KS_INSERT, (e)->insert(), SwingUtils.ACTION_INSERT);
			SwingUtils.assignActionKey(this, SwingUtils.KS_DELETE, (e)->delete(), SwingUtils.ACTION_DELETE);
			
			final TableRowSorter<InnerTableModel> sorter = new TableRowSorter<>(model);

            sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(0, SortOrder.ASCENDING)));			
			setRowSorter(sorter);
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		model.fireTableStructureChanged();		
	}

	public void insert() {
		model.insert();
	}
	
	public void delete() {
		if (!getSelectionModel().isSelectionEmpty()) {
			model.delete(getRowSorter().convertRowIndexToModel(getSelectedRow()));
		}
	}	
	
	public void storeProperties(final SubstitutableProperties props) throws PrintingException {
		if (props == null) {
			throw new NullPointerException("Propertis to set can't be null");
		}
		else {
			props.remove(SUBST_PATTERN);
			for (String[] item : model.content) {
				props.setProperty(SUBST_PREFIX+item[0], item[1]);
			}
		}
	}

	private class InnerTableModel extends DefaultTableModel {
		private static final long serialVersionUID = 81179900913418996L;
		
		private static final String		COL_NAME = "ProjectVariablesTableEditor.InnerTableModel.column.name";
		private static final String		COL_VALUE = "ProjectVariablesTableEditor.InnerTableModel.column.value";
		private static final String		DEFAULT_COL_NAME = "newKey";
		private static final String		DEFAULT_COL_VALUE = "newValue";
		
		private final List<String[]>	content = new ArrayList<>();
		
		public InnerTableModel(final SubstitutableProperties props) {
			for(Entry<Object, Object> entity : props.entrySet()) {
				content.add(new String[] {entity.getKey().toString(), entity.getValue().toString()});
			}
		}
		
		@Override
		public int getRowCount() {
			return content == null ? 0 : content.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(final int columnIndex) {
			switch (columnIndex) {
				case 0 	: return localizer.getValue(COL_NAME);
				case 1	: return localizer.getValue(COL_VALUE);
				default : throw new UnsupportedOperationException();
			}
		}

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			return String.class;
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			return true;
		}

		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			return content.get(rowIndex)[columnIndex];
		}

		@Override
		public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
			content.get(rowIndex)[columnIndex] = aValue.toString();
			fireTableCellUpdated(rowIndex, columnIndex);
		}
		
		private void insert() {
			content.add(new String[]{DEFAULT_COL_NAME+(content.size()+1), DEFAULT_COL_VALUE});
			fireTableRowsInserted(content.size()-1, content.size()-1);
		}
		
		private void delete(final int rowNumer) {
			content.remove(rowNumer);
			fireTableRowsDeleted(rowNumer, rowNumer);
		}
	}
}
