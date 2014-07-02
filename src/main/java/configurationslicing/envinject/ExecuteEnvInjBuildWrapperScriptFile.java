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
public class ExecuteEnvInjBuildWrapperScriptFile extends UnorderedStringSlicer<Project> {

    public ExecuteEnvInjBuildWrapperScriptFile() {
        super(new EnvInjBuildWrapperScriptFileSliceSpec());
    }

    public static class EnvInjBuildWrapperScriptFileSliceSpec extends UnorderedStringSlicer.UnorderedStringSlicerSpec<Project> {

        public static final String DISABLED = "(disabled)";

        public String getDefaultValueString() {
            return DISABLED;
        }

        @Override
        public String getName() {
            return "Environment Inject (post-SCM checkout) Script File";
        }

        @Override
        public String getUrl() {
            return "EnvInjBuildScriptFileSlicing";
        }

        public String getName(Project item) {
            return item.getFullName();
        }

        public List<String> getValues(Project item) {
            List<String> content = new ArrayList<String>();
            
            EnvInjectBuildWrapper envInjectBuildWrapper = GetEnvInjectBuildWrapper(item);            
            if (null != envInjectBuildWrapper) {
                EnvInjectJobPropertyInfo envInjectJobPropertyInfo = envInjectBuildWrapper.getInfo();
                String scriptFilePath = envInjectJobPropertyInfo.getScriptFilePath();
                if (null != scriptFilePath) {
                    scriptFilePath = "";
                }
                content.add(scriptFilePath);
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
            String scriptFilePath = list.iterator().next();
            
            if (DISABLED.equals(scriptFilePath)) {
                if (null != envInjectBuildWrapper) {
                    try {
                        item.getBuildWrappersList().remove(EnvInjectBuildWrapper.class);
                        return true;
                    } catch (IOException ex) {
                        Logger.getLogger(ExecuteEnvInjBuildWrapperScriptFile.class.getName()).log(Level.SEVERE, null, ex);
                        return false;
                    }
                }
                return true;
            }
            
            if (null == envInjectBuildWrapper) {
                return CreateEnvInjectBuildWrapper(item, scriptFilePath);
            } else {
                return UpdateEnvInjectBuildWrapper(item, scriptFilePath);
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
        
        private boolean CreateEnvInjectBuildWrapper(Project item, String scriptFilePath) {
            EnvInjectBuildWrapper envInjectBuildWrapper = GetEnvInjectBuildWrapper(item);
            if (null == envInjectBuildWrapper) {
                envInjectBuildWrapper = new EnvInjectBuildWrapper();
                EnvInjectJobPropertyInfo envInjectJobPropertyInfo = new EnvInjectJobPropertyInfo(null, null, scriptFilePath, null, null, true);
                envInjectBuildWrapper.setInfo(envInjectJobPropertyInfo);
                item.getBuildWrappersList().add(envInjectBuildWrapper);
                return true;
            }
            return false;
        }
        
        private boolean UpdateEnvInjectBuildWrapper(Project item, String scriptFilePath) {
            EnvInjectBuildWrapper envInjectBuildWrapper = GetEnvInjectBuildWrapper(item);
            if (null != envInjectBuildWrapper) {
                EnvInjectJobPropertyInfo oldEnvInjectJobPropertyInfo = envInjectBuildWrapper.getInfo();
                EnvInjectJobPropertyInfo newEnvInjectJobPropertyInfo = 
                        new EnvInjectJobPropertyInfo(
                            oldEnvInjectJobPropertyInfo.getPropertiesFilePath(),
                            oldEnvInjectJobPropertyInfo.getPropertiesContent(),
                            scriptFilePath,
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

