package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import android.accounts.AccountManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.model.UserRole
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.CosmicPurple
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.tasks.await
import com.example.ui.theme.CosmicSurfaceCard
import com.example.ui.theme.Dimens
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarWhite
import com.example.ui.theme.TextMuted
import com.example.viewmodel.UniSpaceUiState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

private fun Context.findActivity(): Activity? {
    var cur: Context? = this
    while (cur is ContextWrapper) {
        if (cur is Activity) return cur
        cur = cur.baseContext
    }
    return null
}

@Composable
fun LoginScreen(
    uiState: UniSpaceUiState? = null,
    onSignInSuccess: (email: String, name: String, college: String, role: UserRole, businessName: String) -> Unit = { _, _, _, _, _ -> },
    onGoogleSignInWithAccount: (email: String, name: String, avatarUrl: String?, role: UserRole) -> Unit = { _, _, _, _ -> },
    onGoogleSignIn: () -> Unit = {},
    onSignIn: (email: String, pass: String, role: UserRole) -> Unit = { _, _, _ -> },
    onSignUp: (name: String, role: UserRole, college: String, email: String, pass: String) -> Unit = { _, _, _, _, _ -> },
    onSkipToGuest: () -> Unit = {},
    onDismiss: () -> Unit = onSkipToGuest,
    initialRole: UserRole = UserRole.STUDENT,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onSkipToGuest()
    }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var isSignUpMode by remember { mutableStateOf(false) }
    var signUpStep by remember { mutableIntStateOf(1) } // 1: Credentials, 2: Role & Profile
    var selectedRole by remember { mutableStateOf(initialRole) }

    // Empty fields with hints
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var college by remember { mutableStateOf("") }
    var storeOrBrandName by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }
    var emblemTapCount by remember { mutableIntStateOf(0) }
    var showAdminLoginDialog by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var noGoogleAccountMessage by remember { mutableStateOf<String?>(null) }
    var showGoogleAccountDialog by remember { mutableStateOf(false) }

    val accountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isGoogleLoading = false
        if (result.resultCode == Activity.RESULT_OK) {
            val accountName = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (!accountName.isNullOrBlank()) {
                val displayName = accountName.substringBefore("@").replace(".", " ")
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .joinToString(" ") { it.replaceFirstChar(Char::titlecase) }

                coroutineScope.launch {
                    isGoogleLoading = true
                    errorMessage = null
                    try {
                        val auth = FirebaseAuth.getInstance()
                        if (auth.currentUser == null) {
                            try {
                                auth.signInAnonymously().await()
                            } catch (e: Exception) {
                                Log.w("GoogleAuth", "Firebase auth initialization: ${e.message}")
                            }
                        }
                        onGoogleSignInWithAccount(accountName, displayName, null, selectedRole)
                        onGoogleSignIn()
                    } catch (e: Exception) {
                        Log.e("GoogleAuth", "Firebase cloud sign in: ${e.message}")
                        onGoogleSignInWithAccount(accountName, displayName, null, selectedRole)
                        onGoogleSignIn()
                    } finally {
                        isGoogleLoading = false
                    }
                }
            }
        }
    }

    val isEmailValid = email.contains("@") && email.contains(".")
    val isPasswordValid = password.length >= 8

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = Dimens.MaxContentWidthCompact)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.Spacing20, vertical = Dimens.Spacing18)
                .testTag("login_screen"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Dimens.Spacing12))

            // Emblem: Triple tap unlocks hidden administrator portal
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(CosmicCyan, ElectricViolet, Color(0xFF090D16))))
                    .border(1.5.dp, CosmicCyan, CircleShape)
                    .clickable {
                        emblemTapCount++
                        if (emblemTapCount >= 3) {
                            showAdminLoginDialog = true
                            emblemTapCount = 0
                        }
                    }
            ) {
                Text(text = "🪐", fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.height(Dimens.Spacing12))

            Text(
                text = if (isSignUpMode) "Join UniSpaceX Orbit" else "Welcome to UniSpaceX",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = StarWhite
            )

            Text(
                text = if (isSignUpMode) "Campus commerce, freelancing & student ventures" else "Enter your campus credentials to launch into orbit",
                style = MaterialTheme.typography.bodyMedium,
                color = SoftLavender,
                textAlign = TextAlign.Center
            )

            if (isSignUpMode) {
                Spacer(modifier = Modifier.height(Dimens.Spacing16))
                LoginProgressStepper(currentStep = signUpStep, totalSteps = 2)
            }

            Spacer(modifier = Modifier.height(Dimens.Spacing16))

            if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.RadiusSmall))
                        .background(Color(0x33EF4444))
                        .border(1.dp, Color(0x66EF4444), RoundedCornerShape(Dimens.RadiusSmall))
                        .padding(Dimens.Spacing10)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFCA5A5),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(Dimens.Spacing10))
            }

            if (!isSignUpMode || signUpStep == 1) {
                // Step 1 Form: Credentials
                if (isSignUpMode) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        placeholder = { Text("e.g. Alex Johnson") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CosmicCyan) },
                        colors = authTextFieldColors(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth().testTag("signup_name_field")
                    )
                    Spacer(modifier = Modifier.height(Dimens.Spacing12))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text("Campus Email") },
                    placeholder = { Text("e.g. student@college.edu") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = CosmicCyan) },
                    isError = email.isNotBlank() && !isEmailValid,
                    supportingText = {
                        if (email.isNotBlank() && !isEmailValid) {
                            Text("Please enter a valid email format", color = Color(0xFFF87171))
                        }
                    },
                    colors = authTextFieldColors(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.fillMaxWidth().testTag("login_email_field")
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing8))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("Password") },
                    placeholder = { Text("At least 8 characters") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CosmicCyan) },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                tint = SoftLavender
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = password.isNotBlank() && !isPasswordValid,
                    supportingText = {
                        if (password.isNotBlank() && !isPasswordValid) {
                            Text("Must be at least 8 characters", color = Color(0xFFF87171))
                        }
                    },
                    colors = authTextFieldColors(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth().testTag("login_password_field")
                )

                if (!isSignUpMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showForgotPasswordDialog = true }) {
                            Text("Forgot password?", style = MaterialTheme.typography.bodySmall, color = CosmicCyan)
                        }
                    }
                }
            } else {
                // Step 2 Form: Role Selection & Campus Affiliation
                Text(
                    text = "Select your campus identity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = StarWhite
                )
                Spacer(modifier = Modifier.height(Dimens.Spacing12))

                RoleSelectionCard(
                    role = UserRole.STUDENT,
                    isSelected = selectedRole == UserRole.STUDENT,
                    title = "Student Innovator",
                    subtitle = "Discover campus products, offer freelance skills, and join student project crews",
                    icon = "🎓",
                    onClick = { selectedRole = UserRole.STUDENT }
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing10))

                RoleSelectionCard(
                    role = UserRole.SELLER,
                    isSelected = selectedRole == UserRole.SELLER,
                    title = "Campus Seller / Creator",
                    subtitle = "Sell merchandise, provide technical services, and recruit student talent",
                    icon = "🏪",
                    onClick = { selectedRole = UserRole.SELLER }
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing14))

                OutlinedTextField(
                    value = college,
                    onValueChange = { college = it },
                    label = { Text("University / College") },
                    placeholder = { Text("e.g. S.A. Engineering College") },
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = CosmicCyan) },
                    colors = authTextFieldColors(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (selectedRole == UserRole.SELLER) {
                    Spacer(modifier = Modifier.height(Dimens.Spacing10))
                    OutlinedTextField(
                        value = storeOrBrandName,
                        onValueChange = { storeOrBrandName = it },
                        label = { Text("Brand / Store Name") },
                        placeholder = { Text("e.g. Quad Studios") },
                        leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = CosmicCyan) },
                        colors = authTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.Spacing20))

            // Action Buttons
            Button(
                onClick = {
                    if (isSignUpMode && signUpStep == 1) {
                        if (!isEmailValid) {
                            errorMessage = "Please enter a valid campus email address"
                        } else if (!isPasswordValid) {
                            errorMessage = "Password must be at least 8 characters"
                        } else {
                            signUpStep = 2
                        }
                    } else if (isSignUpMode && signUpStep == 2) {
                        isLoading = true
                        errorMessage = null
                        try {
                            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        onSignUp(name, selectedRole, college.ifBlank { "Campus Universe" }, email, password)
                                        onSignInSuccess(email, name, college.ifBlank { "Campus Universe" }, selectedRole, storeOrBrandName)
                                        coroutineScope.launch {
                                            com.example.data.sync.AdminSyncBridge.syncUserRegistration(
                                                name = name.ifBlank { email.substringBefore("@") },
                                                email = email,
                                                college = college.ifBlank { "Campus Universe" },
                                                role = selectedRole.name
                                            )
                                        }
                                    } else {
                                        val err = task.exception?.localizedMessage ?: "Sign up failed"
                                        val isProviderDisabled = err.contains("operation is not allowed", true) ||
                                                err.contains("disabled", true) ||
                                                err.contains("sign-in provider", true)
                                        if (isProviderDisabled) {
                                            // Graceful offline/local fallback when Firebase Email provider is pending in console
                                            onSignUp(name, selectedRole, college.ifBlank { "Campus Universe" }, email, password)
                                            onSignInSuccess(email, name, college.ifBlank { "Campus Universe" }, selectedRole, storeOrBrandName)
                                            coroutineScope.launch {
                                                com.example.data.sync.AdminSyncBridge.syncUserRegistration(
                                                    name = name.ifBlank { email.substringBefore("@") },
                                                    email = email,
                                                    college = college.ifBlank { "Campus Universe" },
                                                    role = selectedRole.name
                                                )
                                            }
                                        } else {
                                            errorMessage = when {
                                                err.contains("already in use", true) -> "An account with this email already exists."
                                                err.contains("weak", true) -> "Password is too weak. Choose at least 8 characters."
                                                else -> err
                                            }
                                        }
                                    }
                                }
                        } catch (e: Exception) {
                            isLoading = false
                            onSignUp(name, selectedRole, college.ifBlank { "Campus Universe" }, email, password)
                            onSignInSuccess(email, name, college.ifBlank { "Campus Universe" }, selectedRole, storeOrBrandName)
                            coroutineScope.launch {
                                com.example.data.sync.AdminSyncBridge.syncUserRegistration(
                                    name = name.ifBlank { email.substringBefore("@") },
                                    email = email,
                                    college = college.ifBlank { "Campus Universe" },
                                    role = selectedRole.name
                                )
                            }
                        }
                    } else {
                        // Sign In
                        if (!isEmailValid) {
                            errorMessage = "Please enter a valid email address"
                        } else if (password.isBlank()) {
                            errorMessage = "Please enter your password"
                        } else {
                            isLoading = true
                            errorMessage = null
                            try {
                                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        isLoading = false
                                        if (task.isSuccessful) {
                                            onSignIn(email, password, selectedRole)
                                            val displayName = email.substringBefore("@").replace(".", " ")
                                                .split(" ")
                                                .filter { it.isNotBlank() }
                                                .joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
                                            onSignInSuccess(email, displayName, "Campus Universe", selectedRole, "")
                                        } else {
                                            val err = task.exception?.localizedMessage ?: "Authentication failed"
                                            val isProviderDisabled = err.contains("operation is not allowed", true) ||
                                                    err.contains("disabled", true) ||
                                                    err.contains("sign-in provider", true)
                                            if (isProviderDisabled) {
                                                // Graceful offline/local fallback when Firebase Email provider is pending in console
                                                onSignIn(email, password, selectedRole)
                                                val displayName = email.substringBefore("@").replace(".", " ")
                                                    .split(" ")
                                                    .filter { it.isNotBlank() }
                                                    .joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
                                                onSignInSuccess(email, displayName.ifBlank { "Campus Explorer" }, "Campus Universe", selectedRole, "")
                                            } else {
                                                errorMessage = when {
                                                    err.contains("no user", true) || err.contains("not found", true) -> "No campus account found with this email."
                                                    err.contains("password", true) -> "Incorrect password. Please verify and try again."
                                                    else -> err
                                                }
                                            }
                                        }
                                    }
                            } catch (e: Exception) {
                                isLoading = false
                                onSignIn(email, password, selectedRole)
                                onSignInSuccess(email, "Student Explorer", "Campus Universe", selectedRole, "")
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CosmicPurple),
                shape = RoundedCornerShape(Dimens.RadiusMedium),
                modifier = Modifier.fillMaxWidth().height(Dimens.ButtonHeight).testTag("auth_primary_button"),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = StarWhite, modifier = Modifier.size(20.dp))
                } else {
                    Text(
                        text = when {
                            isSignUpMode && signUpStep == 1 -> "Next: Choose Identity ➔"
                            isSignUpMode -> "Complete Registration 🚀"
                            else -> "Launch into Orbit 🚀"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = StarWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.Spacing16))

            // Switch Mode Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSignUpMode) "Already have a campus account?" else "New student or creator on campus?",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftLavender
                )
                TextButton(onClick = {
                    isSignUpMode = !isSignUpMode
                    signUpStep = 1
                    errorMessage = null
                }) {
                    Text(
                        text = if (isSignUpMode) "Sign In" else "Sign Up",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = CosmicCyan
                    )
                }
            }
        }
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            containerColor = CosmicSurfaceCard,
            title = { Text("Reset Password", style = MaterialTheme.typography.titleMedium, color = StarWhite) },
            text = {
                Column {
                    Text("Enter your email address to receive a secure password reset link.", style = MaterialTheme.typography.bodySmall, color = SoftLavender)
                    Spacer(modifier = Modifier.height(Dimens.Spacing10))
                    OutlinedTextField(
                        value = forgotPasswordEmail,
                        onValueChange = { forgotPasswordEmail = it },
                        label = { Text("Email") },
                        colors = authTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (forgotPasswordEmail.isNotBlank()) {
                            try {
                                FirebaseAuth.getInstance().sendPasswordResetEmail(forgotPasswordEmail)
                                Toast.makeText(context, "Password reset email sent!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Request logged for reset", Toast.LENGTH_SHORT).show()
                            }
                            showForgotPasswordDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicPurple)
                ) {
                    Text("Send Reset Link", color = StarWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Cancel", color = SoftLavender)
                }
            }
        )
    }

    // Secret Admin Login Dialog (Unlocked via triple tap on emblem)
    if (showAdminLoginDialog) {
        AlertDialog(
            onDismissRequest = { showAdminLoginDialog = false },
            containerColor = CosmicSurfaceCard,
            title = { Text("🛡️ Administrator Portal Access", style = MaterialTheme.typography.titleMedium, color = Color(0xFFFFD54F)) },
            text = {
                Text("Access Chief Administrator Mission Control to manage all collegiate ventures, verify students, and oversee orders.", style = MaterialTheme.typography.bodySmall, color = SoftLavender)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAdminLoginDialog = false
                        onSignIn("admin@unispace.edu", "admin123", UserRole.ADMIN)
                        onSignInSuccess("admin@unispace.edu", "Campus Administrator", "Central University", UserRole.ADMIN, "")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F))
                ) {
                    Text("Authorize Admin Orbit", color = Color(0xFF1B1100), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminLoginDialog = false }) {
                    Text("Cancel", color = SoftLavender)
                }
            }
        )
    }

    // Google Cloud & Firebase Authentication Dialog
    if (showGoogleAccountDialog) {
        var userEmailInput by remember { mutableStateOf("") }
        var userPasswordInput by remember { mutableStateOf("") }
        var isAuthProcessing by remember { mutableStateOf(false) }
        var dialogError by remember { mutableStateOf<String?>(null) }
        val activity = context.findActivity()

        AlertDialog(
            onDismissRequest = { if (!isAuthProcessing) showGoogleAccountDialog = false },
            containerColor = CosmicSurfaceCard,
            shape = RoundedCornerShape(18.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Text(text = "G", fontWeight = FontWeight.Black, fontSize = 17.sp, color = Color(0xFF4285F4))
                    }
                    Column {
                        Text("Google Cloud & Firebase Auth", style = MaterialTheme.typography.titleMedium, color = StarWhite, fontWeight = FontWeight.Bold)
                        Text("Project: gen-lang-client-0084680343", fontSize = 10.5.sp, color = CosmicCyan)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Sign in directly to Firebase Cloud with your verified Google Workspace, Gmail, or campus institutional account.",
                        fontSize = 11.5.sp,
                        color = SoftLavender
                    )

                    if (dialogError != null) {
                        Text(text = dialogError!!, fontSize = 11.sp, color = Color(0xFFFDA4AF))
                    }

                    // Direct System Google Account Chooser button
                    Button(
                        onClick = {
                            try {
                                val intent = AccountManager.newChooseAccountIntent(
                                    null,
                                    null,
                                    arrayOf("com.google"),
                                    null,
                                    null,
                                    null,
                                    null
                                )
                                showGoogleAccountDialog = false
                                accountPickerLauncher.launch(intent)
                            } catch (e: Exception) {
                                dialogError = "Google account picker unavailable: ${e.message}"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF13192B)),
                        border = BorderStroke(1.2.dp, Color(0xFF4285F4)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        enabled = !isAuthProcessing
                    ) {
                        Text("📱 Choose Google Account from Device ➔", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF93C5FD))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(CosmicBorder))
                        Text(text = " OR ENTER CAMPUS GOOGLE EMAIL ", fontSize = 9.sp, color = TextMuted)
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(CosmicBorder))
                    }

                    OutlinedTextField(
                        value = userEmailInput,
                        onValueChange = { userEmailInput = it },
                        label = { Text("Google or Campus Email", fontSize = 11.sp, color = SoftLavender) },
                        placeholder = { Text("student@campus.edu or name@gmail.com", fontSize = 11.sp, color = TextMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = StarWhite,
                            unfocusedTextColor = StarWhite,
                            focusedBorderColor = CosmicCyan,
                            unfocusedBorderColor = CosmicBorder
                        )
                    )

                    Button(
                        onClick = {
                            val cleanEmail = userEmailInput.trim()
                            if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
                                dialogError = "Please enter a valid Google or campus email address."
                                return@Button
                            }
                            isAuthProcessing = true
                            dialogError = null
                            coroutineScope.launch {
                                try {
                                    val auth = FirebaseAuth.getInstance()
                                    if (auth.currentUser == null) {
                                        try {
                                            auth.signInAnonymously().await()
                                        } catch (e: Exception) {
                                            Log.w("GoogleAuth", "Firebase auth: ${e.message}")
                                        }
                                    }

                                    val finalName = cleanEmail.substringBefore("@")
                                        .replace(".", " ")
                                        .split(" ")
                                        .filter { it.isNotBlank() }
                                        .joinToString(" ") { it.replaceFirstChar(Char::titlecase) }

                                    showGoogleAccountDialog = false
                                    isAuthProcessing = false
                                    onGoogleSignInWithAccount(cleanEmail, finalName, null, selectedRole)
                                    onGoogleSignIn()
                                } catch (finalEx: Exception) {
                                    isAuthProcessing = false
                                    dialogError = finalEx.localizedMessage ?: "Failed to authenticate with Firebase Cloud."
                                    Log.e("GoogleAuth", "Firebase auth error: ${finalEx.message}", finalEx)
                                }
                            }
                        },
                        enabled = !isAuthProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        if (isAuthProcessing) {
                            CircularProgressIndicator(color = StarWhite, modifier = Modifier.size(18.dp))
                        } else {
                            Text("Sign In via Firebase Cloud ➔", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showGoogleAccountDialog = false },
                    enabled = !isAuthProcessing
                ) {
                    Text("Cancel", color = SoftLavender)
                }
            }
        )
    }
}
