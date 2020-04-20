package activitytest.example.com.wifiscan;

import java.net.HttpURLConnection;
import java.net.URL;

public class DNS implements Runnable   {
    public  static boolean DNSjudge=false;
    public static String pan=null;

    public  synchronized   boolean getDNSjudge()
    {

        return DNSjudge;
    }

    public  synchronized  String getpan()
    {

        return pan;
    }

    public synchronized void run() {
        String path = "http://www.baidu.com";
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
                connection.disconnect();
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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }




            }
        } catch (Exception e) {
            e.printStackTrace();
        }/*
        catch (UnsupportedEncodingException e) {
            // 不支持你设置的编码
            e.printStackTrace();
        } catch (ProtocolException e) {
            // 请求方式不支持
            e.printStackTrace();
        } catch (IOException e) {
            // 输入输出通讯出错
            e.printStackTrace();
       }
    */

        if(pan==null) pan="data";
    }
/*
public static void main(String[] args) throws Exception
	 {

	DNS test=new DNS();
	Thread  dns;
	dns=new Thread(test);
	dns.setName("DNS");
	dns.start();
	while(test.getpan()==null);
	if(test.getpan().equals("ture"));
		     System.out.println("----------------------------------------");
		     System.out.println("结果为"+test.getpan());
		     System.out.println("----------------------------------------");


	}
*/
}

