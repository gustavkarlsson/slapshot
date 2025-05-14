# Slapshot - A snapshot testing library for Kotlin

## About snapshot testing

Snapshot testing is a type of software testing where the output of a piece of code (such as a function, UI component, or
API response) is captured and stored as a "snapshot." This snapshot serves as a reference for future test runs. During
subsequent tests, the new output is compared to the stored snapshot to ensure that the behavior of the code has not
changed unexpectedly.

## How Slapshot works

Slapshot is a Kotlin library with an accompanying Gradle Plugin that integrates with test frameworks
such as JUnit 4 and 5.

With an extensible architecture, it enables you to snapshot test almost any data imaginable.
If it can be serialized, you can snapshot test with it!

Some examples of built-in type support:

* Primitives such as numbers and strings
* JSON
* Image bitmaps with variable tolerance levels (good for screenshots!)

But you can also implement your own serializer, such as for web server requests/responses, or mp3 files.

## Getting Started

### Add the plugin

In your `build.gradle.kts` file:

```kotlin
plugins {
    id("se.gustavkarlsson.slapshot") version "<latest_version>"
}

// Optional config block
slapshot {
    testFramework.set(TestFramework.JUnit5) // Default
}
```

### Set up a test class

**JUnit 5**

```kotlin
 @ExtendWith(SnapshotExtension::class)
class MyTests {
    private lateinit var snapshotter: Snapshotter<String>

    @BeforeEach
    fun initSnapshotContext(snapshotContext: JUnit5SnapshotContext) {
        val serializer = StringSerializer()
        snapshotter = snapshotContext.createSnapshotter(serializer)
    }
}
```

**JUnit 4**

```kotlin
class MyTests {
    @get:Rule
    val snapshotContext = JUnit4SnapshotContext()
    val snapshotter = let {
        val serializer = StringSerializer()
        snapshotContext.createSnapshotter(serializer)
    }
}
```

### Write a test

Here is an example string concatenation test:

```kotlin
class MyTests {
    @Test
    fun `test string`() {
        val result = "foo" + "bar"
        snapshotter.snapshot(result)
    }
}
```

### Run the test

The first time a new test runs, there will be no snapshot to compare against. The test will fail and save a snapshot
file with the results for future reference.

The next time it runs, it will compare the result to the saved snapshot and succeed!

### Commit the snapshots to source control

By committing the snapshot files, we are preventing regressions that would cause the results to change.

## Snapshotting different types of data

Slapshot is not limited to strings! Different data types are supported through different implementations of the
`Serializer` interface.

Slapshot comes with many serializers out of the box. Check them out or implement your own!

## Testing complex data

By default, Slapshot compares snapshots by equality, but sometimes a more sophisticated comparison might be necessary.
For example:

* Some types may not have a working `equals()` function.
* Some types don't have a useful `toString()` for error messages.
* Some types, such as floating point types, might need to be compared with some level of tolerance.

Slapshot uses a `Tester` interface to handle comparisons and produce error messages.
Some are included, but you can also implement your own too.

## Configuration

Slapshot can be configured in multiple different ways.

### build.gradle.kts

The plugin can be configured in an optional extension block:

```kotlin
slapshot {
    testFramework.set(TestFramework.JUnit4) // Only necessary for JUnit4
    snapshotRootDir.set("my/awesome/snapshots") // Relative to project root
    defaultAction.set(SnapshotAction.CompareOnly) // Change how snapshots are handled
}
```

### Gradle properties

Some settings can be overridden with
[Gradle project properties](https://docs.gradle.org/current/userguide/build_environment.html#sec:project_properties).
This can be useful for CI environments.

```shell
./gradlew test -PsnapshotRootDir="my/snapshots" -PdefaultAction=compareOnly
```

### In runtime

When you create a `Snapshotter` from a `SnapshotContext`, you can override some configuration on a one-off basis.

```kotlin
val snapshotter = snapshotContext.createSnapshotter(
    serializer = LongSerializer,
    overrideRootDirectory = Path("some/other/root"),
    overrideSnapshotFileResolver = { rootDirectory, testInfo, fileExtension ->
        val directory = rootDirectory.resolve(testInfo.testClass.get().name)
        val fileName = "${testInfo.displayName}_snapshot.$fileExtension"
        directory.resolve(fileName)
    },
    overrideAction = SnapshotAction.Overwrite,
)
```

## Deleting all snapshots

If you want to start over, you can nuke all snapshots by running:

```shell
./gradlew clearSnapshots
```

This can be useful if you have changed how or where snapshots are stored and want a "fresh start."

## Sample projects

Check out the sample projects to see how to integrate Slapshot in different types of projects.

To run them, you have to first publish a local version of the plugin.

```shell
./gradlew check publishToMavenLocal
./gradlew sample-junit4:check sample-junit5:check --include-build sample-junit4 --include-build sample-junit5
```
