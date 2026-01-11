package com.azrag.heyu.ui.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.azrag.heyu.R
import com.azrag.heyu.data.model.Notice
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeCard(
    notice: Notice,
    onImInClicked: () -> Unit,
    onClick: () -> Unit = {}
) {
    val currentUserId = Firebase.auth.currentUser?.uid
    val isCurrentUserAttending = notice.attendees.contains(currentUserId)
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale("tr"))

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = if (!notice.creatorImageUrl.isNullOrBlank()) {
                            rememberAsyncImagePainter(model = notice.creatorImageUrl)
                        } else {
                            painterResource(id = R.drawable.ic_default_profile)
                        },
                        contentDescription = "Profil Fotoğrafı",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = notice.creatorName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = notice.timestamp?.let { dateFormat.format(it) } ?: "Şimdi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                SuggestionChip(
                    onClick = {  },
                    label = { 
                        Text(
                            text = notice.category,
                            color = MaterialTheme.colorScheme.primary
                        ) 
                    },
                    border = SuggestionChipDefaults.suggestionChipBorder(enabled = true)
                )
            }

            if (!notice.imageUrl.isNullOrBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(model = notice.imageUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(150.dp).clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = notice.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = notice.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 3
                )
            }

            if (notice.eventDate != null || notice.eventTime != null || notice.location != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)
                    
                    if (!notice.location.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn, 
                                contentDescription = null, 
                                modifier = Modifier.size(16.dp), 
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(notice.location, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (!notice.eventDate.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.DateRange, 
                                    contentDescription = null, 
                                    modifier = Modifier.size(16.dp), 
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(notice.eventDate, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        if (!notice.eventTime.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Schedule, 
                                    contentDescription = null, 
                                    modifier = Modifier.size(16.dp), 
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(notice.eventTime, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${notice.attendees.size} kişi katılıyor",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Button(
                    onClick = onImInClicked,
                    colors = if (isCurrentUserAttending) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    } else {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = if (isCurrentUserAttending) "KATILIYORSUN" else "BEN DE VARIM!",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
