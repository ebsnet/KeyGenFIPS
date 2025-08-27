# EBSnet KeyGenFIPS

[![Build Status](https://github.com/ebsnet/KeyGenFIPS/actions/workflows/build.yml/badge.svg)](https://github.com/ebsnet/KeyGenFIPS/actions/workflows/build.yml)

Tool to generate FIPS compatible BrainpoolP256r1 keys.

This tool is developed and published by [EBSnet | eEnergy Software
GmbH](https://www.ebsnet.de).

## Usage

Compiled artifacts of this tool can be found on the [releases
page](https://github.com/ebsnet/KeyGenFIPS/releases/latest). Just download the
archive to your liking, extract and execute:

```
./bin/KeyGenFIPS --out enc.key
./bin/KeyGenFIPS --out sig.key
./bin/KeyGenFIPS --out tls.key
```

Running `KeyGenFIPS` requires at least Java 17.

## Compiling

Compiling from source requires at least Java JDK 17.

`KeyGenFIPS` uses the [Gradle build tool](https://gradle.org/) so you can
compile the tool by invoking `./gradlew build`.

## License

This project is licensed under the GNU AGPLv3 license. The license text can be
found [here](./LICENSE).
