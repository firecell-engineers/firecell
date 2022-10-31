javac -h c -d libCppUtils ../src/main/java/pl/edu/agh/firecell/engine/utils/cCodeManager.java
g++ -c utils.cpp -o libCppUtils.o -I "C:\Program Files\jdk\jdk-16.0.2\include" -I "C:\Program Files\jdk\jdk-16.0.2\include\win32"
g++ -shared -o libCppUtils.dll libCppUtils.o -Wl,--kill-at
del libCppUtils.o
rmdir c /s /q
rmdir libCppUtils /s /q