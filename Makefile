SRC = \
src/CubieCube.java \
src/Phase1Center.java \
src/Phase1Search.java \
src/Phase2Center.java \
src/Phase2Search.java \
src/Phase3Center.java \
src/Phase3Edge.java \
src/Phase3Search.java \
src/Phase4Center.java \
src/Phase4Edge.java \
src/Phase4Search.java \
src/Phase5Center.java \
src/Phase5Edge.java \
src/Phase5Search.java \
src/PhaseSearch.java \
src/Search.java \
src/SolutionChecker.java \
src/SolvingCube.java \
src/Tools.java \
src/Util.java

JAVAC = javac -target 8 -source 8

MAINPROG = example/MainProgram.java

TEST = example/test.java

DIST = dist/cube555.jar

TWOPHASE = lib/twophase.jar

.PHONY: build clean run test

build: $(DIST)

$(DIST): $(SRC) $(MAINPROG)
	@$(JAVAC) -d dist -cp $(TWOPHASE) $(SRC) $(MAINPROG)
	@cp -f $(SRC) dist/cs/cube555/
	@cp -r lib dist
	@cd dist && jar cfm cube555.jar ../cube555.mf lib ui/*.class cs/cube555/*.class cs/cube555/*.java

run: $(DIST)
	@java -jar $(DIST)

test: $(DIST) $(TEST)
	@$(JAVAC) -d dist -cp $(DIST) $(TEST)
	@java -cp dist:$(DIST) test

clean:
	@rm -rf dist/*