# Release process

## Process for development

1. Branch of from **develop**
    1. Prefer prefix your branch with feature/ or bugfix/ depending on content
2. Develop a code on your branch
    1. Do not change version in following files:
        1. setup.py
        2. python_template/__init__.py
        3. docs/source/conf.py
    2. If you add new module/subpackage/method please add header for documentation purposes. Workflow automatically generates API documentation.
    3. Try to create small changes
    4. Create as many commits as you want

1. Create a pull request
    1. Development branch is **develop, **all regular pull requests should be merged to develop.
    2. Get an approval
    3. Make sure develop workflow passes
    4. **Squash and merge** your pull request and add meaningful message to your commit

## Process for patch/minor/major release

**[Manual] **indicates manual action

1. **[Manual] **Create a pull request from **develop** to **master.**
2. **[Manual] **Merge pull request to master with appropriate message (see below)
    1. Do not use rebase as workflow needs to know what kind of release it is (major/minor/patch)
    2. Do not use squash because it will create a conflict between master and develop, and lose all contribution info
    3. In message add one the hashtags
        1. #minor → Minor release
        2. #major → Major release
        3. #patch → Patch release
        4. missing hashtag → Minor release
3. Workflow will create a new commit on master branch and tag it with new version
4. Workflow will update documentation and push new docker image with latest and new version tags
5. Workflow will create a draft of a release in github
6. **[Manual] **Edit release draft if needed and publish

## Assumptions

* Current release is 0.0.0 (no release)
* During development library has **dev** version.



