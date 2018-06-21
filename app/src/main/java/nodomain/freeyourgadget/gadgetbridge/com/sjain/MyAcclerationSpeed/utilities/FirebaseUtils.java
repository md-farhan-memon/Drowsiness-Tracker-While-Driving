package nodomain.freeyourgadget.gadgetbridge.com.sjain.MyAcclerationSpeed.utilities;

import com.google.firebase.database.DatabaseReference;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;

/**
 * Created by sjain on 13/02/18.
 */

public class FirebaseUtils {

    public static final String PATH_INIT = "";

    public static final String PATH_SYS_USERS = PATH_INIT + "/User";
    public static final String PATH_SYS_GEOFIRE_DATA = PATH_INIT + "/geo_location";

    public static DatabaseReference getDatabase() {
        return GBApplication.getFirebaseDatabase().getReference();
    }

}
