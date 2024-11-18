package main

import (
	"context"
	"errors"
	"fmt"
	"net/url"
	"os"
	"strings"

	"github.com/go-git/go-git/v5"
	"github.com/go-git/go-git/v5/config"
	"github.com/go-git/go-git/v5/plumbing"
	"github.com/go-git/go-git/v5/plumbing/transport/http"
	"github.com/go-git/go-git/v5/storage/memory"
	"github.com/google/go-github/v48/github"
	"golang.org/x/oauth2"
)

func main() {

	src_repo := SetEnv("PUBLIC_SRC_REPO")
	src_branch := SetEnv("PUBLIC_SRC_BRANCH")
	dest_repo := SetEnv("DEST_REPO")
	dest_stg_branch := SetEnv("DEST_STAGING_BRANCH")
	dest_branch := SetEnv("DEST_BRANCH")
	token := SetEnv("GITHUB_TOKEN")

	ClonePublicRepo(src_repo, src_branch, dest_repo, dest_stg_branch, token)
	MergeCode(token, dest_repo, dest_branch, dest_stg_branch)

}
func ClonePublicRepo(src_repo string, src_branch string, dest_repo string, dest_stg_branch string, token string) {

	Info("Cloning the source repo: %v, branch: %v", src_repo, src_branch)
	r, err := git.Clone(memory.NewStorage(), nil, &git.CloneOptions{
		URL:           src_repo,
		SingleBranch:  true,
		ReferenceName: plumbing.NewBranchReferenceName(src_branch),
	})
	CheckIfError(err)

	Info("Adding destination repo: %v", dest_repo)
	_, err = r.CreateRemote(&config.RemoteConfig{
		Name: "destination",
		URLs: []string{dest_repo},
	})
	CheckIfError(err)

	Info("Pushing from source branch %v to destination branch %v", src_branch, dest_stg_branch)
	rs := config.RefSpec("refs/heads/" + src_branch + ":refs/heads/" + dest_stg_branch)
	err = r.Push(&git.PushOptions{
		RemoteName: "destination",
		Force:      true,
		Auth: &http.BasicAuth{
			Username: "Github Action BOT",
			Password: token,
		},
		RefSpecs: []config.RefSpec{rs},
	})
	Warning("%s", err)
	Info("Repo cloning completed")
}
func MergeCode(token string, dest_repo string, dest_branch string, dest_stg_branch string) {

	parseRepoUrl, _ := url.Parse(dest_repo)
	gitHost := "https://" + parseRepoUrl.Host
	path := strings.Split(parseRepoUrl.Path, "/")
	org := path[1]
	repoName := strings.ReplaceAll(path[2], ".git", "")

	Info("Creating Github OATHU Client")
	ctx := context.Background()
	ts := oauth2.StaticTokenSource(
		&oauth2.Token{AccessToken: token},
	)
	tc := oauth2.NewClient(ctx, ts)

	Info("Creating Github Client")
	client, err := github.NewEnterpriseClient(gitHost, gitHost, tc)
	CheckIfError(err)

	Info("Creating Pull Request")

	newPR := &github.NewPullRequest{
		Title:               github.String("Github Sync Repo"),
		Head:                github.String(dest_stg_branch),
		Base:                github.String(dest_branch),
		Body:                github.String("This PR is created when the sync action is triggered"),
		MaintainerCanModify: github.Bool(true),
	}
	pr, _, err := client.PullRequests.Create(ctx, org, repoName, newPR)
	CheckIfError(err)

	Info("PR created: %s \n", pr.GetHTMLURL())

	Info("Merging the PR")

	options := &github.PullRequestOptions{MergeMethod: "merge"}
	merge, _, err := client.PullRequests.Merge(ctx, org, repoName, int(*pr.Number), "merging pull request", options)
	CheckIfError(err)
	if *merge.Merged {
		Info("PR Merged Successfully")
	} else {
		CheckIfError(errors.New("PR Merge Failed"))
	}

}
func SetEnv(key string) string {
	value, ok := os.LookupEnv(key)
	if !ok {
		CheckIfError(errors.New("Missing environment value : " + key))
	}
	return value
}
func CheckIfError(err error) {
	if err == nil {
		return
	}

	fmt.Printf("\x1b[31;1m%s\x1b[0m\n", fmt.Sprintf("error: %s", err))
	os.Exit(1)
}
func Info(format string, args ...interface{}) {
	fmt.Printf("\x1b[34;1m%s\x1b[0m\n", fmt.Sprintf(format, args...))
}
func Warning(format string, args ...interface{}) {
	fmt.Printf("\x1b[36;1m%s\x1b[0m\n", fmt.Sprintf(format, args...))
}
