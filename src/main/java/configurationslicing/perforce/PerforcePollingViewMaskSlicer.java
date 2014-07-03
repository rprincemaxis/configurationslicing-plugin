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
public class PerforcePollingViewMaskSlicer extends UnorderedStringSlicer<AbstractProject<?,?>> {

    public PerforcePollingViewMaskSlicer() {
        super(new PerforceViewMaskSliceSpec());
    }

    public static class PerforceViewMaskSliceSpec extends UnorderedStringSlicer.UnorderedStringSlicerSpec<AbstractProject<?,?>> {

        public static final String DISABLED = "(disabled)";

        public String getDefaultValueString() {
            return DISABLED;
        }

        @Override
        public String getName() {
            return "Perforce Polling View Mask";
        }

        @Override
        public String getUrl() {
            return "p4pollingviewmaskslicer";
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
                if(p4scm.isUseViewMask())
                {
                    String viewMask = p4scm.getViewMask();
                    if (null == viewMask) {
                        viewMask = "";
                    }
                    content.add(viewMask);
                }
            }
            if (content.isEmpty()) {
                content.add(DISABLED);
            }

            return content;
        }

        @SuppressWarnings("unchecked")
        public List<AbstractProject<?, ?>> getWorkDomain() {
            List<AbstractProject<?, ?>> list = new ArrayList<AbstractProject<?, ?>>();
            List<AbstractProject> temp = Hudson.getInstance().getAllItems(AbstractProject.class);
            for (AbstractProject p: temp) {
                if (p instanceof Project || p instanceof MatrixProject) {
                    // Only consider jobs with perforce as the SCM - we don't
                    //  want to make assumptions about switching SCM for a job
                    //  to update the view mask
                    SCM scm = p.getScm();
                    if (scm instanceof PerforceSCM) {
                        list.add(p);
                    }
                }
            }
            return list;
        }

        public boolean setValues(AbstractProject<?, ?> item, List<String> list) {
            SCM scm = item.getScm();
            String viewMask = list.iterator().next();

            if (scm instanceof PerforceSCM)
            {
                PerforceSCM p4scm = (PerforceSCM) scm;
                if (!DISABLED.equals(viewMask)) {
                    p4scm.setUseViewMask(true);
                    p4scm.setUseViewMaskForPolling(true);
                    p4scm.setUseViewMaskForChangeLog(true);
                    p4scm.setUseViewMaskForSyncing(false);
                    p4scm.setViewMask(viewMask);
                }
                else {
                    p4scm.setUseViewMask(false);
                }
            }

            return true;
        }

    }
}

