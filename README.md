# RespFit: Real-Time Human Activity & Respiratory Symptom Classifier

## My Key Contributions

-  **Developed a real-time HAR classifier** using CNNs, LSTMs, and custom feature engineering (including magnitude, jerk, FFT).
-  Achieved a **91.8\% F1 score** via Leave-One-Out Cross-Validation (LOOCV) across 3 complex tasks (HAR, respiratory detection, joint classification).
-  Awarded **Distinction (90/100)** in project evaluation for technical execution and robustness.

---

## 1. Why did you start your project?

The project was initiated to build a real-time mobile application that performs Human Activity Recognition (HAR) and respiratory symptom classification using wearable sensors. With rising interest in health-monitoring IoT solutions, our goal was to go beyond simple step counting and accurately classify both physical activities (e.g., walking, lying down) and respiratory states (e.g., coughing, hyperventilating). We aimed to support non-intrusive and real-time health monitoring using affordable and accessible Android devices connected to RESpeck sensors.

---

## 2. What issues did you find technically and in a domain context?

### Domain Issues:
- HAR from a single accelerometer sensor lacks precision for fine-grained activity (especially respiratory) recognition.
- Some target classes (e.g., "other" respiratory symptoms) are vague or highly variable, complicating prediction.
- High inter-subject variability caused inconsistent results, especially with noisy sensor data or broken hardware.

### Technical Issues:
- High memory use and latency when running real-time classification and feature extraction on mobile devices.
- Difficulties integrating Python-based signal processing into an Android app.
- Model performance dropped when predicting complex joint activities (activity + respiration).
- Balancing prediction accuracy with real-time responsiveness in the mobile app.

---

## 3. What solutions did you consider?

- **Feature Extraction**: Engineered statistical, jerk, and FFT-based features from 50-sample windows.
- **Model Architectures**: Tested CNN, LSTM, and hybrid CNN-LSTM models.
- **Model Structures**: Compared flat classifiers vs. staged model architectures (physical activity first, then respiration).
- **Implementation Strategies**:
  - Native Kotlin feature extraction (too complex)
  - Remote server API (too slow due to latency)
  - **Embedded Python (Chaquopy)**: Chosen for its balance of speed and feature richness.
- **Preprocessing**: Applied signal smoothing and class balancing (e.g., downsampling dominant classes).
- **Evaluation**: Used LOOCV and 80/10/10 train-validation-test split with metrics including precision, recall, F1, and confusion matrices.

---

## 4. What is your final decision among solutions?

We finalized a mobile application with the following components:

-  **Mobile App**: Lightweight and intuitive Android interface built using a stripped-down skeleton app with fast navigation (3–6 clicks from login to classification).
-  **Feature Engineering**: Extracted 57 features (statistical + FFT + jerk) per window, optimized with PCA and random forest insights.
-  **Models**:
  - Task 1 (Physical): CNN model (91.0\% accuracy)
  - Task 2 (Respiratory): CNN-LSTM hybrid (77.6\% accuracy)
  - Task 3 (Combined): Staged CNN + Dense + CNN-LSTM pipeline
-  **Integration**: Embedded Python script via Chaquopy for local, low-latency execution (≤0.01s), balancing responsiveness with memory (~350MB).
-  **Database**: Google Firebase Realtime Database with hashed user credentials and smart activity logging via in-app caching.

With a top F1 score of 91.8\%, RespFit demonstrates that deep learning-based HAR can be effectively deployed on real devices, offering real-time feedback for users in medical, fitness, or assistive contexts.

