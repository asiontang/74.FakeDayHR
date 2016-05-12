package cn.asiontang.fake_dayhr;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import cn.asiontang.LogHelper;
import dalvik.system.PathClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedHookLoadPackage implements IXposedHookLoadPackage
{
    private static List<String> mHookPackageNameList;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable
    {
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
                    LogHelper.log("Error:在/data/app找不到APK文件" + packageName);
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
            //aClassMethod.invoke(aClass.newInstance(), mLoadPackageParam);
        }
        catch (final Exception e)
        {
            LogHelper.log(e.getCause() == null ? e : e.getCause());
        }
    }

    public void handleLoadPackage4release(final XC_LoadPackage.LoadPackageParam loadPackageParam)
    {
        if (mHookPackageNameList == null)
            mHookPackageNameList = Arrays.asList(""//
                    , "com.amap.location.demo"//
                    , "com.dayhr"//
            );
        if (mHookPackageNameList.indexOf(loadPackageParam.packageName) == -1)
            return;

        LogHelper.log(XposedHookLoadPackage.class.getPackage().getName() + " Hooking:\n        " + loadPackageParam.packageName);

        //为了获取 Context ，以便调用ContentProvider
        XC_MethodHook_onCreate x = new XC_MethodHook_onCreate(loadPackageParam);
        XC_MethodHook.Unhook hookMethod = XposedHelpers.findAndHookMethod("android.app.Application", loadPackageParam.classLoader, "onCreate", x);
        x.setUnHook(hookMethod);
    }
}




















