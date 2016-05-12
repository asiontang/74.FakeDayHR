package cn.asiontang.fake_dayhr;

import android.content.Context;

import cn.asiontang.LogHelper;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class XC_MethodHook_onCreate extends XC_MethodHook
{
    private final XSharedPreferencesEx packagePreferences = new XSharedPreferencesEx();
    private final XSharedPreferencesEx mPreferences = new XSharedPreferencesEx();
    private ClassLoader mClassLoader;
    private Context mContext;
    private Unhook mUnHookMethod;

    public XC_MethodHook_onCreate(final XC_LoadPackage.LoadPackageParam loadPackageParam)
    {
        this.mClassLoader = loadPackageParam.classLoader;
    }

    @Override
    protected void beforeHookedMethod(final MethodHookParam param) throws Throwable
    {
        //执行一次Hook操作即可。
        this.mUnHookMethod.unhook();

        this.mContext = (Context) param.thisObject;
        this.mClassLoader = this.mContext.getClassLoader();

        try
        {
            final Class<?> aClass = XposedHelpers.findClass("com.amap.api.location.AMapLocation", this.mClassLoader);
            //for (final Method method : aClass.getMethods())
            //{
            //    LogHelper.log(method);
            //}

            LogHelper.log(this.mContext.getClass().getName() + ": found " + aClass.getName());
        }
        catch (final XposedHelpers.ClassNotFoundError e)
        {
            LogHelper.log(this.mContext.getClass().getName() + ": Error Context And ClassLoader Skip Hook.");
            return;
        }

        //APK被加固后，启动时，加载的仅仅是加固框架代码，之后框架代码onCreate执行完毕后，真正的程序代码才开始执行。
        try
        {
            LogHelper.log(this.mContext.getClass().getName() + ": Hook Start.");

            startHook();

            LogHelper.log(this.mContext.getClass().getName() + ": Hook End");
        }
        catch (final Exception e)
        {
            LogHelper.log(this.mContext.getClass().getName() + ": Hook Exception:");
            LogHelper.log(e);
        }
        catch (final Error e)
        {
            LogHelper.log(this.mContext.getClass().getName() + ": Hook Error:");
            LogHelper.log(e);
        }
        super.beforeHookedMethod(param);
    }

    private void findAndHookMethod(final String className, final ClassLoader classLoader, final XC_MethodHook parameterTypesAndCallback, final String methodName)
    {
        try
        {
            XposedHelpers.findAndHookMethod(className, classLoader, methodName, parameterTypesAndCallback);
        }
        catch (final Exception e)
        {
            LogHelper.log(this.mContext.getClass().getName() + ": findAndHookMethod Exception:");
            LogHelper.log(e);
        }
        catch (final NoSuchMethodError e)
        {
            LogHelper.log(this.mContext.getClass().getName() + ": findAndHookMethod NoSuchMethodError:" + e.getMessage());
        }
        catch (final Error e)
        {
            LogHelper.log(this.mContext.getClass().getName() + ": findAndHookMethod Error:");
            LogHelper.log(e);
        }
    }

    public void setUnHook(final Unhook unhook)
    {
        this.mUnHookMethod = unhook;
    }

    private void startHook()
    {
        this.packagePreferences.init(this.mContext, SharedPreferencesProvider.SHARED_PREFERENCES_FILE_NAME_MAIN);

        //当选择的 位置 索引为空时，程序进入“坐标收集模式”，会将定位到的位置记录在案，方便用户确定下次模拟哪个位置。
        final int selectedLocationIndex = this.packagePreferences.getInt("SelectedLocationIndex", -1);
        if (selectedLocationIndex == -1)
        {
            final int nowCount = this.packagePreferences.getInt("count", 1);

            //先判断原来的记录是否有效，无效的话，则覆盖掉。
            this.mPreferences.init(this.mContext, "" + nowCount);
            if (this.mPreferences.getString("getLatitude", null) != null && this.mPreferences.getString("getLongitude", null) != null)
            {
                //将采集到的 位置 索引 自动递增。
                final int newCount = nowCount + 1;
                this.packagePreferences.edit().putInt("count", newCount).commit();

                //记录递增的Count，总共记录的位置数量
                this.mPreferences.init(this.mContext, "" + newCount);
                this.mPreferences.reset();
            }
            this.mPreferences.edit().putBoolean("ReadOnly", false).commit();
        }
        else
        {
            //已经选择默认模拟的位置时，开启只读模式，所有读取的值都从默认Location里取。
            this.mPreferences.init(this.mContext, "" + selectedLocationIndex);
            this.mPreferences.edit().putBoolean("ReadOnly", true).commit();
        }
        final XC_MethodHook_string stringX = new XC_MethodHook_string(this.mPreferences);
        final XC_MethodHook_double doubleX = new XC_MethodHook_double(this.mPreferences);
        final XC_MethodHook_float floatX = new XC_MethodHook_float(this.mPreferences);
        final XC_MethodHook_int intX = new XC_MethodHook_int(this.mPreferences);
        final XC_MethodHook_bool boolX = new XC_MethodHook_bool(this.mPreferences);

        /**
         public double android.location.Location.getLatitude()
         public double android.location.Location.getLongitude()
         public double android.location.Location.getAltitude()

         public float android.location.Location.getAccuracy()
         public float android.location.Location.getBearing()
         public float android.location.Location.getSpeed()

         public long android.location.Location.getTime()
         public java.lang.String android.location.Location.getProvider()
         */

        findAndHookMethod("android.location.Location", this.mClassLoader, doubleX, "getLatitude");
        findAndHookMethod("android.location.Location", this.mClassLoader, doubleX, "getLongitude");
        findAndHookMethod("android.location.Location", this.mClassLoader, doubleX, "getAltitude");
        findAndHookMethod("android.location.Location", this.mClassLoader, floatX, "getAccuracy");
        findAndHookMethod("android.location.Location", this.mClassLoader, floatX, "getSpeed");
        findAndHookMethod("android.location.Location", this.mClassLoader, floatX, "getBearing");
        findAndHookMethod("android.location.Location", this.mClassLoader, floatX, "getProvider");

        //        findAndHookMethod("com.amap.api.location.AMapLocation", this.mClassLoader, doubleX, "getLatitude");//dayHr Diff
        //        findAndHookMethod("com.amap.api.location.AMapLocation", this.mClassLoader, doubleX, "getLongitude");//dayHr Diff
        //        findAndHookMethod("com.amap.api.location.AMapLocation", this.mClassLoader, doubleX, "getAltitude");//dayHr Diff
        //        findAndHookMethod("com.amap.api.location.AMapLocation", this.mClassLoader, floatX, "getAccuracy");//dayHr Diff
        //        findAndHookMethod("com.amap.api.location.AMapLocation", this.mClassLoader, floatX, "getSpeed");//dayHr Diff
        //        findAndHookMethod("com.amap.api.location.AMapLocation", this.mClassLoader, floatX, "getBearing");//dayHr Diff
        //        findAndHookMethod("com.amap.api.location.AMapLocation", this.mClassLoader, stringX, "getProvider");//dayHr Diff

        //        findAndHookMethod("com.amap.api.location.AMapLocation", this.mClassLoader, boolX, "isOffset");//dayHr No
        //        findAndHookMethod("com.amap.api.location.AMapLocation", this.mClassLoader, intX, "getLocationType");//dayHr No
        //        findAndHookMethod("com.amap.api.location.AMapLocation", this.mClassLoader, intX, "getErrorCode");//dayHr No
        //        findAndHookMethod("com.amap.api.location.AMapLocation", this.mClassLoader, intX, "getSatellites");//dayHr No
        //        findAndHookMethod("com.amap.api.location.AMapLocation", this.mClassLoader, stringX, "getLocationDetail");//dayHr No
        //        findAndHookMethod("com.amap.api.location.AMapLocation", this.mClassLoader, stringX, "getStreetNum");//dayHr No
        //        findAndHookMethod("com.amap.api.location.AMapLocation", this.mClassLoader, stringX, "getAoiName");//dayHr No
        //        findAndHookMethod("com.amap.api.location.AMapLocation", this.mClassLoader, stringX, "getErrorInfo");//dayHr No

        findAndHookMethod("com.amap.api.location.AMapLocation", this.mClassLoader, stringX, "getCountry");
        findAndHookMethod("com.amap.api.location.AMapLocation", this.mClassLoader, stringX, "getRoad");
        findAndHookMethod("com.amap.api.location.AMapLocation", this.mClassLoader, stringX, "getAddress");
        findAndHookMethod("com.amap.api.location.AMapLocation", this.mClassLoader, stringX, "getProvince");
        findAndHookMethod("com.amap.api.location.AMapLocation", this.mClassLoader, stringX, "getCity");
        findAndHookMethod("com.amap.api.location.AMapLocation", this.mClassLoader, stringX, "getDistrict");
        findAndHookMethod("com.amap.api.location.AMapLocation", this.mClassLoader, stringX, "getCityCode");
        findAndHookMethod("com.amap.api.location.AMapLocation", this.mClassLoader, stringX, "getAdCode");
        findAndHookMethod("com.amap.api.location.AMapLocation", this.mClassLoader, stringX, "getPoiName");
        findAndHookMethod("com.amap.api.location.AMapLocation", this.mClassLoader, stringX, "getStreet");
    }
}
