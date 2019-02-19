//
//  RNSegmentIOAnalytics
//
//  Created by Tal Kain <tal@kain.net>.
//  Copyright (c) 2015 Fire Place Inc. All rights reserved.
//

#if __has_include(<React/RCTBridgeModule.h>) //ver >= 0.40
#import <React/RCTBridgeModule.h>
#import <React/RCTEventDispatcher.h>
#else //ver < 0.40
#import "RCTBridgeModule.h"
#import "RCTEventDispatcher.h"
#endif

#import "AppboyKit.h"
#import "IDFADelegate.h"
#import "SEGAppboyIntegrationFactory.h"
#import "SEGAppsFlyerIntegrationFactory.h"

/*
 * React native wrapper of the Segment.com's Analytics iOS SDK
 */
@interface RNSegmentIOAnalytics : NSObject <RCTBridgeModule, SEGAppsFlyerTrackerDelegate>

@end
