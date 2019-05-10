#!/bin/bash

set -e

liftVersions=("3.2.0" "3.3.0")

if [ -n "$1" ]; then
  version=$1

  # build and test for each version of Lift
  for v in "${liftVersions[@]}"; do
    sbt "set LiftModule.liftVersion := \"${v}\"" +clean +test
  done

  # generate version file
  echo "git.baseVersion := \"${version}\"" > version.sbt

  # commit and tag
  git add version.sbt
  git commit -m "Release v${version}"
  git tag v${version}

  # publish it for each version of Lift
  for v in "${liftVersions[@]}"; do
    sbt "set LiftModule.liftVersion := \"${v}\"" +publish
  done

  # push
  git push origin master
  git push origin v${version}

else
  echo "Usage: release.sh [version]"
  exit 1
fi
