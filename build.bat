@echo off
jpackage --type app-image --name balls --input demo/target --main-jar demo-1.0-SNAPSHOT.jar --main-class com.example.App --dest run_tree

xcopy assets\ run_tree\balls\assets\ /E /q /y