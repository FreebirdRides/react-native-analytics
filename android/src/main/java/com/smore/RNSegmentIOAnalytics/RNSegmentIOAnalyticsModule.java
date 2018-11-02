package com.smore.RNSegmentIOAnalytics;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;

import com.facebook.react.bridge.ReadableType;
import com.segment.analytics.Analytics;
import com.segment.analytics.Analytics.Builder;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;
import com.segment.analytics.Options;
import android.util.Log;
import android.content.Context;

import com.appsflyer.AppsFlyerLib;
import com.appsflyer.AppsFlyerConversionListener;
import java.util.Map;
import com.segment.analytics.android.integrations.appsflyer.AppsflyerIntegration;
import com.segment.analytics.android.integrations.appboy.AppboyIntegration;

public class RNSegmentIOAnalyticsModule extends ReactContextBaseJavaModule {
  private static Analytics mAnalytics = null;
  private Boolean mEnabled = true;
  private Boolean mDebug = false;

  @Override
  public String getName() {
    return "RNSegmentIOAnalytics";
  }

  private void log(String message) {
    Log.d("RNSegmentIOAnalytics", message);
  }

  public RNSegmentIOAnalyticsModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  /*
   https://segment.com/docs/libraries/android/#identify
   */
  @ReactMethod
  public void setup(String writeKey, Integer flushAt, Boolean shouldUseLocationServices) {
    if (mAnalytics == null) {
      Context context = getReactApplicationContext().getApplicationContext();
      Builder builder = new Analytics.Builder(context, writeKey);
      builder.flushQueueSize(flushAt);

      if (mDebug) {
        builder.logLevel(Analytics.LogLevel.DEBUG);
      }

      AppsflyerIntegration.cld = new AppsflyerIntegration.ConversionListenerDisplay() {
        @Override
        public void display(Map<String, String> attributionData) {
          for (String attrName : attributionData.keySet()) {
            log("attribute: " + attrName + " = " + attributionData.get(attrName));
          }
        }
      };

      mAnalytics = builder
        .trackApplicationLifecycleEvents() // Enable this to record certain application events automatically!
        .use(AppsflyerIntegration.FACTORY) // https://github.com/AppsFlyerSDK/AppsFlyer-Segment-Integration#android
        .use(AppboyIntegration.FACTORY) // https://github.com/Appboy/appboy-segment-android
        .build();
      // Set the initialized instance as a globally accessible instance.
      Analytics.setSingletonInstance(mAnalytics);
    } else {
      log("Segment Analytics already initialized. Refusing to re-initialize.");
    }
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
  public String appsFlyerId(Promise promise) {
    Context context = getReactApplicationContext().getApplicationContext();
    String appsFlyerId = AppsFlyerLib.getInstance().getAppsFlyerUID(context);
    promise.resolve(appsFlyerId);
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
