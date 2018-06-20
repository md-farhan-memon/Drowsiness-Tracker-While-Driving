PULSE
===========

# You snooze, you loose!
We have a tendency to push our limits and not accept the negatives. In our everyday lives, it allows us to push further. However, on the road, this behavior doesn’t translate well. Almost a third of the accidents on Indian roads are because drivers are either unable, or ignorant, in identifying that they are sleepy and continue to drive nevertheless. The magnitude goes up when you look at the truckers, where the wages rely on getting goods from A to B earliest. Truckers willingly drive for as much as 20 hours a day under pressure which results into shocking 57 percent of casualties in trucking-related accidents being caused by drowsy driving.

Leading auto companies address this with complicated solutions involving multiple cameras and sensors fitted to premium cars, accessible to less than 1 percent of the car buyers in India. We are tackling drowsy driving by using basic fitness trackers and using the heart rate data to identify when the driver is drowsy, or drunk.

The heart rate starts dropping as a person starts getting sleepy allowing us to identify drowsy drivers. The data is used to send audio and visual alerts. The Android app, which will also be rolled out as an SDK, also tracks for how long someone has been driving, and sends a prompt to take a break. The entire tech solution would minimize the incidents of drowsy driving by prompting people to take breaks during long drives and alerting them when they are drowsy.

<<<<<<< HEAD
* You're more likely to die from drowsy driving than from texting while driving, distracted driving or drunk driving combined
* The results show that many drivers ignore official advice from the NHAI to take a 15-mins break every two hours on a long journey.
* Existing solutions are vision based and expensive
* Cops cant detect drowsy drivers like they can detect drunk drivers
=======
[Blog](https://blog.freeyourgadget.org)
>>>>>>> master

## Our Solution
* Tracking the Pulse of the driver and detecting whether the driver is sleepy or not
* Using the existing fitness band with heart rate sensors to do that

## Why is our Solution feasible?
* No additional hardware required
* An already existing fitness band market is expected to grow
* The fitness bands value proposition adds to road safety
* Can work offline as Indian Highways have patchy networks

## Tech stack used
* Java (Android)
* Connecting to Smart bands using BLE (Bluetooth Low Energy)
* Detecting drowsiness through Pulse Rate
* GPS Tracking

## Potential impact
* Reduced accidents due to drowsiness using:
    *  Pulse rate tracking and alerting user if he seems drowsy
    *  Timely alerts if he is driving for long hours continuously
    *  Tapping into the competitive nature of people by Gamifying the driving behaviour and motivate them to drive better
* Fitness band turned Life-saver

# ScreenShots

![Screen 1](https://www.dropbox.com/s/ullf06vj6p7vix3/Hear%20rate%201.png?dl=1)

![Screen 2](https://www.dropbox.com/s/9e7po9xi7lw4sl3/Heart%20rate%202%20%281%29.png?dl=1)

![Screen 3](https://www.dropbox.com/s/iuytb6xaaogxqzc/Heart%20Rate%203.png?dl=1)

<<<<<<< HEAD
![Screen 4](https://www.dropbox.com/s/ahcarg6lvazi0eh/Long%20hours%20driving%201.png?dl=1)
=======
## Getting Started (Pebble)

1. Pair your Pebble through the Android's Bluetooth Settings or Gadgetbridge. Pebble 2 MUST be paired though Gadgetbridge (tap on the + in Control Center)
2. Start Gadgetbridge, tap on the device you want to connect to
3. To test, choose "Debug" from the menu and play around

For more information read [this wiki article](https://github.com/Freeyourgadget/Gadgetbridge/wiki/Pebble-Getting-Started) 

## How to use (Mi Band 1+2)

* When starting Gadgetbridge the first time, it will automatically
  attempt to discover and pair your Mi Band. Alternatively you can invoke discovery
  manually via the "+" button. It will ask you for some personal info that appears
  to be needed for proper steps calculation on the band. If you do not provide these,
  some hardcoded default "dummy" values will be used instead. 

  When your Mi Band starts to vibrate and blink during the pairing process,
  tap it quickly a few times in a row to confirm the pairing with the band.

1. Configure other notifications as desired
2. Go back to the "Gadgetbridge" activity
3. Tap the Mi Band item to connect if you're not connected yet
4. To test, chose "Debug" from the menu and play around

**Known Issues:**

* The initial connection to a Mi Band sometimes takes a little patience. Try to connect a few times, wait, 
  and try connecting again. This only happens until you have "bonded" with the Mi Band, i.e. until it 
  knows your MAC address. This behavior may also only occur with older firmware versions.
* If you use other apps like Mi Fit, and "bonding" with Gadgetbridge does not work, please
  try to unpair the band in the other app and try again with Gadgetbridge.
* While all Mi Band devices are supported, some firmware versions might work better than others.
  You can consult the [projects wiki pages](https://github.com/Freeyourgadget/Gadgetbridge/wiki/Mi-Band) 
  to check if your firmware version is fully supported or if an upgrade/downgrade might be beneficial.
* In order to display text notifications on the Mi Band 2, you have to [install a font on the band](https://github.com/Freeyourgadget/Gadgetbridge/wiki/Mi-Band-2).

## Features (Liveview)

* set time (automatically upon connection)
* display notifications and vibrate

## Authors
### Core Team (in order of first code contribution)

* Andreas Shimokawa
* Carsten Pfeiffer
* Daniele Gobbetti

### Additional device support

* João Paulo Barraca (HPlus)
* Vitaly Svyastyn (NO.1 F1)
* Sami Alaoui (Teclast H30)

## Contribute

Contributions are welcome, be it feedback, bug reports, documentation, translation, research or code. Feel free to work
on any of the open [issues](https://github.com/Freeyourgadget/Gadgetbridge/issues?q=is%3Aopen+is%3Aissue);
just leave a comment that you're working on one to avoid duplicated work.

Translations can be contributed via https://hosted.weblate.org/projects/freeyourgadget/gadgetbridge/

## Do you have further questions or feedback?

Feel free to open an issue on our issue tracker, but please:
- do not use the issue tracker as a forum, do not ask for ETAs and read the issue conversation before posting
- use the search functionality to ensure that your question wasn't already answered. Don't forget to check the **closed** issues as well!
- remember that this is a community project, people are contributing in their free time because they like doing so: don't take the fun away! Be kind and constructive.

## Having problems?

0. Phone crashing during device discovery? Disable Privacy Guard (or similarly named functionality) during discovery.
1. Open Gadgetbridge's settings and check the option to write log files
2. Reproduce the problem you encountered
3. Check the logfile at /sdcard/Android/data/nodomain.freeyourgadget.gadgetbridge/files/gadgetbridge.log
4. File an issue at https://github.com/Freeyourgadget/Gadgetbridge/issues/new and possibly provide the logfile

Alternatively you may use the standard logcat functionality to access the log.
>>>>>>> master

![Screen 5](https://www.dropbox.com/s/ht4vg1qt54ggqr5/Long%20Hours%20driving%202.png?dl=1)