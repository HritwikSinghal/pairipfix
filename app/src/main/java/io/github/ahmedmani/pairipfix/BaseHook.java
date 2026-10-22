package io.github.ahmedmani.pairipfix;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public abstract  class BaseHook {
        protected ClassLoader classLoader;

        // Every helper call counts as one attempt, so the summary log reflects individual hooks
        // instead of reporting a whole group as applied when only one of its hooks landed.
        private int attempted = 0;
        private int applied = 0;

        public abstract String getName();
        public abstract void apply();

        public BaseHook init(ClassLoader _classLoader) {
            this.classLoader = _classLoader;
            return this;
        }

        public int getAttempted() {
            return attempted;
        }

        public int getApplied() {
            return applied;
        }

        protected boolean classExists(String className) {
            Class<?> clazz = XposedHelpers.findClassIfExists(className, this.classLoader);
            return !(clazz == null);
        }

        protected Class<?> findClass(String className) {
            return XposedHelpers.findClassIfExists(className, this.classLoader);
        }

        private boolean record(boolean success) {
            attempted++;
            if (success) {
                applied++;
            }
            return success;
        }

        private void logFailure(String what, Throwable t) {
            XposedBridge.log("[PairIPFix] " + getName() + ": failed to " + what + ": " + t);
        }

        protected boolean setStaticField(String className, String fieldName, Object value) {
            try {
                XposedHelpers.setStaticObjectField(findClass(className), fieldName, value);
                return record(true);
            } catch (Throwable t) {
                logFailure("set static field " + className + "." + fieldName + " = " + value, t);
                return record(false);
            }
        }

        protected boolean hookDoNothing(String className, String methodName, Object... paramTypes) {
            try {
                Object[] args = buildArgs(paramTypes, XC_MethodReplacement.DO_NOTHING);
                XposedHelpers.findAndHookMethod(className, this.classLoader, methodName, args);
                return record(true);
            } catch (Throwable t) {
                logFailure("hook " + className + "." + methodName, t);
                return record(false);
            }
        }

        // For methods whose parameter types are not known (they vary across pairip versions):
        // replaces every overload. Only safe for void methods, since DO_NOTHING returns null.
        protected boolean hookAllDoNothing(String className, String methodName) {
            try {
                Class<?> clazz = XposedHelpers.findClass(className, this.classLoader);
                boolean hooked = !XposedBridge.hookAllMethods(clazz, methodName, XC_MethodReplacement.DO_NOTHING).isEmpty();
                if (!hooked) {
                    XposedBridge.log("[PairIPFix] " + getName() + ": no method " + className + "." + methodName + " to hook");
                }
                return record(hooked);
            } catch (Throwable t) {
                logFailure("hook all " + className + "." + methodName, t);
                return record(false);
            }
        }

        protected boolean hookReturnConstant(String className, String methodName, Object returnValue, Object... paramTypes) {
            try {
                XC_MethodReplacement replacement = XC_MethodReplacement.returnConstant(returnValue);
                Object[] args = buildArgs(paramTypes, replacement);
                XposedHelpers.findAndHookMethod(className, this.classLoader, methodName, args);
                return record(true);
            } catch (Throwable t) {
                logFailure("hook " + className + "." + methodName, t);
                return record(false);
            }
        }

        protected boolean hookAfter(String className, String methodName, AfterHookCallback callback, Object... paramTypes) {
            try {
                XC_MethodHook hook = new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        callback.onAfter(param);
                    }
                };
                Object[] args = buildArgs(paramTypes, hook);
                XposedHelpers.findAndHookMethod(className, this.classLoader, methodName, args);
                return record(true);
            } catch (Throwable t) {
                logFailure("hook " + className + "." + methodName, t);
                return record(false);
            }
        }

        protected boolean hookReplace(String className, String methodName, ReplaceCallback callback, Object... paramTypes) {
            try {
                XC_MethodReplacement replacement = new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        return callback.onReplace(param);
                    }
                };
                Object[] args = buildArgs(paramTypes, replacement);
                XposedHelpers.findAndHookMethod(className, this.classLoader, methodName, args);
                return record(true);
            } catch (Throwable t) {
                logFailure("replace " + className + "." + methodName, t);
                return record(false);
            }
        }

        private Object[] buildArgs(Object[] paramTypes, XC_MethodHook callback) {
            List<Object> args = new ArrayList<>();
            for (Object type : paramTypes) {
                args.add(type);
            }
            args.add(callback);
            return args.toArray();
        }


        public interface AfterHookCallback {
            void onAfter(XC_MethodHook.MethodHookParam param);
        }

        public interface ReplaceCallback {
            Object onReplace(XC_MethodHook.MethodHookParam param) throws Throwable;
        }

}
