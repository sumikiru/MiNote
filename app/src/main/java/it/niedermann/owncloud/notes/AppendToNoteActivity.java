/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2020-2021 Stefan Niedermann <info@niedermann.it>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.LiveData;

import it.niedermann.owncloud.notes.main.MainActivity;
import it.niedermann.owncloud.notes.persistence.entity.Note;
import it.niedermann.owncloud.notes.shared.util.ShareUtil;

public class AppendToNoteActivity extends MainActivity {

    private static final String TAG = AppendToNoteActivity.class.getSimpleName();

    String receivedText = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        receivedText = ShareUtil.extractSharedText(getIntent());
        @Nullable final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setTitle(R.string.append_to_note);
        } else {
            Log.e(TAG, "SupportActionBar is null. Expected toolbar to be present to set a title.");
        }
        binding.activityNotesListView.searchToolbar.setSubtitle(receivedText);
    }

    @Override
    public void onNoteClick(int position, View v) {
        if (!TextUtils.isEmpty(receivedText)) {
            final var fullNote$ = mainViewModel.getFullNote$(((Note) adapter.getItem(position)).getId());
            fullNote$.observe(this, (fullNote) -> {
                fullNote$.removeObservers(this);
                final String oldContent = fullNote.getContent();
                String newContent;
                if (!TextUtils.isEmpty(oldContent)) {
                    newContent = oldContent + "\n\n" + receivedText;
                } else {
                    newContent = receivedText;
                }
                final var updateLiveData = mainViewModel.updateNoteAndSync(fullNote, newContent, null);
                updateLiveData.observe(this, (next) -> {
                    Toast.makeText(this, getString(R.string.added_content, receivedText), Toast.LENGTH_SHORT).show();
                    updateLiveData.removeObservers(this);
                });
            });
        } else {
            Toast.makeText(this, R.string.shared_text_empty, Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
//接收文本：活动通过 ShareUtil.extractSharedText(getIntent()) 方法从传入的 Intent 中提取共享的文本，并将其存储在 receivedText 变量中。
//设置标题：活动尝试获取 ActionBar 并设置标题为 R.string.append_to_note，如果 ActionBar 为空，则记录错误日志。
//显示接收到的文本：活动将接收到的文本显示在 binding.activityNotesListView.searchToolbar 的副标题中。
//追加文本到笔记：当用户点击某个笔记时，活动会检查 receivedText 是否为空。如果不为空，活动会获取该笔记的完整内容，并将 receivedText 追加到笔记内容的末尾。然后，活动会更新笔记内容并显示一个 Toast 提示用户内容已添加。