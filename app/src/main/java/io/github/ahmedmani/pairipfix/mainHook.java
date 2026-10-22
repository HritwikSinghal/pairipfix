package io.github.ahmedmani.pairipfix;

import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.ahmedmani.pairipfix.hooks.LicenseActivityBypass;
import io.github.ahmedmani.pairipfix.hooks.LicenseClientBypass;
import io.github.ahmedmani.pairipfix.hooks.LicenseResponseBypass;
import io.github.ahmedmani.pairipfix.hooks.SignatureBypass;

public class mainHook implements IXposedHookLoadPackage {

    private static final String[] PAIRIP_MARKERS = {
            "com.pairip.VMRunner",
            "com.pairip.licensecheck.LicenseClient",
            "com.pairip.licensecheck3.LicenseClientV3",
    };


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        XposedBridge.log("[PairIPFix] Module loaded for package: " + lpparam.packageName);

        // The license hooks only need the licensecheck classes; some apps ship those without
        // VMRunner, so requiring VMRunner alone would skip them.
        boolean hasPairip = false;
        for (String marker : PAIRIP_MARKERS) {
            if (XposedHelpers.findClassIfExists(marker, lpparam.classLoader) != null) {
                hasPairip = true;
                break;
            }
        }
        if (!hasPairip) {
            XposedBridge.log("[PairIPFix] App with package name " + lpparam.packageName + " does not have Pairip protection, or protection is not recognized, exiting.");
            return;
        }

        List<BaseHook> hooks = Arrays.asList(
                new SignatureBypass(),
                new LicenseClientBypass(),
                new LicenseActivityBypass(),
                new LicenseResponseBypass()
        );

        int successCount = 0;
        for (BaseHook hook : hooks) {
            try {
                boolean success = hook.init(lpparam.classLoader).apply();
                if (success) {
                    successCount++;
                    XposedBridge.log("[PairIPFix] " + hook.getName() + " applied successfully");
                }
            } catch (Throwable t) {
                XposedBridge.log("[PairIPFix] Error applying hook with name " + hook.getName());
            }
        }
        XposedBridge.log("[PairIPFix] " + successCount + "/" + hooks.size() + " hooks applied");

    }
}
