
Simple screen scraper for Elite:Dangerous Power Play and system data.

Install [JRE 8u60](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html) or higher.

Download latest release from [here](http://occams.pub/ed/dubliner/) , unpack and click on start.bat .

To use from command line, open a command prompt, switch to the Dubliner directory and type:
```
 start.bat -cli some\image\file-or-directory
```

Dev environment:

* If you get a "Error: Illegal min or max specification!" error set in your
environment "LC_ALL=C" ([known bug](https://code.google.com/p/tesseract-ocr/issues/detail?id=1467))
* Running the app or its tests will fail unless the "dataPath" setting (in conf/settings.json ) points at
the training data dirs in [dubliner-data repository](https://bitbucket.org/lorinescu/dubliner-data) or your own 
trained data. In my environment I check out dubliner and dubliner-data in the same parent directory, switch to 
dubliner directory and create a symbolic link to the data repo (in linux: ln -s ../dubliner-data data or in 
windows: mklink /D data ..\dubliner-data).
    
Notes:

* [all tesseract 3.0.2 params](http://www.sk-spell.sk.cx/tesseract-ocr-parameters-in-302-version)


Changelog:

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