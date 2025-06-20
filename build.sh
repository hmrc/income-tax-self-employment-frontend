#!/bin/bash

sbt clean scalafmtAll scalafmtSbt compile coverage test it/test coverageReport
