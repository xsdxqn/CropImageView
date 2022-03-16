package com.example.mycropimageview.widget.util;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：kk
 * 创建时间： 2018/3/2 11:22
 * 版本： [1.0, 2018/3/2]
 * 版权：
 * 描述： 权限申请工具类
 */

public class PermissionUtil {


    /**
     * 存储权限
     */
    public static int REQUESTCODE_PERMISSION_STORAGE = 1001;
    public static final int TYPE_PERMISSION_STORAGE = 1;
    public static final String[] PERMISSION_STORAGE = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    /**
     * 检查权限是否已经获取
     * 该方法目前存在问题：
     * 已知oppo a57、魅族mx4、锤子OS105无法判断权限，不排除其他型号手机也无法判断权限
     * 目前测试 nexus5、nexus6、华为荣耀、华为p10、小米可以正确判断权限
     *
     * @param context
     * @param permission 权限
     * @return
     */
    public static Boolean checkPermissionGranted(Context context, String permission) {
        boolean ret = true;
        try {
            if (getTargetSdkVersion(context) >= Build.VERSION_CODES.M) {
                ret = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
            } else {
                ret = PermissionChecker.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED;
            }
        } catch (Exception e) {
            ret = false;
        }

        return ret;
    }

    /**
     * 检查是否拥有指定的所有权限
     * 该方法目前存在问题：
     * 已知oppo a57、魅族mx4、锤子OS105无法判断权限，不排除其他型号手机也无法判断权限
     * 目前测试 nexus5、nexus6、华为荣耀、华为p10、小米可以正确判断权限
     *
     * @param context
     * @param permissions 所有权限的数组
     * @return
     */
    public static Boolean checkPermissionAllGranted(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (!checkPermissionGranted(context, permission)) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }


    /**
     * 获取指定的权限中未被授予的权限
     *
     * @param context
     * @param permissions 所有权限的数组
     * @return 未被授予的权限的集合
     */
    public static List<String> getPermissionNoGranted(Context context, String[] permissions) {
        List<String> nopermissions = new ArrayList<String>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                nopermissions.add(permission);
            }
        }
        return nopermissions;
    }

    /**
     * 开始申请所有权限：
     * 一次请求多个权限, 如果其他有权限是已经授予的将会自动忽略掉
     * 调用该方法后需要在对应activity中重新onRequestPermissionsResult方法
     * 部分手机onRequestPermissionsResult回调时permissions和grantResults可能为空，重写onRequestPermissionsResult方法时，判断是否授权成功不要用这两个参数，需要用请求时的权限进行判断
     * 目前已知问题：华为p10 弹出权限请求对话框后无法点击允许
     *
     * @param context     页面类型：activity 或者 fragment
     * @param permissions 所有权限数组
     * @param requestCode 申请权限的请求标识
     */
    public static void startRequestPermissions(Object context, String[] permissions, int requestCode) {
        if (context instanceof Activity) {
            ActivityCompat.requestPermissions((Activity) context, permissions, requestCode);
        } else if (context instanceof Fragment) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ((Fragment) context).requestPermissions(permissions, requestCode);
            } else {
                ActivityCompat.requestPermissions(((Fragment) context).getActivity(), permissions, requestCode);
            }
        } else if (context instanceof androidx.fragment.app.Fragment) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ((androidx.fragment.app.Fragment) context).requestPermissions(permissions, requestCode);
            } else {
                ActivityCompat.requestPermissions(((androidx.fragment.app.Fragment) context).getActivity(), permissions, requestCode);
            }
        }
    }

    /**
     * 申请单个权限
     *
     * @param activity
     * @param permission  要申请的权限
     * @param requestCode 申请权限的请求标识
     */
    public static void startRequestPermission(Activity activity, String permission, int requestCode) {
        startRequestPermissions(activity, new String[]{permission}, requestCode);
    }

    /**
     * 判断是否有必要向用户解释为什么要这项权限
     * 请求权限回调的时候调用该方法进行判断
     *
     * @param activity
     * @param permission
     * @return true：改权限请求的时候会弹出系统请求对话框
     * false：改权限请求的时候不会弹出系统请求对话框，需要弹出对话框让用户去应用界面设置
     */
    public static Boolean shouldShowRequestPermissionRationale(Activity activity, String permission) {
        return getTargetSdkVersion(activity) >= Build.VERSION_CODES.M && ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    /**
     * 跳转app权限请求界面
     *
     * @param activity
     * @param requestCode
     */
    public static void goAppSettingForResult(Activity activity, int requestCode) {
        activity.startActivityForResult(getAppSettingIntent(activity), requestCode);
    }

    /**
     * 跳转app权限请求界面
     *
     * @param context
     */
    public static void goAppSetting(Context context) {
        context.startActivity(getAppSettingIntent(context));
    }

    /**
     * 获取app系统设置页面intent
     *
     * @param context
     * @return
     */
    private static Intent getAppSettingIntent(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        return intent;
    }

    /**
     * 获取targetSdkVersion
     *
     * @param context
     * @return
     */
    private static int getTargetSdkVersion(Context context) {
        int targetSdkVersion = 0;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            targetSdkVersion = info.applicationInfo.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return targetSdkVersion;
    }

}
