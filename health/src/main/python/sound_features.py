import numpy as np
from scipy import signal
from scipy.integrate import simps
from scipy.signal import butter, filtfilt


# noinspection PyTupleAssignmentBalance
class SoundFeaturesExtractor:
    sampleRate = None
    rawData = None
    fft = None
    frequencySpan = None
    xLabel = None
    FREQ_CUTS = [
        (0, 200),
        (300, 425),
        (500, 650),
        (950, 1150),
        (1400, 1800),
        (2300, 2400),
        (2850, 2950),
        (3800, 3900)
    ]

    def __init__(self, rawFloats, sr):
        self.rawData = rawFloats
        self.sampleRate = sr

    # noinspection PyTupleAssignmentBalance
    def signalPreprocessing(
            self,
            cutoffFrequency=4000,
            normalize=True,
            filter=True,
            downSample=True):
        fs_downsample = cutoffFrequency * 2
        if len(self.rawData.shape) > 1:
            self.rawData = np.mean(self.rawData, axis=1)
        if normalize:
            self.rawData = self.rawData / (np.max(np.abs(self.rawData)) + 1e-17)
        if filter:
            b, a = butter(4, fs_downsample / self.sampleRate, btype='lowpass')
            self.rawData = filtfilt(b, a, self.rawData)
        if downSample:
            self.rawData = signal.decimate(self.rawData, int(self.sampleRate / fs_downsample))
        self.sampleRate = fs_downsample

    def extractFeatures(self):
        baseData = (self.sampleRate, self.rawData)
        self.PRE = self.get_PRE(baseData)  # 1 +
        self.ZCR = self.get_ZCR(baseData)  # 1 +
        self.RMSP = self.get_RMSP(baseData)  # 1 +
        self.SF_SSTD = self.get_SF_SSTD(baseData)  # 2 +
        self.SSL_SD = self.get_SSL_SD(baseData)  # 2 +
        self.CF = self.get_CF(baseData)  # 1 +
        self.PSD = self.get_PSD(baseData)  # 8 +
        return self.getFeaturesVector()

    def getFeaturesVector(self):
        features = np.concatenate((
            [self.PRE],
            [self.ZCR],
            [self.RMSP],
            self.SF_SSTD,
            self.SSL_SD,
            [self.CF],
            self.PSD
        )).tolist()
        return features

    # Phase Power Ratio Estimation +
    def get_PRE(self, data):
        fs, cough = data
        phaseLen = int(cough.shape[0] // 3)
        P1 = cough[:phaseLen]
        P2 = cough[phaseLen:2 * phaseLen]
        P3 = cough[2 * phaseLen:]
        f = np.fft.fftfreq(phaseLen, 1 / fs)
        P1 = np.abs(np.fft.fft(P1)[:phaseLen])
        P2 = np.abs(np.fft.fft(P2)[:phaseLen])
        P3 = np.abs(np.fft.fft(P3)[:phaseLen])
        P2norm = P2 / (np.sum(P1) + 1e-17)
        fBin = fs / (2 * phaseLen + 1e-17)
        f750, f1k, f2k5 = int(-(-750 // fBin)), int(-(-1000 // fBin)), int(-(-2500 // fBin))
        ratio = np.sum(P2norm[f1k:f2k5]) / np.sum(P2norm[:f750])
        return ratio

    # Zero Crossing Rate +
    def get_ZCR(self, data):
        # data: wav file of segment; fs, signal = wavfile.read(file)
        # output: value of the feature
        names = ['Zero_Crossing_Rate']
        fs, cough = data
        ZCR = (np.sum(np.multiply(cough[0:-1], cough[1:]) < 0) / (len(cough) - 1))
        return ZCR

    # RMS Power +
    def get_RMSP(self, data):
        # data: wav file of segment; fs, signal = wavfile.read(file)
        # output: value of the feature
        names = ['RMS_Power']
        fs, cough = data
        RMS = np.sqrt(np.mean(np.square(cough)))
        return RMS

    # Spectral Flatness and spectral standard deviation +
    def get_SF_SSTD(self, data):
        # data: wav file of segment; fs, signal = wavfile.read(file)
        # output: value of the feature
        names = ['Spectral_Flatness', 'Spectral_StDev']
        fs, sig = data
        nperseg = min(900, len(sig))
        noverlap = min(600, int(nperseg / 2))
        freqs, psd = signal.welch(sig, fs, nperseg=nperseg, noverlap=noverlap)
        psd_len = len(psd)
        gmean = np.exp((1 / psd_len) * np.sum(np.log(psd + 1e-17)))
        amean = (1 / psd_len) * np.sum(psd)
        SF = gmean / amean
        SSTD = np.std(psd)
        return np.array([SF, SSTD])

    # Spectral Slope+ and Spectral Decrease+
    def get_SSL_SD(self, data):
        names = ['Spectral_Slope', 'Spectral_Decrease']
        b1 = 0
        b2 = 8000

        Fs, x = data
        s = np.absolute(np.fft.fft(x))
        s = s[:s.shape[0] // 2]
        muS = np.mean(s)
        f = np.linspace(0, Fs / 2, s.shape[0])
        muF = np.mean(f)

        bidx = np.where(np.logical_and(b1 <= f, f <= b2))
        slope = np.sum(((f - muF) * (s - muS))[bidx]) / np.sum((f[bidx] - muF) ** 2)

        k = bidx[0][1:]
        sb1 = s[bidx[0][0]]
        decrease = np.sum((s[k] - sb1) / (f[k] - 1 + 1e-17)) / (np.sum(s[k]) + 1e-17)
        return np.array([slope, decrease])

    # Crest Factor +
    def get_CF(self, data):
        """
        Compute the crest factor of the signal
        """
        fs, cough = data
        peak = np.amax(np.absolute(cough))
        RMS = np.sqrt(np.mean(np.square(cough)))
        return peak / RMS

    # Power spectral Density +
    def get_PSD(self, data):
        feat = []
        fs, sig = data
        nperseg = min(900, len(sig))
        noverlap = min(600, int(nperseg / 2))
        freqs, psd = signal.welch(sig, fs, nperseg=nperseg, noverlap=noverlap)
        dx_freq = freqs[1] - freqs[0]
        total_power = simps(psd, dx=dx_freq)
        for lf, hf in self.FREQ_CUTS:
            idx_band = np.logical_and(freqs >= lf, freqs <= hf)
            band_power = simps(psd[idx_band], dx=dx_freq)
            feat.append(band_power / total_power)
        feat = np.array(feat)
        return feat


def extractFeatures(dataFloatArray, sampleRate):
    extractor = SoundFeaturesExtractor(dataFloatArray, sampleRate)
    extractor.signalPreprocessing(cutoffFrequency=4000)
    return extractor.extractFeatures()
