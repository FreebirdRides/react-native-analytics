//
//  RNSegmentIOAnalytics
//
//  Created by Tal Kain <tal@kain.net>.
//  Copyright (c) 2015 Fire Place Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <React/RCTConvert.h>
//#import "SEGAnalytics.h"
#import <Analytics/SEGAnalytics.h>
#import "RNSegmentIOAnalytics.h"

#import "AppboyKit.h"
#import "IDFADelegate.h"
#import "SEGAppboyIntegrationFactory.h"
#import "SEGAppsFlyerIntegrationFactory.h"

@implementation RNSegmentIOAnalytics

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(setup:(NSString*)configKey :(NSUInteger)flushAt :(BOOL)shouldUseLocationServices)
{
    SEGAnalyticsConfiguration *configuration = [SEGAnalyticsConfiguration configurationWithWriteKey:configKey];
    configuration.flushAt = flushAt;
    configuration.shouldUseLocationServices = shouldUseLocationServices;
    configuration.recordScreenViews = NO; // Enable this to record screen views automatically!
    configuration.trackApplicationLifecycleEvents = NO; // Enable this to record certain application events automatically!
//    [[SEGAppboyIntegrationFactory instance] saveLaunchOptions:launchOptions];
    [configuration use:[SEGAppboyIntegrationFactory instance]];
    [configuration use:[SEGAppsFlyerIntegrationFactory instance]];
    configuration.enableAdvertisingTracking = YES;       //OPTIONAL
    configuration.trackApplicationLifecycleEvents = YES; //OPTIONAL
    configuration.trackDeepLinks = YES;                  //OPTIONAL
    configuration.trackPushNotifications = YES;          //OPTIONAL
    configuration.trackAttributionData = YES;            //OPTIONAL
    #if DEBUG
    [SEGAnalytics debug:YES];
    #else
    [SEGAnalytics debug:NO];
    #endif
    [SEGAnalytics setupWithConfiguration:configuration];
}

/*
 https://segment.com/docs/libraries/ios/#identify
 */
RCT_EXPORT_METHOD(identify:(NSString*)userId traits:(NSDictionary *)traits options:(NSDictionary *)options) {
    [[SEGAnalytics sharedAnalytics] identify:userId traits:traits options:options];
}

/*
 https://segment.com/docs/libraries/ios/#alias
 */
RCT_EXPORT_METHOD(alias:(NSString*)userId) {
    [[SEGAnalytics sharedAnalytics] alias:userId];
}

/*
 https://segment.com/docs/libraries/ios/#track
 */
RCT_EXPORT_METHOD(track:(NSString*)event properties:(NSDictionary *)properties options:(NSDictionary *)options) {
    [[SEGAnalytics sharedAnalytics] track:event properties:properties options:options];
}
/*
 https://segment.com/docs/libraries/ios/#screen
 */
RCT_EXPORT_METHOD(screen:(NSString*)screenName properties:(NSDictionary *)properties options:(NSDictionary *)options) {
    [[SEGAnalytics sharedAnalytics] screen:screenName properties:properties options:options];
}

/*
 https://segment.com/docs/libraries/ios/#flushing
 */
RCT_EXPORT_METHOD(flush) {
    [[SEGAnalytics sharedAnalytics] flush];
}

/*
 https://segment.com/docs/libraries/ios/#reset
 */
RCT_EXPORT_METHOD(reset) {
    [[SEGAnalytics sharedAnalytics] reset];
}

/*
 https://segment.com/docs/libraries/ios/#logging
 */
RCT_EXPORT_METHOD(debug: (BOOL)isEnabled) {
    [SEGAnalytics debug:isEnabled];
}

/*
 https://segment.com/docs/libraries/ios/#opt-out
 */
RCT_EXPORT_METHOD(disable) {
    [[SEGAnalytics sharedAnalytics] disable];
}

/*
 https://segment.com/docs/libraries/ios/#opt-out
 */
RCT_EXPORT_METHOD(enable) {
    [[SEGAnalytics sharedAnalytics] enable];
}

@end
