package configurationslicing.executeshell;

import hudson.model.Descriptor;
import hudson.model.Descriptor.FormException;
import hudson.plugins.powershell.PowerShell;
import hudson.tasks.Builder;
import hudson.util.DescribableList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

/**
 * Slicer for the powershell builder
 *
 * @author Ryan Prince
 */
public class ExecutePowerShellSlicer extends AbstractBuildCommandSlicer<PowerShell> {

    public ExecutePowerShellSlicer() {
        super(new ExecutePowerShellSliceSpec());
    }
	@Override
	public void loadPluginDependencyClass() {
		// this is just to demonstrate that the PowerShell plugin is loaded
		PowerShell.class.getClass();
	}
    private static final PowerShell.DescriptorImpl POWERSHELL_DESCRIPTOR = new PowerShell.DescriptorImpl();
    
    public static class ExecutePowerShellSliceSpec extends AbstractBuildCommandSlicer.AbstractBuildCommandSliceSpec<PowerShell> {

        public String getName() {
            return "Execute PowerShell script";
        }

        public String getUrl() {
            return "executepowershellslice";
        }
        @SuppressWarnings({ "rawtypes" })
		@Override
        public PowerShell createBuilder(String command, List<PowerShell> existingBuilders, PowerShell oldBuilder) {
        	PowerShell powershell = null;
        	Constructor[] cons = PowerShell.class.getConstructors();
        	if (cons.length > 0) {
        		try {
            		if (!Modifier.isPublic(cons[0].getModifiers())) {
            			cons[0].setAccessible(true);
            		}
        			powershell = (PowerShell) cons[0].newInstance(command);
				} catch (Exception e) {
					// we'll try another way to get it
					powershell = null;
				}
        	}
        	if (powershell == null) {
	        	// this is an unfortunate workaround that is necessary due to the PowerShell constructor being private in certain versions
	        	StaplerRequest req = null;
	        	JSONObject formData = new JSONObject();
	        	formData.put("powershell", command);
	        	try {
					powershell = (PowerShell) POWERSHELL_DESCRIPTOR.newInstance(req, formData);
				} catch (FormException e) {
					powershell = null;
				}
        	}
        	return powershell;
        }
        @Override
        public PowerShell[] createBuilderArray(int len) {
        	return new PowerShell[len];
        }
        @Override
        public String getCommand(PowerShell builder) {
        	return builder.getCommand();
        }
        @Override
        public List<PowerShell> getConcreteBuildersList(
        		DescribableList<Builder, Descriptor<Builder>> buildersList) {
            return buildersList.getAll(PowerShell.class);
        }

    }
}

