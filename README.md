# siyukio-studio-samples

This project is an open-source example of autonomous software development powered by OpenAI Codex and orchestration tools like oh-my-codex.

## Quick Start

### Installation and Setup

```bash
npm install -g @openai/codex oh-my-codex
omx setup && omx doctor
```

### Copy Skills

```bash
cp -r ./skills/* ~/.codex/skills
```

### Execute Tasks

```bash
omx exec "task description"
```