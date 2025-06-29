# CI/CD Templates

This directory contains templates for various CI/CD platforms to help you quickly set up continuous integration and deployment pipelines for your Spring Boot applications.

## Contents

### GitHub Actions
- java-build.yml: Template for building and testing Java applications
- docker-build.yml: Template for building and pushing Docker images
- kubernetes-deploy.yml: Template for deploying to Kubernetes

### Jenkins
- Jenkinsfile-java: Pipeline for building and testing Java applications
- Jenkinsfile-docker: Pipeline for building and pushing Docker images
- Jenkinsfile-deploy: Pipeline for deploying to Kubernetes

### GitLab CI
- .gitlab-ci.yml: Complete CI/CD pipeline for GitLab

### Azure DevOps
- zure-pipelines.yml: Multi-stage pipeline for Azure DevOps

## Usage

Copy the appropriate template to your project and modify as needed for your specific requirements.

For GitHub Actions, place the files in the .github/workflows/ directory of your repository.

For Jenkins, place the Jenkinsfile in the root of your repository and configure your Jenkins server to use it.

For GitLab CI, place the .gitlab-ci.yml file in the root of your repository.

For Azure DevOps, import the pipeline YAML file when creating a new pipeline.
