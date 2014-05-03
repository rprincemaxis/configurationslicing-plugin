package configurationslicing;

import hudson.plugins.powershell.PowerShell;
import junit.framework.TestCase;
import configurationslicing.executeshell.ExecutePowerShellSlicer;
import static junit.framework.Assert.assertNotNull;

public class PowerShellTest extends TestCase {

	public void testCreatePowerShell() throws Exception {
		// this is failing at runtime for a method not found problem with "newInstance"
		PowerShell p = new ExecutePowerShellSlicer.ExecutePowerShellSliceSpec().createBuilder("hello", null, null);
		assertNotNull(p);
	}
}
