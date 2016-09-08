TODO:

* (request) support for Preparation and Expansion tabs
* reduce memory footprint - currently all original screenshots and the extracted segments sit in memory. they should be loaded only when needed.
* system names have a common OCR problem O is recognized as 0 almost every time
* in a few cases the undermine trigger value is not extracted correctly as no space is "seen" between "TRIGGER" and the actual counter
* show more information about data extraction progress - current image processed out of n, estimated duration, make a sound when done
* scale down a bit the layout, on smaller screens not everything is visible
* in the system ListView mark entries that have invalid data
* use SuperCSV or similar for generating CSV files instead of joining strings with commas
* have a (user) configurable worker pool for processing images in parallel
* to keep the game's screenshot directory clean, move interesting images to a spool directory and process them there
* installer, see [NSIS](http://nsis.sourceforge.net/Java_Launcher_with_automatic_JRE_installation)

META TODO:

* have a central point for collecting power play data; engage EDDN guys for getting an extended PP schema
* add support for mobile API and fortification trade routes; this a vague idea for having a way to build up a trade database and in turn 
an economy alongside fortification runs. for example it would be interesting to pull commodity pricing from systems in Mahon space volume
by polling often the cmdrs 'last station' data 
