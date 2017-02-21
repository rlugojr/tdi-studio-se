package org.talend.repository.preference;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.navigator.NavigatorDecoratingLabelProvider;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ItemState;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.ItemVersionObject;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.dialog.ItemsVersionConfirmDialog;
import org.talend.repository.utils.MavenVersionUtils;

public class MavenVersionManagementProjectSettingPage extends AbstractVersionManagementProjectSettingPage {

    private final Color COLOR_YELLOW = Display.getDefault().getSystemColor(SWT.COLOR_DARK_YELLOW);

    private Button applyVersionButton;

    private Button eachVersionButton;

    private Button useJobVersionButton;

    private String appliedFixedVersion = MavenVersionUtils.DEFAULT_MAVEN_VERSION;

    public MavenVersionManagementProjectSettingPage() {
        super();
        this.noDefaultAndApplyButton();
    }

    @Override
    protected boolean filterRepositoryNode(RepositoryNode node) {
        if (node == null) {
            return false;
        }
        if (node.isBin()) {
            return false;
        }
        ERepositoryObjectType type = node.getObjectType();
        if (type == ERepositoryObjectType.PROCESS || type == ERepositoryObjectType.PROCESS_MR
                || type == ERepositoryObjectType.PROCESS_ROUTE || type == ERepositoryObjectType.PROCESS_SPARK
                || type == ERepositoryObjectType.PROCESS_SPARKSTREAMING || type == ERepositoryObjectType.PROCESS_STORM
                || type == ERepositoryObjectType.TEST_CONTAINER) {
            return true;
        }
        return false;
    }

    @Override
    protected void addItemsOption(Composite parent) {
        Group option = new Group(parent, SWT.NONE);
        option.setLayout(new GridLayout());
        option.setText(Messages.getString("VersionManagementDialog.Options")); //$NON-NLS-1$
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(option);
        fixedVersionButton = new Button(option, SWT.RADIO);
        fixedVersionButton.setText(Messages.getString("VersionManagementDialog.FixedVersion")); //$NON-NLS-1$
        fixedVersionButton.setSelection(true); // default

        Composite versionComposit = new Composite(option, SWT.NONE);
        GridLayout layout = new GridLayout(8, false);
        layout.horizontalSpacing = 1;
        layout.verticalSpacing = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        versionComposit.setLayout(layout);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(versionComposit);

        fixedVersionText = new Text(versionComposit, SWT.BORDER);
        GridData data = new GridData();
        data.widthHint = 120;
        data.minimumWidth = 80;
        data.horizontalIndent = 20;
        fixedVersionText.setLayoutData(data);
        fixedVersionText.setText(MavenVersionUtils.DEFAULT_MAVEN_VERSION);

        applyVersionButton = new Button(versionComposit, SWT.NONE);
        applyVersionButton.setText(Messages.getString("VersionManagementDialog.applyVersion"));

        subjobs = new Button(versionComposit, SWT.NONE);
        subjobs.setText(Messages.getString("VersionManagementDialog.subjob"));
        subjobs.setEnabled(false);

        eachVersionButton = new Button(option, SWT.RADIO);
        eachVersionButton.setText(Messages.getString("VersionManagementDialog.EachVersion")); //$NON-NLS-1$

        useJobVersionButton = new Button(option, SWT.RADIO);
        useJobVersionButton.setText(Messages.getString("VersionManagementDialog.useJobVersion"));
        // event
        fixedVersionButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                fixedVersionText.setEnabled(fixedVersionButton.getSelection());
                applyVersionButton.setEnabled(fixedVersionButton.getSelection());
                researchMaxVersion();
                refreshTableItems();
            }
        });

        applyVersionButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String version = fixedVersionText.getText();
                if (version != null && !"".equals(version.trim())) { //$NON-NLS-1$
                    appliedFixedVersion = version;
                    refreshTableItems();
                }
            }
        });

        subjobs.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                selectSubjob();
            }
        });

        eachVersionButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                refreshTableItems();
            }
        });

        useJobVersionButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                refreshTableItems();
            }
        });

    }

    @Override
    protected void createItemTableColumns() {
        TableColumn itemColumn = new TableColumn(itemTable, SWT.CENTER);
        itemColumn.setText(Messages.getString("VersionManagementDialog.Items")); //$NON-NLS-1$
        itemColumn.setWidth(100);

        TableColumn oldVersionColumn = new TableColumn(itemTable, SWT.CENTER);
        oldVersionColumn.setText(Messages.getString("VersionManagementDialog.Version")); //$NON-NLS-1$
        oldVersionColumn.setWidth(80);

        TableColumn versionColumn = new TableColumn(itemTable, SWT.CENTER);
        versionColumn.setText(Messages.getString("VersionManagementDialog.NewVersion")); //$NON-NLS-1$
        versionColumn.setWidth(100);

        final TableColumn delColumn = new TableColumn(itemTable, SWT.CENTER);
        delColumn.setText(""); //$NON-NLS-1$
        delColumn.setWidth(26);
        delColumn.setResizable(false);

        TableColumn messageColumn = new TableColumn(itemTable, SWT.CENTER);
        messageColumn.setText(""); //$NON-NLS-1$
        messageColumn.setWidth(170);

        versionColumn.addControlListener(new ControlAdapter() {

            @Override
            public void controlResized(ControlEvent e) {
                if (eachVersionButton.getSelection()) {
                    refreshTableItems();
                }
            }

        });
        itemTable.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                checkButtonsState();
            }
        });

    }

    @Override
    protected void addItemElements(List<ItemVersionObject> elements) {
        if (elements == null || elements.isEmpty()) {
            return;
        }
        itemTable.setRedraw(false);

        for (final ItemVersionObject object : elements) {
            final TableItem tableItem = new TableItem(itemTable, SWT.CENTER);
            tableItem.setData(object);
            Item item = object.getItem();
            IBaseLabelProvider labelProvider = treeViewer.getLabelProvider();
            Image itemsImage = null;
            if (labelProvider instanceof NavigatorDecoratingLabelProvider) {
                NavigatorDecoratingLabelProvider navigatorProvider = (NavigatorDecoratingLabelProvider) labelProvider;
                itemsImage = navigatorProvider.getImage(object.getRepositoryNode());
            } else {
                ERepositoryObjectType itemType = ERepositoryObjectType.getItemType(item);
                itemsImage = getItemsImage(CoreImageProvider.getIcon(itemType));
            }
            tableItem.setImage(itemsImage);
            tableItem.setText(item.getProperty().getLabel());
            // old version
            tableItem.setText(1, MavenVersionUtils.getItemMavenVersion(item.getProperty()));

            TableEditor versionEditor = null;

            if (fixedVersionButton.getSelection()) {
                tableItem.setText(2, appliedFixedVersion);
            } else if (useJobVersionButton.getSelection()) {
                tableItem.setText(2, item.getProperty().getVersion());
            } else {
                // new version
                versionEditor = new TableEditor(itemTable);

                Composite versionComposite = new Composite(itemTable, SWT.NONE);
                GridLayout layout = new GridLayout(1, false);
                layout.marginHeight = 0;
                layout.marginWidth = 0;
                layout.marginLeft = 0;
                layout.marginRight = 1;
                versionComposite.setLayout(layout);

                final Text text = new Text(versionComposite, SWT.CENTER | SWT.BORDER);

                GridData data = new GridData(GridData.FILL_BOTH);
                text.setLayoutData(data);
                String newVersion = object.getNewVersion();
                if (newVersion == null || "".equals(newVersion.trim())) { //$NON-NLS-1$
                    newVersion = appliedFixedVersion;
                }
                text.setText(newVersion);
                checkVersionPattern(tableItem, newVersion);

                text.addModifyListener(new ModifyListener() {

                    @Override
                    public void modifyText(ModifyEvent e) {
                        String version = text.getText();
                        checkVersionPattern(tableItem, version);
                        object.setNewVersion(version);
                    }

                });

                versionEditor.minimumWidth = itemTable.getColumn(2).getWidth();
                versionEditor.setEditor(versionComposite, tableItem, 2);
            }
            TableEditor delEditor = new TableEditor(itemTable);
            Label delLabel = new Label(itemTable, SWT.CENTER);
            delLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
            delLabel.setImage(ImageProvider.getImage(EImage.DELETE_ICON));
            delLabel.setToolTipText(Messages.getString("VersionManagementDialog.DeletedTip")); //$NON-NLS-1$
            delLabel.pack();
            delLabel.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseDown(MouseEvent e) {
                    checkedObjects.remove(object);
                    removeTableItem(tableItem);
                    refreshCheckedTreeView();
                    checkButtonsState();
                    checkButtonsState(false);
                }

            });

            delEditor.minimumWidth = 25;
            delEditor.horizontalAlignment = SWT.CENTER;
            delEditor.setEditor(delLabel, tableItem, 3);
            if (fixedVersionButton.getSelection() || useJobVersionButton.getSelection()) {
                tableItem.setData(ITEM_EDITOR_KEY, new TableEditor[] { delEditor });
            } else if (versionEditor != null) {
                tableItem.setData(ITEM_EDITOR_KEY, new TableEditor[] { versionEditor, delEditor });
            }
            itemTable.setRedraw(true);
        }
    }

    private void checkVersionPattern(TableItem tableItem, String version) {
        if (MavenVersionUtils.isValidMavenVersion(version)) {
            tableItem.setText(4, ""); //$NON-NLS-1$
        } else {
            tableItem.setText(4, Messages.getString("VersionManagementDialog.inValidVersionWarning")); //$NON-NLS-1$
            tableItem.setForeground(4, COLOR_YELLOW);
        }
    }

    protected String getNewVersionWithOption(ItemVersionObject object) {
        String newVersion;
        if (fixedVersionButton.getSelection()) {
            newVersion = appliedFixedVersion;
        } else if (eachVersionButton.getSelection()) {
            newVersion = object.getNewVersion();
        } else {
            newVersion = object.getItem().getProperty().getVersion();
        }
        return newVersion;
    }

    protected void updateItemsVersion() {
        final IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

            @Override
            public void run(final IProgressMonitor monitor) throws CoreException {
                RepositoryWorkUnit<Object> rwu = new RepositoryWorkUnit<Object>(project, "Update items Maven version") {

                    @Override
                    protected void run() throws LoginException, PersistenceException {
                        monitor.beginTask("Update items Maven version", checkedObjects.size()); //$NON-NLS-1$
                        for (ItemVersionObject object : checkedObjects) {
                            final Item item = object.getItem();
                            Property itemProperty = item.getProperty();
                            if (!object.getNewVersion().equals(MavenVersionUtils.getItemMavenVersion(itemProperty))) {
                                MavenVersionUtils.setItemMavenVersion(itemProperty, object.getNewVersion());
                                monitor.subTask(itemProperty.getLabel());
                                FACTORY.save(project, itemProperty);
                            }
                            monitor.worked(1);
                        }
                        try {
                            FACTORY.saveProject(project);
                        } catch (PersistenceException e) {
                            ExceptionHandler.process(e);
                        }
                    }
                };
                rwu.setAvoidUnloadResources(true);
                rwu.executeRun();
                monitor.done();
            }
        };

        IRunnableWithProgress iRunnableWithProgress = new IRunnableWithProgress() {

            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                IWorkspace workspace = ResourcesPlugin.getWorkspace();
                try {
                    ISchedulingRule schedulingRule = workspace.getRoot();
                    // the update the project files need to be done in the workspace runnable to avoid all notification
                    // of changes before the end of the modifications.
                    workspace.run(runnable, schedulingRule, IWorkspace.AVOID_UPDATE, monitor);
                } catch (CoreException e) {
                    ExceptionHandler.process(e);
                }

            }
        };

        final ProgressMonitorDialog dialog = new ProgressMonitorDialog(null);
        try {
            dialog.run(false, false, iRunnableWithProgress);
        } catch (InvocationTargetException | InterruptedException e) {
            ExceptionHandler.process(e);
        }
    }

    @Override
    protected ItemVersionObject createItemVersionObject(RepositoryNode node, Property property) {
        String version = MavenVersionUtils.getItemMavenVersion(property);
        ItemVersionObject object = new ItemVersionObject(property, node, version);
        return object;
    }

}
