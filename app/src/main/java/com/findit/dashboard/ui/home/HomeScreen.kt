package com.findit.dashboard.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.findit.dashboard.DashboardItem
import com.findit.dashboard.Floors
import com.findit.dashboard.R
import com.findit.dashboard.ui.components.CircleIconButton
import com.findit.dashboard.ui.components.ItemCard
import com.findit.dashboard.ui.components.PhotoImage
import com.findit.dashboard.ui.theme.DropdownBlue
import com.findit.dashboard.ui.theme.FabBlack
import com.findit.dashboard.ui.theme.GradientTop
import com.findit.dashboard.ui.theme.HeroPill
import com.findit.dashboard.ui.theme.Ink
import com.findit.dashboard.ui.theme.MainGradient
import kotlinx.coroutines.launch

/** The Dashboard: top bar, hero carousel, floor dropdown and the grid of added items. */
@Composable
fun HomeScreen(
    allItems: List<DashboardItem>,
    onSearchClick: () -> Unit,
    onVoiceSearchClick: () -> Unit,
    onAddClick: () -> Unit,
    onDetailsClick: (DashboardItem) -> Unit
) {
    var floor by rememberSaveable { mutableStateOf(Floors.ALL) }
    val shownItems = if (floor == Floors.ALL) allItems else allItems.filter { it.floor == floor }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MainGradient)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, bottom = 110.dp)
        ) {
            // Top bar
            item(span = { GridItemSpan(maxLineSpan) }) {
                TopBar(onSearchClick = onSearchClick)
            }

            // Hero carousel with the 3 newest items
            item(span = { GridItemSpan(maxLineSpan) }) {
                HeroCarousel(
                    heroItems = allItems.take(3),
                    onDetailsClick = onDetailsClick
                )
            }

            // "Added Items" + floor dropdown
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(
                    selectedFloor = floor,
                    onFloorSelected = { floor = it }
                )
            }

            if (shownItems.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = if (allItems.isEmpty()) "No items yet.\nTap + to add your first item."
                        else "No items on this floor.",
                        color = Ink,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp)
                    )
                }
            } else {
                items(shownItems, key = { it.id }) { item ->
                    ItemCard(
                        item = item,
                        onDetailsClick = { onDetailsClick(item) },
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
        }

        // Bottom buttons: voice search and add
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CircleIconButton(
                iconRes = R.drawable.ic_mic,
                description = "Voice Search",
                onClick = onVoiceSearchClick,
                size = 54.dp,
                iconPadding = 14.dp,
                background = FabBlack
            )
            CircleIconButton(
                iconRes = android.R.drawable.ic_input_add,
                description = "Add Item",
                onClick = onAddClick,
                size = 54.dp,
                iconPadding = 14.dp,
                background = FabBlack
            )
        }
    }
}

@Composable
private fun TopBar(onSearchClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 6.dp, end = 6.dp, top = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(android.R.drawable.ic_menu_camera),
            contentDescription = "App Logo",
            modifier = Modifier.size(36.dp)
        )
        Text(
            text = "FindIt",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )
        Image(
            painter = painterResource(android.R.drawable.ic_menu_search),
            contentDescription = "Search",
            modifier = Modifier
                .size(28.dp)
                .clickable(onClick = onSearchClick)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Image(
            painter = painterResource(android.R.drawable.ic_menu_myplaces),
            contentDescription = "Profile",
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun HeroCarousel(
    heroItems: List<DashboardItem>,
    onDetailsClick: (DashboardItem) -> Unit
) {
    val pageCount = maxOf(heroItems.size, 1)
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()
    val current = heroItems.getOrNull(pagerState.currentPage)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    PhotoImage(
                        photoPath = heroItems.getOrNull(page)?.photoPath,
                        modifier = Modifier.fillMaxSize(),
                        placeholderBackground = GradientTop,
                        placeholderIconSize = 56.dp
                    )
                }

                // Dark fade at the bottom so the white text can be read on any photo
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(
                            Brush.verticalGradient(listOf(Color.Transparent, Color(0x99000000)))
                        )
                )

                Text(
                    text = "Recently added items",
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp)
                        .background(Color(0x40000000))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 16.dp, end = 64.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(22.dp)
                                .background(Color.White)
                        )
                        Text(
                            text = current?.title ?: "No items yet",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    if (current != null) {
                        Row(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(HeroPill)
                                .clickable { onDetailsClick(current) }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(android.R.drawable.ic_dialog_info),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "See details",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                    }
                }

                // Next arrow
                if (pageCount > 1) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0x40000000))
                            .clickable {
                                scope.launch {
                                    pagerState.animateScrollToPage((pagerState.currentPage + 1) % pageCount)
                                }
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_chevron_right),
                            contentDescription = "Next Item",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // Carousel dots
        if (pageCount > 1) {
            Row(modifier = Modifier.padding(top = 10.dp)) {
                repeat(pageCount) { index ->
                    Box(
                        modifier = Modifier
                            .padding(3.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == pagerState.currentPage) GradientTop
                                else GradientTop.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(selectedFloor: String, onFloorSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Added Items",
            color = Ink,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Box {
            Row(
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DropdownBlue)
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = selectedFloor, color = Ink, fontSize = 14.sp)
                Text(text = "  ▾", color = Ink, fontSize = 14.sp)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                Floors.choices.forEach { choice ->
                    DropdownMenuItem(
                        text = { Text(choice) },
                        onClick = {
                            onFloorSelected(choice)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(6.dp))
}
