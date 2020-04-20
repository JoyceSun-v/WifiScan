package activitytest.example.com.wifiscan;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

public class ToastUtil{
    private static Toast toast;
    private static Application sContext;

    private long lastShowTime = 0l;
    private String lastShowMsg = null;
    private String curShowMsg = null;
    private final int TOAST_DURATION = 2000;

    public static void init(Application application) {
        sContext = application;
    }

    public static void showShort(CharSequence sequence) {
        if (toast == null) {
            toast = Toast.makeText(sContext, sequence, Toast.LENGTH_SHORT);
        } else {
            toast.setText(sequence);
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        toast.show();
    }

    public static void showLong(CharSequence sequence) {
        if (toast == null) {
            toast = Toast.makeText(sContext, sequence, Toast.LENGTH_LONG);
        } else {
            toast.cancel();
            toast.setDuration(Toast.LENGTH_LONG);
        }
        toast.show();
    }

    public void customShowToast(Context context, CharSequence s) {
        curShowMsg = s.toString();
        long curShowTime = System.currentTimeMillis();
        if (curShowMsg.equals(lastShowMsg)) {
            if (curShowTime - lastShowTime > TOAST_DURATION) {
                Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
                lastShowTime = curShowTime;
                lastShowMsg = curShowMsg;
            }
        } else {
            Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
            lastShowTime = curShowTime;
            lastShowMsg = curShowMsg;
        }
    }
}

