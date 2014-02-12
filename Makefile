MyMines.jar: $(shell ls|grep java)
	javac -d . -source 1.6 -target 1.6 *.java
	jar cvfm MyMines.jar MANIFEST.MF *.class > /dev/null
	rm *.class

test: MyMines.jar
	java -cp MyMines.jar Test

run: MyMines.jar
	java -jar MyMines.jar

.PHONY: test run