; DataVision Host Installer script created by the HM NIS Edit Script Wizard (then hand-modified by hand)

; Date: 2008-06-13

; Author: Jonathan Marsden

; HM NIS Edit Wizard helper defines
!define PRODUCT_NAME "DataVision"
!define PRODUCT_VERSION "1.2.0"
!define PRODUCT_PUBLISHER "DataVision"
!define PRODUCT_WEB_SITE "http://datavision.sourceforge.net"
!define PRODUCT_UNINST_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}"
!define PRODUCT_UNINST_ROOT_KEY "HKLM"

; File locations on build host
!define BUILD_DIR ".."

; MUI 1.67 compatible ------
!include "MUI.nsh"

; MUI Settings
!define MUI_ABORTWARNING
!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\modern-install.ico"
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\modern-uninstall.ico"

Name "${PRODUCT_NAME} ${PRODUCT_VERSION}"
OutFile "datavision-${PRODUCT_VERSION}-setup.exe"
InstallDir "$PROGRAMFILES\DataVision"
ShowInstDetails show
ShowUnInstDetails show

; Welcome page
!insertmacro MUI_PAGE_WELCOME
; License page
!insertmacro MUI_PAGE_LICENSE "${BUILD_DIR}\COPYING"
; Directory page
!insertmacro MUI_PAGE_DIRECTORY
; Instfiles page
!insertmacro MUI_PAGE_INSTFILES
; Finish page
!insertmacro MUI_PAGE_FINISH

; Uninstaller pages
!insertmacro MUI_UNPAGE_INSTFILES

; Language files
!insertmacro MUI_LANGUAGE "English"

; MUI end ------

Section "MainSection" SEC01
  SetOutPath "$INSTDIR"
  SetOverwrite ifnewer
  File /r /x .svn /x packaging /x javadoc /home/jonathan/projects/datavision-1.2.0/*.*

  SetShellVarContext all
  CreateShortCut "$DESKTOP\${PRODUCT_NAME}.lnk" "$INSTDIR\datavision.bat"

; Check that Java is installed, install Java 6u7 if not.
  Call JRE

  SetAutoClose false
SectionEnd

Section -AdditionalIcons
  SetOutPath $INSTDIR
  SetShellVarContext all
  WriteIniStr "$INSTDIR\${PRODUCT_NAME}.url" "InternetShortcut" "URL" "${PRODUCT_WEB_SITE}"
  CreateDirectory "$SMPROGRAMS\DataVision"
  CreateShortCut "$SMPROGRAMS\DataVision\DataVision.lnk" "$INSTDIR\datavision.bat"
  CreateShortCut "$SMPROGRAMS\DataVision\Website.lnk" "$INSTDIR\${PRODUCT_NAME}.url"
  CreateShortCut "$SMPROGRAMS\DataVision\Uninstall.lnk" "$INSTDIR\uninst.exe"
SectionEnd

Section -Post
  WriteUninstaller "$INSTDIR\uninst.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayName" "$(^Name)"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "UninstallString" "$INSTDIR\uninst.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayVersion" "${PRODUCT_VERSION}"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "URLInfoAbout" "${PRODUCT_WEB_SITE}"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "Publisher" "${PRODUCT_PUBLISHER}"
SectionEnd

; This is the install function for Java 6 Update 7 jre
; Note that DataVision will run with any Sun Java JRE >= 1.4 -- can we test for that?  ** FIXME **
Function JRE
  SetOutPath "$PROGRAMFILES\Temp"
  IfFileExists $PROGRAMFILES\Java\jre1.6.0_07\README.txt endJRE
    MessageBox MB_ICONQUESTION|MB_YESNO "This system does not appear to have the Sun Java 6 Update 7 JRE installed.$\n$\nWould you like it to be installed now?" IDYES installJRE IDNO endJRE
  installJRE:
    File "jre-6u7-windows-i586-p-s.exe"
    ExecWait "$PROGRAMFILES\Temp\jre-6u7-windows-i586-p-s.exe"
  endJRE:
FunctionEnd

Function un.onUninstSuccess
  HideWindow
  MessageBox MB_ICONINFORMATION|MB_OK "$(^Name) was successfully removed from your computer."
FunctionEnd

Function un.onInit
  MessageBox MB_ICONQUESTION|MB_YESNO|MB_DEFBUTTON2 "Are you sure you want to completely remove $(^Name) and all of its components?" IDYES +2
  Abort
FunctionEnd

Section Uninstall
  Delete "$INSTDIR\${PRODUCT_NAME}.url"
  Delete "$INSTDIR\uninst.exe"
  Delete "$INSTDIR\*.*"
!include datavision-uninstall.nsi
  RMDir "$INSTDIR"
  
  SetShellVarContext all
  Delete "$SMPROGRAMS\DataVision\DataVision.lnk"
  Delete "$SMPROGRAMS\DataVision\Uninstall.lnk"
  Delete "$SMPROGRAMS\DataVision\Website.lnk"
  RMDir "$SMPROGRAMS\DataVision"

  Delete "$DESKTOP\${PRODUCT_NAME}.lnk"

  DeleteRegKey ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}"
  SetAutoClose true
SectionEnd
