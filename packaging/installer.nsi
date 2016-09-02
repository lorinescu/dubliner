; The name of the installer
Name "Dubliner"

; The file to write
OutFile "Dubliner-0.0.2.exe"

; The default installation directory
InstallDir $PROGRAMFILES\Dubliner

; Registry key to check for directory (so if you install again, it will 
; overwrite the old one automatically)
InstallDirRegKey HKLM "Software\NSIS_Dubliner" "Install_Dir"

; Request application privileges for Windows Vista
RequestExecutionLevel highest

;--------------------------------

; Pages

Page components
Page directory
Page instfiles

UninstPage uninstConfirm
UninstPage instfiles

;--------------------------------

; The stuff to install
Section "Dubliner (required)"

  SectionIn RO
  
  SetOutPath $INSTDIR
  
  ; Put file there
  File /oname=dubliner.jar "..\target\dubliner-0.0.2-SNAPSHOT.jar"
  File "start.bat"

  CreateDirectory "$INSTDIR\conf"
  File /oname=conf\log4j.properties "..\conf\log4j.properties"
  File /oname=conf\settings.json "..\conf\settings.json"
  
  ;CreateDirectory "$INSTDIR\data"
  SetOutPath "$INSTDIR\data"
  File /a /r "..\data\training"

  SetOutPath "$INSTDIR"
  CreateDirectory "$INSTDIR\logs"

  ; Write the installation path into the registry
  WriteRegStr HKLM SOFTWARE\NSIS_Dubliner "Install_Dir" "$INSTDIR"
  
  ; Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Dubliner" "DisplayName" "Dubliner"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Dubliner" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Dubliner" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Dubliner" "NoRepair" 1
  WriteUninstaller "uninstall.exe"
  
SectionEnd

; Optional section (can be disabled by the user)
Section "Start Menu Shortcuts"

  CreateDirectory "$SMPROGRAMS\Dubliner"
  CreateShortcut "$SMPROGRAMS\Dubliner\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 0
  CreateShortcut "$SMPROGRAMS\Dubliner\Dubliner.lnk" "$INSTDIR\start.bat" "" "$INSTDIR\start.bat" 0
  
SectionEnd

;--------------------------------

; Uninstaller

Section "Uninstall"
  
  ; Remove registry keys
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Dubliner"
  DeleteRegKey HKLM SOFTWARE\NSIS_Dubliner

  ; Remove files and uninstaller
  Delete $INSTDIR\Dubliner
  Delete $INSTDIR\uninstall.exe

  ; Remove shortcuts, if any
  Delete "$SMPROGRAMS\Dubliner"

  ; Remove directories used
  RMDir "$SMPROGRAMS\Dubliner"
  RMDir "$INSTDIR"

SectionEnd
