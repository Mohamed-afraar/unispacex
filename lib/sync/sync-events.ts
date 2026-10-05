// Centralized Real-Time Synchronization Engine for UniSpaceX & Admin Portal
import prisma from "@/lib/db/prisma";

export type SyncEventType =
  | "STUDENT_VERIFICATION_SUBMITTED"
  | "STUDENT_VERIFICATION_REVIEWED"
  | "SELLER_APPLICATION_SUBMITTED"
  | "SELLER_APPLICATION_REVIEWED"
  | "USER_STATUS_CHANGED"
  | "ROLE_SELECTED"
  | "PROFILE_UPDATED"
  | "PRODUCT_CHANGED"
  | "SYSTEM_HEARTBEAT";

export interface SyncEventPayload {
  id: string;
  type: SyncEventType;
  userId?: string;
  message?: string;
  data?: Record<string, any>;
  timestamp: number;
}

interface SyncState {
  globalVersion: number;
  studentVerificationVersion: number;
  sellerApplicationVersion: number;
  userVersion: number;
  productVersion: number;
  lastEvent: SyncEventPayload | null;
  recentEvents: SyncEventPayload[];
  listeners: Set<(event: SyncEventPayload) => void>;
}

// Attach to globalThis to ensure singleton state in Next.js development and production
const globalWithSync = globalThis as unknown as {
  __unispaceXSyncState?: SyncState;
};

if (!globalWithSync.__unispaceXSyncState) {
  globalWithSync.__unispaceXSyncState = {
    globalVersion: 1,
    studentVerificationVersion: 1,
    sellerApplicationVersion: 1,
    userVersion: 1,
    productVersion: 1,
    lastEvent: {
      id: "init",
      type: "SYSTEM_HEARTBEAT",
      message: "Sync Engine Online",
      timestamp: Date.now(),
    },
    recentEvents: [],
    listeners: new Set(),
  };
}

const syncState = globalWithSync.__unispaceXSyncState;

export function broadcastSyncEvent(
  type: SyncEventType,
  data?: Record<string, any>,
  userId?: string,
  message?: string
): SyncEventPayload {
  syncState.globalVersion += 1;

  if (type === "STUDENT_VERIFICATION_SUBMITTED" || type === "STUDENT_VERIFICATION_REVIEWED") {
    syncState.studentVerificationVersion += 1;
    syncState.userVersion += 1;
  } else if (type === "SELLER_APPLICATION_SUBMITTED" || type === "SELLER_APPLICATION_REVIEWED") {
    syncState.sellerApplicationVersion += 1;
    syncState.userVersion += 1;
  } else if (type === "USER_STATUS_CHANGED" || type === "ROLE_SELECTED" || type === "PROFILE_UPDATED") {
    syncState.userVersion += 1;
    syncState.studentVerificationVersion += 1;
    syncState.sellerApplicationVersion += 1;
  } else if (type === "PRODUCT_CHANGED") {
    syncState.productVersion += 1;
  }

  const eventPayload: SyncEventPayload = {
    id: `evt_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`,
    type,
    userId,
    message: message || `Sync event: ${type}`,
    data: data || {},
    timestamp: Date.now(),
  };

  syncState.lastEvent = eventPayload;
  syncState.recentEvents.unshift(eventPayload);
  if (syncState.recentEvents.length > 50) {
    syncState.recentEvents.pop();
  }

  // Notify any active SSE subscribers
  syncState.listeners.forEach((listener) => {
    try {
      listener(eventPayload);
    } catch (e) {
      console.error("Error in sync listener:", e);
    }
  });

  return eventPayload;
}

export async function getSyncTelemetry() {
  // Query fast real-time counts from database
  let pendingStudents = 0;
  let pendingSellers = 0;
  let verifiedStudents = 0;
  let approvedSellers = 0;
  let totalUsers = 0;

  try {
    const [pStudents, pSellers, vStudents, aSellers, tUsers] = await Promise.all([
      prisma.studentVerification.count({ where: { status: "PENDING" } }),
      prisma.sellerApplication.count({ where: { status: "PENDING" } }),
      prisma.studentVerification.count({ where: { status: "APPROVED" } }),
      prisma.user.count({ where: { role: "SELLER" } }),
      prisma.user.count(),
    ]);
    pendingStudents = pStudents;
    pendingSellers = pSellers;
    verifiedStudents = vStudents;
    approvedSellers = aSellers;
    totalUsers = tUsers;
  } catch (err) {
    console.error("Error fetching sync counts from database:", err);
  }

  return {
    globalVersion: syncState.globalVersion,
    studentVerificationVersion: syncState.studentVerificationVersion,
    sellerApplicationVersion: syncState.sellerApplicationVersion,
    userVersion: syncState.userVersion,
    productVersion: syncState.productVersion,
    pendingStudents,
    pendingSellers,
    verifiedStudents,
    approvedSellers,
    totalUsers,
    lastEvent: syncState.lastEvent,
    recentEvents: syncState.recentEvents.slice(0, 10),
    timestamp: Date.now(),
  };
}

export function subscribeToSyncEvents(listener: (event: SyncEventPayload) => void) {
  syncState.listeners.add(listener);
  return () => {
    syncState.listeners.delete(listener);
  };
}
