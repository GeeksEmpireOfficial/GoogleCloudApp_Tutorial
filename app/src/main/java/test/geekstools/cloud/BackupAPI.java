package test.geekstools.cloud;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupManager;
import android.app.backup.FileBackupHelper;
import android.app.backup.RestoreObserver;
import android.app.backup.SharedPreferencesBackupHelper;

public class BackupAPI extends BackupAgentHelper {

    // The name of the SharedPreferences file
    static final String HIGH_SCORES_FILENAME = "pref_setting";

    // A key to uniquely identify the set of backup data
    static final String FILES_BACKUP_KEY = "give_unique_name_file";

    // The names of the SharedPreferences groups that the application maintains.  These
    // are the same strings that are passed to getSharedPreferences(String, int).
    static final String PREFS_DISPLAY = "displayprefs";
    static final String PREFS_SCORES = "highscores";

    // An arbitrary string used within the BackupAgentHelper implementation to
    // identify the SharedPreferencesBackupHelper's data.
    static final String MY_PREFS_BACKUP_KEY = "give_unique_name_prefs";

    // Allocate a helper and add it to the backup agent
    @Override
    public void onCreate() {
        FileBackupHelper helperFileBackupHelper = new FileBackupHelper(this, HIGH_SCORES_FILENAME);
        addHelper(FILES_BACKUP_KEY, helperFileBackupHelper);

        SharedPreferencesBackupHelper helperSharedPreferencesBackupHelper =
                new SharedPreferencesBackupHelper(this, PREFS_DISPLAY, PREFS_SCORES);
        addHelper(MY_PREFS_BACKUP_KEY, helperSharedPreferencesBackupHelper);
    }

    public void requestBackup() {
        BackupManager bm = new BackupManager(this);
        bm.dataChanged();
    }

    public void requestRestore() {
        RestoreObserver restoreObserver = new RestoreObserver() {
            @Override
            public void onUpdate(int nowBeingRestored, String currentPackage) {
                super.onUpdate(nowBeingRestored, currentPackage);
            }
        };

        BackupManager bm = new BackupManager(this);
        bm.requestRestore(restoreObserver);
    }
}
