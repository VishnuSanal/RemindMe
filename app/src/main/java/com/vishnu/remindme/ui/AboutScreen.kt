package com.vishnu.remindme.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import com.vishnu.remindme.BuildConfig
import com.vishnu.remindme.Constants
import com.vishnu.remindme.R

/** A single tappable link row inside a grouped section card. */
private data class AboutLink(
    @DrawableRes val icon: Int,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavHostController) {
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                title = {
                    Text(text = stringResource(R.string.about))
                }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AboutHeader()

            PrivacyCard()

            SectionLabel(stringResource(R.string.section_support))

            DonateCard(onClick = { openUrl(context, Constants.ABOUT_URL_BUYMEACOFFEE) })

            LinkGroup(
                AboutLink(
                    icon = R.drawable.ic_star,
                    title = stringResource(R.string.rate_title),
                    subtitle = stringResource(R.string.rate_subtitle),
                    onClick = { openUrl(context, Constants.ABOUT_URL_PLAY_STORE) }
                ),
                AboutLink(
                    icon = R.drawable.ic_share,
                    title = stringResource(R.string.share_title),
                    subtitle = stringResource(R.string.share_subtitle),
                    onClick = { shareApp(context) }
                )
            )

            SectionLabel(stringResource(R.string.section_project))

            LinkGroup(
                AboutLink(
                    icon = R.drawable.ic_code,
                    title = stringResource(R.string.source_title),
                    subtitle = stringResource(R.string.source_subtitle),
                    onClick = { openUrl(context, Constants.ABOUT_URL_GITHUB) }
                ),
                AboutLink(
                    icon = R.drawable.ic_bug,
                    title = stringResource(R.string.bug_title),
                    subtitle = stringResource(R.string.bug_subtitle),
                    onClick = { openUrl(context, Constants.ABOUT_URL_ISSUES) }
                ),
                AboutLink(
                    icon = R.drawable.ic_history,
                    title = stringResource(R.string.releases_title),
                    subtitle = stringResource(R.string.releases_subtitle),
                    onClick = { openUrl(context, Constants.ABOUT_URL_RELEASES) }
                )
            )

            SectionLabel(stringResource(R.string.section_community))

            LinkGroup(
                AboutLink(
                    icon = R.drawable.ic_forum,
                    title = stringResource(R.string.telegram_title),
                    subtitle = stringResource(R.string.telegram_subtitle),
                    onClick = { openUrl(context, Constants.ABOUT_URL_TELEGRAM) }
                )
            )

            SectionLabel(stringResource(R.string.section_legal))

            LinkGroup(
                AboutLink(
                    icon = R.drawable.ic_license,
                    title = stringResource(R.string.license_title),
                    subtitle = stringResource(R.string.license_subtitle),
                    onClick = { openUrl(context, Constants.ABOUT_URL_LICENSE) }
                )
            )

            Spacer(Modifier.height(24.dp))

            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(R.string.about_footer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AboutHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.28f)
                .aspectRatio(1f)
                .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .aspectRatio(1f),
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = stringResource(R.string.app_name),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Text(
            modifier = Modifier.padding(top = 12.dp),
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            modifier = Modifier.padding(top = 2.dp, start = 24.dp, end = 24.dp),
            text = stringResource(R.string.about_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Text(
            modifier = Modifier.padding(top = 6.dp),
            text = BuildConfig.VERSION_NAME,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PrivacyCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_shield),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(36.dp)
            )

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    text = stringResource(R.string.about_privacy_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    modifier = Modifier.padding(top = 2.dp),
                    text = stringResource(R.string.about_privacy_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

/** The single accented "hero" action on the screen — a gentle primaryContainer, not full primary. */
@Composable
private fun DonateCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_favorite),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp)
            )

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.support_coffee_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    modifier = Modifier.padding(top = 2.dp),
                    text = stringResource(R.string.support_coffee_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Icon(
                painter = painterResource(R.drawable.ic_open_in_new),
                contentDescription = stringResource(R.string.cd_open_external),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 6.dp),
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

/** Wraps related rows in one neutral M3 grouped-list card with hairline dividers between them. */
@Composable
private fun LinkGroup(vararg links: AboutLink) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column {
            links.forEachIndexed { index, link ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 72.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                LinkRow(link)
            }
        }
    }
}

@Composable
private fun LinkRow(link: AboutLink) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = link.onClick)
            .heightIn(min = 64.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(link.icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = link.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                modifier = Modifier.padding(top = 1.dp),
                text = link.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Opens a web link in the user's browser (or whichever app handles it, e.g. the Play Store).
 * A missing handler app is the only failure mode we guard against.
 */
fun openUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
        Toast.makeText(context, context.getString(R.string.link_open_failed), Toast.LENGTH_SHORT)
            .show()
    }
}

/** Fires the system share sheet with a pre-filled message pointing at the Play Store listing. */
fun shareApp(context: Context) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_subject))
        putExtra(
            Intent.EXTRA_TEXT,
            context.getString(R.string.share_text, Constants.ABOUT_URL_PLAY_STORE)
        )
    }
    try {
        context.startActivity(
            Intent.createChooser(intent, context.getString(R.string.share_title))
        )
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
        Toast.makeText(context, context.getString(R.string.link_open_failed), Toast.LENGTH_SHORT)
            .show()
    }
}
