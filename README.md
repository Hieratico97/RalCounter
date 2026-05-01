# RalCounter (Ral, Monsoon Mage)

RalCounter is an advanced Android utility application engineered specifically for *Magic: The Gathering* players utilizing **Ral, Monsoon Mage** / **Ral, Leyline Prodigy** mechanics. 

This application provides a comprehensive suite of tools designed to accurately track complex board states, manage probabilistic events (coin flips), and calculate cumulative interactions such as Storm count and spell sequencing, thereby optimizing gameplay efficiency and precision.

## 🌟 Key Features

### 💧 Complete Mana Pool Management
- Track Blue, Red, and Generic mana pools independently.
- Quickly increment or decrement mana with dedicated buttons.
- "Spend" functionality to instantly zero out mana pools after casting large spells.

### ⚡ Tracking Storm & Spells
- **Storm Count:** Keep track of your Storm count for your payoff spells.
- **Instants/Sorceries Cast:** Uniquely tracks the number of Instants and Sorceries cast to know exactly what Ral's loyalty might be upon transformation, or just for tracking prowess-style triggers.

### 🪙 Coin Flip Simulator
- Built-in coin flip mechanic tailored specifically for Ral completely replacing the need for physical coins.
- Instantly win (Heads) or lose (Tails) the flip.
- **Life Total Tracking:** Automatically subtracts life when you lose a flip (Ral deals 1 damage to you).
- **Transform Prompts:** Clearly indicates when you've won a flip and can transform Ral into his Planeswalker form.

### 📖 Flip Log & Statistics
- Comprehensive history of all your coin flips during the game.
- Real-time statistics showing total flips, wins (Heads), losses (Tails), and your overall win percentage.

### ❤️ Life Tracking
- Support for both **Normal (20 Life)** and **Commander (40 Life)** game formats.
- Easily add or subtract life.

### 📱 Responsive UI
- **Portrait & Landscape Support:** Carefully designed layouts for both orientations, ensuring all information is visible without scrolling, maximizing the use of screen real estate depending on how you place your device on the playmat.
- **Multilingual:** Supports English and Spanish.

## 🛠️ Technical Stack & Architecture

- **Language:** Kotlin
- **Architecture:** MVVM (Model-View-ViewModel) utilizing Android's standard Architecture Components (`ViewModel`, `LiveData`).
- **UI:** ViewBinding with responsive XML layouts (`ConstraintLayout`, `LinearLayout`, `ScrollView`). Split landscape layouts designed for maximum visibility.
- **Navigation:** Android Navigation Component with a Bottom Navigation Bar.
- **Minimum SDK:** 26 (Android 8.0)
- **Target SDK:** 35 (Android 15)

## 🚀 Getting Started

To build and run the project locally:

1. Clone the repository: `git clone https://github.com/Hieratico97/RalCounter.git`
2. Open the project in **Android Studio**.
3. Let Gradle sync and resolve all dependencies.
4. Build and run the app on an emulator or physical device via the Run button.

## 🤝 Contributing

Contributions, issues, and feature requests are welcome! 
Feel free to check out the [issues page](https://github.com/Hieratico97/RalCounter/issues) if you want to contribute.

## 📄 License

This project is open-source and available for any non-commercial use. Magic: The Gathering and all related terminology are trademarks of Wizards of the Coast LLC. This application is unaffiliated with Wizards of the Coast.
