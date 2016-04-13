# -*- coding: utf-8 -*-

# some global setttings
settings = {}

#configured server listing (srv_list)
import os, pickle
global srv_list

plugin_dir = os.path.dirname(os.path.realpath(__file__))


    # read the sever names/urls from a file
def read_srv_list():
    insrvlst = os.path.join(plugin_dir, 'config_srvlist.pkl')
    fo = open(insrvlst, 'rb')
    sl = pickle.load(fo)
    fo.close()
    return sl


srv_list = read_srv_list()