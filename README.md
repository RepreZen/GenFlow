# GenFlow
[![Build Status](https://travis-ci.org/RepreZen/GenFlow.svg?branch=master)](https://travis-ci.org/RepreZen/GenFlow)
![](https://img.shields.io/maven-central/v/com.reprezen.genflow/genflow-common.svg)
Code Generation framework used in RepreZen API Studio

Coming Soon: The code generation framework that has always been embedded in API Studio will be re-christened *GenFlow* and broken out into this project. We intend to make open-source sometime in the coming months.

First step will be repackaging existing GenTemplates, as well as the underlying framework code, to this new project, and publish everything on Maven Central. We'll migrate GenTemplates piecemeal (but quickly) once basics are in place.

For some of our users, this will remove a stumbling block, as it will eliminate the need to access RepreZen's own maven repository.

It will also allow us to decouple GenFlow release from API Studio product releases, allowing us to respond to our users' needs with greater speed and agility.
