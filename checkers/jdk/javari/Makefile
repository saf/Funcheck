
# Variables
CHECKERS ?= ../..
CHECKERS_JAR ?= $(CHECKERS)/checkers.jar

LANGTOOLS ?= ../../../../jsr308-langtools
JAVADOCLIB ?= $(LANGTOOLS)/build/classes
JAVAC ?= $(LANGTOOLS)/dist/bin/javac -Xlint
JAVAP ?= $(LANGTOOLS)/dist/bin/javap

RTJAR ?= $(JAVA_HOME)/jre/lib/rt.jar

AFU_SCRIPTS ?= ../../../../annotation-tools/annotation-file-utilities/scripts

all: clean classes
# Separate make command because BUILD_CLASSES needs to be recomputed.
	$(MAKE) jaifs annotated

clean:
	rm -rf build

build: classes


############## Validate annotated jdk against missing methods ##############

SOURCES = $(shell find src/java src/javax -name '*.java')
CLASSES = $(shell find src/java src/javax -name '*.class')
DIFFS = $(patsubst src/%.java,build/%.diff, $(SOURCES))

.PHONY: classes
classes: $(SOURCES)
	mkdir -p build
	echo "building the jdk..."
	@$(JAVAC) -cp $(CHECKERS_JAR):$(JAVADOCLIB) -d build $?

# This rule does not seem to be used.
build/%.class: src/%.java
	mkdir -p build
	$(JAVAC) -sourcepath src -cp $(CHECKERS_JAR):$(JAVADOCLIB) -d build $<

build/%.expected: build/%.class
	$(JAVAP) -cp $(JAVADOCLIB) $(subst /,.,$*)  | grep public | sed 's/ native / /g' | sed 's/? extends java\/lang\/Object/?/g' | sort > $@

build/%.actual: build/%.class
	$(JAVAP) -Xbootclasspath/p:build $(subst /,.,$*)  | grep public | sed 's/ native / /g' | sed 's/? extends java\/lang\/Object/?/g' | sort > $@

build/%.diff: build/%.expected build/%.actual
	echo "validating class:" $(subst /,.,$*)
	diff $? > $@

validate: classes $(DIFFS)

############## Annotate the annotated jdk file ##############

# Problem: build/java doesn't exist until after the "classes" target has
# run!  This means that a new make process must be started, after the
# "classes" rule has executed, before any rule that uses these variables.
# Furthermore, these shell commands may issue errors saying that build/java
# does not exist.  So long as the variables are not used on such a make
# invocation, this doesn't cause a problem, but it is irritating (and a bit
# sloppy).
BUILD_CLASSES = $(shell find build/java build/javax -name '*.class')
JAIFS = $(patsubst build/%.class,build/%.jaif, $(BUILD_CLASSES))
ANNOTATED = $(patsubst build/%.class,annotated/%.class, $(BUILD_CLASSES))

build/%.jaif: build/%.class
	$(AFU_SCRIPTS)/extract-annotations '$<'

jaifs: $(JAIFS)

annotated/%.class: build/%.jaif
	$(AFU_SCRIPTS)/insert-annotations '../$@' '$<'

annotated: $(ANNOTATED)

showvars:
	pwd
	@echo BUILD_CLASSES: ${BUILD_CLASSES}
	@echo JAIFS: ${JAIFS}
	@echo ANNOTATED: ${ANNOTATED}
