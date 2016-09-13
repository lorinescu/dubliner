* 0.0.3
    * support for Preparation and Expansion tabs
    * reduce memory footprint - all original screenshots and the extracted segments no longer sit in memory and are loaded only when needed
    * notification sound after finishing an OCR task
    * when clicking on an a text field with the OCR results show a popup with raw OCR output
    * show more information about data extraction progress: current processed image, % of work done
* 0.0.2
    * control screen data extraction for all powers
    * using OpenCV for automatically identifying data rectangles
    * -cli parameter now accepts directories and will deep search for bmp files
    * -debug parameter will generate excessive debugging data in out/ dir
    * removed some unused UI elements and improved existing
    * updated system name corrections
* 0.0.1 - initial version