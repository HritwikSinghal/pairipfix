package io.github.ahmedmani.pairipfix.hooks;

import android.content.Context;
import io.github.ahmedmani.pairipfix.BaseHook;


public class SignatureBypass extends BaseHook {
    String SIGNATURE_CHECK = "com.pairip.SignatureCheck";
    @Override
    public String getName() {
        return "SignatureBypass";
    }

    @Override
    public void apply() {
        hookDoNothing(SIGNATURE_CHECK, "verifyIntegrity", Context.class);
        hookReturnConstant(SIGNATURE_CHECK, "verifySignatureMatches", true, String.class);
    }
}