const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_sylo_01";
const BOB_UID = "bob_sylo_02";
const ADMIN_UID = "admin_sylo_00";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastDate(offsetMs = 2000) {
  return new Date(Date.now() - offsetMs);
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users are blocked from all collections", async () => {
  const unauth = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauth.collection("posts").where("visibility", "==", "public").get());
  await assertFails(unauth.collection("live_streams").where("status", "==", "LIVE").get());
  await assertFails(unauth.collection("users").doc(ALICE_UID).get());
  await assertFails(unauth.collection("conversations").where("memberUids", "array-contains", ALICE_UID).get());
});

test("Users collection isolates PII and blocks privilege escalation & shadow fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com", email_verified: true }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@example.com", email_verified: true }).firestore();
  const ts = pastDate(5000);

  // Valid user profile creation
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Aarav Sharma",
      username: "aarav_s",
      email: "alice@example.com",
      bio: "Mumbai creator",
      location: "Mumbai, India",
      interfaceLanguage: "hi",
      preferredLanguages: ["hi", "en", "mr"],
      interests: ["Entertainment", "Comedy"],
      privateAccount: false,
      onboardingCompleted: true,
      lowDataMode: false,
      verified: false,
      earningsInr: 0,
      giftCoins: 0,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Bob cannot read or update Alice's private account (PII protection)
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).update({ bio: "Hacked", updatedAt: pastDate(1000) }));

  // Alice cannot self-verify or inflate earningsInr
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      verified: true,
      updatedAt: pastDate(1000),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      earningsInr: 99999,
      updatedAt: pastDate(1000),
    })
  );

  // Valid partial update of allowed profile fields succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      bio: "Updated Hindi & Marathi reel creator",
      lowDataMode: true,
      updatedAt: pastDate(1000),
    })
  );

  // Future timestamp rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      bio: "Future write",
      updatedAt: new Date(Date.now() + 120000),
    })
  );
});

test("Posts, Short Videos & Comments enforce visibility filter, ownership, and parent existence", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = pastDate(5000);

  // Reject ghost `id` field when creating
  await assertFails(
    aliceDb.collection("posts").doc("post_bad").set({
      id: "post_bad",
      authorId: ALICE_UID,
      authorName: "Aarav",
      authorHandle: "@aarav_s",
      contentType: "SHORT_VIDEO",
      languageCode: "hi",
      category: "Entertainment",
      title: "Bad post",
      body: "Includes forbidden id field",
      mediaKind: "VIDEO",
      visibility: "public",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Valid Kotlin-aligned create succeeds
  await assertSucceeds(
    aliceDb.collection("posts").doc("post_1").set({
      authorId: ALICE_UID,
      authorName: "Aarav Sharma",
      authorHandle: "@aarav_s",
      authorVerified: false,
      contentType: "SHORT_VIDEO",
      languageCode: "hi",
      category: "Entertainment",
      title: "Mumbai Dance Reel",
      body: "New monsoon choreography in Hindi!",
      mediaAssetKey: "reel_stage",
      mediaKind: "VIDEO",
      durationSeconds: 30,
      musicTrack: "Monsoon Beats",
      videoFilter: "Saffron Glow",
      videoSpeed: "1x",
      overlayCaption: "Monsoon Vibes",
      hashtags: ["#SyloReels", "#Hindi"],
      visibility: "public",
      featured: false,
      likesCount: 0,
      commentsCount: 0,
      sharesCount: 0,
      viewsCount: 1,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Filtered query matches rule; unfiltered list query is denied
  await assertSucceeds(bobDb.collection("posts").where("visibility", "==", "public").get());
  await assertFails(bobDb.collection("posts").get());

  // Bob can increment engagement counters, cannot edit Alice's body
  await assertSucceeds(
    bobDb.collection("posts").doc("post_1").update({
      likesCount: 1,
      updatedAt: pastDate(1000),
    })
  );
  await assertFails(
    bobDb.collection("posts").doc("post_1").update({
      title: "Hijacked title",
      updatedAt: pastDate(1000),
    })
  );

  // Comment on existing post succeeds; comment on missing post fails
  await assertSucceeds(
    bobDb.collection("posts").doc("post_1").collection("comments").doc("c_1").set({
      postId: "post_1",
      authorId: BOB_UID,
      authorName: "Meera",
      languageCode: "mr",
      text: "खूप छान!",
      visibility: "public",
      createdAt: pastDate(1000),
    })
  );
  await assertFails(
    bobDb.collection("posts").doc("missing_post").collection("comments").doc("c_2").set({
      postId: "missing_post",
      authorId: BOB_UID,
      authorName: "Meera",
      languageCode: "mr",
      text: "Orphan comment",
      visibility: "public",
      createdAt: pastDate(1000),
    })
  );
});

test("Live video streams support viewer engagement, live chat/gifts, and terminal ENDED locking", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = pastDate(5000);

  await assertSucceeds(
    aliceDb.collection("live_streams").doc("live_1").set({
      hostId: ALICE_UID,
      hostName: "Aarav Live",
      hostHandle: "@aarav_s",
      title: "Live Q&A + Hindi Unplugged",
      languageCode: "hi",
      category: "Entertainment",
      status: "LIVE",
      viewersCount: 1,
      likesCount: 0,
      giftsCoinsTotal: 0,
      moderationSlowMode: false,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Filtered LIVE list succeeds; unfiltered fails
  await assertSucceeds(bobDb.collection("live_streams").where("status", "==", "LIVE").get());
  await assertFails(bobDb.collection("live_streams").get());

  // Bob sends a live gift and updates gift total
  await assertSucceeds(
    bobDb.collection("live_streams").doc("live_1").collection("messages").doc("lm_1").set({
      streamId: "live_1",
      senderId: BOB_UID,
      senderName: "Meera",
      text: "Sent Marigold Crown!",
      messageType: "GIFT",
      giftAmountCoins: 50,
      languageCode: "en",
      visibility: "public",
      createdAt: pastDate(1000),
    })
  );
  await assertSucceeds(
    bobDb.collection("live_streams").doc("live_1").update({
      giftsCoinsTotal: 50,
      likesCount: 5,
      updatedAt: pastDate(1000),
    })
  );

  // Host ends the stream
  await assertSucceeds(
    aliceDb.collection("live_streams").doc("live_1").update({
      status: "ENDED",
      updatedAt: pastDate(500),
    })
  );

  // Terminal state lock: once ENDED, non-admin cannot mutate stream
  await assertFails(
    aliceDb.collection("live_streams").doc("live_1").update({
      status: "LIVE",
      updatedAt: pastDate(200),
    })
  );
});

test("Conversations and denormalized chat messages strictly enforce memberUids", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const eveDb = testEnv.authenticatedContext("eve_outsider").firestore();
  const ts = pastDate(5000);

  await assertSucceeds(
    aliceDb.collection("conversations").doc("conv_1").set({
      title: "Aarav & Meera",
      groupChat: false,
      memberUids: [ALICE_UID, BOB_UID],
      memberNames: ["Aarav", "Meera"],
      lastMessage: "Namaste!",
      lastSenderId: ALICE_UID,
      requestStatus: "ACCEPTED",
      createdBy: ALICE_UID,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    aliceDb.collection("conversations").doc("conv_1").collection("messages").doc("msg_1").set({
      conversationId: "conv_1",
      senderId: ALICE_UID,
      senderName: "Aarav",
      memberUids: [ALICE_UID, BOB_UID],
      text: "Namaste!",
      messageKind: "TEXT",
      createdAt: ts,
    })
  );

  // Bob (member) can query & read; Eve (non-member) is blocked
  await assertSucceeds(bobDb.collection("conversations").where("memberUids", "array-contains", BOB_UID).get());
  await assertSucceeds(
    bobDb.collection("conversations").doc("conv_1").collection("messages")
      .where("memberUids", "array-contains", BOB_UID).get()
  );
  await assertFails(eveDb.collection("conversations").doc("conv_1").get());
  await assertFails(eveDb.collection("conversations").doc("conv_1").collection("messages").doc("msg_1").get());
});

test("Admin bootstrap verifies email_verified and protects moderation reports", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const spoofedAdminDb = testEnv.authenticatedContext("spoof_1", {
    email: "disopatil@gmail.com",
    email_verified: false,
  }).firestore();
  const realAdminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "disopatil@gmail.com",
    email_verified: true,
  }).firestore();
  const ts = pastDate(4000);

  // Alice files a report
  await assertSucceeds(
    aliceDb.collection("reports").doc("rep_1").set({
      reporterId: ALICE_UID,
      targetType: "POST",
      targetId: "post_1",
      targetTitle: "Spam Reel",
      reason: "Misleading promotion",
      status: "OPEN",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Spoofed unverified admin email is rejected
  await assertFails(
    spoofedAdminDb.collection("reports").doc("rep_1").update({
      status: "RESOLVED",
      updatedAt: pastDate(1000),
    })
  );

  // Verified admin can list and resolve reports
  await assertSucceeds(realAdminDb.collection("reports").get());
  await assertSucceeds(
    realAdminDb.collection("reports").doc("rep_1").update({
      status: "RESOLVED",
      updatedAt: pastDate(1000),
    })
  );
});

test("User notifications and payouts enforce strict owner isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = pastDate(3000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("notifications").doc("n_1").set({
      category: "LIVE",
      title: "Creator went live",
      body: "Tune in now!",
      read: false,
      createdAt: ts,
    })
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("notifications").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("notifications").get());

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("payouts").doc("pay_1").set({
      amountInr: 1500,
      source: "Ad Revenue + Live Gifts",
      upiOrBankMasked: "aarav@upi",
      status: "PENDING",
      createdAt: ts,
      updatedAt: ts,
    })
  );
  // User cannot directly mark payout PAID
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("payouts").doc("pay_1").update({
      status: "PAID",
      updatedAt: pastDate(1000),
    })
  );
});
