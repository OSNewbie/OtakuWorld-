package com.programmersbox.uiviews.utils

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.GeneratedMessageLite
import com.google.protobuf.InvalidProtocolBufferException
import com.programmersbox.uiviews.NotificationSortBy
import com.programmersbox.uiviews.Settings
import com.programmersbox.uiviews.SystemThemeMode
import com.programmersbox.uiviews.settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

suspend fun <DS : DataStore<MessageType>, MessageType : GeneratedMessageLite<MessageType, BuilderType>, BuilderType : GeneratedMessageLite.Builder<MessageType, BuilderType>> DS.update(
    statsBuilder: suspend BuilderType.() -> BuilderType
) = updateData { statsBuilder(it.toBuilder()).build() }

interface GenericSerializer<MessageType, BuilderType> : Serializer<MessageType>
        where MessageType : GeneratedMessageLite<MessageType, BuilderType>,
              BuilderType : GeneratedMessageLite.Builder<MessageType, BuilderType> {

    /**
     * Call MessageType::parseFrom here!
     */
    val parseFrom: (input: InputStream) -> MessageType

    override suspend fun readFrom(input: InputStream): MessageType =
        withContext(Dispatchers.IO) {
            try {
                parseFrom(input)
            } catch (exception: InvalidProtocolBufferException) {
                throw CorruptionException("Cannot read proto.", exception)
            }
        }

    override suspend fun writeTo(t: MessageType, output: OutputStream) =
        withContext(Dispatchers.IO) { t.writeTo(output) }
}

val Context.settings: DataStore<Settings> by dataStore(
    fileName = "Settings",
    serializer = SettingsSerializer
)

object SettingsSerializer : GenericSerializer<Settings, Settings.Builder> {
    override val defaultValue: Settings
        get() = settings {
            batteryPercent = 20
            historySave = 50
            shareChapter = true
            showAll = true
            shouldCheckUpdate = true
            themeSetting = SystemThemeMode.FollowSystem
            showListDetail = true
            showDownload = true
        }
    override val parseFrom: (input: InputStream) -> Settings get() = Settings::parseFrom
}

class SettingsHandling(context: Context) {
    private val preferences by lazy { context.settings }
    private val all: Flow<Settings> get() = preferences.data

    val systemThemeMode = all.map { it.themeSetting }
    suspend fun setSystemThemeMode(mode: SystemThemeMode) = preferences.update { setThemeSetting(mode) }

    val batteryPercentage = all.map { it.batteryPercent }
    suspend fun setBatteryPercentage(percent: Int) = preferences.update { setBatteryPercent(percent) }

    val shareChapter = all.map { it.shareChapter }
    suspend fun setShareChapter(share: Boolean) = preferences.update { setShareChapter(share) }

    val showAll = all.map { it.showAll }
    suspend fun setShowAll(show: Boolean) = preferences.update { setShowAll(show) }

    val notificationSortBy = all.map { it.notificationSortBy }

    suspend fun setNotificationSortBy(sort: NotificationSortBy) = preferences.update { setNotificationSortBy(sort) }

    val showListDetail = all.map { it.showListDetail }

    suspend fun setShowListDetail(show: Boolean) = preferences.update { setShowListDetail(show) }

    val customUrls = all.map { it.customUrlsList }

    suspend fun addCustomUrl(url: String) = preferences.update { addCustomUrls(url) }
    suspend fun removeCustomUrl(url: String) = preferences.update {
        val l = customUrlsList.toMutableList()
        l.remove(url)
        clearCustomUrls()
        addAllCustomUrls(l)
    }

    val showDownload = SettingInfo(
        flow = all.map { it.showDownload },
        updateValue = { setShowDownload(it) }
    )

    inner class SettingInfo<T>(
        val flow: Flow<T>,
        private val updateValue: suspend Settings.Builder.(T) -> Settings.Builder,
    ) {
        suspend fun updateSetting(value: T) = preferences.update { updateValue(value) }
    }
}