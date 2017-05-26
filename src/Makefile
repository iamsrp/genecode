all:
	@ant jar

doc:
	@ant javadoc

test:
	@ant test

strip:
	@for i in `find . -name '*.java'`; \
	do \
	    sed -i -e 's/  *$$//' $$i; \
	done

clean:
	@ant clean
	@find . -name '*~' | xargs -r rm
