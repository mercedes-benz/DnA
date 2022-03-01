# Githooks

### Git Hooks for branch naming convention

`.githooks/pre-commit` file helps to restrict the branch naming convention and follows the custom naming convention.

### How its works

Every time made a commit on the branch, `.githooks/pre-commit` script will execute on local and validate the branch name. If the branch name adheres to the policy, then it allows doing commit otherwise it will block.

### Setup

Prerequisites:

Git version 2.34 +

Run the below command to makes `.git` to run the `.githooks/pre-commit` script for every commit in the particular repo.

```
git config core.hooksPath .githooks
```

Note: Above, the command needs to run every *first-time clone of the repo*. Not required to run for commit and other git operations.

Checkout the [Branch naming convention](../docs/GitStrategy.md) to define the branch name.
