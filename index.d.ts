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
  }
}

