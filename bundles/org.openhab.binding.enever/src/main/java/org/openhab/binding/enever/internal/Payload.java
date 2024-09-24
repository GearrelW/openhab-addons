package org.openhab.binding.enever.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Class that provides storage for the json objects obtained from HomeWizard devices.
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 */
@NonNullByDefault
public class Payload {
    @SerializedName("status")
    private boolean status = true;

    @SerializedName("data")
    private List<PayloadPriceItem> prices = new ArrayList<PayloadPriceItem>();

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<PayloadPriceItem> getPrices() {
        return prices;
    }

}