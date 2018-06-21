package nodomain.freeyourgadget.gadgetbridge.com.sjain.MyAcclerationSpeed.model;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by sjain on 15/03/18.
 */

@IgnoreExtraProperties
public class GeoLocation {

    private Double latitude;

    private Double longitude;

    private Integer min_speed;

    private String name;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getMin_speed() {
        return min_speed;
    }

    public void setMin_speed(Integer min_speed) {
        this.min_speed = min_speed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
