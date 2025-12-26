package anifh.app.webview;

class anifhConfig {

    /* -- CONFIG VARIABLES -- */

    //complete URL of your anifh website
    static String anifh_URL = "https://anifh.com/";

    // OneSignal APP Id
    static String anifh_ONESIGNAL_APP_ID = "";


    /* -- PERMISSION VARIABLES -- */

    // enable JavaScript for webview
    static boolean anifhApp_JSCRIPT = true;

    // upload file from webview
    static boolean anifhApp_FUPLOAD = true;

    // enable upload from camera for photos
    static boolean anifhApp_CAMUPLOAD = true;

    // incase you want only camera files to upload
    static boolean anifhApp_ONLYCAM = false;

    // upload multiple files in webview
    static boolean anifhApp_MULFILE = true;

    // track GPS locations
    static boolean anifhApp_LOCATION = true;

    // show ratings dialog; auto configured
    // edit method get_rating() for customizations
    static boolean anifhApp_RATINGS = true;

    // pull refresh current url
    static boolean anifhApp_PULLFRESH = true;

    // show progress bar in app
    static boolean anifhApp_PBAR = true;

    // zoom control for webpages view
    static boolean anifhApp_ZOOM = false;

    // save form cache and auto-fill information
    static boolean anifhApp_SFORM = false;

    // whether the loading webpages are offline or online
    static boolean anifhApp_OFFLINE = false;

    // open external url with default browser instead of app webview
    static boolean anifhApp_EXTURL = false;


    /* -- SECURITY VARIABLES -- */

    // verify whether HTTPS port needs certificate verification
    static boolean anifhApp_CERT_VERIFICATION = true;

    //to upload any file type using "*/*"; check file type references for more
    static String anifh_F_TYPE = "*/*";


    /* -- RATING SYSTEM VARIABLES -- */

    static int ASWR_DAYS = 3;    // after how many days of usage would you like to show the dialoge
    static int ASWR_TIMES = 10;  // overall request launch times being ignored
    static int ASWR_INTERVAL = 2;   // reminding users to rate after days interval
}
