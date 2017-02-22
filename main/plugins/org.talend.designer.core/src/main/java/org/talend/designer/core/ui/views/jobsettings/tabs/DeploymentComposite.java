package org.talend.designer.core.ui.views.jobsettings.tabs;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.runtime.maven.MavenConstants;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.core.runtime.repository.build.BuildExportManager;
import org.talend.core.runtime.repository.build.BuildType;
import org.talend.core.runtime.repository.build.IBuildParametes;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.cmd.MavenDeploymentValueChangeCommand;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.repository.utils.MavenVersionUtils;

public class DeploymentComposite extends AbstractTabComposite {

    private final int SPACE_LABEL_TEXT = 180;

    private final int SPACE_TO_LEFT = 23;

    private Text groupIdText;

    private Button versionCheckbox;

    private Text versionText;

    private Label versionWarningLabel;

    private ComboViewer buildTypeCombo;

    private String defaultVersion;

    private Process process;

    public DeploymentComposite(Composite parent, int style, TabbedPropertySheetWidgetFactory widgetFactory,
            IRepositoryViewObject iRepositoryViewObject) {
        super(parent, style, widgetFactory, iRepositoryViewObject);
        if (iRepositoryViewObject instanceof Process) {
            process = (Process) iRepositoryViewObject;
            defaultVersion = process.getVersion();
        }
        createControl();
        initialize();
        addListeners();
    }

    private void createControl() {
        FormLayout layout = new FormLayout();
        setLayout(layout);

        FormData thisFormData = new FormData();
        thisFormData.left = new FormAttachment(0, 0);
        thisFormData.right = new FormAttachment(100, 0);
        thisFormData.top = new FormAttachment(0, 0);
        thisFormData.bottom = new FormAttachment(100, 0);
        setLayoutData(thisFormData);

        Composite composite = widgetFactory.createFlatFormComposite(this);

        FormData compositeData = new FormData();
        compositeData.left = new FormAttachment(0, 0);
        compositeData.right = new FormAttachment(100, 0);
        compositeData.top = new FormAttachment(0, 0);
        compositeData.bottom = new FormAttachment(100, 0);
        composite.setLayoutData(compositeData);

        groupIdText = widgetFactory.createText(composite, ""); //$NON-NLS-1$
        FormData data = new FormData();
        data.left = new FormAttachment(0, SPACE_LABEL_TEXT);
        data.right = new FormAttachment(50, 0);
        data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
        groupIdText.setLayoutData(data);

        Label groupIdLabel = widgetFactory.createLabel(composite, Messages.getString("DeploymentComposite.gourpIdLabel")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, SPACE_TO_LEFT);
        data.right = new FormAttachment(groupIdText, -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(groupIdText, 0, SWT.CENTER);
        groupIdLabel.setLayoutData(data);

        versionWarningLabel = widgetFactory.createLabel(composite, Messages.getString("DeploymentComposite.versionWarning")); //$NON-NLS-1$
        versionWarningLabel.setBackground(getDisplay().getSystemColor(SWT.COLOR_YELLOW));
        data = new FormData();
        data.left = new FormAttachment(50, 10);
        data.right = new FormAttachment(80, 10);
        data.top = new FormAttachment(groupIdLabel, ITabbedPropertyConstants.VSPACE);
        versionWarningLabel.setLayoutData(data);

        versionText = widgetFactory.createText(composite, ""); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, SPACE_LABEL_TEXT);
        data.right = new FormAttachment(50, 0);
        data.top = new FormAttachment(groupIdLabel, ITabbedPropertyConstants.VSPACE);
        versionText.setLayoutData(data);

        Label versionLabel = widgetFactory.createLabel(composite, Messages.getString("DeploymentComposite.versionLabel")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, SPACE_TO_LEFT);
        data.right = new FormAttachment(versionText, -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(versionText, 0, SWT.CENTER);
        versionLabel.setLayoutData(data);

        versionCheckbox = widgetFactory.createButton(composite, "", SWT.CHECK);
        data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(versionLabel, 0);
        data.top = new FormAttachment(versionLabel, 0, SWT.CENTER);
        versionCheckbox.setLayoutData(data);

        buildTypeCombo = new ComboViewer(widgetFactory.createCCombo(composite));
        data = new FormData();
        data.left = new FormAttachment(0, SPACE_LABEL_TEXT);
        data.right = new FormAttachment(50, 0);
        data.top = new FormAttachment(versionText, ITabbedPropertyConstants.VSPACE);
        final Control exportTypeControl = buildTypeCombo.getControl();
        exportTypeControl.setLayoutData(data);
        buildTypeCombo.setContentProvider(ArrayContentProvider.getInstance());
        buildTypeCombo.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                if (element instanceof BuildType) {
                    return ((BuildType) element).getLabel();
                }
                return super.getText(element);
            }

        });

        Label exportTypeLabel = widgetFactory.createLabel(composite, Messages.getString("DeploymentComposite.buildTypeLabel")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, SPACE_TO_LEFT);
        data.right = new FormAttachment(exportTypeControl, -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(exportTypeControl, 0, SWT.CENTER);
        exportTypeLabel.setLayoutData(data);
    }

    private void initialize() {
        Map<Object, Object> processAdditionalProperties = this.process.getAdditionalProperties();
        if (processAdditionalProperties != null) {
            String groupId = (String) processAdditionalProperties.get(MavenConstants.NAME_GROUP_ID);
            if (groupId != null) {
                groupIdText.setText(groupId);
            } else {
                String defaultGroupId = "org.talend"; // TODO get from preference store.
                groupIdText.setText(defaultGroupId);
            }
            String userVersion = (String) processAdditionalProperties.get(MavenConstants.NAME_USER_VERSION);
            if (userVersion != null) {
                boolean isDefaultVersion = userVersion.equals(defaultVersion);
                versionCheckbox.setSelection(!isDefaultVersion);
                versionText.setEnabled(!isDefaultVersion);
                versionText.setText(userVersion);
                versionWarningLabel.setVisible(!MavenVersionUtils.isValidMavenVersion(userVersion));
            } else {
                versionCheckbox.setSelection(false);
                versionText.setEnabled(false);
                versionText.setText(defaultVersion);
                versionWarningLabel.setVisible(false);
            }

            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put(IBuildParametes.PROCESS, this.process);
            final BuildType[] validBuildTypes = BuildExportManager.getInstance().getValidBuildTypes(parameters);
            final Control buildTypeControl = buildTypeCombo.getControl();
            if (validBuildTypes == null || validBuildTypes.length == 0) {
                buildTypeControl.setEnabled(false);
            } else {
                buildTypeCombo.setInput(validBuildTypes);
                buildTypeControl.setEnabled(true);
                String buildType = (String) processAdditionalProperties.get(TalendProcessArgumentConstant.ARG_BUILD_TYPE);
                BuildType foundType = null;
                if (buildType != null) {
                    for (BuildType t : validBuildTypes) {
                        if (t.getName().equals(buildType)) {
                            foundType = t;
                            break;
                        }
                    }
                }
                if (foundType == null) {// set the first one by default
                    foundType = validBuildTypes[0];
                }
                buildTypeCombo.setSelection(new StructuredSelection(foundType));
            }
        }
    }

    private void addListeners() {
        groupIdText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                if (!StringUtils.isEmpty(groupIdText.getText())) {
                    Command cmd = new MavenDeploymentValueChangeCommand(process, MavenConstants.NAME_GROUP_ID, groupIdText
                            .getText());
                    getCommandStack().execute(cmd);
                }
            }
        });

        versionCheckbox.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (versionCheckbox.getSelection()) {
                    versionText.setEnabled(true);
                    versionText.setText(""); //$NON-NLS-1$
                } else {
                    versionText.setEnabled(false);
                    versionText.setText(defaultVersion);
                    // remove key, so will be default version
                    Command cmd = new MavenDeploymentValueChangeCommand(process, MavenConstants.NAME_USER_VERSION, null);
                    getCommandStack().execute(cmd);
                }
            }

        });

        versionText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                String version = versionText.getText();
                if (!StringUtils.isEmpty(version) && !MavenVersionUtils.isValidMavenVersion(version)) {
                    versionWarningLabel.setVisible(true);
                } else {
                    versionWarningLabel.setVisible(false);
                    // if empty, remove it from job, else will set the new value
                    Command cmd = new MavenDeploymentValueChangeCommand(process, MavenConstants.NAME_USER_VERSION, version);
                    getCommandStack().execute(cmd);
                }
            }

        });

        buildTypeCombo.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                final ISelection selection = event.getSelection();
                if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
                    final Object elem = ((IStructuredSelection) selection).getFirstElement();
                    if (elem instanceof BuildType) {
                        Command cmd = new MavenDeploymentValueChangeCommand(process,
                                TalendProcessArgumentConstant.ARG_BUILD_TYPE, ((BuildType) elem).getName());
                        getCommandStack().execute(cmd);
                    }
                }
            }

        });
    }

    private CommandStack getCommandStack() {
        IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if (part instanceof AbstractMultiPageTalendEditor) {
            CommandStack cmdStack = (CommandStack) part.getAdapter(CommandStack.class);
            return cmdStack;
        }
        return null;
    }

}
