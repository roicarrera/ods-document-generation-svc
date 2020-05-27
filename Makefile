.PHONY: clean
clean:
	./gradlew clean

.PHONY: wkhtmltopdf
wkhtmltopdf:
	which wkhtmltopdf &> /dev/null || (echo "ERROR: Missing wkhtmltopdf binary. Download from https://wkhtmltopdf.org/" && exit 1)

.PHONY: test
test: wkhtmltopdf
	./gradlew test

.PHONY: run
run: wkhtmltopdf
	./gradlew run

.PHONY: shadowJar
shadowJar: wkhtmltopdf
	./gradlew shadowJar
