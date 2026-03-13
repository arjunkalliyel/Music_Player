🎵 Music Player App

A modern Android Music Player built using Kotlin, implementing MVVM architecture, real-time waveform visualization, and a fully functional 5-Band Equalizer with Bass & Treble control.


🚀 Highlights

✅ MVVM Architecture (Clean separation of concerns)

✅ Real-time audio waveform visualization

✅ Dynamic Circular Audio Visualizer (FFT based)

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

🎇 Real-time Audio Visualizer

- Uses Android's Visualizer API to extract real-time FFT (Fast Fourier Transform) data

- Dynamic circular visualizer (RoundVisualizerView) reacting to audio frequency and magnitude

- Synchronized, lag-free audio-visual experience

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

UI (Activities & Custom Views) ↓

ViewModel (State Management) ↓

MusicPlayerManager (Playback Engine & Visualizer API) ↓

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

- Handles the Visualizer instance for real-time FFT capture

- Exposes callbacks for UI updates

🧠 ViewModel

PlayerViewModel

- Manages UI state

- Emits playback progress using StateFlow

- Handles playback actions

🎨 UI

- MainActivity – Player screen

- MusicListActivity – Song library / track selection screen

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

> Visualizer (FFT)

- Custom Views:

> WaveformView

> RoundKnobView

> RoundVisualizerView

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

├── roundVisualizerView/

│   └── RoundVisualizerView.kt
│

├── MusicPlayerManager.kt

├── SplashActivity.kt

├── MusicListActivity.kt

├── MainActivity.kt

├── EqualizerActivity.kt

⚙ How It Works

🎵 Playback Flow

1. SplashActivity launches the app and handles initial setup.

2. MusicListActivity requests MusicRepository to load tracks from res/raw and displays the song library.

3. User selects a track from the library, passing the intent to MainActivity.

4. MusicPlayerManager prepares the MediaPlayer and initializes the Visualizer.

5. PlayerViewModel observes playback state and MainActivity updates UI accordingly.

6. WaveformExtractor processes audio data in the background and WaveformView renders it.

7. MusicPlayerManager captures real-time FFT data and triggers UI updates for the RoundVisualizerView.

8. EqualizerActivity can be launched to attach to the active audio session for effects processing.

🏗 Overall Architecture

SplashActivity

        ↓
MusicListActivity → Song Library Selection

        ↓
MainActivity

        ↓
MusicRepository → Loads Tracks

        ↓
MusicPlayerManager → Controls MediaPlayer & Visualizer

        ↓
PlayerViewModel → State & Progress

        ↓
WaveformExtractor → Audio Visualization

        ↓
RoundVisualizerView → Real-Time FFT Visualizer

        ↓
        
EqualizerActivity → Audio Effects



🔮 Future Improvements

- Playlist support

- Shuffle & Repeat modes

- MediaStore integration (device songs)

- ExoPlayer migration

- Dark / Light theme support

- Saving Equalizer presets

👨‍💻 Author

Arjun K A

Android Developer

