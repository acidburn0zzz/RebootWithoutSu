lib/AndroidHiddenAPI.jar: AndroidHiddenAPI/android/os/SystemProperties.java
	javac AndroidHiddenAPI/android/os/*.java
	cd AndroidHiddenAPI; jar -cvf ../lib/AndroidHiddenAPI.jar android/os/SystemProperties.class
