package pe.edu.upc.gigumobile.users.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pe.edu.upc.gigumobile.common.Resource
import pe.edu.upc.gigumobile.common.UIState
import pe.edu.upc.gigumobile.users.data.remote.LoginRequest
import pe.edu.upc.gigumobile.users.data.remote.SignUpRequest
import pe.edu.upc.gigumobile.users.data.repository.UserRepository
import pe.edu.upc.gigumobile.users.domain.model.User

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    private val _loginState = mutableStateOf(UIState<String>())
    val loginState: State<UIState<String>> get() = _loginState

    private val _signupState = mutableStateOf(UIState<Unit>())
    val signupState: State<UIState<Unit>> get() = _signupState

    // Logged User state
    private val _currentUser = mutableStateOf<User?>(null)
    val currentUser: State<User?> get() = _currentUser

    init {
        // When starting ViewModel, load saved user if exists
        viewModelScope.launch {
            val savedUser = repository.getSavedUser()
            _currentUser.value = savedUser
        }
    }

    fun login(email: String, password: String) {
        _loginState.value = UIState(isLoading = true)
        viewModelScope.launch {
            val res = repository.login(LoginRequest(email, password))
            if (res is Resource.Success) {
                // Load user data after successful login
                _currentUser.value = repository.getSavedUser()
                _loginState.value = UIState(data = res.data)
            } else {
                _loginState.value = UIState(message = res.message ?: "Login error")
            }
        }
    }

    fun signUp(name: String, lastname: String, email: String, password: String, role: String, image: String) {
        _signupState.value = UIState(isLoading = true)
        viewModelScope.launch {
            val req = SignUpRequest(name, lastname, email, password, role, image)
            val res = repository.signUp(req)
            if (res is Resource.Success) {
                _signupState.value = UIState(data = Unit)
            } else {
                _signupState.value = UIState(message = res.message ?: "Sign up error")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.clearSession()
            _currentUser.value = null
            _loginState.value = UIState()
            _signupState.value = UIState()
        }
    }

    suspend fun getSavedUser() = repository.getSavedUser()
}
