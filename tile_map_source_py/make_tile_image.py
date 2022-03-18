import cv2
import numpy as np
import matplotlib.pyplot as plt
import os
from pprint import pprint

## cols, rows
'''
    self variables:
        width, height, cols, rows, directory
        cols & rows, set automatically

        result images width size is below 1000px.

'''

class TileMap:
    def __init__(self, load_dirs):
        self.root = load_dirs
        self.padding = 3

    def calcColRow(self):
        self.rows = np.ceil(self.num_imgs)
        self.cols = 1
        for col in range(2, 101, 1):
            tempRows = int(np.ceil(self.num_imgs/col))
            print(col)
            if(col*(self.width+self.padding) < 1300):
                if(self.rows > tempRows):
                    self.rows = tempRows
                    self.cols = col
                elif(self.rows == tempRows):
                    self.rows = tempRows
                    self.cols = col

    def loadIist(self):
        img_list = []

        with open(self.root, 'r') as files:
            for i in files:
                line = i.replace('\n', '')
                img_list.append(line)
        self.img_list = img_list
    
    def loadImages(self):
        imgs = []
        for j in self.img_list:
            img = cv2.imread(j)
            imgs.append(img)
            

        self.width = imgs[0].shape[1]
        self.height = imgs[0].shape[0]
        self.imgs = imgs
        self.num_imgs = len(imgs)

    def getTileMap(self):
        self.calcColRow()
        imgTile = np.zeros(((self.rows*(self.height+self.padding))+self.padding, (self.cols*(self.width+self.padding))+self.padding, 3), dtype=np.uint8)
        imgTile = imgTile+150
        return imgTile

    def add2Tile(self):
        tile = self.getTileMap()
        temp_tiles = []

        for img in self.imgs:
            temp_tile = np.zeros((self.height+self.padding, self.width+self.padding, 3), np.uint8)
            temp_tile = temp_tile + 150
            temp_tile[self.padding:,self.padding:] = img
            temp_tiles.append(temp_tile)
            
        #print("img count : ", len(temp_tiles))
        
        cnt = 0
        for i in range(0, self.rows):
            for j in range(0, self.cols):
                if (cnt != self.num_imgs):
                    a = self.height+self.padding
                    b = self.width+self.padding
                    #print(self.rows, self.cols, j*a, i*b)
                    sx, sy = j*a, i*b
                   # print(cnt)
                    tile[sy:sy+a, sx:sx+b] = temp_tiles[cnt]
##                    cv2.imshow("temp", temp_tiles[cnt])
##                    cv2.waitKey(1000)
##                    cv2.destroyWindow("temp")
                    
                    
                    cnt += 1

        self.tile = tile

    def saveResultTile(self):
        cv2.imwrite('result_tile_image.bmp', self.tile)

tile = TileMap("test_resource/list.txt")
tile.loadIist()
tile.loadImages()
tile.getTileMap()
tile.add2Tile()
tile.saveResultTile()
