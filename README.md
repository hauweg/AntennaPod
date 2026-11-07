> [!WARNING]
> **This is a personal, unofficial fork of AntennaPod.**
> The changes below were "vibe coded" with an AI assistant (Claude Code) for my own use and testing.
> They are not reviewed, not optimized, and come without any warranty.
> They are **not** intended to be merged into the official AntennaPod app.
> For the real app, please use [AntennaPod/AntennaPod](https://github.com/AntennaPod/AntennaPod).
>
> **Changes in this fork** (November 2026, on branch `claude/kind-dirac-17amya`):
> - **Episode tags:** When adding an episode to the queue, a dialog lets you assign tags (podcast tags are preselected). Tags can also be edited from the context menu. They are stored in a separate database file (`EpisodeTags.db`); AntennaPod's own database schema is unchanged.
> - **Tag filter bar** in the queue and the inbox, including an "Untagged" chip. The inbox filter also matches podcast tags.
> - **Playback within a tag:** When a tag is selected in the queue, playback continues with the next queued episode that has that tag.
> - **Inbox "Unseen" view:** Episodes you scroll past are greyed out and hidden after the next refresh of all podcasts. You can switch between "Unseen" and "All new". The seen state is stored in a separate database file (`InboxSeen.db`).
> - **Build workflow** (`.github/workflows/fork-debug-apk.yml`): builds a signed debug APK on every push.
>
> Known limitations: drag-to-reorder is disabled while a tag filter is active; "Remove all from inbox" affects all episodes; tags of played episodes are kept.

# AntennaPod

[![GitHub check runs](https://img.shields.io/github/actions/workflow/status/AntennaPod/AntennaPod/checks.yml?branch=develop)](https://github.com/AntennaPod/AntennaPod/actions/workflows/checks.yml?query=branch%3Adevelop)
[![License: GPL v3](https://img.shields.io/github/license/AntennaPod/AntennaPod)](https://www.gnu.org/licenses/gpl-3.0)
[![GitHub Release](https://img.shields.io/github/v/release/AntennaPod/AntennaPod)](https://github.com/AntennaPod/AntennaPod/releases)
[![https://img.shields.io/github/commits-since/AntennaPod/AntennaPod/latest/develop](https://img.shields.io/github/commits-since/AntennaPod/AntennaPod/latest/develop)](https://github.com/AntennaPod/AntennaPod/commits/develop/)
[![Translations on Weblate](https://hosted.weblate.org/widget/antennapod/app/svg-badge.svg?native=1)](https://hosted.weblate.org/engage/antennapod/)
[![Good first issue](https://img.shields.io/github/issues-search?query=repo%3AAntennaPod%2FAntennaPod%20is%3Aopen%20is%3Aissue%20label%3A%22Good%20first%20issue%22&label=Good%20first%20issue&labelColor=grey&color=%235F1984)](https://github.com/AntennaPod/AntennaPod/labels/Good%20first%20issue)

This is the official repository of AntennaPod, the easy-to-use, flexible and open-source podcast manager for Android.

[<img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png"
      alt="Get it on Google Play"
      height="70">](https://play.google.com/store/apps/details?id=de.danoeh.antennapod)
[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
      alt="Get it on F-Droid"
      height="70">](https://f-droid.org/app/de.danoeh.antennapod)

<img src="https://raw.githubusercontent.com/AntennaPod/StoreMetadata/main/listings/en-US/graphics/phone-screenshots/00.png" alt="Screenshot 0" height="200"> <img src="https://raw.githubusercontent.com/AntennaPod/StoreMetadata/main/listings/en-US/graphics/phone-screenshots/01.png" alt="Screenshot 1" height="200"> <img src="https://raw.githubusercontent.com/AntennaPod/StoreMetadata/main/listings/en-US/graphics/phone-screenshots/02.png" alt="Screenshot 2" height="200"> <img src="https://raw.githubusercontent.com/AntennaPod/StoreMetadata/main/listings/en-US/graphics/phone-screenshots/03.png" alt="Screenshot 3" height="200"> <img src="https://raw.githubusercontent.com/AntennaPod/StoreMetadata/main/listings/en-US/graphics/phone-screenshots/04.png" alt="Screenshot 4" height="200"> <img src="https://raw.githubusercontent.com/AntennaPod/StoreMetadata/main/listings/en-US/graphics/phone-screenshots/05.png" alt="Screenshot 5" height="200">


## Feedback
You can use the [AntennaPod Forum](https://forum.antennapod.org/) for discussions about the app or just podcasting in general.

Bug reports and feature requests can be submitted [here](https://github.com/AntennaPod/AntennaPod/issues) (please read the [instructions](https://github.com/AntennaPod/AntennaPod/blob/develop/CONTRIBUTING.md) on how to report a bug and how to submit a feature request first!).

We also hold regular community calls to discuss anything AntennaPod-related. [Come join the next call](https://forum.antennapod.org/t/monthly-community-call/1869)!

## Help to test AntennaPod
AntennaPod has many users and we don't want them to run into trouble when we add a new feature. It's important that we have a significant group test our app, so that we know all possible combinations of phones, Android versions and use cases work as expected. Check out our wiki on how to join our [Beta testing program](https://antennapod.org/documentation/general/beta)! If a bug is reported during the beta period, chances are high that it will be fixed before the upcoming stable version. If it is reported later, fixing might take another full beta cycle. So definitely let us know if something is not right.

## License

AntennaPod is licensed under the GNU General Public License (GPL-3.0). You can find the license text in the [LICENSE](https://github.com/AntennaPod/AntennaPod/blob/develop/LICENSE) file.

## Translating AntennaPod

If you want to translate AntennaPod into another language, you can visit our [Weblate page](https://hosted.weblate.org/projects/antennapod/).


## Building AntennaPod

You can build AntennaPod just like any other Android project. Refer to the [instructions](https://github.com/AntennaPod/AntennaPod/blob/develop/CONTRIBUTING.md) for more details.

