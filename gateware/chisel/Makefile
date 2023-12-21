SOURCES=$(shell find . -name "*.scala")

.PHONY: build
build: build/top.sv
build/top.sv: $(SCALA_SOURCES)
	mkdir -p build
	sbt --supershell=never 'runMain main.GenerateVerilog'

.PHONY: test
test:
	sbt test

.PHONY: test
clean:
	-rm -r test_run_dir
	-rm -r target
	-rm -r build
	-rm -r test_build

