SRC = $(shell find . -regex ".+\.java")

MyMines.jar: $(SRC)
	@rm -rf build && mkdir build
	@echo compiling...
	@javac -d build -source 1.6 -target 1.6 -nowarn $(SRC)
	@echo packaging...
	@jar cfm MyMines.jar MANIFEST.MF -C build .

clean: 
	@rm -f MyMines.jar
	@rm -rf build

test: MyMines.jar
	@java -cp MyMines.jar Test

run: MyMines.jar
	@java -jar MyMines.jar

.PHONY: test run clean