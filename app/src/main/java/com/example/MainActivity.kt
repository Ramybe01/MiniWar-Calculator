package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MiniWarCalculatorScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniWarCalculatorScreen() {
    val context = LocalContext.current
    var selectedItem by remember { mutableStateOf(MiniWarData.items[0]) }
    var quantityText by remember { mutableStateOf("1000") }
    var profitRateText by remember { mutableStateOf("340") } // Varsayılan: 340
    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCatalogSheet by remember { mutableStateOf(false) }
    val historyList = remember { mutableStateListOf<CalculationResult>() }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun performCalculation() {
        val cleanQty = quantityText.replace(".", "").replace(",", "").trim()
        val cleanProfit = profitRateText.replace("%", "").replace(",", ".").trim()

        val qty = try {
            BigDecimal(cleanQty).takeIf { it > BigDecimal.ZERO }
                ?: throw IllegalArgumentException("Miktar 0'dan büyük olmalıdır.")
        } catch (e: Exception) {
            errorMessage = "Lütfen geçerli bir adet (sayı) giriniz."
            return
        }

        val profitRate = try {
            BigDecimal(cleanProfit).takeIf { it >= BigDecimal.ZERO }
                ?: throw IllegalArgumentException("Kâr oranı negatif olamaz.")
        } catch (e: Exception) {
            errorMessage = "Lütfen geçerli bir % kâr oranı giriniz."
            return
        }

        errorMessage = null

        // Matematiksel Formül:
        // Toplam Kazanç = Adet * (Temel Fiyat * (1 + (Kâr Oranı / 100)))
        val basePrice = BigDecimal(selectedItem.basePrice)
        val profitMultiplier = BigDecimal.ONE.add(profitRate.divide(BigDecimal(100), 10, RoundingMode.HALF_UP))
        val unitFinalPrice = basePrice.multiply(profitMultiplier)
        val totalEarnings = qty.multiply(unitFinalPrice).setScale(2, RoundingMode.HALF_UP)
        val baseEarnings = qty.multiply(basePrice)
        val extraProfitEarnings = totalEarnings.subtract(baseEarnings)

        val result = CalculationResult(
            item = selectedItem,
            quantity = qty,
            profitRate = profitRate,
            unitFinalPrice = unitFinalPrice,
            totalEarnings = totalEarnings,
            baseEarnings = baseEarnings,
            extraProfitEarnings = extraProfitEarnings
        )
        calculationResult = result
        historyList.add(0, result)
        if (historyList.size > 10) historyList.removeLast()
    }

    // İlk açılışta varsayılan hesaplamayı yap
    remember {
        performCalculation()
        true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⚔️ MINIWAR",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = NeonGold,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "Pazar",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showCatalogSheet = true },
                        modifier = Modifier.testTag("catalog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ListAlt,
                            contentDescription = "Fiyat Listesi",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. EŞYA SEÇİMİ (Açılır Liste / Dropdown)
            ItemSelectionCard(
                selectedItem = selectedItem,
                onItemSelected = {
                    selectedItem = it
                    performCalculation()
                }
            )

            // 2. GİRDİ FORMU (Adet ve Kâr Oranı)
            InputCard(
                quantity = quantityText,
                onQuantityChange = { quantityText = it },
                profitRate = profitRateText,
                onProfitRateChange = { profitRateText = it },
                onAddQuantity = { addVal ->
                    val currentVal = quantityText.replace(".", "").replace(",", "").toLongOrNull() ?: 0L
                    quantityText = (currentVal + addVal).toString()
                },
                onQuickProfit = { rate ->
                    profitRateText = rate.toString()
                }
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // 3. HESAPLA BUTONU
            Button(
                onClick = { performCalculation() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("calculate_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TOPLAM KAZANCI HESAPLA",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    letterSpacing = 0.5.sp
                )
            }

            // 4. SONUÇ GÖSTERİM KARTI (Büyük ve Kalın Yazı ile)
            calculationResult?.let { result ->
                ResultCard(
                    result = result,
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(
                            "MiniWar Kazancı",
                            result.totalEarnings.toPlainString()
                        )
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Kazanç panoya kopyalandı!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // 5. SON İŞLEMLER GEÇMİŞİ
            if (historyList.size > 1) {
                HistorySection(
                    history = historyList,
                    onSelectHistory = { hist ->
                        selectedItem = hist.item
                        quantityText = hist.quantity.toPlainString()
                        profitRateText = hist.profitRate.toPlainString()
                        calculationResult = hist
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Eşya Fiyat Kataloğu Alt Sayfası (Bottom Sheet)
    if (showCatalogSheet) {
        CatalogBottomSheet(
            sheetState = sheetState,
            onDismiss = { showCatalogSheet = false },
            onItemSelect = { item ->
                selectedItem = item
                showCatalogSheet = false
                performCalculation()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemSelectionCard(
    selectedItem: MiniWarItem,
    onItemSelected: (MiniWarItem) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "EŞYA SEÇİMİ",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.testTag("item_dropdown")
            ) {
                OutlinedTextField(
                    value = "${selectedItem.iconEmoji} ${selectedItem.name} (${MiniWarData.formatNumber(BigDecimal(selectedItem.basePrice))} Coins)",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .testTag("item_select_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    MiniWarData.items.forEach { item ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = item.iconEmoji, fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = item.name,
                                            fontWeight = if (item.name == selectedItem.name) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                    Text(
                                        text = "${MiniWarData.formatNumber(BigDecimal(item.basePrice))} C",
                                        fontWeight = FontWeight.SemiBold,
                                        color = NeonGold,
                                        fontSize = 13.sp
                                    )
                                }
                            },
                            onClick = {
                                onItemSelected(item)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Kategori: ${selectedItem.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Temel Fiyat: ${MiniWarData.formatNumber(BigDecimal(selectedItem.basePrice))} Coins",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            }
        }
    }
}

@Composable
fun InputCard(
    quantity: String,
    onQuantityChange: (String) -> Unit,
    profitRate: String,
    onProfitRateChange: (String) -> Unit,
    onAddQuantity: (Long) -> Unit,
    onQuickProfit: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ADET GİRDİSİ
            Text(
                text = "SATILACAK TOPLAM ADET",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = quantity,
                onValueChange = onQuantityChange,
                placeholder = { Text("Örn: 1000, 50000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quantity_input"),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    if (quantity.isNotEmpty()) {
                        IconButton(onClick = { onQuantityChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Temizle")
                        }
                    }
                }
            )

            // Hızlı Adet Artırma Butonları
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(100L to "+100", 1_000L to "+1K", 10_000L to "+10K", 100_000L to "+100K", 1_000_000L to "+1M").forEach { (v, label) ->
                    FilterChip(
                        selected = false,
                        onClick = { onAddQuantity(v) },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))

            // % KÂR ORANI GİRDİSİ (Varsayılan 340)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "% KÂR ORANI (ETKİNLİK DALGALANMASI)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Varsayılan: 340",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = profitRate,
                onValueChange = onProfitRateChange,
                placeholder = { Text("340") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = NeonCyan
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profit_rate_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // Hızlı Kâr Oranı Seçenekleri
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(0, 100, 200, 340, 500, 1000).forEach { rate ->
                    val isSelected = profitRate == rate.toString()
                    FilterChip(
                        selected = isSelected,
                        onClick = { onQuickProfit(rate) },
                        label = {
                            Text(
                                text = if (rate == 340) "340% (Oyun)" else "$rate%",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ResultCard(
    result: CalculationResult,
    onCopy: () -> Unit
) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(listOf(NeonGold, MaterialTheme.colorScheme.secondary)),
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("result_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(gradientBrush)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TOPLAM TAHMİNİ KAZANÇ",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sonuç Büyük ve Kalın Fontla
            val formattedTotal = MiniWarData.formatNumber(result.totalEarnings)
            Text(
                text = "$formattedTotal Coins",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = NeonGold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("total_earnings_text")
            )

            val scaleDesc = MiniWarData.getScaleDescription(result.totalEarnings)
            if (scaleDesc.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = scaleDesc,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            // Detay Dağılımı
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailRow(
                    label = "Birim Satış Fiyatı:",
                    value = "${MiniWarData.formatNumber(result.unitFinalPrice)} Coins"
                )
                DetailRow(
                    label = "Etkinliksiz Temel Kazanç:",
                    value = "${MiniWarData.formatNumber(result.baseEarnings)} Coins"
                )
                DetailRow(
                    label = "Kâr Bonusu Katkısı (%${result.profitRate}):",
                    value = "+${MiniWarData.formatNumber(result.extraProfitEarnings)} Coins",
                    valueColor = NeonCyan
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onCopy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("copy_result_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Kopyala",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sonucu Panoya Kopyala",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
fun HistorySection(
    history: List<CalculationResult>,
    onSelectHistory: (CalculationResult) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SON HESAPLAMALAR",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            history.take(4).forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectHistory(item) }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${item.item.iconEmoji} ${item.item.name} x${MiniWarData.formatNumber(item.quantity)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "%${item.profitRate} Kâr Oranı",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${MiniWarData.formatNumber(item.totalEarnings)} C",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeonGold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogBottomSheet(
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onItemSelect: (MiniWarItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredItems = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            MiniWarData.items
        } else {
            MiniWarData.items.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "MiniWar Eşya Fiyat Kataloğu",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = NeonGold
            )
            Text(
                text = "Oyundaki tüm 25 temel pazar eşyası ve baz fiyatları",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Eşya adı veya kategori ara...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredItems) { item ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onItemSelect(item) },
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = item.iconEmoji, fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = item.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = item.category,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "${MiniWarData.formatNumber(BigDecimal(item.basePrice))} Coins",
                                fontWeight = FontWeight.Bold,
                                color = NeonGold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}
