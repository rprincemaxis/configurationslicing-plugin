/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package configurationslicing.envinject;

import configurationslicing.UnorderedStringSlicer;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Project;
import hudson.tasks.BuildWrapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jenkinsci.plugins.envinject.EnvInjectBuildWrapper;
import org.jenkinsci.plugins.envinject.EnvInjectJobPropertyInfo;

/**
 *
 * @author rprince
 */

@Extension
public class ExecuteEnvInjBuildWrapperPropertiesFile extends UnorderedStringSlicer<Project> {

    public ExecuteEnvInjBuildWrapperPropertiesFile() {
        super(new EnvInjBuildWrapperPropertiesFileSliceSpec());
    }

    public static class EnvInjBuildWrapperPropertiesFileSliceSpec extends UnorderedStringSlicer.UnorderedStringSlicerSpec<Project> {

        public static final String DISABLED = "(disabled)";

        public String getDefaultValueString() {
            return DISABLED;
        }

        @Override
        public String getName() {
            return "Environment Inject (post-SCM checkout) Properties File";
        }

        @Override
        public String getUrl() {
            return "EnvInjBuildPropertiesFileSlicing";
        }

        public String getName(Project item) {
            return item.getFullName();
        }

        public List<String> getValues(Project item) {
            List<String> content = new ArrayList<String>();
            
            EnvInjectBuildWrapper envInjectBuildWrapper = GetEnvInjectBuildWrapper(item);            
            if (null != envInjectBuildWrapper) {
                EnvInjectJobPropertyInfo envInjectJobPropertyInfo = envInjectBuildWrapper.getInfo();
                String propertiesFilePath = envInjectJobPropertyInfo.getPropertiesFilePath();
                if (null != propertiesFilePath) {
                    propertiesFilePath = "";
                }
                content.add(propertiesFilePath);
            }
            if (content.isEmpty()) {
                content.add(DISABLED);
            }

            return content;
        }

        @SuppressWarnings("unchecked")
        public List<Project> getWorkDomain() {
            List<Project> list = Hudson.getInstance().getAllItems(Project.class);
            return list;
        }

        public boolean setValues(Project item, List<String> list) {
            EnvInjectBuildWrapper envInjectBuildWrapper = GetEnvInjectBuildWrapper(item);
            String propertiesFilePath = list.iterator().next();
            
            if (DISABLED.equals(propertiesFilePath)) {
                if (null != envInjectBuildWrapper) {
                    try {
                        item.getBuildWrappersList().remove(EnvInjectBuildWrapper.class);
                        return true;
                    } catch (IOException ex) {
                        Logger.getLogger(ExecuteEnvInjBuildWrapperPropertiesFile.class.getName()).log(Level.SEVERE, null, ex);
                        return false;
                    }
                }
                return true;
            }
            
            if (null == envInjectBuildWrapper) {
                return CreateEnvInjectBuildWrapper(item, propertiesFilePath);
            } else {
                return UpdateEnvInjectBuildWrapper(item, propertiesFilePath);
            }
        }
        
        private EnvInjectBuildWrapper GetEnvInjectBuildWrapper(Project item) {
            EnvInjectBuildWrapper envInjectBuildWrapper = null;
            
            Map<Descriptor<BuildWrapper>, BuildWrapper> buildWrapperMap = item.getBuildWrappers();
            Descriptor buildWrapperDescriptor = Descriptor.find(EnvInjectBuildWrapper.class.getName());
            if (null != buildWrapperDescriptor) {
                return (EnvInjectBuildWrapper) buildWrapperMap.get(buildWrapperDescriptor);
            }
            return envInjectBuildWrapper;
        }
        
        private boolean CreateEnvInjectBuildWrapper(Project item, String propertiesFilePath) {
            EnvInjectBuildWrapper envInjectBuildWrapper = GetEnvInjectBuildWrapper(item);
            if (null == envInjectBuildWrapper) {
                envInjectBuildWrapper = new EnvInjectBuildWrapper();
                EnvInjectJobPropertyInfo envInjectJobPropertyInfo = new EnvInjectJobPropertyInfo(propertiesFilePath, null, null, null, null, true);
                envInjectBuildWrapper.setInfo(envInjectJobPropertyInfo);
                item.getBuildWrappersList().add(envInjectBuildWrapper);
                return true;
            }
            return false;
        }
        
        private boolean UpdateEnvInjectBuildWrapper(Project item, String propertiesFilePath) {
            EnvInjectBuildWrapper envInjectBuildWrapper = GetEnvInjectBuildWrapper(item);
            if (null != envInjectBuildWrapper) {
                EnvInjectJobPropertyInfo oldEnvInjectJobPropertyInfo = envInjectBuildWrapper.getInfo();
                EnvInjectJobPropertyInfo newEnvInjectJobPropertyInfo = 
                        new EnvInjectJobPropertyInfo(
                            propertiesFilePath,
                            oldEnvInjectJobPropertyInfo.getPropertiesContent(),
                            oldEnvInjectJobPropertyInfo.getScriptFilePath(),
                            oldEnvInjectJobPropertyInfo.getScriptContent(),
                            oldEnvInjectJobPropertyInfo.getGroovyScriptContent(),
                            oldEnvInjectJobPropertyInfo.isLoadFilesFromMaster()
                        );
                envInjectBuildWrapper.setInfo(newEnvInjectJobPropertyInfo);
                return true;
            }
            return false;
        }

    }
}

