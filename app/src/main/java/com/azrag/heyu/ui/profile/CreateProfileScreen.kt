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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.rememberAsyncImagePainter
import com.azrag.heyu.data.model.UserProfile
import com.azrag.heyu.data.repository.UserRepository
import com.azrag.heyu.ui.theme.LogoFontFamily
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

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (val state = uiState) {
            is ProfileEditUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
            else -> {}
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
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "heyU!",
            style = MaterialTheme.typography.displayLarge.copy(
                fontFamily = LogoFontFamily,
                fontSize = 52.sp,
                color = primaryColor
            )
        )

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(primaryColor)
                .clickable { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null || initialProfile.photoUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(model = imageUri ?: initialProfile.photoUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = onPrimaryColor
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Resimleri veya avatar ekle",
            color = primaryColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(32.dp))

        ProfileInputField(value = displayName, onValueChange = { displayName = it }, label = "Adı Soyadı")
        Spacer(Modifier.height(12.dp))
        ProfileInputField(value = department, onValueChange = { department = it }, label = "Bölüm")
        Spacer(Modifier.height(12.dp))
        ProfileInputField(value = bio, onValueChange = { bio = it }, label = "Bio")
        Spacer(Modifier.height(12.dp))
        ProfileInputField(value = "", onValueChange = {}, label = "Favorilerini ekle", isStatic = true)
        Spacer(Modifier.height(12.dp))
        ProfileInputField(value = "", onValueChange = {}, label = "İlgi alanlarını ekle", isStatic = true)
        Spacer(Modifier.height(12.dp))
        ProfileInputField(value = "", onValueChange = {}, label = "Hakkında kısmını ekle", isStatic = true)

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = {
                val updatedProfile = initialProfile.copy(
                    displayName = displayName,
                    department = department,
                    bio = bio
                )
                onSaveClicked(updatedProfile, imageUri)
            },
            modifier = Modifier
                .width(200.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Bilgileri Kaydet", fontWeight = FontWeight.Bold, color = onPrimaryColor)
        }
        
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun ProfileInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isStatic: Boolean = false
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(label, color = onPrimaryColor.copy(alpha = 0.8f), fontSize = 14.sp) },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = primaryColor,
            unfocusedContainerColor = primaryColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = onPrimaryColor,
            unfocusedTextColor = onPrimaryColor
        ),
        shape = RoundedCornerShape(10.dp),
        singleLine = true,
        readOnly = isStatic,
        trailingIcon = {
            Icon(Icons.Default.Add, null, tint = onPrimaryColor, modifier = Modifier.size(20.dp))
        }
    )
}
