#!/bin/bash -e

unset MILESTONE SUITE
HELP=false
WORKING_TREE="."
TESTCASE_ROOT="com.modelsolv.reprezen.resources/models/autoRealization/Tests"
TESTCODE_ROOT="com.modelsolv.reprezen.restapi.test/src/com/modelsolv/reprezen/restapi/test/realization/processor"

function getargs() {
    longopts="milestone:,suite:,testCasesRoot:,testCodeRoot:,workingTree:"
    shortopts="m:s:h"
    opts=$(getopt -n "$progname" -o "$shortopts" --long "$longopts" -- "$@")
    if [[ $? -ne 0 ]] ; then
	return 1
    fi
    eval set -- "$opts"
    while true; do
	case "$1" in
	    -m|--milestone)
		MILESTONE="$2"; shift 2 ;;
	    -s|--suite)
		SUITE="$2"; shift 2 ;;
	    --testCaseRoot)
		TESTCASE_ROOT="$1"; shift 2 ;;
	    --testCodeRoot)
		TESTCODE_ROOT="$1"; shift 2 ;;
	    --workingTree)
		WORKING_TREE="$1"; shift 2 ;;
	    -h)
		HELP=true; shift ;;
	    --)
		shift
		if [[ ${#@} != 0 ]]; then
		    echo "Unexpected arguments: $@"; return 1
		fi
		break ;;
	    *)
		echo "Unrecognized option: '$1'"; return 1 ;;
	esac
    done
    if $HELP ; then return 1; fi
}

function setFiles() {
    local testCaseDir="$1"
    local testCodeDir="$2"
    GFM_FILE="$testCaseDir/$MILESTONE/$SUITE/$SUITE.md"
    local ucMilestone=$(echo "$MILESTONE" | tr 'A-Z' 'a-z')
    CODE_FILE="$testCodeDir/$ucMilestone/${SUITE}Tests.xtend"

}

function getTestNames() {
    TEST_NAMES=($(sed -e 's/\r$//' -ne '/^\s*def\s/s/^\s*def\s\+test\(.*\)\s*(.*)\s*[{]/\1/p' "$CODE_FILE"))
}

function injectTest() {
    local test="$1"
    sed -ne "/\s*def\s*test$test\s*(.*)\s*[{]/,/\s*[}]/{//!p}" "$CODE_FILE" \
	| expand -i -t 4 \
	| awk '{line = $0; if (NR == 1){match(line, /\s*/, indent);} if (substr(line, 1, length(indent[0])) == indent[0]) {line = substr(line, length(indent[0])+1);} print line;}' \
	| sed -i -e "/^\`\`\`\s*$test/,/^\`\`\`/{//!d}" \
	      -e "/^\`\`\`\s*$test/r /dev/stdin" \
	      $GFM_FILE
}

function main() {
    local testCaseDir="$WORKING_TREE/$TESTCASE_ROOT"
    local testCodeDir="$WORKING_TREE/$TESTCODE_ROOT"
    setFiles "$testCaseDir" "$testCodeDir"
    getTestNames "$testCodeDir"
    for name in ${TEST_NAMES[@]} ; do
	injectTest "$name"
    done
}

if ! getargs "$@" ; then
    usage
    exit 1
fi

main
