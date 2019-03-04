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

@import AppsFlyerTracker;

@implementation RNSegmentIOAnalytics

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(setup:(NSString*)configKey :(NSUInteger)flushAt :(BOOL)shouldUseLocationServices)
{
    @try {
        SEGAnalyticsConfiguration *configuration = [SEGAnalyticsConfiguration configurationWithWriteKey:configKey];
        [[SEGAppboyIntegrationFactory instance] saveLaunchOptions:_bridge.launchOptions];
        [configuration use:[SEGAppboyIntegrationFactory instance]];
        [configuration use:[SEGAppsFlyerIntegrationFactory createWithLaunchDelegate:self]];
        configuration.enableAdvertisingTracking = YES;       //OPTIONAL
        configuration.flushAt = flushAt;
        configuration.recordScreenViews = NO; // Enable this to record screen views automatically!
        configuration.trackApplicationLifecycleEvents = YES; //OPTIONAL
        configuration.trackDeepLinks = YES;                  //OPTIONAL
        configuration.trackPushNotifications = YES;          //OPTIONAL
        configuration.trackAttributionData = YES;            //OPTIONAL
        configuration.shouldUseLocationServices = shouldUseLocationServices;
        #if DEBUG
        [SEGAnalytics debug:YES];
        #else
        [SEGAnalytics debug:NO];
        #endif
        [SEGAnalytics setupWithConfiguration:configuration];
        // On iOS we use method swizzling to intercept lifecycle events
        // However, React-Native calls our library after applicationDidFinishLaunchingWithOptions: is called
        // We fix this by manually calling this method at setup-time
        if (configuration.trackApplicationLifecycleEvents) {
            SEL selector = @selector(_applicationDidFinishLaunchingWithOptions:);
            if ([SEGAnalytics.sharedAnalytics respondsToSelector:selector]) {
                [SEGAnalytics.sharedAnalytics performSelector:selector
                                                withObject:_bridge.launchOptions];
            }
        }
    }
    @catch (NSException * e) {
        NSLog(@"Exception: %@", e);
    }
    @finally {
        NSLog(@"finally");
    }
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

RCT_REMAP_METHOD(appsFlyerId,
                 appsFlyerIdWithResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)
{
  NSString *appsflyerId = [[AppsFlyerTracker sharedTracker] getAppsFlyerUID];
  resolve(appsflyerId);
}

-(void)onConversionDataReceived:(NSDictionary*) installData {
    id status = [installData objectForKey:@"af_status"];
    if ([status isEqualToString:@"Non-organic"]) {
        id sourceID = [installData objectForKey:@"media_source"];
        id campaign = [installData objectForKey:@"campaign"];
        NSLog(@"This is a none organic install. Media source: %@  Campaign: %@",sourceID,campaign);
    } else if([status isEqualToString:@"Organic"]) {
        NSLog(@"This is an organic install.");
    }

    NSDictionary* message = @{
                              @"status": @"success",
                              @"type": @"onInstallConversionDataLoaded",
                              @"data": installData
                              };
    [self performSelectorOnMainThread:@selector(handleCallback:) withObject:message waitUntilDone:NO];
}

-(void)onConversionDataRequestFailure:(NSError *) _errorMessage {
    NSLog(@"%@",_errorMessage);
    NSDictionary* errorMessage = @{
                                   @"status": @"failure",
                                   @"type": @"onInstallConversionFailure",
                                   @"data": _errorMessage.localizedDescription
                                   };
    
    [self performSelectorOnMainThread:@selector(handleCallback:) withObject:errorMessage waitUntilDone:NO];
}

- (void) onAppOpenAttribution:(NSDictionary*) attributionData {
    NSDictionary* message = @{
                                @"status": @"success",
                                @"type": @"onAppOpenAttribution",
                                @"data": attributionData
                            };
    
    [self performSelectorOnMainThread:@selector(handleCallback:) withObject:message waitUntilDone:NO];
}

- (void) onAppOpenAttributionFailure:(NSError *)_errorMessage {
    NSDictionary* errorMessage = @{
                                   @"status": @"failure",
                                   @"type": @"onAttributionFailure",
                                   @"data": _errorMessage.localizedDescription
                                 };

    [self performSelectorOnMainThread:@selector(handleCallback:) withObject:errorMessage waitUntilDone:NO];
}

-(void) handleCallback:(NSDictionary *) message {
    NSError *error;
    
    if ([NSJSONSerialization isValidJSONObject:message]) {
        NSData *jsonMessage = [NSJSONSerialization dataWithJSONObject:message
                                                              options:0
                                                                error:&error];
        if (jsonMessage) {
            NSString *jsonMessageStr = [[NSString alloc] initWithBytes:[jsonMessage bytes] length:[jsonMessage length] encoding:NSUTF8StringEncoding];
            NSString* status = (NSString*)[message objectForKey: @"status"];
            
            if ([status isEqualToString:@"success"]) {
                [self reportOnSuccess:jsonMessageStr];
            } else {
                [self reportOnFailure:jsonMessageStr];
            }
            
            NSLog(@"jsonMessageStr = %@", jsonMessageStr);
        } else {
            NSLog(@"%@", error);
        }
    } else {
       [self reportOnFailure:@"failed to parse response"];
    }
}

-(void) reportOnFailure:(NSString *) errorMessage {
    [_bridge.eventDispatcher sendAppEventWithName:@"onInstallConversionData" body:errorMessage];
}

-(void) reportOnSuccess:(NSString *) data {
    [_bridge.eventDispatcher sendAppEventWithName:@"onInstallConversionData" body:data];
}

@end
