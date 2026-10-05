const { PrismaClient } = require("@prisma/client");
const bcrypt = require("bcryptjs");
const fs = require("fs");
const path = require("path");

const prisma = new PrismaClient();

function generateSampleIdSvg(name, college, idNumber, type = "STUDENT ID") {
  return `<svg xmlns="http://www.w3.org/2000/svg" width="500" height="300" viewBox="0 0 500 300">
    <defs>
      <linearGradient id="bg" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stop-color="#0f172a" />
        <stop offset="100%" stop-color="#1e293b" />
      </linearGradient>
      <linearGradient id="accent" x1="0%" y1="0%" x2="100%" y2="0%">
        <stop offset="0%" stop-color="#3b82f6" />
        <stop offset="100%" stop-color="#8b5cf6" />
      </linearGradient>
    </defs>
    <rect width="500" height="300" rx="16" fill="url(#bg)" stroke="#334155" stroke-width="2"/>
    <rect x="0" y="0" width="500" height="12" fill="url(#accent)"/>
    <text x="30" y="45" font-family="system-ui, sans-serif" font-size="13" font-weight="bold" fill="#38bdf8" letter-spacing="2">UNISPACE VERIFIED VAULT</text>
    <text x="470" y="45" font-family="system-ui, sans-serif" font-size="12" font-weight="bold" fill="#94a3b8" text-anchor="end">${type}</text>
    <line x1="30" y1="60" x2="470" y2="60" stroke="#334155" stroke-width="1"/>
    
    <rect x="30" y="80" width="100" height="130" rx="8" fill="#1e293b" stroke="#475569" stroke-width="1"/>
    <circle cx="80" cy="125" r="28" fill="#334155"/>
    <path d="M52 185 C52 155, 108 155, 108 185 Z" fill="#334155"/>
    <text x="80" y="200" font-family="system-ui, sans-serif" font-size="10" fill="#64748b" text-anchor="middle">PHOTO ID</text>
    
    <text x="150" y="100" font-family="system-ui, sans-serif" font-size="11" fill="#94a3b8" text-transform="uppercase">Full Legal Name</text>
    <text x="150" y="122" font-family="system-ui, sans-serif" font-size="18" font-weight="bold" fill="#f8fafc">${name}</text>
    
    <text x="150" y="150" font-family="system-ui, sans-serif" font-size="11" fill="#94a3b8" text-transform="uppercase">Affiliated Institution</text>
    <text x="150" y="170" font-family="system-ui, sans-serif" font-size="14" font-weight="600" fill="#38bdf8">${college}</text>
    
    <text x="150" y="196" font-family="system-ui, sans-serif" font-size="11" fill="#94a3b8" text-transform="uppercase">Document / Roll Number</text>
    <text x="150" y="216" font-family="system-ui, monospace" font-size="14" font-weight="bold" fill="#e2e8f0">${idNumber}</text>
    
    <rect x="30" y="235" width="440" height="42" rx="6" fill="#0b1120" stroke="#1e293b"/>
    <text x="45" y="260" font-family="system-ui, sans-serif" font-size="11" fill="#10b981" font-weight="bold">● SECURE GOVERNMENT &amp; CAMPUS ARCHIVE</text>
    <text x="455" y="260" font-family="system-ui, sans-serif" font-size="11" fill="#64748b" text-anchor="end">EXP: 2027-06-30</text>
  </svg>`;
}

function writeVaultDoc(docId, name, college, idNumber, type) {
  const vaultDir = path.join(process.cwd(), "storage", "private_vault");
  if (!fs.existsSync(vaultDir)) fs.mkdirSync(vaultDir, { recursive: true });

  const svgContent = generateSampleIdSvg(name, college, idNumber, type);
  fs.writeFileSync(path.join(vaultDir, `${docId}.svg`), svgContent, "utf-8");
  fs.writeFileSync(
    path.join(vaultDir, `${docId}.meta.json`),
    JSON.stringify({
      docId,
      originalFilename: `${docId}.svg`,
      mimeType: "image/svg+xml",
      sizeBytes: Buffer.byteLength(svgContent),
      docType: type.includes("GOVT") ? "govt_id" : "student_id",
      storedAt: new Date().toISOString(),
    }, null, 2),
    "utf-8"
  );
}

async function main() {
  console.log("🌱 Seeding UNISpaceX Database...");

  // Clean existing data in logical reverse order
  await prisma.auditLog.deleteMany({});
  await prisma.notification.deleteMany({});
  await prisma.report.deleteMany({});
  await prisma.favorite.deleteMany({});
  await prisma.productImage.deleteMany({});
  await prisma.product.deleteMany({});
  await prisma.sellerProfile.deleteMany({});
  await prisma.sellerApplication.deleteMany({});
  await prisma.studentVerification.deleteMany({});
  await prisma.user.deleteMany({});
  await prisma.category.deleteMany({});
  await prisma.college.deleteMany({});
  await prisma.announcement.deleteMany({});

  // 1. Create Colleges
  console.log("Creating colleges...");
  const stanford = await prisma.college.create({
    data: {
      name: "Stanford University",
      domain: "stanford.edu",
      city: "Stanford",
      state: "CA",
      country: "USA",
      isActive: true,
    },
  });

  const mit = await prisma.college.create({
    data: {
      name: "Massachusetts Institute of Technology",
      domain: "mit.edu",
      city: "Cambridge",
      state: "MA",
      country: "USA",
      isActive: true,
    },
  });

  const berkeley = await prisma.college.create({
    data: {
      name: "University of California, Berkeley",
      domain: "berkeley.edu",
      city: "Berkeley",
      state: "CA",
      country: "USA",
      isActive: true,
    },
  });

  const saec = await prisma.college.create({
    data: {
      name: "S.A. Engineering College",
      domain: "saec.ac.in",
      city: "Chennai",
      state: "TN",
      country: "India",
      isActive: true,
    },
  });

  // 2. Create Categories
  console.log("Creating categories...");
  const categoriesData = [
    { name: "Electronics", slug: "electronics", icon: "laptop", description: "Laptops, monitors, calculators, tablets & cables" },
    { name: "Books & Textbooks", slug: "books", icon: "book-open", description: "Course textbooks, engineering volumes & test preps" },
    { name: "Study Notes", slug: "notes", icon: "file-text", description: "Curated lecture summaries, cheat sheets & exam solutions" },
    { name: "Campus Fashion", slug: "fashion", icon: "shirt", description: "College hoodies, tees, boots & vintage campus apparel" },
    { name: "Accessories", slug: "accessories", icon: "watch", description: "Backpacks, wireless earbuds, smartwatches & cases" },
    { name: "Stationery & Lab", slug: "stationery", icon: "pen-tool", description: "Notebooks, lab coats, drafting kits & calculators" },
    { name: "Handmade & Art", slug: "handmade", icon: "palette", description: "Student-crafted ceramics, stickers, posters & crafts" },
    { name: "Campus Food & Treats", slug: "food", icon: "coffee", description: "Dorm bakeries, snack boxes, artisan cookies & coffee" },
    { name: "Student Services", slug: "services", icon: "briefcase", description: "Peer tutoring, resume review, photography & coding help" },
    { name: "Event Tickets", slug: "tickets", icon: "ticket", description: "Hackathons, campus festivals, sports & formal galas" },
    { name: "Used Products & Dorm", slug: "used", icon: "repeat", description: "Mini fridges, desk lamps, organizers & pre-loved goods" },
  ];

  const categories = {};
  for (let i = 0; i < categoriesData.length; i++) {
    const c = categoriesData[i];
    const cat = await prisma.category.create({
      data: {
        name: c.name,
        slug: c.slug,
        icon: c.icon,
        description: c.description,
        displayOrder: i + 1,
        isActive: true,
      },
    });
    categories[c.slug] = cat;
  }

  // 3. Create Admin User
  console.log("Creating authorized admin account (unispacexteam@gmail.com)...");
  const adminPasswordHash = await bcrypt.hash(
    process.env.ADMIN_PASSWORD || "AdminSecure2026!",
    10
  );
  const admin = await prisma.user.create({
    data: {
      name: "UniSpaceX Team Admin",
      email: "unispacexteam@gmail.com",
      passwordHash: adminPasswordHash,
      role: "ADMIN",
      collegeId: stanford.id,
      phone: "+15550192834",
      avatarUrl: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80",
      studentVerificationStatus: "APPROVED",
    },
  });

  // 4. Create Student Users (20 students)
  console.log("Creating student accounts across verification states...");
  const defaultPasswordHash = await bcrypt.hash("Password123!", 10);

  const studentProfiles = [
    // Approved Sellers (1-5)
    { name: "Mohamed Afraar", email: "mohamed@saec.ac.in", college: saec, role: "SELLER", status: "APPROVED", phone: "+919840123456", dept: "Computer Science", year: "2026" },
    { name: "Elena Rostova", email: "elena@stanford.edu", college: stanford, role: "SELLER", status: "APPROVED", phone: "+16505550192", dept: "Product Design", year: "2026" },
    { name: "David Chen", email: "dchen@mit.edu", college: mit, role: "SELLER", status: "APPROVED", phone: "+16175550183", dept: "EECS", year: "2027" },
    { name: "Aaliyah Patel", email: "aaliyah@berkeley.edu", college: berkeley, role: "SELLER", status: "APPROVED", phone: "+15105550144", dept: "Business Administration", year: "2026" },
    { name: "Marcus Thorne", email: "marcus@mit.edu", college: mit, role: "SELLER", status: "APPROVED", phone: "+16175550199", dept: "Mechanical Engineering", year: "2027" },

    // Approved Students (6-11)
    { name: "Sarah Jenkins", email: "sjenkins@stanford.edu", college: stanford, role: "STUDENT", status: "APPROVED", phone: "+16505550111", dept: "Bioengineering", year: "2028" },
    { name: "Liam O'Connor", email: "liam@berkeley.edu", college: berkeley, role: "STUDENT", status: "APPROVED", phone: "+15105550122", dept: "Data Science", year: "2027" },
    { name: "Maya Lin", email: "mlin@mit.edu", college: mit, role: "STUDENT", status: "APPROVED", phone: "+16175550133", dept: "Architecture", year: "2026" },
    { name: "Kavya Sundaram", email: "kavya@saec.ac.in", college: saec, role: "STUDENT", status: "APPROVED", phone: "+919840998877", dept: "Information Tech", year: "2027" },
    { name: "Jordan Brooks", email: "jbrooks@stanford.edu", college: stanford, role: "STUDENT", status: "APPROVED", phone: "+16505550145", dept: "Physics", year: "2026" },
    { name: "Zack Miller", email: "zmiller@berkeley.edu", college: berkeley, role: "STUDENT", status: "APPROVED", phone: "+15105550156", dept: "Cognitive Science", year: "2028" },

    // Pending Student Verification Requests (12-15)
    { name: "Chloe Kim", email: "ckim@mit.edu", college: mit, role: "STUDENT", status: "PENDING", phone: "+16175550167", dept: "Materials Science", year: "2028" },
    { name: "Rohan Varma", email: "rohan@saec.ac.in", college: saec, role: "STUDENT", status: "PENDING", phone: "+919840554433", dept: "ECE", year: "2028" },
    { name: "Olivia Vance", email: "ovance@stanford.edu", college: stanford, role: "STUDENT", status: "PENDING", phone: "+16505550178", dept: "Economics", year: "2027" },
    { name: "Ethan Wright", email: "ewright@berkeley.edu", college: berkeley, role: "STUDENT", status: "PENDING", phone: "+15105550189", dept: "Civil Engineering", year: "2028" },

    // Rejected Student Verification Requests (16-18)
    { name: "Hannah Scott", email: "hscott@mit.edu", college: mit, role: "STUDENT", status: "REJECTED", phone: "+16175550190", dept: "Chemical Engineering", year: "2027" },
    { name: "Pooja Hegde", email: "pooja@saec.ac.in", college: saec, role: "STUDENT", status: "REJECTED", phone: "+919840223344", dept: "Biomedical Eng", year: "2028" },
    { name: "Lucas Vance", email: "lvance@stanford.edu", college: stanford, role: "STUDENT", status: "REJECTED", phone: "+16505550101", dept: "History", year: "2026" },

    // Additional Student accounts
    { name: "Noah Carter", email: "ncarter@berkeley.edu", college: berkeley, role: "STUDENT", status: "PENDING", phone: "+15105550102", dept: "Film & Media", year: "2027" },
    { name: "Sophia Diaz", email: "sdiaz@mit.edu", college: mit, role: "STUDENT", status: "APPROVED", phone: "+16175550103", dept: "Mathematics", year: "2027" },
  ];

  const createdUsers = [];
  for (let i = 0; i < studentProfiles.length; i++) {
    const sp = studentProfiles[i];
    const user = await prisma.user.create({
      data: {
        name: sp.name,
        email: sp.email,
        passwordHash: defaultPasswordHash,
        role: sp.role,
        collegeId: sp.college.id,
        phone: sp.phone,
        avatarUrl: `https://api.dicebear.com/7.x/initials/svg?seed=${encodeURIComponent(sp.name)}`,
        studentVerificationStatus: sp.status,
      },
    });

    const isAppr = sp.status === "APPROVED";
    const isRej = sp.status === "REJECTED";

    // Write private sample student ID document to secure vault
    const studentDocId = `doc_sec_demo_student_${i + 1}`;
    const studentRoll = `STU-${sp.college.domain.split(".")[0].toUpperCase()}-${2000 + i}`;
    writeVaultDoc(studentDocId, sp.name, sp.college.name, studentRoll, "STUDENT ID CARD");

    // Student Verification record
    await prisma.studentVerification.create({
      data: {
        userId: user.id,
        collegeId: sp.college.id,
        studentIdNumber: studentRoll,
        department: sp.dept,
        graduationYear: sp.year,
        status: sp.status,
        provider: "campus_admin",
        verificationMethod: "STUDENT_ID_DOCUMENT_AND_DIRECTORY",
        adminNotes: isRej
          ? "Uploaded student card was expired / illegible. Please provide a clear official student ID card showing current academic session."
          : isAppr
          ? "Official student record confirmed with university student directory."
          : null,
        reviewedAt: isAppr || isRej ? new Date() : null,
        reviewedBy: isAppr || isRej ? admin.email : null,
        verifiedAt: isAppr ? new Date() : null,
        expiresAt: isAppr ? new Date(Date.now() + 365 * 24 * 60 * 60 * 1000) : null,
        documentUrl: studentDocId,
      },
    });

    createdUsers.push(user);
  }

  // Write demo government IDs into vault
  for (let i = 0; i < 5; i++) {
    writeVaultDoc(`doc_sec_demo_govt_${i + 1}`, studentProfiles[i].name, studentProfiles[i].college.name, `DL-GOVT-${8000 + i}`, "GOVERNMENT DRIVER LICENSE");
  }
  writeVaultDoc("doc_sec_demo_govt_sarah", "Sarah Jenkins", "Stanford University", "DL-CA-9921443", "GOVERNMENT DRIVER LICENSE");
  writeVaultDoc("doc_sec_demo_govt_liam", "Liam O'Connor", "UC Berkeley", "PASS-US-0847291", "US PASSPORT");
  writeVaultDoc("doc_sec_demo_govt_maya", "Maya Lin", "MIT", "NID-7729104", "NATIONAL IDENTITY CARD");

  // 5. Create Seller Profiles & Applications
  console.log("Creating verified, pending, and rejected seller applications...");
  const sellers = createdUsers.slice(0, 5);
  const sellerBios = [
    "Computer Science senior & hardware hacker. Building custom embedded dev kits, micro-PCBs, and campus electronics.",
    "Stanford Product Design major. Custom hand-screenprinted campus streetwear, collegiate totes, and minimalist accessories.",
    "MIT EECS junior. Tutoring in Algorithms, Discrete Math & selling top-ranked exam notes with 4.9 average review score.",
    "Berkeley Haas entrepreneur. Handcrafted dorm decor, sustainable resin desk coasters, and artisan room accents.",
    "Robotics enthusiast. Refurbishing graph calculators, monitors, dorm gadgets, and precision 3D printing services.",
  ];

  // 5 Approved Sellers
  for (let i = 0; i < sellers.length; i++) {
    const s = sellers[i];
    await prisma.sellerApplication.create({
      data: {
        userId: s.id,
        fullName: s.name,
        collegeName: studentProfiles[i].college.name,
        govtIdType: "DRIVER_LICENSE",
        govtIdNumber: `DL-GOVT-${8000 + i}`,
        govtIdUrl: `doc_sec_demo_govt_${i + 1}`,
        whatsappNumber: s.phone,
        description: sellerBios[i],
        productCategories: "Electronics, Notes, Fashion, Services",
        sampleImages: JSON.stringify([
          "https://images.unsplash.com/photo-1550009158-9ebf69173e03?w=500&auto=format&fit=crop&q=80",
          "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=500&auto=format&fit=crop&q=80",
        ]),
        status: "APPROVED",
        adminNotes: "Government ID and student directory credentials verified. Approved for campus merchant status.",
        reviewedAt: new Date(),
        reviewedBy: admin.email,
      },
    });

    await prisma.sellerProfile.create({
      data: {
        userId: s.id,
        displayName: s.name,
        bio: sellerBios[i],
        whatsappNumber: s.phone,
        isWhatsappPublic: true,
        rating: 4.9,
        totalSales: 15 + i * 8,
        isVerifiedSeller: true,
      },
    });
  }

  // Pending Seller Applications (for Admin Review)
  console.log("Creating pending seller applications for admin review...");
  const pendingSeller1 = createdUsers[5]; // Sarah Jenkins (Approved Student)
  await prisma.sellerApplication.create({
    data: {
      userId: pendingSeller1.id,
      fullName: pendingSeller1.name,
      collegeName: "Stanford University",
      govtIdType: "DRIVER_LICENSE",
      govtIdNumber: "DL-CA-9921443",
      govtIdUrl: "doc_sec_demo_govt_sarah",
      whatsappNumber: "+16505550111",
      description: "Bioengineering sophomore offering organic sourdough bread, vegan dorm treats, and handmade campus snacks.",
      productCategories: "Campus Food & Treats, Handmade & Art",
      sampleImages: JSON.stringify([
        "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=500&auto=format&fit=crop&q=80",
      ]),
      status: "PENDING",
    },
  });

  const pendingSeller2 = createdUsers[6]; // Liam O'Connor (Approved Student)
  await prisma.sellerApplication.create({
    data: {
      userId: pendingSeller2.id,
      fullName: pendingSeller2.name,
      collegeName: "University of California, Berkeley",
      govtIdType: "PASSPORT",
      govtIdNumber: "PASS-US-0847291",
      govtIdUrl: "doc_sec_demo_govt_liam",
      whatsappNumber: "+15105550122",
      description: "Data Science junior selling mechanical keyboards, custom keycap sets, and coiled USB-C cables.",
      productCategories: "Electronics, Accessories",
      sampleImages: JSON.stringify([
        "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500&auto=format&fit=crop&q=80",
      ]),
      status: "PENDING",
    },
  });

  // Rejected Seller Application (for rejection & resubmission demo)
  const rejectedSeller = createdUsers[7]; // Maya Lin
  await prisma.sellerApplication.create({
    data: {
      userId: rejectedSeller.id,
      fullName: rejectedSeller.name,
      collegeName: "Massachusetts Institute of Technology",
      govtIdType: "NATIONAL_ID",
      govtIdNumber: "NID-7729104",
      govtIdUrl: "doc_sec_demo_govt_maya",
      whatsappNumber: "+16175550133",
      description: "Architecture student selling custom 3D printed desk planters and scale architectural models.",
      productCategories: "Handmade & Art, Student Services",
      status: "REJECTED",
      adminNotes: "The uploaded Government ID was cropped and the expiration date was unreadable. Please resubmit with a clear, full photo of your Government ID.",
      reviewedAt: new Date(),
      reviewedBy: admin.email,
    },
  });

  // 6. Create 20 Rich Campus Products
  console.log("Creating 20 campus products across categories...");
  const productsData = [
    {
      seller: sellers[0],
      name: "TI-84 Plus CE Color Graphing Calculator",
      slug: "ti-84-plus-ce-graphing-calculator",
      description: "Like-new condition Texas Instruments TI-84 Plus CE Color Graphing Calculator. Screen is scratch-free, includes slide cover and original USB charging cable. Essential for Calculus, Linear Algebra, and Statistics exams.",
      price: 65.0,
      category: categories["electronics"],
      condition: "LIKE_NEW",
      campusLocation: "Engineering Quad & Library",
      images: [
        "https://images.unsplash.com/photo-1594980596870-8aa52a78d8cd?w=800&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1611117775350-ac3950990985?w=800&auto=format&fit=crop&q=80",
      ],
      isFeatured: true,
    },
    {
      seller: sellers[1],
      name: "Vintage Campus Oversized Fleece Hoodie",
      slug: "vintage-campus-oversized-fleece-hoodie",
      description: "Heavyweight 450gsm organic cotton hoodie with collegiate embroidery. Dropped shoulders, super warm fleece lining, unisex size L. Custom student limited batch.",
      price: 38.0,
      category: categories["fashion"],
      condition: "NEW",
      campusLocation: "Student Center / Dining Commons",
      images: [
        "https://images.unsplash.com/photo-1556905055-8f358a7a47b2?w=800&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1509967419530-da38b4704bc6?w=800&auto=format&fit=crop&q=80",
      ],
      isFeatured: true,
    },
    {
      seller: sellers[2],
      name: "CS106B / Algorithms Comprehensive Exam Notes & Cheat Sheets",
      slug: "algorithms-comprehensive-exam-notes",
      description: "52-page bound full-color visual guide covering Dynamic Programming, Graph Traversals, Big-O Derivations, and past midterm problem walk-throughs with verified solutions. Helped 140+ students achieve an A.",
      price: 15.0,
      category: categories["notes"],
      condition: "NEW",
      campusLocation: "Gates Computer Science Building",
      images: [
        "https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=800&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1517842645767-c639042777db?w=800&auto=format&fit=crop&q=80",
      ],
      isFeatured: true,
    },
    {
      seller: sellers[0],
      name: "ESP32-S3 Dual-Core IoT Prototype Board with OLED",
      slug: "esp32-s3-iot-prototype-board",
      description: "Custom engineered student IoT development board featuring ESP32-S3 (WiFi 4 + Bluetooth 5 LE), on-board 0.96 inch I2C OLED display, USB-C programming port, and LiPo battery management.",
      price: 24.5,
      category: categories["electronics"],
      condition: "NEW",
      campusLocation: "Hardware Maker Lab",
      images: [
        "https://images.unsplash.com/photo-1518770660439-4636190af475?w=800&auto=format&fit=crop&q=80",
      ],
      isFeatured: true,
    },
    {
      seller: sellers[4],
      name: "Dell UltraSharp 24-inch IPS Monitor (USB-C Hub)",
      slug: "dell-ultrasharp-24-inch-monitor",
      description: "Dell 1080p IPS monitor with 65W USB-C power delivery. One single cable powers your MacBook/Windows laptop while extending display. Great desk setup for coding and studying.",
      price: 110.0,
      category: categories["electronics"],
      condition: "GOOD",
      campusLocation: "North Campus Dorms",
      images: [
        "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=800&auto=format&fit=crop&q=80",
      ],
      isFeatured: false,
    },
    {
      seller: sellers[3],
      name: "Handmade Botanical Ceramic Mug & Coaster Set",
      slug: "handmade-botanical-ceramic-mug-set",
      description: "Hand-thrown speckled stoneware ceramic mug with wildflower imprint. Dishwasher safe, microwave safe. Holds 14 oz. Crafted in campus arts studio.",
      price: 22.0,
      category: categories["handmade"],
      condition: "NEW",
      campusLocation: "Art Practice Building",
      images: [
        "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=800&auto=format&fit=crop&q=80",
      ],
      isFeatured: true,
    },
    {
      seller: sellers[1],
      name: "Collegiate Heavy Canvas Laptop Tote Bag",
      slug: "collegiate-canvas-laptop-tote-bag",
      description: "Sturdy 16oz cotton canvas tote with padded 15-inch laptop sleeve, brass zippers, key leash, and water bottle pocket. Weather-treated for campus rainy days.",
      price: 26.0,
      category: categories["accessories"],
      condition: "NEW",
      campusLocation: "Main Quad Bookstore Steps",
      images: [
        "https://images.unsplash.com/photo-1544816155-12df9643f363?w=800&auto=format&fit=crop&q=80",
      ],
      isFeatured: false,
    },
    {
      seller: sellers[2],
      name: "1-on-1 Python & Data Structures Tutoring (1 Hour Session)",
      slug: "python-data-structures-tutoring-session",
      description: "One hour personalized coaching covering recursion, pointers, trees, graphs, and leetcode interview prep. Led by TA with 3 semesters of teaching experience.",
      price: 30.0,
      category: categories["services"],
      condition: "NEW",
      campusLocation: "Campus Library or Zoom",
      images: [
        "https://images.unsplash.com/photo-1531482615713-2afd69097998?w=800&auto=format&fit=crop&q=80",
      ],
      isFeatured: false,
    },
    {
      seller: sellers[4],
      name: "Anker Soundcore Space Q45 Noise Cancelling Headphones",
      slug: "anker-soundcore-space-q45-headphones",
      description: "Adaptive active noise cancelling over-ear headphones. 50 hours battery life with ANC on, LDAC hi-res audio, ultra-comfortable memory foam earcups. Perfect for library deep work.",
      price: 75.0,
      category: categories["electronics"],
      condition: "LIKE_NEW",
      campusLocation: "Graduate Student Lounge",
      images: [
        "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&auto=format&fit=crop&q=80",
      ],
      isFeatured: true,
    },
    {
      seller: sellers[3],
      name: "Dorm Scent Bar: Artisan Soy Wax Candles (Set of 3)",
      slug: "dorm-scent-bar-soy-wax-candles",
      description: "Hand-poured 100% natural soy wax candles with cotton wicks. Scents: Espresso & Vanilla, Clean Linen, and Fresh Eucalyptus. 25hr burn time each.",
      price: 18.0,
      category: categories["handmade"],
      condition: "NEW",
      campusLocation: "West Hall Commons",
      images: [
        "https://images.unsplash.com/photo-1603006905003-be475563bc59?w=800&auto=format&fit=crop&q=80",
      ],
      isFeatured: false,
    },
    {
      seller: sellers[0],
      name: "Raspberry Pi 4 Model B (4GB RAM) with Aluminum Heatsink Case",
      slug: "raspberry-pi-4-model-b-4gb",
      description: "Raspberry Pi 4 Model B with 4GB LPDDR4, dual micro-HDMI outputs, gigabit ethernet, passive cooling armor case, official 15W USB-C power supply, and 64GB SanDisk Extreme micro-SD.",
      price: 55.0,
      category: categories["electronics"],
      condition: "GOOD",
      campusLocation: "Robotics Innovation Wing",
      images: [
        "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=800&auto=format&fit=crop&q=80",
      ],
      isFeatured: false,
    },
    {
      seller: sellers[1],
      name: "Organic Chemistry: Structure and Function (8th Edition)",
      slug: "organic-chemistry-structure-function-8th-ed",
      description: "Hardcover textbook in clean condition. No highlighter marks or missing pages. Includes molecular model kit for stereochemistry lab assignments.",
      price: 45.0,
      category: categories["books"],
      condition: "GOOD",
      campusLocation: "Chemistry Annex",
      images: [
        "https://images.unsplash.com/photo-1532012164546-f432f2e3777f?w=800&auto=format&fit=crop&q=80",
      ],
      isFeatured: false,
    },
    {
      seller: sellers[4],
      name: "Compact Dorm Mini Fridge (1.6 Cu. Ft) with Freezer Compartment",
      slug: "compact-dorm-mini-fridge-1-6-cu-ft",
      description: "Energy Star certified black mini fridge with reversible door and small freezer tray. Quiet compressor, cleaned and sanitized. Used for one academic semester.",
      price: 50.0,
      category: categories["used"],
      condition: "GOOD",
      campusLocation: "Frosh Quad Hall A",
      images: [
        "https://images.unsplash.com/photo-1584992236310-6edddc08acff?w=800&auto=format&fit=crop&q=80",
      ],
      isFeatured: false,
    },
    {
      seller: sellers[3],
      name: "Matcha Latte & Espresso Dorm Pastry Box (Pack of 6)",
      slug: "matcha-latte-dorm-pastry-box",
      description: "Freshly baked Saturday morning pastry box: 3 Uji matcha white chocolate cookies and 3 browned butter espresso blondies. Baked with organic butter.",
      price: 14.0,
      category: categories["food"],
      condition: "NEW",
      campusLocation: "Campus Plaza Fountain",
      images: [
        "https://images.unsplash.com/photo-1495147466023-ac5c588e2e94?w=800&auto=format&fit=crop&q=80",
      ],
      isFeatured: true,
    },
    {
      seller: sellers[2],
      name: "Professional Resume & Portfolio Critique (Engineering & Business)",
      slug: "resume-portfolio-critique-service",
      description: "Full line-by-line review of your technical resume and portfolio website. Includes ATS optimization, action verb tightening, and tips that helped land FAANG & Big 4 interviews.",
      price: 20.0,
      category: categories["services"],
      condition: "NEW",
      campusLocation: "Online via Notion / PDF",
      images: [
        "https://images.unsplash.com/photo-1586281380349-632531db7ed4?w=800&auto=format&fit=crop&q=80",
      ],
      isFeatured: false,
    },
    {
      seller: sellers[0],
      name: "Keychron K2 Wireless Mechanical Keyboard (Brown Switches)",
      slug: "keychron-k2-wireless-keyboard",
      description: "75% layout mechanical keyboard with Gateron Brown tactile switches, RGB backlighting, Mac and Windows keycaps, and braided USB-C cable. Bluetooth connects up to 3 devices.",
      price: 58.0,
      category: categories["electronics"],
      condition: "LIKE_NEW",
      campusLocation: "Engineering Computing Lab",
      images: [
        "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=800&auto=format&fit=crop&q=80",
      ],
      isFeatured: false,
    },
    {
      seller: sellers[1],
      name: "Campus Hackathon 2026 Premium Swag Jacket (Limited)",
      slug: "campus-hackathon-2026-swag-jacket",
      description: "Waterproof windbreaker jacket from the annual collegiate hackathon. Size M, black with subtle reflective geometric accents. Brand new with tags.",
      price: 32.0,
      category: categories["fashion"],
      condition: "NEW",
      campusLocation: "Innovation Hub",
      images: [
        "https://images.unsplash.com/photo-1544923246-77307dd654cb?w=800&auto=format&fit=crop&q=80",
      ],
      isFeatured: false,
    },
    {
      seller: sellers[4],
      name: "Adjustable Ergonomic Mesh Desk Chair",
      slug: "adjustable-ergonomic-mesh-desk-chair",
      description: "Breathable mesh back with adjustable lumbar support, 3D armrests, and smooth rollerblade wheels that won't scratch dorm floorboards. Great condition.",
      price: 65.0,
      category: categories["used"],
      condition: "GOOD",
      campusLocation: "Graduate Dorm South",
      images: [
        "https://images.unsplash.com/photo-1580481077195-c328a37db719?w=800&auto=format&fit=crop&q=80",
      ],
      isFeatured: false,
    },
    {
      seller: sellers[3],
      name: "Collegiate Campus Map & Architecture Minimalist Posters (Set of 2)",
      slug: "collegiate-campus-map-posters",
      description: "Two 18x24 inch museum-quality matte art prints featuring minimal architectural blueprints of iconic campus landmarks. Ready for framing in dorm rooms.",
      price: 20.0,
      category: categories["handmade"],
      condition: "NEW",
      campusLocation: "Architecture Studio Lobby",
      images: [
        "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?w=800&auto=format&fit=crop&q=80",
      ],
      isFeatured: false,
    },
    {
      seller: sellers[2],
      name: "Spring Campus Gala & Music Festival VIP Access Ticket",
      slug: "spring-campus-gala-ticket",
      description: "Official student ticket transfer for the upcoming Spring Music Festival & Campus Formal Gala. Includes front stage wristband and event catering pass.",
      price: 25.0,
      category: categories["tickets"],
      condition: "NEW",
      campusLocation: "Student Union Box Office",
      images: [
        "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=800&auto=format&fit=crop&q=80",
      ],
      isFeatured: true,
    },
  ];

  const createdProducts = [];
  for (let i = 0; i < productsData.length; i++) {
    const p = productsData[i];
    const product = await prisma.product.create({
      data: {
        sellerId: p.seller.id,
        name: p.name,
        slug: `${p.slug}-${Date.now() + i}`,
        description: p.description,
        price: p.price,
        categoryId: p.category.id,
        condition: p.condition,
        status: "ACTIVE",
        quantity: 1,
        collegeId: p.seller.collegeId,
        campusLocation: p.campusLocation,
        whatsappContact: p.seller.phone,
        viewCount: Math.floor(Math.random() * 80) + 12,
        isFeatured: p.isFeatured,
      },
    });

    for (let imgIdx = 0; imgIdx < p.images.length; imgIdx++) {
      await prisma.productImage.create({
        data: {
          productId: product.id,
          url: p.images[imgIdx],
          thumbnailUrl: p.images[imgIdx],
          isPrimary: imgIdx === 0,
          displayOrder: imgIdx,
        },
      });
    }

    createdProducts.push(product);
  }

  // 7. Create Sample Favorites
  console.log("Creating user favorites...");
  for (let i = 5; i < 12; i++) {
    const student = createdUsers[i];
    const targetProduct = createdProducts[(i * 3) % createdProducts.length];
    await prisma.favorite.create({
      data: {
        userId: student.id,
        productId: targetProduct.id,
      },
    });
  }

  // 8. Create Sample Reports for Admin Moderation Demo
  console.log("Creating sample moderation reports...");
  await prisma.report.create({
    data: {
      reporterId: createdUsers[6].id,
      productId: createdProducts[11].id, // Organic Chem Book
      reason: "WRONG_INFO",
      description: "The listing says 8th Edition but the picture shows 7th Edition cover. Please verify with seller.",
      status: "PENDING",
    },
  });

  await prisma.report.create({
    data: {
      reporterId: createdUsers[7].id,
      productId: createdProducts[12].id, // Mini Fridge
      reason: "OTHER",
      description: "Seller mentioned pickup time changed to next weekend only.",
      status: "RESOLVED",
      adminNotes: "Contacted seller and confirmed pickup timeline is fine.",
      resolvedAt: new Date(),
    },
  });

  // 9. Create Sample Notifications
  console.log("Creating sample notifications...");
  await prisma.notification.create({
    data: {
      userId: sellers[0].id,
      title: "Seller Application Approved! 🎉",
      message: "Congratulations! Your UNISpaceX seller application has been approved by campus administration. You can now post listings.",
      type: "SELLER_APPROVED",
      linkUrl: "/dashboard",
      isRead: true,
    },
  });

  await prisma.notification.create({
    data: {
      userId: pendingSeller1.id,
      title: "Seller Application Received",
      message: "Your seller verification application is currently Under Review by campus moderators.",
      type: "INFO",
      linkUrl: "/dashboard",
      isRead: false,
    },
  });

  // 10. Create Platform Announcement
  console.log("Creating campus announcements...");
  await prisma.announcement.create({
    data: {
      title: "Welcome to UNISpaceX Spring 2026 Semester!",
      content: "All college students with verified .edu or .ac campus emails receive instant verified student badges and direct WhatsApp buyer protections.",
      type: "INFO",
      isActive: true,
    },
  });

  console.log("✅ UNISpaceX Database Seed completed successfully!");
  console.log("--------------------------------------------------");
  console.log(`Colleges created: 4`);
  console.log(`Users created: ${createdUsers.length + 1} (1 Admin, 5 Verified Sellers, 15 Students)`);
  console.log(`Products created: ${createdProducts.length}`);
  console.log(`Admin Login: ${process.env.ADMIN_EMAIL || "admin@unispace.edu"}`);
  console.log(`Admin Password: ${process.env.ADMIN_PASSWORD || "AdminPassword123!"}`);
  console.log(`Student Login: mohamed@saec.ac.in (or elena@stanford.edu)`);
  console.log(`Student Password: Password123!`);
  console.log("--------------------------------------------------");
}

main()
  .catch((e) => {
    console.error("❌ Seed error:", e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
