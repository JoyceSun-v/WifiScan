package activitytest.example.com.wifiscan;

import android.os.Handler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class  ARP{

    private Thread arpReader;
    private List<LanHost> mHosts;
    private List<LanHost> mCheckHosts;
    private NetworkInterface networkInterface = null;
    private Thread discoveryThread;
    private Handler mHandler = new Handler();

    private static final Pattern ARP_TABLE_PARSER = Pattern
            .compile("^([\\d]{1,3}\\.[\\d]{1,3}\\.[\\d]{1,3}\\.[\\d]{1,3})\\s+([0-9-a-fx]+)\\s+([0-9-a-fx]+)\\s+([a-f0-9]{2}:[a-f0-9]{2}:[a-f0-9]{2}:[a-f0-9]{2}:[a-f0-9]{2}:[a-f0-9]{2})\\s+([^\\s]+)\\s+(.+)$",
                    Pattern.CASE_INSENSITIVE);
    public static final byte[] NBREQ = { (byte) 0x82, (byte) 0x28, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x1,
            (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x20, (byte) 0x43, (byte) 0x4B,
            (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41,
            (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41,
            (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41,
            (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x0, (byte) 0x0, (byte) 0x21, (byte) 0x0, (byte) 0x1 };

    private static final short NETBIOS_UDP_PORT = 137;

    public static boolean stop = true;


    public String long2ip(long ip){
        StringBuffer sb=new StringBuffer();
        sb.append(String.valueOf((int)(ip&0xff)));
        sb.append('.');
        sb.append(String.valueOf((int)((ip>>8)&0xff)));
        sb.append('.');
        sb.append(String.valueOf((int)((ip>>16)&0xff)));
        sb.append('.');
        sb.append(String.valueOf((int)((ip>>24)&0xff)));
        return sb.toString();
    }
    //转化IP地址
    public void startDiscovery() {
        stop = false;

        if (discoveryThread != null && !discoveryThread.isAlive()) {
            discoveryThread.interrupt();
            discoveryThread = null;
        }
        discoveryThread = new DiscoveryThread();
        discoveryThread.start();

        if (arpReader != null && !arpReader.isAlive()) {
            arpReader.interrupt();
            arpReader = null;
        }
        arpReader = new ArpReadThread();
        arpReader.start();
    }
    class DiscoveryThread extends Thread {

        ExecutorService executor;//线程池对象

        public void run() {
            if (executor != null && !executor.isShutdown()) {
                executor.shutdownNow();
                executor = null;
            }
            executor = Executors.newFixedThreadPool(10);//定长线程池，可控制线程最大并发数，超出的线程会在队列中等待

            int next_int_ip = 0;
            try {
                while (!stop) {
                    next_int_ip = AppContext.getIntNetMask() & AppContext.getIntGateway();//子网号
                    for (int i = 0; i < AppContext.getHostCount() && !stop; i++) {
                        next_int_ip = NetworkUtils.nextIntIp(next_int_ip);
                        if (next_int_ip != -1) {
                            String ip = NetworkUtils.netfromInt(next_int_ip);
                            try {
                                executor.execute(new UDPThread(ip));
                            } catch (RejectedExecutionException e) {
                                break;
                            } catch (OutOfMemoryError m) {
                                break;
                            }
                        }
                    }
                    Thread.sleep(5000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (executor != null)
                    executor.shutdownNow();
            }
        }
    }

    class RecvThread extends Thread {

        private static final int DATASET_HOST_ALIAS_CHANGED = 0;
        String target_ip;

        public RecvThread(String target_ip) {
            this.target_ip = target_ip;
        }

        public void run() {
            byte[] buffer = new byte[128];
            DatagramSocket socket = null;
            String name;
            try {
                InetAddress inetAddress = InetAddress.getByName(target_ip);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, inetAddress, NETBIOS_UDP_PORT), query = new DatagramPacket(NBREQ,
                        NBREQ.length, inetAddress, NETBIOS_UDP_PORT);
                socket = new DatagramSocket();
                socket.setSoTimeout(200);

                for (int i = 0; i < 3; i++) {
                    socket.send(query);
                    socket.receive(packet);
                    byte[] data = packet.getData();
                    if (data != null && data.length >= 74) {
                        String response = new String(data, "ASCII");
                        name = response.substring(57, 73).trim();

                        for (int k = 0; k < mHosts.size(); k++) {
                            LanHost h = mHosts.get(k);
                            if (h.getIp().equals(target_ip)) {
                                mHandler.obtainMessage(DATASET_HOST_ALIAS_CHANGED, k, 0, name).sendToTarget();
                                break;
                            }
                        }
                        break;
                    }
                }
            } catch (SocketTimeoutException ste) {
            } catch (IOException e) {
            } finally {
                if (socket != null)
                    socket.close();
            }

        }
    }
    class ArpReadThread extends Thread {

        private static final int DATASET_CHANGED = 0;
        ExecutorService executor;

        public void run() {
            if (executor != null && !executor.isShutdown()) {
                executor.shutdownNow();
                executor = null;
            }
            executor = Executors.newFixedThreadPool(5);

            RandomAccessFile fileReader = null;
            try {
                fileReader = new RandomAccessFile("/proc/net/arp", "r");
                StringBuilder sb = new StringBuilder();
                int len = -1;
                String line = null;
                Matcher matcher = null;

                while (!stop) {
                    fileReader.seek(0);
                    while (!stop && (len = fileReader.read()) >= 0) {
                        sb.append((char) len);
                        if (len != '\n')
                            continue;
                        line = sb.toString();
                        sb.setLength(0);

                        if ((matcher = ARP_TABLE_PARSER.matcher(line)) != null && matcher.find()) {
                            String address = matcher.group(1), flags = matcher.group(3), hwaddr = matcher.group(4), device = matcher.group(6);
                            if (device.equals(networkInterface.getDisplayName()) && !hwaddr.equals("00:00:00:00:00:00") && flags.contains("2")) {

                                synchronized (Activity_detection.class) {

                                    boolean contains = false;

                                    for (LanHost h : mCheckHosts) {
                                        if (h.getMac().equals(hwaddr) || h.getIp().equals(address)) {
                                            contains = true;
                                            break;
                                        }
                                    }
                                    if (!contains) {
                                        byte[] mac_bytes = NetworkUtils.stringMacToByte(hwaddr);
                                        String vendor = NetworkUtils.vendorFromMac(mac_bytes);
                                        LanHost host = new LanHost(hwaddr, address, vendor);
                                        mCheckHosts.add(host);
                                        mHandler.obtainMessage(DATASET_CHANGED, host).sendToTarget();
                                        executor.execute(new RecvThread(address));
                                    }
                                }
                            }
                        }
                    }
                    Thread.sleep(3000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fileReader != null)
                        fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (executor != null)
                    executor.shutdownNow();
            }
        }
    }


    // UDPThread
    public class UDPThread extends Thread {
        private String target_ip = "";


        public UDPThread(String target_ip) {
            this.target_ip = target_ip;
        }

        @Override
        public synchronized void run() {
            if (target_ip == null || target_ip.equals("")) return;
            DatagramSocket socket = null;//接收和发送数据包
            InetAddress address = null;
            DatagramPacket packet = null; //构造数据包

            try {
                address = InetAddress.getByName(target_ip);
                packet = new DatagramPacket(NBREQ, NBREQ.length, address, NETBIOS_UDP_PORT);
                socket = new DatagramSocket();
                socket.setSoTimeout(200);
                socket.send(packet);
                socket.close();
            } catch (SocketException se) {
            } catch (UnknownHostException e) {
            } catch (IOException e) {
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }
        }
    }

    public String getMacFromArpCache(String ip) {
        if (ip == null)
            return null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4 && ip.equals(splitted[0])) {
                    // Basic sanity check
                    String mac = splitted[3];
                    if (mac.matches("..:..:..:..:..:..")) {
                        return mac;
                    } else {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    //获取WiFi网关MAC地址
    public int getnum(String ip) {
        if (ip == null)
            return 0;
        int i=0;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4 && ip.equals(splitted[0])) {
                    // Basic sanity check
                    String mac1 = splitted[3];
                    if (mac1.matches("..:..:..:..:..:..")) {
                        i++;
                    }
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return i;
    }
    //获取网关条目

    public int getsum() {
        int j=0;
        RandomAccessFile fileReader = null;
        try {
            fileReader = new RandomAccessFile("/proc/net/arp", "r");

            int len = -1;
            fileReader.seek(0);
            while ( (len = fileReader.read())>= 0) {

                if (len != '\n')
                {
                    j++;
                }
                continue;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return j;
    }
    //获取总条目
}
