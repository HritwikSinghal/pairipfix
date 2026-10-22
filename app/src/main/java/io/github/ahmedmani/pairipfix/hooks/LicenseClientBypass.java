package io.github.ahmedmani.pairipfix.hooks;

import android.os.Bundle;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import io.github.ahmedmani.pairipfix.BaseHook;

public class LicenseClientBypass extends BaseHook {
    String LICENSE_CLIENT = "com.pairip.licensecheck.LicenseClient";
    String LICENSE_CLIENT_V3 = "com.pairip.licensecheck3.LicenseClientV3";
    String LICENSE_CHECK_STATE = "com.pairip.licensecheck.LicenseClient$LicenseCheckState";
    @Override
    public String getName() {
        return "LicenseClientBypass";
    }

    @Override
    public void apply() {
        hookDoNothing(LICENSE_CLIENT, "initializeLicenseCheck");
        hookReturnConstant(LICENSE_CLIENT, "performLocalInstallerCheck", true);
        // Backstop for checks that start some other way: when the licensing service cannot be
        // bound (e.g. Play Store disabled), retryOrThrow -> handleError -> startErrorDialogActivity
        // is what shows the "Something went wrong" dialog.
        hookAllDoNothing(LICENSE_CLIENT, "handleError");
        hookAllDoNothing(LICENSE_CLIENT, "startErrorDialogActivity");

        Class<?> stateClass = findClass(LICENSE_CHECK_STATE);
        if (stateClass != null) {
            // A pairip build without FULL_CHECK_OK must not abort the hooks that follow.
            try {
                Object fullCheckOk = XposedHelpers.getStaticObjectField(stateClass, "FULL_CHECK_OK");
                setStaticField(LICENSE_CLIENT, "licenseCheckState", fullCheckOk);
            } catch (Throwable t) {
                XposedBridge.log("[PairIPFix] " + getName() + ": no FULL_CHECK_OK in " + LICENSE_CHECK_STATE + ": " + t);
            }
        }

        if (classExists(LICENSE_CLIENT_V3)) {
            hookDoNothing(LICENSE_CLIENT_V3, "processResponse", int.class, Bundle.class);
        }
    }
}
