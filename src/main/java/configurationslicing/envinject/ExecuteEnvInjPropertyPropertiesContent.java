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
public class ExecuteEnvInjPropertyPropertiesContent extends UnorderedStringSlicer<Job<?,?>> {

    public ExecuteEnvInjPropertyPropertiesContent() {
        super(new EnvInjPropertyPropertiesContentSliceSpec());
    }

    public static class EnvInjPropertyPropertiesContentSliceSpec extends UnorderedStringSlicer.UnorderedStringSlicerSpec<Job<?,?>> {

        public static final String DISABLED = "(disabled)";
        public static final String ENABLED = "(enabled)";

        public String getDefaultValueString() {
            return DISABLED;
        }

        @Override
        public String getName() {
            return "Environment Inject (before SCM checkout) Properties Content";
        }

        @Override
        public String getUrl() {
            return "EnvInjGroovyPropertiesContentSlicing";
        }


        public String getName(Job<?, ?> item) {
            return item.getFullName();
        }

        public List<String> getValues(Job<?, ?> item) {
            List<String> content = new ArrayList<String>();
            EnvInjectJobProperty envInjectJobProperty = item.getProperty(EnvInjectJobProperty.class);
            if (null != envInjectJobProperty && envInjectJobProperty.isOn()) {
                EnvInjectJobPropertyInfo envInjectJobPropertyInfo = envInjectJobProperty.getInfo();
                content.add(envInjectJobPropertyInfo.getPropertiesContent());
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
            String propertiesContent = list.iterator().next();
            
            if (DISABLED.equals(propertiesContent)) {
                if (null != envInjectJobProperty) {
                    envInjectJobProperty.setOn(false);                
                }
                return true;
            }
            
            if (ENABLED.equals(propertiesContent)) {
                if (null != envInjectJobProperty) {
                    envInjectJobProperty.setOn(true);
                    return true;
                }
                else
                {
                    propertiesContent = null;
                    return CreateEnvInjectJobProperty(item, propertiesContent);
                }
            }
            
            if (null == envInjectJobProperty) {
                return CreateEnvInjectJobProperty(item, propertiesContent);
            } else {
                return UpdateEnvInjectJobProperty(item, propertiesContent);
            }
        }
        
        private boolean CreateEnvInjectJobProperty(Job<?, ?> item, String propertiesContent) {
            EnvInjectJobProperty envInjectJobProperty = item.getProperty(EnvInjectJobProperty.class);
            if (null == envInjectJobProperty) {
                envInjectJobProperty = new EnvInjectJobProperty();
                envInjectJobProperty.setOn(true);
                envInjectJobProperty.setKeepBuildVariables(true);
                envInjectJobProperty.setKeepJenkinsSystemVariables(true);
                EnvInjectJobPropertyInfo envInjectJobPropertyInfo = new EnvInjectJobPropertyInfo(null, propertiesContent, null, null, null, true);
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
        
        private boolean UpdateEnvInjectJobProperty(Job<?, ?> item, String propertiesContent) {
            EnvInjectJobProperty envInjectJobProperty = item.getProperty(EnvInjectJobProperty.class);
            envInjectJobProperty.setOn(true);
            EnvInjectJobPropertyInfo oldEnvInjectJobPropertyInfo = envInjectJobProperty.getInfo();
            EnvInjectJobPropertyInfo newEnvInjectJobPropertyInfo = 
                    new EnvInjectJobPropertyInfo(
                        oldEnvInjectJobPropertyInfo.getPropertiesFilePath(),
                        propertiesContent,
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

