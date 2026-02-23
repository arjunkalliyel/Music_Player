🎵 Music Player App

A modern Android Music Player built using Kotlin, implementing MVVM architecture, real-time waveform visualization, and a fully functional 5-Band Equalizer with Bass & Treble control.


🚀 Highlights

✅ MVVM Architecture (Clean separation of concerns)

✅ Real-time audio waveform visualization

✅ 5-Band Equalizer with Presets

✅ Custom Rotary Knob UI Component

✅ StateFlow-based playback updates

✅ Proper MediaPlayer lifecycle handling

✅ Smooth, responsive UI



📱 Features
🎧 Core Playback

- Play / Pause music

- Next / Previous track navigation

- Seek through track

- Auto-play next song on completion

- Real-time playback progress updates

🖼 Metadata & UI

- Displays:

> Song title

> Artist name

> Album artwork (embedded)

- Clean modern UI design

- Splash screen on launch

🌊 Waveform Visualization

- Extracts waveform data from audio files

- Displays dynamic waveform bars

- Shows played vs unplayed portion visually

🎛 Equalizer System

- 5-band Equalizer:

> 60Hz

> 230Hz

> 1kHz

> 3.5kHz

> 10kHz

- Bass Boost (Rotary knob control)

- Treble control (Rotary knob)

- Built-in presets:

> Flat

> Rock

> Jazz

> Classical

> Pop

- Real-time audio effect updates using audioSessionId

🏗 Architecture

The project follows MVVM (Model–View–ViewModel) architecture.

UI (Activities) 

        ↓
ViewModel (State Management)

        ↓
      
MusicPlayerManager (Playback Engine)

        ↓
MediaPlayer (Android Framework)

Layers
📂 Model

- AudioTrack – Represents song metadata.

📦 Repository

MusicRepository

 - Loads songs from res/raw

 - Extracts metadata using MediaMetadataRetriever
 
 🎛 Manager

MusicPlayerManager

- Controls MediaPlayer

- Manages playback

- Handles track switching

- Exposes callbacks for UI updates

🧠 ViewModel

PlayerViewModel

- Manages UI state

- Emits playback progress using StateFlow

- Handles playback actions

🎨 UI

- MainActivity – Player screen

- EqualizerActivity – Audio effects screen

- SplashActivity – App launch screen

🛠 Tech Stack

- Language: Kotlin

- Architecture: MVVM

- UI: XML Layouts

- State Management: Kotlin StateFlow

- Coroutines: For background processing

- Audio Engine: Android MediaPlayer

- Audio Effects:

> Equalizer

> BassBoost

- Custom Views:

> WaveformView

> RoundKnobView

📁 Project Structure

com.example.musicplayer
│

├── model/

│   └── AudioTrack.kt
│

├── repository/

│   └── MusicRepository.kt
│

├── viewModel/

│   └── PlayerViewModel.kt
│

├── waveform/

│   ├── WaveformExtractor.kt

│   └── WaveformView.kt
│

├── roundKnobView/

│   └── RoundKnobView.kt
│

├── MusicPlayerManager.kt

├── MainActivity.kt

├── EqualizerActivity.kt

├── SplashActivity.kt

⚙ How It Works

🎵 Playback Flow

1. MusicRepository loads tracks from res/raw

2. MusicPlayerManager prepares MediaPlayer

3. PlayerViewModel observes playback state

4. MainActivity updates UI accordingly

5. WaveformExtractor processes audio data in background

6. WaveformView renders waveform

7. EqualizerActivity attaches to audio session

🏗 Overall Architecture

SplashActivity

        ↓
MainActivity

        ↓
MusicRepository → Loads Tracks

        ↓
MusicPlayerManager → Controls MediaPlayer

        ↓
PlayerViewModel → State & Progress

        ↓
WaveformExtractor → Audio Visualization

        ↓
EqualizerActivity → Audio Effects



🔮 Future Improvements

- Background playback service

- Notification controls

- Playlist support

- Shuffle & Repeat modes

- MediaStore integration (device songs)

- ExoPlayer migration

- Dark / Light theme support

- Saving Equalizer presets

👨‍💻 Author

Arjun K A

Android Developer

