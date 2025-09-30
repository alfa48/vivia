# 📱 Vívia – Assistente Android com IA

**Vívia** é um **aplicativo Android** que integra **Inteligência Artificial** (via [OpenRouter API](https://openrouter.ai/)) para controlar recursos do dispositivo através de comandos em linguagem natural.  

---

## ✨ Funcionalidades
- **Chat com IA**: o usuário pode escrever mensagens como:  
  - *"Altera a luminosidade para 100%"*  
  - *"Define o volume para 5%"*  
- **Execução automática**: a IA interpreta o comando e retorna um JSON no formato:  
  ```json
  {"action":"set", "target":"brightness|volume", "value":N}

