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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
                    is Result.Error -> ProfileEditUiState.Error(result.message ?: "Unknown error")
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
                is Result.Error -> ProfileEditUiState.Error(result.message ?: "Save error")
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
    onBackClick: () -> Unit,
    viewModel: CreateProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = editMode) { viewModel.loadInitialProfile(editMode) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ProfileEditUiState.SaveSuccess -> {
                Toast.makeText(context, "Profile saved!", Toast.LENGTH_SHORT).show()
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
                    onBackClick = onBackClick,
                    onSaveClicked = { updatedProfile, newImageUri ->
                        viewModel.saveProfile(updatedProfile, newImageUri)
                    }
                )
            }
            is ProfileEditUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("An error occurred: ${state.message}")
                }
            }
            else -> {}
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileEditForm(
    initialProfile: UserProfile,
    onBackClick: () -> Unit,
    onSaveClicked: (profile: UserProfile, newImageUri: Uri?) -> Unit
) {
    val context = LocalContext.current
    var displayName by remember { mutableStateOf(initialProfile.displayName) }
    var bio by remember { mutableStateOf(initialProfile.bio) }

    var hobbies by remember { mutableStateOf(initialProfile.hobbies) }
    var interests by remember { mutableStateOf(initialProfile.interests) }
    var aboutTags by remember { mutableStateOf(initialProfile.aboutTags) }

    var currentHobby by remember { mutableStateOf("") }
    var currentInterest by remember { mutableStateOf("") }
    var currentTag by remember { mutableStateOf("") }
    
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    val handleAddItem = { item: String, type: String ->
        if (item.isNotBlank()) {
            Toast.makeText(context, "$type added: $item", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = primaryColor)
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "heyU!",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = LogoFontFamily,
                    fontSize = 42.sp,
                    color = primaryColor
                )
            )
            Spacer(Modifier.weight(1.3f))
        }

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(primaryColor.copy(alpha = 0.1f))
                .clickable { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null || initialProfile.photoUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri ?: initialProfile.photoUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(80.dp), tint = primaryColor)
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("Edit pictures or avatar", color = primaryColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(32.dp))

        ProfileInputField(value = displayName, onValueChange = { displayName = it }, label = "Full Name")
        Spacer(Modifier.height(12.dp))
        ProfileInputField(value = bio, onValueChange = { bio = it }, label = "Change bio")

        Spacer(Modifier.height(24.dp))

        ProfileInputField(
            value = currentHobby,
            onValueChange = { currentHobby = it },
            label = "Add your favorites (Movie, Music...)",
            onAddClick = {
                if (currentHobby.isNotBlank()) {
                    handleAddItem(currentHobby, "Hobby")
                    hobbies = hobbies + currentHobby
                    currentHobby = ""
                }
            }
        )
        TagChipGroup(tags = hobbies) { hobbies = hobbies - it }

        Spacer(Modifier.height(12.dp))

        ProfileInputField(
            value = currentInterest,
            onValueChange = { currentInterest = it },
            label = "Add your interests (Swimming, Yoga...)",
            onAddClick = {
                if (currentInterest.isNotBlank()) {
                    handleAddItem(currentInterest, "Interest")
                    interests = interests + currentInterest
                    currentInterest = ""
                }
            }
        )
        TagChipGroup(tags = interests) { interests = interests - it }

        Spacer(Modifier.height(12.dp))

        ProfileInputField(
            value = currentTag,
            onValueChange = { currentTag = it },
            label = "Add about section (Vegan, Animal lover...)",
            onAddClick = {
                if (currentTag.isNotBlank()) {
                    handleAddItem(currentTag, "About")
                    aboutTags = aboutTags + currentTag
                    currentTag = ""
                }
            }
        )
        TagChipGroup(tags = aboutTags) { aboutTags = aboutTags - it }

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = {
                val updatedProfile = initialProfile.copy(
                    displayName = displayName,
                    bio = bio,
                    hobbies = hobbies,
                    interests = interests,
                    aboutTags = aboutTags
                )
                onSaveClicked(updatedProfile, imageUri)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Save Information", fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagChipGroup(tags: List<String>, onRemove: (String) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            AssistChip(
                onClick = { onRemove(tag) },
                label = { Text("*$tag", color = Color.White) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                ),
                border = null
            )
        }
    }
}

@Composable
fun ProfileInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    onAddClick: (() -> Unit)? = null
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = Color.White

    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(label, color = onPrimaryColor.copy(alpha = 0.8f), fontSize = 14.sp) },
        modifier = Modifier.fillMaxWidth().height(52.dp),
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
        trailingIcon = {
            if (onAddClick != null) {
                IconButton(onClick = onAddClick) {
                    Icon(Icons.Default.Add, null, tint = onPrimaryColor)
                }
            }
        }
    )
}
