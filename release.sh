#!/usr/bin/env bash

# Work out the appropriate service name from the repo directory
PROJECT_NAME=$(basename `pwd`)

# Work out the local branch for version tagging
if BRANCH=$(git symbolic-ref --short -q HEAD)
then
  echo -e "Releasing \033[32m${PROJECT_NAME}-${BRANCH}\033[0m"
else
  echo -e "\033[31mFailed\033[0m - not on any branch.\n"
  exit -1
fi

# Check local branch for staged files
if git diff-index --quiet HEAD --; then
  echo -e "\033[32mOK\033[0m - No staged files awaiting commit."
else
  echo -e "\033[31mFailed\033[0m - Your issue branch contains files that are staged and not committed.\n"
  exit -1
fi

# Check local branch relative to remote
UPSTREAM=${1:-'@{u}'}
LOCAL=$(git rev-parse "@{0}")
REMOTE=$(git rev-parse "$UPSTREAM")
BASE=$(git merge-base "@{0}" "$UPSTREAM")

if [ ${LOCAL} = ${REMOTE} ]; then
    echo -e "\033[32mOK\033[0m - Your branch can be released."
elif [ ${LOCAL} = ${BASE} ]; then
    echo -e "\033[31mFailed\033[0m - You need to pull from upstream.\n"
    exit -1
elif [ ${REMOTE} = ${BASE} ]; then
    echo -e "\033[31mFailed\033[0m - You need to push to upstream.\n"
    exit -1
else
    echo -e "\033[31mFailed\033[0m - Your local branch has diverged from the remote.\n"
    exit -1
fi

BEHIND_MASTER=$(git rev-list --left-right --count master...@)
BEHIND_DEVELOP=$(git rev-list --left-right --count develop...@)

# Check issue branch rules
if [[ "$BRANCH" =~ ^issue.* ]]; then
    echo -e "\nApplying release rules for 'issue' to 'develop'."

    if [[ "$BEHIND_DEVELOP" =~ ^0 ]]; then
      echo -e "\033[32mOK\033[0m - This branch is ahead of 'develop'."
    else
      echo -e "\033[31mFailed\033[0m - Your issue branch is behind develop. You should rebase and resolve conflicts.\n"
      exit -1
    fi

    if grep -s -q ":issue-" "./pom.xml"; then
      echo -e "\033[31mFailed\033[0m - The pom.xml contains ':issue-' implying an issue dependency which is not allowed in the develop branch.\n"
      exit -1
    else
      echo -e "\033[32mOK\033[0m - No 'issue' dependencies in './pom.xml'"
    fi

    # Force user to be sure about releasing
    read -p "Please enter the project name to confirm the release ($PROJECT_NAME): " userInput
    if [[ "$userInput" == "$PROJECT_NAME" ]]; then
       echo -e "\033[32mOK\033[0m - Releasing to '$userInput'"
       git checkout develop
       git merge ${BRANCH}
       git push
    else
       echo -e "\033[31mFailed\033[0m - Project name did not match. Aborting with no changes.\n"
       exit -1
    fi
fi

# Check develop branch rules
if [[ "$BRANCH" =~ ^develop ]]; then
    echo -e "\nApplying release rules for 'develop' to a release branch."

    if [[ "$BEHIND_MASTER" =~ ^0 ]]; then
      echo -e "\033[32mOK\033[0m - This branch is ahead of 'master'."
    else
      echo -e "\033[31mFailed\033[0m - Your release branch is behind 'master'. You should rebase and resolve conflicts.\n"
      exit -1
    fi

    if grep -s ":.*\-SNAPSHOT" "./pom.xml"; then
      echo -e "\033[31mFailed\033[0m - The pom.xml contains '-SNAPSHOT' implying a SNAPSHOT dependency which is not allowed in a release branch.\n"
      exit -1
    else
      echo -e "\033[32mOK\033[0m - No SNAPSHOT dependencies in './pom.xml'"
    fi

    echo -e "Most recent tagged version:\n$(git describe --abbrev=0 --tags)"

    # Force user to be sure about releasing
    read -p "Please enter the agreed version number (e.g. 1.2.3) to confirm the release: " userInput
    echo -e "\033[32mOK\033[0m - Releasing to '$userInput'"
    git branch ${userInput}
    git checkout ${userInput}
    git merge develop
    git push --set-upstream origin ${userInput}

fi

# Check release branch rules (0.0.0 format)
if [[ "$BRANCH" =~ ^[0-9] ]]; then
    echo -e "\nApplying release rules for release branch to master and develop."

    if [[ "$BEHIND_MASTER" =~ ^0 ]]; then
      echo -e "\033[32mOK\033[0m - This branch is ahead of 'master'."
    else
      echo -e "\033[31mFailed\033[0m - Your release branch is behind master. You should rebase and resolve conflicts.\n"
      exit -1
    fi

    if grep -s ":.*\-SNAPSHOT" "./pom.xml"; then
      echo -e "\033[31mFailed\033[0m - The pom.xml contains '-SNAPSHOT' implying a SNAPSHOT dependency which is not allowed in a release branch.\n"
      exit -1
    else
      echo -e "\033[32mOK\033[0m - No SNAPSHOT dependencies in './build.gradle'"
    fi

    # Force user to be sure about releasing
    read -p "Please enter the project name to confirm the release ($PROJECT_NAME): " userInput
    if [[ "$userInput" == "$PROJECT_NAME" ]]; then
      echo -e "\033[32mOK\033[0m - Releasing to '$userInput' on 'master'"
      git checkout master
      git merge ${BRANCH}
      # Use prefix to avoid branch naming problems when rebuilding release branches
      git tag -a version-${BRANCH} -m "version-${BRANCH}"
      git push --follow-tags
    fi

    # Check if merge to develop to retrofit fixes can be applied safely
    if [[ "$BEHIND_DEVELOP" =~ ^0 ]]; then
      echo -e "\033[32mOK\033[0m - This branch is ahead of 'develop'."
      echo -e "\033[32mOK\033[0m - Merging ${BRANCH} into 'develop'"
      git checkout develop
      git merge ${BRANCH}
      git push
    else
      echo -e "\033[33mWarning\033[0m - Your release branch is behind 'develop'. You should resolve conflicts.\n"
      git checkout develop
      git merge ${BRANCH}
      git push
    fi

fi

echo -e "Done."