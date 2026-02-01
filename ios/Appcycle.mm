#import "Appcycle.h"
#import <UIKit/UIKit.h>

@implementation Appcycle {
  BOOL isForeground;
  BOOL hasListeners;
}

RCT_EXPORT_MODULE();

+ (BOOL)requiresMainQueueSetup {
  return YES;
}

- (instancetype)init {
  if (self = [super init]) {
    isForeground = ([UIApplication sharedApplication].applicationState == UIApplicationStateActive);
    hasListeners = NO;

    NSNotificationCenter *center = [NSNotificationCenter defaultCenter];

    [center addObserver:self
               selector:@selector(onForeground)
                   name:UIApplicationDidBecomeActiveNotification
                 object:nil];

    [center addObserver:self
               selector:@selector(onBackground)
                   name:UIApplicationWillResignActiveNotification
                 object:nil];
  }
  return self;
}

- (void)dealloc {
  [[NSNotificationCenter defaultCenter] removeObserver:self];
}

#pragma mark - Events

- (NSArray<NSString *> *)supportedEvents {
  return @[@"onForeground", @"onBackground"];
}

- (void)startObserving {
  hasListeners = YES;
}

- (void)stopObserving {
  hasListeners = NO;
}

- (void)onForeground {
  if (!isForeground) {
    isForeground = YES;
    if (hasListeners) {
      [self sendEventWithName:@"onForeground" body:nil];
    }
  }
}

- (void)onBackground {
  if (isForeground) {
    isForeground = NO;
    if (hasListeners) {
      [self sendEventWithName:@"onBackground" body:nil];
    }
  }
}

#pragma mark - JS API

RCT_EXPORT_METHOD(getCurrentState:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
  resolve(isForeground ? @"foreground" : @"background");
}

// Required by NativeEventEmitter
RCT_EXPORT_METHOD(addListener:(NSString *)eventName) {}
RCT_EXPORT_METHOD(removeListeners:(double)count) {}

@end
