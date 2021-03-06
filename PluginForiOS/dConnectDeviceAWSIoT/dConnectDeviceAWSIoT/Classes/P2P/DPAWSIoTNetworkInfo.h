//
//  DPAWSIoTNetworkInfo.h
//  dConnectDeviceAWSIoT
//
//  Copyright (c) 2016 NTT DOCOMO, INC.
//  Released under the MIT license
//  http://opensource.org/licenses/mit-license.php
//

#import <Foundation/Foundation.h>

@interface DPAWSIoTNetworkInfo : NSObject

+ (NSString *) getLocalIp;
+ (NSString *) getGlobalIp;

@end
