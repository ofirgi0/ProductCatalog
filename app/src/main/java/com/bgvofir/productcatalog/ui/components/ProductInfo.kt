package com.bgvofir.productcatalog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.bgvofir.productcatalog.R
import com.bgvofir.productcatalog.ui.theme.starAmber
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

// dummyjson prices are in US dollars.
fun formatPrice(price: Double): String = NumberFormat.getCurrencyInstance(Locale.US).format(price)

@Composable
fun DiscountTag(discountPercentage: Double, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.discount_percent, discountPercentage.roundToInt()),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.error,
        modifier = modifier
            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp),
    )
}

@Composable
fun Rating(rating: Double, modifier: Modifier = Modifier) {
    val value = String.format(Locale.US, "%.2f", rating)
    val description = stringResource(R.string.rating_content_description, value)
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.starAmber,
            modifier = Modifier.size(16.dp),
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
