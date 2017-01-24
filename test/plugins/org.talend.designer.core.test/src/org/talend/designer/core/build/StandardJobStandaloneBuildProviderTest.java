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
package org.talend.designer.core.build;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.junit.Assert;
import org.junit.Test;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.repository.build.IBuildParametes;
import org.talend.core.runtime.repository.build.IBuildPomCreatorParameters;
import org.talend.core.runtime.repository.build.IMavenPomCreator;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.RoutinesParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.maven.tools.creator.CreateMavenJobPom;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class StandardJobStandaloneBuildProviderTest {

    class StandardJobStandaloneBuildProviderTestClass extends StandardJobStandaloneBuildProvider {

    }

    @Test
    public void test_createPomCreator_emptyParam() {
        StandardJobStandaloneBuildProviderTestClass provider = new StandardJobStandaloneBuildProviderTestClass();
        Assert.assertNull(provider.createPomCreator(null));
        Assert.assertNull(provider.createPomCreator(Collections.emptyMap()));
    }

    @Test
    public void test_createPomCreator_nullProcessor() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        // parameters.put(IBuildPomCreatorParameters.PROCESSOR, null);

        StandardJobStandaloneBuildProviderTestClass provider = new StandardJobStandaloneBuildProviderTestClass();
        Assert.assertNull(provider.createPomCreator(parameters));
    }

    @Test
    public void test_createPomCreator_nonProcessor() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildPomCreatorParameters.PROCESSOR, new Object());

        StandardJobStandaloneBuildProviderTestClass provider = new StandardJobStandaloneBuildProviderTestClass();
        Assert.assertNull(provider.createPomCreator(parameters));
    }

    @Test
    public void test_createPomCreator_nullPomFile() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildPomCreatorParameters.PROCESSOR, new TestProcessor());
        // parameters.put(IBuildPomCreatorParameters.FILE_POM, null);

        StandardJobStandaloneBuildProviderTestClass provider = new StandardJobStandaloneBuildProviderTestClass();
        Assert.assertNull(provider.createPomCreator(parameters));
    }

    @Test
    public void test_createPomCreator_nonPomFile() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildPomCreatorParameters.PROCESSOR, new TestProcessor());
        parameters.put(IBuildPomCreatorParameters.FILE_POM, new Object());

        StandardJobStandaloneBuildProviderTestClass provider = new StandardJobStandaloneBuildProviderTestClass();
        Assert.assertNull(provider.createPomCreator(parameters));
    }

    @Test
    public void test_createPomCreator_nullAssemblyFile() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildPomCreatorParameters.PROCESSOR, new TestProcessor());
        ITalendProcessJavaProject talendProcessJavaProject = RepositoryPlugin.getDefault().getRunProcessService()
                .getTalendProcessJavaProject();
        parameters.put(IBuildPomCreatorParameters.FILE_POM, talendProcessJavaProject.getProject().getFile("pom_abc.xml"));
        // parameters.put(IBuildPomCreatorParameters.FILE_ASSEMBLY, null);

        StandardJobStandaloneBuildProviderTestClass provider = new StandardJobStandaloneBuildProviderTestClass();
        Assert.assertNull(provider.createPomCreator(parameters));
    }

    @Test
    public void test_createPomCreator_nonAssemblyFile() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildPomCreatorParameters.PROCESSOR, new TestProcessor());
        ITalendProcessJavaProject talendProcessJavaProject = RepositoryPlugin.getDefault().getRunProcessService()
                .getTalendProcessJavaProject();
        parameters.put(IBuildPomCreatorParameters.FILE_POM, talendProcessJavaProject.getProject().getFile("pom_abc.xml"));
        parameters.put(IBuildPomCreatorParameters.FILE_ASSEMBLY, new Object());

        StandardJobStandaloneBuildProviderTestClass provider = new StandardJobStandaloneBuildProviderTestClass();
        Assert.assertNull(provider.createPomCreator(parameters));
    }

    @Test
    public void test_createPomCreator_nullWinCP() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildPomCreatorParameters.PROCESSOR, new TestProcessor());
        ITalendProcessJavaProject talendProcessJavaProject = RepositoryPlugin.getDefault().getRunProcessService()
                .getTalendProcessJavaProject();
        parameters.put(IBuildPomCreatorParameters.FILE_POM, talendProcessJavaProject.getProject().getFile("pom_abc.xml"));
        parameters.put(IBuildPomCreatorParameters.FILE_ASSEMBLY, talendProcessJavaProject.getProject().getFile("pom_abc.xml"));

        StandardJobStandaloneBuildProviderTestClass provider = new StandardJobStandaloneBuildProviderTestClass();
        Assert.assertNull(provider.createPomCreator(parameters));
    }

    @Test
    public void test_createPomCreator_nullLinuxCP() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildPomCreatorParameters.PROCESSOR, new TestProcessor());
        ITalendProcessJavaProject talendProcessJavaProject = RepositoryPlugin.getDefault().getRunProcessService()
                .getTalendProcessJavaProject();
        parameters.put(IBuildPomCreatorParameters.FILE_POM, talendProcessJavaProject.getProject().getFile("pom_abc.xml"));
        parameters.put(IBuildPomCreatorParameters.FILE_ASSEMBLY, talendProcessJavaProject.getProject().getFile("pom_abc.xml"));
        parameters.put(IBuildPomCreatorParameters.CP_WIN, "");

        StandardJobStandaloneBuildProviderTestClass provider = new StandardJobStandaloneBuildProviderTestClass();
        Assert.assertNull(provider.createPomCreator(parameters));
    }

    @Test
    public void test_createPomCreator_nullItem() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildPomCreatorParameters.PROCESSOR, new TestProcessor());
        ITalendProcessJavaProject talendProcessJavaProject = RepositoryPlugin.getDefault().getRunProcessService()
                .getTalendProcessJavaProject();
        parameters.put(IBuildPomCreatorParameters.FILE_POM, talendProcessJavaProject.getProject().getFile("pom_abc.xml"));
        parameters.put(IBuildPomCreatorParameters.FILE_ASSEMBLY, talendProcessJavaProject.getProject().getFile("pom_abc.xml"));
        parameters.put(IBuildPomCreatorParameters.CP_WIN, "");
        parameters.put(IBuildPomCreatorParameters.CP_LINUX, "");

        StandardJobStandaloneBuildProviderTestClass provider = new StandardJobStandaloneBuildProviderTestClass();
        Assert.assertNull(provider.createPomCreator(parameters));
    }

    @Test
    public void test_createPomCreator_nonItem() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildPomCreatorParameters.PROCESSOR, new TestProcessor());
        ITalendProcessJavaProject talendProcessJavaProject = RepositoryPlugin.getDefault().getRunProcessService()
                .getTalendProcessJavaProject();
        parameters.put(IBuildPomCreatorParameters.FILE_POM, talendProcessJavaProject.getProject().getFile("pom_abc.xml"));
        parameters.put(IBuildPomCreatorParameters.FILE_ASSEMBLY, talendProcessJavaProject.getProject().getFile("pom_abc.xml"));
        parameters.put(IBuildPomCreatorParameters.CP_WIN, "");
        parameters.put(IBuildPomCreatorParameters.CP_LINUX, "");
        parameters.put(IBuildParametes.ITEM, new Object());

        StandardJobStandaloneBuildProviderTestClass provider = new StandardJobStandaloneBuildProviderTestClass();
        Assert.assertNull(provider.createPomCreator(parameters));
    }

    @Test
    public void test_createPomCreator_nonArgumentsMap() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(IBuildPomCreatorParameters.PROCESSOR, new TestProcessor());
        ITalendProcessJavaProject talendProcessJavaProject = RepositoryPlugin.getDefault().getRunProcessService()
                .getTalendProcessJavaProject();
        parameters.put(IBuildPomCreatorParameters.FILE_POM, talendProcessJavaProject.getProject().getFile("pom_abc.xml"));
        parameters.put(IBuildPomCreatorParameters.FILE_ASSEMBLY, talendProcessJavaProject.getProject().getFile("pom_abc.xml"));
        parameters.put(IBuildPomCreatorParameters.CP_WIN, "");
        parameters.put(IBuildPomCreatorParameters.CP_LINUX, "");
        parameters.put(IBuildParametes.ITEM, PropertiesFactory.eINSTANCE.createProcessItem());
        parameters.put(IBuildPomCreatorParameters.ARGUMENTS_MAP, new Object());

        StandardJobStandaloneBuildProviderTestClass provider = new StandardJobStandaloneBuildProviderTestClass();
        Assert.assertNull(provider.createPomCreator(parameters));
    }

    protected Item createJobItem() throws PersistenceException {
        final IProxyRepositoryFactory repositoryFactory = DesignerPlugin.getDefault().getRepositoryService()
                .getProxyRepositoryFactory();
        final String testJobId = repositoryFactory.getNextId();
        final String testJobLabel = StandardJobStandaloneBuildProvider.class.getName() + System.currentTimeMillis();

        // copied from NewProcessWizard
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        property.setStatusCode("");
        property.setId(testJobId);
        property.setDisplayName(testJobLabel);
        property.setLabel(property.getDisplayName());

        ProcessItem processItem = PropertiesFactory.eINSTANCE.createProcessItem();
        processItem.setProperty(property);
        property.setItem(processItem);

        ProcessType process = TalendFileFactory.eINSTANCE.createProcessType();
        ParametersType parameterType = TalendFileFactory.eINSTANCE.createParametersType();
        List<RoutinesParameterType> dependenciesInPreference = RoutinesUtil.createDependenciesInPreference();
        parameterType.getRoutinesParameter().addAll(dependenciesInPreference);
        process.setParameters(parameterType);
        processItem.setProcess(process);

        repositoryFactory.create(processItem, new Path(""));

        return processItem;
    }

    @Test
    public void test_createPomCreator() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();

        Item jobItem = createJobItem();

        final IDesignerCoreService designerCoreService = CorePlugin.getDefault().getDesignerCoreService();
        Assert.assertNotNull(designerCoreService);
        final IProcess processFromItem = designerCoreService.getProcessFromItem(jobItem);

        IProcessor processor = ProcessorUtilities.getProcessor(processFromItem, jobItem.getProperty());

        final Class<? extends IProcessor> processorClazz = processor.getClass();
        Assert.assertEquals("org.talend.designer.runprocess.maven.MavenJavaProcessor", processorClazz.getName());

        processor.setContext(processor.getProcess().getContextManager().getDefaultContext());

        parameters.put(IBuildPomCreatorParameters.PROCESSOR, processor);

        final Method getPomFileMethod = processorClazz.getDeclaredMethod("getPomFile");
        getPomFileMethod.setAccessible(true);
        final Object getPomFile = getPomFileMethod.invoke(processor);
        Assert.assertTrue(getPomFile instanceof IFile);
        IFile pomFile = (IFile) getPomFile;
        parameters.put(IBuildPomCreatorParameters.FILE_POM, pomFile);

        final Method getAssemblyFileMethod = processorClazz.getDeclaredMethod("getAssemblyFile");
        getAssemblyFileMethod.setAccessible(true);
        final Object getAssemblyFile = getAssemblyFileMethod.invoke(processor);
        Assert.assertTrue(getPomFile instanceof IFile);
        IFile assemblyFile = (IFile) getAssemblyFile;
        parameters.put(IBuildPomCreatorParameters.FILE_ASSEMBLY, assemblyFile);

        // before set classpath, must do initJobClasspath
        final Method initJobClasspathMethod = processorClazz.getDeclaredMethod("initJobClasspath");
        initJobClasspathMethod.setAccessible(true);
        initJobClasspathMethod.invoke(processor);

        final Field windowsClasspathField = processorClazz.getDeclaredField("windowsClasspath");
        windowsClasspathField.setAccessible(true);
        parameters.put(IBuildPomCreatorParameters.CP_WIN, windowsClasspathField.get(processor));

        final Field unixClasspathField = processorClazz.getDeclaredField("unixClasspath");
        unixClasspathField.setAccessible(true);
        parameters.put(IBuildPomCreatorParameters.CP_LINUX, unixClasspathField.get(processor));

        parameters.put(IBuildParametes.ITEM, processor.getProperty().getItem());

        final Map<String, Object> arguments = processor.getArguments();
        parameters.put(IBuildPomCreatorParameters.ARGUMENTS_MAP, arguments);
        parameters.put(IBuildPomCreatorParameters.OVERWRITE_POM, true);

        // create pom and assembly
        StandardJobStandaloneBuildProviderTestClass provider = new StandardJobStandaloneBuildProviderTestClass();
        final IMavenPomCreator pomCreator = provider.createPomCreator(parameters);
        Assert.assertNotNull("Create the pom creator failure", pomCreator);
        Assert.assertEquals(CreateMavenJobPom.class, pomCreator.getClass());
        pomCreator.create(null);

        pomFile.getParent().refreshLocal(IResource.DEPTH_ONE, null);
        Assert.assertTrue("the pom file " + pomFile.getName() + " is not created ", pomFile.exists());

        assemblyFile.getParent().refreshLocal(IResource.DEPTH_ONE, null);
        Assert.assertTrue("the assembly file " + assemblyFile.getName() + " is not created ", assemblyFile.exists());
    }

}
