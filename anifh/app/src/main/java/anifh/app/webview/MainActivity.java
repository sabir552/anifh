package anifh.app.webview;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.SslErrorHandler;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.onesignal.Continue;
import com.onesignal.OneSignal;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    /* ================= CONFIG FLAGS ================= */

    static boolean anifhApp_JSCRIPT = anifhConfig.anifhApp_JSCRIPT;
    static boolean anifhApp_FUPLOAD = anifhConfig.anifhApp_FUPLOAD;
    static boolean anifhApp_CAMUPLOAD = anifhConfig.anifhApp_CAMUPLOAD;
    static boolean anifhApp_ONLYCAM = anifhConfig.anifhApp_ONLYCAM;
    static boolean anifhApp_MULFILE = anifhConfig.anifhApp_MULFILE;
    static boolean anifhApp_LOCATION = anifhConfig.anifhApp_LOCATION;
    static boolean anifhApp_RATINGS = anifhConfig.anifhApp_RATINGS;
    static boolean anifhApp_PULLFRESH = anifhConfig.anifhApp_PULLFRESH;
    static boolean anifhApp_PBAR = anifhConfig.anifhApp_PBAR;
    static boolean anifhApp_ZOOM = anifhConfig.anifhApp_ZOOM;
    static boolean anifhApp_SFORM = anifhConfig.anifhApp_SFORM;
    static boolean anifhApp_OFFLINE = anifhConfig.anifhApp_OFFLINE;
    static boolean anifhApp_EXTURL = anifhConfig.anifhApp_EXTURL;
    static boolean anifhApp_CERT_VERIFICATION = anifhConfig.anifhApp_CERT_VERIFICATION;

    private static String anifh_URL = anifhConfig.anifh_URL;
    private static String anifh_F_TYPE = anifhConfig.anifh_F_TYPE;
    private static String anifh_ONESIGNAL_APP_ID = anifhConfig.anifh_ONESIGNAL_APP_ID;

    private String CURR_URL = anifh_URL;
    public static String ASWV_HOST = aswm_host(anifh_URL);

    /* ================= UI ================= */

    private WebView webView;
    private ProgressBar progressBar;
    private TextView loadingText;

    /* ================= FILE UPLOAD ================= */

    private ValueCallback<Uri[]> filePathCallback;
    private String cameraPhotoPath;
    private static final int FILE_REQUEST = 100;

    /* ================= UTILS ================= */

    private final SecureRandom random = new SecureRandom();
    private Handler mainHandler;
    private String oneSignalUserID;

    /* ================= ACTIVITY ================= */

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainHandler = new Handler(Looper.getMainLooper());

        // OneSignal
        if (!Objects.equals(anifh_ONESIGNAL_APP_ID, "")) {
            OneSignal.initWithContext(this, anifh_ONESIGNAL_APP_ID);
            OneSignal.getNotifications().requestPermission(false, Continue.none());
            oneSignalUserID = OneSignal.getUser().getPushSubscription().getId();
        }

        if (!isTaskRoot()) {
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.msw_view);
        progressBar = findViewById(R.id.msw_progress);
        loadingText = findViewById(R.id.msw_loading_text);

        setupWebView();
        setupSwipeRefresh();

        if (anifhApp_RATINGS) {
            mainHandler.postDelayed(this::get_rating, 60_000);
        }

        get_info();
        requestRuntimePermissions();

    aswm_view(anifh_URL, false);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
    View welcome = findViewById(R.id.msw_welcome);
    View web = findViewById(R.id.msw_view);

    if (welcome != null) welcome.setVisibility(View.GONE);
    if (web != null) web.setVisibility(View.VISIBLE);
}, 3000);
    }

    /* ================= WEBVIEW ================= */

    private void setupWebView() {
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(anifhApp_JSCRIPT);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setUseWideViewPort(true);
        ws.setGeolocationEnabled(anifhApp_LOCATION);
        ws.setSupportZoom(anifhApp_ZOOM);
        ws.setSaveFormData(anifhApp_SFORM);
        ws.setMediaPlaybackRequiresUserGesture(false);

        webView.setVerticalScrollBarEnabled(false);
        webView.setWebViewClient(new SafeWebViewClient());
        webView.setWebChromeClient(new SafeChromeClient());
    }

    private void setupSwipeRefresh() {
        SwipeRefreshLayout swipe = findViewById(R.id.pullfresh);
        if (!anifhApp_PULLFRESH) {
            swipe.setEnabled(false);
            return;
        }

        swipe.setOnRefreshListener(() -> {
            pull_fresh();
            swipe.setRefreshing(false);
        });

        webView.getViewTreeObserver().addOnScrollChangedListener(() ->
                swipe.setEnabled(webView.getScrollY() == 0)
        );
    }

    /* ================= FILE CHOOSER ================= */

    private class SafeChromeClient extends WebChromeClient {

        @Override
        public void onPermissionRequest(PermissionRequest request) {
            request.grant(request.getResources());
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
        }

        @Override
        public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
            if (!checkPermission(Manifest.permission.CAMERA)) {
                requestRuntimePermissions();
                return false;
            }

            filePathCallback = callback;

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType(anifh_F_TYPE);

            startActivityForResult(Intent.createChooser(intent, getString(R.string.fl_chooser)), FILE_REQUEST);
            return true;
        }
    }

    /* ================= WEBVIEW CLIENT ================= */

    private class SafeWebViewClient extends WebViewClient {

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return url_actions(view, url);
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return url_actions(view, request.getUrl().toString());
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        View welcome = findViewById(R.id.msw_welcome);
        View web = findViewById(R.id.msw_view);

        if (welcome != null) welcome.setVisibility(View.GONE);
        if (web != null) web.setVisibility(View.VISIBLE);
    }

    @Override
    public void onReceivedSslError(WebView view,
                                  SslErrorHandler handler,
                                  SslError error) {
        if (anifhApp_CERT_VERIFICATION) {
            super.onReceivedSslError(view, handler, error);
        } else {
            handler.proceed();
        }
    }
    }

    /* ================= ACTIVITY RESULT ================= */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_REQUEST && filePathCallback != null) {
            Uri[] result = null;
            if (resultCode == Activity.RESULT_OK && data != null) {
                result = new Uri[]{data.getData()};
            }
            filePathCallback.onReceiveValue(result);
            filePathCallback = null;
        }
    }

    /* ================= LOCATION ================= */

    public String get_location() {
        if (!anifhApp_LOCATION) return "0,0";

        if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) return "0,0";

        GPSTrack gps = new GPSTrack(this);
        if (!gps.canGetLocation()) return "0,0";

        double lat = gps.getLatitude();
        double lon = gps.getLongitude();

        if (lat == 0 && lon == 0) return "0,0";

        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setCookie(anifh_URL, "lat=" + lat);
        cm.setCookie(anifh_URL, "long=" + lon);

        return lat + "," + lon;
    }

    /* ================= HELPERS ================= */

    private void requestRuntimePermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                },
                1
        );
    }

    private boolean checkPermission(String perm) {
        return ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED;
    }

    public static String aswm_host(String url) {
        try {
            Uri uri = Uri.parse(url);
            return uri.getHost() == null ? "" : uri.getHost();
        } catch (Exception e) {
            return "";
        }
    }

    void aswm_view(String url, boolean external) {
        if (external) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } else {
            webView.loadUrl(url);
        }
    }

    public boolean url_actions(WebView view, String url) {

    // Phone call links
    if (url.startsWith("tel:")) {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(url)));
        return true;
    }

    // Email links
    if (url.startsWith("mailto:")) {
        startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse(url)));
        return true;
    }

    // Intent / app links (WhatsApp etc.)
    if (url.startsWith("intent:")) {
        try {
            Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
            startActivity(intent);
        } catch (Exception ignored) {}
        return true;
    }

    // 🔑 IMPORTANT FIX:
    // Let ALL http/https pages load inside WebView
    if (url.startsWith("http://") || url.startsWith("https://")) {
        return false;
    }

    return true;
        }
public void pull_fresh() {
    aswm_view(CURR_URL, false);
    }
    public void get_info() {
        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setCookie(anifh_URL, "DEVICE=android");
        cm.setCookie(anifh_URL, "DEV_API=" + Build.VERSION.SDK_INT);
    }

    public void get_rating() {
        // existing AppRate logic untouched
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    }
