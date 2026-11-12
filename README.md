> [!WARNING]
> **This is a personal, unofficial fork of AntennaPod.**
> The changes below were "vibe coded" with an AI assistant (Claude Code) for my own use and testing.
> They are not reviewed, not optimized, and come without any warranty.
> They are **not** intended to be merged into the official AntennaPod app.
> For the real app, please use [AntennaPod/AntennaPod](https://github.com/AntennaPod/AntennaPod).
>
> **Changes in this fork** (last updated 2026-11-12, on branch `fork-main`):
> - **Episode tags:** When adding an episode to the queue, a dialog lets you assign tags (podcast tags are preselected). Tags can also be edited from the context menu. They are stored in a separate database file (`EpisodeTags.db`); AntennaPod's own database schema is unchanged.
> - **Tag filter bar** in the queue and the inbox, including an "Untagged" chip. The inbox filter also matches podcast tags.
> - **Playback within a tag:** When a tag is selected in the queue, playback continues with the next queued episode that has that tag.
> - **Inbox "Unseen" view:** Episodes you scroll past are greyed out and hidden after the next refresh of all podcasts. You can switch between "Unseen" and "All". The seen state is stored in a separate database file (`InboxSeen.db`).
> - **Build workflow** (`.github/workflows/fork-debug-apk.yml`): builds a signed debug APK on every push.
> - **Reordering with a tag filter** (2026-11-07): Drag-to-reorder also works while a tag is selected in the queue. The episode is placed directly before or after its neighbour in the filtered view; hidden episodes keep their order.
> - **"Remove all from inbox" with a tag filter** (2026-11-07): Only the inbox episodes with the selected tag are removed (including ones hidden by the "Unseen" view). The confirmation shows how many. Without a tag filter, everything is removed as before.
> - **Tag clean-up** (2026-11-07): After each refresh of all podcasts, tags of episodes that are played and no longer in the queue (or no longer exist) are removed.
> - **"Mark all as seen" in the inbox** (2026-11-09): A menu entry in the "Unseen" view marks all shown episodes (only those of the selected tag, if a tag filter is active) as seen, so the last episodes at the bottom also disappear after the next refresh.
> - **"All" view in the inbox** (2026-11-09): "All new" is now called "All" and behaves like the original AntennaPod inbox. Scrolling there no longer marks episodes as seen; the seen/hide logic only applies to the "Unseen" view.
> - **Add single episode by link** (2026-11-09): On the "Add podcast" screen, a new entry accepts a link to an audio file or to an episode web page. The app looks for the audio file on the page (meta tags, audio players, links), stores the episode in a separate podcast called "Single episodes" and adds it to the queue. This podcast is never refreshed and is not sent to gpodder/Nextcloud sync.
> - **Keep played episodes in the queue** (2026-11-09): Played episodes are no longer removed from the queue automatically. They stay greyed out and are skipped when playback continues (also within a tag). A new queue menu entry "Remove played" removes them after a confirmation showing how many (only those of the selected tag, if a tag filter is active); single episodes can still be removed via "Remove from queue". Their tags are kept as long as they are in the queue. Downloaded files of played episodes are still deleted automatically if enabled, and played episodes are not downloaded again. Can be turned off in Settings → Playback → Queue ("Keep played episodes").
> - **Full episode titles** (2026-11-11): Episode titles in episode lists (queue, inbox, episodes, podcast page, downloads, history) and on the episode details page are no longer cut off after two (or five) lines; the whole title is shown. Home screen tiles and the player screen are unchanged.
> - **Sleep timer without movement** (2026-11-12): A new sleep timer type ("min. without movement", German "Min ohne Bewegungen") uses the accelerometer. Every movement restarts the rest time (default 15 minutes). When it runs out, the volume fades out slowly and playback pauses, rewound by the rest time. Moving during the fade-out restores the volume. The sleep timer dialog shows when movement is detected, so the sensitivity can be tested in bed. Sensitivity (low/medium/high) and fade-out duration (10/30/60 seconds) are in Settings → Playback. The new texts are also translated to German.
>
> Known limitations: "Mark all as seen" only hides episodes after the next refresh of all podcasts, not immediately. "Add single episode by link" cannot find the audio file on pages that load their player only via JavaScript, and episodes added from Spotify/YouTube links do not work. With "Keep played episodes" enabled, the setting "Delete removes from queue" does not remove played episodes from the queue. The sensitivity levels of the sleep timer without movement were only tested on one phone and mattress; a partner moving in the same bed also restarts the timer. Only the new texts of the sleep timer without movement are translated to German; the other fork features are English only.

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

