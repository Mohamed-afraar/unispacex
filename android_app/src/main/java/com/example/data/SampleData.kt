package com.example.data

import com.example.model.ApplicationStatus
import com.example.model.Business
import com.example.model.CampusEvent
import com.example.model.CampusFeedPost
import com.example.model.CampusMeetupLocation
import com.example.model.CampusPlanet
import com.example.model.ChatMessage
import com.example.model.CollaborationRequest
import com.example.model.CrewApplication
import com.example.model.FeedCategory
import com.example.model.ItemCondition
import com.example.model.OrderProcess
import com.example.model.OrderProgressStep
import com.example.model.PortfolioItem
import com.example.model.Product
import com.example.model.ReviewItem
import com.example.model.Service
import com.example.model.SkillNode
import com.example.model.Student
import com.example.model.UniverseCategory
import com.example.model.VerificationRequest
import com.example.model.VerificationType

object SampleData {

    val categories = listOf(
        UniverseCategory("Products", "Food, clothing, accessories, handmade, electronics", "products", 0),
        UniverseCategory("Services", "Design, editing, photography, tutoring, programming", "services", 0),
        UniverseCategory("Tech", "Web apps, PCB design, IoT, VLSI & CAD", "tech", 0),
        UniverseCategory("Creative", "Graphic design, video editing, photography, 3D", "creative", 0),
        UniverseCategory("Academic", "Tutoring, code coaching, workshops & viva prep", "academic", 0),
        UniverseCategory("Events", "DJ, photography, stage lighting & audio visual", "events", 0),
        UniverseCategory("Fashion", "Custom T-shirts, oversized hoodies, campus merch", "fashion", 0),
        UniverseCategory("Startups", "Student-run ventures & incubator spinouts", "startups", 0)
    )

    val students: List<Student> = emptyList()

    val businesses: List<Business> = emptyList()

    val products: List<Product> = emptyList()

    val services: List<Service> = emptyList()

    val collaborations: List<CollaborationRequest> = emptyList()

    val campusPlanets = listOf(
        CampusPlanet(
            id = "planet-saec",
            name = "S.A. Engineering College",
            shortName = "SAEC",
            studentCount = 0,
            businessCount = 0,
            serviceCount = 0,
            productCount = 0,
            colorHex = 0xFF6C3BFF,
            accentHex = 0xFF38BDF8,
            description = "Pioneering student hardware innovation, VLSI engineering, and custom electronics ventures.",
            trendingBusinesses = emptyList(),
            popularServices = emptyList()
        ),
        CampusPlanet(
            id = "planet-vit",
            name = "VIT Chennai",
            shortName = "VITC",
            studentCount = 0,
            businessCount = 0,
            serviceCount = 0,
            productCount = 0,
            colorHex = 0xFF38BDF8,
            accentHex = 0xFF22D3EE,
            description = "A thriving hub of student apparel, design studios, and software consultancies.",
            trendingBusinesses = emptyList(),
            popularServices = emptyList()
        ),
        CampusPlanet(
            id = "planet-srm",
            name = "SRM Institute of Science and Technology",
            shortName = "SRM IST",
            studentCount = 0,
            businessCount = 0,
            serviceCount = 0,
            productCount = 0,
            colorHex = 0xFF9B5CFF,
            accentHex = 0xFFC4B5FD,
            description = "Vibrant tech incubators, AI product labs, and active student freelance collectives.",
            trendingBusinesses = emptyList(),
            popularServices = emptyList()
        ),
        CampusPlanet(
            id = "planet-ssn",
            name = "SSN College of Engineering",
            shortName = "SSN",
            studentCount = 0,
            businessCount = 0,
            serviceCount = 0,
            productCount = 0,
            colorHex = 0xFF22D3EE,
            accentHex = 0xFF38BDF8,
            description = "Renowned for creative visual media, symposium production, and robotics fabrication.",
            trendingBusinesses = emptyList(),
            popularServices = emptyList()
        ),
        CampusPlanet(
            id = "planet-saveetha",
            name = "Saveetha Engineering College",
            shortName = "SEC",
            studentCount = 0,
            businessCount = 0,
            serviceCount = 0,
            productCount = 0,
            colorHex = 0xFFEC4899,
            accentHex = 0xFFF43F5E,
            description = "Innovative food artisans, lifestyle accessories, and event design initiatives.",
            trendingBusinesses = emptyList(),
            popularServices = emptyList()
        ),
        CampusPlanet(
            id = "planet-rec",
            name = "Rajalakshmi Engineering College",
            shortName = "REC",
            studentCount = 0,
            businessCount = 0,
            serviceCount = 0,
            productCount = 0,
            colorHex = 0xFF10B981,
            accentHex = 0xFF22D3EE,
            description = "Specialized automotive EV teams, drone prototyping, and telemetry hardware.",
            trendingBusinesses = emptyList(),
            popularServices = emptyList()
        )
    )

    val campusFeedPosts: List<CampusFeedPost> = emptyList()

    val sampleChatConversations: Map<String, List<ChatMessage>> = emptyMap()

    val sampleChatMessages: List<ChatMessage> = emptyList()

    val sampleOrders: List<OrderProcess> = emptyList()

    val sampleMeetupLocations = listOf(
        CampusMeetupLocation("loc-1", "Central Library Foyer", "Ground floor reading room entrance & reception", "📚"),
        CampusMeetupLocation("loc-2", "Main Student Food Court", "Central canteen juice counter & outdoor seating", "☕"),
        CampusMeetupLocation("loc-3", "Hostel Block Gate", "Main hostel security gate & visitor lounge", "🏢"),
        CampusMeetupLocation("loc-4", "Tech Park Quadrangle", "Center fountain lawn opposite Innovation Hub", "🏛️"),
        CampusMeetupLocation("loc-5", "Student Activity Center (SAC)", "SAC porch & indoor sports breezeway", "🎯")
    )

    val sampleEvents: List<CampusEvent> = emptyList()

    val sampleCrewApplications: List<CrewApplication> = emptyList()

    val sampleVerificationRequests: List<VerificationRequest> = emptyList()
}
