Packaging README

Packages envisioned include:

 * Tarball (as done previously)
 * .exe installer for Windows (using NSIS)
 * .deb for Ubuntu and Debian Linuxes
 * .rpm for Feedora and RHEL and CentOS Linuxes
 * Java Web Start
 * Something (.dmg ?) for OS X

Tarball: Already works, except for maintaining exec bits

Windows: First experimental .exe installer now exists.

Windows installer TODO: 

1) Generate uninstall file list automatically using Ant
2) Set version in datavision.nsi automatically using Ant
3) Integrate .exe build process using Ant
4) Add pretty icon to both installer and .bat file shortcut
5) Add ability to select what parts of DV to install?
6) Add ability to view README/launch DV on final installer dialog?
