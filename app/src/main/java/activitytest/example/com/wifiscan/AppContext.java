package activitytest.example.com.wifiscan;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class AppContext extends Application {

    public static final String LICENSE = "<h5><font color='#009966'><b>Lanmitm</b> v0.9 alpha by oinux</h5><br/>";
    private static Context mContext;
    private static String[] TOOLS_FILENAME = { "arpspoof", "tcpdump" };
    private static String[] TOOLS_COMMAND = { "chmod 755 [ROOT_PATH]/arpspoof", "chmod 755 [ROOT_PATH]/tcpdump" };
    private static SharedPreferences preferences = null;

    private static InetAddress mInetAddress;
    private static int int_gateway;
    private static int int_ip;
    private static int int_net_mask;
    private static LanHost mTarget = null;
    private static String gatewayMac;
    public static boolean isHttpserverRunning = false;
    public static boolean isHijackRunning = false;
    public static boolean isTcpdumpRunning = false;
    public static boolean isInjectRunning = false;
    public static boolean isKillRunning = false;

    private static String mStoragePath = null;
    private static StringBuilder serverLog;

    @Override
    public void onCreate() {

        mContext = this;



        mStoragePath = Environment.getExternalStorageDirectory().toString();

        preferences = getSharedPreferences("app", Context.MODE_PRIVATE);
        super.onCreate();
    }

    public static void initWifiInfo() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        int_ip = wifiManager.getDhcpInfo().ipAddress;
        int_net_mask = wifiManager.getDhcpInfo().netmask;

        if (int_net_mask == 0) {
            int_net_mask = (0 << 24) + (0xff << 16) + (0xff << 8) + 0xff ;
        }
        int_gateway = wifiManager.getDhcpInfo().gateway;
        try {
            mInetAddress = InetAddress.getByName(NetworkUtils.netfromInt(int_ip));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        gatewayMac = wifiManager.getConnectionInfo().getBSSID().replace('-', ':');
    }

    public static StringBuilder getServerLog() {
        if (serverLog == null)
            serverLog = new StringBuilder();
        return serverLog;
    }

    public static String getStoragePath() {
        return mStoragePath;
    }

    public static LanHost getTarget() {
        return mTarget;
    }

    public static void setTarget(LanHost target) {
        mTarget = target;
    }

    public static InetAddress getInetAddress() {
        return mInetAddress;
    }

    public static int getIntGateway() {
        return int_gateway;
    }

    public static String getGateway() {
        return NetworkUtils.netfromInt(int_gateway);
    }

    public static String getGatewayMac() {
        return gatewayMac;
    }
    public static int getHostCount() {
        return NetworkUtils.countHost(int_net_mask);
    }
    public static int getIntIp() {
        return int_gateway;
    }

    public static int getIntNetMask() {
        return int_net_mask;
    }
    public static Context getContext() {
        return mContext;
    }
}


