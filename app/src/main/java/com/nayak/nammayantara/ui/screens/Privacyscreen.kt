package com.nayak.nammayantara.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nayak.nammayantara.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = YantraAsphalt,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(YantraTeal.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) { Text("🔒", fontSize = 16.sp) }
                        Text(
                            "Privacy Policy",
                            style      = MaterialTheme.typography.titleLarge,
                            color      = YantraWhite,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = YantraAmber)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = YantraSurface),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {

            // Intro banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                color    = YantraTeal.copy(alpha = 0.08f),
                border   = BorderStroke(1.dp, YantraTeal.copy(alpha = 0.25f)),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Namma Yantra — Privacy Policy",
                        style = MaterialTheme.typography.titleMedium, color = YantraTeal, fontWeight = FontWeight.Bold)
                    Text("Effective date: May 2026",
                        style = MaterialTheme.typography.labelSmall, color = YantraGrey60)
                    Text("Your privacy matters to us. This policy explains what data we collect, how we use it, and your rights as a user of Namma Yantra.",
                        style = MaterialTheme.typography.bodySmall, color = YantraGrey60, lineHeight = 20.sp)
                }
            }

            PrivacySection(
                number = "1",
                title  = "Information We Collect",
                body   = "We collect the following types of information when you use Namma Yantra:\n\n• Phone number — used for OTP-based authentication via Firebase\n• Profile details — name, birth date, gender, and address that you voluntarily provide\n• Profile photo — stored as Base64 encoded data in Firestore\n• Equipment details — listings, location, and pricing provided by owners\n• Device token — FCM token used for push notifications",
            )

            PrivacySection(
                number = "2",
                title  = "How We Use Your Information",
                body   = "Your information is used to:\n\n• Authenticate your identity and secure your account\n• Display your profile to other users for rental purposes\n• Show equipment listings relevant to your location\n• Send booking request and status notifications via Firebase Cloud Messaging\n• Improve the platform and fix technical issues",
            )

            PrivacySection(
                number = "3",
                title  = "Data Storage & Security",
                body   = "All user data is stored securely on Google Firebase infrastructure, which includes Firestore (database) and Firebase Authentication. We apply Firestore security rules to ensure users can only access their own data. We do not store payment information — all financial transactions are handled directly between users.",
            )

            PrivacySection(
                number = "4",
                title  = "Location Data",
                body   = "Location access is requested for the following purposes:\n\n• To display equipment near you on the map as a renter\n• To tag the pickup location of your equipment as an owner\n\nWe do not collect or track your location in the background. Location data is used only at the moment you request it within the app.",
            )

            PrivacySection(
                number = "5",
                title  = "Data Sharing",
                body   = "We do not sell, trade, or rent your personal data to third parties.\n\n• Your phone number is masked in public reviews (e.g., +91 98765•••••)\n• Equipment owners can see renter contact details only for confirmed bookings\n• We may share data with Google Firebase as our infrastructure provider, subject to Google's privacy policy",
            )

            PrivacySection(
                number = "6",
                title  = "Push Notifications",
                body   = "We use Firebase Cloud Messaging (FCM) to send:\n\n• New booking request alerts (for owners)\n• Booking acceptance or decline updates (for renters)\n• General service announcements\n\nYou may disable push notifications at any time through your device's notification settings. This will not affect your ability to use the app.",
            )

            PrivacySection(
                number = "7",
                title  = "Cookies & Analytics",
                body   = "Namma Yantra does not use browser cookies. We may use anonymised Firebase Analytics data to understand how users interact with the app and to improve user experience. This data does not identify individual users.",
            )

            PrivacySection(
                number = "8",
                title  = "Data Retention",
                body   = "Your personal data is retained for as long as your account remains active. If you wish to delete your account and all associated data, please contact us at the email below. We will process your request within 30 days.",
            )

            PrivacySection(
                number = "9",
                title  = "Your Rights",
                body   = "As a user of Namma Yantra, you have the right to:\n\n• Access the personal data we hold about you\n• Correct inaccurate profile information via Edit Profile\n• Request deletion of your account and data\n• Withdraw consent for notifications at any time\n\nTo exercise any of these rights, contact us at the email below.",
            )

            PrivacySection(
                number = "10",
                title  = "Children's Privacy",
                body   = "Namma Yantra is not intended for users under the age of 18. We do not knowingly collect personal data from minors. If we become aware that a minor has provided us with personal data, we will delete it immediately.",
            )

            PrivacySection(
                number = "11",
                title  = "Third-Party Services",
                body   = "Our app uses the following third-party services, each governed by their own privacy policies:\n\n• Google Firebase — Authentication, Firestore, FCM\n• Google Maps — Equipment map and location picker\n• Google Gemini AI — AI farming assistant chat feature",
            )

            PrivacySection(
                number = "12",
                title  = "Changes to This Policy",
                body   = "We may update this Privacy Policy from time to time. We will notify you of significant changes via in-app notification. Your continued use of the app after changes are posted constitutes your acceptance of the updated policy.",
            )

            PrivacySection(
                number = "13",
                title  = "Contact Us",
                body   = "For any privacy-related questions, data requests, or concerns:\n\nEmail: privacy@nammayantara.in\nTeam: TechExpert99\nLocation: Bengaluru, Karnataka, India",
            )

            // Footer
            LegalPageFooter()
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PrivacySection(number: String, title: String, body: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        color    = YantraSurface,
        border   = BorderStroke(0.5.dp, YantraGrey30),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Number badge — teal for privacy (vs amber for terms)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(YantraTeal.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(number, style = MaterialTheme.typography.labelMedium,
                        color = YantraTeal, fontWeight = FontWeight.Bold)
                }
                Text(title, style = MaterialTheme.typography.titleMedium,
                    color = YantraWhite, fontWeight = FontWeight.SemiBold)
            }
            Text(body, style = MaterialTheme.typography.bodySmall,
                color = YantraGrey60, lineHeight = 20.sp)
        }
    }
}