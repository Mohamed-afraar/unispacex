package com.example.ui.navigation

enum class NavDestination(val title: String, val route: String) {
    HOME("Home", "/"),
    EXPLORE("Explore", "/explore"),
    BUSINESSES("Businesses", "/businesses"),
    SERVICES("Services", "/services"),
    PRODUCTS("Products", "/products"),
    STUDENTS("Students", "/students"),
    COLLABORATE("Find Crew", "/collaborate"),
    CAMPUS("Campus", "/campus"),
    FEED("Space Feed", "/feed"),
    MESSAGES("Messages", "/messages"),
    MISSION_CONTROL("Mission Control", "/dashboard"),
    PROFILE("Profile", "/profile"),
    SETTINGS("Settings", "/settings"),
    PROFILE_HUB("Profile Hub", "/profile-hub"),
    LOGIN("Student Login", "/login")
}

enum class CreateType(val title: String, val subtitle: String, val icon: String) {
    BUSINESS("Create Business", "Launch your student company or studio", "☄️"),
    PRODUCT("Add Product", "Sell physical goods, prints, apparel & hardware", "🛍"),
    SERVICE("Add Service", "Offer freelancing, tutoring & tech skills", "💼"),
    COLLAB("Find Collaborators", "Post a project bounty or recruit co-founders", "🤝"),
    POST("Create Campus Post", "Share milestones, drops & opportunities", "📡")
}

data class UiNotification(
    val id: String,
    val title: String,
    val description: String,
    val time: String,
    val isRead: Boolean = false
)
