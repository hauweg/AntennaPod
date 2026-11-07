package de.danoeh.antennapod.ui.episodetags;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.databinding.EpisodeTagsDialogBinding;
import de.danoeh.antennapod.event.FeedItemEvent;
import de.danoeh.antennapod.model.feed.Feed;
import de.danoeh.antennapod.model.feed.FeedItem;
import de.danoeh.antennapod.model.feed.FeedPreferences;
import de.danoeh.antennapod.storage.database.DBReader;
import de.danoeh.antennapod.storage.database.DBWriter;
import de.danoeh.antennapod.storage.database.NavDrawerData;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.greenrobot.eventbus.EventBus;

public class EpisodeTagDialog {
    private static final String TAG = "EpisodeTagDialog";

    private EpisodeTagDialog() {
    }

    public static void addToQueue(Context context, FeedItem item) {
        show(context, Collections.singletonList(item), true, null);
    }

    public static void addToQueue(Context context, List<FeedItem> items, @Nullable Runnable onAdded) {
        show(context, items, true, onAdded);
    }

    public static void editTags(Context context, FeedItem item) {
        show(context, Collections.singletonList(item), false, null);
    }

    private static void show(Context context, List<FeedItem> items, boolean addToQueue, @Nullable Runnable onAdded) {
        if (items.isEmpty()) {
            if (onAdded != null) {
                onAdded.run();
            }
            return;
        }
        EpisodeTagStorage storage = EpisodeTagStorage.getInstance(context);
        Observable.fromCallable(() -> {
            List<String> allTags = loadAllTags(storage, true);
            Set<String> selected = new HashSet<>();
            for (FeedItem item : items) {
                Set<String> stored = storage.getTags(item.getId());
                if (!stored.isEmpty()) {
                    selected.addAll(stored);
                } else if (addToQueue) {
                    selected.addAll(getFeedTags(item));
                }
            }
            for (String tag : selected) {
                if (!allTags.contains(tag)) {
                    allTags.add(tag);
                }
            }
            Collections.sort(allTags, String.CASE_INSENSITIVE_ORDER);
            return new Pair<>(allTags, selected);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> showDialog(context, items, addToQueue, onAdded, result.first, result.second),
                        error -> Log.e(TAG, Log.getStackTraceString(error)));
    }

    private static void showDialog(Context context, List<FeedItem> items, boolean addToQueue,
                                   @Nullable Runnable onAdded, List<String> allTags, Set<String> selected) {
        EpisodeTagsDialogBinding binding = EpisodeTagsDialogBinding.inflate(LayoutInflater.from(context));
        for (String tag : allTags) {
            addChip(binding, tag, selected.contains(tag));
        }
        binding.episodeTagsNewTagInput.setEndIconOnClickListener(v -> addNewTag(binding));
        binding.episodeTagsNewTagEditText.setOnEditorActionListener((v, actionId, event) -> {
            addNewTag(binding);
            return true;
        });

        boolean[] confirmed = {false};
        new MaterialAlertDialogBuilder(context)
                .setTitle(addToQueue ? R.string.add_to_queue_label : R.string.edit_tags)
                .setView(binding.getRoot())
                .setPositiveButton(addToQueue ? R.string.add_to_queue_label : android.R.string.ok, (d, which) -> {
                    confirmed[0] = true;
                    addNewTag(binding);
                    save(context, items, getCheckedTags(binding), addToQueue, onAdded);
                })
                .setNegativeButton(R.string.cancel_label, null)
                .setOnDismissListener(d -> {
                    if (!confirmed[0]) {
                        EventBus.getDefault().post(new FeedItemEvent(items, false));
                    }
                })
                .show();
    }

    private static void save(Context context, List<FeedItem> items, Set<String> tags, boolean addToQueue,
                             @Nullable Runnable onAdded) {
        List<Long> ids = new ArrayList<>();
        for (FeedItem item : items) {
            ids.add(item.getId());
        }
        Completable.fromAction(() -> EpisodeTagStorage.getInstance(context).setTags(ids, tags))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    if (addToQueue) {
                        DBWriter.addQueueItem(context, items.toArray(new FeedItem[0]));
                        if (onAdded != null) {
                            onAdded.run();
                        }
                    }
                }, error -> Log.e(TAG, Log.getStackTraceString(error)));
    }

    private static void addChip(EpisodeTagsDialogBinding binding, String tag, boolean checked) {
        Chip chip = (Chip) LayoutInflater.from(binding.getRoot().getContext())
                .inflate(R.layout.item_tag_chip, binding.episodeTagsChipGroup, false);
        chip.setId(View.generateViewId());
        chip.setText(tag);
        chip.setChecked(checked);
        binding.episodeTagsChipGroup.addView(chip);
    }

    private static void addNewTag(EpisodeTagsDialogBinding binding) {
        String tag = binding.episodeTagsNewTagEditText.getText() == null
                ? "" : binding.episodeTagsNewTagEditText.getText().toString().trim();
        if (TextUtils.isEmpty(tag)) {
            return;
        }
        binding.episodeTagsNewTagEditText.setText("");
        for (int i = 0; i < binding.episodeTagsChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) binding.episodeTagsChipGroup.getChildAt(i);
            if (tag.equalsIgnoreCase(chip.getText().toString())) {
                chip.setChecked(true);
                return;
            }
        }
        addChip(binding, tag, true);
    }

    private static Set<String> getCheckedTags(EpisodeTagsDialogBinding binding) {
        Set<String> tags = new HashSet<>();
        for (int i = 0; i < binding.episodeTagsChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) binding.episodeTagsChipGroup.getChildAt(i);
            if (chip.isChecked()) {
                tags.add(chip.getText().toString());
            }
        }
        return tags;
    }

    static Set<String> getFeedTags(FeedItem item) {
        Set<String> tags = new HashSet<>();
        if (item.getFeed() != null && item.getFeed().getPreferences() != null) {
            tags.addAll(item.getFeed().getPreferences().getTags());
            tags.remove(FeedPreferences.TAG_ROOT);
        }
        return tags;
    }

    static List<String> loadAllTags(EpisodeTagStorage storage, boolean includeFeedTags) {
        List<String> tags = new ArrayList<>(storage.getAllTags());
        if (includeFeedTags) {
            for (NavDrawerData.TagItem tagItem : DBReader.getAllTags(Feed.STATE_SUBSCRIBED)) {
                String title = tagItem.getTitle();
                if (!FeedPreferences.TAG_ROOT.equals(title) && !FeedPreferences.TAG_UNTAGGED.equals(title)
                        && !tags.contains(title)) {
                    tags.add(title);
                }
            }
        }
        Collections.sort(tags, String.CASE_INSENSITIVE_ORDER);
        return tags;
    }
}
