package nodomain.freeyourgadget.gadgetbridge.util;

import android.content.Context;
import android.content.SharedPreferences;

public class ERPrefs {

    private static final String PREFERENCES_NAME = "easyroads_preferences";
    private static final String IS_LOGGED_IN = "is_logged_in";
    private static final String USER_ID = "user_id";
    private static final String USER_NAME = "user_name";
    private static final String USER_SURNAME = "user_surname";
    private static final String USER_DATE_BIRTH = "user_date_birth";
    private static final String USER_GENDER = "user_gender";
    private static final String USER_CITY = "user_city";
    private static final String USER_STATE = "user_state";
    private static final String USER_PINCODE = "user_pincode";
    private static final String USER_EMAIL = "user_email";
    private static final String USER_PICTURE = "user_picture";
    private static final String USER_MOBILE_NUMBER = "user_mobile_number";
    private static final String USER_XTRA_REWARDS_TNC_ACCEPTED = "xtra_rewards_tnc_accepted";
    private static final String USER_AUTH_TOKEN = "user_auth_token";


    private static ERPrefs easyRoadsPreferences;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;


    private ERPrefs(Context mContext) {
        preferences = mContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.apply();
    }

    public static synchronized ERPrefs getInstance(Context context) {
        if (easyRoadsPreferences == null) {
            easyRoadsPreferences = new ERPrefs(context);
        }
        return easyRoadsPreferences;
    }

    public void clearprefs() {
        editor.clear();
        editor.commit();
    }

    public String getUserAdmin() {
        return "Admin";
    }

    public String getPassword() {
        return "Admin";
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(IS_LOGGED_IN, false);
    }

    public void setIsLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(IS_LOGGED_IN, isLoggedIn);
        editor.commit();
    }

    public String getUserMobileNumber() {
        return preferences.getString(USER_MOBILE_NUMBER, "");
    }

    /*public void saveUserData(UserDetails user) {
        String imageURL = "";
        imageURL = user.getAvatarURL();
        editor.putString(USER_ID, user.getUserId());
        editor.putString(USER_NAME, user.getUserName());
        editor.putString(USER_SURNAME, user.getSurname());
        editor.putString(USER_EMAIL, user.getUserEmail());
        editor.putString(USER_PICTURE, imageURL);
        editor.putString(USER_DATE_BIRTH, user.getDateOfBirth());
        editor.putString(USER_GENDER, user.getGender());
        editor.putString(USER_CITY, user.getCity());
        editor.putString(USER_STATE, user.getState());
        editor.putString(USER_PINCODE, user.getPincode());
        editor.putString(USER_MOBILE_NUMBER, user.getUserMobileNumber());
        editor.putBoolean(USER_XTRA_REWARDS_TNC_ACCEPTED, user.isXtra_rewards_tnc_accepted());
        editor.putBoolean(IS_FB_LOGGED_IN, user.isFbLogin());
        editor.commit();
    }*/

    public String getUserName() {
        return preferences.getString(USER_NAME, "");
    }

    public String getUserSurname() {
        return preferences.getString(USER_SURNAME, "");
    }

    public String getUserDOB() {
        return preferences.getString(USER_DATE_BIRTH, "");
    }

    public String getUserGender() {
        return preferences.getString(USER_GENDER, "");
    }

    public String getUserPincode() {
        return preferences.getString(USER_PINCODE, "");
    }

    public String getUserState() {
        return preferences.getString(USER_STATE, "");
    }

    public String getUserCity() {
        return preferences.getString(USER_CITY, "");
    }

    public String getUserId() {
        return preferences.getString(USER_ID, "");
    }

    public String getUserEmail() {
        return preferences.getString(USER_EMAIL, "");
    }

    public String getUserPicture() {
        return preferences.getString(USER_PICTURE, "");
    }

    public boolean getUserXtraRewardsTncAccepted() {
        return preferences.getBoolean(USER_XTRA_REWARDS_TNC_ACCEPTED, false);
    }


    public String getUserAuthToken() {
        return preferences.getString(USER_AUTH_TOKEN, "");
    }

    public void setUserAuthToken(String userAuthToken) {
        editor.putString(USER_AUTH_TOKEN, userAuthToken);
        editor.commit();
    }
}
