### Reset JetBrains IDEs Evaluation Period on Windows
```bat
@echo off
SETLOCAL

REM Define IDE names
SET IDE_NAMES="WebStorm" "IntelliJ" "CLion" "Rider" "GoLand" "PhpStorm" "Resharper" "PyCharm" "DataGrip"

REM Delete eval folder with license key and options.xml which contains a reference to it
FOR %%I IN (%IDE_NAMES%) DO (
    FOR /D %%a IN ("%USERPROFILE%\.%%I*") DO (
        IF EXIST "%%a\config\eval" (
            RD /S /Q "%%a\config\eval"
        )
        IF EXIST "%%a\config\options\other.xml" (
            DEL /Q "%%a\config\options\other.xml"
        )
    )
)

REM Delete registry key and JetBrains folder
IF EXIST "%APPDATA%\JetBrains" (
    RMDIR /S /Q "%APPDATA%\JetBrains"
)

REG DELETE "HKEY_CURRENT_USER\Software\JavaSoft" /f

ENDLOCAL
```

### Reset JetBrains IDEs Evaluation Period on macOS
```shell
#!/bin/bash

for product in IntelliJIdea WebStorm DataGrip PhpStorm CLion PyCharm GoLand RubyMine Rider; do
  #force close product
  ps aux | grep -i MacOs/$product | cut -d " " -f 5 | xargs kill -9

  #removing evaluation key
  rm -rf ~/Library/Preferences/$product*/eval
  rm -rf ~/Library/Application\ Support/JetBrains/$product*/eval

  #removing all evlsprt properties in options.xml...
  sed -i '' '/evlsprt/d' ~/Library/Preferences/$product*/options/other.xml
  sed -i '' '/evlsprt/d' ~/Library/Application\ Support/JetBrains/$product*/options/other.xml

  echo
done

#removing additional plist files...
rm -f ~/Library/Preferences/com.apple.java.util.prefs.plist
rm -f ~/Library/Preferences/com.jetbrains.*.plist
rm -f ~/Library/Preferences/jetbrains.*.*.plist

#restarting cfprefsd
killall cfprefsd

echo
echo "Done!"
```