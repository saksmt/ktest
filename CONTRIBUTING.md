# Contributing to kTest

## Issues

If you wan't to submit new issue first check if it doesn't exist already!

### Submitting bugs

[Submit a bug](https://github.com/saksmt/ktest/issues/new?template=bug.md)

1. Provide full description of bug
2. It would be great if you provide sample project reproducing that bug
3. Even greater will be if you'll provide solution to fix that bug :)

### Submitting feature requests

[Create new feature request](https://github.com/saksmt/ktest/issues/new?template=feature_request.md)

1. Provide full usage example for feature request
2. Provide your use-case for such feature

### Asking question

[Ask question](https://github.com/saksmt/ktest/issues/new?template=question.md)

1. Check if there is already an answer for your question
2. Try to use stackoverflow (will be grateful for creation of specialized tag for that)
3. Create an issue

## Pull-requests

### Codestyle

Please follow default codestyle of IntelliJ IDEA it is tweaked through [.editorconfig](.editorconfig)

#### Bugs

1. Provide full description of bug
2. Don't forget to write test for it if possible

#### Features

1. Provide full usage example for feature
2. Provide use-case
3. Provide documentation for feature. Note that it should be compilable which is checked on `./gradlew build` (See description below)
4. Provide sample for feature in [samples](sample) directory

#### Help with issues

If you want to contribute but don't know what to do good start will be help with currently [active issues](https://github.com/saksmt/ktest/issues?utf8=%E2%9C%93&q=is%3Aissue+is%3Aopen+label%3A%22help+wanted%22+no%3Aassignee)
Please before doing any work write a comment "I'm working on it" that will allow to assign an issue to you.


## Building

All you need to do is to type

```bash
./gradlew build
``` 

on POSIX compatible system (linux, mac, bsd, ...) and

```commandline
gradlew build
```

on windows

## Documentation

All kotlin examples in [documentation](doc) are compiled at build time so that there is minimal chance of making mistake.

### Special annotation on documentation

Since all documentation is built and all code block are built in same package there are some useful annotation which you can use to fix build in case of errors:

 - `[//] # (no_check)` - completely disables check of following block, you should use this only for blocks that are not meant to be compilable at all!
 - `[//] # (package:com.mypackage)` - puts that block in separate package named `com.mypackage`, don't forget that you still need to write `package com.mypackage` in your code block
