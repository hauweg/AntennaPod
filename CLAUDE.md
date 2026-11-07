@AGENTS.md

# Fork workflow (hauweg/AntennaPod)
This is a personal fork. Never open a pull request against the upstream AntennaPod repository.
Whenever a feature is finished, close it cleanly and remind the user of every step, in German, explained for a GitHub beginner:
1. Push all commits to the working branch and make sure the GitHub Actions build `Fork Debug APK` is green. Give the link to the build and its APK artifact.
2. Update the English notice at the top of `README.md`: add the new changes to "Changes in this fork" with the current date and update "Known limitations". Do not change the rest of the README.
3. Tell the user the name of the branch that now holds the newest state.
4. Remind the user to switch the fork's default branch to this branch (Settings → General → Default branch), so visitors see the current README.
5. Remind the user that other or older sessions must first fetch and merge this branch before continuing, and that two sessions should never work in parallel.
6. Remind the user not to click "Compare & pull request" on GitHub, because that would target the upstream repository.
7. If open points remain, offer a ready-to-copy prompt for the next session that includes the branch setup and the current state.
