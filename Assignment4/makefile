JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
    Id3Entry.java \
    Id3Node.java \
    Id3Impl.java \
	Id3Test.java 

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
