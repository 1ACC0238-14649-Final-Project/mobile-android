package pe.edu.upc.gigumobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.room.Room
import pe.edu.upc.gigumobile.common.Constants
import pe.edu.upc.gigumobile.common.data.local.AppDatabase
import pe.edu.upc.gigumobile.common.data.remote.ServiceBuilder
import pe.edu.upc.gigumobile.presentation.AppNavigation
import pe.edu.upc.gigumobile.ui.theme.tpblueprintTheme
import pe.edu.upc.gigumobile.users.data.remote.AuthService
import pe.edu.upc.gigumobile.users.data.repository.UserRepository
import pe.edu.upc.gigumobile.users.presentation.UserViewModel
import pe.edu.upc.gigumobile.common.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Retrofit services
        val authService = ServiceBuilder.createService(AuthService::class.java)

        // Room database
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            Constants.DB_NAME
        ).build()

        // Session Manager
        val sessionManager = SessionManager(applicationContext)

        // Repositories TEST
        val userRepository = UserRepository(
            authService,
            db.getUserDao(),
            this
        )

        // Repositories ORIGINAL
        //val userRepository = UserRepository(authService, db.getUserDao())

        // ViewModels
        val userViewModel = UserViewModel(userRepository)

        setContent {
            tpblueprintTheme {
                // AppNavigation
                AppNavigation(
                    userViewModel = userViewModel
                )
            }
        }
    }
}
