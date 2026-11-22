package app.olet

import android.app.Application
import app.olet.data.AppDatabase
import app.olet.data.Repository
import app.olet.data.UserPrefs

class OletApp : Application() {
    lateinit var repository: Repository
        private set
    lateinit var prefs: UserPrefs
        private set

    override fun onCreate() {
        super.onCreate()
        repository = Repository(AppDatabase.build(this))
        prefs = UserPrefs(this)
    }
}
