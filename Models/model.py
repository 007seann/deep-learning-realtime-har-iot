from sklearn.metrics import mean_squared_error  # or any other metric you're interested in
import numpy as np
import pandas as pd
from scipy.fft import fft, ifft
from scipy.interpolate import interp1d
from scipy.stats import entropy, iqr, kurtosis, mode, skew
from sklearn.decomposition import PCA
import tensorflow_decision_forests as tfdf
from sklearn.metrics import classification_report, confusion_matrix, ConfusionMatrixDisplay, accuracy_score
from sklearn.model_selection import train_test_split, LeaveOneGroupOut, KFold
from sklearn.preprocessing import LabelEncoder, OneHotEncoder, StandardScaler
import tensorflow as tf, keras
from sklearn.ensemble import RandomForestClassifier
from tensorflow.keras.callbacks import EarlyStopping, ModelCheckpoint
from tensorflow.keras.layers import (Activation, BatchNormalization, Bidirectional,
                                     Conv1D, Conv2D, Dense, Dropout,
                                     Flatten, GlobalAveragePooling1D, LSTM,
                                     MaxPool1D, MaxPooling2D, Reshape,
                                     TimeDistributed)
from tensorflow.keras.models import Sequential
from tensorflow.keras.utils import to_categorical
import matplotlib.pyplot as plt
import os
import pickle
from pandas import read_csv, unique
from scipy.signal import argrelextrema, butter, find_peaks, lfilter, lfilter_zi
import time

# Files
X_respeck_path_task1 = "/Users/apple/Library/Mobile Documents/com~apple~CloudDocs/Year 3/Final-Project/0 Modules/IoT/CW3/dataset/X_respeck_phys"
y_respeck_path_task1 = "/Users/apple/Library/Mobile Documents/com~apple~CloudDocs/Year 3/Final-Project/0 Modules/IoT/CW3/dataset/y_respeck_phys"

X_respeck_path_task2 = "/Users/apple/Library/Mobile Documents/com~apple~CloudDocs/Year 3/Final-Project/0 Modules/IoT/CW3/dataset/X_respeck_resp"
y_respeck_path_task2 = "/Users/apple/Library/Mobile Documents/com~apple~CloudDocs/Year 3/Final-Project/0 Modules/IoT/CW3/dataset/y_respeck_resp"

X_respeck_path_task3 = "/Users/apple/Library/Mobile Documents/com~apple~CloudDocs/Year 3/Final-Project/0 Modules/IoT/CW3/dataset/X_respeck_all"
y_respeck_path_task3 = "/Users/apple/Library/Mobile Documents/com~apple~CloudDocs/Year 3/Final-Project/0 Modules/IoT/CW3/dataset/y_respeck_all"

model_keras_task1 = "/Users/apple/Library/Mobile Documents/com~apple~CloudDocs/Year 3/Final-Project/0 Modules/IoT/PDIoT-cw3-q1-1/Models/Trained_Task1_2.keras"

class ModelEnsemble:
    def __init__(self, X_respeck_path, y_respeck_path):
        self.X_respeck = pickle.load(open(X_respeck_path, "rb"))
        self.y_respeck = pickle.load(open(y_respeck_path, "rb"))
        self.num_of_students = 45
        self.rf_model = None
        self.cnn_lstm = None
        self.enc = None

    def one_hot_encode(self, y_train, y_test):
        self.enc = OneHotEncoder(handle_unknown='ignore', sparse=False)
        self.enc = self.enc.fit(np.array(y_train))
        return self.enc.transform(y_train), self.enc.transform(y_test)

    def train_rf(self, X_train, y_train):
        self.rf_model = RandomForestClassifier(random_state=42)
        X_train_rf = X_train.reshape(X_train.shape[0], -1)
        # self.rf_model.compile(metrics=['accuracy'])
        self.rf_model.fit(X_train_rf, y_train)

    def train_cnn_lstm_task1_1(self, X_train, y_train):
        # Outcome: 92%
        print("X_train.shape", X_train.shape)
        self.cnn_lstm = Sequential()
        self.cnn_lstm.add(LSTM(32, return_sequences=True, input_shape=X_train[0].shape, activation='relu'))
        self.cnn_lstm.add(LSTM(32, return_sequences=True, activation='relu'))

        self.cnn_lstm.add(Conv1D(filters=64, kernel_size=2, activation='relu', strides=2))
        self.cnn_lstm.add(MaxPool1D(pool_size=4, padding='same'))
        self.cnn_lstm.add(Conv1D(filters=192, kernel_size=2, activation='relu', strides=1))
        self.cnn_lstm.add(GlobalAveragePooling1D())
        self.cnn_lstm.add(BatchNormalization(epsilon=1e-06))
        self.cnn_lstm.add(Dense(12))
        self.cnn_lstm.add(Activation('softmax'))
        self.cnn_lstm.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])
        self.cnn_lstm.fit(X_train, y_train, epochs=25, batch_size=64, verbose=1)
        print(self.cnn_lstm.summary())
        
        
    def train_cnn_lstm_task2(self, X_train, y_train):
        # Outcome: 90%
        print("X_train.shape", X_train.shape)
        self.cnn_lstm = Sequential()
        self.cnn_lstm.add(LSTM(32, return_sequences=True, input_shape=X_train[0].shape, activation='relu'))
        self.cnn_lstm.add(LSTM(32, return_sequences=True, activation='relu'))
        self.cnn_lstm.add(Conv1D(filters=64, kernel_size=2, activation='relu', strides=2))
        self.cnn_lstm.add(MaxPool1D(pool_size=4, padding='same'))
        self.cnn_lstm.add(Conv1D(filters=192, kernel_size=2, activation='relu', strides=1))
        self.cnn_lstm.add(GlobalAveragePooling1D())
        self.cnn_lstm.add(BatchNormalization(epsilon=1e-06))
        self.cnn_lstm.add(Dense(18)) 
        self.cnn_lstm.add(Activation('softmax'))
        self.cnn_lstm.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])
        self.cnn_lstm.fit(X_train, y_train, epochs=25, batch_size=64, verbose=1)
        print(self.cnn_lstm.summary())
        
    def train_cnn_lstm_task3(self, X_train, y_train):
        # Average Ensemble Accuracy: 0.8208804523424879, training_time: 1222.2950670719147
        print("X_train.shape", X_train.shape)
        self.cnn_lstm = Sequential()
        self.cnn_lstm.add(LSTM(64, return_sequences=True, input_shape=X_train[0].shape, activation='relu'))
        self.cnn_lstm.add(LSTM(64, return_sequences=True, activation='relu'))
        self.cnn_lstm.add(Conv1D(filters=64, kernel_size=2, activation='relu', strides=2))
        self.cnn_lstm.add(MaxPool1D(pool_size=4, padding='same'))
        self.cnn_lstm.add(Conv1D(filters=192, kernel_size=2, activation='relu', strides=1))
        self.cnn_lstm.add(GlobalAveragePooling1D())
        self.cnn_lstm.add(BatchNormalization(epsilon=1e-06))
        self.cnn_lstm.add(Dense(38))
        self.cnn_lstm.add(Activation('softmax'))
        self.cnn_lstm.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])
        self.cnn_lstm.fit(X_train, y_train, epochs=50, batch_size=64, verbose=1)
        print(self.cnn_lstm.summary())

    def evaluate_ensemble_task2_LOO(self):
        kf = KFold(n_splits=self.num_of_students, shuffle=True, random_state=42)
        ensemble_accuracies = []

        for train_index, test_index in kf.split(self.X_respeck):
            X_train_loo, X_test_loo = self.X_respeck[train_index], self.X_respeck[test_index]
            y_train_loo, y_test_loo = self.y_respeck[train_index], self.y_respeck[test_index]

            y_train_loo, y_test_loo = self.one_hot_encode(y_train_loo, y_test_loo)

            self.train_rf(X_train_loo, y_train_loo)
            self.train_cnn_lstm_task2(X_train_loo, y_train_loo)

            X_test_rf_loo = X_test_loo.reshape(X_test_loo.shape[0], -1)
            y_pred_rf_loo = self.rf_model.predict(X_test_rf_loo)
            y_pred_cnn_lstm_loo = 1.3 * self.cnn_lstm.predict(X_test_loo)
            y_pred_ensemble_loo = np.mean(np.array([y_pred_cnn_lstm_loo, y_pred_rf_loo]), axis=0)
            y_pred_ensemble_loo = np.argmax(y_pred_ensemble_loo, axis=1)
            # for i, p in enumerate(y_pred_ensemble_loo):
            #     if p == 10 or p == 11:
            #         y_pred_ensemble_loo[i] = np.argmax(y_pred_rf_loo[i], axis=1)
            #     if y_pred_ensemble_loo[i] == 0:
            #         y_pred_ensemble_loo[i] = np.argmax(y_pred_cnn_lstm_loo[i])

            y_true_loo = np.argmax(y_test_loo, axis=1)
            ensemble_accuracies.append(accuracy_score(y_true_loo, y_pred_ensemble_loo))

        return np.mean(ensemble_accuracies)
    
    def evaluate_ensemble_task3_LOO(self):
        kf = KFold(n_splits=self.num_of_students, shuffle=True, random_state=42)
        ensemble_accuracies = []

        for train_index, test_index in kf.split(self.X_respeck):
            X_train_loo, X_test_loo = self.X_respeck[train_index], self.X_respeck[test_index]
            y_train_loo, y_test_loo = self.y_respeck[train_index], self.y_respeck[test_index]

            y_train_loo, y_test_loo = self.one_hot_encode(y_train_loo, y_test_loo)

            self.train_rf(X_train_loo, y_train_loo)
            self.train_cnn_lstm_task3(X_train_loo, y_train_loo)

            X_test_rf_loo = X_test_loo.reshape(X_test_loo.shape[0], -1)
            y_pred_rf_loo = self.rf_model.predict(X_test_rf_loo)
            y_pred_cnn_lstm_loo = 1.3 * self.cnn_lstm.predict(X_test_loo)
            y_pred_ensemble_loo = np.mean(np.array([y_pred_cnn_lstm_loo, y_pred_rf_loo]), axis=0)
            y_pred_ensemble_loo = np.argmax(y_pred_ensemble_loo, axis=1)
            # for i, p in enumerate(y_pred_ensemble_loo):
            #     if p == 10 or p == 11:
            #         y_pred_ensemble_loo[i] = np.argmax(y_pred_rf_loo[i], axis=1)
            #     if y_pred_ensemble_loo[i] == 0:
            #         y_pred_ensemble_loo[i] = np.argmax(y_pred_cnn_lstm_loo[i])

            y_true_loo = np.argmax(y_test_loo, axis=1)
            ensemble_accuracies.append(accuracy_score(y_true_loo, y_pred_ensemble_loo))

        return np.mean(ensemble_accuracies)

    def evaluate_ensemble_task1(self):
        start_time = time.time()
        X_train, X_test, y_train, y_test = train_test_split(self.X_respeck, self.y_respeck, test_size=0.2, random_state=42)
        y_train, y_test = self.one_hot_encode(y_train, y_test)
		
        self.train_rf(X_train, y_train)
        self.train_cnn_lstm_task1(X_train, y_train)

        X_test_rf = X_test.reshape(X_test.shape[0], -1)
        y_pred_rf = self.rf_model.predict(X_test_rf)
        y_pred_cnn_lstm = 1.3 * self.cnn_lstm.predict(X_test)
        y_pred_ensemble = np.mean(np.array([y_pred_cnn_lstm, y_pred_rf]), axis=0)
        y_pred_ensemble = np.argmax(y_pred_ensemble, axis=1)
        # for i, p in enumerate(y_pred_ensemble):
        #     if p == 10 or p == 11:
        #         y_pred_ensemble[i] = np.argmax(y_pred_rf[i], axis=1)
        #     if y_pred_ensemble[i] == 0:
        #         y_pred_ensemble[i] = np.argmax(y_pred_cnn_lstm[i])

        y_true = np.argmax(y_test, axis=1)
        end_time = time.time()
        training_time = end_time - start_time
        print("training_time:", training_time)
        return accuracy_score(y_true, y_pred_ensemble)
    
    def evaluate_ensemble_task1_2(self, model):
        X_train, X_test, y_train, y_test = train_test_split(self.X_respeck, self.y_respeck, test_size=0.2, random_state=42)
        y_train, y_test = self.one_hot_encode(y_train, y_test)
        trained_model = keras.models.load_model(model)
        return trained_model.evaluate(X_test, y_test)
        
    

    
if __name__ == "__main__":
    start_time_task1 = time.time()
    model = ModelEnsemble(X_respeck_path_task1, y_respeck_path_task1)
    avg_accuracy_task1 = model.evaluate_ensemble_task1_2(model_keras_task1)
    end_time_task1 = time.time()
    
    # start_time_task2 = time.time()
    # ensemble = ModelEnsemble(X_respeck_path_task2, y_respeck_path_task2)
    # avg_accuracy_task2 = ensemble.evaluate_ensemble_task2_LOO()
    # end_time_task2 = time.time()
    
    # start_time_task3 = time.time()
    # ensemble = ModelEnsemble(X_respeck_path_task3, y_respeck_path_task3)
    # avg_accuracy_task3 = ensemble.evaluate_ensemble_task3_LOO()
    # end_time_task3 = time.time()
    
    print(f"Training Time_task1: {end_time_task1 - start_time_task1}")
    # print(f"Average Ensemble Accuracy_LOO_task2: {avg_accuracy_task2}")
    # print(f"Training Time_LOO_task2: {end_time_task2 - start_time_task2}")
    # print(f"Average Ensemble Accuracy_LOO_task2: {avg_accuracy_task3}")
    # print(f"Training Time_LOO_task3: {end_time_task3 - start_time_task3}")