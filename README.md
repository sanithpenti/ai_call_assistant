# Hands-Free AI Call Assistant 📞🤖

An automated incoming call agent that detects ringing calls, listens for local voice triggers, and handles conversations in real time.

> ⚠️ **Project Status:** Active Work-in-Progress (V0.1 Prototype)

---

## 🚀 Features Implemented
- [x] Native telephony state management (incoming call detection)
- [x] Local offline keyword spotting for hands-free answering
- [x] Integration with Speech-to-Text & ChatGPT APIs

## 🚧 Roadmap & Upcoming Features
- [ ] Low-latency WebSockets / WebRTC audio streaming
- [ ] On-device Voice Activity Detection (VAD) for instant interruptions
- [ ] Automated call hanging / intent action triggers

## 🛠️ Tech Stack
* **Language:** Python / Kotlin
* **Speech Processing:** Whisper STT, ElevenLabs / Cartesia TTS
* **LLM Engine:** OpenAI ChatGPT API

---

## ⚡ How to Run Locally

```bash
# Clone the repository
git clone [https://github.com/your-username/ai-voice-call-assistant.git](https://github.com/your-username/ai-voice-call-assistant.git)

# Install dependencies
pip install -r requirements.txt

# Add your environment variables (.env)
OPENAI_API_KEY="your-api-key-here"
