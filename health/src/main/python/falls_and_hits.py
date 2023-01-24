import numpy as np
import pandas as pd
from datetime import datetime

def isFall(acc_df, gyro_df):

    #slice by time
    acc_cols = acc_df.columns
    acc_df[acc_cols[0]] = acc_df[acc_cols[0]].astype(int)
    acc_df[acc_cols[0]] = acc_df[acc_cols[0]] - acc_df[acc_cols[0]][0]
    acc_df = acc_df.set_index(acc_cols[0])
    acc_end_df = acc_df.loc[1000:]
    acc_end_df = acc_end_df.reset_index()
    acc_df = acc_df.loc[:1000]
    acc_df = acc_df.reset_index()

    mean_acc_end = np.mean(np.array(acc_end_df[acc_cols[2]].astype('float64')))

    time_win_len = 200
    cols = gyro_df.columns
    timestamps = gyro_df[cols[0]].to_numpy().astype(int)
    if timestamps[len(timestamps) - 1] - timestamps[0] < time_win_len:
        return - 1
    gyro_df = gyro_df.astype('float64')
    gyro_x = np.abs(np.array(gyro_df[cols[1]]))
    gyro_y = np.abs(np.array(gyro_df[cols[2]]))
    gyro_z = np.abs(np.array(gyro_df[cols[3]]))

    mx_x = np.max(gyro_x)
    mx_y = np.max(gyro_y)
    mx_z = np.max(gyro_z)
#     print(mx_x)
#     print(mx_y)
#     print(mx_z)
    #gyros = np.sqrt(gyro_df[cols[1]][:]**2 + gyro_df[cols[2]][:]**2 + gyro_df[cols[3]][:]**2)
    #print(acc_df)


    #df2 = acc_df.iloc[:5, :].astype('float64')

    df1 = acc_df.iloc[:5, :].astype('float64')
    preend = - 5
    #print(preend)
    df2 = acc_df.iloc[preend:, :].astype('float64')

    #print(df1)
    #print(df2)

    acc_df = acc_df.astype('float64')
    cols = acc_df.columns
    #print(acc_df[cols[1]][:]**2)
    accs1 = np.sqrt(acc_df[cols[1]][:]**2 + acc_df[cols[2]][:]**2 + acc_df[cols[3]][:]**2)
    #print(accs1)
    #accs2 = np.sqrt(df2[cols[1]][:]**2 + df2[cols[2]][:]**2 + df2[cols[3]][:]**2)
    acc_med = np.median(accs1)
    #print(acc_med)

    acc1_x = np.abs(np.array(df1[cols[1]]))
    acc1_y = np.abs(np.array(df1[cols[2]]))
    acc1_z = np.abs(np.array(df1[cols[3]]))

    acc2_x = np.abs(np.array(df2[cols[1]]))
    acc2_y = np.abs(np.array(df2[cols[2]]))
    acc2_z = np.abs(np.array(df2[cols[3]]))

    med1_x = np.median(acc1_x)
    med1_y = np.median(acc1_y)
    med1_z = np.median(acc1_z)

    #print(med1_y)

    med2_x = np.median(acc2_x)
    med2_y = np.median(acc2_y)
    med2_z = np.median(acc2_z)

    #print(med2_y)

    #print( 'v = ' + str(mx_x) + ' start = ' + str(med1_y) + ' div = ' + str(med1_y - med2_y))

    #print('vx = ' + str(mx_x) + ' vy = ' + str(mx_y))
    fall_gyro_thresh = 3.5
    fall_acc_thresh = 8.0
    #print(' div = ' + str(med1_y - med2_y))
    #print('med1_y = ' + str(med1_y) + ' med2_y = ' + str(med2_y))

    condition1 = mx_x > fall_gyro_thresh or mx_z > fall_gyro_thresh # максимальная угловая скорость гироскопа
    condition2 = med1_y - med2_y > fall_acc_thresh #разница конечного и начального положение
    condition3 = med1_y > 7.5 # конус начального положения
    #print('g = ' + str(np.min(accs1)))
    condition4 = np.min(accs1) < 2.5 #ускорение реакции опоры
    condition5 = mean_acc_end < 2.0 #не встает # не меняяется ускорение  по y
    #print('fall: ' + 'mean_acc_end = ' + str(mean_acc_end))
    #if condition1 and condition2 and condition3:
    if condition5 and condition4 and condition3 and condition2 and condition1:
        print('fall:')
        #print( 'w = ' + str(mx_x) + ' start = ' + str(med1_y) + ' div = ' + str(med1_y - med2_y))
        print('wx = ' + str(mx_x) + ' wy = ' + str(mx_y))
        print('med1_y = ' + str(med1_y) + ' med2_y = ' + str(med2_y))
        print('div = ' + str(med1_y - med2_y))
        print('g = ' + str(np.min(accs1)))
        return 1
    else:
        return 0


def is_fall(csv_string, csv_string1):
    #print(csv_string1)
    if csv_string == '' or csv_string1 == '':
            return -1
    #print(csv_string1)
    list = [row.split(',') for row in csv_string.split('\n')]
    list1 = [row.split(',') for row in csv_string1.split('\n')]

    cols = ['time', 'acc_x', 'acc_y', 'acc_z']
    df_acc = pd.DataFrame(list, columns=cols)
    cols = ['time', 'gyro_x', 'gyro_y', 'gyro_z']
    df_gyro = pd.DataFrame(list1, columns=cols)
    time1 = datetime.now().timestamp()
    res = isFall(df_acc, df_gyro)
    time2 = datetime.now().timestamp()
    #print('fall_alg_time = ' + str(time2 - time1))
    return res

def isHit(acc_df, gyro_df):
    is_log = 0
    hit_thresh = 40#9.5
    time_win_len = 40
    cols = acc_df.columns
    timestamps = acc_df[cols[0]].to_numpy().astype(int)
    #print(acc_df)
    if timestamps[len(timestamps) - 1] - timestamps[0] < time_win_len:
        if is_log:
            print('Hit: not enough data')
        return - 1

    #print(acc_df.shape[0])
    divider = int(np.floor(acc_df.shape[0]/2))
    df1 = acc_df.iloc[divider:, :].astype('float64')
    df2 = acc_df.iloc[:divider, :].astype('float64')

    #print(df2)
    #print(df1)

    accs1 = np.sqrt(df1[cols[1]][:]**2 + df1[cols[2]][:]**2 + df1[cols[3]][:]**2)
    accs2 = np.sqrt(df2[cols[1]][:]**2 + df2[cols[2]][:]**2 + df2[cols[3]][:]**2)

    accs1_np = accs1.to_numpy()
    accs2_np = accs2.to_numpy()

    #mx_1 = np.max(accs1)
    #mx_2 = np.max(accs2)

    #     print(mx_1)
    #     print(mx_2)



    energy1 = np.sum(accs1)/divider
    energy2 = np.sum(accs2)/divider

    #print(divider)
    #print(energy1)
    #print(energy2)
    if is_log:
        print('Hit: ' + str(energy2) + ' ' + str(energy1))
    if abs(energy2 - energy1) > hit_thresh:
        if is_log:
            print('hit')
        return 1
    else:
        if is_log:
            print('no_hit')
        return 0

    #print(df1)
    #print(df2)
    return 0

def is_hit(csv_string, csv_string1):
    if csv_string == '' or csv_string1 == '':
        #print('Hit: NO DATA')
        return -1
#     else:
    #print(csv_string)
    #print(csv_string)

    list = [row.split(',') for row in csv_string.split('\n')]
    list1 = [row.split(',') for row in csv_string1.split('\n')]

    cols = ['time', 'acc_x', 'acc_y', 'acc_z']
    df_acc = pd.DataFrame(list, columns=cols)
    cols = ['time', 'gyro_x', 'gyro_y', 'gyro_z']
    df_gyro = pd.DataFrame(list1, columns=cols)

    #print(df_acc)
    #print(df_gyro)
    time1 = datetime.now().timestamp()
    res = isHit(df_acc, df_gyro)
    time2 = datetime.now().timestamp()
    #print('hit_alg_time = ' + str(time2 - time1))
    return res