package com.aaronmaxlab.filepicker;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Predicate;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * An implementation of the picker that operates on a document tree.
 * <br>
 * See also:
 * - https://developer.android.com/training/data-storage/shared/documents-files
 * - https://developer.android.com/reference/android/provider/DocumentsContract.Document#MIME_TYPE_DIR
 * - https://github.com/android/storage-samples/blob/main/ActionOpenDocumentTree/app/src/main/java/com/example/android/ktfiles/DirectoryFragmentViewModel.kt#L42
 * - https://github.com/googlearchive/android-DirectorySelection/blob/master/Application/src/main/java/com/example/android/directoryselection/DirectorySelectionFragment.java
 */
public class DocumentPickerFragment extends AbstractFilePickerFragment<Uri> {
    private final @NonNull Uri mRoot;
    // The structure of the file picker assumes that only the file URIs matter and you can
    // grab additional info for free afterwards. This is not the case with the documents API so we
    // have to work around it.
    private final HashMap<Uri, Document> mLastRead;
    // maps document ID of directories to parent path
    private final HashMap<String, Uri> mParents;
    protected Predicate<Document> mFilterPredicate;

    public DocumentPickerFragment(@NonNull Uri root) {
        mRoot = root;
        mLastRead = new HashMap<>();
        mParents = new HashMap<>();
    }

    /**
     * Check whether the given tree can be used with this file picker class.
     * @param context Application context
     * @param treeUri Tree URI from e.g. ACTION_OPEN_DOCUMENT_TREE
     * @return true if the directory exists
     */
    public static boolean isTreeUsable(@NonNull Context context, @NonNull Uri treeUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!DocumentsContract.isTreeUri(treeUri))
                return false;
        }
        Uri docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri,
                DocumentsContract.getTreeDocumentId(treeUri));
        try {
            return isDir(context, docUri);
        } catch (SecurityException e) {
            return false;
        }
    }

    /**
     * This method is used to set the filter that determines the documents to be shown
     *
     * @param predicate filter implementation or null
     */
    public void setFilterPredicate(@Nullable Predicate<Document> predicate) {
        this.mFilterPredicate = predicate;
        refresh(mCurrentPath);
    }

    /**
     * Returns the filter that determines the documents to be shown
     *
     * @return filter implementation or null
     */
    public @Nullable Predicate<Document> getFilterPredicate() {
        return this.mFilterPredicate;
    }

    @Override
    public boolean isDir(@NonNull Uri path) {
        Document doc = mLastRead.get(path);
        if (doc != null) {
            return doc.isDir;
        }

        // retrieve the data uncached (not supposed to happen)
        Log.w(TAG, "isDir(): uncached read");
        return isDir(requireContext(), path);
    }

    private static boolean isDir(@NonNull Context context, @NonNull Uri path) {
        final ContentResolver contentResolver = context.getContentResolver();
        final String[] cols = new String[] { DocumentsContract.Document.COLUMN_MIME_TYPE };
        Cursor c = contentResolver.query(path, cols, null, null, null, null);
        boolean ret = false;
        if (c == null)
            return ret;
        if (c.moveToFirst()) {
            final int i = c.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE);
            ret = c.getString(i).equals(DocumentsContract.Document.MIME_TYPE_DIR);
        }
        c.close();
        return ret;
    }

    @NonNull
    @Override
    public String getName(@NonNull Uri path) {
        Document doc = mLastRead.get(path);
        if (doc != null) {
            return doc.displayName;
        }

        // retrieve the data uncached (not supposed to happen)
        final ContentResolver contentResolver = requireContext().getContentResolver();
        final String[] cols = new String[] { DocumentsContract.Document.COLUMN_DISPLAY_NAME };
        Cursor c = contentResolver.query(path, cols, null, null, null, null);
        String ret = "";
        if (c == null)
            return ret;
        if (c.moveToFirst()) {
            final int i = c.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
            ret = c.getString(i);
        }
        c.close();
        return ret;
    }

    @NonNull
    @Override
    public Uri getParent(@NonNull Uri from) {
        // root path is a tree and would error if given to getDocumentId(), catch that early
        if (from.equals(getRoot()))
            return getRoot();

        String docId = DocumentsContract.getDocumentId(from);
        Uri parent = mParents.get(docId);
        if (parent != null)
            return parent;
        Log.e(TAG, "getParent() has not seen this document before");
        return getRoot();
    }

    @NonNull
    @Override
    public String pathToString(@NonNull Uri path) {
        return path.toString();
    }

    @NonNull
    @Override
    public Uri pathFromString(@NonNull String path) {
        return Uri.parse(path);
    }

    @NonNull
    @Override
    public Uri getRoot() {
        return mRoot;
    }

    @NonNull
    @Override
    public Loader<List<Uri>> getLoader() {
        final Uri root = mRoot;
        final Uri currentPath = mCurrentPath;

        final String docId = currentPath.equals(root) ? DocumentsContract.getTreeDocumentId(currentPath) :
                DocumentsContract.getDocumentId(currentPath);
        final Uri childUri = DocumentsContract.buildChildDocumentsUriUsingTree(root, docId);

        final String[] cols = new String[] {
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        };
        return new AsyncTaskLoader<>(requireContext()) {
            @Override
            public List<Uri> loadInBackground() {
                final ContentResolver contentResolver = getContext().getContentResolver();
                Cursor c = null;
                try {
                    c = contentResolver.query(childUri, cols, null, null, null, null);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to query system document provider: " + e.getMessage());
                    return new ArrayList<>(0);
                }

                if (c == null) {
                    return new ArrayList<>(0);
                }

                ArrayList<Document> files = new ArrayList<>();

                // SAFE ENTRY POINT: Dynamically lookup by column name string instead of static array order
                final int idIdx = c.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
                final int mimeIdx = c.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE);
                final int nameIdx = c.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME);

                // If Android 10 reordered or hidden columns break the index, exit safely instead of crashing
                if (idIdx == -1 || mimeIdx == -1 || nameIdx == -1) {
                    Log.e(TAG, "Critical columns missing from OS Cursor layout scheme on Android 10.");
                    c.close();
                    return new ArrayList<>(0);
                }

                while (c.moveToNext()) {
                    try {
                        final String docId = c.getString(idIdx);
                        final String mimeType = c.getString(mimeIdx);
                        final String displayName = c.getString(nameIdx);

                        if (docId == null || mimeType == null) continue;

                        final boolean isDir = mimeType.equals(DocumentsContract.Document.MIME_TYPE_DIR);
                        final String safeName = displayName != null ? displayName : "Untitled File";

                        final Document doc = new Document(
                                DocumentsContract.buildDocumentUriUsingTree(root, docId),
                                isDir,
                                safeName
                        );
                        if (mFilterPredicate != null && !mFilterPredicate.test(doc))
                            continue;
                        files.add(doc);

                        if (isDir)
                            mParents.put(docId, currentPath);

                    } catch (Exception rowException) {
                        Log.w(TAG, "Skipping bad row data layout row processing: " + rowException.getMessage());
                    }
                }
                c.close();

                Collections.sort(files);

                ArrayList<Uri> ret = new ArrayList<>(files.size());
                for (Document doc : files)
                    ret.add(doc.uri);

                mLastRead.clear();
                for (Document doc : files)
                    mLastRead.put(doc.uri, doc);
                return ret;
            }

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                forceLoad();
            }
        };
    }

    /**
     * Class that represents a document.
     * Wrapper around a content:// URI but with extra information provided at no extra cost (cached).
     */
    public static class Document implements Comparable<Document> {
        private final @NonNull Uri uri;
        private final boolean isDir;
        private final @NonNull String displayName;

        private Document(@NonNull Uri uri, boolean dir, @NonNull String name) {
            this.uri = uri;
            isDir = dir;
            displayName = name;
        }

        @NonNull
        public Uri getUri() {
            return uri;
        }

        public boolean isDirectory() {
            return isDir;
        }

        @NonNull
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            // Cached info is irrelevant, same URI = same document
            return uri.equals(((Document) o).uri);
        }

        // Sort directories before files, alphabetically otherwise
        @Override
        public int compareTo(Document other) {
            if (isDir != other.isDir)
                return other.isDir ? 1 : -1;
            return displayName.compareToIgnoreCase(other.displayName);
        }
    }

    private static final String TAG = "mpv";
}
