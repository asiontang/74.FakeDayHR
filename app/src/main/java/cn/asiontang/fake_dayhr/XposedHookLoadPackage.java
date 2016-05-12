package cn.asiontang.fake_dayhr;

import android.content.Context;

import java.io.File;
import java.lang.reflect.Method;

import dalvik.system.PathClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedHookLoadPackage implements IXposedHookLoadPackage
{
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable
    {
        if (!"com.amap.location.demo".equals(loadPackageParam.packageName))
            return;
        try
        {
            //在发布时，直接调用即可。
            if (!BuildConfig.DEBUG)
            {
                handleLoadPackage4release(loadPackageParam);
                return;
            }
            //在调试模式为了不频繁重启，使用反射的方式调用自身的指定函数。

            /*【方法2】*/
            final String packageName = XposedHookLoadPackage.class.getPackage().getName();
            String filePath = String.format("/data/app/%s-%s.apk", packageName, 1);
            if (!new File(filePath).exists())
            {
                filePath = String.format("/data/app/%s-%s.apk", packageName, 2);
                if (!new File(filePath).exists())
                {
                    XposedBridge.log("Error:在/data/app找不到APK文件" + packageName);
                    return;
                }
            }
            final PathClassLoader pathClassLoader = new PathClassLoader(filePath, ClassLoader.getSystemClassLoader());
            final Class<?> aClass = Class.forName(packageName + "." + XposedHookLoadPackage.class.getSimpleName(), true, pathClassLoader);
            final Method aClassMethod = aClass.getMethod("handleLoadPackage4release", XC_LoadPackage.LoadPackageParam.class);
            aClassMethod.invoke(aClass.newInstance(), loadPackageParam);

            /*【方法1】：无法达到效果*/
            //final Class<MainActivity> pathClassLoader = MainActivity.class;
            //final Class<?> aClass = Class.forName(pathClassLoader.getPackage().getName() + "." + XposedHookLoadPackage.class.getSimpleName(), true, pathClassLoader.getClassLoader());
            //final Method aClassMethod = aClass.getMethod("handleLoadPackage4release", XC_LoadPackage.LoadPackageParam.class);
            //aClassMethod.invoke(aClass.newInstance(), loadPackageParam);
        }
        catch (final Exception e)
        {
            XposedBridge.log(e.getCause() == null ? e : e.getCause());
        }
    }

    public void handleLoadPackage4release(final XC_LoadPackage.LoadPackageParam loadPackageParam)
    {
        final XSharedPreferencesEx packagePreferences = new XSharedPreferencesEx();

        final XSharedPreferencesEx mPreferences = new XSharedPreferencesEx();

        //为了获取 Context ，以便调用ContentProvider
        XposedHelpers.findAndHookMethod("android.app.Application", loadPackageParam.classLoader, "onCreate", new XC_MethodHook()
        {
            @Override
            protected void beforeHookedMethod(final MethodHookParam param) throws Throwable
            {
                final Context context = (Context) param.thisObject;

                packagePreferences.init(context, SharedPreferencesProvider.SHARED_PREFERENCES_FILE_NAME_MAIN);

                //当选择的 位置 索引为空时，程序进入“坐标收集模式”，会将定位到的位置记录在案，方便用户确定下次模拟哪个位置。
                final int selectedLocationIndex = packagePreferences.getInt("SelectedLocationIndex", -1);

                //将采集到的 位置 索引 自动递增。
                if (selectedLocationIndex == -1)
                {
                    final int newCount = packagePreferences.getInt("count", 0) + 1;
                    packagePreferences.edit().putInt("count", newCount).commit();

                    //记录递增的Count，总共记录的位置数量
                    mPreferences.init(context, "" + newCount);
                    mPreferences.edit().putBoolean("ReadOnly", false).commit();
                }
                else
                {
                    //已经选择默认模拟的位置时，开启只读模式，所有读取的值都从默认Location里取。
                    mPreferences.init(context, "" + selectedLocationIndex);
                    mPreferences.edit().putBoolean("ReadOnly", true).commit();
                }
                super.beforeHookedMethod(param);
            }
        });

        final stringX stringX = new stringX(mPreferences);
        final doubleX doubleX = new doubleX(mPreferences);
        final floatX floatX = new floatX(mPreferences);
        final intX intX = new intX(mPreferences);
        final boolX boolX = new boolX(mPreferences);

        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation", loadPackageParam.classLoader, "isOffset", boolX);

        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation", loadPackageParam.classLoader, "getLatitude", doubleX);
        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation", loadPackageParam.classLoader, "getLongitude", doubleX);
        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation", loadPackageParam.classLoader, "getAltitude", doubleX);

        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation", loadPackageParam.classLoader, "getAccuracy", floatX);
        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation", loadPackageParam.classLoader, "getSpeed", floatX);
        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation", loadPackageParam.classLoader, "getBearing", floatX);

        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation", loadPackageParam.classLoader, "getLocationType", intX);
        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation", loadPackageParam.classLoader, "getErrorCode", intX);
        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation", loadPackageParam.classLoader, "getSatellites", intX);

        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation", loadPackageParam.classLoader, "getLocationDetail", stringX);
        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation", loadPackageParam.classLoader, "getErrorInfo", stringX);
        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation", loadPackageParam.classLoader, "getCountry", stringX);
        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation", loadPackageParam.classLoader, "getRoad", stringX);
        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation", loadPackageParam.classLoader, "getAddress", stringX);
        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation", loadPackageParam.classLoader, "getProvince", stringX);
        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation", loadPackageParam.classLoader, "getCity", stringX);
        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation", loadPackageParam.classLoader, "getDistrict", stringX);
        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation", loadPackageParam.classLoader, "getCityCode", stringX);
        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation", loadPackageParam.classLoader, "getAdCode", stringX);
        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation", loadPackageParam.classLoader, "getPoiName", stringX);
        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation", loadPackageParam.classLoader, "getStreet", stringX);
        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation", loadPackageParam.classLoader, "getStreetNum", stringX);
        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation", loadPackageParam.classLoader, "getAoiName", stringX);
        XposedHelpers.findAndHookMethod("com.amap.api.location.AMapLocation", loadPackageParam.classLoader, "getProvider", stringX);
    }

    class stringX extends XC_MethodHook
    {
        private final XSharedPreferencesEx mPreferences;

        public stringX(final XSharedPreferencesEx preferences)
        {
            this.mPreferences = preferences;
        }

        @Override
        protected void afterHookedMethod(final MethodHookParam param) throws Throwable
        {
            if (this.mPreferences.getBoolean("ReadOnly", false))
                param.setResult(this.mPreferences.getString(param.method.getName(), (String) param.getResult()));
            else if (param.getResult() == null || param.getResult() instanceof String)
                this.mPreferences.edit().putString(param.method.getName(), (String) param.getResult()).apply();
            else
                XposedBridge.log("ErrorType:" + param.method.getName() + " | " + param.getResult() + "|" + param.getResult().getClass().getName());
        }
    }

    private class boolX extends XC_MethodHook
    {
        private final XSharedPreferencesEx mPreferences;

        public boolX(final XSharedPreferencesEx preferences)
        {
            this.mPreferences = preferences;
        }

        @Override
        protected void afterHookedMethod(final XC_MethodHook.MethodHookParam param) throws Throwable
        {
            if (this.mPreferences.getBoolean("ReadOnly", false))
                param.setResult(this.mPreferences.getBoolean(param.method.getName(), (Boolean) param.getResult()));
            else if (param.getResult() == null || param.getResult() instanceof Boolean)
                this.mPreferences.edit().putBoolean(param.method.getName(), (Boolean) param.getResult()).apply();
            else
                XposedBridge.log("ErrorType:" + param.method.getName() + " | " + param.getResult() + "|" + param.getResult().getClass().getName());
        }
    }

    private class intX extends XC_MethodHook
    {
        private final XSharedPreferencesEx mPreferences;

        public intX(final XSharedPreferencesEx preferences)
        {
            this.mPreferences = preferences;
        }

        @Override
        protected void afterHookedMethod(final XC_MethodHook.MethodHookParam param) throws Throwable
        {
            if (this.mPreferences.getBoolean("ReadOnly", false))
                param.setResult(this.mPreferences.getInt(param.method.getName(), (Integer) param.getResult()));
            else if (param.getResult() == null || param.getResult() instanceof Integer)
                this.mPreferences.edit().putInt(param.method.getName(), (Integer) param.getResult()).apply();
            else
                XposedBridge.log("ErrorType:" + param.method.getName() + " | " + param.getResult() + "|" + param.getResult().getClass().getName());
        }
    }

    private class floatX extends XC_MethodHook
    {
        private final XSharedPreferencesEx mPreferences;

        public floatX(final XSharedPreferencesEx preferences)
        {
            this.mPreferences = preferences;
        }

        @Override
        protected void afterHookedMethod(final XC_MethodHook.MethodHookParam param) throws Throwable
        {
            if (this.mPreferences.getBoolean("ReadOnly", false))
                param.setResult(this.mPreferences.getFloat(param.method.getName(), (Float) param.getResult()));
            else if (param.getResult() == null || param.getResult() instanceof Float)
                this.mPreferences.edit().putFloat(param.method.getName(), (Float) param.getResult()).apply();
            else
                XposedBridge.log("ErrorType:" + param.method.getName() + " | " + param.getResult() + "|" + param.getResult().getClass().getName());
        }
    }

    private class doubleX extends XC_MethodHook
    {
        private final XSharedPreferencesEx mPreferences;

        public doubleX(final XSharedPreferencesEx preferences)
        {
            this.mPreferences = preferences;
        }

        @Override
        protected void afterHookedMethod(final XC_MethodHook.MethodHookParam param) throws Throwable
        {
            if (this.mPreferences.getBoolean("ReadOnly", false))
                param.setResult(this.mPreferences.getDouble(param.method.getName(), (Double) param.getResult()));
            else if (param.getResult() == null || param.getResult() instanceof Double)
                this.mPreferences.edit().putDouble(param.method.getName(), (Double) param.getResult()).apply();
            else
                XposedBridge.log("ErrorType:" + param.method.getName() + " | " + param.getResult() + "|" + param.getResult().getClass().getName());
        }
    }
}




















