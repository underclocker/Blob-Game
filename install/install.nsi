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
  OutFile "BlobInstall.exe"

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
  File "LICENSE.txt"
  File "libgdx-LICENSE.txt"
  File "box2dlights-LICENSE.txt"
  File "json-LICENSE.txt"
  File "poly2tri-LICENSE.txt"

  ;Store installation folder
  WriteRegStr HKCU "Software\Blob Game" "" $INSTDIR

  ; Add/Remove Programs entry
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Blob Game" \
                   "DisplayName" "Blob Game -- It's a game! With Blobs!"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Blob Game" \
                   "UninstallString" "$\"$INSTDIR\uninstall.exe$\""

  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"


SectionEnd

Section "Uninstall"

  Delete "$INSTDIR\*"
  Delete "$INSTDIR\*.*"
  RMDir "$INSTDIR"

  DeleteRegKey /ifempty HKCU "Software\Blob Game"
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Blob Game"


SectionEnd
