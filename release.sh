#!/bin/bash

git fetch --tags
mvn releaser:release
git push --tags