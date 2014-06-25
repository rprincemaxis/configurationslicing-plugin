package configurationslicing.perforce;

import hudson.matrix.MatrixProject;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.Project;
import hudson.scm.SCM;
import hudson.plugins.perforce.PerforceSCM;

import java.util.ArrayList;
import java.util.List;

import configurationslicing.UnorderedStringSlicer;
import hudson.Extension;

@Extension
public class PerforceClientNameSlicer extends UnorderedStringSlicer<AbstractProject<?,?>> {

    public PerforceClientNameSlicer() {
        super(new PerforceViewSpecSliceSpec());
    }

    public static class PerforceViewSpecSliceSpec extends UnorderedStringSlicer.UnorderedStringSlicerSpec<AbstractProject<?,?>> {

        public static final String NOTHING = "(nothing)";

        public String getDefaultValueString() {
            return NOTHING;
        }

        @Override
        public String getName() {
            return "Perforce Client Name";
        }

        @Override
        public String getUrl() {
            return "p4clientnameslicer";
        }


        public String getName(AbstractProject<?, ?> item) {
            return item.getFullName();
        }

        public List<String> getValues(AbstractProject<?, ?> item) {
            List<String> content = new ArrayList<String>();
            SCM scm = item.getScm();

            if (scm instanceof PerforceSCM)
            {
                PerforceSCM p4scm = (PerforceSCM) scm;
                String clientName = p4scm.getP4Client();
                content.add(clientName);
            }
            if (content.isEmpty()) {
                content.add(NOTHING);
            }

            return content;
        }

        @SuppressWarnings("unchecked")
        public List<AbstractProject<?, ?>> getWorkDomain() {
            List<AbstractProject<?, ?>> list = new ArrayList<AbstractProject<?, ?>>();
            List<AbstractProject> temp = Hudson.getInstance().getAllItems(AbstractProject.class);
            for (AbstractProject p: temp) {
                if (p instanceof Project || p instanceof MatrixProject) {
                    list.add(p);
                }
            }
            return list;
        }

        public boolean setValues(AbstractProject<?, ?> item, List<String> list) {
            SCM scm = item.getScm();
            String clientName = list.iterator().next();

            if (scm instanceof PerforceSCM && !NOTHING.equals(clientName))
            {
                PerforceSCM p4scm = (PerforceSCM) scm;
                p4scm.setP4Client(clientName);
            }

            return true;
        }

    }
}

