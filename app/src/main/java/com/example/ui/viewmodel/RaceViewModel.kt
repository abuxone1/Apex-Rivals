package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.DriverProfile
import com.example.data.firebase.FirebaseAuthAndFirestoreService
import com.example.data.gemini.ChatMessage
import com.example.data.gemini.GeminiCoachModel
import com.example.data.gemini.GeminiImageService
import com.example.data.gemini.GeminiLiveVoiceService
import com.example.data.gemini.GeminiMusicService
import com.example.data.gemini.GeminiRaceEngineerService
import com.example.data.gemini.GeminiTranscribeService
import com.example.data.gemini.GeminiVideoService
import com.example.data.gemini.GeneratedLiveryResult
import com.example.data.gemini.GeneratedMusicTrack
import com.example.data.gemini.GeneratedVideoResult
import com.example.data.gemini.LiveVoiceMessage
import com.example.data.gemini.LyriaMusicModel
import com.example.data.gemini.VeoAspectRatio
import com.example.data.local.AppDatabase
import com.example.data.local.RacePerformanceMetric
import com.example.data.local.RaceResult
import com.example.data.local.RaceSessionEntity
import com.example.data.local.TelemetryConverters
import com.example.data.repository.RaceRepository
import com.example.engine.MultiplayerSyncEngine
import com.example.engine.RacingPhysicsEngine
import com.example.model.AVAILABLE_TRACKS
import com.example.model.AVAILABLE_VEHICLES
import com.example.model.GlobalRaceRanking
import com.example.model.LeaderboardEntry
import com.example.model.LiveRaceMetrics
import com.example.model.MultiplayerRacer
import com.example.model.MultiplayerRoom
import com.example.model.PlatformType
import com.example.model.RaceReplay
import com.example.model.SocialTelemetryPost
import com.example.model.TelemetrySnapshot
import com.example.model.Track
import com.example.model.Vehicle
import com.example.model.VehicleType
import com.example.util.RaceLapShareHelper
import com.example.util.ShareFormatPreset
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

data class RaceUiState(
  val liveMetrics: LiveRaceMetrics = LiveRaceMetrics(),
  val activeVehicle: Vehicle = AVAILABLE_VEHICLES[0],
  val activeTrack: Track = AVAILABLE_TRACKS[0],
  val isMph: Boolean = false,
  val isDrivingActive: Boolean = true,
  val playerThrottleInput: Float = 0f,
  val playerBrakeInput: Float = 0f,
  val playerSteerInput: Float = 0f,
  // Replay State
  val activeReplay: RaceReplay? = null,
  val isReplayPlaying: Boolean = false,
  val replayPlaybackSpeed: Float = 1.0f,
  val replayProgress: Float = 0f,
  val replayCamera: String = "Cockpit HUD",
  // Multiplayer State
  val multiplayerRooms: List<MultiplayerRoom> = emptyList(),
  val activeRoom: MultiplayerRoom? = null,
  val liveMultiplayerRacers: List<MultiplayerRacer> = emptyList(),
  val multiplayerEvents: List<String> = emptyList(),
  // Leaderboard State
  val selectedLeaderboardTrack: Track = AVAILABLE_TRACKS[0],
  val selectedVehicleFilter: VehicleType? = null,
  val selectedPlatformFilter: PlatformType? = null,
  val leaderboardEntries: List<LeaderboardEntry> = emptyList(),
  // Social State
  val socialPosts: List<SocialTelemetryPost> = emptyList(),
  val userSavedSessions: List<RaceSessionEntity> = emptyList(),
  val userRaceResults: List<RaceResult> = emptyList(),
  val shareDialogVisible: Boolean = false,
  // Gemini AI Coach State
  val aiChatMessages: List<ChatMessage> = emptyList(),
  val isAiGenerating: Boolean = false,
  // Livery Image Gen State (gemini-3.1-flash-image-preview)
  val generatedLiveries: List<GeneratedLiveryResult> = emptyList(),
  val isGeneratingLivery: Boolean = false,
  // Firebase Auth State
  val driverProfile: DriverProfile? = null,
  val isGoogleSigningIn: Boolean = false,
  val isSyncingToFirestore: Boolean = false,
  // Music Generation (Lyria 3)
  val generatedMusicTracks: List<GeneratedMusicTrack> = emptyList(),
  val isGeneratingMusic: Boolean = false,
  // Video Generation (Veo 3)
  val generatedVideos: List<GeneratedVideoResult> = emptyList(),
  val isGeneratingVideo: Boolean = false,
  // Live Voice Comms (Gemini 3.8 Live)
  val liveVoiceMessages: List<LiveVoiceMessage> = emptyList(),
  val isLiveVoiceActive: Boolean = false,
  // Audio Transcription (Gemini 3.5 Transcribe)
  val transcribedText: String? = null,
  val isTranscribing: Boolean = false,
  // Global Firebase Firestore Rankings
  val globalRankings: List<GlobalRaceRanking> = emptyList(),
  val isLoadingGlobalRankings: Boolean = false,
  val selectedRankingTrack: String = "All Tracks",
  val selectedRankingVehicleClass: String = "All Classes",
  val rankingSearchQuery: String = "",
  val firestoreSyncSuccessMessage: String? = null
)

class RaceViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: RaceRepository
  private val physicsEngine: RacingPhysicsEngine
  private var multiplayerSyncEngine: MultiplayerSyncEngine? = null

  private val geminiCoachService = GeminiRaceEngineerService()
  private val geminiImageService = GeminiImageService()
  private val geminiMusicService = GeminiMusicService()
  private val geminiVideoService = GeminiVideoService()
  private val geminiLiveVoiceService = GeminiLiveVoiceService()
  private val geminiTranscribeService = GeminiTranscribeService()
  private val firebaseService = FirebaseAuthAndFirestoreService(application)

  private val _uiState = MutableStateFlow(RaceUiState())
  val uiState: StateFlow<RaceUiState> = _uiState.asStateFlow()

  private var physicsJob: Job? = null
  private var replayJob: Job? = null

  init {
    val db = AppDatabase.getDatabase(application)
    repository = RaceRepository(db.raceDao(), db.raceResultDao(), db.racePerformanceMetricDao())
    physicsEngine = RacingPhysicsEngine(AVAILABLE_VEHICLES[0], AVAILABLE_TRACKS[0])

    val rooms = repository.getMultiplayerRooms()
    val initialRoom = rooms.first()
    multiplayerSyncEngine = MultiplayerSyncEngine(initialRoom)

    _uiState.update {
      it.copy(
        multiplayerRooms = rooms,
        activeRoom = initialRoom,
        liveMultiplayerRacers = initialRoom.racers,
        multiplayerEvents = multiplayerSyncEngine?.getRecentEvents() ?: emptyList(),
        socialPosts = repository.getSocialPosts(),
        leaderboardEntries = repository.getLeaderboard(it.selectedLeaderboardTrack.id)
      )
    }

    // Load initial sample replay for instant review
    val sampleReplay = repository.generateSampleReplay(
      AVAILABLE_TRACKS[0],
      AVAILABLE_VEHICLES[0],
      AVAILABLE_TRACKS[0].referenceLapTimeMs
    )
    _uiState.update { it.copy(activeReplay = sampleReplay) }
    physicsEngine.setGhost(sampleReplay.telemetryFrames)

    // Observe saved sessions from Room
    viewModelScope.launch {
      repository.allSessions.collect { sessions ->
        _uiState.update { it.copy(userSavedSessions = sessions) }
      }
    }

    // Observe saved race results from Room
    repository.allRaceResults?.let { flow ->
      viewModelScope.launch {
        flow.collect { results ->
          _uiState.update { it.copy(userRaceResults = results) }
        }
      }
    }

    // Observe Firebase Auth State
    viewModelScope.launch {
      firebaseService.observeAuthState().collect { profile ->
        _uiState.update { it.copy(driverProfile = profile) }
      }
    }

    loadGlobalRankings()
    observeGlobalRankings()

    startPhysicsLoop()
  }

  private fun startPhysicsLoop() {
    physicsJob?.cancel()
    physicsJob = viewModelScope.launch {
      val dt = 0.033f // ~30 fps update cycle for smooth state flow
      while (isActive) {
        val state = _uiState.value
        if (state.isDrivingActive) {
          val metrics = physicsEngine.update(
            dtSeconds = dt,
            throttle = state.playerThrottleInput,
            brake = state.playerBrakeInput,
            steer = state.playerSteerInput
          )

          // Update multiplayer sync with local racer's position
          val updatedRacers = multiplayerSyncEngine?.updateCompetitors(
            dtSeconds = dt,
            localPlayerProgress = metrics.trackProgress,
            localPlayerSpeed = metrics.speedKmh
          ) ?: emptyList()

          val events = multiplayerSyncEngine?.getRecentEvents() ?: emptyList()

          _uiState.update {
            it.copy(
              liveMetrics = metrics,
              liveMultiplayerRacers = updatedRacers,
              multiplayerEvents = events
            )
          }

          // Auto-save best laps into Room database
          if (metrics.lastLapTimeMs != null && metrics.lastLapTimeMs == metrics.bestLapTimeMs) {
            val frames = physicsEngine.getRecordedLapFrames()
            if (frames.isNotEmpty()) {
              saveLapToDatabase(metrics, frames)
            }
          }
        }
        delay(33L)
      }
    }
  }

  fun setThrottleInput(value: Float) {
    _uiState.update { it.copy(playerThrottleInput = value.coerceIn(0f, 1f)) }
  }

  fun setBrakeInput(value: Float) {
    _uiState.update { it.copy(playerBrakeInput = value.coerceIn(0f, 1f)) }
  }

  fun setSteerInput(value: Float) {
    _uiState.update { it.copy(playerSteerInput = value.coerceIn(-1f, 1f)) }
  }

  fun shiftUp() {
    physicsEngine.shiftUp()
  }

  fun shiftDown() {
    physicsEngine.shiftDown()
  }

  fun toggleDrsOrTuckIn() {
    physicsEngine.toggleDrsOrTuckIn()
  }

  fun toggleSpeedUnit() {
    _uiState.update { it.copy(isMph = !it.isMph) }
  }

  fun selectVehicle(vehicle: Vehicle) {
    physicsEngine.setVehicleAndTrack(vehicle, _uiState.value.activeTrack)
    _uiState.update { it.copy(activeVehicle = vehicle) }
  }

  fun selectTrack(track: Track) {
    physicsEngine.setVehicleAndTrack(_uiState.value.activeVehicle, track)
    val sampleReplay = repository.generateSampleReplay(
      track,
      _uiState.value.activeVehicle,
      track.referenceLapTimeMs
    )
    physicsEngine.setGhost(sampleReplay.telemetryFrames)
    _uiState.update {
      it.copy(
        activeTrack = track,
        activeReplay = sampleReplay,
        selectedLeaderboardTrack = track,
        leaderboardEntries = repository.getLeaderboard(track.id, it.selectedVehicleFilter, it.selectedPlatformFilter)
      )
    }
  }

  fun resetDriveSession() {
    physicsEngine.resetSession()
  }

  // --- REPLAY ENGINE CONTROLS ---

  fun toggleReplayPlay() {
    val willPlay = !_uiState.value.isReplayPlaying
    _uiState.update { it.copy(isReplayPlaying = willPlay) }
    if (willPlay) {
      startReplayLoop()
    } else {
      replayJob?.cancel()
    }
  }

  fun setReplayScrubProgress(progress: Float) {
    _uiState.update { it.copy(replayProgress = progress.coerceIn(0f, 1f)) }
  }

  fun setReplaySpeed(speed: Float) {
    _uiState.update { it.copy(replayPlaybackSpeed = speed) }
  }

  fun setReplayCamera(camera: String) {
    _uiState.update { it.copy(replayCamera = camera) }
  }

  fun loadReplay(replay: RaceReplay) {
    _uiState.update {
      it.copy(
        activeReplay = replay,
        replayProgress = 0f,
        isReplayPlaying = true
      )
    }
    physicsEngine.setGhost(replay.telemetryFrames)
    startReplayLoop()
  }

  private fun startReplayLoop() {
    replayJob?.cancel()
    replayJob = viewModelScope.launch {
      while (isActive && _uiState.value.isReplayPlaying) {
        val speed = _uiState.value.replayPlaybackSpeed
        val step = (0.005f * speed)
        val nextProgress = (_uiState.value.replayProgress + step)
        if (nextProgress >= 1.0f) {
          _uiState.update { it.copy(replayProgress = 0f) }
        } else {
          _uiState.update { it.copy(replayProgress = nextProgress) }
        }
        delay(33L)
      }
    }
  }

  // --- LEADERBOARD & MULTIPLAYER ---

  fun filterLeaderboard(track: Track, vehicleType: VehicleType?, platform: PlatformType?) {
    val entries = repository.getLeaderboard(track.id, vehicleType, platform)
    _uiState.update {
      it.copy(
        selectedLeaderboardTrack = track,
        selectedVehicleFilter = vehicleType,
        selectedPlatformFilter = platform,
        leaderboardEntries = entries
      )
    }
  }

  fun challengeLeaderboardRival(entry: LeaderboardEntry) {
    // Generate rival replay and load as ghost
    val rivalVehicle = AVAILABLE_VEHICLES.find { it.name.contains(entry.vehicleName.take(6), ignoreCase = true) }
      ?: AVAILABLE_VEHICLES[0]
    val rivalReplay = repository.generateSampleReplay(
      _uiState.value.selectedLeaderboardTrack,
      rivalVehicle,
      entry.lapTimeMs
    ).copy(
      playerName = entry.playerName,
      platform = entry.platform
    )
    loadReplay(rivalReplay)
  }

  fun selectMultiplayerRoom(room: MultiplayerRoom) {
    multiplayerSyncEngine = MultiplayerSyncEngine(room)
    _uiState.update {
      it.copy(
        activeRoom = room,
        liveMultiplayerRacers = room.racers,
        multiplayerEvents = multiplayerSyncEngine?.getRecentEvents() ?: emptyList()
      )
    }
  }

  // --- SOCIAL & SHARING ---

  fun toggleLikePost(postId: String) {
    _uiState.update { state ->
      val updated = state.socialPosts.map { post ->
        if (post.id == postId) post.copy(likesCount = post.likesCount + 1) else post
      }
      state.copy(socialPosts = updated)
    }
  }

  fun shareTelemetryCard(
    context: Context,
    lapTimeMs: Long,
    topSpeedKmh: Float,
    peakG: Float,
    vehicleName: String,
    trackName: String,
    vehicleType: VehicleType = VehicleType.CAR,
    sector1Ms: Long? = null,
    sector2Ms: Long? = null,
    sector3Ms: Long? = null,
    driverName: String = "Apex Driver"
  ) {
    RaceLapShareHelper.shareLapTime(
      context = context,
      driverName = driverName,
      trackName = trackName,
      vehicleName = vehicleName,
      vehicleType = vehicleType,
      lapTimeMs = lapTimeMs,
      topSpeedKmh = topSpeedKmh,
      sector1Ms = sector1Ms,
      sector2Ms = sector2Ms,
      sector3Ms = sector3Ms,
      peakG = peakG,
      platform = PlatformType.ANDROID,
      preset = ShareFormatPreset.SOCIAL_POST
    )
  }

  fun shareLapTime(
    context: Context,
    lapTimeMs: Long,
    topSpeedKmh: Float,
    vehicleName: String,
    trackName: String,
    vehicleType: VehicleType = VehicleType.CAR,
    sector1Ms: Long? = null,
    sector2Ms: Long? = null,
    sector3Ms: Long? = null,
    peakG: Float = 2.5f,
    driverName: String = "Apex Driver",
    customMessage: String = "",
    preset: ShareFormatPreset = ShareFormatPreset.SOCIAL_POST
  ) {
    RaceLapShareHelper.shareLapTime(
      context = context,
      driverName = driverName,
      trackName = trackName,
      vehicleName = vehicleName,
      vehicleType = vehicleType,
      lapTimeMs = lapTimeMs,
      topSpeedKmh = topSpeedKmh,
      sector1Ms = sector1Ms,
      sector2Ms = sector2Ms,
      sector3Ms = sector3Ms,
      peakG = peakG,
      platform = PlatformType.ANDROID,
      customMessage = customMessage,
      preset = preset
    )
  }

  private fun saveLapToDatabase(metrics: LiveRaceMetrics, frames: List<TelemetrySnapshot>) {
    viewModelScope.launch {
      val lapTime = metrics.lastLapTimeMs ?: metrics.currentLapTimeMs
      val entity = RaceSessionEntity(
        vehicleId = metrics.activeVehicle.id,
        vehicleName = metrics.activeVehicle.name,
        vehicleType = metrics.activeVehicle.type,
        trackId = metrics.activeTrack.id,
        trackName = metrics.activeTrack.name,
        lapTimeMs = lapTime,
        topSpeedKmh = metrics.topSpeedKmh,
        peakLateralG = metrics.peakLateralG,
        sector1Ms = metrics.sector1TimeMs ?: 0L,
        sector2Ms = metrics.sector2TimeMs ?: 0L,
        sector3Ms = metrics.sector3TimeMs ?: 0L,
        telemetryJson = TelemetryConverters.serializeTelemetryList(frames)
      )
      repository.saveRaceSession(entity)

      val raceResult = RaceResult(
        trackName = metrics.activeTrack.name,
        vehicleName = metrics.activeVehicle.name,
        lapTimeMs = lapTime,
        topSpeedKmh = metrics.topSpeedKmh
      )
      repository.saveRaceResult(raceResult)

      val performanceMetric = RacePerformanceMetric(
        trackName = metrics.activeTrack.name,
        lapTimeMs = lapTime,
        vehicleSpeed = metrics.topSpeedKmh,
        vehicleName = metrics.activeVehicle.name,
        driverName = "Apex Driver",
        lapNumber = metrics.currentLap,
        topSpeedKmh = metrics.topSpeedKmh,
        avgSpeedKmh = if (metrics.topSpeedKmh > 0) metrics.topSpeedKmh * 0.78f else metrics.speedKmh,
        peakAccelerationG = metrics.longitudinalG,
        maxLateralG = metrics.peakLateralG,
        sector1Ms = metrics.sector1TimeMs ?: 0L,
        sector2Ms = metrics.sector2TimeMs ?: 0L,
        sector3Ms = metrics.sector3TimeMs ?: 0L,
        raceMode = "Grand Prix"
      )
      repository.savePerformanceMetric(performanceMetric)
      // Sync to Firebase Firestore cloud
      firebaseService.syncLapToFirestore(raceResult)
    }
  }

  // Gemini AI Race Engineer Chat with Search & Maps Grounding
  fun sendAiCoachMessage(
    prompt: String,
    model: GeminiCoachModel = GeminiCoachModel.FLASH,
    enableSearch: Boolean = true,
    enableMaps: Boolean = false,
    role: com.example.data.gemini.ChatbotRole = com.example.data.gemini.ChatbotRole.CHIEF_ENGINEER
  ) {
    if (prompt.isBlank() || _uiState.value.isAiGenerating) return

    val userMessage = ChatMessage(
      role = "user",
      content = prompt
    )

    _uiState.update {
      it.copy(
        aiChatMessages = it.aiChatMessages + userMessage,
        isAiGenerating = true
      )
    }

    viewModelScope.launch {
      val response = geminiCoachService.sendMessage(
        history = _uiState.value.aiChatMessages,
        newPrompt = prompt,
        selectedModel = model,
        enableSearchGrounding = enableSearch,
        enableMapsGrounding = enableMaps,
        role = role
      )

      _uiState.update {
        it.copy(
          aiChatMessages = it.aiChatMessages + response,
          isAiGenerating = false
        )
      }
    }
  }

  // Livery Concept Generator with Gemini 3.1 Flash Image Preview
  fun generateLiveryConcept(prompt: String) {
    createLiveryImage(prompt, aspectRatio = "1:1", resolution = "1K")
  }

  // Create Image with gemini-3.1-flash-image-preview
  fun createLiveryImage(
    prompt: String,
    aspectRatio: String = "1:1",
    resolution: String = "1K"
  ) {
    if (prompt.isBlank() || _uiState.value.isGeneratingLivery) return

    _uiState.update { it.copy(isGeneratingLivery = true) }

    viewModelScope.launch {
      val result = geminiImageService.createImage(prompt, aspectRatio, resolution)
      _uiState.update {
        it.copy(
          generatedLiveries = listOf(result) + it.generatedLiveries,
          isGeneratingLivery = false
        )
      }
    }
  }

  // Edit Existing Image with gemini-3.1-flash-image-preview
  fun editLiveryImage(
    prompt: String,
    inputImageBase64: String,
    mimeType: String = "image/jpeg",
    aspectRatio: String = "1:1"
  ) {
    if (prompt.isBlank() || inputImageBase64.isBlank() || _uiState.value.isGeneratingLivery) return

    _uiState.update { it.copy(isGeneratingLivery = true) }

    viewModelScope.launch {
      val result = geminiImageService.editImage(prompt, inputImageBase64, mimeType, aspectRatio)
      _uiState.update {
        it.copy(
          generatedLiveries = listOf(result) + it.generatedLiveries,
          isGeneratingLivery = false
        )
      }
    }
  }

  // Lyria 3 Music Generation (Clip up to 30s or Pro Full Track)
  fun generateMusicTrack(
    prompt: String,
    model: LyriaMusicModel = LyriaMusicModel.CLIP
  ) {
    if (prompt.isBlank() || _uiState.value.isGeneratingMusic) return

    _uiState.update { it.copy(isGeneratingMusic = true) }

    viewModelScope.launch {
      val track = geminiMusicService.generateRacingMusic(prompt, model)
      _uiState.update {
        it.copy(
          generatedMusicTracks = listOf(track) + it.generatedMusicTracks,
          isGeneratingMusic = false
        )
      }
    }
  }

  // Veo 3 Video Generation from Text (16:9 or 9:16)
  fun generateVeoVideo(
    prompt: String,
    aspectRatio: VeoAspectRatio = VeoAspectRatio.LANDSCAPE_16_9
  ) {
    if (prompt.isBlank() || _uiState.value.isGeneratingVideo) return

    _uiState.update { it.copy(isGeneratingVideo = true) }

    viewModelScope.launch {
      val video = geminiVideoService.generateVideoFromText(prompt, aspectRatio)
      _uiState.update {
        it.copy(
          generatedVideos = listOf(video) + it.generatedVideos,
          isGeneratingVideo = false
        )
      }
    }
  }

  // Veo 3 Animate Image into Video (16:9 or 9:16)
  fun animateVeoImage(
    prompt: String,
    imageBase64: String,
    aspectRatio: VeoAspectRatio = VeoAspectRatio.LANDSCAPE_16_9
  ) {
    if (imageBase64.isBlank() || _uiState.value.isGeneratingVideo) return

    _uiState.update { it.copy(isGeneratingVideo = true) }

    viewModelScope.launch {
      val video = geminiVideoService.animateImageToVideo(prompt, imageBase64, aspectRatio)
      _uiState.update {
        it.copy(
          generatedVideos = listOf(video) + it.generatedVideos,
          isGeneratingVideo = false
        )
      }
    }
  }

  // Gemini 3.8 Live Voice Conversations (Live API)
  fun sendLiveVoiceComms(driverSpokenPrompt: String) {
    if (driverSpokenPrompt.isBlank() || _uiState.value.isLiveVoiceActive) return

    val driverMessage = LiveVoiceMessage(
      sender = "Driver (Pit Radio)",
      text = driverSpokenPrompt
    )

    _uiState.update {
      it.copy(
        liveVoiceMessages = it.liveVoiceMessages + driverMessage,
        isLiveVoiceActive = true
      )
    }

    viewModelScope.launch {
      val engineerReply = geminiLiveVoiceService.conductLiveVoiceTurn(
        driverSpokenPrompt = driverSpokenPrompt,
        conversationHistory = _uiState.value.liveVoiceMessages
      )

      _uiState.update {
        it.copy(
          liveVoiceMessages = it.liveVoiceMessages + engineerReply,
          isLiveVoiceActive = false
        )
      }
    }
  }

  // Audio Transcription with Gemini 3.5 Transcribe
  fun transcribePitRadioAudio(audioBytes: ByteArray, mimeType: String = "audio/mp4") {
    if (audioBytes.isEmpty() || _uiState.value.isTranscribing) return

    _uiState.update { it.copy(isTranscribing = true, transcribedText = null) }

    viewModelScope.launch {
      val resultText = geminiTranscribeService.transcribeAudio(audioBytes, mimeType)
      _uiState.update {
        it.copy(
          transcribedText = resultText,
          isTranscribing = false
        )
      }
    }
  }

  fun clearTranscribedText() {
    _uiState.update { it.copy(transcribedText = null) }
  }

  // Firebase Auth Handlers
  fun signInDriver() {
    viewModelScope.launch {
      val profile = firebaseService.signInAnonymously()
      _uiState.update { it.copy(driverProfile = profile) }
    }
  }

  // Google Sign-In with Firebase Auth via Credential Manager
  fun signInWithGoogle(context: android.content.Context, onComplete: ((Boolean) -> Unit)? = null) {
    if (_uiState.value.isGoogleSigningIn) return
    _uiState.update { it.copy(isGoogleSigningIn = true) }

    viewModelScope.launch {
      val profile = firebaseService.signInWithGoogle(context)
      _uiState.update {
        it.copy(
          driverProfile = profile,
          isGoogleSigningIn = false,
          firestoreSyncSuccessMessage = if (profile != null) "Signed in with Google! Cloud profile active." else null
        )
      }
      onComplete?.invoke(profile != null)
    }
  }

  // Sync user profile & local Room database records to Cloud Firestore
  fun syncUserDataToFirestore(context: android.content.Context) {
    val currentProfile = _uiState.value.driverProfile ?: return
    if (_uiState.value.isSyncingToFirestore) return

    _uiState.update { it.copy(isSyncingToFirestore = true) }

    viewModelScope.launch {
      val db = com.example.data.local.AppDatabase.getDatabase(context)
      val localMetrics = db.performanceMetricsDao().getAllList()
      val success = firebaseService.syncUserTelemetryToFirestore(currentProfile, localMetrics)

      _uiState.update {
        it.copy(
          isSyncingToFirestore = false,
          firestoreSyncSuccessMessage = if (success) "Firestore: All profile stats & telemetry synced to Cloud!" else "Firestore: Offline queue updated"
        )
      }
    }
  }

  fun signOutDriver() {
    viewModelScope.launch {
      firebaseService.signOut()
      _uiState.update { it.copy(driverProfile = null) }
    }
  }

  // --- FIREBASE FIRESTORE GLOBAL RANKINGS ---

  private var rankingsObserveJob: Job? = null

  fun loadGlobalRankings() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoadingGlobalRankings = true) }
      val rankings = firebaseService.fetchGlobalRankings(
        trackFilter = _uiState.value.selectedRankingTrack,
        vehicleClassFilter = _uiState.value.selectedRankingVehicleClass
      )
      val query = _uiState.value.rankingSearchQuery
      val filtered = if (query.isBlank()) rankings else {
        rankings.filter {
          it.driverName.contains(query, ignoreCase = true) ||
            it.trackName.contains(query, ignoreCase = true) ||
            it.vehicleName.contains(query, ignoreCase = true)
        }
      }
      _uiState.update {
        it.copy(
          globalRankings = filtered,
          isLoadingGlobalRankings = false
        )
      }
    }
  }

  fun observeGlobalRankings() {
    rankingsObserveJob?.cancel()
    rankingsObserveJob = viewModelScope.launch {
      firebaseService.observeGlobalRankings(_uiState.value.selectedRankingTrack).collect { list ->
        val query = _uiState.value.rankingSearchQuery
        val vehicleClass = _uiState.value.selectedRankingVehicleClass

        val filtered = list.filter { item ->
          val matchesSearch = query.isBlank() ||
            item.driverName.contains(query, ignoreCase = true) ||
            item.trackName.contains(query, ignoreCase = true) ||
            item.vehicleName.contains(query, ignoreCase = true)

          val matchesVehicle = when (vehicleClass) {
            "Cars Only", "GT3-R" -> item.vehicleType == VehicleType.CAR
            "Bikes Only", "Superbike 1000RR" -> item.vehicleType == VehicleType.MOTORBIKE
            else -> true
          }

          matchesSearch && matchesVehicle
        }

        _uiState.update {
          it.copy(
            globalRankings = filtered,
            isLoadingGlobalRankings = false
          )
        }
      }
    }
  }

  fun setSelectedRankingTrack(trackName: String) {
    _uiState.update { it.copy(selectedRankingTrack = trackName) }
    loadGlobalRankings()
    observeGlobalRankings()
  }

  fun setSelectedRankingVehicleClass(vehicleClass: String) {
    _uiState.update { it.copy(selectedRankingVehicleClass = vehicleClass) }
    loadGlobalRankings()
  }

  fun setRankingSearchQuery(query: String) {
    _uiState.update { it.copy(rankingSearchQuery = query) }
    loadGlobalRankings()
  }

  fun submitLapToFirestore(
    driverName: String,
    trackName: String,
    vehicleName: String,
    lapTimeMs: Long,
    topSpeedKmh: Float,
    platform: PlatformType = PlatformType.ANDROID
  ) {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoadingGlobalRankings = true) }
      val success = firebaseService.submitManualLap(
        driverName = driverName,
        trackName = trackName,
        vehicleName = vehicleName,
        lapTimeMs = lapTimeMs,
        topSpeedKmh = topSpeedKmh,
        platform = platform
      )
      if (success) {
        _uiState.update {
          it.copy(
            firestoreSyncSuccessMessage = "Telemetry lap successfully uploaded to Firestore!"
          )
        }
        loadGlobalRankings()
      } else {
        _uiState.update {
          it.copy(
            isLoadingGlobalRankings = false,
            firestoreSyncSuccessMessage = "Uploaded to local leaderboard queue (Firestore sync pending)"
          )
        }
      }
    }
  }

  fun dismissSyncMessage() {
    _uiState.update { it.copy(firestoreSyncSuccessMessage = null) }
  }
}

