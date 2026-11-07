package de.danoeh.antennapod.ui.episodetags;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.model.feed.FeedItem;
import de.danoeh.antennapod.model.feed.FeedPreferences;
import de.danoeh.antennapod.storage.database.EpisodeTagStorage;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class EpisodeTagFilterBar extends HorizontalScrollView {
    private static final String TAG = "EpisodeTagFilterBar";

    private final String screenKey;
    private final boolean includeFeedTags;
    private final Runnable onSelectionChanged;
    private final ChipGroup chipGroup;
    private final SharedPreferences prefs;
    private volatile String selectedTag;
    private volatile int lastResultSize;
    private Disposable disposable;

    public static EpisodeTagFilterBar attachTo(View root, String screenKey, boolean includeFeedTags,
                                               Runnable onSelectionChanged) {
        EpisodeTagFilterBar bar = new EpisodeTagFilterBar(root.getContext(), screenKey, includeFeedTags,
                onSelectionChanged);
        ((ViewGroup) root.findViewById(R.id.appbar)).addView(bar, 1);
        return bar;
    }

    private EpisodeTagFilterBar(Context context, String screenKey, boolean includeFeedTags,
                                Runnable onSelectionChanged) {
        super(context);
        this.screenKey = screenKey;
        this.includeFeedTags = includeFeedTags;
        this.onSelectionChanged = onSelectionChanged;
        prefs = context.getSharedPreferences(EpisodeTagStorage.FILTER_PREFS, Context.MODE_PRIVATE);
        selectedTag = prefs.getString(screenKey, null);
        setHorizontalScrollBarEnabled(false);
        setVisibility(selectedTag == null ? GONE : VISIBLE);
        chipGroup = new ChipGroup(context);
        chipGroup.setSingleLine(true);
        chipGroup.setSingleSelection(true);
        chipGroup.setSelectionRequired(true);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        chipGroup.setPadding(padding, 0, padding, padding / 4);
        addView(chipGroup, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }

    public boolean isFiltering() {
        return selectedTag != null;
    }

    public int getLastResultSize() {
        return lastResultSize;
    }

    public List<FeedItem> filter(List<FeedItem> items) {
        String tag = selectedTag;
        if (tag == null) {
            return items;
        }
        Map<Long, Set<String>> itemTags = EpisodeTagStorage.getInstance(getContext()).getAllItemTags();
        List<FeedItem> result = new ArrayList<>();
        for (FeedItem item : items) {
            Set<String> tags = itemTags.get(item.getId());
            boolean matches = tags != null && tags.contains(tag);
            if (!matches && includeFeedTags) {
                matches = EpisodeTagDialog.getFeedTags(item).contains(tag);
            }
            if (FeedPreferences.TAG_UNTAGGED.equals(tag)) {
                matches = (tags == null || tags.isEmpty())
                        && (!includeFeedTags || EpisodeTagDialog.getFeedTags(item).isEmpty());
            }
            if (matches) {
                result.add(item);
            }
        }
        lastResultSize = result.size();
        return result;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        refreshTags();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
        if (disposable != null) {
            disposable.dispose();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTagsChanged(EpisodeTagStorage.ChangedEvent event) {
        refreshTags();
        if (isFiltering()) {
            onSelectionChanged.run();
        }
    }

    private void refreshTags() {
        if (disposable != null) {
            disposable.dispose();
        }
        disposable = Observable.fromCallable(() -> EpisodeTagDialog.loadAllTags(
                        EpisodeTagStorage.getInstance(getContext()), includeFeedTags))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showTags, error -> Log.e(TAG, Log.getStackTraceString(error)));
    }

    private void showTags(List<String> tags) {
        if (selectedTag != null && !tags.contains(selectedTag) && !FeedPreferences.TAG_UNTAGGED.equals(selectedTag)) {
            tags.add(selectedTag);
        }
        chipGroup.removeAllViews();
        addChip(null, getContext().getString(R.string.tag_all));
        addChip(FeedPreferences.TAG_UNTAGGED, getContext().getString(R.string.tag_untagged));
        for (String tag : tags) {
            addChip(tag, tag);
        }
        setVisibility(tags.isEmpty() ? GONE : VISIBLE);
    }

    private void addChip(String tag, String label) {
        Chip chip = (Chip) LayoutInflater.from(getContext()).inflate(R.layout.item_tag_chip, chipGroup, false);
        chip.setId(View.generateViewId());
        chip.setText(label);
        chip.setChecked(Objects.equals(tag, selectedTag));
        chip.setOnClickListener(v -> select(tag));
        chipGroup.addView(chip);
    }

    private void select(String tag) {
        if (Objects.equals(tag, selectedTag)) {
            return;
        }
        selectedTag = tag;
        prefs.edit().putString(screenKey, tag).apply();
        onSelectionChanged.run();
    }
}
