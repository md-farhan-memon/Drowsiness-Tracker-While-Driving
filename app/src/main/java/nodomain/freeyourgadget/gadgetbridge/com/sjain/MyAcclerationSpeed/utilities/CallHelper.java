package nodomain.freeyourgadget.gadgetbridge.com.sjain.MyAcclerationSpeed.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import nodomain.freeyourgadget.gadgetbridge.com.sjain.MyAcclerationSpeed.DailogMessageActivity;

/**
 * Created by sjain on 14/03/18.
 */

public class CallHelper {


    public static Double AVG_SPEED = 0.0;
    public static double MAX_SPEED = 25.00;
    private Context ctx;
    private TelephonyManager tm;
    private CallStateListener callStateListener;
    private Boolean wasRinging;
    private OutgoingReceiver outgoingReceiver;

    public CallHelper(Context ctx) {
        this.ctx = ctx;
        wasRinging = false;
        callStateListener = new CallStateListener();
        outgoingReceiver = new OutgoingReceiver();
    }

    /**
     * Start calls detection.
     */
    public void start() {
        tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
        ctx.registerReceiver(outgoingReceiver, intentFilter);
    }

    /**
     * Listener to detect incoming calls.
     */
    private class CallStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    // called when someone is ringing to this phone
                    if (AVG_SPEED > MAX_SPEED) {
                        wasRinging = true;
                        Toast.makeText(ctx,
                                "Incoming: " + incomingNumber,
                                Toast.LENGTH_LONG).show();
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (AVG_SPEED > MAX_SPEED) {
                        Intent intent = new Intent(ctx, DailogMessageActivity.class);
                        intent.putExtra("MESSAGE", "Please avoid using mobile while driving!!");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ctx.startActivity(intent);
                        if (!wasRinging) {
                            // Start your new activity
                        } else {
                            // Cancel your old activity
                        }

                        // this should be the last piece of code before the break
                        wasRinging = true;

                    }

                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
            }
        }
    }

    /**
     * Broadcast receiver to detect the outgoing calls.
     */
    public class OutgoingReceiver extends BroadcastReceiver {
        public OutgoingReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (AVG_SPEED > MAX_SPEED) {
                String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                Intent intent1 = new Intent(ctx, DailogMessageActivity.class);
                intent1.putExtra("MESSAGE", "Please avoid using mobile while driving!!");
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(intent1);
            }
        }

    }

    /**
     * Stop calls detection.
     */
    /*public void stop() {
        tm.listen(callStateListener, PhoneStateListener.LISTEN_NONE);
        ctx.unregisterReceiver(outgoingReceiver);
    }*/

}
