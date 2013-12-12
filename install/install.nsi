;NSIS Modern User Interface
;Welcome/Finish Page Example Script
;Written by Joost Verburg

;--------------------------------
;Include Modern UI

  !include "MUI2.nsh"

;--------------------------------
;General

  ;Name and file
  Name "Blob Game"
  OutFile "Blob Game Setup.exe"

  ;Default installation folder
  InstallDir "C:\Blob Game"

  ;Get installation folder from registry if available
  InstallDirRegKey HKCU "Software\Blob Game" ""

  ;Request application privileges for Windows Vista
  RequestExecutionLevel user

;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING
  !define MUI_ICON "blob.ico"
  !define MUI_UNICON "blob.ico"
  !define MUI_HEADERIMAGE_BITMAP "blob.bmp"
  !define MUI_HEADERIMAGE_UNBITMAP "blob.bmp"

;--------------------------------
;Pages

  !insertmacro MUI_PAGE_WELCOME
  !insertmacro MUI_PAGE_LICENSE "LICENSE.txt"
  !insertmacro MUI_PAGE_DIRECTORY

  !define MUI_STARTMENUPAGE_REGISTRY_ROOT "HKCU"
  !define MUI_STARTMENUPAGE_REGISTREY_KEY "Software\Blob Game"
  !define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "Start Menu Folder"
  Var StartMenuFolder
  !insertmacro MUI_PAGE_STARTMENU Application $StartMenuFolder
  !insertmacro MUI_PAGE_INSTFILES
  !insertmacro MUI_PAGE_FINISH

  !insertmacro MUI_UNPAGE_WELCOME
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  !insertmacro MUI_UNPAGE_FINISH

;--------------------------------
;Languages

  !insertmacro MUI_LANGUAGE "English"

;--------------------------------
;Installer Sections

Section "Blob Game" SecBlobGame

  SetOutPath "$INSTDIR"

  File "..\Blob Game.jar"
  File "blob.ico"
  File "jre-7u45-windows-i586-iftw.exe"
  File "LICENSE.txt"
  File "libgdx-LICENSE.txt"
  File "box2dlights-LICENSE.txt"
  File "json-LICENSE.txt"
  File "poly2tri-LICENSE.txt"

  ;Store installation folder
  WriteRegStr HKCU "Software\Blob Game" "" $INSTDIR

  ; Add/Remove Programs entry
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Blob Game" \
                   "DisplayName" "Blob Game"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Blob Game" \
                   "DisplayIcon" "$\"$INSTDIR\blob.ico$\""
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Blob Game" \
                   "DisplayVersion" "1.0"
  WriteRegDword HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Blob Game" \
                   "EstimatedSize" "51200"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Blob Game" \
                   "UninstallString" "$\"$INSTDIR\uninstall.exe$\""

  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
    CreateDirectory "$SMPROGRAMS\$StartMenuFolder"
    CreateShortCut "$DESKTOP\Blob Game.lnk" "$INSTDIR\Blob Game.jar" "" "$INSTDIR\blob.ico"
    CreateShortCut "$SMPROGRAMS\$StartMenuFolder\Blob Game.lnk" "$INSTDIR\Blob Game.jar" "" "$INSTDIR\blob.ico"
    CreateShortCut "$SMPROGRAMS\$StartMenuFolder\Uninstall.lnk" "$INSTDIR\Uninstall.exe"
  !insertmacro MUI_STARTMENU_WRITE_END
 
  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"


SectionEnd

Section "Uninstall"

  Delete "$INSTDIR\*"
  Delete "$INSTDIR\*.*"
  RMDir "$INSTDIR"

  !insertmacro MUI_STARTMENU_GETFOLDER Application $StartMenuFolder

  Delete "$DESKTOP\Blob Game.lnk" 
  Delete "$SMPROGRAMS\$StartMenuFolder\Uninstall.lnk"
  Delete "$SMPROGRAMS\$StartMenuFolder\Blob Game.lnk"
  RMDir "$SMPROGRAMS\$StartMenuFolder"

  DeleteRegKey /ifempty HKCU "Software\Blob Game"
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Blob Game"


SectionEnd
