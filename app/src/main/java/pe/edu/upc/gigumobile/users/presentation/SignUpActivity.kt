package pe.edu.upc.gigumobile.users.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.room.Room
import pe.edu.upc.gigumobile.common.Constants
import pe.edu.upc.gigumobile.common.data.local.AppDatabase
import pe.edu.upc.gigumobile.common.data.remote.ServiceBuilder
import pe.edu.upc.gigumobile.ui.theme.tpblueprintTheme
import pe.edu.upc.gigumobile.users.data.remote.AuthService
import pe.edu.upc.gigumobile.users.data.repository.UserRepository

class SignUpActivity : ComponentActivity() {

    private lateinit var viewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, Constants.DB_NAME).build()
        val service = ServiceBuilder.createService(AuthService::class.java)
        val repo = UserRepository(service, db.getUserDao())
        viewModel = UserViewModel(repo)

        setContent {
            tpblueprintTheme {
                RegisterScreen(
                    viewModel = viewModel,
                    onRegisterSuccess = {
                        finish()
                    },
                    onBack = { finish() }
                )
            }
        }
    }
}
