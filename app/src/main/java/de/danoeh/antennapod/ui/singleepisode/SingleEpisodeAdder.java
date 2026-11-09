package de.danoeh.antennapod.ui.singleepisode;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.model.feed.Feed;
import de.danoeh.antennapod.model.feed.FeedItem;
import de.danoeh.antennapod.model.feed.FeedMedia;
import de.danoeh.antennapod.net.common.AntennapodHttpClient;
import de.danoeh.antennapod.parser.feed.util.DateUtils;
import de.danoeh.antennapod.parser.feed.util.MimeTypeUtils;
import de.danoeh.antennapod.storage.database.DBWriter;
import de.danoeh.antennapod.storage.database.FeedDatabaseWriter;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SingleEpisodeAdder {
    private static final String FEED_URL = Feed.PREFIX_SINGLE_EPISODES + "default";
    private static final String AUDIO_EXTENSIONS = "mp3|m4a|m4b|aac|ogg|oga|opus|flac|wav";
    private static final Pattern AUDIO_URL_PATTERN = Pattern.compile(
            "https?://[^\\s\"'<>\\\\]+?\\.(?:" + AUDIO_EXTENSIONS + ")(?:\\?[^\\s\"'<>\\\\]*)?",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern AUDIO_PATH_PATTERN = Pattern.compile(
            ".*\\.(?:" + AUDIO_EXTENSIONS + ")$", Pattern.CASE_INSENSITIVE);

    private SingleEpisodeAdder() {
    }

    public static FeedItem add(Context context, String url) throws IOException {
        String mediaUrl = null;
        String mimeType = null;
        long size = 0;
        String title = null;
        String description = null;
        String imageUrl = null;
        Date pubDate = null;

        Request request = new Request.Builder().url(url).build();
        try (Response response = AntennapodHttpClient.getHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP " + response.code());
            }
            ResponseBody body = response.body();
            String contentType = body != null && body.contentType() != null
                    ? body.contentType().type() + "/" + body.contentType().subtype() : null;
            if (MimeTypeUtils.isMediaFile(contentType) || isAudioPath(url)) {
                mediaUrl = url;
                mimeType = contentType;
                size = Math.max(0, body != null ? body.contentLength() : 0);
            } else if (body != null) {
                String html = body.string();
                Document document = Jsoup.parse(html, response.request().url().toString());
                mediaUrl = findMediaUrl(document, html);
                title = firstNonEmpty(meta(document, "og:title"), document.title());
                description = firstNonEmpty(meta(document, "og:description"), meta(document, "description"));
                imageUrl = metaUrl(document, "og:image");
                String published = meta(document, "article:published_time");
                if (!TextUtils.isEmpty(published)) {
                    pubDate = DateUtils.parse(published);
                }
            }
        }
        if (TextUtils.isEmpty(mediaUrl)) {
            throw new IOException(context.getString(R.string.single_episode_not_found));
        }
        if (TextUtils.isEmpty(title)) {
            title = fileName(mediaUrl);
        }
        mimeType = MimeTypeUtils.getMimeType(mimeType, mediaUrl);
        if (!MimeTypeUtils.isMediaFile(mimeType)) {
            mimeType = "audio/mpeg";
        }

        Feed feed = new Feed(FEED_URL, null, context.getString(R.string.single_episodes_feed_title), null, null);
        feed.getPreferences().setKeepUpdated(false);
        FeedItem item = new FeedItem();
        item.setTitle(title);
        item.setItemIdentifier(mediaUrl);
        item.setLink(url);
        item.setPubDate(pubDate != null ? pubDate : new Date());
        item.setImageUrl(imageUrl);
        if (description != null) {
            item.setDescriptionIfLonger(description);
        }
        item.setMedia(new FeedMedia(item, mediaUrl, size, mimeType));
        item.setFeed(feed);
        List<FeedItem> items = new ArrayList<>();
        items.add(item);
        feed.setItems(items);

        Feed saved = FeedDatabaseWriter.updateFeed(context, feed, false);
        FeedItem savedItem = null;
        if (saved != null && saved.getItems() != null) {
            for (FeedItem candidate : saved.getItems()) {
                if (mediaUrl.equals(candidate.getItemIdentifier())) {
                    savedItem = candidate;
                    break;
                }
            }
        }
        if (savedItem == null) {
            throw new IOException(context.getString(R.string.single_episode_not_found));
        }
        try {
            DBWriter.addQueueItem(context, savedItem).get();
        } catch (Exception e) {
            throw new IOException(e);
        }
        return savedItem;
    }

    private static String findMediaUrl(Document document, String html) {
        String found = firstNonEmpty(metaUrl(document, "og:audio"), metaUrl(document, "og:audio:url"),
                metaUrl(document, "og:audio:secure_url"), metaUrl(document, "twitter:player:stream"));
        if (!TextUtils.isEmpty(found)) {
            return found;
        }
        for (Element element : document.select("audio[src], audio source[src], source[type^=audio][src]")) {
            String src = element.absUrl("src");
            if (!TextUtils.isEmpty(src)) {
                return src;
            }
        }
        for (Element element : document.select("a[href]")) {
            String href = element.absUrl("href");
            if (isAudioPath(href)) {
                return href;
            }
        }
        Matcher matcher = AUDIO_URL_PATTERN.matcher(html.replace("\\/", "/"));
        if (matcher.find()) {
            return matcher.group().replace("&amp;", "&");
        }
        return null;
    }

    private static boolean isAudioPath(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        String path = Uri.parse(url).getPath();
        return path != null && AUDIO_PATH_PATTERN.matcher(path).matches();
    }

    private static String meta(Document document, String name) {
        Element element = document.selectFirst("meta[property=\"" + name + "\"], meta[name=\"" + name + "\"]");
        if (element == null) {
            return null;
        }
        return element.attr("content");
    }

    private static String metaUrl(Document document, String name) {
        Element element = document.selectFirst("meta[property=\"" + name + "\"], meta[name=\"" + name + "\"]");
        if (element == null) {
            return null;
        }
        String absolute = element.absUrl("content");
        return TextUtils.isEmpty(absolute) ? element.attr("content") : absolute;
    }

    private static String firstNonEmpty(String... values) {
        for (String value : values) {
            if (!TextUtils.isEmpty(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private static String fileName(String url) {
        String segment = Uri.parse(url).getLastPathSegment();
        if (TextUtils.isEmpty(segment)) {
            return url;
        }
        int dot = segment.lastIndexOf('.');
        return dot > 0 ? segment.substring(0, dot) : segment;
    }
}
