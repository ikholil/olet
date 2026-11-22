package app.olet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.olet.data.ThemeMode
import app.olet.ui.MainViewModel
import app.olet.ui.navigation.OletNavHost
import app.olet.ui.theme.OletTheme

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels { MainViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val profile by vm.profile.collectAsStateWithLifecycle()
            OletTheme(mode = profile?.theme ?: ThemeMode.SYSTEM) {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    profile?.let { OletNavHost(vm, it) }
                }
            }
        }
    }
}
