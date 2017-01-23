// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.wizards.exportjob.scriptsmanager;

import java.util.HashMap;
import java.util.Map;

import org.talend.core.model.properties.ProcessItem;
import org.talend.core.runtime.process.IBuildJobHandler;
import org.talend.core.runtime.repository.build.AbstractBuildProvider;
import org.talend.core.runtime.repository.build.BuildExportManager;
import org.talend.core.runtime.repository.build.IBuildJobParameters;
import org.talend.core.runtime.repository.build.IBuildParametes;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;
import org.talend.repository.ui.wizards.exportjob.handler.BuildJobHandler;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;

/**
 * created by ycbai on 2015年5月15日 Detailled comment
 *
 */
public class BuildJobFactory {

    /**
     * Create the build job handler according the job export type. Now only implement the handler of standalone job.
     * <p>
     * DOC ycbai Comment method "createBuildJobHandler".
     * 
     * @param exportChoiceMap
     * @param contextName
     * @param jobExportType
     * @return
     */
    public static IBuildJobHandler createBuildJobHandler(ProcessItem processItem, String contextName, String version,
            Map<ExportChoice, Object> exportChoiceMap, JobExportType jobExportType) {

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildParametes.ITEM, processItem);
        parameters.put(IBuildParametes.VERSION, version);
        parameters.put(IBuildJobParameters.CONTEXT_GROUP, contextName);
        parameters.put(IBuildJobParameters.CHOICE_OPTION, exportChoiceMap);

        IBuildJobHandler handler = null;
        switch (jobExportType) {
        case POJO:
            final AbstractBuildProvider buildProvider = BuildExportManager.getInstance().getBuildProvider("Standalone",
                    parameters);
            if (buildProvider != null) {
                handler = buildProvider.createHandler(parameters);
            }
            if (handler == null) {
                // TODO, should remove
                handler = new BuildJobHandler(processItem, version, contextName, exportChoiceMap);
            }
            break;
        case WSWAR:
        case WSZIP:
        case OSGI:
            break;
        default:
            handler = new BuildJobHandler(processItem, version, contextName, exportChoiceMap);
        }

        return handler;
    }

}
