package activitytest.example.com.wifiscan;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Activity_main extends AppCompatActivity {

    private Button wifitest;
    private boolean wifiscan=true;
    private Button btn_getWifiList;
    private ListView lv_getWifiList;
    private TextView Tv;
    private WifiManager wifiManager;
    private List<ScanResult>mWifiList;
    private List<ScanResult>mWifiList1;
    private List<String>wifiList;
    private WifiInfo wifiInfo;
    protected String ssid;
    protected WifiAdmin mWifiAdmin;

    protected Context mContext;
    private int i = -1;
    private CountDownTimer myCount;

    private static  final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION  = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWifiAdmin = new WifiAdmin(Activity_main.this);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mReceiver, filter);
        mContext = this;


        wifiInfo = wifiManager.getConnectionInfo();
        final int strength = wifiInfo.getRssi();
        final int speed = wifiInfo.getLinkSpeed();
        final String mBssid = wifiInfo.getBSSID();
        final String mSsid = mWifiAdmin.getWIFISSID(Activity_main.this);
        final String unit = WifiInfo.LINK_SPEED_UNITS;
        final String mMac = mWifiAdmin.getMacAddress();
        final String CipherT = mWifiAdmin.getCipherType(mContext,mWifiAdmin.getWIFISSID(Activity_main.this));




        mWifiList = new ArrayList<>();
        mWifiList1 = new ArrayList<>();
        wifiList = new ArrayList<>();


        lv_getWifiList = (ListView) findViewById(R.id.ListView);
        btn_getWifiList = (Button) findViewById(R.id.button);
        wifitest = (Button) findViewById(R.id.button4);
        //Tv = (TextView) findViewById(R.id.textView3);

        //switch样式实例化
        final Switch mSwitch = (Switch) findViewById(R.id.switch1);
        if (wifiManager.isWifiEnabled()) {
            mSwitch.setChecked(true);
        } else {
            mSwitch.setChecked(false);
        }
        mSwitch.setSwitchTextAppearance(Activity_main.this, R.style.s_false);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //控制开关字体颜色
                if (b) {
                    mSwitch.setSwitchTextAppearance(Activity_main.this, R.style.s_true);
                } else {
                    mSwitch.setSwitchTextAppearance(Activity_main.this, R.style.s_false);
                }
            }

        });


        setListener();
        wifitest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Activity_main.this, Activity_detection.class);
                intent.putExtra("isConnected",mWifiAdmin.isWifiConnected1(Activity_main.this));
                intent.putExtra("mSsid",mWifiAdmin.getWIFISSID(Activity_main.this));
                intent.putExtra("mBssid",wifiInfo.getBSSID());
                intent.putExtra("Ipadd",mWifiAdmin.getIPAddress());
                intent.putExtra("CipherT",mWifiAdmin.getCipherType(mContext,mWifiAdmin.getWIFISSID(Activity_main.this)));
                intent.putExtra("mMac",mWifiAdmin.getMacAddress());
                intent.putExtra("Speed",wifiInfo.getLinkSpeed());
                intent.putExtra("mUnit",WifiInfo.LINK_SPEED_UNITS);
                intent.putExtra("Strength",wifiInfo.getRssi());
                startActivity(intent);
            }
        });


        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {
                    //mWifiAdmin.openWifi(mContext);
                    wifiManager.setWifiEnabled(true);
                    System.out.println(wifiscan+"1");
                    wifiscan = true;
                } else {
                    //mWifiAdmin.closeWifi(mContext);
                    wifiManager.setWifiEnabled(false);
                    if(lv_getWifiList.getAdapter()!=null&&lv_getWifiList.getAdapter().getCount()>0){
                        clearAdapterList(lv_getWifiList);
                    }

                }
            }
        });

        lv_getWifiList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder alert = new AlertDialog.Builder(Activity_main.this);
                if (mWifiList1!=null&&mWifiList1.size()>0) {
                    ssid = mWifiList1.get(position).SSID;
                }else{
                    Toast.makeText(mContext,"Please scan again!",Toast.LENGTH_SHORT);
                   // clearAdapterList(lv_getWifiList);
                   // wifiscan = true;
                }
                if (mWifiAdmin.isExsits(ssid) == null) {
                        alert.setTitle(ssid);
                        alert.setMessage("输入密码:");
                        final EditText et_password = new EditText(Activity_main.this);
                        final SharedPreferences preferences = getSharedPreferences("wifi_password", Context.MODE_PRIVATE);
                        et_password.setText(preferences.getString(ssid, ""));
                        alert.setView(et_password);
                        alert.setPositiveButton("连接", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String pw = et_password.getText().toString();
                                if (pw.length() == 0) {
                                    Toast.makeText(Activity_main.this, "请输入密码", Toast.LENGTH_SHORT).show();
                                    return;
                                } else if (0 < pw.length() && pw.length() < 8) {
                                    Toast.makeText(Activity_main.this, "密码至少8位", Toast.LENGTH_SHORT).show();
                                    return;
                                }


                                SharedPreferences.Editor editor = preferences.edit();
                                editor.commit();
                                mWifiAdmin.addNetwork(mWifiAdmin.CreateWifiInfo(ssid, et_password.getText().toString(), 3));
                                //加入等待提示框
                                final SweetAlertDialog pDialog = new SweetAlertDialog(Activity_main.this, SweetAlertDialog.PROGRESS_TYPE)
                                        .setTitleText("Loading");
                                pDialog.show();
                                pDialog.setCancelable(false);
                                if(myCount!=null){
                                    myCount.cancel();
                                }
                                myCount = new CountDownTimer(800 * 7, 800) {
                                    public void onTick(long millisUntilFinished) {
                                        // you can change the progress bar color by ProgressHelper every 800 millis
                                        if(mWifiAdmin.isWifiConnected1(Activity_main.this)&&
                                                Objects.equals(mWifiAdmin.getWIFISSID(Activity_main.this), ssid)){
                                            onFinish();
                                            myCount.cancel();
                                            myCount = null;
                                        }else{
                                            i++;
                                        }
                                        switch (i){
                                            case 0:
                                                pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.blue_btn_bg_color));
                                                break;
                                            case 1:
                                                pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.material_deep_teal_50));
                                                break;
                                            case 2:
                                                pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.success_stroke_color));
                                                break;
                                            case 3:
                                                pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.material_deep_teal_20));
                                                break;
                                            case 4:
                                                pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.material_blue_grey_80));
                                                break;
                                            case 5:
                                                pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.warning_stroke_color));
                                                break;
                                            case 6:
                                                pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.success_stroke_color));
                                                break;
                                        }
                                    }

                                    public void onFinish() {
                                        i = -1;
                                        if(mWifiAdmin.isWifiConnected1(Activity_main.this)){
                                            if(Objects.equals(mWifiAdmin.getWIFISSID(Activity_main.this), ssid)){
                                                pDialog.setTitleText(ssid+"已连接!")
                                                        .setContentText(mBssid+"/"+speed+"/"+mMac)
                                                        .setConfirmText("OK")
                                                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                            }else{
                                                pDialog.setTitleText(mWifiAdmin.getWIFISSID(Activity_main.this)+"已连接!")
                                                        .setContentText(ssid+"连接失败！")
                                                        .setConfirmText("OK")
                                                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                            }
                                        }else{
                                            pDialog.setTitleText(ssid+"连接失败！")
                                                    .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                                        }

                                    }
                                }.start();
                            }
                        });
                        alert.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        alert.create();
                        alert.show();
                }else if(Objects.equals(mWifiAdmin.getWIFISSID(Activity_main.this), ssid)){
                    new SweetAlertDialog(Activity_main.this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText(ssid + "已连接!")
                            .setContentText(mBssid+"/"+unit+"/"+mMac)
                            .setCancelText("Cancel")
                            .setConfirmText("不保存")
                            .showCancelButton(true)
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.cancel();
                                }
                            })
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    int netId = mWifiAdmin.getNetworkId();
                                    mWifiAdmin.removeWifi(netId);
                                    sDialog
                                            .setTitleText("已断开连接!")
                                            .setConfirmText("OK")
                                            .setConfirmClickListener(null)
                                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                }
                            })
                            .show();
                }else if(!Objects.equals(mWifiAdmin.getWIFISSID(Activity_main.this), ssid)){
                    new SweetAlertDialog(Activity_main.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText(ssid)
                            .setCancelText("Cancel")
                            .setConfirmText("不保存")
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.cancel();
                                }
                            })
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    int netId = mWifiAdmin.getNetworkId();
                                    mWifiAdmin.removeWifi(netId);
                                    sDialog
                                            .setTitleText(ssid+"已移除！")
                                            .setConfirmText("OK")
                                            .setConfirmClickListener(null)
                                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                }
                            })
                            .show();

                }

            }
        });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mReceiver);
        if(myCount!=null){
            myCount.cancel();
            myCount = null;
        }
    }

    protected void setAdapter() {
        ArrayAdapter<String> adapter= new ArrayAdapter<>(Activity_main.this, android.R.layout.simple_list_item_1, wifiList);
        lv_getWifiList.setAdapter(adapter);
    }
    //清空
    private void clearAdapterList(ListView listView){
        ArrayAdapter adapter = (ArrayAdapter) listView.getAdapter();
        int count = adapter.getCount();// listview多少个组件
        if (count > 0) {
            //Toast.makeText(this, "Size" + count, Toast.LENGTH_LONG).show();
            listView.setAdapter(new ArrayAdapter<>(Activity_main.this, android.R.layout.simple_list_item_1));
            mWifiList1.clear();
            //mWifiList.clear();
            wifiList.clear();
        }
    }

    public boolean containName(List<ScanResult> sr, String name) {
        for (ScanResult result : sr)
        {
            if (!TextUtils.isEmpty(result.SSID) && result.SSID.equals(name))
                return true;
        }
        return false;
    }

    public List<ScanResult> noSameName(List<ScanResult> oldSr) {
        List<ScanResult> newSr = new ArrayList<>();
        for (ScanResult result : oldSr)
        {
            if (!TextUtils.isEmpty(result.SSID) && !containName(newSr, result.SSID))
                newSr.add(result);
        }
        return newSr;
    }

    private void setListener() {
            btn_getWifiList.setOnClickListener(listener);
    }
    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button:
                    //先判断wifi是否已经开启
                    if(wifiManager.isWifiEnabled()){
                        System.out.println(wifiscan+"2");
                        if(lv_getWifiList.getCount()==0){
                            wifiscan = true;
                            System.out.println(wifiscan+"2-1");
                        }
                        System.out.println(wifiscan+"2-2");
                        if(wifiscan)//使只更新一次
                        {
                            wifiManager.startScan();
                            mWifiList = wifiManager.getScanResults();
                            mWifiList1=noSameName(mWifiList);
                            //mWifiList1=mWifiAdmin.getWifiList(mContext);//noSameName(mWifiList);
                            //把mwifilist1里面的内容放在wifilist里面
                            for(int i = 0;i < mWifiList1.size();i++){
                                wifiList.add(mWifiList1.get(i).SSID);
                               // String capabilities =mWifiList1.get(i).capabilities;
                            }
                            setAdapter();
                            wifiscan=false;
                        }
                        System.out.println(wifiscan+"3");
                    } else {
                        Toast.makeText(Activity_main.this, "wifi没有开启，无法扫描", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }

        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver (){
        @Override
        public void onReceive(Context context, Intent intent) {
            registerPermission();
            if(mWifiAdmin.isWifiConnected1(Activity_main.this)){
                Toast.makeText(context, ssid+"连接成功", Toast.LENGTH_SHORT).show();
            }
        }

    };
    //申请定位权限（Android9.0以上需要）
    private void registerPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },
                    PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
        } else {
             ssid = mWifiAdmin.getWIFISSID(Activity_main.this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            ssid = mWifiAdmin.getWIFISSID(Activity_main.this);
        }
    }

}



