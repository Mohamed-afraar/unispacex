import { NextResponse } from "next/server";
import { getSyncTelemetry, subscribeToSyncEvents } from "@/lib/sync/sync-events";

// Support both standard GET polling and SSE streaming
export async function GET(request: Request) {
  const { searchParams } = new URL(request.url);
  const stream = searchParams.get("stream") === "true";

  const corsHeaders = {
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Methods": "GET, OPTIONS",
    "Access-Control-Allow-Headers": "Authorization, Content-Type",
  };

  // If client requested SSE streaming
  if (stream) {
    const encoder = new TextEncoder();
    let unsubscribe: (() => void) | null = null;

    const readable = new ReadableStream({
      async start(controller) {
        // Send initial state immediately
        const initial = await getSyncTelemetry();
        controller.enqueue(
          encoder.encode(`event: init\ndata: ${JSON.stringify(initial)}\n\n`)
        );

        // Listen for new sync events
        unsubscribe = subscribeToSyncEvents((event) => {
          try {
            controller.enqueue(
              encoder.encode(`event: update\ndata: ${JSON.stringify(event)}\n\n`)
            );
          } catch {
            // Stream closed
          }
        });

        // Keep-alive heartbeat every 15 seconds
        const heartbeat = setInterval(() => {
          try {
            controller.enqueue(encoder.encode(`: heartbeat\n\n`));
          } catch {
            clearInterval(heartbeat);
          }
        }, 15000);

        request.signal.addEventListener("abort", () => {
          if (unsubscribe) unsubscribe();
          clearInterval(heartbeat);
          try {
            controller.close();
          } catch {
            // Closed
          }
        });
      },
      cancel() {
        if (unsubscribe) unsubscribe();
      },
    });

    return new Response(readable, {
      headers: {
        "Content-Type": "text/event-stream",
        "Cache-Control": "no-cache, no-transform",
        Connection: "keep-alive",
        ...corsHeaders,
      },
    });
  }

  // Fast polling response
  const telemetry = await getSyncTelemetry();
  return NextResponse.json(telemetry, {
    status: 200,
    headers: {
      "Cache-Control": "no-store, no-cache, must-revalidate",
      ...corsHeaders,
    },
  });
}

export async function OPTIONS() {
  return new NextResponse(null, {
    status: 204,
    headers: {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET, OPTIONS",
      "Access-Control-Allow-Headers": "Authorization, Content-Type",
    },
  });
}
