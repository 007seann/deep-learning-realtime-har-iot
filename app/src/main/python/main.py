import numpy as np
from scipy.signal import argrelextrema, butter, find_peaks, lfilter, lfilter_zi
import pandas as pd
from sklearn.decomposition import PCA

def parse(x_string, y_string, z_string):
    x = np.array(x_string.split(",")).astype(np.float)
    y = np.array(y_string.split(",")).astype(np.float)
    z = np.array(z_string.split(",")).astype(np.float)
    print("Parsed x:", x)
    print("Parsed y:", y)
    print("Parsed z:", z)
    return x, y, z

def signal_smoothing(df, cutoff):
    # Define the filter parameters
    nyquist_freq = 0.5 * 25  # Nyquist frequency for a 25 Hz sample rate
    normalized_cutoff = cutoff / nyquist_freq

    # Filter order selection (adjust as needed)
    order = 4

    # Create a Butterworth low-pass filter
    b, a = butter(order, float(normalized_cutoff), btype='low')
    
    # Apply the filter to the data with initial conditions
    zi = lfilter_zi(b, a)
    filtered_data, _ = lfilter(b, a, df, zi=zi*df[0])
    
    return filtered_data

def base_feature_extraction(df):
    time_interval = 1/25 # 1/freq
    pca = PCA(n_components=1)

    df["accelX"] = signal_smoothing(df["accelX"], 2.25)
    df["accelY"] = signal_smoothing(df["accelY"], 2.25)
    df["accelZ"] = signal_smoothing(df["accelZ"], 2.25)

    df["accelMag"] = np.sqrt(df['accelX']**2 + df['accelY']**2 + df['accelZ']**2)
    
    # vector normalise 
    df["accelX"] = df["accelX"]/df['accelMag']
    df["accelY"] = df["accelY"]/df['accelMag']
    df["accelZ"] = df["accelZ"]/df['accelMag']
    
    max_jerk = 10
    # calculate jerk of data
    df["jerkX"] = np.clip(np.gradient(df["accelX"], time_interval), -max_jerk, max_jerk)
    df["jerkY"] = np.clip(np.gradient(df["accelY"], time_interval), -max_jerk, max_jerk)
    df["jerkZ"] = np.clip(np.gradient(df["accelZ"], time_interval), -max_jerk, max_jerk)
    df["jerkMag"] = np.sqrt(df['jerkX']**2 + df['jerkY']**2 + df['jerkZ']**2)
    
    # calculate most segnificant components
    df["pcaAccel"] = pca.fit_transform(df[["accelX","accelY","accelZ"]])
    df["pcaJerk"] = pca.fit_transform(df[["jerkX","jerkY","jerkZ"]])
    
    df = df.dropna()
    return df

def preprocess(x_string, y_string, z_string):
    x, y, z = parse(x_string, y_string, z_string)
    data = {
        "accelX": x,
        "accelY": y,
        "accelZ": z
    }
    df = pd.DataFrame(data)
    print(df)
    extracted_df = base_feature_extraction(df)
    print(extracted_df)
    return extracted_df.to_json(orient='records')
