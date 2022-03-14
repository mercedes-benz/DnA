# Git Branching Strategy

`main` is the default branch and has the latest stable production-ready code. Committing is only possible by PR from the `dev` branch.

### Checkout a new branch from a  `dev `for your contribution

* Contributors are requested to checkout a new branch from the `dev` and start contributing. Based on the type of work the contributor would like to do(patch/feature etc.), we expect the contributor to create a branch using the naming convention mentioned in the below table..
* Upon the completion of the feature/patch, please create **Pull Request** to the `dev `branch from your `feature/patch` branch.
* Our technical team would review the PR and merge it to `dev` branch.
* Once the changes are on the `dev` branch, we will run different test to ensure quality, regression, security, and compliance of this new code.
* Iranckf all tests pass, we will merge to the `main` branch by reaise a new release **PR** from the `dev` branch.

  ![](images/gitStrategy.png)
* Note: All new branches should checkout from `dev` branch, except for any patches. 

* We expect users to please follow the below branch naming conventions for any new branches they create. **Else such branches would be deleted without further intimation**.

### Branch naming convention

| Acceptable Branch     | Description                                                                                                                                                                                                               |
| --------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| main                  | `main` is the default branch, committing is only possible by PR from `dev `branch.                                                                                                                                    |
| dev                   | `dev` branch where our development code will be there, committing is only possible by PR from `your_branch `. Note: It has an unstable version of the code.                                                           |
| feature/<branch_name> | Example:`feature/imageUpload` <br />If you are working on new features that are under the development stage then, create the branch name with the prefix `feature/`. `<branch_name>` replace with the feature name. |
| bugfix/<branch_name>  | Example:`bugfix/codeFix` <br />If you are working bug then, create the branch name with the prefix `bugfix/`.`<branch_name>` replace with the bug name.                                                             |
| patch/<branch_name>   | Example:`patch/urlIncorrect` <br />If you are working production bug then, create the branch name with the prefix `patch/`.`<branch_name>` replace with the bug name.                                               |
| docs/<branch_name>    | Example:`docs/installDoc` <br />If you are working documentation then, create the branch name with the prefix `docs/`.`<branch_name>` replace with the bug name.                                                    |
