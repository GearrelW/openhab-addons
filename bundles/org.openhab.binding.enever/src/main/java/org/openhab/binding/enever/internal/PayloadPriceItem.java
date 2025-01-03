package org.openhab.binding.enever.internal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Class that provides storage for the json objects obtained from HomeWizard devices.
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 */
@NonNullByDefault
public class PayloadPriceItem {

    @SerializedName("datum")
    private String datum = "";

    @SerializedName("prijsZP")
    private String prijs = "";

    public LocalDate getDatum() {
        return LocalDateTime.parse(datum, DateTimeFormatter.ofPattern("yyy-MM-dd HH:mm:ss")).toLocalDate();
    }

    public LocalTime getTime() {
        return LocalDateTime.parse(datum, DateTimeFormatter.ofPattern("yyy-MM-dd HH:mm:ss")).toLocalTime();
    }

    public Double getPrijs() {
        return Double.parseDouble(prijs);
    }

}