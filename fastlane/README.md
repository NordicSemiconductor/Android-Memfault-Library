fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android deployNexus

```sh
[bundle exec] fastlane android deployNexus
```

Deploy libraries to Nexus.

### android deploy

```sh
[bundle exec] fastlane android deploy
```

Deploy a new version to the Google Play

### android deployAlpha

```sh
[bundle exec] fastlane android deployAlpha
```

Deploy build to Alpha channel.

### android deployBeta

```sh
[bundle exec] fastlane android deployBeta
```

Deploy build to Beta channel.

### android deployInternal

```sh
[bundle exec] fastlane android deployInternal
```

Deploy build to internal channel.

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
