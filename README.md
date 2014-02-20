distributed-documentor
======================

Overview
--------

Desktop application providing a collaborated way of creating software documentation with wiki syntax.

* Uses Mercurial repositories to store documentations
* Pages are written in MediaWiki format, with live preview
* Documentation can be exported to static HTML or CHM

Building
--------
First install the wikitext libraries to your local repository using `lib/install-local.sh` or `lib/install-local.bat`.

Then to build the WebStart application use:
```
mvn webstart:jnlp
```

To create a single JAR:

```
mvn assembly:assembly
```
