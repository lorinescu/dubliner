
Simple screen scraper for Elite:Dangerous Power Play data.

Install [JRE 8u60](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html) or higher.

Download latest release from [here](http://occams.pub/ed/dubliner/) , unpack and click on start.bat

To use from command line, open a command prompt, switch to the Dubliner directory and type:
```
 start.bat -cli some\image\file.bmp
```

Dev environment:

* If you get a "Error: Illegal min or max specification!" error set in your
environment "LC_ALL=C" ([known bug](https://code.google.com/p/tesseract-ocr/issues/detail?id=1467))
* Running the app or it's tests will fail unless the "dataPath" setting (in conf/settings.json ) points at
the training data dirs in [dubliner-data repository](https://bitbucket.org/lorinescu/dubliner-data) or your own 
trained data. In my environment I checkout dubliner and dubliner-data in the same parent directory, switch to 
dubliner directory and create a symbolic link to the data repo (in linux: ln -s ../dubliner-data data or in 
windows: mklink /D data ..\dubliner-data).
    
Notes:

* [all tesseract 3.0.2 params](http://www.sk-spell.sk.cx/tesseract-ocr-parameters-in-302-version)
