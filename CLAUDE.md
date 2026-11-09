@AGENTS.md

# Fork workflow (hauweg/AntennaPod)
This is a personal fork. Never open a pull request against the upstream AntennaPod repository.

Branch model:
- `fork-main` is the main branch with the finished state and the default branch on GitHub.
- Every new feature gets its own branch `feature/<name>`, created from the latest `fork-main`
  (`git fetch origin fork-main` and `git checkout -B feature/<name> origin/fork-main`).
- Pushing to the feature branch, and to `fork-main` only when merging, is allowed instead of the automatically assigned session branch.
- Only one session works at a time. Before starting, always branch from the latest `fork-main`.

Working with the user:
- The user is a GitHub beginner. Communicate in German, use gender-neutral language with a colon (e.g. "Nutzer:innen"),
  outline the steps before executing them and show every git command with a one-sentence explanation.
- Never create a pull request.
- The local machine has no Android SDK. The compile check is the GitHub Actions build `Fork Debug APK`.

Starting a new feature (when the user only describes a feature and names a branch `feature/<name>`):
1. `git fetch origin fork-main`
2. `git checkout -B feature/<name> origin/fork-main`
3. Check the "Changes in this fork" list in `README.md` to understand the current state.
4. First push with `git push -u origin feature/<name>`.
If the user names no branch, suggest a short `feature/<name>` and use it.
If deleting a branch on GitHub is refused, tell the user how to delete it in the GitHub web interface.

Whenever a feature is finished, close it cleanly and remind the user of every step, in German, explained for a GitHub beginner
(show every git command and explain it in one sentence):
1. Push all commits to the feature branch and make sure the GitHub Actions build `Fork Debug APK` is green. Give the link to the build and its APK artifact. Then wait until the user has tested the APK and confirmed that it works.
2. Merge into `fork-main`: `git checkout fork-main`, `git pull origin fork-main`, `git merge --no-ff feature/<name>`, `git push origin fork-main`. Make sure the build on `fork-main` is green.
3. Update the English notice at the top of `README.md` on `fork-main`: add the new changes to "Changes in this fork" with the current date, keep the branch reference at `fork-main` and update "Known limitations". Do not change the rest of the README. Commit and push.
4. Delete the feature branch: `git push origin --delete feature/<name>` and `git branch -d feature/<name>`.
5. Remind the user not to click "Compare & pull request" on GitHub, because that would target the upstream repository.
6. If open points remain, offer a ready-to-copy prompt for the next session that includes the branch setup and the current state.

# Fork rules (hauweg/AntennaPod)
- One feature per session. Never work on several branches in parallel.
- Before starting, check for unmerged feature branches and ask the user to finish and merge them into `fork-main` first.
- For design questions, first present the options with their pros and cons and let the user decide. This takes precedence over AGENTS.md ('Do not ask for permission before making initial code changes').
- When a feature is finished, state the name of its feature branch.
