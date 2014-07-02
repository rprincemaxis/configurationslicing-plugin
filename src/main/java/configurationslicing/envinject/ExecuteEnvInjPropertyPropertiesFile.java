/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package configurationslicing.envinject;

import configurationslicing.UnorderedStringSlicer;
import hudson.Extension;
import hudson.matrix.MatrixProject;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.Project;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jenkinsci.plugins.envinject.EnvInjectJobProperty;
import org.jenkinsci.plugins.envinject.EnvInjectJobPropertyInfo;

/**
 *
 * @author rprince
 */

@Extension
public class ExecuteEnvInjPropertyPropertiesFile extends UnorderedStringSlicer<Job<?,?>> {

    public ExecuteEnvInjPropertyPropertiesFile() {
        super(new EnvInjPropertyPropertiesFileSliceSpec());
    }

    public static class EnvInjPropertyPropertiesFileSliceSpec extends UnorderedStringSlicer.UnorderedStringSlicerSpec<Job<?,?>> {

        public static final String DISABLED = "(disabled)";
        public static final String ENABLED = "(enabled)";

        public String getDefaultValueString() {
            return DISABLED;
        }

        @Override
        public String getName() {
            return "Environment Inject (before SCM checkout) Properties File";
        }

        @Override
        public String getUrl() {
            return "EnvInjGroovyPropFileSlicing";
        }


        public String getName(Job<?, ?> item) {
            return item.getFullName();
        }

        public List<String> getValues(Job<?, ?> item) {
            List<String> content = new ArrayList<String>();
            EnvInjectJobProperty envInjectJobProperty = item.getProperty(EnvInjectJobProperty.class);
            if (null != envInjectJobProperty && envInjectJobProperty.isOn()) {
                EnvInjectJobPropertyInfo envInjectJobPropertyInfo = envInjectJobProperty.getInfo();
                content.add(envInjectJobPropertyInfo.getPropertiesFilePath());
            }
            if (content.isEmpty()) {
                content.add(DISABLED);
            }

            return content;
        }

        @SuppressWarnings("unchecked")
        public List<Job<?, ?>> getWorkDomain() {
            List<Job<?, ?>> list = new ArrayList<Job<?, ?>>();
            List<Job> temp = Hudson.getInstance().getAllItems(Job.class);
            for (Job p: temp) {
                if (p instanceof Project || p instanceof MatrixProject) {
                    list.add(p);
                }
            }
            return list;
        }

        public boolean setValues(Job<?, ?> item, List<String> list) {
            EnvInjectJobProperty envInjectJobProperty = item.getProperty(EnvInjectJobProperty.class);
            String propertiesFilePath = list.iterator().next();
            
            if (DISABLED.equals(propertiesFilePath)) {
                if (null != envInjectJobProperty) {
                    envInjectJobProperty.setOn(false);                
                }
                return true;
            }
            
            if (ENABLED.equals(propertiesFilePath)) {
                if (null != envInjectJobProperty) {
                    envInjectJobProperty.setOn(true);
                    return true;
                }
                else
                {
                    propertiesFilePath = null;
                    return CreateEnvInjectJobProperty(item, propertiesFilePath);
                }
            }
            
            if (null == envInjectJobProperty) {
                return CreateEnvInjectJobProperty(item, propertiesFilePath);
            } else {
                return UpdateEnvInjectJobProperty(item, propertiesFilePath);
            }
        }
        
        private boolean CreateEnvInjectJobProperty(Job<?, ?> item, String propertiesFilePath) {
            EnvInjectJobProperty envInjectJobProperty = item.getProperty(EnvInjectJobProperty.class);
            if (null == envInjectJobProperty) {
                envInjectJobProperty = new EnvInjectJobProperty();
                envInjectJobProperty.setOn(true);
                envInjectJobProperty.setKeepBuildVariables(true);
                envInjectJobProperty.setKeepJenkinsSystemVariables(true);
                EnvInjectJobPropertyInfo envInjectJobPropertyInfo = new EnvInjectJobPropertyInfo(propertiesFilePath, null, null, null, null, true);
                envInjectJobProperty.setInfo(envInjectJobPropertyInfo);
                try {
                    item.addProperty(envInjectJobProperty);
                    return true;
                } catch (IOException e) {
                    return false;
                }
            }
            return false;
        }
        
        private boolean UpdateEnvInjectJobProperty(Job<?, ?> item, String propertiesFilePath) {
            EnvInjectJobProperty envInjectJobProperty = item.getProperty(EnvInjectJobProperty.class);
            envInjectJobProperty.setOn(true);
            EnvInjectJobPropertyInfo oldEnvInjectJobPropertyInfo = envInjectJobProperty.getInfo();
            EnvInjectJobPropertyInfo newEnvInjectJobPropertyInfo = 
                    new EnvInjectJobPropertyInfo(
                        propertiesFilePath,
                        oldEnvInjectJobPropertyInfo.getPropertiesContent(),
                        oldEnvInjectJobPropertyInfo.getScriptFilePath(),
                        oldEnvInjectJobPropertyInfo.getScriptContent(),
                        oldEnvInjectJobPropertyInfo.getGroovyScriptContent(),
                        oldEnvInjectJobPropertyInfo.isLoadFilesFromMaster()
                    );
            envInjectJobProperty.setInfo(newEnvInjectJobPropertyInfo);
            return true;
        }

    }
}

