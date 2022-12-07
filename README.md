# Analogue Clock View library for Android
[![](https://jitpack.io/v/naz013/clock-view.svg)](https://jitpack.io/#naz013/clock-view)

<img src="https://github.com/naz013/analogueclock-android/raw/master/res/icon.png" width="100" alt="Analogue Clock View library for Android">

Simple analog clock view library for Android.

Inspired by this work - [UpLabs](https://www.uplabs.com/posts/ios-clock-app-light-and-dark-theme)
--------

Screenshot

<img src="https://github.com/naz013/analogueclock-android/raw/master/res/scr_1.png" width="400" alt="Screenshot">
<img src="https://github.com/naz013/analogueclock-android/raw/master/res/scr_2.png" width="400" alt="Screenshot">

Sample APP
--------
[Download](https://github.com/naz013/analogueclock-android/raw/master/app/release/app-release.apk)

[Google Play](https://play.google.com/store/apps/details?id=com.github.naz013.clockviewlibrary)


Download
--------
Download latest version with Gradle:
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.naz013:clock-view:1.0.0'
}
```

Usage
-----
```xml
<com.github.naz013.analoguewatch.AnalogueClockView
    android:id="@+id/clockView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:clock_backgroundColor="@color/clock_background"
    app:clock_hourTickColor="@color/clock_hour_tick"
    app:clock_hourTickWidth="0.06"
    app:clock_innerCircleColor="@color/clock_pin_circle"
    app:clock_innerCircleSize="0.07"
    app:clock_labelsColor="@color/theme_switch"
    app:clock_labelsTextSize="18sp"
    app:clock_minuteTickColor="@color/clock_minute_tick"
    app:clock_minuteTickWidth="0.03"
    app:clock_pinColor="@color/clock_pin"
    app:clock_secondsTickColor="@color/clock_seconds_tick"
    app:clock_secondsTickWidth="0.003"
    app:clock_shadowColor="@color/clock_shadow"
    app:clock_showHourLabel="true"
    app:clock_showInnerCircleBorder="@bool/show_inner_circle_border"
    app:clock_showSecondsTick="true"
    app:clock_showShadow="@bool/clock_shadow" />
```


License
-------

    Copyright 2022 Nazar Suhovich

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
