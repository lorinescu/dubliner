
Simple screen scraper for Elite:Dangerous Power Play and system data.

Install [JRE 8u60](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html) or higher.

Download latest release from [here](http://occams.pub/ed/dubliner/) , unpack and click on start.bat .

To use from command line, open a command prompt, switch to the Dubliner directory and type:
```
 start.bat -cli some\image\file-or-directory
```

Dev environment:

* Source code sits in [bitbucket](https://bitbucket.org/lorinescu/dubliner)
* If you get a "Error: Illegal min or max specification!" error set in your
environment "LC_ALL=C" ([known bug](https://code.google.com/p/tesseract-ocr/issues/detail?id=1467))
* Running the app or its tests will fail unless the "dataPath" setting (in conf/settings.json ) points at
the training data dirs in [dubliner-data repository](https://bitbucket.org/lorinescu/dubliner-data) or your own 
trained data. In my environment I check out dubliner and dubliner-data in the same parent directory, switch to 
dubliner directory and create a symbolic link to the data repo (in linux: ln -s ../dubliner-data data or in 
windows: mklink /D data ..\dubliner-data).
* JavaFX SceneBuilder might not load some fxml file. Usually this is related to having custom/3rd party UI controls 
(ex: ControlFX) that are not on the classpath. The solution is to update the classpath by editing app/SceneBuilder.cfg 
file in the SceneBuilder installation directory.
    
Notes:

* [all tesseract 3.0.2 params](http://www.sk-spell.sk.cx/tesseract-ocr-parameters-in-302-version)


Changelog:

* 0.0.4
    * improved power name character recognition - Tesseract does not deal well with huge fonts so scaling
    up the power name rectangle does not help at all
    * additional corrections for marker words variations
    * configurable csv export separator
    * missing separator in csv header
    * WIP - mock-up UI for system reports
    * WIP - ImageApi plumbing changes to support other type of screenshots besides PP
* 0.0.3
    * support for Preparation and Expansion tabs
    * reduce memory footprint - all original screenshots and the extracted segments no longer sit in memory and are loaded only when needed
    * notification sound after finishing an OCR task
    * when clicking on an a text field with the OCR results show a popup with raw OCR output
    * show more information about data extraction progress: current processed image, % of work done
    * use populated system list from EDDB as a whitelist for system name corrections
* 0.0.2
    * control screen data extraction for all powers
    * using OpenCV for automatically identifying data rectangles
    * -cli parameter now accepts directories and will deep search for bmp files
    * -debug parameter will generate excessive debugging data in out/ dir
    * removed some unused UI elements and improved existing
    * updated system name corrections
* 0.0.1 - initial version

TODO:

* (request) dynamically reload settings and allow to manually re-trigger an individual image processing
* (request) traffic report, bounty hounter report, crime report and top 5 bounties
* system names have a common OCR problem - O is recognized as 0 almost every time
* in a few cases the undermine trigger value is not extracted correctly as no space is "seen" between "TRIGGER" and the actual counter
* Control and Expansion screen data extraction flows are similar, they could be merged somehow
* scale down a bit the layout, on smaller screens not everything is visible
* in the system ListView mark entries that have invalid data
* use SuperCSV or similar for generating CSV files instead of joining strings with commas
* have a (user) configurable worker pool for processing images in parallel
* to keep the game's screenshot directory clean, move interesting images to a spool directory and process them there
* installer, see [NSIS](http://nsis.sourceforge.net/Java_Launcher_with_automatic_JRE_installation)
