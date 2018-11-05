//
//  RNSegmentIOAnalytics
//
//  Created by Tal Kain <tal@kain.net>.
//  Copyright (c) 2015 Fire Place Inc. All rights reserved.
//

#import <React/RCTBridgeModule.h>

#import "AppboyKit.h"
#import "IDFADelegate.h"
#import "SEGAppboyIntegrationFactory.h"
#import "SEGAppsFlyerIntegrationFactory.h"

/*
 * React native wrapper of the Segment.com's Analytics iOS SDK
 */
@interface RNSegmentIOAnalytics : NSObject <RCTBridgeModule, SEGAppsFlyerTrackerDelegate>

@end
