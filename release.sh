#!/bin/sh

set -e

if [ -n "$1" ]; then
  VERSION=$1

  # generate version file
  echo "git.baseVersion := \"${VERSION}\"" > version.sbt

  # git commit and tag
  git add version.sbt
  git commit -m "Release v${VERSION}"
  git tag v${VERSION}

  # build and publish it
  sbt +clean +test +publish

  git push origin master
  git push origin v${VERSION}

else
  echo "Usage: release.sh [version]"
  exit 1
fi
