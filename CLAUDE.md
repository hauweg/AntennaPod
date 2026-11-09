@AGENTS.md

# Fork workflow (hauweg/AntennaPod)
This is a personal fork. Never open a pull request against the upstream AntennaPod repository.

Branch model:
- `fork-main` is the main branch with the finished state and the default branch on GitHub.
- Every new feature gets its own branch `feature/<name>`, created from the latest `fork-main`
  (`git fetch origin fork-main` and `git checkout -B feature/<name> origin/fork-main`).
- Pushing to the feature branch, and to `fork-main` only when merging, is allowed instead of the automatically assigned session branch.
- Only one session works at a time. Before starting, always branch from the latest `fork-main`.

Whenever a feature is finished, close it cleanly and remind the user of every step, in German, explained for a GitHub beginner
(show every git command and explain it in one sentence):
1. Push all commits to the feature branch and make sure the GitHub Actions build `Fork Debug APK` is green. Give the link to the build and its APK artifact. Then wait until the user has tested the APK and confirmed that it works.
2. Merge into `fork-main`: `git checkout fork-main`, `git pull origin fork-main`, `git merge --no-ff feature/<name>`, `git push origin fork-main`. Make sure the build on `fork-main` is green.
3. Update the English notice at the top of `README.md` on `fork-main`: add the new changes to "Changes in this fork" with the current date, keep the branch reference at `fork-main` and update "Known limitations". Do not change the rest of the README. Commit and push.
4. Delete the feature branch: `git push origin --delete feature/<name>` and `git branch -d feature/<name>`.
5. Remind the user not to click "Compare & pull request" on GitHub, because that would target the upstream repository.
6. If open points remain, offer a ready-to-copy prompt for the next session that includes the branch setup and the current state.
