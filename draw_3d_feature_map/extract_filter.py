import numpy as np
import matplotlib
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
import mpl_toolkits.mplot3d.art3d as art3d
import matplotlib.patches as patches
import matplotlib.colors as cr
from matplotlib import cm
import cv2 as cv
import os

import sys
caffe_root = 'C:/caffe/'  # this file should be run from {caffe_root}/examples (otherwise change this line)
sys.path.insert(0, caffe_root + 'python')

import caffe

caffe.set_mode_cpu()

model_def = 'test_resource/ResNet-50-DB1-test.prototxt'
model_weights = 'test_resource/lamp50_iter_850704.caffemodel'

net = caffe.Net(model_def,      # defines the structure of the model
                model_weights,  # contains the trained weights
                caffe.TEST)     # use test mode (e.g., don't perform dropout)


net.forward()

def extract_and_drawn_3dplot(layer_name, idx):
    original_feat = net.blobs[layer_name].data[idx] # get original_feature data
    show_range = 1000
    heigth_term = show_range * 1.3
    pad = 2
    fig = plt.figure()
    ax = fig.gca(projection='3d')
    ax._axis3don = False

    original_image = cv.resize(cv.imread('test_resource/S2001R01.bmp', -1), (224, 224))
    original_image = np.flipud(original_image)
    original_feat = original_feat.transpose(1, 2, 0).astype(np.uint8)
    original_feat = np.flipud(original_feat)
    original_feat = np.average(original_feat, axis=2)
    original_feat = cv.resize(original_feat, (228, 228))

    if pad is not 0:
        original_feat = original_feat[pad:-pad, pad:-pad]
    else:
        pass

    X = range(0, original_feat.shape[0], 1)
    Y = range(0, original_feat.shape[1], 1)
    X, Y = np.meshgrid(X, Y)
    Z = np.copy(original_feat)
    Z = (Z - Z.min()) / (Z.max() - Z.min()) * 255
    surf = ax.plot_surface(X, Y, Z, cmap=cm.gray, linewidth=0, antialiased=False)#, rstride=3, cstride=3)
    # -----------------------------------------------------------------------------------------------------
    original_feat = original_feat.reshape(original_feat.shape[0], original_feat.shape[1], 1)
    base_color_data = np.copy(original_image) # 0~1 float64 (gray)

    XX = range(0, base_color_data.shape[0], 1)
    YY = range(0, base_color_data.shape[1], 1)
    XX, YY = np.meshgrid(XX, YY)
    ZZ = np.zeros((base_color_data.shape[0], base_color_data.shape[1]), np.float64)-heigth_term
    # base_color_data = ((base_color_data - base_color_data.min()) / (base_color_data.max() - base_color_data.min()) * 255) # 0 ~ 255 float64 (gray)
    for_color = np.copy(base_color_data).astype(np.uint8) # 0 ~ 255 uint8 (gray)
    rgba_feat = cv.cvtColor(for_color, cv.COLOR_GRAY2RGBA).astype(np.float64) # 0~255 uint8 (r, g, b, a)
    rgba_feat = (rgba_feat - rgba_feat.min()) / (rgba_feat.max() - rgba_feat.min())
    # surf2 = ax.plot_surface(XX, YY, np.zeros((feat.shape[0], feat.shape[1]), dtype=np.uint8), facecolors=feat2)
    surf2 = ax.plot_surface(XX, YY, ZZ, linewidth=0, antialiased=False, facecolors=rgba_feat)
    # need 0~1 float64 type to above line.

    # for saving

    ax.set_zlim3d(-show_range, show_range)
    if os.path.exists('3D/'+layer_name) is False:
        os.mkdir('3D/'+layer_name)
    plt.savefig('3D/'+layer_name+'/'+str(idx)+'_3D.png')
    if os.path.exists('feat/'+layer_name) is False:
        os.mkdir('feat/'+layer_name)
    original_feat = (original_feat - original_feat.min()) / (original_feat.max() - original_feat.min()) * 255
    cv.imwrite('feat/'+layer_name+'/'+str(idx)+'.png', original_feat)
    # average_feat = (average_feat - average_feat.min()) / (average_feat.max() - average_feat.min()) * 255
    # cv.imwrite('image.bmp', average_feat)


for idx in range(3):
    extract_and_drawn_3dplot('conv1', idx)
    extract_and_drawn_3dplot('res2a', idx)
    extract_and_drawn_3dplot('res2b', idx)
    extract_and_drawn_3dplot('res2c', idx)
    extract_and_drawn_3dplot('res3d', idx)
    extract_and_drawn_3dplot('res4f', idx)
    extract_and_drawn_3dplot('res5a', idx)
    extract_and_drawn_3dplot('res5b', idx)
    extract_and_drawn_3dplot('res5c', idx)
    print(str(idx)+'done')
#
