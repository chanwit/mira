#!/usr/bin/env bash

find . -name "Mirafile" | xargs -I {} mira -f {} provision clean
