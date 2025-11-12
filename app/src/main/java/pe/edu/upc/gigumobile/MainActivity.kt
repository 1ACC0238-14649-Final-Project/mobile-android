package pe.edu.upc.gigumobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.room.Room
import pe.edu.upc.gigumobile.common.Constants
import pe.edu.upc.gigumobile.common.data.local.AppDatabase
import pe.edu.upc.gigumobile.common.data.local.AppDatabase.Companion.MIGRATION_1_2
import pe.edu.upc.gigumobile.common.data.remote.ServiceBuilder
import pe.edu.upc.gigumobile.gigs.data.remote.GigService
import pe.edu.upc.gigumobile.gigs.data.repository.GigRepository
import pe.edu.upc.gigumobile.gigs.presentation.GigViewModel
import pe.edu.upc.gigumobile.presentation.AppNavigation
import pe.edu.upc.gigumobile.ui.theme.GiguMobileTheme
import pe.edu.upc.gigumobile.users.data.remote.AuthService
import pe.edu.upc.gigumobile.users.data.repository.UserRepository
import pe.edu.upc.gigumobile.users.presentation.UserViewModel
import pe.edu.upc.gigumobile.pulls.data.remote.PullService
import pe.edu.upc.gigumobile.pulls.data.repository.PullRepository
import pe.edu.upc.gigumobile.pulls.presentation.PullViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Retrofit services
        val authService = ServiceBuilder.createService(AuthService::class.java)
        val gigService = ServiceBuilder.createService(GigService::class.java)
        val pullService = ServiceBuilder.createService(PullService::class.java)

        // Room database
        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, Constants.DB_NAME)
            .addMigrations(
                MIGRATION_1_2, 
                AppDatabase.MIGRATION_2_3, 
                AppDatabase.MIGRATION_3_4, 
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6
            )
            .fallbackToDestructiveMigration()
            .build()


        // Repositories
        val userRepository = UserRepository(authService, db.getUserDao(), this)
        val gigRepository = GigRepository(gigService, db.getGigDao(), db.getUserDao())
        val pullRepository = PullRepository(pullService, db.getPullDao(), db.getUserDao())

        // ViewModels
        val userViewModel = UserViewModel(userRepository)
        val gigViewModel = GigViewModel(gigRepository)
        val pullViewModel = PullViewModel(pullRepository)

        // En false el darkTheme para que la interfaz en el dispositivo se vea igual que en la interfaz de android studio
        setContent {
            GiguMobileTheme(darkTheme = false)  {
                AppNavigation(
                    userViewModel = userViewModel,
                    gigViewModel = gigViewModel,
                    pullViewModel = pullViewModel
                )
            }
        }
    }
}
