import pandas as pd
import scipy as sc
import numpy as np
import os

def re_sample(x, t, fs):
    #t -- ms
    x1 = np.asarray(x)
    t1 = np.asarray(t)
    t1 = t1.astype(float).astype(int)
    T = np.floor(1000/fs)#ms
    time_interval = np.array([0, T])
    aver = np.array([0, 0, 0])
    N = 0
    x2 = np.array([[0, 0, 0]])

    time = t1[1] - t1[0]
    for i in range(1, x1.shape[0]):
        if (time_interval[0] <= time) & (time < time_interval[1]):
            aver = aver + x1[i, 0:3]
            N = N + 1
            time = t1[i] - t1[0]
        else:
            if N != 0:
                x2 = np.concatenate((x2, [aver/N]), axis=0)
            else:
                zer = np.array([0, 0, 0])
                x2 = np.concatenate((x2, [zer]), axis=0)
            aver = np.array([0, 0, 0])
            N = 0
            time_interval = time_interval + T
    return x2

def isTremor(df, fs, n_freqs, thresholds, n_votes, is_calibration_on):

    #print(df)
    calib_list = [473.69813045, 668.48992644, 714.62926598, 627.61535883, 562.61314549,
                      643.33006101, 722.52935401, 620.89505825, 683.33300347, 624.13660066,
                      568.76095412, 780.11973657]
    calib_list_arr = np.array(calib_list)
    time_win_len = 10
    #print(df[1:10][1:10])
    #n_freqs -- num of control freqs
    #threshold -- n_freqs threshs for conting votes to detect tremor
    #n_votes -- num of votes to detect tremor freqs
    hz_tremor_diapazone = np.array([0, 12])
    #eng = matlab.engine.start_matlab()
    #dataSc = eng.ScDatasetToCSV()
    #print_hi(dataSc)
    cols = df.columns
    #print(cols)
    df_acc = df[cols[1:len(cols)]]
    #print(df_acc)
    np_acc = df_acc.to_numpy(dtype=float)
    #print(np_acc)
    df_time = df[cols[0]]
    #print(np_acc)
    #print(df_time)
    #x = re_sample(np_acc, df_time, fs)
    x = np_acc
    #print(x.shape)
    #print(x)
    if len(x) < int(fs*time_win_len):
        print('not enough data')
        return -1, -1, -1
    x = x[(len(x) - int(fs*time_win_len)):len(x), :]
    #print(x.shape)
    eukl_norm_x = np.sqrt(x[:,0]**2 + x[:,1]**2 + x[:,2]**2)

    #print(len(eukl_norm_x))
    X = sc.fft.fft(eukl_norm_x)
    amp_spec = np.abs(X)
    #print('amp_spec = ' + str(amp_spec))
    idx_tremor_diapazone = np.floor(hz_tremor_diapazone * len(amp_spec) / fs)
    idx_tremor_diapazone = idx_tremor_diapazone.astype(int)
    #print(idx_tremor_diapazone)
    A_tremor = amp_spec[idx_tremor_diapazone[0]:idx_tremor_diapazone[1]]
    f = np.linspace(0,n_freqs,n_freqs + 1)
    control_idxes = np.floor(f * len(amp_spec) / fs)
    control_idxes = control_idxes - control_idxes[0]
    control_idxes = control_idxes.astype(int)
    #print(control_idxes)
    #print(len(A_tremor))
    med = np.median(A_tremor)
    start_idx = 7

    is_tremor = int(0)
    energies = np.zeros(len(f) - 1)
    result = 0

    if is_calibration_on:
        #print(energies)
        #print(len(energies))
        #print(len(control_idxes))
        for i in range(0, len(energies)):
            start1 = control_idxes[i]
            if start1 < start_idx:
                start1 = start_idx
            A_tremor_on_diapazone = A_tremor[start1:control_idxes[i+1]]
            s = 0
            for j in range(0, len(A_tremor_on_diapazone)):
                s = s + A_tremor_on_diapazone[j]**2
            energies[i] = s/len(A_tremor_on_diapazone)
            #energies[i] = np.sum(A_tremor_on_diapazone**2)/len(A_tremor_on_diapazone)
        energies = energies - med#np.min(energies)
        energies = energies - thresholds
        #print(energies)
        #print(np.floor(energies))
        return is_tremor, energies
    else:
        A_tremor = A_tremor[start_idx:(len(A_tremor) - 1)]
        A_tremor = A_tremor
        #find nmax idxes
        idxes = np.argpartition(A_tremor, -n_votes)[-n_votes:]
        idxes = idxes.astype(int)
        #print(idxes)
        top = A_tremor[idxes]
        #print('top =' + str(top))

        up_thresholds = thresholds + 107.5
        thresholds2 = thresholds + 62.0

        #amps = [] #tremor amps
        n_votes_for_freq = np.zeros(n_freqs)
        n_votes_for_freq2 = np.zeros(n_freqs)
        for i in range(0, len(idxes)):
            #search freq_diap for idxes[i]
            i_diap = -1
            for j in range(0, len(n_votes_for_freq)):
                start1 = control_idxes[j]
                end1 = control_idxes[j+1]
                if start1 < start_idx:
                    start1 = start_idx
                if (start1 <= idxes[i]) & (idxes[i] < end1):
                    i_diap = j
                    break
            if (thresholds[i_diap] < A_tremor[idxes[i]]) & (A_tremor[idxes[i]] < up_thresholds[i_diap]):
                #amps = amps.append(A_tremor[idxes[i]])
                #print('idxes = ' + str(A_tremor[idxes[i]]))
                n_votes_for_freq[i_diap] = n_votes_for_freq[i_diap] + 1
            if thresholds2[i_diap] < A_tremor[idxes[i]]:
                n_votes_for_freq2[i_diap] = n_votes_for_freq2[i_diap] + 1

        print(n_votes_for_freq)
        print(n_votes_for_freq2)
        #print(amps)

        is_tremor = 0
        for n_votes in n_votes_for_freq:
            if n_votes > 1:
                #print('tremor')
                is_tremor = 1
                break

        #these may be other variants
        #find top1 freqs
#        for i in range(0, len(n_votes_for_freq)):
#         start1 = control_idxes[i]
#         end1 = control_idxes[i+1]
#         if start1 < start_idx:
#             start1 = start_idx
#         for j in range(0, len(idxes)):
#             if (start1 <= idxes[j]) & (idxes[j] < end1):
#                 if A_tremor[idxes[j]] > thresholds[i]:
#                     n_votes_for_freq[i] = n_votes_for_freq[i] + 1
        idxes = np.argpartition(n_votes_for_freq, -1)[-1:]
        #top = n_votes_for_freq[idxes]
        #print(top)
        #print(idxes)
        #print(control_idxes)
#         n_votes_for_all = 0
#         for i in range(0, len(n_votes_for_freq)):
#             if n_votes_for_freq[i] == 0:
#                 n_votes_for_all= n_votes_for_all + 1


        n_votes_for_all = int(np.sum(n_votes_for_freq))
#         if n_votes_for_all > 2:
#             is_tremor = int(idxes[0])

        #energy_calc #use x
        #slicer
        #df_acc
        #eukl_norm_x
#         n_partitions = 10
#         part_win_len = np.floor(len(eukl_norm_x)/n_partitions)
#
#         for i in range(0, len(eukl_norm_x), part_win_len):


        if ((1 <= idxes[0]) and (idxes[0] <= 9)) or ((9 <= idxes[0]) and (idxes[0] <= 11)):
            if (n_votes_for_freq2[0] < 2 and n_votes_for_freq2[0] < 2):
                if n_votes_for_all > 2:
                    is_tremor = 1
                else:
                    is_tremor = 0
            else:
                is_tremor = 0
        else:
            is_tremor = 0
        print(is_tremor)
        #is_tremor = int(len(n_votes_for_freq))
        is_movement = 0
    return is_tremor, is_movement, energies

def is_Tremor(csv_string):
    #print('tremor started')
    if csv_string == '':
        #print('string is empty')
        return -1
    #print(csv_string)
    columns = ['time', 'acc_x', 'acc_y', 'acc_z']
    #csv_StringIO = StringIO(csv_string)
    #df = pd.read_csv(csv_StringIO, sep=",", header=None)
    #print(csv_string)
    df = pd.DataFrame([row.split(',') for row in csv_string.split('\n')],
                       columns=columns)
    #print(df)
    #print(os.getcwd())
    #df.to_csv(str(datetime.now().timestamp()) + '.csv')
#     for root, dirs, files in os.walk("."):
#         print(dirs)
    #a = os.walk(".")
    #print(os.walk("."))
    #print('1')
    #df = pd.read_csv('./logs_1.txt')
    fs = 50#62.5
    n_freqs = 12
    sens = 18 #need to calibrate on the mobile
#     thresholds = np.array([200, 200, 200, 25, 25, 25, 25, 200, 200, 200, 200, 200])
#    thresholds = np.array([0, 0, 0, 0, 0, 0, 200, 200, 200, 200, 200, 200])
    thresholds = np.array([sens, sens, sens, sens, sens, sens, sens, sens, sens, sens, sens, sens])
    #amend1 = np.array([0, 0, 0, 0, 0, 0, 4, 8, 12, 16, 20, 24])
    amend1 = np.array([0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0])
    thresholds = thresholds + amend1
    n_votes = 100
    is_calibration_on = 0
    is_tremor, is_movement, energies = isTremor(df, fs, n_freqs, thresholds, n_votes, is_calibration_on)

    return is_tremor

#only for movement detection
#only stay state
def isInPocket(acc_df):
    time_win_len = 200
    cols = acc_df.columns
    timestamps = acc_df[cols[0]].to_numpy().astype(int)
    if timestamps[len(timestamps) - 1] - timestamps[0] < time_win_len:
        return - 1
    acc_df = acc_df.astype('float64')
    acc_x = np.abs(np.array(acc_df[cols[1]]))
    acc_y = np.abs(np.array(acc_df[cols[2]]))
    acc_z = np.abs(np.array(acc_df[cols[3]]))

    sum_x = np.sum(acc_x)
    sum_y = np.sum(acc_y)
    sum_z = np.sum(acc_z)

    if sum_y > sum_x and sum_y > sum_z:
        return 1
    else:
        return 0

def is_in_pocket(csv_string):
    if csv_string == '':
        return -1

    list = [row.split(',') for row in csv_string.split('\n')]
    cols = ['time', 'acc_x', 'acc_y', 'acc_z']
    df_acc = pd.DataFrame(list, columns=cols)
    return isInPocket(df_acc)

def isMovement(gyro_df, time_win_len, movement_thresh_min, movement_thresh_max):
    cols = gyro_df.columns
    timestamps = gyro_df[cols[0]].to_numpy().astype(int)

    if timestamps[len(timestamps) - 1] - timestamps[0] < time_win_len:
        return - 1
    gyro_df = gyro_df.astype('float64')
    gyros = np.sqrt(gyro_df[cols[1]][:]**2 + gyro_df[cols[2]][:]**2 + gyro_df[cols[3]][:]**2)
    gyros_2 = gyros**2
    gyros_np = gyros.to_numpy()
    ind = np.argpartition(gyros, -2)[-2:]
    top2 = gyros[ind]
    top2_np = np.array(top2)
    #TODO:
    #1)need fourier for slow motions or fast tremor
    #2)need ml classifier based on feats

    #print(np.percentile(gyros_np, 15))
    #print(np.median(gyros_np))
    #print(np.percentile(gyros_np, 99))
    #energy = np.sum(gyros_2)
    #print(energy)
    #     print(top2_np)
    #     print(top2_np[0])
    #     print(top2_np[1])
    if top2_np[0] > movement_thresh_max and top2_np[1] > movement_thresh_max:
        return 1
    else:
        return 0

def is_movement(csv_string):
    #print(csv_string)
    time_win_len = 500
    movement_thresh_min = 0
    movement_thresh_max = 2.25#60
    #[row.split(',') for row in csv_string.split('\n')]
    if csv_string == '':
        return -1
    list1 = [row.split(',') for row in csv_string.split('\n')]
    #print(list)
    #print(df_acc)
    cols = ['time', 'gyro_x', 'gyro_y', 'gyro_z']
    df_gyro = pd.DataFrame(list1, columns=cols)
    #print(df_gyro)
    #print(df_acc.shape)
    return isMovement(df_gyro, time_win_len, movement_thresh_min, movement_thresh_max)
