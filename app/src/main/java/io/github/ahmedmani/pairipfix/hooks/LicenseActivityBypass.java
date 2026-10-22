package io.github.ahmedmani.pairipfix.hooks;


import android.app.Activity;
import android.os.Bundle;

import io.github.ahmedmani.pairipfix.BaseHook;

public class LicenseActivityBypass extends BaseHook {
    String LICENSE_ACTIVITY = "com.pairip.licensecheck.LicenseActivity";
    @Override
    public String getName() {
        return "LicenseActivityBypass";
    }

    @Override
    public void apply() {
        // If the error/paywall activity is launched anyway, dismiss it straight away. Only
        // blocking its close methods would leave the dialog on screen with a dead CLOSE button.
        hookAfter(LICENSE_ACTIVITY, "onCreate",
                param -> ((Activity) param.thisObject).finish(), Bundle.class);

        hookDoNothing(LICENSE_ACTIVITY, "closeApp");
        hookDoNothing(LICENSE_ACTIVITY, "exitApp");
        hookDoNothing(LICENSE_ACTIVITY, "showErrorDialog");
        hookDoNothing(LICENSE_ACTIVITY, "showPaywallAndCloseApp");
        hookDoNothing(LICENSE_ACTIVITY, "logAndShowErrorDialog", String.class);
        hookDoNothing(LICENSE_ACTIVITY, "logAndShowErrorDialog", String.class, Exception.class);
    }
}
