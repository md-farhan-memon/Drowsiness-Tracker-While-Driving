package nodomain.freeyourgadget.gadgetbridge.com.sjain.MyAcclerationSpeed.utilities;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by sjain on 14/03/18.
 */

public class CallDetectService extends Service {
    private CallHelper callHelper;

    public CallDetectService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        callHelper = new CallHelper(this);
        int res = super.onStartCommand(intent, flags, startId);
        callHelper.start();
        return res;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // not supporting binding
        return null;
    }
}
