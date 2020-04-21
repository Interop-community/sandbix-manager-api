#!/usr/bin/env bash

echo $(aws ecs register-task-definition --region us-east-1 --family ${PROJECT_FULL_NAME} --cli-input-json file://${TEMPLATE_FILE})
