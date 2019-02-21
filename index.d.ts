declare module 'react-native-analytics' {
  export default class Segment {
    static setup(
      segmentIOWriteKey: string,
      flushEverySecondsCount: number,
      shouldUseLocationServices?: boolean
    ): void
    static identify(userId: string, traits?: any, options?: any): void
    static track(eventName: string, eventProperties?: any, options?: any): void
    static screen(
      screenName: string,
      screenProperties?: any,
      options?: any
    ): void
    static flush(): void
    static reset(): void
    static debug(isEnabled: boolean): void
    static enable(): void
    static disable(): void
    static appsFlyerId(): Promise<string | null>
    static onInstallConversionData(
      callback: (data: any) => void
    ): AppsFlyerInstallConversionDisposer
  }

  interface IAppsFlyerDeviceIdWrapper {
    appsFlyerId: string
  }
  export interface IAppsFlyerSDKOptions {
    devKey: string
    isDebug: boolean
    appId?: string
  }
  export interface IAppsFlyerEventProps {
    context: {
      device: {
        type: string
        advertisingId: string
      }
    }
    integrations: {
      AppsFlyer: IAppsFlyerDeviceIdWrapper | boolean
    }
  }
  export interface IAppsFlyerEmailOptions {
    emails: string[]
    emailsCryptType: 0 | 1 | 2 // NONE - 0 (default), SHA1 - 1, MD5 - 2
  }
  export type AppsFlyerInstallConversionDisposer = () => void

}

