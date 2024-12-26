package com.programmersbox.uiviews.utils.components

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.programmersbox.uiviews.details.PaletteSwatchType

class DataStoreHandling(context: Context) {

    val showBySource = DataStoreHandler(
        context = context,
        key = booleanPreferencesKey("showBySource"),
        defaultValue = false
    )

    val shouldCheck = DataStoreHandler(
        context = context,
        key = booleanPreferencesKey("shouldCheckUpdate"),
        defaultValue = true
    )

    val historySave = DataStoreHandler(
        context = context,
        key = intPreferencesKey("history_save"),
        defaultValue = 50
    )

    val updateCheckingStart = DataStoreHandler(
        context = context,
        key = longPreferencesKey("lastUpdateCheckStart"),
        defaultValue = System.currentTimeMillis()
    )

    val updateCheckingEnd = DataStoreHandler(
        context = context,
        key = longPreferencesKey("lastUpdateCheckEnd"),
        defaultValue = System.currentTimeMillis()
    )

    val swatchType = DataStoreHandlerObject(
        context = context,
        key = stringPreferencesKey("swatchType"),
        mapToType = { runCatching { PaletteSwatchType.valueOf(it) }.getOrDefault(PaletteSwatchType.Vibrant) },
        mapToKey = { it.name },
        defaultValue = PaletteSwatchType.Vibrant
    )

    val floatingNavigation = DataStoreHandler(
        context = context,
        key = booleanPreferencesKey("floatingNavigation"),
        defaultValue = true
    )
}