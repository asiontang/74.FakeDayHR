package cn.asiontang.fake_dayhr;

import cn.asiontang.LogHelper;
import de.robv.android.xposed.XC_MethodHook;

class XC_MethodHook_bool extends XC_MethodHook
{
    private final XSharedPreferencesEx mPreferences;

    public XC_MethodHook_bool(final XSharedPreferencesEx preferences)
    {
        this.mPreferences = preferences;
    }

    @Override
    protected void afterHookedMethod(final MethodHookParam param) throws Throwable
    {
        if (this.mPreferences.getBoolean("ReadOnly", false))
            param.setResult(this.mPreferences.getBoolean(param.method.getName(), (Boolean) param.getResult()));
        else if (param.getResult() == null || param.getResult() instanceof Boolean)
            this.mPreferences.edit().putBoolean(param.method.getName(), (Boolean) param.getResult()).apply();
        else
            LogHelper.log("ErrorType:" + param.method.getName() + " | " + param.getResult() + "|" + param.getResult().getClass().getName());
    }
}
