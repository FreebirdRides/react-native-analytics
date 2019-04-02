package com.smore.RNSegmentIOAnalytics;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.segment.analytics.Analytics;
import com.segment.analytics.Analytics.Builder;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;
import com.segment.analytics.Options;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.appsflyer.AppsFlyerLib;
import com.appsflyer.AppsFlyerConversionListener;
import com.segment.analytics.android.integrations.appsflyer.AppsflyerIntegration;
import com.segment.analytics.android.integrations.appboy.AppboyIntegration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.Exception;
import java.util.Map;

public class RNSegmentIOAnalyticsModule extends ReactContextBaseJavaModule {

  static final String AF_SEGMENT_SHARED_PREF = "appsflyer-segment-data";
  static final String ATTR_KEY = "AF_onInstall_Attr";
  static final String CONV_KEY = "AF_onConversion_Data";

  private static Analytics mAnalytics = null;
  private Boolean mEnabled = true;
  private Boolean mDebug = false;
  private ReactApplicationContext reactContext;
  private Application application;

  @Override
  public String getName() {
    return "RNSegmentIOAnalytics";
  }

  private void log(String message) {
    Log.d("RNSegmentIOAnalytics", message);
  }

  public RNSegmentIOAnalyticsModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    this.application = (Application) reactContext.getApplicationContext();
  }

  private boolean getFlag(final String key) {
    // Context context = getContext();
    Context context = this.application;

    if (context == null) {
      return false;
    }

    SharedPreferences sharedPreferences = context.getSharedPreferences(AF_SEGMENT_SHARED_PREF, 0);
    return sharedPreferences.getBoolean(key, false);
  }

  /*
   https://segment.com/docs/libraries/android/#identify
   */
  @ReactMethod
  public void setup(String writeKey, Integer flushAt, Boolean shouldUseLocationServices) {
    if (mAnalytics == null) {
      // Context context = getReactApplicationContext().getApplicationContext();
      Builder builder = new Analytics.Builder(this.application, writeKey);
      builder.flushQueueSize(flushAt);

      if (mDebug) {
        builder.logLevel(Analytics.LogLevel.DEBUG);
      }

      AppsflyerIntegration.cld = new AppsflyerIntegration.ConversionListenerDisplay() {
        @Override
        public void display(Map<String, String> attributionData) {
          AppsFlyerConversionListener listener = registerConversionListener();
          for (String attrName : attributionData.keySet()) {
            log("attribute: " + attrName + " = " + attributionData.get(attrName));
          }
          if (getFlag(ATTR_KEY)) {
            listener.onAppOpenAttribution(attributionData);
          }
          if (getFlag(CONV_KEY)) {
            listener.onInstallConversionDataLoaded(attributionData);
          }
        }
      };

      // AppsFlyerLib instance = AppsFlyerLib.getInstance();
      // instance.init("", registerConversionListener(), this.application.getApplicationContext());

      mAnalytics = builder
        .use(AppsflyerIntegration.FACTORY) // https://github.com/AppsFlyerSDK/AppsFlyer-Segment-Integration#android
        .use(AppboyIntegration.FACTORY) // https://github.com/Appboy/appboy-segment-android
        .trackAttributionInformation() // Install Attributed event
        .trackApplicationLifecycleEvents() // Enable this to record certain application events automatically!
        .build();
      // Set the initialized instance as a globally accessible instance.
      Analytics.setSingletonInstance(mAnalytics);
      
    } else {
      log("Segment Analytics already initialized. Refusing to re-initialize.");
    }
  }

  private AppsFlyerConversionListener registerConversionListener() {
    return new AppsFlyerConversionListener() {

      @Override
      public void onAppOpenAttribution(Map<String, String> attributionData) {
        handleSuccess("onAppOpenAttribution", attributionData);
      }

      @Override
      public void onAttributionFailure(String errorMessage) {
        handleError("onAttributionFailure", errorMessage);
      }

      @Override
      public void onInstallConversionDataLoaded(Map<String, String> conversionData) {
        handleSuccess("onInstallConversionDataLoaded", conversionData);
      }

      @Override
      public void onInstallConversionFailure(String errorMessage) {
        handleError("onInstallConversionFailure", errorMessage);
      }

      private void handleSuccess(String eventType, Map<String, String> data) {
        JSONObject obj = new JSONObject();

        try {
          obj.put("status", "success");
          obj.put("type", eventType);
          obj.put("data", new JSONObject(data));
          sendEvent(reactContext, "onInstallConversionData", obj.toString());
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }

      private void handleError(String eventType, String errorMessage) {
        JSONObject obj = new JSONObject();

        try {
          obj.put("status", "failure");
          obj.put("type", eventType);
          obj.put("data", errorMessage);
          sendEvent(reactContext, "onInstallConversionData", obj.toString());
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }

      private void sendEvent(ReactContext reactContext, String eventName, Object params) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
      }
    };
  }

  /*
   https://segment.com/docs/libraries/android/#identify
   */
  @ReactMethod
  public void identify(String userId, ReadableMap traits, ReadableMap options) {
    if (!mEnabled) {
      return;
    }
    mAnalytics.identify(userId, toTraits(traits), toOptions(options));
  }

  /*
   https://segment.com/docs/libraries/android/#alias
   */
  @ReactMethod
  public void alias(String newId) {
    if (!mEnabled) {
      return;
    }
    mAnalytics.alias(newId);
  }

  /*
   https://segment.com/docs/libraries/android/#track
   */
  @ReactMethod
  public void track(String trackText, ReadableMap properties, ReadableMap options) {
    if (!mEnabled) {
      return;
    }
    mAnalytics.track(trackText, toProperties(properties), toOptions(options));
  }

  /*
   https://segment.com/docs/libraries/android/#screen
   */
  @ReactMethod
  public void screen(String screenName, ReadableMap properties, ReadableMap options) {
    if (!mEnabled) {
      return;
    }
    mAnalytics.screen(null, screenName, toProperties(properties), toOptions(options));
  }

  /*
   https://segment.com/docs/libraries/android/#flushing
   */
  @ReactMethod
  public void flush() {
    mAnalytics.flush();
  }

  /*
   https://segment.com/docs/libraries/android/#reset
   */
  @ReactMethod
  public void reset() {
    mAnalytics.reset();
  }

  /*
   https://segment.com/docs/libraries/android/#logging
   */
  @ReactMethod
  public void debug(Boolean isEnabled) {
    if (isEnabled == mDebug) {
      return;
    } else if (mAnalytics == null) {
      mDebug = isEnabled;
    } else {
      log("On Android, debug level may not be changed after calling setup");
    }
  }

  /*
   https://segment.com/docs/libraries/android/#opt-out
   */
  @ReactMethod
  public void disable() {
    mEnabled = false;
  }

  /*
   https://segment.com/docs/libraries/android/#opt-out
   */
  @ReactMethod
  public void enable() {
    mEnabled = true;
  }

  @ReactMethod
  public void appsFlyerId(Promise promise) {
    try {
      // Context context = this.application.getApplicationContext();
      String appsFlyerId = AppsFlyerLib.getInstance().getAppsFlyerUID(this.application);
      promise.resolve(appsFlyerId);
    } catch (Exception e) {
      promise.reject("Segment", e);
    }
  }

  private Properties toProperties (ReadableMap map) {
    if (map == null) {
      return new Properties();
    }
    Properties props = new Properties();

    ReadableMapKeySetIterator iterator = map.keySetIterator();
    while (iterator.hasNextKey()) {
      String key = iterator.nextKey();
      ReadableType type = map.getType(key);
      switch (type){
        case Array:
          props.putValue(key, map.getArray(key));
          break;
        case Boolean:
          props.putValue(key, map.getBoolean(key));
          break;
        case Map:
          props.putValue(key, toProperties(map.getMap(key)));
          break;
        case Null:
          props.putValue(key, null);
          break;
        case Number:
          props.putValue(key, map.getDouble(key));
          break;
        case String:
          props.putValue(key, map.getString(key));
          break;
        default:
          log("Unknown type:" + type.name());
          break;
      }
    }
    return props;
  }

  private Traits toTraits (ReadableMap map) {
    if (map == null) {
      return new Traits();
    }
    Traits traits = new Traits();

    ReadableMapKeySetIterator iterator = map.keySetIterator();
    while (iterator.hasNextKey()) {
      String key = iterator.nextKey();
      ReadableType type = map.getType(key);
      switch (type){
        case Array:
          traits.putValue(key, map.getArray(key));
          break;
        case Boolean:
          traits.putValue(key, map.getBoolean(key));
          break;
        case Map:
          traits.putValue(key, toProperties(map.getMap(key)));
          break;
        case Null:
          traits.putValue(key, null);
          break;
        case Number:
          traits.putValue(key, map.getDouble(key));
          break;
        case String:
          traits.putValue(key, map.getString(key));
          break;
        default:
          log("Unknown type:" + type.name());
          break;
      }
    }
    return traits;
  }

  private Options toOptions (ReadableMap map) {
    Options options = new Options();
    if (map == null) {
      return options;
    }

    options.setIntegration("AppsFlyer", true);
    options.setIntegrationOptions("AppsFlyer", toProperties(map));

    return options;
  }
}
