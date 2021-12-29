default:
	/opt/arduino-1.8.19/arduino --verify irobot.ino

upload:
	/opt/arduino-1.8.19/arduino --upload irobot.ino

monitor:
	minicom -D /dev/ttyUSB0 -b 115200

format:
	@clang-format -i irobot.ino
