package it.jaschke.alexandria.utilities;

/**
 * Created by Oti Rowland on 1/7/2016.
 */

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class is used to provide network availability information
 */
public class NetworkUtility {
    /**
     * Returns true if network is available or about to become available
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static boolean isNetworkInWifiOrWimaxOrMobileAvailable(Context context) {
        // Check for mobile and Wifi networks
        boolean isMobile = false;
        boolean isWifi = false;
        boolean isWimax = false;
        // Grasp an instance of ConnectivityManager
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // This List will contain all Network information
        List<NetworkInfo> infoAvailableNetworks = new ArrayList<NetworkInfo>();
        // Should be Marshmarllow or higher
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            // Put all available networks into an array
            Network[] availableNetworks = cm.getAllNetworks();
            // Now add each network information
            for (Network availableNetwork : availableNetworks) {
                infoAvailableNetworks.add(cm.getNetworkInfo(availableNetwork));
            }
        } else {
            infoAvailableNetworks = Arrays.asList(cm.getAllNetworkInfo());
        }

        if (infoAvailableNetworks != null) {
            for (NetworkInfo network : infoAvailableNetworks) {

                if (network.getType() == ConnectivityManager.TYPE_WIFI) {
                    if (network.isConnected() && network.isAvailable())
                        isWifi = true;
                }
                if (network.getType() == ConnectivityManager.TYPE_MOBILE) {
                    if (network.isConnected() && network.isAvailable())
                        isMobile = true;
                }
                if (network.getType() == ConnectivityManager.TYPE_WIMAX) {
                    if (network.isConnected() && network.isAvailable())
                        isWimax = true;
                }
            }
        }

        return isMobile || isWifi || isWimax;
    }

}