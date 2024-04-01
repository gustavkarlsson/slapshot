To check samples, do the following:
```shell
./gradlew check publishToMavenLocal
./gradlew sample-junit4:check sample-junit5:check --include-build sample-junit4 --include-build sample-junit5
```
