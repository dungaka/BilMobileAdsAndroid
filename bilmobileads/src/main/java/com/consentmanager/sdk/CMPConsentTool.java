package com.consentmanager.sdk;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;

import com.consentmanager.sdk.activities.CMPConsentToolActivity;
import com.consentmanager.sdk.callbacks.CustomOpenActionCallback;
import com.consentmanager.sdk.callbacks.OnCloseCallback;
import com.consentmanager.sdk.callbacks.OnErrorDialogCallback;
import com.consentmanager.sdk.callbacks.OnNetworkExceptionCallback;
import com.consentmanager.sdk.callbacks.OnOpenCallback;
import com.consentmanager.sdk.exceptions.CMPConsentToolInitialiseException;
import com.consentmanager.sdk.exceptions.CMPConsentToolNetworkException;
import com.consentmanager.sdk.exceptions.CMPConsentToolSettingsException;
import com.consentmanager.sdk.model.CMPConfig;
import com.consentmanager.sdk.model.CMPSettings;
import com.consentmanager.sdk.model.SubjectToGdpr;
import com.consentmanager.sdk.server.ServerContacter;
import com.consentmanager.sdk.server.ServerResponse;
import com.consentmanager.sdk.storage.CMPPrivateStorage;
import com.consentmanager.sdk.storage.CMPStorageConsentManager;
import com.consentmanager.sdk.storage.CMPStorageV1;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This is the Main class providing all functionalities of the CMP Consent Tool.
 * You have to initialise this class with the method createInstance(...). The config
 * data needs to be send via the method or been declared in the AndroidManifest of this module.
 * The following three information must be given:
 * <meta-data android:name="com.consentmanager.sdk.ID" android:value="<Your CMP ID>" />
 * <meta-data android:name="com.consentmanager.sdk.SERVER_DOMAIN" android:value="<The Server Domain that needs to be called>" />
 * <meta-data android:name="com.consentmanager.sdk.APP_NAME" android:value="<The Name of your app, as mentioned in your account>" />
 * <meta-data android:name="com.consentmanager.sdk.LANGUAGE" android:value="<The Language of the consent values>" />
 * These information are not required, if you instantiate this class with the config Parameters in your main class.
 */
public class CMPConsentTool {
    /**
     * The singleton CMPConsentTool class. Only over this class you can attemp the functionalities
     */
    private static CMPConsentTool consentTool;
    /**
     * The App Context
     */
    private Context context;
    /**
     * The config, which holds the correct Config parameters, explained at the class description.
     */
    private CMPConfig config;

    /**
     * A function that will be called, when the Modal View will be closed.
     */
    private OnCloseCallback closeListener;

    /**
     * A function that will be called, when the Modal View will be opened.
     */
    private OnOpenCallback openListener;

    /**
     * A function that will be called, if an error Message will be send from the server.
     */
    private OnErrorDialogCallback errorDialog;

    /**
     * If set, this function will be called, if the Server send a request.
     */
    private CustomOpenActionCallback customAction;
    /**
     * If set, this function will be called, if the Server send a request.
     */
    private OnNetworkExceptionCallback networkErrorHandler;

    /**
     * The last consentTool ServerResponse
     */
    private ServerResponse response;

    /**
     * The receiver that is being called, if the Network State changes
     */
    private BroadcastReceiver networkReceiver;

    /**
     * Constructor can only be called through the static functions.
     * The Constructor initialise the default close behavior.
     */
    private CMPConsentTool() {
        closeListener = new OnCloseCallback() {
            @Override
            public void onWebViewClosed() {

            }
        };
        openListener = new OnOpenCallback() {
            @Override
            public void onWebViewOpened() {

            }
        };
    }

    /**
     * Constructor can only be called through the static functions. This creates the Functions, witch
     * includes all functionalities of consentManager.
     *
     * @param context The app Context
     * @param config  The Config for Consent Manager
     */
    private CMPConsentTool(Context context, CMPConfig config) {
        this();
        this.context = context;
        this.config = config;
        this.checkAndProceedConsentUpdate();

//        //on connection settings changed listener
//        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
//        networkReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//                boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
//                if (isConnected) {
//                    CMPConsentTool.this.checkAndProceedConsentUpdate();
//                }
//            }
//        };
//        this.context.registerReceiver(networkReceiver, filter);
    }

    private CMPConsentTool(Context context, CMPConfig config, OnCloseCallback onCloseCallback) {
        this();
        this.context = context;
        this.config = config;
        this.closeListener = onCloseCallback;

        this.checkAndProceedConsentUpdate();
        // on reload app listener
//        ((AppCompatActivity) context).getApplication().registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
//
//            @Override
//            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
//            }
//
//            @Override
//            public void onActivityStarted(@NonNull Activity activity) {
//
//            }
//
//            @Override
//            public void onActivityResumed(@NonNull Activity activity) {
////                CMPConsentTool.this.checkAndProceedConsentUpdate();
//            }
//
//            @Override
//            public void onActivityPaused(@NonNull Activity activity) {
//            }
//
//            @Override
//            public void onActivityStopped(@NonNull Activity activity) {
//
//            }
//
//            @Override
//            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
//
//            }
//
//            @Override
//            public void onActivityDestroyed(@NonNull Activity activity) {
//
//            }
//        });

//        //on connection settings changed listener
//        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
//        networkReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//                boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
//                if (isConnected) {
//                    CMPConsentTool.this.checkAndProceedConsentUpdate();
//                }
//            }
//        };
//        this.context.registerReceiver(networkReceiver, filter);
    }

    /**
     * Gives the ConsentManager a new context.
     *
     * @param context The new Context
     */
    private void setContext(Context context) {
        this.context = context;
    }

    /**
     * Returns the Context that is set
     *
     * @return the set App Context
     */
    public Context getContext() {
        return context;
    }

    /**
     * Sets the Config to the Consent Manager
     *
     * @param config The ne config
     */
    private void setConfig(CMPConfig config) {
        this.config = config;
    }

    /**
     * Returns the Config that is set
     *
     * @return the set Config
     */
    public CMPConfig getConfig() {
        return config;
    }

    /**
     * Returns the CMPConsentTool. If you have not initialised the CMPConsentTool before,
     * The CMPConsentToolInitialisation Exception will be thrown.
     *
     * @return the initialised singleton Instant of the consent Manager.
     * @throws CMPConsentToolInitialiseException If the CMPConsentTool wasn't initialised
     */
    public static CMPConsentTool getInstance() {
        if (consentTool == null) {
            throw new CMPConsentToolInitialiseException();
        }
        return consentTool;
    }

    /**
     * Returns the CMPConsentTool, or null if it wasn't initialised. This method is unsafe,
     * because it can lead to null pointer exceptions, but if you are sure, the ConsentTool
     * was initialised before, you can use this Method, without the need to catch the error.
     * We recommend to save the returned Object from the createInstant Method, to use the
     * Methods of the consentManager.
     *
     * @return the initialised singleton Instant of the consent Manager, or null.
     */
    public static CMPConsentTool getInstanceUnsafe() {
        return consentTool;
    }

    /**
     * Initialises the CMPConsentTool with the given context and the config parameters given
     * in the AndroidManifest.xml . You need to use this or an other createInstant Method to
     * initialise the CMPConsentTool before you use the functionality.
     *
     * @param context The App Context
     * @return The created singleton Consent Manager Instance
     * @throws CMPConsentToolSettingsException If there are missing configs in the AndroidManifest.xml
     */
    public static CMPConsentTool createInstance(Context context) {
        if (!consentToolExists()) {
            consentTool = createInstance(context, CMPConfig.createInstance(context));
        } else {
            consentTool.setContext(context);
        }
        return consentTool;
    }

    /**
     * Initialises the CMPConsentTool with the given context and the config parameters. You need to
     * use this or an other createInstant Method to initialise the CMPConsentTool before you use the
     * functionality. Alternatively, you can insert these parameters in the AndroidManifest.xml to
     * make your code more readable and to have all your configs on one place.
     *
     * @param context       The App Context
     * @param id            The Id you got from consentmanager online platform
     * @param server_domain The Domain of the Server that should be called.
     * @param app_name      The name of your App.
     * @return The created singleton Consent Manager Instance
     */
    public static CMPConsentTool createInstance(Context context, int id, String server_domain, String app_name, String language) {
        if (!consentToolExists()) {
            consentTool = createInstance(context, CMPConfig.createInstance(id, server_domain, app_name, language));
        } else {
            consentTool.setContext(context);
            consentTool.setConfig(CMPConfig.createInstance(id, server_domain, app_name, language));
        }
        return consentTool;
    }

    /**
     * Initialises the CMPConsentTool with the given context and a given Config. You need to
     * use this or an other createInstant Method to initialise the CMPConsentTool before you use the
     * functionality. Alternatively, you can insert these parameters in the AndroidManifest.xml to
     * make your code more readable and to have all your configs on one place.
     *
     * @param context The App Context
     * @param config  The filled CMP Config.
     * @return The created singleton Consent Manager Instance
     */
    public static CMPConsentTool createInstance(Context context, CMPConfig config) {
        if (!consentToolExists()) {
            consentTool = new CMPConsentTool(context, config);
        } else {
            consentTool.setContext(context);
            consentTool.setConfig(config);
        }
        return consentTool;
    }

    public static CMPConsentTool createInstance(Context context, CMPConfig config, OnCloseCallback onCloseCallback) {
        if (!consentToolExists()) {
            consentTool = new CMPConsentTool(context, config, onCloseCallback);
        } else {
            consentTool.setContext(context);
            consentTool.setConfig(config);
            consentTool.setCloseCmpConsentToolViewListener(onCloseCallback);
        }
        return consentTool;
    }

    /**
     * If the ConsentManager was initialised
     * return If the ConsentManager was initialised
     */
    private static boolean consentToolExists() {
        return consentTool != null;
    }

    /**
     * Sets a Listener to the given button, If the Button is clicked, a modal view will be displayed
     * with the consent web view. If the Compliance is accepted or rejected, a close function will be
     * called. You can override this close function with your own. Therefor implement the OnCloseCallback
     * and add this as an other parameter. If the parameter is not set, but the setCloseCmpConsentToolViewListener
     * was used to add a listener to the close event, this will be used.
     *
     * @param gdprButton The Button, the openCmpConsentToolViewListener should be added to.
     */
    public void setOpenCmpConsentToolViewListener(Button gdprButton) {
        this.setOpenCmpConsentToolViewListener(gdprButton, this.closeListener);
    }

    /**
     * Sets a Listener to the given button, If the Button is clicked, a modal view will be displayed
     * with the consent web view. If the Compliance is accepted or rejected, a close function will be
     * called. You can override this close function with your own. Therefor implement the OnCloseCallback
     * and add this as the last parameter.
     *
     * @param gdprButton The Button, the openCmpConsentToolViewListener should be added to.
     * @param callback   The OnCloseCallback, that should be called, when the button was closed.
     */
    public void setOpenCmpConsentToolViewListener(Button gdprButton, final OnCloseCallback callback) {
        gdprButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CMPConsentTool.this.openCmpConsentToolView(callback);
            }
        });
    }

    /**
     * Sets a listener that is called, if a network error occurs.
     *
     * @param callback The OnErrorNetworkCallback that needs to be called.
     */
    public void setNetworkExceptionListener(final OnNetworkExceptionCallback callback) {
        this.networkErrorHandler = callback;
    }

    /**
     * Sets a Listener. If the Compliance is accepted or rejected, this function will be
     * called. You can override this close function with your own. Therefor implement the OnCloseCallback
     * and add this as the parameter.
     *
     * @param callback The OnCloseCallback, that should be called, when the View was closed.
     */
    public void setCloseCmpConsentToolViewListener(final OnCloseCallback callback) {
        this.closeListener = callback;
    }

    /**
     * Sets a Listener. If the Compliance View is opened this function will be called.
     * You can override this open function with your own. Therefor implement the OnOpenCallback
     * and add this as the parameter.
     *
     * @param callback The OnOpenCallback, that should be called, when the View will be opened.
     */
    public void setOpenCmpConsentToolViewListener(final OnOpenCallback callback) {
        this.openListener = callback;
    }

    /**
     * Sets a Listener. If the Compliance View is opened this function will be called.
     * You can override this open function with your own. Therefor implement the OnOpenCallback
     * and add this as the parameter.
     *
     * @param callback The OnOpenCallback, that should be called, when the View will be opened.
     */
    public void setErrorDialogCmpConsentToolViewListener(final OnErrorDialogCallback callback) {
        this.errorDialog = callback;
    }

    /**
     * Sets a Custom action to a server response.
     *
     * @param customAction The customAction that should be called, apart from showing the consentTool
     */
    public void setCustomCmpConsentToolViewAction(CustomOpenActionCallback customAction) {
        this.customAction = customAction;
    }

    /**
     * Displays a modal view with the consent web view. If the Compliance is accepted or rejected,
     * a close function will be called. You can override this close function with your own. Therefor
     * implement the OnCloseCallback and add this as a parameter.
     */
    public void openCmpConsentToolView() {
        openCmpConsentToolView(this.closeListener);
    }

    /**
     * Displays a modal view with the consent web view. If the Compliance is accepted or rejected,
     * a close function will be called. You can override this close function with your own. Therefor
     * implement the OnCloseCallback and give it to this function. This Method will not send a request
     * to the consentTool Server again. It will use the last state. If you only wnt to open the consent
     * Tool View again, if the server gives a response status == 1 use the checkAndProceedConsentUpdate
     * method.
     *
     * @param callback The OnCloseCallback, that should be called, when the button was closed.
     */
    public void openCmpConsentToolView(final OnCloseCallback callback) {
        this.openListener.onWebViewOpened();
        CMPStorageV1.setCmpPresentValue(context, true);

        if (this.customAction != null) {
            this.customAction.onOpenCMPConsentToolActivity(response, CMPSettings.getInstance(context));
            return;
        }
        CMPConsentToolActivity.openCmpConsentToolView(CMPSettings.getInstance(context), context, new OnCloseCallback() {
            @Override
            public void onWebViewClosed() {
                callback.onWebViewClosed();
            }
        }, networkErrorHandler);
    }

    /**
     * This Method will request the consent Tool server for updates and updates the CMPSettings. The
     * Server Connections Parameters are set through the CMPConfig while initialising this instance or
     * put them in the AndroidManifest.xml
     * If the response from the Server has the status 0, nothing is done,
     * If the response from the Server has the status 1, the View is displayed, or the customAction is
     * called, if given.
     * If the response from the Server has the status 2, the message from the Server is shown as a
     * display Alert, or the errorDialog Listener is called, if given.
     */
    private void checkAndProceedConsentUpdate() {
        // My CMP
        //  if (needsServerUpdate()) {
        if (needShowCMP(context)) {
            response = proceedServerRequest();
            switch (response.getStatus()) {
                case 0:
                    return;
                case 1:
                    CMPSettings.getInstance(context, SubjectToGdpr.CMPGDPRUnknown, response.getUrl(), null);
                    openCmpConsentToolView();
                    return;
                default:
                    showErrorMessage(response.getMessage());
            }
        }
    }

    // My CMP
    private static boolean compareNowLessFuture(Date futureDate) {
        Date now = new Date();
        if (now.compareTo(futureDate) <= 0) {
            return false;
        } else if (now.compareTo(futureDate) > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean needShowCMP(Context context) {
        String consentS = CMPStorageConsentManager.getConsentString(context);
        Date lastDate = CMPPrivateStorage.getLastRequested(context);

        if (consentS == null || consentS.isEmpty()) {
            // Reject -> Answer after x day or 365d
            if (lastDate != null) return compareNowLessFuture(lastDate);
            // First Time
            return true;
        } else {
            // Accepted -> Answer after 365d
            return compareNowLessFuture(lastDate);
        }
    }

    /**
     * Shows an error Message or calls the errorDialog Listener, if given. The message, that needs
     * to be displayed is the given parameter.
     *
     * @param message Error message that was send from the consentTool Server.
     */
    private void showErrorMessage(final String message) {
        if (errorDialog == null) {
            new AlertDialog.Builder(context)
                    .setTitle("Error")
                    .setMessage(message)
                    .setNegativeButton("Accept", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    })
                    .show();
        } else {
            errorDialog.onErrorOccur(message);
        }
    }

    /**
     * If the CMPSettings need to be updated from teh server, cause they were not at this day.
     *
     * @return If they need to be updated
     */
    private boolean needsServerUpdate() {
        return !calledThisDay();
    }

    /**
     * Proceeds the request to the Server declared in the CPMConfig and returns the ServerResponse.
     *
     * @return The Response from the Server
     */
    private ServerResponse proceedServerRequest() {
        try {
            response = ServerContacter.getAndSaveResponse(config, context);
            return response;
        } catch (CMPConsentToolNetworkException e) {
            if (networkErrorHandler != null) {
                networkErrorHandler.onErrorOccur(e.getMessage());
            }
        }
        return null;
    }

    /**
     * Returns if the server was already contacted this day.
     *
     * @return if the server was already contacted this day.
     */
    private boolean calledThisDay() {
        Date last = getCalledLast();
        if (last != null) {
            Date now = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            return formatter.format(now).equals(formatter.format(last));
        }
        return false;
    }

    /**
     * When the server was last contacted.
     *
     * @return teh date, when the server was contacted for the last time
     */
    private Date getCalledLast() {
        return CMPPrivateStorage.getLastRequested(context);
    }

    /**
     * Returns the Vendor String, that was set by consentmanager
     *
     * @return The String of vendors that was set through consentmanager
     */
    public String getVendorsString() {
        String value = CMPStorageConsentManager.getVendorsString(context);
        if (value != null) {
            return value;
        }
        return "";
    }

    /**
     * Returns the purposes String, that was set by consentmanager
     *
     * @return The String of purposes that was set through consentmanager
     */
    public String getPurposes() {
        String value = CMPStorageConsentManager.getPurposesString(context);
        if (value != null) {
            return value;
        }
        return "";
    }

    /**
     * Returns the US Privacy String, that was set by consentmanager
     *
     * @return The US Privacy String of vendors that was set through consentmanager
     */
    public String getUSPrivacyString() {
        String value = CMPStorageConsentManager.getUSPrivacyString(context);
        if (value != null) {
            return value;
        }
        return "";
    }

    /**
     * Returns, if the Vendor (id) has the rights to set cookies
     *
     * @param id          The ID of the vendor
     * @param isIABVendor If the vendor is set by IAB standard (V1 / V2)
     * @return If the Vendor has the Consent to set cookies.
     */
    public boolean hasVendorConsent(String id, boolean isIABVendor) {
        if (isIABVendor) {
//            String x = CMPStorageV2.getVendorConsents(context);
//            if( x.charAt(Integer.parseInt(id) + 1) == 1){
//                return true;
//            }
            String x = CMPStorageV1.getVendorsString(context);
            if (x.charAt(Integer.parseInt(id) + 1) == 1) {
                return true;
            }
            return false;
        } else {
            String x = CMPStorageConsentManager.getVendorsString(context);
            return x.contains(String.format(("_%s_"), x));
        }
    }

    /**
     * Returns, if the purpose (id) has the rights to set cookies
     *
     * @param id           The ID of the purpose
     * @param isIABPurpose If the purpose is set by IAB standard (V1 / V2)
     * @return If the purpose has the consent to set cookies.
     */
    public boolean hasPurposeConsent(String id, boolean isIABPurpose) {
        if (isIABPurpose) {
//            String x = CMPStorageV2.getPurposeConsents(context);
//            if (x.charAt(Integer.parseInt(id) + 1) == 1) {
//                return true;
//            }
            String x = CMPStorageV1.getPurposesString(context);
            if (x.charAt(Integer.parseInt(id) + 1) == 1) {
                return true;
            }
            return false;
        } else {
            String x = CMPStorageConsentManager.getPurposesString(context);
            return x.contains(String.format(("_%s_"), x));
        }
    }

}
