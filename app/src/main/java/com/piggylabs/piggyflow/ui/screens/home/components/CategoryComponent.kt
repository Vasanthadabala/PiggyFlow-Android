package com.piggylabs.piggyflow.ui.screens.home.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.piggylabs.piggyflow.data.local.entity.UserCategoryEntity
import com.piggylabs.piggyflow.ui.screens.home.Category
import com.piggylabs.piggyflow.ui.theme.appColors


sealed class CategoryUi {
    data class EnumCategory(val category: Category) : CategoryUi()
    data class UserCategory(val category: UserCategoryEntity) : CategoryUi()
}


@Composable
fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if(isSelected) Color(0xFF27C152).copy(alpha = 0.6f) else appColors().container,
            contentColor = appColors().text,
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category.label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun UserCategoryItem(
    category: UserCategoryEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: ()-> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .combinedClickable(   // ✅ supports both click & long click
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if(isSelected) Color(0xFF27C152).copy(alpha = 0.6f) else appColors().container,
            contentColor = appColors().text
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${category.emoji} ${category.name}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun CombinedCategoryGrid(
    categories: List<CategoryUi>,
    selectedEnum: Category?,
    selectedUser: UserCategoryEntity?,
    onEnumClick: (Category) -> Unit,
    onUserClick: (UserCategoryEntity) -> Unit,
    onUserLongClick: (UserCategoryEntity) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories.size) { index ->
            when (val item = categories[index]) {

                is CategoryUi.UserCategory -> {
                    Log.d("Cat", item.category.name)
                    UserCategoryItem(
                        category = item.category,
                        isSelected = item.category == selectedUser,
                        onClick = { onUserClick(item.category) },
                        onLongClick = {
                            onUserLongClick(item.category)   // ✅ delete trigger
                        }
                    )
                }

                is CategoryUi.EnumCategory -> {
                    CategoryItem(
                        category = item.category,
                        isSelected = item.category == selectedEnum,
                        onClick = { onEnumClick(item.category) }
                    )
                }
            }
        }
    }
}