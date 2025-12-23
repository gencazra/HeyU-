package com.azrag.heyu.ui.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.rememberAsyncImagePainter
import com.azrag.heyu.R
import com.azrag.heyu.data.model.UserProfile
import com.azrag.heyu.data.repository.UserRepository
import com.azrag.heyu.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileEditUiState>(ProfileEditUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private var initialProfileLoaded = false

    fun loadInitialProfile(editMode: Boolean) {
        if (initialProfileLoaded) return
        initialProfileLoaded = true
        viewModelScope.launch {
            if (editMode) {
                _uiState.value = when (val result = userRepository.getCurrentUserProfile()) {
                    is Result.Success<UserProfile?> -> ProfileEditUiState.Success(result.data ?: UserProfile())
                    is Result.Error -> ProfileEditUiState.Error(result.message ?: "Bilinmeyen hata")
                    is Result.Loading -> ProfileEditUiState.Loading
                }
            } else {
                _uiState.value = ProfileEditUiState.Success(UserProfile())
            }
        }
    }

    fun saveProfile(profileData: UserProfile, newImageUri: Uri?) {
        viewModelScope.launch {
            _uiState.value = ProfileEditUiState.Loading
            val result = userRepository.saveUserProfile(profileData, newImageUri)
            _uiState.value = when (result) {
                is Result.Success<*> -> ProfileEditUiState.SaveSuccess
                is Result.Error -> ProfileEditUiState.Error(result.message ?: "Kayıt hatası")
                is Result.Loading -> ProfileEditUiState.Loading
            }
        }
    }
}

sealed class ProfileEditUiState {
    object Loading : ProfileEditUiState()
    data class Success(val profile: UserProfile) : ProfileEditUiState()
    data class Error(val message: String) : ProfileEditUiState()
    object SaveSuccess : ProfileEditUiState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProfileScreen(
    editMode: Boolean,
    onProfileSaved: () -> Unit,
    viewModel: CreateProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = editMode) { viewModel.loadInitialProfile(editMode) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ProfileEditUiState.SaveSuccess -> {
                Toast.makeText(context, "Profil kaydedildi!", Toast.LENGTH_SHORT).show()
                onProfileSaved()
            }
            is ProfileEditUiState.Error -> {
                Toast.makeText(context, (uiState as ProfileEditUiState.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (editMode) "Profili Düzenle" else "Profil Oluştur") })
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val state = uiState) {
                is ProfileEditUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ProfileEditUiState.Success -> {
                    ProfileEditForm(
                        initialProfile = state.profile,
                        onSaveClicked = { updatedProfile, newImageUri ->
                            viewModel.saveProfile(updatedProfile, newImageUri)
                        }
                    )
                }
                is ProfileEditUiState.Error -> {
                    ProfileEditForm(
                        initialProfile = UserProfile(),
                        onSaveClicked = { updatedProfile, newImageUri ->
                            viewModel.saveProfile(updatedProfile, newImageUri)
                        }
                    )
                }
                else -> { }
            }
        }
    }
}

@Composable
private fun ProfileEditForm(
    initialProfile: UserProfile,
    onSaveClicked: (profile: UserProfile, newImageUri: Uri?) -> Unit
) {
    var displayName by remember { mutableStateOf(initialProfile.displayName ?: "") }
    var department by remember { mutableStateOf(initialProfile.department ?: "") }
    var bio by remember { mutableStateOf(initialProfile.bio ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
            val imageModel = imageUri ?: initialProfile.photoUrl.ifEmpty { R.drawable.ic_default_profile }

            Image(
                painter = rememberAsyncImagePainter(model = imageModel),
                contentDescription = "Profil Fotoğrafı",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .clickable { imagePickerLauncher.launch("image/*") }
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AddAPhoto, "Fotoğraf Seç", tint = Color.White, modifier = Modifier.size(36.dp))
            }
        }

        Spacer(Modifier.height(24.dp))
        OutlinedTextField(value = displayName, onValueChange = { displayName = it }, label = { Text("İsim Soyisim") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = department, onValueChange = { department = it }, label = { Text("Bölüm") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Hakkında") }, modifier = Modifier.fillMaxWidth().height(120.dp))

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                val updatedProfile = initialProfile.copy(
                    displayName = displayName,
                    department = department,
                    bio = bio
                )
                onSaveClicked(updatedProfile, imageUri)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("KAYDET")
        }
    }
}
