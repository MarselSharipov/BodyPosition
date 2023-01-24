from scipy import signal as sc
import numpy as np

def cutAndResampleAudio(oldSampleRate, newSampleRate, cutLenSec, rawData):
    newSize = int(cutLenSec * oldSampleRate)
    return sc.resample(x=np.array(rawData[:newSize]), num=int(newSize * newSampleRate/oldSampleRate))

def cutAudio(cutLenSec, rawData, sampleRate):
    return rawData[:int(cutLenSec * sampleRate)]