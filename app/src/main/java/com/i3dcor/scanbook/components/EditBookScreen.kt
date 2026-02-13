package com.i3dcor.scanbook.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.i3dcor.scanbook.ui.theme.ScanBookTheme

@Composable
fun EditBookScreen(
    modifier: Modifier = Modifier,
    onSaveClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    // State holders for preview purposes
    var title by remember { mutableStateOf("Domain-Driven Design") }
    var author by remember { mutableStateOf("Eric Evans") }
    var isbn by remember { mutableStateOf("978-0-321-12521-7") }
    var genre by remember { mutableStateOf("Computer Science") }
    var price by remember { mutableStateOf("54.99") }
    var condition by remember { mutableStateOf("Good") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EditBookHeader()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            BookPhotoSection()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Form Fields
            BookTextField(
                label = "ISBN",
                value = isbn,
                onValueChange = { isbn = it },
                trailingIcon = {
                    // Placeholder for barcode icon
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.Gray, RoundedCornerShape(2.dp))
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BookTextField(
                label = "Title",
                value = title,
                onValueChange = { title = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BookTextField(
                label = "Author",
                value = author,
                onValueChange = { author = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            GenreDropdownField(
                label = "Genre",
                value = genre,
                onClick = { /* Open dropdown */ }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BookTextField(
                label = "Price",
                value = price,
                onValueChange = { price = it },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Condition",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            
            ConditionSelector(
                selectedCondition = condition,
                onConditionSelected = { condition = it }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            SaveButton(onClick = onSaveClick)
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun EditBookHeader() {
    Text(
        text = "Edit Book",
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = Color.White
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun BookPhotoSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PhotoPlaceholderButton(
            text = "Front Cover",
            modifier = Modifier.weight(1f)
        )
        PhotoPlaceholderButton(
            text = "Back Cover",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun PhotoPlaceholderButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        color = Color(0xFF252528),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .height(80.dp)
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = Color(0xFF3A3A3C), // Dark gray border
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF1E2838), CircleShape), // Dark blue circle
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Color(0xFF448AFF), // Blue icon
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Add Photo",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun BookTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Surface(
            color = Color(0xFF252528),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF3A3A3C), RoundedCornerShape(8.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = LocalTextStyle.current.copy(
                        color = Color.White,
                        fontSize = 16.sp
                    ),
                    cursorBrush = SolidColor(Color.White),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    trailingIcon()
                }
            }
        }
    }
}

@Composable
fun GenreDropdownField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Surface(
            color = Color(0xFF252528),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .border(1.dp, Color(0xFF3A3A3C), RoundedCornerShape(8.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
        }
    }
}

@Composable
fun ConditionSelector(
    selectedCondition: String,
    onConditionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val options = listOf("New", "Good", "Damaged")
        
        options.forEach { option ->
            val isSelected = option == selectedCondition
            ConditionOption(
                text = option,
                isSelected = isSelected,
                onClick = { onConditionSelected(option) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ConditionOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Color(0xFF2962FF) else Color(0xFF252528)
    val textColor = if (isSelected) Color.White else Color.Gray
    val borderColor = if (isSelected) Color(0xFF2962FF) else Color(0xFF3A3A3C)

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = textColor,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
fun SaveButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2962FF) // Primary Blue
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = "Save Changes",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditBookScreenPreview() {
    ScanBookTheme {
        EditBookScreen()
    }
}
