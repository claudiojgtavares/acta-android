# ACTA — reuniões, áudio e revisão assistida

Aplicação Android em Kotlin/Jetpack Compose para registar reuniões, controlar consentimentos, gravar áudio, rever transcrições e produzir uma proposta estruturada de ata. O projeto combina Room, processamento local, áudio e uma integração opcional com Gemini.

> **Demo pública segura:** o aplicativo inicia com dados fictícios (`DemoDataProvider`) e não contém chaves, contas ou gravações reais. A IA só é usada quando uma chave é configurada localmente.

## Funcionalidades

- criação de reuniões e participantes;
- bloqueio da gravação quando falta consentimento;
- gravação e revisão de segmentos com rótulos de orador;
- geração de atas, deliberações, ações e versões;
- trilho de auditoria e exportação JSON;
- limpeza explícita dos dados locais;
- tema Compose e suporte a Android API 24+.

## Tecnologias e arquitetura

```text
Compose UI -> ActaViewModel -> ActaRepository -> Room DAOs
                                      |-> AudioRecorderManager
                                      |-> GeminiMinutesService (opcional)
```

O pacote público é `cv.claudiotavares.acta`. A aplicação não depende de um backend para a demonstração local.

## Executar

1. Abra o projeto no Android Studio recente.
2. Para a demo offline, mantenha a configuração de `DemoDataProvider` e não configure chaves.
3. Para testar a geração Gemini, copie `.env.example` para `.env` e forneça uma chave apenas no ambiente local. Nunca faça commit da chave.
4. Execute `./gradlew test` e, num dispositivo/emulador, `./gradlew assembleDebug`.

O APK distribuído deve ser um artefacto de debug/demo produzido a partir deste código. Instalação num dispositivo físico e publicação de Release dependem do ambiente Android disponível.

## English

ACTA is an Android Kotlin/Jetpack Compose application for meetings, consent, audio capture, transcript review and structured minutes. Room, local processing and an optional Gemini integration are separated behind a repository boundary.

The public demo uses fictional data only, contains no keys or real recordings, and starts offline through `DemoDataProvider`. Configure Gemini only in a local `.env` file. Run `./gradlew test` and `./gradlew assembleDebug` from Android Studio or a machine with the Android SDK.

## License

MIT — see [LICENSE](LICENSE).
