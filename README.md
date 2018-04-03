PRT Status for Android
=====

[https://prtstat.us](https://prtstat.us)

![prt_promotion](https://raw.githubusercontent.com/AustinDizzy/prtstatus-android/master/artwork/listing-promo.png)

[![get_it_on_play](https://developer.android.com/images/brand/en_generic_rgb_wo_60.png)](https://play.google.com/store/apps/details?id=me.austindizzy.wvuprtstatus.app)

[As featured on WVUToday](http://wvutoday.wvu.edu/n/2014/08/15/student-employee-develops-wvu-prt-status-app-to-alert-riders-about-service), this Android application is to help the community at West Virginia University track the uptime status of the [Personal Rapid Transit \(PRT\)](https://transportation.wvu.edu/prt) and be notified instantly of any service disruptions.

The app receives data from [a companion server](https://github.com/AustinDizzy/prtstatus-go), which is written in Go and running on Google App Engine, using Firebase Cloud Messaging.

## Technologies

The Android app currently supports Android versions 4.0.3 (ICS) through 8.1 (Oreo) and up, using:
* [Room](https://developer.android.com/reference/android/arch/persistence/room/package-summary.html) - for local data storage
* [Volley](https://github.com/google/volley) - for making and managing network requests
* [Firebase](htps://firebase.google.com) - for sending event notifications and messages to clients, displaying ads from [AdMob](https://ww.google.com/admob/), and collecting app analytics


## License

This project is free and open source software, with usage provided under the [MIT License](https://abs.mit-license.org). See [LICENSE](./LICENSE) for full details.

## Issues & Contributing

Notice an issue and know how to fix it? Pull requests are welcome! After your code has been reviewed and proven sound, I'll accept the pull request and my local CI will pull and prepare the app as an alpha build on Google Play, to be upgraded to a full release at my discretion.

Please also feel free to [post an issue](https://github.com/AustinDizzy/prtstatus-android/issues/new) with any other feature requests, bugs, or general questions about the project.