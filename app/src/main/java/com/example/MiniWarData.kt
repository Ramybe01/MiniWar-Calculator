package com.example

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

data class MiniWarItem(
    val name: String,
    val basePrice: Long,
    val category: String,
    val iconEmoji: String
)

object MiniWarData {
    val items = listOf(
        MiniWarItem("Buğday", 14L, "Tarım", "🌾"),
        MiniWarItem("Mısır", 18L, "Tarım", "🌽"),
        MiniWarItem("Kömür", 27L, "Madencilik", "🪨"),
        MiniWarItem("Ahşap", 38L, "Temel", "🪵"),
        MiniWarItem("Un", 49L, "İşlenmiş", "🍞"),
        MiniWarItem("Havuçlar", 67L, "Tarım", "🥕"),
        MiniWarItem("Kitaplar", 94L, "Bilgi", "📚"),
        MiniWarItem("Demir", 120L, "Madencilik", "⚙️"),
        MiniWarItem("Yağ", 120L, "Sanayi", "🛢️"),
        MiniWarItem("Beton", 170L, "İnşaat", "🧱"),
        MiniWarItem("Altın", 260L, "Değerli Maden", "🪙"),
        MiniWarItem("Para Çantası", 410L, "Finans", "💰"),
        MiniWarItem("Araştırma", 560L, "Bilim", "🔬"),
        MiniWarItem("Elmaslar", 840L, "Değerli Maden", "💎"),
        MiniWarItem("Uran Cevheri", 1330L, "Nükleer", "☢️"),
        MiniWarItem("Sabit Uran", 2100L, "Nükleer", "🧪"),
        MiniWarItem("Robo Kafası", 2500L, "Sibernetik", "🤖"),
        MiniWarItem("Veri Küpü", 3000L, "Teknoloji", "🧊"),
        MiniWarItem("Karanlık Madde", 5000L, "Kozmik", "🌌"),
        MiniWarItem("Uzaylı Özü", 20000L, "Kozmik", "🛸"),
        MiniWarItem("Anti madde", 40000L, "Kuantum", "⚛️"),
        MiniWarItem("Kuantum Çekirdeği", 90000L, "Kuantum", "🔮"),
        MiniWarItem("Süpernova Şarjı", 225000L, "Astrofizik", "💥"),
        MiniWarItem("Gamma Işını", 450000L, "Kozmik Işın", "⚡"),
        MiniWarItem("Anomali Çekirdeği", 675000L, "Efsanevi", "🌀")
    )

    val itemsMap: Map<String, Long> = items.associate { it.name to it.basePrice }

    fun formatNumber(value: BigDecimal): String {
        val symbols = DecimalFormatSymbols(Locale("tr", "TR")).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val isWhole = value.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0
        val pattern = if (isWhole) "#,##0" else "#,##0.##"
        val formatter = DecimalFormat(pattern, symbols)
        return formatter.format(value)
    }

    fun getScaleDescription(value: BigDecimal): String {
        val doubleVal = try {
            value.toDouble()
        } catch (e: Exception) {
            Double.POSITIVE_INFINITY
        }

        return when {
            doubleVal >= 1e24 -> "≈ ${(doubleVal / 1e24).formatCompact()} Septilyon"
            doubleVal >= 1e21 -> "≈ ${(doubleVal / 1e21).formatCompact()} Seksilyon"
            doubleVal >= 1e18 -> "≈ ${(doubleVal / 1e18).formatCompact()} Kentilyon"
            doubleVal >= 1e15 -> "≈ ${(doubleVal / 1e15).formatCompact()} Katrilyon"
            doubleVal >= 1e12 -> "≈ ${(doubleVal / 1e12).formatCompact()} Trilyon"
            doubleVal >= 1e9 -> "≈ ${(doubleVal / 1e9).formatCompact()} Milyar"
            doubleVal >= 1e6 -> "≈ ${(doubleVal / 1e6).formatCompact()} Milyon"
            doubleVal >= 1e3 -> "≈ ${(doubleVal / 1e3).formatCompact()} Bin"
            else -> ""
        }
    }

    private fun Double.formatCompact(): String {
        return String.format(Locale("tr", "TR"), "%.2f", this)
    }
}

data class CalculationResult(
    val item: MiniWarItem,
    val quantity: BigDecimal,
    val profitRate: BigDecimal,
    val unitFinalPrice: BigDecimal,
    val totalEarnings: BigDecimal,
    val baseEarnings: BigDecimal,
    val extraProfitEarnings: BigDecimal,
    val timestamp: Long = System.currentTimeMillis()
)
