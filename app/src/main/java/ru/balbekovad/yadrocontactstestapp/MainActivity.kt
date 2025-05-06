package ru.balbekovad.yadrocontactstestapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import ru.balbekovad.yadrocontactstestapp.ui.screen.YadroApp
import ru.balbekovad.yadrocontactstestapp.ui.theme.YadroContactsTestAppTheme


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            YadroContactsTestAppTheme {
                YadroApp()
            }
        }
    }
}
