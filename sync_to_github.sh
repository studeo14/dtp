#! /usr/bin/env bash

GITHUB_ORIGIN=github

# function to check if the github remote is present
check_github_remote () {
    # get the current list of remotes
    local remotes=$(git remote)
    # iterate and check
    for remote in $remotes
    do
        if [ $remote == $GITHUB_ORIGIN ]
        then
            return 1
        fi
    done
    return 0
}

add_github_remote () {
    echo git remote add $GITHUB_ORIGIN git@github.com:grc4delv/datasheet_processor.git
    git remote add $GITHUB_ORIGIN git@github.com:grc4delv/datasheet_processor.git
}

remove_github_remote () {
    echo git remote remove $GITHUB_ORIGIN
    git remote remove $GITHUB_ORIGIN
}

push_to_github () {
    # get the current branch name
    local branch=$(git branch | grep \* | cut -d ' ' -f2)
    echo git push $GITHUB_ORIGIN $branch
    git push $GITHUB_ORIGIN $branch
}

main () {
    # perform check
    check_github_remote
    if [ $? -eq 0 ]
    then
        echo Adding github remote
        add_github_remote
    fi
    echo Pushing to github
    # pushing to github
    push_to_github
}

# execute main
main
