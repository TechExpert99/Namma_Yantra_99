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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nayak.nammayantara.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(onBack: () -> Unit) {
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
                                .background(YantraAmber.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) { Text("📋", fontSize = 16.sp) }
                        Text(
                            "Terms of Service",
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
                color    = YantraAmber.copy(alpha = 0.08f),
                border   = BorderStroke(1.dp, YantraAmber.copy(alpha = 0.25f)),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Namma Yantra — Terms of Service",
                        style = MaterialTheme.typography.titleMedium, color = YantraAmber, fontWeight = FontWeight.Bold)
                    Text("Effective date: May 2026",
                        style = MaterialTheme.typography.labelSmall, color = YantraGrey60)
                    Text("Please read these terms carefully before using the app. By continuing, you accept all terms listed below.",
                        style = MaterialTheme.typography.bodySmall, color = YantraGrey60, lineHeight = 20.sp)
                }
            }

            TermsSection(
                number = "1",
                title  = "Acceptance of Terms",
                body   = "By downloading, accessing, or using the Namma Yantra application, you agree to be bound by these Terms of Service and all applicable laws and regulations. If you do not agree with any of these terms, you are prohibited from using or accessing this app.",
            )

            TermsSection(
                number = "2",
                title  = "Eligibility",
                body   = "You must be at least 18 years of age and a resident of India to use this platform. By creating an account, you represent and warrant that you meet all eligibility requirements. Namma Yantra reserves the right to terminate accounts that do not meet these requirements.",
            )

            TermsSection(
                number = "3",
                title  = "User Accounts",
                body   = "You are responsible for maintaining the confidentiality of your phone number and OTP. You agree to accept responsibility for all activities that occur under your account. Notify us immediately of any unauthorized use of your account. We cannot and will not be liable for any loss or damage arising from your failure to comply with this obligation.",
            )

            TermsSection(
                number = "4",
                title  = "Equipment Listings",
                body   = "Owners are solely responsible for the accuracy, completeness, and legality of their equipment listings, including pricing, availability, and condition. Namma Yantra does not inspect, verify, or guarantee the condition, safety, or legality of any listed equipment. Misleading or fraudulent listings will result in immediate account suspension.",
            )

            TermsSection(
                number = "5",
                title  = "Bookings & Payments",
                body   = "Rental agreements are strictly between the equipment owner and the renter. Namma Yantra facilitates the connection but is not a party to any rental transaction. All pricing, payment, and refund disputes must be resolved directly between the owner and renter. Namma Yantra bears no responsibility for financial transactions conducted outside the platform.",
            )

            TermsSection(
                number = "6",
                title  = "Prohibited Conduct",
                body   = "You agree not to:\n• Post false, inaccurate, or misleading listings\n• Harass, abuse, or harm other users\n• Use the platform for any unlawful purpose\n• Attempt to gain unauthorized access to any part of the service\n• Interfere with or disrupt the integrity of the platform\n\nViolations may result in immediate and permanent account termination.",
            )

            TermsSection(
                number = "7",
                title  = "Intellectual Property",
                body   = "The Namma Yantra name, logo, design, and all content are the property of Team TechExpert99. You may not reproduce, distribute, or create derivative works without explicit written permission.",
            )

            TermsSection(
                number = "8",
                title  = "Disclaimer of Warranties",
                body   = "The service is provided on an \"as is\" and \"as available\" basis without any warranties of any kind, either express or implied. Namma Yantra does not warrant that the service will be uninterrupted, error-free, or free of viruses or other harmful components.",
            )

            TermsSection(
                number = "9",
                title  = "Limitation of Liability",
                body   = "Namma Yantra and Team TechExpert99 shall not be liable for any indirect, incidental, special, or consequential damages arising from the use of or inability to use the service, including damages from equipment accidents, rental disputes, data loss, or service interruptions. Use of the platform is entirely at your own risk.",
            )

            TermsSection(
                number = "10",
                title  = "Termination",
                body   = "We reserve the right to terminate or suspend your account at any time, with or without notice, for conduct that we determine violates these Terms of Service or is harmful to other users, Namma Yantra, or third parties.",
            )

            TermsSection(
                number = "11",
                title  = "Governing Law",
                body   = "These terms shall be governed by and construed in accordance with the laws of India. Any disputes arising under these terms shall be subject to the exclusive jurisdiction of the courts in Karnataka, India.",
            )

            TermsSection(
                number = "12",
                title  = "Changes to Terms",
                body   = "We reserve the right to modify these terms at any time. We will notify users of significant changes via in-app notifications. Your continued use of the application after any changes constitutes your acceptance of the new terms.",
            )

            TermsSection(
                number = "13",
                title  = "Contact Us",
                body   = "If you have any questions about these Terms of Service, please contact us:\n\nEmail: support@nammayantara.in\nTeam: TechExpert99\nLocation: Bengaluru, Karnataka, India",
            )

            // Footer
            LegalPageFooter()
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Shared composables (used by both pages) ───────────────────────────────────

@Composable
internal fun TermsSection(number: String, title: String, body: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        color    = YantraSurface,
        border   = BorderStroke(0.5.dp, YantraGrey30),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Number badge
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(YantraAmber.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(number, style = MaterialTheme.typography.labelMedium,
                        color = YantraAmber, fontWeight = FontWeight.Bold)
                }
                Text(title, style = MaterialTheme.typography.titleMedium,
                    color = YantraWhite, fontWeight = FontWeight.SemiBold)
            }
            Text(body, style = MaterialTheme.typography.bodySmall,
                color = YantraGrey60, lineHeight = 20.sp)
        }
    }
}

@Composable
internal fun LegalPageFooter() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        color    = YantraSurface,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("🚜", fontSize = 24.sp)
            Text("Namma Yantra", style = MaterialTheme.typography.titleMedium,
                color = YantraAmber, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text("Last updated: May 2026",
                style = MaterialTheme.typography.labelSmall, color = YantraGrey60)
            Text("Built with ❤️ by Team TechExpert99 · Bengaluru, India",
                style = MaterialTheme.typography.labelSmall, color = YantraGrey60,
                textAlign = TextAlign.Center)
        }
    }
}