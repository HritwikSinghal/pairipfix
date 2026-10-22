package io.github.ahmedmani.pairipfix.hooks;

import android.os.Bundle;
import java.security.PublicKey;

import io.github.ahmedmani.pairipfix.BaseHook;

public class LicenseResponseBypass extends BaseHook {
    String LICENSE_RESPONSE_HELPER = "com.pairip.licensecheck.LicenseResponseHelper";
    String OLD_RESPONSE_VALIDATOR = "com.pairip.licensecheck.ResponseValidator";
    String RESPONSE_VALIDATOR_V3 = "com.pairip.licensecheck3.ResponseValidator";
    @Override
    public String getName() {
        return "LicenseResponseBypass";
    }

    @Override
    public void apply() {

        hookDoNothing(LICENSE_RESPONSE_HELPER, "validateResponse", Bundle.class, String.class);
        hookReturnConstant(LICENSE_RESPONSE_HELPER, "getRepeatedCheckMetadata", null, Bundle.class);
        hookDoNothing(LICENSE_RESPONSE_HELPER, "verifySignature", String.class, String.class, String.class, PublicKey.class);

        if (classExists(OLD_RESPONSE_VALIDATOR)) {
            hookDoNothing(OLD_RESPONSE_VALIDATOR, "validateResponse", Bundle.class, String.class);
            hookDoNothing(OLD_RESPONSE_VALIDATOR, "verifySignature", String.class, String.class, String.class, PublicKey.class);
        }

        if (classExists(RESPONSE_VALIDATOR_V3)) {
            hookDoNothing(RESPONSE_VALIDATOR_V3, "validateResponse", Bundle.class, String.class);
        }

    }
}