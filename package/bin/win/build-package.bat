set INSTALLER_TYPE=%1
set INPUT=%2
set MAIN_JAR=%3
set MAIN_CLASS=%4
set APP_NAME=%5
set APP_VERSION=%6
set APP_ICON=%7
set OUTPUT=%8

call "jpackage" ^
    --type "%INSTALLER_TYPE%" ^
    --input "%INPUT%" ^
    --main-jar "%MAIN_JAR%" ^
    --main-class "%MAIN_CLASS%" ^
    --name "%APP_NAME%" ^
    --app-version "%APP_VERSION%" ^
    --icon "%APP_ICON%" ^
    --dest "%OUTPUT%" ^
    --module-path "%MODULE_PATH%" ^
    --win-dir-chooser ^
    --win-shortcut ^
    --win-menu ^
    --verbose
