package com.tugasss.laundryapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import java.util.Locale;

public class LanguageUtils {
    private static final String PREFS_NAME = "language_prefs";
    private static final String LANGUAGE_KEY = "selected_language";
    private static final String LANGUAGE_INDONESIAN = "id";
    private static final String LANGUAGE_ENGLISH = "en";

    /**
     * Menyimpan preferensi bahasa yang dipilih
     */
    public static void saveLanguagePreference(Context context, String languageCode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(LANGUAGE_KEY, languageCode);
        editor.apply();
    }

    /**
     * Mengambil preferensi bahasa yang tersimpan
     */
    public static String getLanguagePreference(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(LANGUAGE_KEY, LANGUAGE_INDONESIAN); // Default Indonesian
    }

    /**
     * Mengatur bahasa aplikasi
     */
    public static void setAppLanguage(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);

        context.createConfigurationContext(config);
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        // Simpan preferensi
        saveLanguagePreference(context, languageCode);
    }

    /**
     * Mengecek apakah bahasa saat ini adalah bahasa Inggris
     */
    public static boolean isEnglish(Context context) {
        return LANGUAGE_ENGLISH.equals(getLanguagePreference(context));
    }

    /**
     * Toggle bahasa antara Indonesia dan Inggris
     */
    public static String toggleLanguage(Context context) {
        String currentLanguage = getLanguagePreference(context);
        String newLanguage = LANGUAGE_INDONESIAN.equals(currentLanguage) ?
                LANGUAGE_ENGLISH : LANGUAGE_INDONESIAN;
        setAppLanguage(context, newLanguage);
        return newLanguage;
    }

    /**
     * Konstanta untuk kode bahasa
     */
    public static final class Languages {
        public static final String INDONESIAN = LANGUAGE_INDONESIAN;
        public static final String ENGLISH = LANGUAGE_ENGLISH;
    }
}