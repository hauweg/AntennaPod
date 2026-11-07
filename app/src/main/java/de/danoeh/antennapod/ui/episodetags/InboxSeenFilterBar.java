package de.danoeh.antennapod.ui.episodetags;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.event.FeedUpdateRunningEvent;
import de.danoeh.antennapod.model.feed.FeedItem;
import de.danoeh.antennapod.net.download.serviceinterface.FeedUpdateManager;
import de.danoeh.antennapod.storage.database.InboxSeenStorage;
import de.danoeh.antennapod.ui.episodeslist.EpisodeItemViewHolder;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class InboxSeenFilterBar extends ChipGroup {
    private static final String TAG = "InboxSeenFilterBar";
    private static final String PREFS = "InboxSeenFilter";
    private static final String PREF_UNSEEN_ONLY = "unseenOnly";

    private final Runnable onSelectionChanged;
    private final SharedPreferences prefs;
    private final Map<Long, Long> seenItems = new ConcurrentHashMap<>();
    private final Set<Long> visibleItems = new HashSet<>();
    private volatile boolean unseenOnly;
    private volatile boolean applyRefresh = true;
    private volatile long manualRefreshAt = 0;
    private volatile long appliedRefresh = 0;
    private volatile int lastResultSize;

    public static InboxSeenFilterBar attachTo(View root, RecyclerView recyclerView,
                                              SwipeRefreshLayout swipeRefreshLayout, Runnable onSelectionChanged) {
        InboxSeenFilterBar bar = new InboxSeenFilterBar(root.getContext(), onSelectionChanged);
        ((ViewGroup) root.findViewById(R.id.appbar)).addView(bar, 1);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView view, int dx, int dy) {
                bar.onScrolled(view, dy);
            }
        });
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                bar.greyOutSeen(parent);
            }
        });
        recyclerView.getAdapter().registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                bar.visibleItems.clear();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                bar.visibleItems.clear();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                bar.visibleItems.clear();
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                bar.visibleItems.clear();
            }
        });
        swipeRefreshLayout.setOnRefreshListener(() -> {
            bar.manualRefreshAt = System.currentTimeMillis();
            FeedUpdateManager.getInstance().runOnceOrAsk(root.getContext());
        });
        return bar;
    }

    private InboxSeenFilterBar(Context context, Runnable onSelectionChanged) {
        super(context);
        this.onSelectionChanged = onSelectionChanged;
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        unseenOnly = prefs.getBoolean(PREF_UNSEEN_ONLY, true);
        setSingleLine(true);
        setSingleSelection(true);
        setSelectionRequired(true);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        setPadding(padding, 0, padding, padding / 4);
        addChip(true, R.string.inbox_view_unseen);
        addChip(false, R.string.inbox_view_all_new);
    }

    public boolean isFiltering() {
        return unseenOnly;
    }

    public int getLastResultSize() {
        return lastResultSize;
    }

    public void onLeave() {
        applyRefresh = true;
    }

    public List<FeedItem> filter(List<FeedItem> items) {
        long lastRefresh = InboxSeenStorage.getLastRefresh(getContext());
        long manual = manualRefreshAt;
        if (manual != 0 && lastRefresh >= manual) {
            manualRefreshAt = 0;
            applyRefresh = true;
        }
        if (applyRefresh) {
            appliedRefresh = lastRefresh;
            applyRefresh = false;
        }
        InboxSeenStorage storage = InboxSeenStorage.getInstance(getContext());
        seenItems.putAll(storage.getAll());
        if (!unseenOnly) {
            return items;
        }
        Set<Long> inboxIds = new HashSet<>();
        List<FeedItem> result = new ArrayList<>();
        for (FeedItem item : items) {
            inboxIds.add(item.getId());
            Long seenAt = seenItems.get(item.getId());
            if (seenAt == null || seenAt >= appliedRefresh) {
                result.add(item);
            }
        }
        storage.retainOnly(inboxIds);
        seenItems.keySet().retainAll(inboxIds);
        lastResultSize = result.size();
        return result;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFeedUpdateRunning(FeedUpdateRunningEvent event) {
        if (!event.isFeedUpdateRunning && manualRefreshAt != 0) {
            onSelectionChanged.run();
        }
    }

    private void onScrolled(RecyclerView view, int dy) {
        Set<Long> nowVisible = new HashSet<>();
        for (int i = 0; i < view.getChildCount(); i++) {
            FeedItem item = getItem(view, view.getChildAt(i));
            if (item != null) {
                nowVisible.add(item.getId());
            }
        }
        if (dy > 0) {
            List<Long> newlySeen = new ArrayList<>();
            for (long itemId : visibleItems) {
                if (!nowVisible.contains(itemId) && !seenItems.containsKey(itemId)) {
                    newlySeen.add(itemId);
                }
            }
            markSeen(newlySeen);
        }
        visibleItems.clear();
        visibleItems.addAll(nowVisible);
    }

    private void markSeen(List<Long> itemIds) {
        if (itemIds.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        for (long itemId : itemIds) {
            seenItems.put(itemId, now);
        }
        Context context = getContext();
        Completable.fromAction(() -> InboxSeenStorage.getInstance(context).markSeen(itemIds, now))
                .subscribeOn(Schedulers.io())
                .subscribe(() -> { }, error -> Log.e(TAG, Log.getStackTraceString(error)));
    }

    private void greyOutSeen(RecyclerView view) {
        if (!unseenOnly) {
            return;
        }
        for (int i = 0; i < view.getChildCount(); i++) {
            View child = view.getChildAt(i);
            FeedItem item = getItem(view, child);
            if (item != null && seenItems.containsKey(item.getId())) {
                ((EpisodeItemViewHolder) view.getChildViewHolder(child)).container.setAlpha(0.5f);
            }
        }
    }

    private static FeedItem getItem(RecyclerView view, View child) {
        RecyclerView.ViewHolder holder = view.getChildViewHolder(child);
        if (holder instanceof EpisodeItemViewHolder) {
            return ((EpisodeItemViewHolder) holder).getFeedItem();
        }
        return null;
    }

    private void addChip(boolean unseen, int label) {
        Chip chip = (Chip) LayoutInflater.from(getContext()).inflate(R.layout.item_tag_chip, this, false);
        chip.setId(View.generateViewId());
        chip.setText(label);
        chip.setChecked(unseen == unseenOnly);
        chip.setOnClickListener(v -> select(unseen));
        addView(chip);
    }

    private void select(boolean unseen) {
        if (unseen == unseenOnly) {
            return;
        }
        unseenOnly = unseen;
        prefs.edit().putBoolean(PREF_UNSEEN_ONLY, unseen).apply();
        onSelectionChanged.run();
    }
}
