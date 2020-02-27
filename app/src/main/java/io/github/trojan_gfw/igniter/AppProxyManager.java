package io.github.trojan_gfw.igniter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppProxyManager {

    public static AppProxyManager Instance;
    private static final String PROXY_APPS = "PROXY_APPS";
    private Context mContext;

    public List<AppInfo> mlistAppInfo = new ArrayList<AppInfo>();
    public List<AppInfo> proxyAppInfo = new ArrayList<AppInfo>();

    public AppProxyManager(Context context){
        this.mContext = context;
        Instance = this;
        queryInstalledAppInfo();
    }

    public void removeProxyApp(String pkg){
        for (AppInfo app : this.proxyAppInfo) {
            if (app.getPkgName().equals(pkg)){
                proxyAppInfo.remove(app);
                break;
            }
        }
        writeProxyAppsList();
    }

    public void addProxyApp(String pkg){
        for (AppInfo app : this.mlistAppInfo) {
            if (app.getPkgName().equals(pkg)){
                proxyAppInfo.add(app);
                break;
            }
        }
        writeProxyAppsList();
    }

    public boolean isAppProxy(String pkg){
        for (AppInfo app : this.proxyAppInfo) {
            if (app.getPkgName().equals(pkg)){
                return true;
            }
        }
        return false;
    }

    public boolean isMlistApp(String pkg){
        for (AppInfo app : this.mlistAppInfo) {
            if (app.getPkgName().equals(pkg)){
                return true;
            }
        }
        return false;
    }

    private void queryInstalledAppInfo() {
        PackageManager pm = mContext.getPackageManager(); // 获得PackageManager对象
        //android.intent.action.MAIN：决定应用的入口Activity，也就是我们启动应用时首先显示哪一个Activity
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        //android.intent.category.LAUNCHER：表示activity应该被列入系统的启动器(launcher)(允许用户启动它)。Launcher是安卓系统中的桌面启动器，是桌面UI的统称。
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
        Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(pm));
        if (AppProxyManager.Instance.mlistAppInfo != null) {
            AppProxyManager.Instance.mlistAppInfo.clear();
            for (ResolveInfo reInfo : resolveInfos) {
                String pkgName = reInfo.activityInfo.packageName; // 获得应用程序的包名
                if (!mContext.getPackageName().equals(pkgName)){
                    String appLabel = (String) reInfo.loadLabel(pm); // 获得应用程序的Label
                    Drawable icon = reInfo.loadIcon(pm); // 获得应用程序图标
                    AppInfo appInfo = new AppInfo();
                    appInfo.setAppLabel(appLabel);
                    appInfo.setPkgName(pkgName);
                    appInfo.setAppIcon(icon);
                    AppProxyManager.Instance.mlistAppInfo.add(appInfo);
                }
            }
        }
    }


    public void writeProxyAppsList() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < proxyAppInfo.size() ; i++){
                JSONObject object = new JSONObject();
                AppInfo appInfo = proxyAppInfo.get(i);
                object.put("label", appInfo.getAppLabel());
                object.put("pkg", appInfo.getPkgName());
                jsonArray.put(object);
            }
            Globals.getTrojanConfigInstance().setBypassAppList(jsonArray);
            TrojanHelper.WriteTrojanConfig(
                    Globals.getTrojanConfigInstance(),
                    Globals.getTrojanConfigPath()
            );
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
