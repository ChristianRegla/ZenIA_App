# ZenIA
> **PINZANA:** Psychological Integration & Networking for ZenIA Awareness & Nexus Application

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-039BE5?style=for-the-badge&logo=Firebase&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Gemini AI](https://img.shields.io/badge/Gemini%201.5%20Flash-8E75B2?style=for-the-badge&logo=google&logoColor=white)

---

## 📖 Acerca del Proyecto

**ZenIA** (Tu aliado emocional) es una aplicación móvil nativa para Android diseñada como una solución tecnológica de "primeros auxilios emocionales". Su propósito es democratizar el acceso a herramientas de salud mental, fungiendo como un instrumento de autoayuda para el usuario y un recurso complementario para la práctica clínica.

Este proyecto aborda la alta prevalencia de ansiedad y la falta de cobertura en servicios de salud mental, ofreciendo un espacio seguro, privado y accesible 24/7.

> ⚠️ **Disclaimer:** ZenIA es una herramienta de apoyo y acompañamiento. No sustituye la terapia psicológica profesional ni el tratamiento psiquiátrico.

---

## ✨ Funcionalidades Principales

* 🤖 **Chatbot de Apoyo Emocional (IA):** Asistente impulsado por **Gemini 2.5 Flash** (vía Vertex AI) que ofrece conversación empática y contextual en tiempo real, operando bajo guiones validados clínicamente.
* 📝 **Diario de Bienestar Inteligente:** Registro diario de estado de ánimo y notas personales con retroalimentación automatizada basada en patrones emocionales.
* ❤️ **Integración Biométrica:** Sincronización con **Health Connect** para leer datos fisiológicos (como frecuencia cardíaca) de *wearables* compatibles y correlacionarlos con el estado de ánimo.
* 🧘 **Módulo de Relajación:** Biblioteca curada de ejercicios de respiración guiada y *mindfulness*, apoyada con animaciones visuales (**Lottie**) para reducir la carga cognitiva.
* 📞 **Directorio Profesional:** Acceso directo a líneas de ayuda y contacto con profesionales de la salud mental.

---

## 🛠️ Stack Tecnológico

El proyecto está construido sobre una arquitectura moderna, escalable y segura.

### 📱 Cliente Móvil (Frontend)
* **Lenguaje:** Kotlin
* **UI Framework:** Jetpack Compose (Material Design 3)
* **Navegación:** Jetpack Navigation Compose
* **Seguridad:** Credential Manager (Google ID)
* **Sensores:** Health Connect API
* **Animaciones:** Lottie

### ☁️ Infraestructura (Backend Serverless)
* **Plataforma:** Google Firebase
* **Base de Datos:** Cloud Firestore (NoSQL en tiempo real)
* **Autenticación:** Firebase Authentication
* **Lógica de Negocio:** Cloud Functions (Intermediario seguro para IA)
* **Monitoreo:** Firebase Crashlytics

### 🧠 Inteligencia Artificial
* **Modelo:** Gemini 2.5 Flash
* **Plataforma:** Google Cloud Vertex AI

---

## 🚀 Instalación y Configuración

Para ejecutar este proyecto localmente, necesitarás Android Studio Ladybug o superior.

1.  **Clonar el repositorio:**
    ```bash
    git clone [https://github.com/ChristianRegla/ZenIA_App.git](https://github.com/ChristianRegla/ZenIA_App.git)
    ```

2.  **Configurar Firebase:**
    * Crea un proyecto en la consola de Firebase.
    * Descarga el archivo `google-services.json`.
    * Colócalo en el directorio: `app/google-services.json`.

3.  **Configurar Variables de Entorno (local.properties):**
    Crea o edita el archivo `local.properties` en la raíz del proyecto y agrega tus claves (si aplica):
    ```properties
    sdk.dir=/ruta/a/tu/sdk
    # Agrega otras claves API si es necesario
    ```

4.  **Sincronizar y Ejecutar:**
    * Sincroniza el proyecto con Gradle.
    * Ejecuta la app en un emulador o dispositivo físico (Mínimo Android SDK 28).

---

## 👥 Creadores

Este proyecto fue desarrollado como parte de la titulación de Ingeniería en Desarrollo de Software en el **Centro de Enseñanza Técnica Industrial (CETI), Plantel Colomos**.

| **Christian Josue Regla Andrade** | **Alexis Osvaldo Villegas Ochoa** |
| :---: | :---: |
| **Project Manager & Lead Backend Dev** | **Lead AI Developer & UI/UX Designer** |
| [GitHub Profile](https://github.com/ChristianRegla) | [GitHub Profile](https://github.com/Alexis-Ochoa) |

<p align="center">
  Hecho con ❤️ en Guadalajara, Jalisco, México 🇲🇽 <br>
  2025
</p>
