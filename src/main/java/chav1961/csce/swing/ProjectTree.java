package chav1961.csce.swing;


import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import chav1961.csce.Application;
import chav1961.csce.project.ProjectChangeEvent;
import chav1961.csce.project.ProjectContainer;
import chav1961.csce.project.ProjectNavigator;
import chav1961.csce.project.ProjectNavigator.ItemType;
import chav1961.csce.project.ProjectNavigator.ProjectNavigatorItem;
import chav1961.csce.utils.SearchUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.swing.SwingUtils;

public class ProjectTree extends JTree implements LocalizerOwner, LocaleChangeListener, DropTargetListener, DragSourceListener, DragGestureListener {
	private static final long serialVersionUID = 1L;

    private static final DataFlavor			TREE_ITEM;
    
    static {
        try {
			TREE_ITEM = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + ProjectItemTreeNode[].class.getName() + "\"");
		} catch (ClassNotFoundException e) {
			throw new PreparationException(e);
		}
    }
	
    private final DropTarget 				dropTarget = new DropTarget(this, this);
    private final DragSource 				dragSource = DragSource.getDefaultDragSource();	
    private final ProjectViewer				viewer;
	private final ProjectContainer			project;
	private final ContentMetadataInterface	mdi;
	private ProjectItemTreeNode				rootNode;
	final JPopupMenu						popup; 
	
	public ProjectTree(final ProjectViewer viewer, final ProjectContainer project, final ContentMetadataInterface mdi) {
		super();
		if (viewer == null) {
			throw new NullPointerException("Project viewer can't be null"); 
		}
		else if (project == null) {
			throw new NullPointerException("Project container can't be null"); 
		}
		else {
			this.viewer = viewer;
			this.project = project;
			this.mdi = mdi;
			this.popup = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.treemenu")), JPopupMenu.class);
			this.rootNode = buildTree(project.getProjectNavigator());
			
			setDropMode(DropMode.ON);
			setDropTarget(new DropTarget() {
				private static final long serialVersionUID = 1L;
				
				@Override
				public synchronized void drop(final DropTargetDropEvent dtde) {
					final TreePath	path = getPathForLocation(dtde.getLocation().x, dtde.getLocation().y);
					
					if (path != null) {
						final ProjectItemTreeNode	pitn = (ProjectItemTreeNode)path.getLastPathComponent();
						
						for (DataFlavor flavor : dtde.getCurrentDataFlavors()) {
							if (flavor.equals(DataFlavor.javaFileListFlavor) && (pitn.getUserObject().type == ItemType.Subtree || pitn.getUserObject().type == ItemType.Root)) {
								dtde.acceptDrop(DnDConstants.ACTION_COPY);
								
								try{final List<File> 	files = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
		
									 for (File item : files) {
										 if (Application.PDF_FILTER.accept(item) || Application.DJVU_FILTER.accept(item)) { 
											 project.addProjectPart(pitn.getUserObject().id, ItemType.DocumentRef, item);
										 }
										 else if (Application.IMAGE_FILTER.accept(item)) { 
											 project.addProjectPart(pitn.getUserObject().id, ItemType.ImageRef, item);
										 }
									 }
									return;
								} catch (IOException | UnsupportedFlavorException e) {
									SwingUtils.getNearestLogger(ProjectTree.this).message(Severity.error, e, e.getLocalizedMessage());
								}
							}
							else if (flavor.equals(TREE_ITEM)) {
								System.err.println("ITEM");
	//							dtde.acceptDrop(DnDConstants.ACTION_MOVE);
								dtde.rejectDrop();
								return;
							}
						}
						dtde.rejectDrop();
					}
					else {
						dtde.rejectDrop();
					}
				}
			});
			
			
//			setDragEnabled(true);
//			dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
//		    setTransferHandler(new TreeTransferHandler());			
			
			SwingUtils.assignActionListeners(popup, project.getApplication());
			SwingUtils.assignActionKey(this, SwingUtils.KS_CONTEXTMENU, (e)->{
				if (!isSelectionEmpty()) {
					showMenu(getSelectionPoint());
				}
			}, SwingUtils.ACTION_CONTEXTMENU);
			SwingUtils.assignActionKey(this, SwingUtils.KS_DELETE, (e)->{
				if (!isSelectionEmpty()) {
					project.getApplication().deleteItem();
				}
			}, SwingUtils.ACTION_DELETE);
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON3) {
						showMenu(e.getPoint());
					}
					else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2) {
						openTab(e.getPoint());
					}
					else {
						super.mouseClicked(e);
					}
				}
			});
			getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			
			setModel(new DefaultTreeModel(this.rootNode));
			
			setCellRenderer(SwingUtils.getCellRenderer(ProjectNavigatorItem.class, new FieldFormat(ProjectNavigatorItem.class), TreeCellRenderer.class));
		}
	}

	@Override
	public Localizer getLocalizer() {
		return project.getLocalizer();
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		SwingUtils.refreshLocale(popup, oldLocale, newLocale);
		walkAndRefresh(rootNode);
	}

	@Override
	public void dragEnter(final DropTargetDragEvent dtde) {
		// TODO Auto-generated method stub
		dtde.acceptDrag (DnDConstants.ACTION_COPY_OR_MOVE);		
	}

	@Override
	public void dragOver(final DropTargetDragEvent dtde) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dropActionChanged(final DropTargetDragEvent dtde) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dragExit(final DropTargetEvent dte) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drop(final DropTargetDropEvent dropTargetDropEvent) {
		// TODO Auto-generated method stub
		try
        {
            Transferable tr = dropTargetDropEvent.getTransferable();
            if (tr.isDataFlavorSupported (DataFlavor.javaFileListFlavor))
            {
                dropTargetDropEvent.acceptDrop (
                    DnDConstants.ACTION_COPY_OR_MOVE);
                java.util.List fileList = (java.util.List)
                    tr.getTransferData(DataFlavor.javaFileListFlavor);
                Iterator iterator = fileList.iterator();
                while (iterator.hasNext())
                {
                  File file = (File)iterator.next();
                  Hashtable hashtable = new Hashtable();
                  hashtable.put("name",file.getName());
                  hashtable.put("url",file.toURL().toString());
                  hashtable.put("path",file.getAbsolutePath());
  //                ((DefaultListModel)getModel()).addElement(hashtable);
                }
                dropTargetDropEvent.getDropTargetContext().dropComplete(true);
          } else {
            System.err.println ("Rejected");
            dropTargetDropEvent.rejectDrop();
          }
        } catch (IOException io) {
            io.printStackTrace();
            dropTargetDropEvent.rejectDrop();
        } catch (UnsupportedFlavorException ufe) {
            ufe.printStackTrace();
            dropTargetDropEvent.rejectDrop();
        }	}
	
	@Override
	public void dragEnter(final DragSourceDragEvent dsde) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dragOver(final DragSourceDragEvent dsde) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dropActionChanged(final DragSourceDragEvent dsde) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dragExit(final DragSourceEvent dse) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dragDropEnd(final DragSourceDropEvent dsde) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dragGestureRecognized(DragGestureEvent dge) {
		// TODO Auto-generated method stub
//		if (getSelectedIndex() == -1)
//	        return;
//	    Object obj = getSelectedValue();
//	    if (obj == null) {
//	        // Nothing selected, nothing to drag
//	        System.out.println ("Nothing selected - beep");
//	        getToolkit().beep();
//	    } else {
//	        Hashtable table = (Hashtable)obj;
//	        FileSelection transferable =
//	          new FileSelection(new File((String)table.get("path")));
//	        dragGestureEvent.startDrag(
//	          DragSource.DefaultCopyDrop,
//	          transferable,
//	          this);
//	    }	
    }
	
	public void refreshTree(final ProjectChangeEvent event) {
		switch (event.getChangeType()) {
			case PART_INSERTED : case ITEM_INSERTED :
				final TreePath 				parentPathInserted = long2TreePath((long)event.getParameters()[0]);
				final ProjectItemTreeNode	parentNodeInserted = ((ProjectItemTreeNode)parentPathInserted.getLastPathComponent());
				final ProjectItemTreeNode	childInserted = new ProjectItemTreeNode(project.getProjectNavigator().getItem((long)event.getParameters()[1]));

				((DefaultTreeModel)getModel()).insertNodeInto(childInserted, parentNodeInserted, parentNodeInserted.getChildCount());
				getSelectionModel().setSelectionPath(parentPathInserted.pathByAddingChild(childInserted));
				break;
			case PART_REMOVED : case ITEM_REMOVED :
				final TreePath 				pathRemoved = long2TreePath((long)event.getParameters()[1]);
				final ProjectItemTreeNode	nodeRemoved = ((ProjectItemTreeNode)pathRemoved.getLastPathComponent());

				((DefaultTreeModel)getModel()).removeNodeFromParent(nodeRemoved);
				getSelectionModel().setSelectionPath(pathRemoved.getParentPath());
				break;
			case PART_CHANGED : case ITEM_CHANGED :
				final TreePath 				pathChanged = long2TreePath((long)event.getParameters()[1]);
				final ProjectItemTreeNode	nodeChanged = ((ProjectItemTreeNode)pathChanged.getLastPathComponent());
				final ProjectNavigatorItem 	itemChanged = project.getProjectNavigator().getItem((long)event.getParameters()[1]);

				nodeChanged.setUserObject(itemChanged);
				((DefaultTreeModel)getModel()).reload(nodeChanged);
				break;
			case PART_CONTENT_CHANGED : case ITEM_CONTENT_CHANGED :
				break;
			case PROJECT_LOADED :
				((DefaultTreeModel)getModel()).reload((ProjectItemTreeNode)getModel().getRoot());
				break;
			case PROJECT_FILENAME_CHANGED	:
				break;
			default :
				throw new UnsupportedOperationException("Change type ["+event.getChangeType()+"] is not supprted yet");
		}
	}	

	
	private TreePath long2TreePath(final long id) {
		final TreePath[]	path = new TreePath[] {null};
		
		walkAndProcess((ProjectItemTreeNode)getModel().getRoot(), (node)->{
			if (node.getUserObject().id == id) {
				path[0] = new TreePath(node.getPath());
			}
		});
		return path[0];
	}

	private void walkAndProcess(final ProjectItemTreeNode node, final Consumer<ProjectItemTreeNode> consumer) {
		for (int index = 0; index < node.getChildCount(); index++) {
			walkAndProcess((ProjectItemTreeNode)node.getChildAt(index), consumer);
		}
		consumer.accept(node);
	}
	
	
	private void walkAndRefresh(final ProjectItemTreeNode node) {
		for (int index = 0; index < node.getChildCount(); index++) {
			walkAndRefresh((ProjectItemTreeNode)node.getChildAt(index));
		}
		((DefaultTreeModel)getModel()).nodeChanged(node);
	}

	private Point getSelectionPoint() {
		final Rectangle	rect = getRowBounds(getRowForPath(getSelectionPath()));
		
		return new Point((int)rect.getCenterX(), (int)rect.getCenterX());
	}
	
	private void showMenu(final Point p) {
		if (!isSelectionEmpty()) {
			final TreePath	path = getClosestPathForLocation(p.x, p.y);
			
			if (path != null) {
				final ProjectItemTreeNode	pitn = (ProjectItemTreeNode)path.getLastPathComponent();
				final int					row = getRowForPath(path);
				final StringBuilder			sb = new StringBuilder();
				
				for (Object item : path.getPath()) {
					if (((ProjectItemTreeNode)item).getUserObject().type == ItemType.Subtree) {
						sb.append('/').append(((ProjectItemTreeNode)item).getUserObject().name);
					}
				}
				
				switch (pitn.getUserObject().type) {
					case CreoleRef		:
						final String	partName = project.getPartNameById(pitn.getUserObject().id);
						final String	content = project.getProjectPartContent(partName);
						final boolean	linksPresent = fillCreoleLinks((sb.isEmpty() ? "" : sb.append('/').toString()) + partName, content, (JMenu)SwingUtils.findComponentByName(popup, "treemenu.copyLinkList"));
						
						SwingUtils.findComponentByName(popup, "treemenu.copyLinkList").setVisible(true);
						SwingUtils.findComponentByName(popup, "treemenu.copyLinkList").setEnabled(linksPresent);
						SwingUtils.findComponentByName(popup, "treemenu.copyLink").setVisible(false);
						break;
					case DocumentRef	:
						SwingUtils.findComponentByName(popup, "treemenu.copyLinkList").setVisible(false);
						SwingUtils.findComponentByName(popup, "treemenu.copyLink").setVisible(true);
						break;
					case ImageRef		:
						SwingUtils.findComponentByName(popup, "treemenu.copyLinkList").setVisible(false);
						SwingUtils.findComponentByName(popup, "treemenu.copyLink").setVisible(true);
						break;
					case Root : case Subtree :
						SwingUtils.findComponentByName(popup, "treemenu.copyLinkList").setVisible(false);
						SwingUtils.findComponentByName(popup, "treemenu.copyLink").setVisible(false);
						break;
					default :
						throw new UnsupportedOperationException("Item type ["+pitn.getUserObject().type+"] is npt supported yet");
				}
				
				SwingUtils.findComponentByName(popup, "treemenu.properties").setEnabled(pitn.getUserObject().type.isEditingSupported());
				SwingUtils.findComponentByName(popup, "treemenu.delete").setEnabled(row != 0);
				popup.show(this, p.x, p.y);
			}
		}
	}

	private void openTab(final Point p) {
		if (!isSelectionEmpty()) {
			final TreePath	path = getClosestPathForLocation(p.x, p.y);
			
			if (path != null) {
				final ProjectItemTreeNode	pitn = (ProjectItemTreeNode)path.getLastPathComponent();

				switch (pitn.getUserObject().type) {
					case CreoleRef		:
						viewer.getProjectTabbedPane().openCreoleTab(pitn.getUserObject());
						break;
					case DocumentRef	:
						break;
					case ImageRef		:
						viewer.getProjectTabbedPane().openImageTab(pitn.getUserObject());
						break;
					case Root : case Subtree :
						break;
					default :
						throw new UnsupportedOperationException("Type ["+pitn.getUserObject().type+"] is not supported yet");
				}
			}
		}
	}

	private boolean fillCreoleLinks(final String partName, final String content, final JMenu menu) {
		boolean	found = false;
		
		menu.removeAll();
		if (!Utils.checkEmptyOrNullString(content)) {
			for (String item : SearchUtils.extractCreoleAnchors(content)) {
				final JMenuItem	mi = new JMenuItem(item.trim());
				
				menu.add(mi);
				mi.addActionListener((e)->((Application)SwingUtils.getNearestOwner(viewer, Application.class)).copyCreoleLink2Clipboard(partName, item));
				found = true;
			}
		}
		return found;
	}

	
	private static ProjectItemTreeNode buildTree(final ProjectNavigator navigator) {
		return buildTree(navigator, navigator.getRoot().id);
	}	
	
	private static ProjectItemTreeNode buildTree(final ProjectNavigator navigator, final long id) {
		final ProjectItemTreeNode	result = new ProjectItemTreeNode(navigator.getItem(id));
		
		for (ProjectNavigatorItem item : navigator.getChildren(id)) {
			result.add(buildTree(navigator, item.id));
		}
		return result;
	}
	
	static class ProjectItemTreeNode extends DefaultMutableTreeNode {
		private static final long serialVersionUID = 1L;

		public ProjectItemTreeNode(final ProjectNavigatorItem userObject) {
			this(userObject, !userObject.type.isLeafItem());
		}
		
		public ProjectItemTreeNode(final ProjectNavigatorItem userObject, boolean allowsChildren) {
			super(userObject, allowsChildren);
			if (userObject == null) {
				throw new NullPointerException("User object can't be null");
			}
		}

		@Override
		public ProjectNavigatorItem getUserObject() {
			return (ProjectNavigatorItem)super.getUserObject();
		}
		
		@Override
		public void setUserObject(final Object userObject) {
			if (!(userObject instanceof ProjectNavigatorItem)) {
				throw new IllegalArgumentException("User object no set can't be null and must be ProjectNavigatorItem instance"); 
			}
			else {
				super.setUserObject(userObject);
			}
		}
		
		@Override
		public boolean isLeaf() {
			return getUserObject().type.isLeafItem();
		}
	}

	private static class TreeTransferHandler extends TransferHandler {
		private static final long serialVersionUID = 1L;
		
		private final DataFlavor			nodesFlavor;
		private final DataFlavor[] 			flavors;
		private DefaultMutableTreeNode[] 	nodesToRemove;

	    public TreeTransferHandler() {
	        try {
	            nodesFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + ProjectItemTreeNode[].class.getName() + "\"");
	            flavors = new DataFlavor[]{nodesFlavor};
	        } catch(ClassNotFoundException e) {
	            throw new EnvironmentException(e);
	        }
	    }

	    public boolean canImport(TransferSupport support) {
	        if(!support.isDrop()) {
	            return false;
	        }
	        else {
		        support.setShowDropLocation(true);
		        if(!support.isDataFlavorSupported(nodesFlavor)) {
		            return false;
		        }
		        // Do not allow a drop on the drag source selections.
		        JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
		        JTree tree = (JTree)support.getComponent();
		        int dropRow = tree.getRowForPath(dl.getPath());
		        int[] selRows = tree.getSelectionRows();
		        for(int i = 0; i < selRows.length; i++) {
		            if(selRows[i] == dropRow) {
		                return false;
		            }
		        }
		        // Do not allow MOVE-action drops if a non-leaf node is
		        // selected unless all of its children are also selected.
		        int action = support.getDropAction();
		        if(action == MOVE) {
		            return haveCompleteNode(tree);
		        }
		        // Do not allow a non-leaf node to be copied to a level
		        // which is less than its source level.
		        TreePath dest = dl.getPath();
		        DefaultMutableTreeNode target =
		            (DefaultMutableTreeNode)dest.getLastPathComponent();
		        TreePath path = tree.getPathForRow(selRows[0]);
		        DefaultMutableTreeNode firstNode =
		            (DefaultMutableTreeNode)path.getLastPathComponent();
		        if(firstNode.getChildCount() > 0 &&
		               target.getLevel() < firstNode.getLevel()) {
		            return false;
		        }
		        return true;
	        }
	    }

	    private boolean haveCompleteNode(JTree tree) {
	        int[] selRows = tree.getSelectionRows();
	        TreePath path = tree.getPathForRow(selRows[0]);
	        DefaultMutableTreeNode first =
	            (DefaultMutableTreeNode)path.getLastPathComponent();
	        int childCount = first.getChildCount();
	        // first has children and no children are selected.
	        if(childCount > 0 && selRows.length == 1)
	            return false;
	        // first may have children.
	        for(int i = 1; i < selRows.length; i++) {
	            path = tree.getPathForRow(selRows[i]);
	            DefaultMutableTreeNode next =
	                (DefaultMutableTreeNode)path.getLastPathComponent();
	            if(first.isNodeChild(next)) {
	                // Found a child of first.
	                if(childCount > selRows.length-1) {
	                    // Not all children of first are selected.
	                    return false;
	                }
	            }
	        }
	        return true;
	    }

	    protected Transferable createTransferable(JComponent c) {
	        JTree tree = (JTree)c;
	        TreePath[] paths = tree.getSelectionPaths();
	        if(paths != null) {
	            // Make up a node array of copies for transfer and
	            // another for/of the nodes that will be removed in
	            // exportDone after a successful drop.
	            List<DefaultMutableTreeNode> copies = new ArrayList<DefaultMutableTreeNode>();
	            List<DefaultMutableTreeNode> toRemove = new ArrayList<DefaultMutableTreeNode>();
	            DefaultMutableTreeNode node = (DefaultMutableTreeNode)paths[0].getLastPathComponent();
	            DefaultMutableTreeNode copy = copy(node);
	            
	            copies.add(copy);
	            toRemove.add(node);
	            for(int i = 1; i < paths.length; i++) {
	                DefaultMutableTreeNode next =
	                    (DefaultMutableTreeNode)paths[i].getLastPathComponent();
	                // Do not allow higher level nodes to be added to list.
	                if(next.getLevel() < node.getLevel()) {
	                    break;
	                } else if(next.getLevel() > node.getLevel()) {  // child node
	                    copy.add(copy(next));
	                    // node already contains child
	                } else {                                        // sibling
	                    copies.add(copy(next));
	                    toRemove.add(next);
	                }
	            }
	            DefaultMutableTreeNode[] nodes =
	                copies.toArray(new DefaultMutableTreeNode[copies.size()]);
	            nodesToRemove =
	                toRemove.toArray(new DefaultMutableTreeNode[toRemove.size()]);
	            return null; //new NodesTransferable(nodes);
	        }
	        return null;
	    }

	    /** Defensive copy used in createTransferable. */
	    private DefaultMutableTreeNode copy(TreeNode node) {
	        return new DefaultMutableTreeNode(node);
	    }

	    protected void exportDone(JComponent source, Transferable data, int action) {
	        if((action & MOVE) == MOVE) {
	            JTree tree = (JTree)source;
	            DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
	            // Remove nodes saved in nodesToRemove in createTransferable.
	            for(int i = 0; i < nodesToRemove.length; i++) {
	                model.removeNodeFromParent(nodesToRemove[i]);
	            }
	        }
	    }

	    public int getSourceActions(JComponent c) {
	        return COPY_OR_MOVE;
	    }

	    public boolean importData(TransferHandler.TransferSupport support) {
	        if(!canImport(support)) {
	            return false;
	        }
	        // Extract transfer data.
	        DefaultMutableTreeNode[] nodes = null;
	        try {
	            Transferable t = support.getTransferable();
	            nodes = (DefaultMutableTreeNode[])t.getTransferData(nodesFlavor);
	        } catch(UnsupportedFlavorException ufe) {
	            System.out.println("UnsupportedFlavor: " + ufe.getMessage());
	        } catch(java.io.IOException ioe) {
	            System.out.println("I/O error: " + ioe.getMessage());
	        }
	        // Get drop location info.
	        JTree.DropLocation dl =
	                (JTree.DropLocation)support.getDropLocation();
	        int childIndex = dl.getChildIndex();
	        TreePath dest = dl.getPath();
	        DefaultMutableTreeNode parent =
	            (DefaultMutableTreeNode)dest.getLastPathComponent();
	        JTree tree = (JTree)support.getComponent();
	        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
	        // Configure for drop mode.
	        int index = childIndex;    // DropMode.INSERT
	        if(childIndex == -1) {     // DropMode.ON
	            index = parent.getChildCount();
	        }
	        // Add data to model.
	        for(int i = 0; i < nodes.length; i++) {
	            model.insertNodeInto(nodes[i], parent, index++);
	        }
	        return true;
	    }

	    public String toString() {
	        return getClass().getName();
	    }

	    public class NodesTransferable implements Transferable {
	        final ProjectItemTreeNode[] nodes;

	        public NodesTransferable(final ProjectItemTreeNode[] nodes) {
	            this.nodes = nodes;
	         }

	        public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException {
	            if(!isDataFlavorSupported(flavor)) {
	                throw new UnsupportedFlavorException(flavor);
	            }
	            else {
		            return nodes;
	            }
	        }

	        public DataFlavor[] getTransferDataFlavors() {
	            return flavors;
	        }

	        public boolean isDataFlavorSupported(final DataFlavor flavor) {
	            return nodesFlavor.equals(flavor);
	        }
	    }
	}

}
