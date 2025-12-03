package pe.edu.upc.gigumobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.room.Room
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import pe.edu.upc.gigumobile.R
import android.widget.Toast
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
    
    private lateinit var googleSignInClient: GoogleSignInClient
    
    // ActivityResultLauncher para Google Sign-In
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleGoogleSignInResult(task)
    }
    
    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account?.idToken
            val email = account?.email
            val name = account?.displayName
            val image = account?.photoUrl?.toString()
            
            if (idToken != null) {
                // Log para obtener el token (solo para desarrollo/testing)
                android.util.Log.d("GoogleSignIn", "ID Token: $idToken")
                android.util.Log.d("GoogleSignIn", "Email: $email")
                android.util.Log.d("GoogleSignIn", "Name: $name")
                
                // Mostrar token en Logcat y copiar a clipboard para pruebas
                // Para ver el token completo, revisa Logcat con filtro "GoogleSignIn"
                // O usa: adb logcat | grep "ID Token"
                
                // Obtener el ViewModel y llamar al método de login
                // Necesitamos pasar el callback al LoginScreen
                onGoogleSignInSuccess?.invoke(idToken, email, name, image)
            } else {
                onGoogleSignInError?.invoke("No se pudo obtener el token de Google")
            }
        } catch (e: ApiException) {
            onGoogleSignInError?.invoke("Error en Google Sign-In: ${e.statusCode}")
        }
    }
    
    // Callbacks para comunicarse con LoginScreen
    private var onGoogleSignInSuccess: ((String, String?, String?, String?) -> Unit)? = null
    private var onGoogleSignInError: ((String) -> Unit)? = null
    
    fun setGoogleSignInCallbacks(
        onSuccess: (String, String?, String?, String?) -> Unit,
        onError: (String) -> Unit
    ) {
        onGoogleSignInSuccess = onSuccess
        onGoogleSignInError = onError
    }
    
    fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configurar Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(this, gso)

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
                    pullViewModel = pullViewModel,
                    mainActivity = this,
                    context = this
                )
            }
        }
    }
}
