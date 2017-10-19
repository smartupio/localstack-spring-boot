#!/bin/bash
set -e

if [ $# -ne 1 ]
then
	echo "You must provide a version for the next release"
fi

if [[ ! $1 =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] 
then
	echo "You should provide a version number in the form of x.y.z, I will take care of the rest"
	exit 1
fi

CURRENT_BRANCH=$(git branch | grep \* | cut -d ' ' -f2)
if [ ! $CURRENT_BRANCH = "develop" ]
then
	echo "Checking out develop..."
	git checkout develop >> .debug.log 2>&1
fi
git pull >> .debug.log 2>&1

NEXT_VERSION=$1
CURRENT_VERSION_SNAPSHOT=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version|grep -Ev '(^\[|Download\w+:)')

if [[ ! $CURRENT_VERSION_SNAPSHOT =~ ^[0-9]+\.[0-9]+\.[0-9]+-SNAPSHOT$ ]]
then
	echo "You must provide a repo with SNAPSHOT version"
	exit 1
fi

CURRENT_VERSION_RELEASE=$(echo $CURRENT_VERSION_SNAPSHOT | cut -d"-" -f1)

if [ $CURRENT_VERSION_RELEASE = $(echo -e "$NEXT_VERSION\n$CURRENT_VERSION_RELEASE" | sort -V | tail -1) ]
then
	echo "You must provide a version number bigger than $CURRENT_VERSION_RELEASE"
	exit 1
fi

echo "Creating the RELEASE version with version $CURRENT_VERSION_RELEASE"
echo "Modifying version from $CURRENT_VERSION_SNAPSHOT to $CURRENT_VERSION_RELEASE"
find . -name "pom.xml" -exec mvn versions:set -DnewVersion=$CURRENT_VERSION_RELEASE  -f {} >> .debug.log 2>&1 \;
echo "Adding modified pom.xml's to git"
find . -name "pom.xml" -exec git add -f {} >> .debug.log 2>&1 \;
echo "Creating commit with changes"
git commit -m "Release version $CURRENT_VERSION_RELEASE" >> .debug.log 2>&1

RELEASE_COMMIT_ID=$(git rev-parse HEAD)
echo "Release commit created with commitId $RELEASE_COMMIT_ID"

echo "Creating the SNAPSHOT version with version $NEXT_VERSION-SNAPSHOT"
echo "Modifying version to $NEXT_VERSION-SNAPSHOT"
find . -name "pom.xml" -exec mvn versions:set -DnewVersion="$NEXT_VERSION-SNAPSHOT"  -f {} >> .debug.log 2>&1 \;
echo "Adding modified pom.xml's to git"
find . -name "pom.xml" -exec git add -f {} >> .debug.log 2>&1 \;
echo "Creating commit with changes"
git commit -m "Snapshot version $NEXT_VERSION-SNAPSHOT" >> .debug.log 2>&1

echo "Pushing changes to develop"
git push >> .debug.log 2>&1

echo "Checking out release commit"
git checkout $RELEASE_COMMIT_ID >> .debug.log 2>&1

echo "Checkout out branch release/$CURRENT_VERSION_RELEASE"
git checkout -b release/$CURRENT_VERSION_RELEASE >> .debug.log 2>&1

echo "Pushing branch to origin"
git push --set-upstream origin release/$CURRENT_VERSION_RELEASE >> .debug.log 2>&1

# TODO make PR on github