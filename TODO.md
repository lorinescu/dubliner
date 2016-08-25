TODO:

* (request) support for Preparation and Expansion tabs
* (request) support other power's scraping
* reduce memory footprint - currently all original screenshots and the extracted segments sit in memory. they should be loaded only when needed.
* use SuperCSV or similar for generating CSV files instead of joining strings with commas
* have a (user) configurable worker pool for processing images in parallel
* to keep the game's screenshot directory clean, move interesting images to a spool directory and process them there
* use relative instead of absolute segment coordinates depending on esolution if Elite scales UI elements based on some formula
* installer, see [NSIS](http://nsis.sourceforge.net/Java_Launcher_with_automatic_JRE_installation)

META TODO:

* have a central point for collecting power play data; engage EDDN guys for getting an extended PP schema
* add support for mobile API and fortification trade routes; this a vague idea for having a way to build up a trade database and in turn 
an economy alongside fortification runs. for example it would be interesting to pull commodity pricing from systems in Mahon space volume
by polling often the cmdrs 'last station' data 
