package activitytest.example.com.wifiscan;

import android.content.Intent;
import android.graphics.Color;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.net.URL;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Activity_detection extends AppCompatActivity {

    private Button btn_getWifiInfo;
    private Button btn_Dns;
    //private Button btn_Arp;
    private TextView Tv_getInfoList1;
    private TextView Tv_getInfoList2;
    private TextView Tv;
    private TextView Tv1;
    private  String getwayIpS;
    private Handler uiHandler = new Handler();
    private String arp1=null;
    private String dns1=null;
    private WifiManager wm1;
    private WifiInfo wifiInfo1;
    //protected WifiAdmin mWifiAdmin;

    String mSsid;
    String mBssid;
    String mMac;
    String mUnit;
    int speed;
    int strength;
    String Ipadd;
    String CipherT;
    boolean isConnected;

    String wifiinformation = "正在连接的WiFi信息: \n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);
        Intent intent = getIntent();
        isConnected = intent.getBooleanExtra("isConnected",false);
        mSsid = intent.getStringExtra("mSsid");
        mBssid = intent.getStringExtra("mBssid");
        Ipadd = intent.getStringExtra("Ipadd");
        CipherT = intent.getStringExtra("CipherT");
        mMac = intent.getStringExtra("mMac");
        speed = intent.getIntExtra("Speed", 0);
        mUnit = intent.getStringExtra("mUnit");
        strength = intent.getIntExtra("Strength", 0);


        Tv_getInfoList1 = findViewById(R.id.textView4);
        Tv_getInfoList2 = findViewById(R.id.textView6);
        btn_getWifiInfo = findViewById(R.id.button2);
        btn_Dns = findViewById(R.id.button3);
        //btn_Arp = findViewById(R.id.button5);
        Tv = findViewById(R.id.textView3);
        Tv1 = findViewById(R.id.textView5);
        wm1 = (WifiManager) getSystemService(WIFI_SERVICE);
        wifiInfo1 = wm1.getConnectionInfo();

        setListener();

        if (isConnected) {
            Tv.setText("WiFi已连接！");
            btn_getWifiInfo.setEnabled(true);
            //btn_Arp.setEnabled(true);
            btn_Dns.setEnabled(true);
        } else {
            Tv.setText("请先连接WiFi！");
            btn_getWifiInfo.setEnabled(false);
            //btn_Arp.setEnabled(false);
            btn_Dns.setEnabled(false);
        }
    }

    private void showDialog() {

        new SweetAlertDialog(Activity_detection.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("警告")
                .setContentText("当前所连WiFi存在安全威胁，建议更换WiFi连接或关闭WiFi！")
                .setCancelText("知道了")
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                    }
                })
                .show();
//        AlertDialog.Builder builder=new AlertDialog.Builder(this);
//        //builder.setIcon(R.drawable.picture);
//        builder.setTitle("警告");
//        builder.setMessage("当前所连WiFi存在安全威胁，建议更换WiFi连接或关闭WiFi！");
//        builder.setPositiveButton("我知道了",
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//
//                    }
//                });
//        AlertDialog dialog=builder.create();
//        dialog.show();
    }

    private void setListener() {
        btn_getWifiInfo.setOnClickListener(listener);
        btn_Dns.setOnClickListener(listener);
        //btn_Arp.setOnClickListener(listener);
    }
    View.OnClickListener listener = new View.OnClickListener()
    {
        @SuppressWarnings("static-access")
        @Override
        public void onClick(View v){
            switch (v.getId()) {
                case R.id.button3://5
                    if(wm1.isWifiEnabled())
                    {
                        ARP arp=new ARP();
                        DhcpInfo di = wm1.getDhcpInfo();
                        long getewayIpL=di.gateway;
                        getwayIpS=arp.long2ip(getewayIpL);//网关IP
                        long netmaskIpL=di.netmask;
                        String netmaskIpS=arp.long2ip(netmaskIpL);//子网地址
                        //discover(netmaskIpS);
                        arp.startDiscovery();
                        String mac1 = wifiInfo1.getBSSID(); //BSSID
                        String mac2 = arp.getMacFromArpCache(getwayIpS);
                        int n=arp.getnum(getwayIpS);
                        int m=arp.getsum();
                        DNS2 test=new DNS2();
                        Thread  dns;
                        dns=new Thread(test);
                        dns.setName("DNS");
                        dns.start();
                    }
                    else
                        Tv.setText("wifi未开启");
                    break;
                case R.id.button2:
                    if(wm1.isWifiEnabled()) {
                        ARP arp=new ARP();
                        DhcpInfo di = wm1.getDhcpInfo();
                        long getewayIpL=di.gateway;
                        getwayIpS=arp.long2ip(getewayIpL);//网关IP
                        long netmaskIpL=di.netmask;
                        String netmaskIpS=arp.long2ip(netmaskIpL);//子网地址
                        String SSID = wifiInfo1.getSSID();
                        String bssid = wifiInfo1.getBSSID();//The address of the access point.
                        int netWorkId = wifiInfo1.getNetworkId();//the network ID, or -1 if there is no currently connected network
                        int speed = wifiInfo1.getLinkSpeed();

                        String text = "网络详情:";
                        Tv1.setText(text);
                        Tv1.setTextColor(Color.parseColor("#ff5e9cff"));

                        String text1 = "\nssid：" + "\nbssid：" +  "\n分配的IP地址："+ "\n加密方式："+"\n网络号："+"\n连接速度："  + "\n强度：" ;
                        String text2 = "\n"+mSsid+"\n"+bssid+"\n"+String.valueOf(Ipadd)+"\n"+CipherT+"\n"+netmaskIpS+"\n"+String.valueOf(speed)+String.valueOf(mUnit)+"\n"+ strength;
                        Tv_getInfoList1.setText(text2);
                        Tv_getInfoList1.setTextColor(Color.parseColor("#ff5e9cff"));
                        Tv_getInfoList2.setText(text1);
                        Tv_getInfoList2.setTextColor(Color.parseColor("#ff5e9cff"));

                        //textView1.setText("目前连入的wifi是：" + SSID + "\n接入点的BSSID为：" + bssid +"\n网关IP："+ getwayIpS+"\n网络号："+ netmaskIpS + "\n网络ID：" + netWorkId + "\n链接速度：" + speed);
                    }
                    else {
                        String text = "当前未连接WiFi";
                        Tv1.setText(text);
                        Tv1.setTextColor(Color.parseColor("#FFC125"));
                    }
                    break;
                default:
                    break;
            }
        }
    };
    public class DNS2 implements Runnable   {
        public boolean DNSjudge=false;
        public  String pan=null;

        public  synchronized   boolean getDNSjudge()
        {

            return DNSjudge;
        }

        public  synchronized  String getpan()
        {

            return pan;
        }

        public synchronized void run() {
            String path = "https://www.baidu.com";
            String path1="http://tools.3g.qq.com/wifi/ssl";
            try {
                URL url,url1 ;
                int code,code1 ;
                HttpURLConnection connection,connection1;
                String str;
                do {
                    url = new URL(path1);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(false);//设置是否向HttpURLConnection输出
                    connection.setDoInput(true);//设置是否从httpUrlConnection读入
                    connection.setRequestMethod("GET");//设置请求方式
                    connection.setUseCaches(false);//设置是否使用缓存
                    connection.setInstanceFollowRedirects(true);//设置此 HttpURLConnection 实例是否应该自动执行 HTTP 重定向
                    connection.setConnectTimeout(3000);//设置超时
                    connection.connect();//链接
                    code = connection.getResponseCode();//设置返回的类型
                    str = connection.getHeaderField("Location");
                }while(code==502||code==404);
                if(code==301||code==302)
                {
                    if(str.equals("https://www.baidu.com")||str.equals("http://www.baidu.com")) {
                        try {
                            do {
                                url1 = new URL(path);
                                connection1 = (HttpURLConnection) url1.openConnection();
                                connection1.setDoOutput(false);//设置是否向HttpURLConnection输出
                                connection1.setDoInput(true);//设置是否从httpUrlConnection读入
                                connection1.setRequestMethod("GET");//设置请求方式
                                connection1.setUseCaches(false);//设置是否使用缓存
                                connection1.setInstanceFollowRedirects(true);//设置此 HttpURLConnection 实例是否应该自动执行 HTTP 重定向
                                connection1.setConnectTimeout(3000);//设置超时
                                connection1.connect();//链接
                                code1 = connection1.getResponseCode();//设置返回的类型
                                connection1.disconnect();
                            }while(code1==502||code1==404);
                            if(code1==200) { DNSjudge=true; pan="true";  }
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    if(DNSjudge) {
                                        String text1 = "是否存在ARP欺骗：\n" + "否";
                                        String text2 = "是否存在DNS欺骗:\n" + "否";
                                        Activity_detection.this. Tv_getInfoList2.setText(text1);
                                        Tv_getInfoList2.setTextColor(Color.parseColor("#ff5e9cff"));
                                        Activity_detection.this. Tv_getInfoList1.setText(text2);
                                        Tv_getInfoList1.setTextColor(Color.parseColor("#ff5e9cff"));

                                    }else{
                                        String text1 = "是否存在ARP欺骗：\n" + "是";
                                        String text2 = "是否存在DNS欺骗:\n" + "是";
                                        Activity_detection.this. Tv_getInfoList2.setText(text1);
                                        Tv_getInfoList2.setTextColor(Color.parseColor("#FF4040"));
                                        Activity_detection.this. Tv_getInfoList1.setText(text2);
                                        Tv_getInfoList1.setTextColor(Color.parseColor("#FF4040"));
                                        //Activity_detection.this. textView2.setText("是否存在ARP欺骗：\n是\n是否存在DNS欺骗:\n是");
                                    }

                                }
                            };
                            uiHandler.post(runnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }



                }

            }catch (Exception e) {
                e.printStackTrace();
            }


            if(pan==null) pan="data";
        }
    }


//    private void setListener() {
//        btn_getWifiInfo.setOnClickListener(listener);
//    }
//    View.OnClickListener listener = new View.OnClickListener() {
//
//        @Override
//        public void onClick(View v) {
//            /**
//             * 获取扫描到的所有wifi相关信息
//             */
//
//            String text = "网络详情:";
//            String text1 = "\nssid：" + "\nbssid：" +  "\n分配的IP地址："+ "\n加密方式："+"\n连接速度："  + "\n强度：" ;
//            String text2 = "\n"+mSsid+"\n"+mBssid+"\n"+String.valueOf(Ipadd)+"\n"+CipherT+"\n"+String.valueOf(speed)+String.valueOf(mUnit)+"\n"+ strength;
////            wifiinformation += "\n\n";
////            wifiinformation += text;
//            Tv1.setText(text);
//            Tv1.setTextColor(Color.parseColor("#ff5e9cff"));
//
//            Tv_getInfoList1.setText(text2);
//            Tv_getInfoList1.setTextColor(Color.parseColor("#ff5e9cff"));
//            Tv_getInfoList2.setText(text1);
//            Tv_getInfoList2.setTextColor(Color.parseColor("#ff5e9cff"));
//        }
//    };


}
